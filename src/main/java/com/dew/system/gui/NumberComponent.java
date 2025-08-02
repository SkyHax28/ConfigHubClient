package com.dew.system.gui;

import com.dew.DewCommon;
import com.dew.system.settingsvalue.NumberValue;
import net.minecraft.client.gui.Gui;

import java.awt.*;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class NumberComponent implements ValueComponent {
    private final NumberValue value;
    private boolean dragging = false;

    public NumberComponent(NumberValue value) {
        this.value = value;
    }

    @Override
    public void draw(int x, int y, int width, int mouseX, int mouseY) {
        double range = value.getMax() - value.getMin();
        double normalized = (value.get() - value.getMin()) / range;
        int filled = (int) (normalized * width);

        drawBlurRect(x, y, x + width, y + 16, new Color(20, 20, 20, 100));
        Gui.drawRect(x, y, x + filled, y + 16, new Color(85, 153, 255, 180).getRGB());
        Gui.drawRect(x, y, x + width, y + 1, new Color(85, 153, 255, 150).getRGB());
        Gui.drawRect(x, y + 15, x + width, y + 16, new Color(85, 153, 255, 100).getRGB());
        if (mouseX >= x && mouseX <= x + ClickGuiState.NEW_GUI_WIDTH && mouseY >= y && mouseY <= y + 16) {
            Gui.drawRect(x, y, x + width, y + 18, new Color(255, 255, 255, 50).getRGB());
        }

        double val = value.get();
        String formatted;
        if (val == 0.0) {
            formatted = "0.0";
        } else {
            BigDecimal bd = new BigDecimal(val).setScale(2, RoundingMode.HALF_UP);
            formatted = bd.stripTrailingZeros().toPlainString();
            if (formatted.indexOf('.') == -1) {
                formatted += ".0";
            }
        }

        String label = value.getName() + ": " + formatted;
        DewCommon.customFontRenderer.drawStringWithShadow(label, x + 4, y + 1, Color.WHITE.getRGB(), 0.35f);

        if (dragging) {
            double ratio = (mouseX - x) / (double) width;
            ratio = Math.max(0.0, Math.min(1.0, ratio));
            double newValue = value.getMin() + (value.getMax() - value.getMin()) * ratio;
            value.set(newValue);
        }
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button, int x, int y) {
        if (mouseX >= x && mouseX <= x + ClickGuiState.NEW_GUI_WIDTH && mouseY >= y && mouseY <= y + 16 && button == 0) {
            dragging = true;
            double ratio = (double) (mouseX - x) / ClickGuiState.NEW_GUI_WIDTH;
            ratio = Math.max(0.0, Math.min(1.0, ratio));
            double newValue = value.getMin() + (value.getMax() - value.getMin()) * ratio;
            value.set(newValue);
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int button) {
        if (button == 0) {
            dragging = false;
        }
    }

    @Override
    public int getHeight() {
        return 17;
    }

    private void drawBlurRect(int left, int top, int right, int bottom, Color color) {
        for (int i = 0; i < 4; i++) {
            int alpha = (int) (color.getAlpha() * (1f - (i / 4f)));
            Color blurred = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
            Gui.drawRect(left - i, top - i, right + i, bottom + i, blurred.getRGB());
        }
    }
}