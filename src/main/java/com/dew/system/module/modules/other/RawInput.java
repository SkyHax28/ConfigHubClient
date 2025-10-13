package com.dew.system.module.modules.other;

import com.dew.DewCommon;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.utils.LogUtil;
import org.lwjgl.input.Keyboard;

public class RawInput extends Module {

    public RawInput() {
        super("Raw Input", ModuleCategory.OTHER, Keyboard.KEY_NONE, true, false, true);
    }

    @Override
    public void onEnable() {
        if (mc.rawInputNotSupported) {
            LogUtil.infoLog("Your environment does not support Raw Input");
        }
    }

    public static boolean shouldReplace() {
        return DewCommon.moduleManager != null;
    }
}