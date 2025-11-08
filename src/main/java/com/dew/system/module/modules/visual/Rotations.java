package com.dew.system.module.modules.visual;

import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.settingsvalue.SelectionValue;
import org.lwjgl.input.Keyboard;

public class Rotations extends Module {

    private static final SelectionValue mode = new SelectionValue("Mode", "Smooth", "Smooth", "Cool", "GameSense");

    public Rotations() {
        super("Rotations", ModuleCategory.VISUAL, Keyboard.KEY_NONE, true, false, true);
    }

    @Override
    public String tag() {
        return mode.get();
    }

    public String getMode() {
        return mode.get();
    }
}
