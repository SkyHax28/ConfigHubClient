package com.dew.system.module.modules.mods;

import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import org.lwjgl.input.Keyboard;

public class FpsBooster extends Module {

    public FpsBooster() {
        super("Fps Booster", ModuleCategory.MODS, Keyboard.KEY_NONE, true, false, true);
    }
}