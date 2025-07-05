package com.dew.system.gui;

import com.dew.DewCommon;
import com.dew.IMinecraft;
import com.dew.system.module.ModuleCategory;
import com.dew.utils.Lerper;
import com.dew.utils.LogUtil;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NewClickGuiScreen extends GuiScreen {
    private final List<CategoryWindow> windows = new ArrayList<>();
    private float openAnimationProgress = 0.0f;

    private long lastUpdateTime = System.nanoTime();

    public void updateHeightAnimation() {
    }

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

        GL11.glPushMatrix();
        GL11.glTranslatef(centerX, centerY + slideYOffset, 0);
        GL11.glScalef(openAnimationProgress, openAnimationProgress, 1);
        GL11.glTranslatef(-centerX, -centerY, 0);

        for (CategoryWindow window : windows) {
            window.draw(mouseX, mouseY);
        }

        GL11.glPopMatrix();
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        if (openAnimationProgress < 1.0f) return;
        for (CategoryWindow window : windows) {
            window.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        if (openAnimationProgress < 1.0f) return;
        for (CategoryWindow window : windows) {
            window.mouseReleased(mouseX, mouseY, mouseButton);
            window.resolveOverlap(windows);
        }
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