package com.dew.system.gui;

import com.dew.DewCommon;
import com.dew.IMinecraft;
import com.dew.system.module.ModuleCategory;
import com.dew.utils.LogUtil;
import net.minecraft.client.gui.Gui;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;

public class CategoryWindow {
    private static final Random random = new Random();
    private final ModuleCategory category;
    private final List<ModuleButton> moduleButtons = new ArrayList<>();
    private final List<String> moduleConfigList;
    private final List<String> bindConfigList;
    private final int configButtonHeight = 14;
    public int x, y, headerHeight = 16;
    public boolean open;
    private boolean dragging;
    private int dragX, dragY;

    private final String categoryTitle;

    public CategoryWindow(ModuleCategory category, int x, int y) {
        this.category = category;
        this.categoryTitle = formatCategoryName(category.name());

        ClickGuiState.WindowState state = ClickGuiState.getOrCreate(category, x, y);
        this.x = state.x;
        this.y = state.y;
        this.open = state.open;

        DewCommon.moduleManager.getModules().stream()
                .filter(m -> m.category == category)
                .sorted((a, b) -> a.name.compareToIgnoreCase(b.name))
                .forEach(m -> moduleButtons.add(new ModuleButton(m)));

        moduleConfigList = DewCommon.moduleConfigManager.getConfigNames()
                .stream()
                .sorted(String::compareToIgnoreCase)
                .collect(Collectors.toList());

        bindConfigList = DewCommon.bindConfigManager.getConfigNames()
                .stream()
                .sorted(String::compareToIgnoreCase)
                .collect(Collectors.toList());
    }

    private String formatCategoryName(String name) {
        String lower = name.toLowerCase();
        if (lower.contains("_")) {
            String[] parts = lower.split("_");
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < parts.length; i++) {
                if (!parts[i].isEmpty()) {
                    builder.append(Character.toUpperCase(parts[i].charAt(0)))
                            .append(parts[i].substring(1));
                    if (i != parts.length - 1) builder.append(" ");
                }
            }
            return builder.toString();
        }
        return lower.substring(0, 1).toUpperCase() + lower.substring(1);
    }

    public void draw(int mouseX, int mouseY) {
        updatePosition(mouseX, mouseY);

        for (ModuleButton btn : moduleButtons) {
            btn.updateHeightAnimation();
        }

        drawBlurRect(x, y, x + ClickGuiState.NEW_GUI_WIDTH, y + headerHeight, new Color(0, 0, 0, 170));

        Gui.drawRect(x, y, x + ClickGuiState.NEW_GUI_WIDTH, y + 1, new Color(85, 153, 255, 180).getRGB());
        Gui.drawRect(x, y + headerHeight - 1, x + ClickGuiState.NEW_GUI_WIDTH, y + headerHeight, new Color(85, 153, 255, 120).getRGB());

        DewCommon.customFontRenderer.drawCenteredStringWithShadow(
                categoryTitle,
                x + ClickGuiState.NEW_GUI_WIDTH / 2f,
                y + 1,
                Color.WHITE.getRGB(),
                0.35f
        );

        if (open) {
            if (category == ModuleCategory.MODULE_CONFIG_MANAGER) {
                renderConfigThingy(moduleConfigList, mouseX, mouseY);
            } else if (category == ModuleCategory.BIND_CONFIG_MANAGER) {
                renderConfigThingy(bindConfigList, mouseX, mouseY);
            }

            int yOffset = headerHeight;
            for (ModuleButton btn : moduleButtons) {
                btn.draw(x, y + yOffset, ClickGuiState.NEW_GUI_WIDTH, mouseX, mouseY);
                yOffset += btn.getHeight();
            }
        }

        ClickGuiState.windowStates.get(category).x = x;
        ClickGuiState.windowStates.get(category).y = y;
        ClickGuiState.windowStates.get(category).open = open;
    }

    private void renderConfigThingy(List<String> configList, int mouseX, int mouseY) {
        int yOffset = headerHeight;

        boolean hovered = mouseX >= x && mouseX <= x + ClickGuiState.NEW_GUI_WIDTH && mouseY >= y + yOffset && mouseY <= y + yOffset + configButtonHeight - 1;
        Color bgColor = hovered ? new Color(85, 153, 255, 100) : new Color(0, 0, 0, 100);
        Gui.drawRect(x, y + yOffset, x + ClickGuiState.NEW_GUI_WIDTH, y + yOffset + configButtonHeight, bgColor.getRGB());
        DewCommon.customFontRenderer.drawCenteredStringWithShadow("Open folder", x + ClickGuiState.NEW_GUI_WIDTH / 2f, y + yOffset + 3, Color.WHITE.getRGB(), 0.3f);

        yOffset += configButtonHeight;

        for (String config : configList) {
            boolean hoveredConfig = mouseX >= x && mouseX <= x + ClickGuiState.NEW_GUI_WIDTH && mouseY >= y + yOffset && mouseY <= y + yOffset + configButtonHeight - 1;
            Color bgColorConfig = hoveredConfig ? new Color(85, 153, 255, 100) : new Color(0, 0, 0, 100);
            Gui.drawRect(x, y + yOffset, x + ClickGuiState.NEW_GUI_WIDTH, y + yOffset + configButtonHeight, bgColorConfig.getRGB());
            DewCommon.customFontRenderer.drawCenteredStringWithShadow(config, x + ClickGuiState.NEW_GUI_WIDTH / 2f, y + yOffset + 3, Color.WHITE.getRGB(), 0.3f);
            yOffset += configButtonHeight;
        }
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

        if (dragging) return;

        if (open) {
            if (category == ModuleCategory.MODULE_CONFIG_MANAGER || category == ModuleCategory.BIND_CONFIG_MANAGER) {
                int yOffset = headerHeight;

                int top = y + yOffset;
                int bottom = top + configButtonHeight;
                if (mouseX >= x && mouseX <= x + ClickGuiState.NEW_GUI_WIDTH && mouseY >= top && mouseY <= bottom - 1) {
                    if (button == 0) {
                        if (category == ModuleCategory.MODULE_CONFIG_MANAGER) {
                            LogUtil.printChat("Opening module config folder...");
                            DewCommon.moduleConfigManager.openFolder();
                        } else {
                            LogUtil.printChat("Opening bind config folder...");
                            DewCommon.bindConfigManager.openFolder();
                        }
                    }
                    return;
                }
                yOffset += configButtonHeight;

                if (category == ModuleCategory.MODULE_CONFIG_MANAGER) {
                    for (String config : moduleConfigList) {
                        top = y + yOffset;
                        bottom = top + configButtonHeight;

                        if (mouseX >= x && mouseX <= x + ClickGuiState.NEW_GUI_WIDTH && mouseY >= top && mouseY <= bottom - 1) {
                            if (button == 0) {
                                if (DewCommon.moduleConfigManager.load(config, DewCommon.moduleManager.getModules())) {
                                    LogUtil.printChat("Loaded module config: " + config);
                                } else {
                                    LogUtil.printChat("Module config " + config + " was not found");
                                }
                            } else if (button == 2) {
                                DewCommon.moduleConfigManager.save(config, DewCommon.moduleManager.getModules());
                                LogUtil.printChat("Saved module config: " + config);
                            }
                            break;
                        }
                        yOffset += configButtonHeight;
                    }
                    return;
                } else if (category == ModuleCategory.BIND_CONFIG_MANAGER) {
                    for (String config : bindConfigList) {
                        top = y + yOffset;
                        bottom = top + configButtonHeight;

                        if (mouseX >= x && mouseX <= x + ClickGuiState.NEW_GUI_WIDTH && mouseY >= top && mouseY <= bottom - 1) {
                            if (button == 0) {
                                if (DewCommon.bindConfigManager.load(config, DewCommon.moduleManager.getModules())) {
                                    LogUtil.printChat("Loaded bind config: " + config);
                                } else {
                                    LogUtil.printChat("Bind config " + config + " was not found");
                                }
                            } else if (button == 2) {
                                DewCommon.bindConfigManager.save(config, DewCommon.moduleManager.getModules());
                                LogUtil.printChat("Saved bind config: " + config);
                            }
                            break;
                        }
                        yOffset += configButtonHeight;
                    }
                    return;
                }
            }

            int yOffset = headerHeight;
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
            int yOffset = headerHeight;
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

    public void resolveOverlap(List<CategoryWindow> others) {
        int attempts = 0;
        while (attempts++ < 10) {
            boolean moved = false;

            for (CategoryWindow other : others) {
                if (other == this) continue;

                int thisLeft = this.x;
                int thisRight = this.x + ClickGuiState.NEW_GUI_WIDTH;
                int thisTop = this.y;
                int thisBottom = this.y + this.headerHeight;

                int otherLeft = other.x;
                int otherRight = other.x + ClickGuiState.NEW_GUI_WIDTH;
                int otherTop = other.y;
                int otherBottom = other.y + other.headerHeight;

                boolean overlapX = thisLeft < otherRight && thisRight > otherLeft;
                boolean overlapY = thisTop < otherBottom && thisBottom > otherTop;

                if (overlapX && overlapY) {
                    int dx = random.nextBoolean() ? 15 : -15;
                    int dy = random.nextBoolean() ? 15 : -15;

                    this.x += dx;
                    this.y += dy;

                    int screenWidth = IMinecraft.mc.displayWidth / IMinecraft.mc.gameSettings.guiScale;
                    int screenHeight = IMinecraft.mc.displayHeight / IMinecraft.mc.gameSettings.guiScale;

                    this.x = Math.max(0, Math.min(this.x, screenWidth - ClickGuiState.NEW_GUI_WIDTH));
                    this.y = Math.max(0, Math.min(this.y, screenHeight - this.headerHeight - 100));

                    moved = true;
                    break;
                }
            }

            if (!moved) break;
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