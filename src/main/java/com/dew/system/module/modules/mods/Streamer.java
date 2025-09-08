package com.dew.system.module.modules.mods;

import com.dew.DewCommon;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import org.lwjgl.input.Keyboard;

public class Streamer extends Module {

    public Streamer() {
        super("Streamer", ModuleCategory.MODS, Keyboard.KEY_NONE, false, true, true);
    }

    public static boolean shouldReplace() {
        return DewCommon.moduleManager != null;
    }
}