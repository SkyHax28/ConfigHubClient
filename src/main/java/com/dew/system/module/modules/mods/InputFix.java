package com.dew.system.module.modules.mods;

import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import org.lwjgl.input.Keyboard;

public class InputFix extends Module {

    public InputFix() {
        super("Input Fix", ModuleCategory.MODS, Keyboard.KEY_NONE, true, false, true);
    }
}
