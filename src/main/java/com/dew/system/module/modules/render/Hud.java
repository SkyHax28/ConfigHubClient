package com.dew.system.module.modules.render;

import com.dew.DewCommon;
import com.dew.system.event.events.Render2DEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.module.modules.combat.Aura;
import com.dew.system.module.modules.ghost.BridgeAssist;
import com.dew.system.module.modules.player.Scaffold;
import com.dew.system.settingsvalue.BooleanValue;
import com.dew.system.settingsvalue.MultiSelectionValue;
import com.dew.system.userdata.DataSaver;
import com.dew.utils.Lerper;
import com.dew.utils.font.CustomFontRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
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
    public Hud() {
        super("Hud", ModuleCategory.RENDER, Keyboard.KEY_NONE, true, false, true);
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

    @Override
    public void onRender2D(Render2DEvent event) {
        ScaledResolution sr = new ScaledResolution(mc);

        long now = System.nanoTime();
        float deltaTime = (now - lastRenderTime) / 1_000_000_000.0f;
        lastRenderTime = now;

        CustomFontRenderer fontRenderer = DewCommon.customFontRenderer;
        float fontSize = 0.35f;

        if (features.isSelected("Watermark")) {
            String clientName = DewCommon.clientName + " | " + DataSaver.userName + " | " + Minecraft.getDebugFPS() + " fps";
            String beforeWater = clientName.substring(0, 1);
            String afterWater = clientName.substring(1);

            float speed = 2000f;
            float time = (System.currentTimeMillis() % (int) speed) / speed;
            float progress = (time + 1f) % 1.0f;

            mc.bitFontRendererObj.drawBlackOutlinedString(beforeWater, 4, 4, getSmoothPurpleGradient(progress).getRGB());
            mc.bitFontRendererObj.drawBlackOutlinedString(afterWater, 4 + mc.bitFontRendererObj.getStringWidth(beforeWater), 4, Color.WHITE.getRGB());
        }

        if (features.isSelected("Armor Hud")) {
            int x = sr.getScaledWidth() / 2 + 15;
            int y = sr.getScaledHeight() - 56;

            for (int i = 0; i < 4; i++) {
                ItemStack armorStack = mc.thePlayer.inventory.armorInventory[3 - i];
                if (armorStack != null) {
                    int xOffset = i * 18;
                    int additionalY = mc.thePlayer.capabilities.isCreativeMode ? 15 : 0;

                    RenderHelper.enableGUIStandardItemLighting();
                    mc.getRenderItem().renderItemAndEffectIntoGUI(armorStack, x + xOffset, y + additionalY);
                    mc.getRenderItem().renderItemOverlayIntoGUI(mc.bitFontRendererObj, armorStack, x + xOffset, y + additionalY, null);
                    RenderHelper.disableStandardItemLighting();
                }
            }
        }

        if (features.isSelected("Potion Hud")) {
            Collection<PotionEffect> effects = mc.thePlayer.getActivePotionEffects();
            if (!effects.isEmpty()) {
                int x = sr.getScaledWidth() - 30;
                int y = sr.getScaledHeight() - 30;
                int index = 0;

                for (PotionEffect effect : effects) {
                    Potion potion = Potion.potionTypes[effect.getPotionID()];
                    if (potion == null) continue;

                    mc.getTextureManager().bindTexture(new ResourceLocation("textures/gui/container/inventory.png"));
                    int iconIndex = potion.getStatusIconIndex();
                    int iconX = iconIndex % 8 * 18;
                    int iconY = 198 + (iconIndex / 8) * 18;

                    GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                    mc.ingameGUI.drawTexturedModalRect(x, y - index * 25, iconX, iconY, 18, 18);

                    String potionName = StatCollector.translateToLocal(potion.getName());
                    String levelStr = toRoman(effect.getAmplifier() + 1);
                    String durationStr = Potion.getDurationString(effect);

                    int durationTicks = effect.getDuration();
                    String colorCode;
                    if (durationTicks <= 200) {
                        colorCode = "§c";
                    } else if (durationTicks <= 600) {
                        colorCode = "§e";
                    } else {
                        colorCode = "§f";
                    }

                    String display = potionName + " " + levelStr;
                    String timeDisplay = colorCode + durationStr;

                    mc.bitFontRendererObj.drawStringWithShadow(display, x - mc.bitFontRendererObj.getStringWidth(display) - 4, y - index * 25 + 1, 0xFFFFFF);
                    mc.bitFontRendererObj.drawStringWithShadow(timeDisplay, x - mc.bitFontRendererObj.getStringWidth(durationStr) - 4, y - index * 25 + 11, 0xFFFFFF);

                    index++;
                }
            }
        }

        if (features.isSelected("Target Hud")) {
            Aura auraModule = DewCommon.moduleManager.getModule(Aura.class);
            if (auraModule.isEnabled() && auraModule.target instanceof EntityLivingBase || auraModule.target == null && mc.currentScreen instanceof GuiChat) {
                EntityLivingBase target = auraModule.target == null ? mc.thePlayer : (EntityLivingBase) auraModule.target;

                int x = sr.getScaledWidth() / 2 - 220;
                int y = sr.getScaledHeight() / 2 - 90;
                int width = 120;
                int height = 42;

                drawBlurredBackground(
                        x, y, width, height,
                        5,
                        new Color(0, 0, 0, 50).getRGB()
                );

                String name = target.getName();
                mc.bitFontRendererObj.drawStringWithShadow(name, x + 40, y + 3.5f, Color.WHITE.getRGB());

                float health = target.getHealth();
                float maxHealth = target.getMaxHealth();
                float healthRatio = health / maxHealth;
                healthRatio = Math.max(0.0f, Math.min(1.0f, healthRatio));
                int healthBarWidth = (int) (healthRatio * (width - 50));
                Color healthColor = new Color(
                        (int) (255 * Math.min(1.0f, (1.0f - healthRatio) * 2)),
                        (int) (255 * Math.min(1.0f, healthRatio * 2)),
                        0
                );

                Gui.drawRect(x + 40, y + 15, x + 40 + healthBarWidth, y + 25, healthColor.getRGB());
                mc.bitFontRendererObj.drawStringWithShadow(String.format("%.1f", health), x + 64, y + 16.5f, Color.WHITE.getRGB());

                if (target instanceof AbstractClientPlayer) {
                    ResourceLocation skin = ((AbstractClientPlayer) target).getLocationSkin();
                    mc.getTextureManager().bindTexture(skin);
                    Gui.drawScaledCustomSizeModalRect(x + 4, y + 5, 8, 8, 8, 8, 32, 32, 64, 64);
                    Gui.drawScaledCustomSizeModalRect(x + 4, y + 5, 40, 8, 8, 8, 32, 32, 64, 64);
                }

                for (int i = 0; i < 4; i++) {
                    ItemStack armor = target.getCurrentArmor(i);
                    if (armor != null) {
                        int itemX = x + width - 16 * (i + 1) - 18;
                        RenderHelper.enableGUIStandardItemLighting();
                        mc.getRenderItem().renderItemAndEffectIntoGUI(armor, itemX, y + 25);
                        mc.getRenderItem().renderItemOverlayIntoGUI(mc.bitFontRendererObj, armor, itemX, y + 25, null);
                        RenderHelper.disableStandardItemLighting();
                    }
                }
            }
        }

        if (DewCommon.moduleManager.getModule(Scaffold.class).isEnabled() || DewCommon.moduleManager.getModule(BridgeAssist.class).isEnabled() && DewCommon.moduleManager.getModule(BridgeAssist.class).isBridging()) {
            int totalBlocks = this.getTotalValidBlocksInHotbar();
            String display = totalBlocks + " blocks";

            int x = sr.getScaledWidth() / 2 + 20;
            int y = sr.getScaledHeight() / 2;

            GlStateManager.pushMatrix();
            RenderHelper.disableStandardItemLighting();
            mc.bitFontRendererObj.drawStringWithShadow(display, x, y, 0xFFFFFF);
            GlStateManager.popMatrix();
        }

        if (features.isSelected("Module List")) {
            int rightEdgeX = sr.getScaledWidth() - 12;

            List<Module> moduleList = DewCommon.moduleManager.getModules().stream()
                    .filter(m -> m.showOnArray)
                    .sorted(Comparator.comparingDouble((Module m) -> fontRenderer.getStringWidth(m.name + (!m.tag().isEmpty() ? " " + m.tag() : ""), fontSize)).reversed())
                    .collect(Collectors.toList());

            float speed = 1800f;
            float time = (System.currentTimeMillis() % (int) speed) / speed;

            float cumulativeY = 0;
            for (Module module : moduleList) {
                boolean enabled = module.isEnabled();
                float current = animationProgress.getOrDefault(module, enabled ? 0f : 1f);
                float target = enabled ? 1f : 0f;
                float animationSpeed = 18f * deltaTime;

                float interpolated = Lerper.lerp(current, target, animationSpeed);

                animationProgress.put(module, interpolated);

                if (interpolated <= 0.25f) {
                    continue;
                }

                String moduleName = module.name;
                String tag = module.tag();
                String display = moduleName + (!tag.isEmpty() ? " " + tag : "");

                float progress = (time + cumulativeY / 100f) % 1.0f;
                Color accentColor = getSmoothPurpleGradient(progress);

                float displayWidth = fontRenderer.getStringWidth(display, fontSize);
                float displayHeight = 14;

                float slideOffset = (1.0f - interpolated) * 10;
                float finalY = cumulativeY + slideOffset + 12;

                int bgLeft = (int) (rightEdgeX - displayWidth - 6);
                int bgTop = (int) (finalY - 1);
                int bgBottom = (int) (finalY + displayHeight);

                int alpha = Math.max(0, Math.min(255, (int) (255 * interpolated)));
                Color bgColor = new Color(10, 10, 10, Math.max(0, Math.min(255, (int) (170 * interpolated))));

                int softAlpha = (int) (alpha * 0.22);
                softAlpha = Math.min(softAlpha, 100);

                drawBlurredBackground(
                        bgLeft + 1, bgTop + 1,
                        (rightEdgeX + 1) - (bgLeft + 1),
                        bgBottom - bgTop - 2,
                        5,
                        new Color(bgColor.getRed(), bgColor.getGreen(), bgColor.getBlue(), softAlpha).getRGB()
                );

                if (!tag.isEmpty()) {
                    float tagWidth = fontRenderer.getStringWidth(" " + tag, fontSize);
                    float nameWidth = fontRenderer.getStringWidth(moduleName, fontSize);

                    float tagX = rightEdgeX - tagWidth - 2;
                    float nameX = tagX - nameWidth;

                    Color nameColor = new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), alpha);
                    Color tagColor = new Color(150, 150, 150, alpha);

                    fontRenderer.drawStringWithShadow(moduleName, nameX, finalY - 0.5f, nameColor.getRGB(), fontSize);
                    fontRenderer.drawStringWithShadow(" " + tag, tagX, finalY - 0.5f, tagColor.getRGB(), fontSize);
                } else {
                    float nameWidth = fontRenderer.getStringWidth(moduleName, fontSize);
                    float nameX = rightEdgeX - nameWidth - 2;
                    Color nameColor = new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), alpha);
                    fontRenderer.drawStringWithShadow(moduleName, nameX, finalY - 0.5f, nameColor.getRGB(), fontSize);
                }

                cumulativeY += displayHeight + 1;
            }
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

    private Color getSmoothPurpleGradient(float progress) {
        progress = progress % 1.0f;
        float segment = progress * 3;

        if (segment < 1) {
            int red = (int) (255 - 127 * segment);
            return new Color(red, 0, 255);
        } else if (segment < 2) {
            float t = segment - 1;
            int green = (int) (64 * t);
            return new Color(128, green, 255);
        } else {
            float t = segment - 2;
            int red = (int) (128 + 127 * t);
            int green = (int) (64 * (1 - t));
            return new Color(red, green, 255);
        }
    }

    private String toRoman(int number) {
        String[] romans = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
        return (number >= 1 && number <= 10) ? romans[number - 1] : String.valueOf(number);
    }
}
