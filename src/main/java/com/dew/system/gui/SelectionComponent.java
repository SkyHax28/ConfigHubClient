package com.dew.system.gui;

import com.dew.DewCommon;
import com.dew.system.settingsvalue.SelectionValue;
import com.dew.utils.Lerper;
import net.minecraft.client.gui.Gui;

import java.awt.*;
import java.util.HashMap;
import java.util.Map;

class SelectionComponent implements ValueComponent {
    private final SelectionValue value;
    private float animatedHeight = 16f;
    private static final Map<SelectionValue, Boolean> expandedMap = new HashMap<>();

    public SelectionComponent(SelectionValue value) {
        this.value = value;
        expandedMap.putIfAbsent(value, false);
    }

    private boolean isExpanded() {
        return expandedMap.getOrDefault(value, false);
    }

    private void toggleExpanded() {
        expandedMap.put(value, !isExpanded());
    }

    @Override
    public void draw(int x, int y, int width, int mouseX, int mouseY) {
        drawBlurRect(x, y, x + width, y + 16, new Color(30, 30, 30, 120));
        Gui.drawRect(x, y, x + width, y + 1, new Color(85, 153, 255, 150).getRGB());
        Gui.drawRect(x, y + 15, x + width, y + 16, new Color(85, 153, 255, 100).getRGB());
        if (mouseX >= x && mouseX <= x + ClickGuiState.NEW_GUI_WIDTH && mouseY >= y && mouseY <= y + 15) {
            Gui.drawRect(x, y, x + width, y + 18, new Color(255, 255, 255, 50).getRGB());
        }

        String label = value.getName() + ": " + value.get();
        DewCommon.customFontRenderer.drawStringWithShadow(label, x + 4, y + 1, Color.WHITE.getRGB(), 0.35f);

        int targetHeight = isExpanded() ? 16 + value.getOptions().size() * 16 : 16;
        animatedHeight = Lerper.lerp(animatedHeight, targetHeight, 0.3f);

        if (animatedHeight > 16.5f) {
            int offsetY = 16;
            for (String option : value.getOptions()) {
                if (offsetY >= animatedHeight) break;
                boolean selected = value.get().equals(option);
                drawBlurRect(x, y + offsetY, x + width, y + offsetY + 16, new Color(30, 30, 30, 120));
                Gui.drawRect(x, y + offsetY, x + width, y + offsetY + 16, selected ? 0xFF5599FF : 0xFF222222);
                DewCommon.customFontRenderer.drawString(option, x + 12, y + offsetY + 1, Color.WHITE.getRGB(), 0.35f);
                offsetY += 16;
            }
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button, int x, int y) {
        if (mouseX >= x && mouseX <= x + ClickGuiState.NEW_GUI_WIDTH && mouseY >= y && mouseY <= y + 15) {
            if (button == 1) {
                toggleExpanded();
                return;
            } else if (button == 0 && !isExpanded()) {
                value.next();
                return;
            }
        }

        if (isExpanded()) {
            int offsetY = 16;
            for (String option : value.getOptions()) {
                if (mouseX >= x && mouseX <= x + ClickGuiState.NEW_GUI_WIDTH && mouseY >= y + offsetY && mouseY <= y + offsetY + 15 && button == 0) {
                    value.set(option);
                    break;
                }
                offsetY += 16;
            }
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int button) {
    }

    @Override
    public int getHeight() {
        return (int) animatedHeight;
    }

    private void drawBlurRect(int left, int top, int right, int bottom, Color color) {
        for (int i = 0; i < 4; i++) {
            int alpha = (int) (color.getAlpha() * (1f - (i / 4f)));
            Color blurred = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
            Gui.drawRect(left - i, top - i, right + i, bottom + i, blurred.getRGB());
        }
    }
}