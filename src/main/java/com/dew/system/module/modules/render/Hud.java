package com.dew.system.module.modules.render;

import com.dew.DewCommon;
import com.dew.system.event.events.Render2DEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.module.modules.combat.KillAura;
import com.dew.system.module.modules.player.Scaffold;
import com.dew.system.settingsvalue.MultiSelectionValue;
import com.dew.utils.Lerper;
import com.dew.utils.fonts.CustomFontRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.AbstractClientPlayer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.potion.PotionEffect;
import net.minecraft.src.Config;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class Hud extends Module {

    public Hud() {
        super("Hud", ModuleCategory.RENDER, Keyboard.KEY_NONE, true, false, true);
    }

    private static final MultiSelectionValue features = new MultiSelectionValue("Features", Arrays.asList("Watermark", "Module List", "Armor Hud", "Potion Hud", "Target Hud"), "Watermark", "Module List", "Armor Hud", "Potion Hud", "Target Hud");

    private final Map<Module, Float> animationProgress = new HashMap<>();

    @Override
    public void onRender2D(Render2DEvent event) {
        ScaledResolution sr = new ScaledResolution(mc);

        CustomFontRenderer fontRenderer = DewCommon.customFontRenderer;
        float fontSize = 0.5f;

        if (features.isSelected("Watermark")) {
            String clientName = DewCommon.clientName + " | " + Minecraft.getDebugFPS() + " fps";
            String beforeWater = clientName.substring(0, 1);
            String afterWater = clientName.substring(1);

            float speed = 2000f;
            float time = (System.currentTimeMillis() % (int) speed) / speed;
            float progress = (time + 1f) % 1.0f;

            mc.bitFontRendererObj.drawString(beforeWater, 4, 4, getSmoothPurpleGradient(progress).getRGB(), true);
            mc.bitFontRendererObj.drawString(afterWater, 4 + mc.bitFontRendererObj.getStringWidth(beforeWater), 4, Color.WHITE.getRGB(), true);
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
            KillAura killAuraModule = DewCommon.moduleManager.getModule(KillAura.class);
            if (killAuraModule.isEnabled() && killAuraModule.target instanceof EntityLivingBase || killAuraModule.target == null && mc.currentScreen instanceof GuiChat) {
                EntityLivingBase target = killAuraModule.target == null ? mc.thePlayer : (EntityLivingBase) killAuraModule.target;

                int x = sr.getScaledWidth() / 2 - 220;
                int y = sr.getScaledHeight() / 2 - 90;
                int width = 120;
                int height = 42;

                Gui.drawRect(x, y, x + width, y + height, new Color(0, 0, 0, 120).getRGB());

                String name = target.getName();
                mc.bitFontRendererObj.drawStringWithShadow(name, x + 40, y + 4, 0xFFFFFF);

                float health = target.getHealth();
                float maxHealth = target.getMaxHealth();
                float healthRatio = health / maxHealth;
                healthRatio = Math.max(0.0f, Math.min(1.0f, healthRatio));
                int healthBarWidth = (int)(healthRatio * (width - 50));
                Color healthColor = new Color(
                        (int)(255 * Math.min(1.0f, (1.0f - healthRatio) * 2)),
                        (int)(255 * Math.min(1.0f, healthRatio * 2)),
                        0
                );

                Gui.drawRect(x + 40, y + 15, x + 40 + healthBarWidth, y + 25, healthColor.getRGB());
                mc.bitFontRendererObj.drawStringWithShadow(String.format("%.1f", health), x + 64, y + 16, 0xFFFFFF);

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

        Scaffold scaffoldModule = DewCommon.moduleManager.getModule(Scaffold.class);
        if (scaffoldModule.isEnabled()) {
            int totalBlocks = scaffoldModule.getTotalValidBlocksInHotbar();
            String display = totalBlocks + " blocks";

            int x = sr.getScaledWidth() / 2 + 20;
            int y = sr.getScaledHeight() / 2;

            GlStateManager.pushMatrix();
            RenderHelper.disableStandardItemLighting();
            mc.bitFontRendererObj.drawStringWithShadow(display, x, y, 0xFFFFFF);
            GlStateManager.popMatrix();
        }

        if (features.isSelected("Module List")) {
            int rightEdgeX = sr.getScaledWidth();

            List<Module> moduleList = DewCommon.moduleManager.getModules().stream()
                    .filter(m -> m.showOnArray)
                    .sorted(Comparator.comparingDouble((Module m) -> fontRenderer.getStringWidth(m.name + (!m.tag().isEmpty() ? " " + m.tag() : ""), fontSize)).reversed())
                    .collect(Collectors.toList());

            float speed = 2000f;
            float time = (System.currentTimeMillis() % (int) speed) / speed;

            float cumulativeY = 0;
            for (Module module : moduleList) {
                boolean enabled = module.isEnabled();
                float current = animationProgress.getOrDefault(module, enabled ? 0f : 1f);
                float target = enabled ? 1f : 0f;
                float animationSpeed = 0.2f;

                float interpolated = Lerper.lerp(current, target, animationSpeed);

                animationProgress.put(module, interpolated);

                if (interpolated <= 0.01f) {
                    continue;
                }

                String moduleName = module.name;
                String tag = module.tag();
                String display = moduleName + (!tag.isEmpty() ? " " + tag : "");

                float progress = (time + (float) cumulativeY / 100f) % 1.0f;
                Color accentColor = getSmoothPurpleGradient(progress);

                float displayWidth = fontRenderer.getStringWidth(display, fontSize);
                float displayHeight = 11;

                float slideOffset = (1.0f - interpolated) * 10;
                float finalY = cumulativeY + slideOffset;

                int bgLeft = (int) (rightEdgeX - displayWidth - 6);
                int bgTop = (int) (finalY - 1);
                int bgBottom = (int) (finalY + displayHeight);

                int alpha = (int) (255 * interpolated);
                Color bgColor = new Color(10, 10, 10, (int)(170 * interpolated));
                Color barColor = new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), (int)(170 * interpolated));

                Gui.drawRect(bgLeft + 1, bgTop, rightEdgeX + 1, bgBottom, bgColor.getRGB());
                Gui.drawRect(bgLeft + 1, bgTop, bgLeft + 2, bgBottom, barColor.getRGB());

                if (!tag.isEmpty()) {
                    float tagWidth = fontRenderer.getStringWidth(" " + tag, fontSize);
                    float nameWidth = fontRenderer.getStringWidth(moduleName, fontSize);

                    float tagX = rightEdgeX - tagWidth - 2;
                    float nameX = tagX - nameWidth;

                    Color nameColor = new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), alpha);
                    Color tagColor = new Color(150, 150, 150, alpha);

                    fontRenderer.drawStringWithShadow(moduleName, nameX, finalY, nameColor.getRGB(), fontSize);
                    fontRenderer.drawStringWithShadow(" " + tag, tagX, finalY, tagColor.getRGB(), fontSize);
                } else {
                    float nameWidth = fontRenderer.getStringWidth(moduleName, fontSize);
                    float nameX = rightEdgeX - nameWidth - 2;
                    Color nameColor = new Color(accentColor.getRed(), accentColor.getGreen(), accentColor.getBlue(), alpha);
                    fontRenderer.drawStringWithShadow(moduleName, nameX, finalY, nameColor.getRGB(), fontSize);
                }

                cumulativeY += displayHeight + 1;
            }
        }
    }

    private Color getSmoothPurpleGradient(float progress) {
        progress = progress % 1.0f;
        float segment = progress * 3;

        if (segment < 1) {
            int red = (int)(255 - 127 * segment);
            return new Color(red, 0, 255);
        } else if (segment < 2) {
            float t = segment - 1;
            int green = (int)(64 * t);
            return new Color(128, green, 255);
        } else {
            float t = segment - 2;
            int red = (int)(128 + 127 * t);
            int green = (int)(64 * (1 - t));
            return new Color(red, green, 255);
        }
    }

    private String toRoman(int number) {
        String[] romans = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
        return (number >= 1 && number <= 10) ? romans[number - 1] : String.valueOf(number);
    }
}
