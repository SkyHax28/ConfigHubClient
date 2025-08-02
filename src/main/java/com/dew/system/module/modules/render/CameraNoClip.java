package com.dew.system.module.modules.render;

import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import org.lwjgl.input.Keyboard;

public class CameraNoClip extends Module {

    public CameraNoClip() {
        super("Camera NoClip", ModuleCategory.RENDER, Keyboard.KEY_NONE, false, false, true);
    }
}