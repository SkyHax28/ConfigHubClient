package com.dew.system.module.modules.mods;

import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.utils.LogUtil;
import org.lwjgl.input.Keyboard;

public class RawInput extends Module {

    public RawInput() {
        super("Raw Input", ModuleCategory.MODS, Keyboard.KEY_NONE, true, false, true);
    }

    @Override
    public void onEnable() {
        if (mc.rawInputNotSupported) {
            LogUtil.printChat("Your system does not support Raw Input");
        }
    }
}