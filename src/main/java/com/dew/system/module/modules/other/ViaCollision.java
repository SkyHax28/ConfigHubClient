package com.dew.system.module.modules.other;

import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.settingsvalue.SelectionValue;
import org.lwjgl.input.Keyboard;

public class ViaCollision extends Module {

    private static final SelectionValue mode = new SelectionValue("Mode", "1.14.x", "1.9-1.12.2", "1.13-1.13.2", "1.14.x");
    public ViaCollision() {
        super("Via Collision", ModuleCategory.OTHER, Keyboard.KEY_NONE, false, true, true);
    }

    public String getMode() {
        return mode.get();
    }

    @Override
    public String tag() {
        return mode.get();
    }
}
