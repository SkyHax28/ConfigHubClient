package com.dew.system.module.modules.render;

import com.dew.DewCommon;
import com.dew.system.event.events.Render2DEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.module.modules.combat.KillAura;
import com.dew.system.settingsvalue.MultiSelectionValue;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

public class Hud extends Module {

    public Hud() {
        super("Hud", ModuleCategory.RENDER, Keyboard.KEY_NONE, true, false, true);
    }

    private static final MultiSelectionValue features = new MultiSelectionValue("Features", Arrays.asList("Watermark", "Module List", "Armor Hud", "Potion Hud", "Target Hud"), "Watermark", "Module List", "Armor Hud", "Potion Hud", "Target Hud");

    @Override
    public void onRender2D(Render2DEvent event) {
        ScaledResolution sr = new ScaledResolution(mc);

        if (features.isSelected("Watermark")) {
            String clientName = DewCommon.clientName;
            String fps = Minecraft.getDebugFPS() + " fps";
            String beforeWater = clientName.substring(0, 1);
            String afterWater = clientName.substring(1);
            mc.bitFontRendererObj.drawString(beforeWater, 4, 4, Color.RED.darker().getRGB(), true);
            mc.bitFontRendererObj.drawString(afterWater, 4 + mc.bitFontRendererObj.getStringWidth(beforeWater), 4, Color.WHITE.getRGB(), true);
            mc.bitFontRendererObj.drawString(fps, 4, 15, Color.WHITE.getRGB(), true);
        }

        if (features.isSelected("Module List")) {
            int rightEdgeX = sr.getScaledWidth();

            List<Module> moduleList = DewCommon.moduleManager.getModules().stream()
                    .filter(i -> i.showOnArray && i.isEnabled())
                    .sorted(Comparator.comparingInt((Module m) -> mc.bitFontRendererObj.getStringWidth(m.name + (!m.tag().isEmpty() ? " " + m.tag() : ""))).reversed())
                    .collect(Collectors.toList());

            if (!moduleList.isEmpty()) {
                int index = 0;
                for (Module module : moduleList) {
                    if (module.showOnArray && module.isEnabled()) {
                        String moduleName = module.name;
                        String tag = module.tag();
                        float speed = 3000f;
                        float hueOffset = (float) index / moduleList.size();
                        float hue = ((System.currentTimeMillis() % (int) speed) / speed + hueOffset) % 1.0f;
                        Color rainbowColor = Color.getHSBColor(hue, 0.8f, 0.9f);
                        mc.bitFontRendererObj.drawString(moduleName, rightEdgeX - mc.bitFontRendererObj.getStringWidth(moduleName) - (!tag.isEmpty() ? mc.bitFontRendererObj.getStringWidth(" " + tag) : 0) - 4, 4 + (index * 11), rainbowColor.getRGB(), true);
                        if (!tag.isEmpty())
                            mc.bitFontRendererObj.drawString(" " + tag, rightEdgeX - mc.bitFontRendererObj.getStringWidth(" " + tag) - 4, 4 + (index * 11), Color.GRAY.getRGB(), true);
                        index++;
                    }
                }
            }
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
    }

    private String toRoman(int number) {
        String[] romans = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
        return (number >= 1 && number <= 10) ? romans[number - 1] : String.valueOf(number);
    }
}
