package com.dew.system.module.modules.render;

import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.settingsvalue.SelectionValue;
import org.lwjgl.input.Keyboard;

public class ClickGui extends Module {

    private static final SelectionValue mode = new SelectionValue("Mode", "Modern", "Modern", "Nostalgia");

    public ClickGui() {
        super("Click Gui", ModuleCategory.RENDER, Keyboard.KEY_RSHIFT, false, false, false);
    }

    public String getMode() {
        return mode.get();
    }
}
