package com.dew.utils;

import com.dew.IMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.inventory.GuiContainer;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class ClickEffectHandler {

    private static final Minecraft mc = IMinecraft.mc;

    public static long lastClickTime = 0L;
    public static int lastSlotX = 0;
    public static int lastSlotY = 0;

    public static void onContainerClick(int slotId, int clickedButton, int mode, EntityPlayer player) {
        if (mc.currentScreen instanceof GuiContainer) {
            GuiContainer gui = (GuiContainer) mc.currentScreen;
            if (slotId >= 0 && slotId < gui.inventorySlots.inventorySlots.size()) {
                Slot slot = gui.inventorySlots.inventorySlots.get(slotId);
                lastSlotX = slot.xDisplayPosition + gui.guiLeft + 8;
                lastSlotY = slot.yDisplayPosition + gui.guiTop + 8;
                lastClickTime = System.currentTimeMillis();
            }
        }
    }

    public static void drawCircleEffect(int x, int y, float radius, Color color) {
        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(color.getRed() / 255f, color.getGreen() / 255f, color.getBlue() / 255f, color.getAlpha() / 255f);

        GL11.glBegin(GL11.GL_TRIANGLE_FAN);
        GL11.glVertex2f(x, y);
        for (int i = 0; i <= 360; i += 10) {
            double angle = Math.toRadians(i);
            GL11.glVertex2f((float) (x + Math.sin(angle) * radius), (float) (y + Math.cos(angle) * radius));
        }
        GL11.glEnd();

        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    public static void guiContainerClick() {
        if (lastClickTime > 0L) {
            long elapsed = System.currentTimeMillis() - lastClickTime;
            if (elapsed < 300) {
                float alpha = 1.0f - (elapsed / 300.0f);
                drawCircleEffect(lastSlotX, lastSlotY, elapsed / 12.5f, new Color(255, 255, 255, (int)(alpha * 255)));
            }
        }
    }
}