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

    public ModuleButton(Module module) {
        this.module = module;
    }

    private void updateVisibleComponents() {
        List<ValueComponentHolder> newList = new ArrayList<>();

        for (Value<?> v : module.getValues()) {
            if (!v.isVisible()) continue;

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

        valueComponents.clear();
        valueComponents.addAll(newList);
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

        drawBlurRect(x, y, x + width, y + 18, module.isEnabled() ? new Color(85, 153, 255, 90) : new Color(10, 10, 10, 120));

        if (isHovered) {
            Gui.drawRect(x, y, x + width, y + 18, new Color(255, 255, 255, 50).getRGB());
        }

        Gui.drawRect(x, y, x + width, y + 1, new Color(85, 153, 255, 180).getRGB());
        Gui.drawRect(x, y + 17, x + width, y + 18, new Color(85, 153, 255, 120).getRGB());

        DewCommon.customFontRenderer.drawStringWithShadow(module.name, x + 4 + (hasSettings ? 2 : 0), y + 2, Color.WHITE.getRGB(), 0.35f);

        if (module.key != Keyboard.KEY_NONE) {
            String keyInfo = "(" + Keyboard.getKeyName(module.key) + ")";
            DewCommon.customFontRenderer.drawStringWithShadow(keyInfo, x + width - DewCommon.customFontRenderer.getStringWidth(keyInfo, 0.35f) - 2, y + 2, Color.LIGHT_GRAY.getRGB(), 0.35f);
        }

        if (module.isEnabled()) {
            glowProgress += 0.02f;

            float phase = (float) Math.sin(glowProgress);
            int barHeight = 2;

            for (int i = 0; i < width; i++) {
                float fade = (float) (Math.sin((i / (float) width + glowProgress) * Math.PI * 2) * 0.5 + 0.5);
                int alpha = (int) (fade * 50);

                Color c = new Color(255, 200, 150, alpha);

                Gui.drawRect(x + i, y + (int) (16 + phase * 1.5f), x + i + 1, y + (int) (16 + phase * 1.5f) + barHeight, c.getRGB());
            }
        } else {
            glowProgress = 0f;
        }

        if (hasSettings) {
            int stripeWidth = 2;
            Color stripeColor = new Color(150, 180, 255, 180);

            Gui.drawRect(
                    x,
                    y + 1,
                    x + stripeWidth,
                    y + 17,
                    expanded ? Color.YELLOW.getRGB() : stripeColor.getRGB()
            );
        }

        if (currentHeight > 18f && module.isGuiExpanded()) {
            int yOffset = 18;
            for (ValueComponentHolder holder : valueComponents) {
                holder.component.draw(x + 4, y + yOffset, width - 8, mouseX, mouseY);
                yOffset += holder.component.getHeight();
            }
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
        for (int i = 0; i < 6; i++) {
            int alpha = (int) (color.getAlpha() * (1f - (i / 6f)));
            Color blurred = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
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