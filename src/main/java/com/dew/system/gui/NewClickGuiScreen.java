package com.dew.system.gui;

import com.dew.DewCommon;
import com.dew.system.module.ModuleCategory;
import com.dew.system.module.modules.render.ClickGui;
import com.dew.utils.Lerper;
import com.dew.utils.shader.BlurUtil;
import javafx.scene.transform.Scale;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class NewClickGuiScreen extends GuiScreen {
    private final List<CategoryWindow> windows = new ArrayList<>();
    private float openAnimationProgress = 0.0f;

    private long lastUpdateTime = System.nanoTime();

    @Override
    public void initGui() {
        windows.clear();
        int x = 20;
        for (ModuleCategory category : ModuleCategory.values()) {
            CategoryWindow categoryWindow = new CategoryWindow(category, x, 20);
            windows.add(categoryWindow);
            x += ClickGuiState.NEW_GUI_WIDTH + 10;
        }
        openAnimationProgress = 0.0f;
        lastUpdateTime = System.nanoTime();
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        if (openAnimationProgress < 1.0f) {
            long now = System.nanoTime();
            float deltaSec = (now - lastUpdateTime) / 1_000_000_000f;
            lastUpdateTime = now;
            openAnimationProgress = Lerper.animate(1.1f, openAnimationProgress, 10f * deltaSec);
            if (openAnimationProgress > 1.0f) {
                openAnimationProgress = 1.0f;
            }
        }

        float slideYOffset = (1.0f - openAnimationProgress) * 200f;

        int centerX = width / 2;
        int centerY = height / 2;

        BlurUtil.drawBlurredRect(0, 0, mc.displayWidth, mc.displayHeight, 10);

        GL11.glPushMatrix();

        float scale = DewCommon.moduleManager.getModule(ClickGui.class).getGuiScale() - 0.1f;
        float leftX = 0f;
        float topY = 0f;
        GL11.glTranslatef(leftX, topY, 0);
        GL11.glScalef(scale, scale, 1f);
        GL11.glTranslatef(-leftX, -topY, 0);

        GL11.glTranslatef(centerX, centerY + slideYOffset, 0);
        GL11.glScalef(openAnimationProgress, openAnimationProgress, 1);
        GL11.glTranslatef(-centerX, -centerY, 0);

        for (CategoryWindow window : windows) {
            float[] fixed = unscaleMouse(mouseX, mouseY);
            window.draw((int) fixed[0], (int) fixed[1]);
        }

        GL11.glPopMatrix();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (openAnimationProgress < 1.0f) return;
        float[] fixed = unscaleMouse(mouseX, mouseY);
        for (CategoryWindow window : windows) {
            window.mouseClicked((int) fixed[0], (int) fixed[1], mouseButton);
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        if (openAnimationProgress < 1.0f) return;
        float[] fixed = unscaleMouse(mouseX, mouseY);
        for (CategoryWindow window : windows) {
            window.mouseReleased((int) fixed[0], (int) fixed[1], mouseButton);
            window.resolveOverlap(windows);
        }
    }

    private float[] unscaleMouse(int mouseX, int mouseY) {
        float scale = DewCommon.moduleManager.getModule(ClickGui.class).getGuiScale() - 0.1f;
        float slideYOffset = (1.0f - openAnimationProgress) * 200f;
        int centerX = width / 2;
        int centerY = height / 2;

        float unscaledX = mouseX;
        float unscaledY = mouseY;

        unscaledX -= 0f;
        unscaledY -= 0f;
        unscaledX /= scale;
        unscaledY /= scale;
        unscaledX += 0f;
        unscaledY += 0f;

        unscaledX -= centerX;
        unscaledY -= (centerY + slideYOffset);
        unscaledX /= openAnimationProgress;
        unscaledY /= openAnimationProgress;
        unscaledX += centerX;
        unscaledY += centerY;

        return new float[]{ unscaledX, unscaledY };
    }

    @Override
    public void onGuiClosed() {
        openAnimationProgress = 0.0f;
        lastUpdateTime = System.nanoTime();
        DewCommon.clientConfigManager.save();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}