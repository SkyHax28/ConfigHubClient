package com.dew.system.gui;

import com.dew.DewCommon;
import com.dew.IMinecraft;
import com.dew.system.module.ModuleCategory;
import net.minecraft.client.gui.GuiScreen;
import org.lwjgl.input.Mouse;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class NewClickGuiScreen extends GuiScreen {
    private final List<CategoryWindow> windows = new ArrayList<>();

    @Override
    public void initGui() {
        windows.clear();
        int x = 20;
        for (ModuleCategory category : ModuleCategory.values()) {
            CategoryWindow categoryWindow = new CategoryWindow(category, x, 20);
            windows.add(categoryWindow);
            x += ClickGuiState.NEW_GUI_WIDTH + 10;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        for (CategoryWindow window : windows) {
            window.draw(mouseX, mouseY);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
        for (CategoryWindow window : windows) {
            window.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int mouseButton) {
        for (CategoryWindow window : windows) {
            window.mouseReleased(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();

        int dWheel = org.lwjgl.input.Mouse.getDWheel();
        if (dWheel != 0) {
            int scrollAmount = Integer.signum(dWheel) * 20;

            for (CategoryWindow window : DewCommon.clickGuiScreen.windows) {
                if (isMouseOver(window)) {
                    window.scrollOffset -= scrollAmount;
                    window.scrollOffset = Math.max(0, Math.min(window.scrollOffset, window.maxScroll));
                }
            }
        }
    }

    @Override
    public void onGuiClosed() {
        DewCommon.clientConfigManager.save();
    }

    private boolean isMouseOver(CategoryWindow window) {
        int mouseX = Mouse.getEventX() * IMinecraft.mc.displayWidth / IMinecraft.mc.displayWidth;
        int mouseY = IMinecraft.mc.displayHeight - Mouse.getEventY() * IMinecraft.mc.displayHeight / IMinecraft.mc.displayHeight - 1;

        return mouseX >= window.x && mouseX <= window.y + ClickGuiState.NEW_GUI_WIDTH &&
                mouseY >= window.y && mouseY <= window.y + window.headerHeight + 150;
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }
}