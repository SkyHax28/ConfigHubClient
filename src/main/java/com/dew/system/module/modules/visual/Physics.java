package com.dew.system.module.modules.visual;

import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import org.lwjgl.input.Keyboard;

public class Physics extends Module {

    public Physics() {
        super("Physics", ModuleCategory.VISUAL, Keyboard.KEY_NONE, true, false, true);
    }
}
