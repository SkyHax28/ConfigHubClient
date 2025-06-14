package com.dew.system.module.modules.render;

import com.dew.DewCommon;
import com.dew.system.event.events.Render2DEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.settingsvalue.MultiSelectionValue;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
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

    private static final MultiSelectionValue features = new MultiSelectionValue("Features", Arrays.asList("Watermark", "Module List", "Armor Hud", "Potion Hud"), "Watermark", "Module List", "Armor Hud", "Potion Hud");

    @Override
    public void onRender2D(Render2DEvent event) {
        ScaledResolution sr = new ScaledResolution(mc);

        if (features.isSelected("Watermark")) {
            String clientName = DewCommon.clientName;
            String fps = Minecraft.getDebugFPS() + " fps";
            String beforeWater = clientName.substring(0, 1);
            String afterWater = clientName.substring(1);
            mc.fontRendererObj.drawString(beforeWater, 4, 4, Color.RED.darker().getRGB(), true);
            mc.fontRendererObj.drawString(afterWater, 4 + mc.fontRendererObj.getStringWidth(beforeWater), 4, Color.WHITE.getRGB(), true);
            mc.fontRendererObj.drawString(fps, 4, 15, Color.WHITE.getRGB(), true);
        }

        if (features.isSelected("Module List")) {
            int rightEdgeX = sr.getScaledWidth();

            List<Module> moduleList = DewCommon.moduleManager.getModules().stream()
                    .filter(i -> i.showOnArray && i.isEnabled())
                    .sorted(Comparator.comparingInt((Module m) -> mc.fontRendererObj.getStringWidth(m.name + (!m.tag().isEmpty() ? " " + m.tag() : ""))).reversed())
                    .collect(Collectors.toList());

            if (!moduleList.isEmpty()) {
                int index = 0;
                for (Module module : moduleList) {
                    if (module.showOnArray && module.isEnabled()) {
                        String moduleName = module.name;
                        String tag = module.tag();
                        mc.fontRendererObj.drawString(moduleName, rightEdgeX - mc.fontRendererObj.getStringWidth(moduleName) - (!tag.isEmpty() ? mc.fontRendererObj.getStringWidth(" " + tag) : 0) - 4, 4 + (index * 11), Color.RED.darker().getRGB(), true);
                        if (!tag.isEmpty())
                            mc.fontRendererObj.drawString(" " + tag, rightEdgeX - mc.fontRendererObj.getStringWidth(" " + tag) - 4, 4 + (index * 11), Color.GRAY.getRGB(), true);
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
                    mc.getRenderItem().renderItemOverlayIntoGUI(mc.fontRendererObj, armorStack, x + xOffset, y + additionalY, null);
                    RenderHelper.disableStandardItemLighting();
                }
            }
        }

        if (features.isSelected("Potion Hud")) {
            Collection<PotionEffect> effects = mc.thePlayer.getActivePotionEffects();
            if (effects.isEmpty()) return;

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

                mc.fontRendererObj.drawStringWithShadow(display, x - mc.fontRendererObj.getStringWidth(display) - 4, y - index * 25 + 1, 0xFFFFFF);
                mc.fontRendererObj.drawStringWithShadow(timeDisplay, x - mc.fontRendererObj.getStringWidth(durationStr) - 4, y - index * 25 + 11, 0xFFFFFF);

                index++;
            }
        }
    }

    private String toRoman(int number) {
        String[] romans = {"I", "II", "III", "IV", "V", "VI", "VII", "VIII", "IX", "X"};
        return (number >= 1 && number <= 10) ? romans[number - 1] : String.valueOf(number);
    }
}
