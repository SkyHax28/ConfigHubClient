package com.dew.system.gui;

import com.dew.DewCommon;
import com.dew.IMinecraft;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.utils.LogUtil;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class CategoryWindow {
    private final ModuleCategory category;
    private final List<ModuleButton> moduleButtons = new ArrayList<>();
    public int x, y, headerHeight = 16;
    private boolean open;
    private boolean dragging;
    private int dragX, dragY;
    public int scrollOffset;
    public int maxScroll = 0;

    public CategoryWindow(ModuleCategory category, int x, int y) {
        this.category = category;

        ClickGuiState.WindowState state = ClickGuiState.getOrCreate(category, x, y);
        this.x = state.x;
        this.y = state.y;
        this.open = state.open;
        this.scrollOffset = state.scrollOffset;

        for (Module m : DewCommon.moduleManager.getModules()) {
            if (m.category == category)
                moduleButtons.add(new ModuleButton(m));
        }
    }

    public void draw(int mouseX, int mouseY) {
        updatePosition(mouseX, mouseY);

        drawBlurRect(x, y, x + ClickGuiState.NEW_GUI_WIDTH, y + headerHeight, new Color(0, 0, 0, 170));

        Gui.drawRect(x, y, x + ClickGuiState.NEW_GUI_WIDTH, y + 1, new Color(85, 153, 255, 180).getRGB());
        Gui.drawRect(x, y + headerHeight - 1, x + ClickGuiState.NEW_GUI_WIDTH, y + headerHeight, new Color(85, 153, 255, 120).getRGB());

        String categoryTitle = category.name().substring(0,1).toUpperCase() + category.name().substring(1).toLowerCase();
        DewCommon.customFontRenderer.drawCenteredStringWithShadow(
                categoryTitle,
                x + 60,
                y + 1,
                Color.WHITE.getRGB(),
                0.35f
        );

        if (open) {
            int yOffset = headerHeight - scrollOffset;
            int totalContentHeight = 0;
            for (ModuleButton btn : moduleButtons) {
                btn.draw(x, y + yOffset, ClickGuiState.NEW_GUI_WIDTH, mouseX, mouseY);
                yOffset += btn.getHeight();
                totalContentHeight += btn.getHeight();
            }

            maxScroll = Math.max(0, totalContentHeight - (IMinecraft.mc.displayHeight - y - 50));
        }

        ClickGuiState.windowStates.get(category).x = x;
        ClickGuiState.windowStates.get(category).y = y;
        ClickGuiState.windowStates.get(category).open = open;
        ClickGuiState.windowStates.get(category).scrollOffset = scrollOffset;
    }

    public void mouseClicked(int mouseX, int mouseY, int button) {
        if (mouseX >= x && mouseX <= x + ClickGuiState.NEW_GUI_WIDTH && mouseY >= y && mouseY <= y + headerHeight) {
            if (button == 0) {
                dragging = true;
                dragX = mouseX - x;
                dragY = mouseY - y;
            } else if (button == 1) {
                this.open = !this.open;
            }
        }

        if (open) {
            int yOffset = headerHeight - scrollOffset;
            for (ModuleButton btn : moduleButtons) {
                btn.mouseClicked(mouseX, mouseY, button, x, y + yOffset, ClickGuiState.NEW_GUI_WIDTH);
                yOffset += btn.getHeight();
            }
        }
    }

    public void mouseReleased(int mouseX, int mouseY, int button) {
        if (dragging && button == 0) {
            dragging = false;
            x = (x / 10) * 10;
            y = (y / 10) * 10;
        }

        if (open) {
            int yOffset = headerHeight - scrollOffset;
            for (ModuleButton btn : moduleButtons) {
                btn.mouseReleased(mouseX, mouseY + yOffset, button);
                yOffset += btn.getHeight();
            }
        }
    }

    public void updatePosition(int mouseX, int mouseY) {
        if (dragging) {
            x = mouseX - dragX;
            y = mouseY - dragY;
        }
    }

    private void drawBlurRect(int left, int top, int right, int bottom, Color color) {
        for (int i = 0; i < 6; i++) {
            int alpha = (int) (color.getAlpha() * (1f - (i / 6f)));
            Color blurred = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
            Gui.drawRect(left - i, top - i, right + i, bottom + i, blurred.getRGB());
        }
    }
}