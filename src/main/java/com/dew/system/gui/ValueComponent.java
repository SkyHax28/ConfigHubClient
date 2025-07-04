package com.dew.system.gui;

public interface ValueComponent {
    void draw(int x, int y, int width, int mouseX, int mouseY);
    void mouseClicked(int mouseX, int mouseY, int button, int x, int y);
    void mouseReleased(int mouseX, int mouseY, int button);
    int getHeight();
}