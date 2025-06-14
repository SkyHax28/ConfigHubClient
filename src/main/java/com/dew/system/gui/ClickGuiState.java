package com.dew.system.gui;

import com.dew.system.module.Module;

import java.util.HashSet;
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
}