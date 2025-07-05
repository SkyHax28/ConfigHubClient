package com.dew.system.gui;

import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class ClickGuiState {
    public static int x = 50;
    public static int y = 50;
    public static int width = 420;
    public static int height = 300;
    public static int scroll = 0;
    public static int selectedCategory = 0;
    public static Set<Module> expandedModules = new HashSet<>();

    public static float animatedX = ClickGuiState.x;
    public static float animatedY = ClickGuiState.y;
    public static float animatedWidth = ClickGuiState.width;
    public static float animatedHeight = ClickGuiState.height;

    public static final int NEW_GUI_WIDTH = 120;

    public static class WindowState {
        public int x, y;
        public boolean open;

        public WindowState(int x, int y, boolean open) {
            this.x = x;
            this.y = y;
            this.open = open;
        }
    }

    public static final Map<ModuleCategory, WindowState> windowStates = new HashMap<>();

    public static WindowState getOrCreate(ModuleCategory category, int defaultX, int defaultY) {
        if (windowStates.containsKey(category)) {
            return windowStates.get(category);
        }

        WindowState newState = new WindowState(defaultX, defaultY, true);
        windowStates.put(category, newState);
        return newState;
    }
}