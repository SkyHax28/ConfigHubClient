package com.dew.system.module.modules.render;

import com.dew.DewCommon;
import com.dew.system.event.events.PostMotionEvent;
import com.dew.system.event.events.WorldLoadEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.rotation.RotationManager;
import com.dew.system.settingsvalue.SelectionValue;
import org.lwjgl.input.Keyboard;

public class SilentView extends Module {

    public static final SelectionValue mode = new SelectionValue("Mode", "Normal", "Normal", "GameSense");
    public SilentView() {
        super("Silent View", ModuleCategory.RENDER, Keyboard.KEY_NONE, true, true, true);
    }

    @Override
    public String tag() {
        return mode.get();
    }
}
