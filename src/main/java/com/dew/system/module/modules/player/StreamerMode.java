package com.dew.system.module.modules.player;

import com.dew.DewCommon;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import org.lwjgl.input.Keyboard;

public class StreamerMode extends Module {

    public StreamerMode() {
        super("Streamer Mode", ModuleCategory.PLAYER, Keyboard.KEY_NONE, false, true, true);
    }

    public static boolean shouldReplace() {
        return DewCommon.moduleManager != null;
    }
}