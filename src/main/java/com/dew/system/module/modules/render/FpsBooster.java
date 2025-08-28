package com.dew.system.module.modules.render;

import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import org.lwjgl.input.Keyboard;

public class FpsBooster extends Module {

    public FpsBooster() {
        super("Fps Booster", ModuleCategory.RENDER, Keyboard.KEY_NONE, true, false, true);
    }
}