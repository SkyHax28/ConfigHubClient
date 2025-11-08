package com.dew.system.module.modules.visual;

import com.dew.DewCommon;
import com.dew.IMinecraft;
import com.dew.system.event.events.ReceivedPacketEvent;
import com.dew.system.event.events.Render2DEvent;
import com.dew.system.event.events.SendPacketEvent;
import com.dew.system.event.events.LoadWorldEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.module.modules.combat.Aura;
import com.dew.system.module.modules.other.MurdererDetector;
import com.dew.system.module.modules.exploit.Plugins;
import com.dew.system.module.modules.ghost.BridgeAssist;
import com.dew.system.module.modules.player.Breaker;
import com.dew.system.module.modules.player.CivBreak;
import com.dew.system.module.modules.player.Scaffold;
import com.dew.system.settingsvalue.BooleanValue;
import com.dew.system.settingsvalue.MultiSelectionValue;
import com.dew.system.settingsvalue.NumberValue;
import com.dew.system.settingsvalue.SelectionValue;
import com.dew.system.userdata.DataSaver;
import com.dew.system.viapatcher.PlayerFixer;
import com.dew.utils.*;
import com.dew.utils.font.CustomFontRenderer;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.WorldSettings;
import org.lwjgl.BufferUtils;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL15;

import java.awt.*;
import java.nio.FloatBuffer;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class Hud extends Module {

    private static final MultiSelectionValue features = new MultiSelectionValue("Features", Arrays.asList("Watermark", "Water Info", "Module List", "Potion Hud", "Target Hud", "Armor Hud", "Packet Monitor"), "Watermark", "Water Info", "Module List", "Potion Hud", "Target Hud", "Armor Hud", "Packet Monitor");
    private static final SelectionValue watermarkPosition = new SelectionValue("Watermark Position", "Left Top", () -> features.isSelected("Watermark"), "Left Top", "Center Top");
    private static final NumberValue uiScale = new NumberValue("UI Scale", 1.0, 0.5, 2.0, 0.1);
    private static final BooleanValue disableAchievementsNotification = new BooleanValue("Disable Achievements Notification", true);
    private final Map<Module, Float> animationProgress = new HashMap<>();
    private long lastRenderTime = System.nanoTime();
    private final List<Module> cachedSortedModules = new ArrayList<>();
    private final Map<Module, Float> cachedWidths = new HashMap<>();
    private float watermarkWidthLerp = 0f;
    private float combinedTextWidthLerp = 0f;
    private float targetHpLerp = 0f;
    private boolean targetHudVisible = false;
    private float targetHudAnimationProgress = 0f;
    private boolean moduleListDirty = true;
    private int finalModuleListHeight = 0;
    private static int unitQuadVbo = 0;
    private static final int MAX_HISTORY = 120;
    private int inboundThisTick = 0;
    private int outboundThisTick = 0;
    private float timeSinceLastUpdate = 0f;
    private final Deque<Integer> inboundHeights = new ArrayDeque<>();
    private final Deque<Integer> outboundHeights = new ArrayDeque<>();
    private final Deque<Integer> inboundPackets = new ArrayDeque<>();
    private final Deque<Integer> outboundPackets = new ArrayDeque<>();

    private float bloodSplatterAnimation = 0f;
    private final Random random = new Random();
    private final List<BloodParticle> bloodParticles = new ArrayList<>();

    public Hud() {
        super("Hud", ModuleCategory.VISUAL, Keyboard.KEY_NONE, true, false, true);
    }

    @Override
    public void onDisable() {
        deleteUnitQuadVbo();
    }

    @Override
    public void onLoadWorld(LoadWorldEvent event) {
        deleteUnitQuadVbo();
        inboundThisTick = 0;
        outboundThisTick = 0;
        timeSinceLastUpdate = 0f;
        inboundHeights.clear();
        outboundHeights.clear();
        inboundPackets.clear();
        outboundPackets.clear();
        bloodParticles.clear();
    }

    public int getModuleListHeight() {
        return this.isEnabled() && features.isSelected("Module List") ? finalModuleListHeight / 2 : 0;
    }

    public void markModuleListDirty() {
        moduleListDirty = true;
    }

    private void updateModuleListCache(CustomFontRenderer fontRenderer, float fontSize) {
        if (!moduleListDirty) return;
        cachedSortedModules.clear();

        for (Module m : DewCommon.moduleManager.getModules()) {
            if (m.showOnArray) {
                float width = fontRenderer.getStringWidth(
                        m.name + (!m.tag().isEmpty() ? " " + m.tag() : ""),
                        fontSize
                );
                cachedWidths.put(m, width);
                cachedSortedModules.add(m);
            }
        }

        cachedSortedModules.sort((a, b) -> Float.compare(
                cachedWidths.get(b),
                cachedWidths.get(a)
        ));

        moduleListDirty = false;
    }

    public void updateTargetHudVisibility(boolean shouldShow, float deltaTime) {
        if (shouldShow) {
            targetHudVisible = true;
            targetHudAnimationProgress += 6f * deltaTime;
            if (targetHudAnimationProgress > 1f) targetHudAnimationProgress = 1f;
        } else {
            targetHudAnimationProgress -= 6f * deltaTime;
            if (targetHudAnimationProgress < 0f) {
                targetHudAnimationProgress = 0f;
                targetHudVisible = false;
            }
        }
    }

    private void drawJaggedEdge(int x, int y, int width, int height, int color) {
        int segments = width / 4;
        for (int i = 0; i < segments; i++) {
            int x1 = x + i * 4;
            int y1 = y + (i % 2 == 0 ? 0 : 2);
            Gui.drawRect(x1, y1, x1 + 4, y1 + height, color);
        }
    }

    private void drawBloodSplatter(int x, int y, float alpha) {
        int splatterColor = new Color(180, 0, 0, (int)(alpha * 100)).getRGB();

        for (int i = 0; i < 3; i++) {
            int offsetX = random.nextInt(10) - 5;
            int offsetY = random.nextInt(10) - 5;
            int size = 2 + random.nextInt(3);
            Gui.drawRect(x + offsetX, y + offsetY, x + offsetX + size, y + offsetY + size, splatterColor);
        }
    }

    private void renderModuleList(ScaledResolution sr, CustomFontRenderer fontRenderer, float fontSize, float deltaTime) {
        updateModuleListCache(fontRenderer, fontSize);

        int rightEdgeX = sr.getScaledWidth() - 12;

        float cumulativeY = 0;
        for (Module module : cachedSortedModules) {
            boolean enabled = module.isEnabled();
            float current = animationProgress.getOrDefault(module, enabled ? 0f : 1f);
            float target = enabled ? 1f : 0f;
            float animationSpeed = 18f * deltaTime;

            float interpolated = Lerper.lerp(current, target, animationSpeed);
            animationProgress.put(module, interpolated);

            if (interpolated <= 0.25f) continue;

            String moduleName = module.name;
            String tag = module.tag();
            String display = moduleName + (!tag.isEmpty() ? " " + tag : "");

            Color accentColor = new Color(
                    Math.min(255, 200 + (int)(Math.sin(System.currentTimeMillis() * 0.001 + cumulativeY * 0.1) * 55)),
                    20,
                    20
            );

            float displayWidth = cachedWidths.getOrDefault(module, fontRenderer.getStringWidth(display, fontSize));
            float displayHeight = 14;

            float slideOffset = (1.0f - interpolated) * 10;
            float finalY = cumulativeY + slideOffset + 12;

            int bgLeft = (int) (rightEdgeX - displayWidth - 6);
            int bgTop = (int) (finalY - 1);
            int bgBottom = (int) (finalY + displayHeight);

            int alpha = Math.max(0, Math.min(255, (int) (255 * interpolated)));

            Color bgColor = new Color(5, 5, 5, Math.max(0, Math.min(255, (int) (220 * interpolated))));

            drawBlurredBackground(
                    bgLeft + 1, bgTop + 1,
                    (rightEdgeX + 1) - (bgLeft + 1),
                    bgBottom - bgTop - 2,
                    5,
                    bgColor.getRGB()
            );

            drawJaggedEdge(bgLeft, bgTop, 2, bgBottom - bgTop, new Color(200, 0, 0, alpha).getRGB());

            float xMultiplier = 2.5f;

            if (!tag.isEmpty()) {
                float tagWidth = fontRenderer.getStringWidth(" " + tag, fontSize);
                float nameWidth = fontRenderer.getStringWidth(moduleName, fontSize);
                float tagX = rightEdgeX - tagWidth;
                float nameX = tagX - nameWidth;
                Color nameColor = new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), alpha);
                Color tagColor = new Color(100, 100, 100, alpha);
                fontRenderer.drawStringWithShadow(moduleName, nameX - xMultiplier, finalY - 0.5f, nameColor.getRGB(), fontSize);
                fontRenderer.drawStringWithShadow(" " + tag, tagX - xMultiplier, finalY - 0.5f, tagColor.getRGB(), fontSize);
            } else {
                float nameWidth = fontRenderer.getStringWidth(moduleName, fontSize);
                float nameX = rightEdgeX - nameWidth;
                Color nameColor = new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), alpha);
                fontRenderer.drawStringWithShadow(moduleName, nameX - xMultiplier, finalY - 0.5f, nameColor.getRGB(), fontSize);
            }

            cumulativeY += displayHeight + 1;
        }

        finalModuleListHeight = (int) cumulativeY;
    }

    private static void ensureUnitQuadVbo() {
        if (unitQuadVbo != 0) return;
        FloatBuffer buf = BufferUtils.createFloatBuffer(8);
        buf.put(0f).put(0f);
        buf.put(0f).put(1f);
        buf.put(1f).put(1f);
        buf.put(1f).put(0f);
        buf.flip();

        unitQuadVbo = GL15.glGenBuffers();
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, unitQuadVbo);
        GL15.glBufferData(GL15.GL_ARRAY_BUFFER, buf, GL15.GL_STATIC_DRAW);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);
    }

    private static void deleteUnitQuadVbo() {
        if (unitQuadVbo != 0) {
            GL15.glDeleteBuffers(unitQuadVbo);
            unitQuadVbo = 0;
        }
    }

    private static void drawBlurredBackground(double x, double y, double width, double height, int passes, int color) {
        if (passes <= 0 || width <= 0 || height <= 0) return;

        ensureUnitQuadVbo();

        float a = (color >>> 24 & 255) / 255f;
        float r = (color >>> 16 & 255) / 255f;
        float g = (color >>>  8 & 255) / 255f;
        float b = (color         & 255) / 255f;

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.color(r, g, b, a);

        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, unitQuadVbo);
        GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
        GL11.glVertexPointer(2, GL11.GL_FLOAT, 0, 0L);

        for (int i = 0; i < passes; i++) {
            double off = i * 0.5;
            GlStateManager.pushMatrix();
            GlStateManager.translate((float)(x - off), (float)(y - off), 0f);
            GlStateManager.scale((float)(width + off * 2), (float)(height + off * 2), 1f);
            GL11.glDrawArrays(GL11.GL_QUADS, 0, 4);
            GlStateManager.popMatrix();
        }

        GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);
        GL15.glBindBuffer(GL15.GL_ARRAY_BUFFER, 0);

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    public boolean disableAchievementsUI() {
        return disableAchievementsNotification.get();
    }

    @Override
    public void onSendPacket(SendPacketEvent event) {
        if (event.isCancelled()) return;
        inboundThisTick++;
    }

    @Override
    public void onReceivedPacket(ReceivedPacketEvent event) {
        if (event.isCancelled()) return;
        outboundThisTick++;
    }

    @Override
    public void onRender2D(Render2DEvent event) {
        ScaledResolution sr = new ScaledResolution(mc);

        long now = System.nanoTime();
        float deltaTime = (now - lastRenderTime) / 1_000_000_000.0f;
        lastRenderTime = now;
        deltaTime = Math.min(deltaTime, 0.05f);

        bloodSplatterAnimation += deltaTime * 2f;

        CustomFontRenderer fontRenderer = DewCommon.customFontRenderer;
        float fontSize = 0.35f;

        float scale = uiScale.get().floatValue() - 0.2f;

        if (features.isSelected("Watermark")) {
            GL11.glPushMatrix();
            float glXPos = watermarkPosition.get().equals("Left Top") ? 0f : sr.getScaledWidth() / 2f;
            float topY = 0f;
            GL11.glTranslatef(glXPos, topY, 0);
            GL11.glScalef(scale, scale, 1f);
            GL11.glTranslatef(-glXPos, -topY, 0);

            String clientName = DewCommon.clientName;
            String renderVersion = IMinecraft.mc.isSingleplayer() ? ProtocolVersion.v1_8.getName() : ViaLoadingBase.getInstance().getTargetVersion().getName();
            String userInfo = " | " + DataSaver.userName + " | " + Minecraft.getDebugFPS() + " fps | " + (mc.isSingleplayer() ? "SP" : "MP") + " | " + renderVersion + " | " + PacketUtil.getCurrentPingOrMinusOne() + "ms" + (mc.isSingleplayer() ? "" : DewCommon.mongoManager.isConnected() && DewCommon.mongoManager.online.stream().anyMatch(p -> p.getLeft().equals(mc.thePlayer) && p.getRight().equals(DataSaver.userName)) ? " | Connected" : " | Connecting...");
            String display = clientName + userInfo;

            float targetWidth = fontRenderer.getStringWidth(display, fontSize);
            watermarkWidthLerp = Lerper.lerp(watermarkWidthLerp, targetWidth, 10f * deltaTime);

            float displayHeight = 14f;
            int x = watermarkPosition.get().equals("Left Top") ? 10 : (int) ((sr.getScaledWidth() - watermarkWidthLerp) / 2);
            int y = watermarkPosition.get().equals("Center Top") && BossStatus.bossName != null && BossStatus.statusBarTime > 0 ? 28 : 8;

            Color bgColor = new Color(5, 5, 5, 220);
            drawBlurredBackground(
                    x - 3, y - 2,
                    watermarkWidthLerp + 6, displayHeight,
                    6,
                    bgColor.getRGB()
            );

            Gui.drawRect(x - 3, y - 2, (int)(x + watermarkWidthLerp + 3), y - 1, new Color(180, 0, 0, 255).getRGB());
            Gui.drawRect(x - 3, (int)(y + displayHeight - 2), (int)(x + watermarkWidthLerp + 3), (int)(y + displayHeight - 1), new Color(180, 0, 0, 255).getRGB());

            float charX = x;
            for (int i = 0; i < display.length(); i++) {
                char c = display.charAt(i);
                int red = 200 + (int)(Math.sin(System.currentTimeMillis() * 0.002 + i * 0.3) * 55);
                Color accentColor = new Color(Math.min(255, red), 20, 20);
                fontRenderer.drawStringWithShadow(
                        String.valueOf(c),
                        charX,
                        y - 1.5f,
                        accentColor.getRGB(),
                        fontSize
                );
                charX += fontRenderer.getStringWidth(String.valueOf(c), fontSize);
            }

            GL11.glPopMatrix();
        }

        if (features.isSelected("Water Info")) {
            float displayHeight = 14f;
            int y = BossStatus.bossName != null && BossStatus.statusBarTime > 0 ? 28 : 8;

            if (mc.thePlayer != null) {
                GL11.glPushMatrix();
                float centerX = sr.getScaledWidth() / 2f;
                float topY = 0f;
                GL11.glTranslatef(centerX, topY, 0);
                GL11.glScalef(scale, scale, 1f);
                GL11.glTranslatef(-centerX, -topY, 0);

                String burningText = mc.thePlayer.isBurning() ? "Burning" : "";
                String blockInfo = DewCommon.moduleManager.getModule(Scaffold.class).isEnabled() || DewCommon.moduleManager.getModule(BridgeAssist.class).isEnabled() && DewCommon.moduleManager.getModule(BridgeAssist.class).isBridging() ? this.getTotalValidBlocksInHotbar() + " blocks available" : "";

                List<EntityPlayer> murderers = DewCommon.moduleManager.getModule(MurdererDetector.class).getMurderers();
                String murdererInfo = "";
                if (DewCommon.moduleManager.getModule(MurdererDetector.class).isEnabled() && !murderers.isEmpty()) {
                    String names = murderers.stream()
                            .map(EntityPlayer::getName)
                            .collect(Collectors.joining(", "));

                    murdererInfo = "Murderers: " + murderers.size() + " (" + names + ")";
                }

                String breakingInfo = DewCommon.moduleManager.getModule(Breaker.class).isEnabled() && DewCommon.moduleManager.getModule(Breaker.class).isBreaking || DewCommon.moduleManager.getModule(CivBreak.class).isEnabled() && DewCommon.moduleManager.getModule(CivBreak.class).isBreaking ? "Breaking..." : "";
                String timerInfo = mc.timer.timerSpeed != 1 ? "Balance: " + mc.timer.timerSpeed : "";
                String blinkInfo = BlinkUtil.blinking ? "Blinking... (x" +  BlinkUtil.packets.size() + ")": "";
                String deadInfo = mc.thePlayer.isDead ? "Wasted!" : "";
                String swimmingInfo = PlayerFixer.shouldSwimOrCrawl() ? "Swimming" : "";
                String pluginsInfo = DewCommon.moduleManager.getModule(Plugins.class).isEnabled() ? "Detecting... (" + DewCommon.moduleManager.getModule(Plugins.class).getClockMs() + "ms passed)" : "";
                String spectatingInfo = mc.playerController != null && mc.playerController.getCurrentGameType() == WorldSettings.GameType.SPECTATOR ? "Spectating" : "";

                String combinedText = "";
                if (!burningText.isEmpty()) combinedText += burningText;
                if (!blockInfo.isEmpty()) combinedText += (combinedText.isEmpty() ? "" : "  ") + blockInfo;
                if (!murdererInfo.isEmpty()) combinedText += (combinedText.isEmpty() ? "" : "  ") + murdererInfo;
                if (!breakingInfo.isEmpty()) combinedText += (combinedText.isEmpty() ? "" : "  ") + breakingInfo;
                if (!timerInfo.isEmpty()) combinedText += (combinedText.isEmpty() ? "" : "  ") + timerInfo;
                if (!blinkInfo.isEmpty()) combinedText += (combinedText.isEmpty() ? "" : "  ") + blinkInfo;
                if (!deadInfo.isEmpty()) combinedText += (combinedText.isEmpty() ? "" : "  ") + deadInfo;
                if (!swimmingInfo.isEmpty()) combinedText += (combinedText.isEmpty() ? "" : "  ") + swimmingInfo;
                if (!pluginsInfo.isEmpty()) combinedText += (combinedText.isEmpty() ? "" : "  ") + pluginsInfo;
                if (!spectatingInfo.isEmpty()) combinedText += (combinedText.isEmpty() ? "" : "  ") + spectatingInfo;

                float lerpSpeed = Math.min(10f * deltaTime, 1f);

                if (!combinedText.isEmpty()) {
                    float targetCombinedWidth = fontRenderer.getStringWidth(combinedText, fontSize);
                    combinedTextWidthLerp = Lerper.lerp(combinedTextWidthLerp, targetCombinedWidth, lerpSpeed);

                    displayHeight += 2;

                    Color bgColor = new Color(5, 5, 5, 220);
                    drawBlurredBackground(
                            (sr.getScaledWidth() - combinedTextWidthLerp) / 2f - 3,
                            y + displayHeight + 2,
                            combinedTextWidthLerp + 6, 12f,
                            6,
                            bgColor.getRGB()
                    );

                    Gui.drawRect((int)((sr.getScaledWidth() - combinedTextWidthLerp) / 2f - 3), (int)(y + displayHeight + 2),
                            (int)((sr.getScaledWidth() + combinedTextWidthLerp) / 2f + 3), (int)(y + displayHeight + 3),
                            new Color(180, 0, 0, 255).getRGB());

                    float xPos = (sr.getScaledWidth() - combinedTextWidthLerp) / 2f;

                    if (!burningText.isEmpty()) {
                        fontRenderer.drawString(
                                burningText,
                                xPos,
                                y + displayHeight + 2.5f,
                                new Color(255, 80, 80, 255).getRGB(),
                                fontSize
                        );
                        xPos += fontRenderer.getStringWidth(burningText + "  ", fontSize);
                    }

                    if (!blockInfo.isEmpty()) {
                        fontRenderer.drawString(
                                blockInfo,
                                xPos,
                                y + displayHeight + 2.5f,
                                new Color(200, 200, 200, 255).getRGB(),
                                fontSize
                        );
                        xPos += fontRenderer.getStringWidth(blockInfo + "  ", fontSize);
                    }

                    if (!murdererInfo.isEmpty()) {
                        fontRenderer.drawString(
                                murdererInfo,
                                xPos,
                                y + displayHeight + 2.5f,
                                new Color(255, 0, 0, 255).getRGB(),
                                fontSize
                        );
                        xPos += fontRenderer.getStringWidth(murdererInfo + "  ", fontSize);
                    }

                    if (!breakingInfo.isEmpty()) {
                        fontRenderer.drawString(
                                breakingInfo,
                                xPos,
                                y + displayHeight + 2.5f,
                                new Color(255, 50, 50, 255).getRGB(),
                                fontSize
                        );
                        xPos += fontRenderer.getStringWidth(breakingInfo + "  ", fontSize);
                    }

                    if (!timerInfo.isEmpty()) {
                        fontRenderer.drawString(
                                timerInfo,
                                xPos,
                                y + displayHeight + 2.5f,
                                new Color(200, 0, 0, 255).getRGB(),
                                fontSize
                        );
                        xPos += fontRenderer.getStringWidth(timerInfo + "  ", fontSize);
                    }

                    if (!blinkInfo.isEmpty()) {
                        fontRenderer.drawString(
                                blinkInfo,
                                xPos,
                                y + displayHeight + 2.5f,
                                new Color(180, 0, 0, 255).getRGB(),
                                fontSize
                        );
                        xPos += fontRenderer.getStringWidth(blinkInfo + "  ", fontSize);
                    }

                    if (!deadInfo.isEmpty()) {
                        fontRenderer.drawString(
                                deadInfo,
                                xPos,
                                y + displayHeight + 2.5f,
                                new Color(255, 0, 0, 255).getRGB(),
                                fontSize
                        );
                        xPos += fontRenderer.getStringWidth(deadInfo + "  ", fontSize);
                    }

                    if (!swimmingInfo.isEmpty()) {
                        fontRenderer.drawString(
                                swimmingInfo,
                                xPos,
                                y + displayHeight + 2.5f,
                                new Color(150, 0, 0, 255).getRGB(),
                                fontSize
                        );
                        xPos += fontRenderer.getStringWidth(swimmingInfo + "  ", fontSize);
                    }

                    if (!pluginsInfo.isEmpty()) {
                        fontRenderer.drawString(
                                pluginsInfo,
                                xPos,
                                y + displayHeight + 2.5f,
                                new Color(200, 100, 0, 255).getRGB(),
                                fontSize
                        );
                        xPos += fontRenderer.getStringWidth(pluginsInfo + "  ", fontSize);
                    }

                    if (!spectatingInfo.isEmpty()) {
                        fontRenderer.drawString(
                                spectatingInfo,
                                xPos,
                                y + displayHeight + 2.5f,
                                new Color(150, 150, 150, 255).getRGB(),
                                fontSize
                        );
                    }
                } else {
                    combinedTextWidthLerp = Lerper.lerp(combinedTextWidthLerp, 0f, lerpSpeed);

                    if (combinedTextWidthLerp > 5f) {
                        Color bgColor = new Color(5, 5, 5, 220);
                        drawBlurredBackground(
                                (sr.getScaledWidth() - combinedTextWidthLerp) / 2f - 3,
                                y + displayHeight + 2,
                                combinedTextWidthLerp + 6, 12f,
                                6,
                                bgColor.getRGB()
                        );

                        Gui.drawRect((int)((sr.getScaledWidth() - combinedTextWidthLerp) / 2f - 3), (int)(y + displayHeight + 2),
                                (int)((sr.getScaledWidth() + combinedTextWidthLerp) / 2f + 3), (int)(y + displayHeight + 3),
                                new Color(180, 0, 0, 255).getRGB());
                    }
                }

                GL11.glPopMatrix();
            }
        }

        if (features.isSelected("Potion Hud")) {
            GL11.glPushMatrix();
            float rightX = sr.getScaledWidth();
            float bottomY = sr.getScaledHeight();
            GL11.glTranslatef(rightX, bottomY, 0);
            GL11.glScalef(scale, scale, 1f);
            GL11.glTranslatef(-rightX, -bottomY, 0);

            Collection<PotionEffect> effects = mc.thePlayer.getActivePotionEffects();
            if (!effects.isEmpty()) {
                int index = 0;
                int x = sr.getScaledWidth() - 4;

                for (PotionEffect effect : effects) {
                    Potion potion = Potion.potionTypes[effect.getPotionID()];
                    if (potion == null) continue;

                    String potionName = StatCollector.translateToLocal(potion.getName()) + " " + toRoman(effect.getAmplifier() + 1);
                    potionName = StringHelper.removeMcColorCodes(potionName);
                    int durationTicks = effect.getDuration();
                    String durationStr = Potion.getDurationString(effect);

                    Color durationColor = durationTicks <= 200 ? new Color(255, 0, 0) : durationTicks <= 600 ? new Color(200, 50, 0) : new Color(200, 200, 200);
                    int nameWidth = (int) fontRenderer.getStringWidth(potionName, fontSize);
                    int durationWidth = (int) fontRenderer.getStringWidth(durationStr, fontSize);
                    int widest = Math.max(nameWidth, durationWidth);

                    int boxHeight = 26;
                    int y = sr.getScaledHeight() - 40 - index * (boxHeight + 4) - (features.isSelected("Packet Monitor") ? 54 : 0) - (mc.currentScreen instanceof GuiChat ? 20 : 0);

                    drawBlurredBackground(x - widest - 12, y + 2, widest + 8, boxHeight, 4, new Color(5, 5, 5, 220).getRGB());

                    Gui.drawRect(x - widest - 12, y + 2, x - widest - 11, y + boxHeight + 2, new Color(180, 0, 0, 255).getRGB());

                    fontRenderer.drawStringWithShadow(potionName, x - widest - 8, y + 2, new Color(200, 50, 50).getRGB(), fontSize);
                    fontRenderer.drawStringWithShadow(durationStr, x - durationWidth - 8, y + 14, durationColor.getRGB(), fontSize);

                    index++;
                }
            }

            GL11.glPopMatrix();
        }

        if (features.isSelected("Target Hud")) {
            GL11.glPushMatrix();
            float centerX = sr.getScaledWidth() / 2f;
            float topY = 0f;
            GL11.glTranslatef(centerX, topY, 0);
            GL11.glScalef(scale, scale, 1f);
            GL11.glTranslatef(-centerX, -topY, 0);

            Aura auraModule = DewCommon.moduleManager.getModule(Aura.class);
            boolean shouldShow = auraModule.isEnabled() && auraModule.target instanceof EntityLivingBase;
            this.updateTargetHudVisibility(shouldShow, deltaTime);
            if (targetHudVisible) {
                EntityLivingBase target = auraModule.target != null ? (EntityLivingBase) auraModule.target : null;

                int width = 140;
                int baseX = sr.getScaledWidth() / 2 - 140 / 2;
                int baseY = BossStatus.bossName != null && BossStatus.statusBarTime > 0 ? 75 : 55;

                float slideOffsetY = -10 * (1f - targetHudAnimationProgress);
                int baseAlpha = 220;
                int animatedAlpha = (int)(baseAlpha * targetHudAnimationProgress);
                int y = baseY + (int) slideOffsetY;

                Color bgColor = new Color(5, 5, 5, animatedAlpha);
                drawBlurredBackground(baseX, y, 140, 44, 8, bgColor.getRGB());

                int borderAlpha = (int)(255 * targetHudAnimationProgress);
                Gui.drawRect(baseX, y, baseX + 140, y + 1, new Color(180, 0, 0, borderAlpha).getRGB());
                Gui.drawRect(baseX, y + 43, baseX + 140, y + 44, new Color(180, 0, 0, borderAlpha).getRGB());

                drawJaggedEdge(baseX, y + 1, 140, 2, new Color(150, 0, 0, borderAlpha).getRGB());

                if (target != null) {
                    String name = target.getName();
                    Color nameColor = new Color(200, 50, 50, (int)(255 * targetHudAnimationProgress));
                    fontRenderer.drawStringWithShadow(
                            name,
                            baseX + 40,
                            y + 3.5f,
                            nameColor.getRGB(),
                            fontSize
                    );
                }

                float targetHpRatio = target != null ? Math.max(0, Math.min(1, target.getHealth() / target.getMaxHealth())) : 0;
                targetHpLerp = Lerper.lerp(targetHpLerp, targetHpRatio, 10f * deltaTime);

                int hpBarWidth = (int) (targetHpLerp * (width - 50));

                for (int i = 0; i < hpBarWidth; i++) {
                    int red = 180 + (int)(Math.sin(i * 0.1) * 75);
                    Color hpColor = new Color(Math.min(255, red), 0, 0, (int)(200 * targetHudAnimationProgress));
                    Gui.drawRect(
                            baseX + 40 + i,
                            y + 17,
                            baseX + 40 + i + 1,
                            y + 25,
                            hpColor.getRGB()
                    );
                }

                Gui.drawRect(baseX + 39, y + 16, baseX + 40 + (width - 50), y + 17, new Color(30, 0, 0, (int)(150 * targetHudAnimationProgress)).getRGB());
                Gui.drawRect(baseX + 39, y + 25, baseX + 40 + (width - 50), y + 26, new Color(30, 0, 0, (int)(150 * targetHudAnimationProgress)).getRGB());

                if (target != null) {
                    Color hpTextColor = new Color(255, 100, 100, (int) (255 * targetHudAnimationProgress));
                    fontRenderer.drawStringWithShadow(
                            String.format("%.1f", target.getHealth()),
                            baseX + 45 + hpBarWidth,
                            y + 14,
                            hpTextColor.getRGB(),
                            fontSize
                    );
                }

                if (target instanceof AbstractClientPlayer) {
                    GL11.glPushMatrix();
                    GL11.glEnable(GL11.GL_BLEND);
                    GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
                    GL11.glDisable(GL11.GL_LIGHTING);
                    GL11.glEnable(GL11.GL_ALPHA_TEST);
                    GL11.glColor4f(1f, 1f, 1f, targetHudAnimationProgress);

                    ResourceLocation skin = ((AbstractClientPlayer) target).getLocationSkin();
                    mc.getTextureManager().bindTexture(skin);

                    Gui.drawScaledCustomSizeModalRect(
                            baseX + 4, y + 6,
                            8, 8, 8, 8,
                            32, 32,
                            64, 64
                    );

                    Gui.drawScaledCustomSizeModalRect(
                            baseX + 4, y + 6,
                            40, 8, 8, 8,
                            32, 32,
                            64, 64
                    );

                    GL11.glColor4f(1f, 1f, 1f, 1f);
                    GL11.glDisable(GL11.GL_BLEND);
                    GL11.glPopMatrix();

                    int frameAlpha = (int)(200 * targetHudAnimationProgress);
                    Gui.drawRect(baseX + 3, y + 5, baseX + 37, y + 6, new Color(180, 0, 0, frameAlpha).getRGB());
                    Gui.drawRect(baseX + 3, y + 38, baseX + 37, y + 39, new Color(180, 0, 0, frameAlpha).getRGB());
                }

                if (target != null) {
                    for (int i = 0; i < 4; i++) {
                        ItemStack armor = target.getCurrentArmor(i);
                        if (armor != null) {
                            int itemX = baseX + width - (i + 1) * 19 - 24;

                            GL11.glPushMatrix();
                            GL11.glEnable(GL11.GL_BLEND);
                            GL11.glColor4f(1f, 1f, 1f, targetHudAnimationProgress);

                            RenderHelper.enableGUIStandardItemLighting();
                            mc.getRenderItem().renderItemAndEffectIntoGUI(armor, itemX, y + 27);
                            mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRendererObj, armor, itemX, y + 27, null);
                            RenderHelper.disableStandardItemLighting();

                            GL11.glColor4f(1f, 1f, 1f, 1f);
                            GL11.glDisable(GL11.GL_BLEND);
                            GL11.glPopMatrix();
                        }
                    }
                }

                GL11.glColor4f(1f, 1f, 1f, 1f);
                GlStateManager.disableLighting();
                GlStateManager.disableBlend();
            } else {
                targetHpLerp = 0f;
            }

            GL11.glPopMatrix();
        }

        if (features.isSelected("Module List")) {
            GL11.glPushMatrix();
            float rightX = sr.getScaledWidth();
            float topY = 0f;
            GL11.glTranslatef(rightX, topY, 0);
            GL11.glScalef(scale, scale, 1f);
            GL11.glTranslatef(-rightX, -topY, 0);
            this.renderModuleList(sr, fontRenderer, fontSize, deltaTime);
            GL11.glPopMatrix();
        }

        if (features.isSelected("Armor Hud")) {
            GL11.glPushMatrix();
            RenderHelper.enableGUIStandardItemLighting();

            int centerX = sr.getScaledWidth() / 2;
            int baseY = mc.thePlayer.capabilities.isCreativeMode ? sr.getScaledHeight() - 41 : mc.thePlayer.getAir() < 300 ? sr.getScaledHeight() - 65 : sr.getScaledHeight() - 54;
            int slot = 0;

            ItemStack[] armors = mc.thePlayer.inventory.armorInventory;

            for (int i = armors.length - 1; i >= 0; i--) {
                ItemStack armorStack = armors[i];
                if (armorStack != null) {
                    int x = centerX + 15 + (slot * 17);

                    mc.getRenderItem().renderItemAndEffectIntoGUI(armorStack, x, baseY);
                    mc.getRenderItem().renderItemOverlays(mc.bitFontRendererObj, armorStack, x, baseY);
                }
                slot++;
            }

            RenderHelper.disableStandardItemLighting();
            GL11.glPopMatrix();
        }

        if (features.isSelected("Packet Monitor")) {
            GL11.glPushMatrix();
            float rightX = sr.getScaledWidth();
            float bottomY = sr.getScaledHeight();
            GL11.glTranslatef(rightX, bottomY, 0);
            GL11.glScalef(scale, scale, 1f);
            GL11.glTranslatef(-rightX, -bottomY, 0);

            int graphWidth = 120;
            int graphHeight = 45;

            float updateInterval = 0.03f;
            timeSinceLastUpdate += deltaTime;
            while (timeSinceLastUpdate >= updateInterval) {
                inboundPackets.addLast(inboundThisTick);
                outboundPackets.addLast(outboundThisTick);

                if (inboundPackets.size() > MAX_HISTORY) inboundPackets.removeFirst();
                if (outboundPackets.size() > MAX_HISTORY) outboundPackets.removeFirst();

                int inHeight = (int) ((inboundThisTick / 50.0f) * (graphHeight - 2));
                int outHeight = (int) ((outboundThisTick / 50.0f) * (graphHeight - 2));

                inboundHeights.addLast(Math.min(graphHeight - 2, inHeight));
                outboundHeights.addLast(Math.min(graphHeight - 2, outHeight));

                if (inboundHeights.size() > MAX_HISTORY) inboundHeights.removeFirst();
                if (outboundHeights.size() > MAX_HISTORY) outboundHeights.removeFirst();

                inboundThisTick = 0;
                outboundThisTick = 0;

                timeSinceLastUpdate -= updateInterval;
            }

            int baseX = sr.getScaledWidth() - 125;
            int baseY = sr.getScaledHeight() - graphHeight - 10 - (mc.currentScreen instanceof GuiChat ? 20 : 0);

            drawBlurredBackground(baseX, baseY, graphWidth, graphHeight, 6, new Color(5, 5, 5, 220).getRGB());

            Gui.drawRect(baseX, baseY, baseX + graphWidth, baseY + 1, new Color(180, 0, 0, 255).getRGB());
            Gui.drawRect(baseX, baseY + graphHeight - 1, baseX + graphWidth, baseY + graphHeight, new Color(180, 0, 0, 255).getRGB());

            int i = 0;

            for (Integer h : outboundHeights) {
                int x = baseX + i;
                int top = baseY + graphHeight - h;
                int bottom = baseY + graphHeight;
                if (top < bottom) {
                    Gui.drawRect(x, top, x + 1, bottom, new Color(180, 50, 0, 200).getRGB());
                }
                i++;
            }

            i = 0;
            for (Integer h : inboundHeights) {
                int x = baseX + i;
                int top = baseY + graphHeight - h;
                int bottom = baseY + graphHeight;
                if (top < bottom) {
                    Gui.drawRect(x, top, x + 1, bottom, new Color(200, 0, 0, 200).getRGB());
                }
                i++;
            }

            GL11.glPopMatrix();
        }
    }

    public int getTotalValidBlocksInHotbar() {
        int total = 0;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
            if (DewCommon.moduleManager.getModule(Scaffold.class).isInvalidBlock(stack)) continue;
            total += stack.stackSize;
        }
        return total;
    }

    private Color applyAlpha(Color c, float alpha) {
        int a = (int)(c.getAlpha() * alpha);
        a = Math.min(255, Math.max(0, a));
        return new Color(c.getRed(), c.getGreen(), c.getBlue(), a);
    }

    private String toRoman(int number) {
        String[] romans = {"N", "I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
        return (number >= 0 && number <= 10) ? romans[number] : String.valueOf(number);
    }

    private static class BloodParticle {
        float x, y;
        float vx, vy;
        float life;

        BloodParticle(float x, float y) {
            this.x = x;
            this.y = y;
            Random r = new Random();
            this.vx = (r.nextFloat() - 0.5f) * 2f;
            this.vy = r.nextFloat() * 2f;
            this.life = 1f;
        }

        void update(float delta) {
            x += vx * delta * 50;
            y += vy * delta * 50;
            vy += delta * 100;
            life -= delta;
        }

        boolean isDead() {
            return life <= 0;
        }
    }
}