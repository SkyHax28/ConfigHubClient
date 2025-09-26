package com.dew.system.gui;

import com.dew.DewCommon;
import com.dew.IMinecraft;
import com.dew.system.module.ModuleCategory;
import com.dew.utils.Lerper;
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
    private final int configButtonHeight = 18;
    public int x, y, headerHeight = 22;
    public boolean open;
    private boolean dragging;
    private int dragX, dragY;

    private float expandAnimation = 0f;
    private long lastTime = System.currentTimeMillis();

    private static final Color BACKGROUND_PRIMARY = new Color(25, 25, 35, 240);
    private static final Color BACKGROUND_SECONDARY = new Color(35, 35, 45, 220);
    private static final Color ACCENT_COLOR = new Color(100, 150, 255, 200);
    private static final Color HOVER_COLOR = new Color(120, 170, 255, 150);
    private static final Color TEXT_PRIMARY = new Color(255, 255, 255, 255);
    private static final Color BORDER_COLOR = new Color(60, 60, 80, 180);

    private final String categoryTitle;

    public CategoryWindow(ModuleCategory category, int x, int y) {
        this.category = category;
        this.categoryTitle = formatCategoryName(category.name());

        ClickGuiState.WindowState state = ClickGuiState.getOrCreate(category, x, y);
        this.x = state.x;
        this.y = state.y;
        this.open = state.open;

        this.expandAnimation = open ? 1f : 0f;

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
        updateAnimations();

        for (ModuleButton btn : moduleButtons) {
            btn.updateHeightAnimation();
        }

        boolean headerHovered = mouseX >= x && mouseX <= x + ClickGuiState.NEW_GUI_WIDTH &&
                mouseY >= y && mouseY <= y + headerHeight;

        drawGradientRect(x, y, x + ClickGuiState.NEW_GUI_WIDTH, y + headerHeight,
                BACKGROUND_PRIMARY, BACKGROUND_SECONDARY);

        if (headerHovered) {
            drawRect(x, y, x + ClickGuiState.NEW_GUI_WIDTH, y + headerHeight,
                    new Color(HOVER_COLOR.getRed(), HOVER_COLOR.getGreen(), HOVER_COLOR.getBlue(), 30));
        }

        drawRect(x, y, x + ClickGuiState.NEW_GUI_WIDTH, y + 2, ACCENT_COLOR);

        drawBorder(x, y, x + ClickGuiState.NEW_GUI_WIDTH, y + headerHeight, BORDER_COLOR);

        DewCommon.customFontRenderer.drawCenteredStringWithShadow(
                categoryTitle,
                x + ClickGuiState.NEW_GUI_WIDTH / 2f,
                y + 4,
                TEXT_PRIMARY.getRGB(),
                0.4f
        );

        if (expandAnimation > 0.01f) {
            int contentHeight = calculateContentHeight();
            int animatedHeight = (int)(contentHeight * expandAnimation);

            if (animatedHeight > 0) {
                drawGradientRect(x, y + headerHeight, x + ClickGuiState.NEW_GUI_WIDTH,
                        y + headerHeight + animatedHeight,
                        new Color(BACKGROUND_SECONDARY.getRed(), BACKGROUND_SECONDARY.getGreen(),
                                BACKGROUND_SECONDARY.getBlue(), (int)(BACKGROUND_SECONDARY.getAlpha() * expandAnimation)),
                        new Color(BACKGROUND_PRIMARY.getRed(), BACKGROUND_PRIMARY.getGreen(),
                                BACKGROUND_PRIMARY.getBlue(), (int)(BACKGROUND_PRIMARY.getAlpha() * expandAnimation)));

                drawBorder(x, y + headerHeight, x + ClickGuiState.NEW_GUI_WIDTH,
                        y + headerHeight + animatedHeight,
                        new Color(BORDER_COLOR.getRed(), BORDER_COLOR.getGreen(),
                                BORDER_COLOR.getBlue(), (int)(BORDER_COLOR.getAlpha() * expandAnimation)));
            }

            if (open && expandAnimation > 0.8f) {
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
        }

        ClickGuiState.windowStates.get(category).x = x;
        ClickGuiState.windowStates.get(category).y = y;
        ClickGuiState.windowStates.get(category).open = open;
    }

    private void updateAnimations() {
        long currentTime = System.currentTimeMillis();
        float deltaTime = (currentTime - lastTime) / 1000f;
        lastTime = currentTime;

        float targetExpand = open ? 1f : 0f;
        float speed = 10f;

        if (Math.abs(expandAnimation - targetExpand) > 0.01f) {
            expandAnimation = Lerper.lerp(expandAnimation, targetExpand, speed * deltaTime);
            expandAnimation = Math.max(0f, Math.min(1f, expandAnimation));
        }
    }

    private int calculateContentHeight() {
        if (category == ModuleCategory.MODULE_CONFIG_MANAGER ||
                category == ModuleCategory.BIND_CONFIG_MANAGER) {
            List<String> configList = category == ModuleCategory.MODULE_CONFIG_MANAGER ?
                    moduleConfigList : bindConfigList;
            return configButtonHeight * (configList.size() + 1);
        }

        int height = 0;
        for (ModuleButton btn : moduleButtons) {
            height += btn.getHeight();
        }
        return height;
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
        if (mouseX >= x && mouseX <= x + ClickGuiState.NEW_GUI_WIDTH &&
                mouseY >= y && mouseY <= y + headerHeight) {
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

    private void drawRect(int left, int top, int right, int bottom, Color color) {
        Gui.drawRect(left, top, right, bottom, color.getRGB());
    }

    private void drawGradientRect(int left, int top, int right, int bottom, Color startColor, Color endColor) {
        int steps = bottom - top;
        for (int i = 0; i < steps; i++) {
            float ratio = (float) i / steps;
            int r = (int) (startColor.getRed() + (endColor.getRed() - startColor.getRed()) * ratio);
            int g = (int) (startColor.getGreen() + (endColor.getGreen() - startColor.getGreen()) * ratio);
            int b = (int) (startColor.getBlue() + (endColor.getBlue() - startColor.getBlue()) * ratio);
            int a = (int) (startColor.getAlpha() + (endColor.getAlpha() - startColor.getAlpha()) * ratio);

            Gui.drawRect(left, top + i, right, top + i + 1, new Color(r, g, b, a).getRGB());
        }
    }

    private void drawBorder(int left, int top, int right, int bottom, Color color) {
        Gui.drawRect(left, top, left + 1, bottom, color.getRGB());
        Gui.drawRect(right - 1, top, right, bottom, color.getRGB());
        Gui.drawRect(left, top, right, top + 1, color.getRGB());
        Gui.drawRect(left, bottom - 1, right, bottom, color.getRGB());
    }
}