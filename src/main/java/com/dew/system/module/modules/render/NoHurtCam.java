package com.dew.system.module.modules.render;

import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import org.lwjgl.input.Keyboard;

public class NoHurtCam extends Module {

    public NoHurtCam() {
        super("No Hurt Cam", ModuleCategory.RENDER, Keyboard.KEY_NONE, false, false, true);
    }
}