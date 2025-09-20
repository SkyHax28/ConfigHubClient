package com.dew.system.gui;

import com.dew.DewCommon;
import com.dew.IMinecraft;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.settingsvalue.*;
import com.dew.utils.Lerper;
import com.dew.utils.font.CustomFontRenderer;
import com.dew.utils.shader.BlurUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

import java.awt.*;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class ClickGuiScreen extends GuiScreen {
    private final Minecraft mc = IMinecraft.mc;
    private final int resizeHandleSize = 10;
    private final int minWidth = 400, minHeight = 120;
    private final CustomFontRenderer fontRenderer = DewCommon.customFontRenderer;
    private boolean resizing = false;
    private int dragOffsetX, dragOffsetY;
    private boolean dragging = false;
    private long lastTime = System.nanoTime();

    private static int getContentHeight(List<Module> modules) {
        int contentHeight = 0;
        for (Module module : modules) {
            contentHeight += 14;
            if (ClickGuiState.expandedModules.contains(module)) {
                for (Value<?> value : module.getValues()) {
                    if (!value.isVisible()) continue;
                    if (value instanceof BooleanValue || value instanceof SelectionValue) {
                        contentHeight += 12;
                    } else if (value instanceof NumberValue) {
                        contentHeight += 21;
                    } else if (value instanceof MultiSelectionValue) {
                        MultiSelectionValue listVal = (MultiSelectionValue) value;
                        contentHeight += 12;
                        if (listVal.isExpanded()) contentHeight += 12 * listVal.getOptions().size();
                    }
                }
            }
        }
        return contentHeight;
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        long now = System.nanoTime();
        float deltaTime = (now - lastTime) / 1_000_000_000.0f;
        lastTime = now;

        float lerpSpeedPerSecond = 10f;
        float lerpAmount = 1.0f - (float) Math.pow(1.0f - 1.0f / lerpSpeedPerSecond, deltaTime * 60f);

        ClickGuiState.animatedX = Lerper.lerp(ClickGuiState.animatedX, ClickGuiState.x, lerpAmount);
        ClickGuiState.animatedY = Lerper.lerp(ClickGuiState.animatedY, ClickGuiState.y, lerpAmount);
        ClickGuiState.animatedWidth = Lerper.lerp(ClickGuiState.animatedWidth, ClickGuiState.width, lerpAmount);
        ClickGuiState.animatedHeight = Lerper.lerp(ClickGuiState.animatedHeight, ClickGuiState.height, lerpAmount);

        int x = (int) ClickGuiState.animatedX;
        int y = (int) ClickGuiState.animatedY;
        int w = (int) ClickGuiState.animatedWidth;
        int h = (int) ClickGuiState.animatedHeight;

        BlurUtil.drawBlurredRect(0, 0, mc.displayWidth, mc.displayHeight, 10);

        drawRect(x, y, x + w, y + h, new Color(20, 20, 20, 200).getRGB());

        drawGradientRect(x, y, x + w, y + 15, new Color(45, 45, 45).getRGB(), new Color(25, 25, 25).getRGB());

        drawRect(x - 1, y, x, y + h, new Color(0, 0, 0, 100).getRGB());
        drawRect(x + w, y, x + w + 1, y + h, new Color(0, 0, 0, 100).getRGB());
        drawRect(x, y - 1, x + w, y, new Color(0, 0, 0, 100).getRGB());
        drawRect(x, y + h, x + w, y + h + 1, new Color(0, 0, 0, 100).getRGB());

        drawRect(x + w - resizeHandleSize, y + h - resizeHandleSize, x + w, y + h, resizing || mouseX >= ClickGuiState.animatedX + ClickGuiState.animatedWidth - resizeHandleSize && mouseX <= ClickGuiState.animatedX + ClickGuiState.animatedWidth && mouseY >= ClickGuiState.animatedY + ClickGuiState.animatedHeight - resizeHandleSize && mouseY <= ClickGuiState.animatedY + ClickGuiState.animatedHeight ? new Color(255, 255, 255, 180).getRGB() : new Color(150, 150, 150, 180).getRGB());

        ModuleCategory[] categories = Arrays.stream(ModuleCategory.values())
                .filter(category -> category != ModuleCategory.MODULE_CONFIG_MANAGER && category != ModuleCategory.BIND_CONFIG_MANAGER)
                .toArray(ModuleCategory[]::new);

        for (int i = 0; i < categories.length; i++) {
            int tabX = (int) (ClickGuiState.animatedX + i * 60);
            int tabY = (int) ClickGuiState.animatedY;
            boolean isSelected = i == ClickGuiState.selectedCategory;

            int bgColor = isSelected ? new Color(70, 130, 180).getRGB() : new Color(0, 0, 0, 120).getRGB();

            drawRect(tabX, tabY, tabX + 60, tabY + 15, bgColor);
            drawRect(tabX, tabY, tabX + 60, tabY + 1, new Color(255, 255, 255, 40).getRGB());
            drawRect(tabX, tabY + 14, tabX + 60, tabY + 15, new Color(0, 0, 0, 100).getRGB());

            int textColor = isSelected ? Color.WHITE.getRGB() : new Color(200, 200, 200).getRGB();
            fontRenderer.drawCenteredStringWithShadow(categories[i].name().toLowerCase(), tabX + 30, tabY + 2, textColor, 0.28f);
        }

        List<Module> modules = DewCommon.moduleManager.getModules().stream()
                .filter(module -> module.category == categories[ClickGuiState.selectedCategory])
                .collect(Collectors.toList());

        int offsetY = (int) ClickGuiState.animatedY + 20 - ClickGuiState.scroll;

        for (Module module : modules) {
            if (offsetY + 12 < ClickGuiState.animatedY + 20) {
                offsetY += 14;
                if (ClickGuiState.expandedModules.contains(module)) {
                    for (Value<?> value : module.getValues()) {
                        if (!value.isVisible()) continue;
                        if (value instanceof BooleanValue || value instanceof SelectionValue) {
                            offsetY += 12;
                        } else if (value instanceof NumberValue) {
                            offsetY += 21;
                        } else if (value instanceof MultiSelectionValue) {
                            offsetY += 12;
                            MultiSelectionValue listVal = (MultiSelectionValue) value;
                            if (listVal.isExpanded()) offsetY += 12 * listVal.getOptions().size();
                        }
                    }
                }
                continue;
            }

            if (offsetY > ClickGuiState.animatedY + ClickGuiState.animatedHeight - 12) break;

            boolean hovered = mouseX >= ClickGuiState.animatedX + 5 && mouseX <= ClickGuiState.animatedX + ClickGuiState.animatedWidth - 5 && mouseY >= offsetY + 2 && mouseY <= offsetY + 12 - 2;
            boolean enabled = module.isEnabled();

            int color;
            if (enabled) {
                color = hovered ? 0xFFFFFF00 : 0xFFCCCC00;
            } else {
                color = hovered ? 0xFFAAAAAA : 0xFF777777;
            }

            String arrow = ClickGuiState.expandedModules.contains(module) && !module.getValues().isEmpty() ? "v" : !module.getValues().isEmpty() ? ">" : "  ";
            String label = arrow + " " + module.name + (module.key != Keyboard.KEY_NONE ? " [" + Keyboard.getKeyName(module.key) + "]" : "");
            fontRenderer.drawStringWithShadow(label, (int) ClickGuiState.animatedX + 5, offsetY, color, 0.28f);

            offsetY += 14;

            if (ClickGuiState.expandedModules.contains(module)) {
                for (Value<?> value : module.getValues()) {
                    if (!value.isVisible()) continue;

                    if (value instanceof BooleanValue) {
                        BooleanValue boolVal = (BooleanValue) value;
                        fontRenderer.drawStringWithShadow(" - " + boolVal.getName() + ": " + boolVal.get(), (int) ClickGuiState.animatedX + 10, offsetY, 0xFFFFFFFF, 0.28f);
                        offsetY += 12;
                    } else if (value instanceof NumberValue) {
                        NumberValue numVal = (NumberValue) value;

                        double min = numVal.getMin();
                        double max = numVal.getMax();
                        double val = numVal.get();

                        int sliderX = (int) ClickGuiState.animatedX + 10;
                        int sliderY = offsetY + 10;
                        int sliderWidth = 100;
                        int sliderHeight = 6;

                        drawRect(sliderX, sliderY, sliderX + sliderWidth, sliderY + sliderHeight, 0xFF444444);

                        double percent = (val - min) / (max - min);
                        int filledWidth = (int) (percent * sliderWidth);
                        drawRect(sliderX, sliderY, sliderX + filledWidth, sliderY + sliderHeight, 0xFF00AAFF);

                        fontRenderer.drawStringWithShadow(" - " + numVal.getName() + ": " + val, sliderX, offsetY - 2, 0xFFFFFFFF, 0.28f);

                        if (Mouse.isButtonDown(0) && !dragging && !resizing) {
                            if (mouseX >= sliderX && mouseX <= sliderX + sliderWidth && mouseY >= sliderY && mouseY <= sliderY + sliderHeight) {
                                double clickPercent = (double) (mouseX - sliderX) / sliderWidth;
                                double rawValue = min + (max - min) * clickPercent;

                                numVal.set(rawValue);
                            }
                        }

                        offsetY += 21;
                    } else if (value instanceof SelectionValue) {
                        SelectionValue selVal = (SelectionValue) value;
                        String prefix = selVal.isExpanded() ? " v " : " > ";

                        fontRenderer.drawStringWithShadow(prefix + selVal.getName() + ": " + selVal.get(), (int) ClickGuiState.animatedX + 10, offsetY, 0xFFFFFFFF, 0.28f);
                        offsetY += 12;

                        if (selVal.isExpanded()) {
                            for (String option : selVal.getOptions()) {
                                boolean selected = option.equals(selVal.get());
                                String display = "     " + (selected ? "> " : "") + option;
                                fontRenderer.drawStringWithShadow(display, (int) ClickGuiState.animatedX + 10, offsetY, selected ? 0xFF55FF55 : 0xFFAAAAAA, 0.28f);
                                offsetY += 12;
                            }
                        }
                    } else if (value instanceof MultiSelectionValue) {
                        MultiSelectionValue listVal = (MultiSelectionValue) value;
                        String prefix = listVal.isExpanded() ? " v " : " > ";

                        fontRenderer.drawStringWithShadow(prefix + listVal.getName() + ": " + listVal.get().stream().sorted().collect(Collectors.toList()), (int) ClickGuiState.animatedX + 10, offsetY, 0xFFFFFFFF, 0.28f);
                        offsetY += 12;

                        if (listVal.isExpanded()) {
                            for (String option : listVal.getOptions()) {
                                boolean selected = listVal.isSelected(option);
                                String display = "     " + (selected ? "[*] " : "[ ] ") + option;
                                fontRenderer.drawStringWithShadow(display, (int) ClickGuiState.animatedX + 10, offsetY, selected ? 0xFF55FF55 : 0xFFAAAAAA, 0.28f);
                                offsetY += 12;
                            }
                        }
                    }
                }
            }
        }

        ScaledResolution scaled = new ScaledResolution(mc);
        int screenWidth = scaled.getScaledWidth();
        int screenHeight = scaled.getScaledHeight();

        ClickGuiState.x = Math.max(0, Math.min(ClickGuiState.x, screenWidth - ClickGuiState.width));
        ClickGuiState.y = Math.max(0, Math.min(ClickGuiState.y, screenHeight - ClickGuiState.height));
        ClickGuiState.width = Math.max(minWidth, Math.min(ClickGuiState.width, screenWidth));
        ClickGuiState.height = Math.max(minHeight, Math.min(ClickGuiState.height, screenHeight));
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        super.mouseClicked(mouseX, mouseY, mouseButton);

        if (mouseX >= ClickGuiState.animatedX + ClickGuiState.animatedWidth - resizeHandleSize && mouseX <= ClickGuiState.animatedX + ClickGuiState.animatedWidth && mouseY >= ClickGuiState.animatedY + ClickGuiState.animatedHeight - resizeHandleSize && mouseY <= ClickGuiState.animatedY + ClickGuiState.animatedHeight && mouseButton == 0) {
            resizing = true;
            return;
        }

        ModuleCategory[] categories = Arrays.stream(ModuleCategory.values())
                .filter(category -> category != ModuleCategory.MODULE_CONFIG_MANAGER && category != ModuleCategory.BIND_CONFIG_MANAGER)
                .toArray(ModuleCategory[]::new);

        if (mouseButton == 0) {
            for (int i = 0; i < categories.length; i++) {
                int tabX = (int) ClickGuiState.animatedX + i * 60;
                if (mouseX >= tabX && mouseX <= tabX + 60 && mouseY >= ClickGuiState.animatedY && mouseY <= ClickGuiState.animatedY + 15) {
                    ClickGuiState.selectedCategory = i;
                    return;
                }
            }
        }

        if (mouseX >= ClickGuiState.animatedX && mouseX <= ClickGuiState.animatedX + ClickGuiState.animatedWidth && mouseY >= ClickGuiState.animatedY && mouseY <= ClickGuiState.animatedY + 15 && mouseButton == 0) {
            dragging = true;
            dragOffsetX = (int) (mouseX - ClickGuiState.animatedX);
            dragOffsetY = (int) (mouseY - ClickGuiState.animatedY);
            return;
        }

        int offsetY = (int) ClickGuiState.animatedY + 20 - ClickGuiState.scroll;
        List<Module> modules = com.dew.DewCommon.moduleManager.getModules().stream()
                .filter(m -> m.category == categories[ClickGuiState.selectedCategory])
                .collect(Collectors.toList());

        for (Module module : modules) {
            if (offsetY + 12 < ClickGuiState.animatedY + 20) {
                offsetY += 14;
                if (ClickGuiState.expandedModules.contains(module)) {
                    for (Value<?> value : module.getValues()) {
                        if (!value.isVisible()) continue;
                        if (value instanceof BooleanValue || value instanceof SelectionValue) {
                            offsetY += 12;
                        } else if (value instanceof NumberValue) {
                            offsetY += 21;
                        } else if (value instanceof MultiSelectionValue) {
                            offsetY += 12;
                            MultiSelectionValue listVal = (MultiSelectionValue) value;
                            if (listVal.isExpanded()) offsetY += 12 * listVal.getOptions().size();
                        }
                    }
                }
                continue;
            }

            if (offsetY > ClickGuiState.animatedY + ClickGuiState.animatedHeight - 12) break;

            int clickTop = offsetY + 2;
            int clickBottom = offsetY + 12 - 2;

            if (mouseX >= ClickGuiState.animatedX + 5 && mouseX <= ClickGuiState.animatedX + width - 5 && mouseY >= clickTop && mouseY <= clickBottom) {
                if (mouseButton == 0) {
                    module.toggleState();
                } else if (mouseButton == 1) {
                    if (ClickGuiState.expandedModules.contains(module)) {
                        ClickGuiState.expandedModules.remove(module);
                    } else if (!module.getValues().isEmpty()) {
                        ClickGuiState.expandedModules.add(module);
                    }
                }
                return;
            }

            offsetY += 14;

            if (ClickGuiState.expandedModules.contains(module)) {
                for (Value<?> value : module.getValues()) {
                    if (!value.isVisible()) continue;

                    if (offsetY + 12 < ClickGuiState.animatedY || offsetY > ClickGuiState.animatedY + ClickGuiState.animatedHeight - 1) {
                        if (value instanceof NumberValue) offsetY += 21;
                        else offsetY += 12;
                        continue;
                    }

                    if (value instanceof BooleanValue) {
                        if (mouseX >= ClickGuiState.animatedX + 10 && mouseX <= ClickGuiState.animatedX + width && mouseY >= offsetY + 2 && mouseY <= offsetY + 10 && mouseButton == 0) {
                            ((BooleanValue) value).set(!((BooleanValue) value).get());
                        }
                        offsetY += 12;
                    } else if (value instanceof SelectionValue) {
                        SelectionValue sel = (SelectionValue) value;

                        final int LINE_HEIGHT = 12;
                        final int CLICK_MARGIN = 2;

                        int clickTopSel = offsetY + CLICK_MARGIN;
                        int clickBottomSel = offsetY + LINE_HEIGHT - CLICK_MARGIN;

                        if (mouseY >= clickTopSel && mouseY <= clickBottomSel && mouseX >= ClickGuiState.animatedX + 10 && mouseX <= ClickGuiState.animatedX + width) {
                            if (mouseButton == 0) {
                                sel.toggleExpanded();
                            }
                        }

                        offsetY += LINE_HEIGHT;

                        if (sel.isExpanded()) {
                            for (String option : sel.getOptions()) {
                                int optClickTop = offsetY + CLICK_MARGIN;
                                int optClickBottom = offsetY + LINE_HEIGHT - CLICK_MARGIN;

                                if (mouseY >= optClickTop && mouseY <= optClickBottom && mouseX >= ClickGuiState.animatedX + 10 && mouseX <= ClickGuiState.animatedX + width && mouseButton == 0) {
                                    sel.setSelected(option);
                                }

                                offsetY += LINE_HEIGHT;
                            }
                        }
                    } else if (value instanceof NumberValue) {
                        offsetY += 21;
                    } else if (value instanceof MultiSelectionValue) {
                        MultiSelectionValue multi = (MultiSelectionValue) value;

                        if (mouseY >= offsetY + 2 && mouseY <= offsetY + 10 && mouseX >= ClickGuiState.animatedX + 10 && mouseX <= ClickGuiState.animatedX + width) {
                            if (mouseButton == 0) {
                                multi.toggleExpanded();
                            }
                        }

                        offsetY += 12;

                        if (multi.isExpanded()) {
                            for (String option : multi.getOptions()) {
                                int clickTopMulti = offsetY + 2;
                                int clickBottomMulti = offsetY + 10;

                                boolean inside = mouseY >= clickTopMulti && mouseY <= clickBottomMulti && mouseX >= ClickGuiState.animatedX + 10 && mouseX <= ClickGuiState.animatedX + width;

                                if (inside && mouseButton == 0) {
                                    multi.toggle(option);
                                }

                                offsetY += 12;
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void handleMouseInput() throws IOException {
        super.handleMouseInput();
        int scroll = Mouse.getEventDWheel();
        if (scroll != 0) {
            int SCROLL_STEP = 20;

            ModuleCategory[] categories = Arrays.stream(ModuleCategory.values())
                    .filter(category -> category != ModuleCategory.MODULE_CONFIG_MANAGER && category != ModuleCategory.BIND_CONFIG_MANAGER)
                    .toArray(ModuleCategory[]::new);

            List<Module> modules = DewCommon.moduleManager.getModules().stream()
                    .filter(module -> module.category == categories[ClickGuiState.selectedCategory])
                    .collect(Collectors.toList());

            int contentHeight = getContentHeight(modules);

            int visibleHeight = ClickGuiState.height - 20;
            int maxScroll = Math.max(0, contentHeight - visibleHeight);

            ClickGuiState.scroll -= Integer.signum(scroll) * SCROLL_STEP;
            ClickGuiState.scroll = Math.max(0, Math.min(ClickGuiState.scroll, maxScroll));
        }
    }

    @Override
    protected void mouseReleased(int mouseX, int mouseY, int state) {
        resizing = false;
        dragging = false;
    }

    @Override
    protected void mouseClickMove(int mouseX, int mouseY, int clickedMouseButton, long timeSinceLastClick) {
        if (resizing) {
            ClickGuiState.width = Math.max(minWidth, mouseX - ClickGuiState.x);
            ClickGuiState.height = Math.max(minHeight, mouseY - ClickGuiState.y);
            return;
        }

        if (dragging) {
            ClickGuiState.x = mouseX - dragOffsetX;
            ClickGuiState.y = mouseY - dragOffsetY;
        }
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    @Override
    public void onGuiClosed() {
        ClickGuiState.x = (int) ClickGuiState.animatedX;
        ClickGuiState.y = (int) ClickGuiState.animatedY;
        ClickGuiState.width = (int) ClickGuiState.animatedWidth;
        ClickGuiState.height = (int) ClickGuiState.animatedHeight;
        DewCommon.clientConfigManager.save();
    }
}
