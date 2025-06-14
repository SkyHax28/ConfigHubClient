package com.dew.system.module.modules.player;

import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import org.lwjgl.input.Keyboard;

public class KeepSprint extends Module {

    public KeepSprint() {
        super("Keep Sprint", ModuleCategory.PLAYER, Keyboard.KEY_NONE, false, true, true);
    }
}
