package com.dew.system.module.modules.visual;

import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import org.lwjgl.input.Keyboard;

public class CameraNoClip extends Module {

    public CameraNoClip() {
        super("Camera NoClip", ModuleCategory.VISUAL, Keyboard.KEY_NONE, false, false, true);
    }
}