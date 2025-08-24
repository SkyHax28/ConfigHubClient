package com.dew.system.module.modules.render;

import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import org.lwjgl.input.Keyboard;

public class MinimalBobbing extends Module {

    public MinimalBobbing() {
        super("Minimal Bobbing", ModuleCategory.RENDER, Keyboard.KEY_NONE, true, false, true);
    }
}