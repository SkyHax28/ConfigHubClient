package com.dew.system.module.modules.render;

import com.dew.DewCommon;
import com.dew.system.event.events.Render2DEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.module.modules.combat.Aura;
import com.dew.system.module.modules.exploit.MurdererDetector;
import com.dew.system.module.modules.ghost.BridgeAssist;
import com.dew.system.module.modules.player.Breaker;
import com.dew.system.module.modules.player.CivBreak;
import com.dew.system.module.modules.player.Scaffold;
import com.dew.system.settingsvalue.BooleanValue;
import com.dew.system.settingsvalue.MultiSelectionValue;
import com.dew.system.userdata.DataSaver;
import com.dew.utils.*;
import com.dew.utils.font.CustomFontRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import net.minecraft.world.WorldSettings;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class Hud extends Module {

    private static final MultiSelectionValue features = new MultiSelectionValue("Features", Arrays.asList("Watermark", "Module List", "Armor Hud", "Potion Hud", "Target Hud", "Hotbar"), "Watermark", "Module List", "Armor Hud", "Potion Hud", "Target Hud", "Hotbar");
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
    private static final ResourceLocation INVENTORY_TEXTURE = new ResourceLocation("textures/gui/container/inventory.png");
    public Hud() {
        super("Hud", ModuleCategory.RENDER, Keyboard.KEY_NONE, true, false, true);
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

    private void renderModuleList(ScaledResolution sr, CustomFontRenderer fontRenderer, float fontSize, float deltaTime) {
        updateModuleListCache(fontRenderer, fontSize);

        int rightEdgeX = sr.getScaledWidth() - 12;
        float speed = 1800f;
        float time = (System.currentTimeMillis() % (int) speed) / speed;

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

            float progress = (time + cumulativeY / 100f) % 1.0f;
            Color accentColor = this.getHudColor(progress, 0);

            float displayWidth = cachedWidths.getOrDefault(module, fontRenderer.getStringWidth(display, fontSize));
            float displayHeight = 14;

            float slideOffset = (1.0f - interpolated) * 10;
            float finalY = cumulativeY + slideOffset + 12;

            int bgLeft = (int) (rightEdgeX - displayWidth - 6);
            int bgTop = (int) (finalY - 1);
            int bgBottom = (int) (finalY + displayHeight);

            int alpha = Math.max(0, Math.min(255, (int) (255 * interpolated)));
            Color bgColor = new Color(10, 10, 10, Math.max(0, Math.min(255, (int) (170 * interpolated))));
            int softAlpha = Math.min((int) (alpha * 0.22), 100);

            drawBlurredBackground(
                    bgLeft + 1, bgTop + 1,
                    (rightEdgeX + 1) - (bgLeft + 1),
                    bgBottom - bgTop - 2,
                    5,
                    new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), softAlpha).getRGB()
            );

            float xMultiplier = 3f;

            if (!tag.isEmpty()) {
                float tagWidth = fontRenderer.getStringWidth(" " + tag, fontSize);
                float nameWidth = fontRenderer.getStringWidth(moduleName, fontSize);

                float tagX = rightEdgeX - tagWidth;
                float nameX = tagX - nameWidth;

                Color nameColor = new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), alpha);
                Color tagColor = new Color(150, 150, 150, alpha);

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
    }

    private static void drawBlurredBackground(double x, double y, double width, double height, int passes, int color) {
        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

        for (int i = 0; i < passes; i++) {
            double offset = i * 0.5;
            drawRect(x - offset, y - offset, x + width + offset, y + height + offset, color);
        }

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private static void drawRect(double left, double top, double right, double bottom, int color) {
        float alpha = (color >> 24 & 255) / 255.0F;
        float red = (color >> 16 & 255) / 255.0F;
        float green = (color >> 8 & 255) / 255.0F;
        float blue = (color & 255) / 255.0F;

        GlStateManager.color(red, green, blue, alpha);
        GL11.glBegin(GL11.GL_QUADS);
        GL11.glVertex2d(left, top);
        GL11.glVertex2d(left, bottom);
        GL11.glVertex2d(right, bottom);
        GL11.glVertex2d(right, top);
        GL11.glEnd();
    }

    public boolean disableAchievementsUI() {
        return disableAchievementsNotification.get();
    }

    public boolean renderCustomHotbar() {
        return features.isSelected("Hotbar");
    }

    public boolean renderWatermark() {
        return features.isSelected("Watermark");
    }

    @Override
    public void onRender2D(Render2DEvent event) {
        ScaledResolution sr = new ScaledResolution(mc);

        long now = System.nanoTime();
        float deltaTime = (now - lastRenderTime) / 1_000_000_000.0f;
        lastRenderTime = now;

        CustomFontRenderer fontRenderer = DewCommon.customFontRenderer;
        float fontSize = 0.35f;

        if (features.isSelected("Watermark")) {
            String clientName = DewCommon.clientName;
            String userInfo = " | " + DataSaver.userName + " | " + Minecraft.getDebugFPS() + " fps | " + (mc.isSingleplayer() ? "Singleplayer" : "Multiplayer") + " | " + PacketUtil.getCurrentPingOrMinusOne() + "ms" + (mc.isSingleplayer() ? "" : DewCommon.mongoManager.isConnected() && DewCommon.mongoManager.online.stream().anyMatch(p -> p.getLeft().equals(mc.thePlayer) && p.getRight().equals(DataSaver.userName)) ? " | Connected" : " | Connecting...");
            String display = clientName + userInfo;

            float targetWidth = fontRenderer.getStringWidth(display, fontSize);
            watermarkWidthLerp = Lerper.lerp(watermarkWidthLerp, targetWidth, 10f * deltaTime);

            float speed = 1800f;
            float time = (System.currentTimeMillis() % (int) speed) / speed;

            float displayHeight = 14f;
            int x = (int) ((sr.getScaledWidth() - watermarkWidthLerp) / 2);
            int y = 8;

            int alpha = 255;
            Color bgColor = new Color(10, 10, 10, (int)(170 * (alpha / 255f)));
            int softAlpha = (int)(alpha * 0.22);

            drawBlurredBackground(
                    x - 3, y - 2,
                    watermarkWidthLerp + 6, displayHeight,
                    6,
                    new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), softAlpha).getRGB()
            );

            float charX = x;
            for (int i = 0; i < display.length(); i++) {
                char c = display.charAt(i);
                Color accentColor = getHudColor(time, i);
                fontRenderer.drawStringWithShadow(
                        String.valueOf(c),
                        charX,
                        y - 1.5f,
                        accentColor.getRGB(),
                        fontSize
                );
                charX += fontRenderer.getStringWidth(String.valueOf(c), fontSize);
            }

            if (mc.thePlayer != null) {
                String burningText = mc.thePlayer.isBurning() ? "Burning!" : "";
                String damageInfo = mc.thePlayer.hurtTime != 0 ? "Invulnerable: " + mc.thePlayer.hurtTime : "";
                String flyInfo = mc.thePlayer.capabilities.isFlying ? "Flying" : mc.thePlayer.capabilities.allowFlying ? "Flyable" : "";
                String blockInfo = DewCommon.moduleManager.getModule(Scaffold.class).isEnabled() || DewCommon.moduleManager.getModule(BridgeAssist.class).isEnabled() && DewCommon.moduleManager.getModule(BridgeAssist.class).isBridging() ? this.getTotalValidBlocksInHotbar() + " blocks available" : "";
                String murdererInfo = DewCommon.moduleManager.getModule(MurdererDetector.class).isEnabled() && !DewCommon.moduleManager.getModule(MurdererDetector.class).getMurderers().isEmpty() ? "Murderers: " + DewCommon.moduleManager.getModule(MurdererDetector.class).getMurderers().size() : "";
                String breakingInfo = DewCommon.moduleManager.getModule(Breaker.class).isEnabled() && DewCommon.moduleManager.getModule(Breaker.class).isBreaking || DewCommon.moduleManager.getModule(CivBreak.class).isEnabled() && DewCommon.moduleManager.getModule(CivBreak.class).isBreaking ? "Breaking..." : "";
                String timerInfo = mc.timer.timerSpeed != 1 ? "Balance: " + mc.timer.timerSpeed : "";
                String blinkInfo = BlinkUtil.blinking ? "Blinking" : "";
                String deadInfo = mc.thePlayer.isDead ? "Wasted" : "";
                String spectatingInfo = mc.playerController != null && mc.playerController.getCurrentGameType() == WorldSettings.GameType.SPECTATOR ? "Spectating" : "";

                String combinedText = "";
                if (!burningText.isEmpty()) combinedText += burningText;
                if (!damageInfo.isEmpty()) combinedText += (combinedText.isEmpty() ? "" : "  ") + damageInfo;
                if (!flyInfo.isEmpty()) combinedText += (combinedText.isEmpty() ? "" : "  ") + flyInfo;
                if (!blockInfo.isEmpty()) combinedText += (combinedText.isEmpty() ? "" : "  ") + blockInfo;
                if (!murdererInfo.isEmpty()) combinedText += (combinedText.isEmpty() ? "" : "  ") + murdererInfo;
                if (!breakingInfo.isEmpty()) combinedText += (combinedText.isEmpty() ? "" : "  ") + breakingInfo;
                if (!timerInfo.isEmpty()) combinedText += (combinedText.isEmpty() ? "" : "  ") + timerInfo;
                if (!blinkInfo.isEmpty()) combinedText += (combinedText.isEmpty() ? "" : "  ") + blinkInfo;
                if (!deadInfo.isEmpty()) combinedText += (combinedText.isEmpty() ? "" : "  ") + deadInfo;
                if (!spectatingInfo.isEmpty()) combinedText += (combinedText.isEmpty() ? "" : "  ") + spectatingInfo;

                float lerpSpeed = Math.min(10f * deltaTime, 1f);

                if (!combinedText.isEmpty()) {
                    float targetCombinedWidth = fontRenderer.getStringWidth(combinedText, fontSize);
                    combinedTextWidthLerp = Lerper.lerp(combinedTextWidthLerp, targetCombinedWidth, lerpSpeed);

                    displayHeight += 2;

                    drawBlurredBackground(
                            (sr.getScaledWidth() - combinedTextWidthLerp) / 2f - 3,
                            y + displayHeight + 2,
                            combinedTextWidthLerp + 6, 12f,
                            6,
                            new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), softAlpha).getRGB()
                    );

                    float xPos = (sr.getScaledWidth() - combinedTextWidthLerp) / 2f;

                    if (!burningText.isEmpty()) {
                        fontRenderer.drawStringWithShadow(
                                burningText,
                                xPos,
                                y + displayHeight + 1.5f,
                                new Color(255, 80, 80, alpha).getRGB(),
                                fontSize
                        );
                        xPos += fontRenderer.getStringWidth(burningText + "  ", fontSize);
                    }

                    if (!damageInfo.isEmpty()) {
                        fontRenderer.drawStringWithShadow(
                                damageInfo,
                                xPos,
                                y + displayHeight + 1.5f,
                                new Color(255, 215, 0, alpha).getRGB(),
                                fontSize
                        );
                        xPos += fontRenderer.getStringWidth(damageInfo + "  ", fontSize);
                    }

                    if (!flyInfo.isEmpty()) {
                        fontRenderer.drawStringWithShadow(
                                flyInfo,
                                xPos,
                                y + displayHeight + 1.5f,
                                new Color(255, 215, 0, alpha).getRGB(),
                                fontSize
                        );
                        xPos += fontRenderer.getStringWidth(flyInfo + "  ", fontSize);
                    }

                    if (!blockInfo.isEmpty()) {
                        fontRenderer.drawStringWithShadow(
                                blockInfo,
                                xPos,
                                y + displayHeight + 1.5f,
                                new Color(255, 255, 255, alpha).getRGB(),
                                fontSize
                        );
                        xPos += fontRenderer.getStringWidth(blockInfo + "  ", fontSize);
                    }

                    if (!murdererInfo.isEmpty()) {
                        fontRenderer.drawStringWithShadow(
                                murdererInfo,
                                xPos,
                                y + displayHeight + 1.5f,
                                new Color(255, 0, 0, alpha).getRGB(),
                                fontSize
                        );
                        xPos += fontRenderer.getStringWidth(murdererInfo + "  ", fontSize);
                    }

                    if (!breakingInfo.isEmpty()) {
                        fontRenderer.drawStringWithShadow(
                                breakingInfo,
                                xPos,
                                y + displayHeight + 1.5f,
                                new Color(255, 0, 0, alpha).getRGB(),
                                fontSize
                        );
                        xPos += fontRenderer.getStringWidth(breakingInfo + "  ", fontSize);
                    }

                    if (!timerInfo.isEmpty()) {
                        fontRenderer.drawStringWithShadow(
                                timerInfo,
                                xPos,
                                y + displayHeight + 1.5f,
                                new Color(0, 255, 255, alpha).getRGB(),
                                fontSize
                        );
                        xPos += fontRenderer.getStringWidth(timerInfo + "  ", fontSize);
                    }

                    if (!blinkInfo.isEmpty()) {
                        fontRenderer.drawStringWithShadow(
                                blinkInfo,
                                xPos,
                                y + displayHeight + 1.5f,
                                new Color(0, 255, 100, alpha).getRGB(),
                                fontSize
                        );
                        xPos += fontRenderer.getStringWidth(blinkInfo + "  ", fontSize);
                    }

                    if (!deadInfo.isEmpty()) {
                        fontRenderer.drawStringWithShadow(
                                deadInfo,
                                xPos,
                                y + displayHeight + 1.5f,
                                new Color(255, 0, 0, alpha).getRGB(),
                                fontSize
                        );
                        xPos += fontRenderer.getStringWidth(spectatingInfo + "  ", fontSize);
                    }

                    if (!spectatingInfo.isEmpty()) {
                        fontRenderer.drawStringWithShadow(
                                spectatingInfo,
                                xPos,
                                y + displayHeight + 1.5f,
                                new Color(200, 200, 200, alpha).getRGB(),
                                fontSize
                        );
                    }
                } else {
                    combinedTextWidthLerp = Lerper.lerp(combinedTextWidthLerp, 0f, lerpSpeed);

                    if (combinedTextWidthLerp > 0.1f) {
                        drawBlurredBackground(
                                (sr.getScaledWidth() - combinedTextWidthLerp) / 2f - 3,
                                y + displayHeight + 2,
                                combinedTextWidthLerp + 6, 12f,
                                6,
                                new Color(0, 0, 0, softAlpha).getRGB()
                        );
                    }
                }
            }
        }

        if (features.isSelected("Armor Hud")) {
            int baseX = sr.getScaledWidth() / 2 + 15;
            int baseY = sr.getScaledHeight() - 56 + (mc.thePlayer.capabilities.isCreativeMode ? 15 : 0);

            RenderHelper.enableGUIStandardItemLighting();
            for (int i = 0; i < 4; i++) {
                ItemStack armorStack = mc.thePlayer.inventory.armorInventory[3 - i];
                if (armorStack != null) {
                    mc.getRenderItem().renderItemAndEffectIntoGUI(armorStack, baseX + i * 18, baseY);
                    mc.getRenderItem().renderItemOverlayIntoGUI(mc.bitFontRendererObj, armorStack, baseX + i * 18, baseY, null);
                }
            }
            RenderHelper.disableStandardItemLighting();
        }

        if (features.isSelected("Potion Hud")) {
            Collection<PotionEffect> effects = mc.thePlayer.getActivePotionEffects();
            if (!effects.isEmpty()) {
                int x = sr.getScaledWidth() - 30;
                int index = 0;
                mc.getTextureManager().bindTexture(INVENTORY_TEXTURE);

                for (PotionEffect effect : effects) {
                    Potion potion = Potion.potionTypes[effect.getPotionID()];
                    if (potion == null) continue;

                    int iconIndex = potion.getStatusIconIndex();
                    mc.ingameGUI.drawTexturedModalRect(x, sr.getScaledHeight() - 30 - index * 25, iconIndex % 8 * 18, 198 + (iconIndex / 8) * 18, 18, 18);

                    String potionName = StatCollector.translateToLocal(potion.getName()) + " " + toRoman(effect.getAmplifier() + 1);
                    int durationTicks = effect.getDuration();
                    String colorCode = durationTicks <= 200 ? "§c" : durationTicks <= 600 ? "§e" : "§f";
                    String durationStr = colorCode + Potion.getDurationString(effect);

                    mc.bitFontRendererObj.drawStringWithShadow(potionName, x - mc.bitFontRendererObj.getStringWidth(potionName) - 4, sr.getScaledHeight() - 29 - index * 25, 0xFFFFFF);
                    mc.bitFontRendererObj.drawStringWithShadow(durationStr, x - mc.bitFontRendererObj.getStringWidth(Potion.getDurationString(effect)) - 4, sr.getScaledHeight() - 19 - index * 25, 0xFFFFFF);

                    index++;
                }
            }
        }

        if (features.isSelected("Target Hud")) {
            Aura auraModule = DewCommon.moduleManager.getModule(Aura.class);
            boolean shouldShow = auraModule.isEnabled() && auraModule.target instanceof EntityLivingBase;
            this.updateTargetHudVisibility(shouldShow, deltaTime);
            if (targetHudVisible) {
                EntityLivingBase target = auraModule.target != null ? (EntityLivingBase) auraModule.target : null;

                int width = 140;
                int baseX = sr.getScaledWidth() / 2 - 140 / 2;
                int baseY = 80;

                float slideOffsetY = -10 * (1f - targetHudAnimationProgress);
                int baseAlpha = 170;
                int animatedAlpha = (int)(baseAlpha * targetHudAnimationProgress);
                int y = baseY + (int) slideOffsetY;

                Color bgColor = new Color(10, 10, 10, animatedAlpha);
                int softAlpha = (int)(255 * 0.22 * targetHudAnimationProgress);
                drawBlurredBackground(baseX, y, 140, 44, 8,
                        new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), softAlpha).getRGB()
                );

                float speed = 1800f;
                float time = (System.currentTimeMillis() % (int) speed) / speed;

                if (target != null) {
                    String name = target.getName();
                    for (int i = 0; i < name.length(); i++) {
                        char c = name.charAt(i);
                        Color letterColor = getHudColor(time, i + 20);
                        letterColor = applyAlpha(letterColor, targetHudAnimationProgress);
                        fontRenderer.drawStringWithShadow(
                                String.valueOf(c),
                                baseX + 40 + fontRenderer.getStringWidth(name.substring(0, i), fontSize),
                                y + 2,
                                letterColor.getRGB(),
                                fontSize
                        );
                    }
                }

                float targetHpRatio = target != null ? Math.max(0, Math.min(1, target.getHealth() / target.getMaxHealth())) : 0;
                targetHpLerp = Lerper.lerp(targetHpLerp, targetHpRatio, 10f * deltaTime);

                int hpBarWidth = (int) (targetHpLerp * (width - 50));
                for (int i = 0; i < hpBarWidth; i++) {
                    Color hpGradColor = getHudColor(time, (float) i + 50);
                    hpGradColor = applyAlpha(hpGradColor, targetHudAnimationProgress);
                    Gui.drawRect(
                            baseX + 40 + i,
                            y + 17,
                            baseX + 40 + i + 1,
                            y + 25,
                            hpGradColor.getRGB()
                    );
                }

                if (target != null) {
                    Color hpTextColor = new Color(255, 255, 255, (int) (255 * targetHudAnimationProgress));
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
        }

        if (features.isSelected("Module List")) {
            this.renderModuleList(sr, fontRenderer, fontSize, deltaTime);
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

    private Color getHudColor(float progress, float index) {
        float offset = index * 0.01f;
        progress = ((progress + offset) % 1.0f + 1.0f) % 1.0f;

        float segment = progress * 3;

        if (segment < 1) {
            return Lerper.lerpColor(new Color(0, 120, 255), new Color(0, 230, 255), segment);
        } else if (segment < 2) {
            float t = segment - 1;
            return Lerper.lerpColor(new Color(0, 230, 255), new Color(0, 180, 220), t);
        } else {
            float t = segment - 2;
            return Lerper.lerpColor(new Color(0, 180, 220), new Color(0, 120, 255), t);
        }
    }

    private String toRoman(int number) {
        String[] romans = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
        return (number >= 1 && number <= 10) ? romans[number - 1] : String.valueOf(number);
    }
}
