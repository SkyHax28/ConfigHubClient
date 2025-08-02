package com.dew.system.gui;

import com.dew.DewCommon;
import com.dew.IMinecraft;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.settingsvalue.Value;
import com.dew.utils.font.CustomFontRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;

import java.awt.*;
import java.io.IOException;
import java.util.*;
import java.util.List;
import java.util.stream.Collectors;

public class ClickGuiDropdownScreen extends GuiScreen {
    private final Minecraft mc = IMinecraft.mc;
    private final CustomFontRenderer fontRenderer = DewCommon.customFontRenderer;
    private final Map<ModuleCategory, CategoryWindow> categoryWindows = new EnumMap<>(ModuleCategory.class);

    @Override
    public void initGui() {
        int startX = 20;
        int startY = 20;
        for (ModuleCategory category : ModuleCategory.values()) {
            categoryWindows.put(category, new CategoryWindow(category, startX, startY));
            startX += 120;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        for (CategoryWindow window : categoryWindows.values()) {
            window.render(mouseX, mouseY);
        }
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        for (CategoryWindow window : categoryWindows.values()) {
            window.mouseClicked(mouseX, mouseY, mouseButton);
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        for (CategoryWindow window : categoryWindows.values()) {
            window.mouseReleased(mouseX, mouseY, state);
        }
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        for (CategoryWindow window : categoryWindows.values()) {
            window.mouseDragged(mouseX, mouseY);
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private class CategoryWindow {
        private final ModuleCategory category;
        private final List<Module> modules;
        private final Set<Module> expanded = new HashSet<>();
        private int x, y;
        private final int width = 110;
        private int height;
        private boolean dragging;
        private int dragOffsetX, dragOffsetY;

        public CategoryWindow(ModuleCategory category, int x, int y) {
            this.category = category;
            this.x = x;
            this.y = y;
            this.modules = DewCommon.moduleManager.getModules().stream()
                    .filter(m -> m.category == category)
                    .collect(Collectors.toList());
        }

        public void render(int mouseX, int mouseY) {
            int offsetY = y;
            drawRect(x, y, x + width, y + 15, new Color(60, 60, 60).getRGB());
            fontRenderer.drawCenteredStringWithShadow(category.name(), x + width / 2, y + 2, Color.WHITE.getRGB(), 0.5f);

            offsetY += 17;
            for (Module module : modules) {
                fontRenderer.drawStringWithShadow(module.name, x + 4, offsetY, module.isEnabled() ? Color.YELLOW.getRGB() : Color.GRAY.getRGB(), 0.5f);
                offsetY += 12;
                if (expanded.contains(module)) {
                    for (Value<?> value : module.getValues()) {
                        if (!value.isVisible()) continue;
                        fontRenderer.drawStringWithShadow("- " + value.getName(), x + 8, offsetY, Color.LIGHT_GRAY.getRGB(), 0.5f);
                        offsetY += 10;
                    }
                }
            }
            height = offsetY - y;
        }

        public void mouseClicked(int mouseX, int mouseY, int mouseButton) {
            if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 15 && mouseButton == 0) {
                dragging = true;
                dragOffsetX = mouseX - x;
                dragOffsetY = mouseY - y;
                return;
            }

            int offsetY = y + 17;
            for (Module module : modules) {
                if (mouseX >= x && mouseX <= x + width && mouseY >= offsetY && mouseY <= offsetY + 12) {
                    if (mouseButton == 0) {
                        module.toggleState();
                    } else if (mouseButton == 1) {
                        if (expanded.contains(module)) expanded.remove(module);
                        else expanded.add(module);
                    }
                    return;
                }
                offsetY += 12;
                if (expanded.contains(module)) {
                    for (Value<?> value : module.getValues()) {
                        if (value.isVisible()) offsetY += 10;
                    }
                }
            }
        }

        public void mouseReleased(int mouseX, int mouseY, int state) {
            dragging = false;
        }

        public void mouseDragged(int mouseX, int mouseY) {
            if (dragging) {
                this.x = mouseX - dragOffsetX;
                this.y = mouseY - dragOffsetY;
            }
        }
    }
}
