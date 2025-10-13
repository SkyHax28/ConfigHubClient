package com.dew.system.gui;

import com.dew.DewCommon;
import com.dew.system.module.Module;
import com.dew.system.settingsvalue.*;
import com.dew.utils.Lerper;
import net.minecraft.client.gui.Gui;
import org.lwjgl.input.Keyboard;

import java.awt.*;
import java.util.ArrayList;
import java.util.List;

public class ModuleButton {
    private final Module module;
    private final List<ValueComponentHolder> valueComponents = new ArrayList<>();
    private float currentHeight = 18f;
    private float glowProgress = 0f;

    private long lastUpdateTime = System.nanoTime();

    private static final Color COLOR_ENABLED_BG = new Color(85, 153, 255, 90);
    private static final Color COLOR_DISABLED_BG = new Color(10, 10, 10, 120);
    private static final Color COLOR_HOVER = new Color(255, 255, 255, 50);
    private static final int COLOR_HOVER_RGB = COLOR_HOVER.getRGB();
    private static final Color COLOR_TOP_BORDER = new Color(85, 153, 255, 180);
    private static final int COLOR_TOP_BORDER_RGB = COLOR_TOP_BORDER.getRGB();
    private static final Color COLOR_BOTTOM_BORDER = new Color(85, 153, 255, 120);
    private static final int COLOR_BOTTOM_BORDER_RGB = COLOR_BOTTOM_BORDER.getRGB();
    private static final int WHITE_RGB = Color.WHITE.getRGB();
    private static final int LIGHT_GRAY_RGB = Color.LIGHT_GRAY.getRGB();
    private static final int YELLOW_RGB = Color.YELLOW.getRGB();
    private static final Color COLOR_DISABLED_STRIPE = new Color(255, 0, 0, 180);
    private static final int COLOR_DISABLED_STRIPE_RGB = COLOR_DISABLED_STRIPE.getRGB();
    private static final Color COLOR_SETTINGS_STRIPE = new Color(150, 180, 255, 180);
    private static final int COLOR_SETTINGS_STRIPE_RGB = COLOR_SETTINGS_STRIPE.getRGB();

    private boolean cachedHasSettings = false;
    private String cachedKeyInfo = null;
    private float cachedKeyInfoWidth = 0;
    private int lastKeyCode = -1;

    public ModuleButton(Module module) {
        this.module = module;
    }

    private void updateVisibleComponents() {
        List<ValueComponentHolder> newList = new ArrayList<>();
        boolean hasVisibleComponents = false;

        for (Value<?> v : module.getValues()) {
            if (!v.isVisible()) continue;

            hasVisibleComponents = true;
            ValueComponentHolder existingHolder = null;
            for (ValueComponentHolder holder : valueComponents) {
                if (holder.value == v) {
                    existingHolder = holder;
                    break;
                }
            }

            if (existingHolder != null) {
                newList.add(existingHolder);
            } else {
                newList.add(new ValueComponentHolder(v, createComponentFor(v)));
            }
        }

        if (newList.size() != valueComponents.size() || hasVisibleComponents != cachedHasSettings) {
            valueComponents.clear();
            valueComponents.addAll(newList);
            cachedHasSettings = hasVisibleComponents;
        }
    }

    private ValueComponent createComponentFor(Value<?> v) {
        if (v instanceof BooleanValue)
            return new BooleanComponent((BooleanValue) v);
        else if (v instanceof NumberValue)
            return new NumberComponent((NumberValue) v);
        else if (v instanceof SelectionValue)
            return new SelectionComponent((SelectionValue) v);
        else if (v instanceof MultiSelectionValue)
            return new MultiSelectionComponent((MultiSelectionValue) v);
        throw new IllegalArgumentException("Unknown Value type: " + v.getClass());
    }

    public void draw(int x, int y, int width, int mouseX, int mouseY) {
        updateVisibleComponents();

        boolean hasSettings = !valueComponents.isEmpty();
        boolean isHovered = mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 17;
        boolean expanded = module.isGuiExpanded();
        boolean enabled = module.isEnabled();

        drawBlurRect(x, y, x + width, y + 18, enabled ? COLOR_ENABLED_BG : COLOR_DISABLED_BG);

        if (isHovered) {
            Gui.drawRect(x, y, x + width, y + 18, COLOR_HOVER_RGB);
        }

        Gui.drawRect(x, y, x + width, y + 1, COLOR_TOP_BORDER_RGB);
        Gui.drawRect(x, y + 17, x + width, y + 18, COLOR_BOTTOM_BORDER_RGB);

        DewCommon.customFontRenderer.drawStringWithShadow(module.name, x + 4 + (hasSettings ? 2 : 0), y + 2, WHITE_RGB, 0.35f);

        if (module.key != Keyboard.KEY_NONE) {
            if (module.key != lastKeyCode) {
                String keyName = Keyboard.getKeyName(module.key);
                cachedKeyInfo = "(" + keyName + ")";
                cachedKeyInfoWidth = DewCommon.customFontRenderer.getStringWidth(cachedKeyInfo, 0.35f);
                lastKeyCode = module.key;
            }
            if (cachedKeyInfo != null) {
                DewCommon.customFontRenderer.drawStringWithShadow(cachedKeyInfo,
                        x + width - cachedKeyInfoWidth - 2, y + 2, LIGHT_GRAY_RGB, 0.35f);
            }
        }

        if (enabled) {
            glowProgress += 0.02f;
            drawGlowEffect(x, y, width, glowProgress);
        } else {
            glowProgress = 0f;
        }

        drawSideBar(x, y, expanded, hasSettings);

        if (currentHeight > 18f && expanded) {
            int yOffset = 18;
            for (ValueComponentHolder holder : valueComponents) {
                holder.component.draw(x + 4, y + yOffset, width - 8, mouseX, mouseY);
                yOffset += holder.component.getHeight();
            }
        }
    }

    private void drawGlowEffect(int x, int y, int width, float glowProgress) {
        int barHeight = 2;
        float phase = (float) Math.sin(glowProgress);

        for (int i = 0; i < width; i++) {
            float fade = (float) (Math.sin((i / (float) width + glowProgress) * Math.PI * 2) * 0.5 + 0.5);
            int alpha = (int) (fade * 50);

            if (alpha > 0) {
                Color c = new Color(255, 200, 150, alpha);
                Gui.drawRect(x + i, y + (int) (16 + phase * 1.5f), x + i + 1, y + (int) (16 + phase * 1.5f) + barHeight, c.getRGB());
            }
        }
    }

    private void drawSideBar(int x, int y, boolean expanded, boolean hasSettings) {
        int stripeWidth = 2;
        int stripeColor = YELLOW_RGB;

        if (!expanded) {
            stripeColor = module.canBeEnabled ? (hasSettings ? COLOR_SETTINGS_STRIPE_RGB : 0) : COLOR_DISABLED_STRIPE_RGB;
        }

        if (stripeColor != 0 && (hasSettings || !module.canBeEnabled)) {
            Gui.drawRect(x, y + 1, x + stripeWidth, y + 17, stripeColor);
        }
    }

    public void mouseClicked(int mouseX, int mouseY, int button, int x, int y, int width) {
        if (mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 17) {
            if (button == 0) {
                module.toggleState();
            } else if (button == 1) {
                module.setGuiExpanded(!module.isGuiExpanded());
            }
        }

        if (module.isGuiExpanded()) {
            int yOffset = 18;
            for (ValueComponentHolder holder : valueComponents) {
                holder.component.mouseClicked(mouseX, mouseY, button, x + 4, y + yOffset);
                yOffset += holder.component.getHeight();
            }
        }
    }

    public void mouseReleased(int mouseX, int mouseY, int button) {
        if (module.isGuiExpanded()) {
            int yOffset = 18;
            for (ValueComponentHolder holder : valueComponents) {
                holder.component.mouseReleased(mouseX, mouseY + yOffset, button);
                yOffset += holder.component.getHeight();
            }
        }
    }

    public int getTargetHeight() {
        if (!module.isGuiExpanded()) return 18;
        int height = 18;
        for (ValueComponentHolder holder : valueComponents) {
            height += holder.component.getHeight();
        }
        return height;
    }

    public int getHeight() {
        return (int) currentHeight;
    }

    public void updateHeightAnimation() {
        float targetHeight = getTargetHeight();
        long now = System.nanoTime();
        float deltaSec = (now - lastUpdateTime) / 1_000_000_000f;
        lastUpdateTime = now;
        currentHeight = Lerper.animate(targetHeight, currentHeight, 30f * deltaSec);
    }

    private void drawBlurRect(int left, int top, int right, int bottom, Color color) {
        int baseAlpha = color.getAlpha();
        int r = color.getRed();
        int g = color.getGreen();
        int b = color.getBlue();

        for (int i = 0; i < 6; i++) {
            int alpha = (int) (baseAlpha * (1f - (i / 6f)));
            if (alpha <= 0) break;

            Color blurred = new Color(r, g, b, alpha);
            Gui.drawRect(left - i, top - i, right + i, bottom + i, blurred.getRGB());
        }
    }

    private static class ValueComponentHolder {
        final Value<?> value;
        final ValueComponent component;

        ValueComponentHolder(Value<?> value, ValueComponent component) {
            this.value = value;
            this.component = component;
        }
    }
}