package com.dew.system.gui;

import com.dew.DewCommon;
import com.dew.IMinecraft;
import com.dew.system.settingsvalue.BooleanValue;
import net.minecraft.client.gui.Gui;

import java.awt.*;

class BooleanComponent implements ValueComponent {
    private final BooleanValue value;

    public BooleanComponent(BooleanValue value) {
        this.value = value;
    }

    @Override
    public void draw(int x, int y, int width, int mouseX, int mouseY) {
        Color base = value.get() ? new Color(102, 187, 102, 180) : new Color(68, 68, 68, 160);
        drawBlurRect(x, y + 1, x + width, y + 16, base);
        if (mouseX >= x && mouseX <= x + ClickGuiState.NEW_GUI_WIDTH && mouseY >= y && mouseY <= y + 15) {
            Gui.drawRect(x, y, x + width, y + 18, new Color(255, 255, 255, 50).getRGB());
        }
        DewCommon.customFontRenderer.drawStringWithShadow(value.getName(), x + 4, y + 1, Color.WHITE.getRGB(), 0.35f);
    }

    @Override
    public void mouseClicked(int mouseX, int mouseY, int button, int x, int y) {
        if (mouseX >= x && mouseX <= x + ClickGuiState.NEW_GUI_WIDTH && mouseY >= y && mouseY <= y + 15) {
            if (button == 0) value.toggle();
        }
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY, int button) {
    }

    @Override
    public int getHeight() {
        return 16;
    }

    private void drawBlurRect(int left, int top, int right, int bottom, Color color) {
        for (int i = 0; i < 4; i++) {
            int alpha = (int) (color.getAlpha() * (1f - (i / 4f)));
            Color blurred = new Color(color.getRed(), color.getGreen(), color.getBlue(), alpha);
            Gui.drawRect(left - i, top - i, right + i, bottom + i, blurred.getRGB());
        }
    }
}