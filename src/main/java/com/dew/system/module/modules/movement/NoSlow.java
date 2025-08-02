package com.dew.system.module.modules.movement;

import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.settingsvalue.SelectionValue;
import org.lwjgl.input.Keyboard;

public class NoSlow extends Module {

    private static final SelectionValue mode = new SelectionValue("Mode", "Vanilla", "Vanilla");

    public NoSlow() {
        super("No Slow", ModuleCategory.MOVEMENT, Keyboard.KEY_NONE, false, true, true);
    }

    @Override
    public String tag() {
        return mode.get();
    }

    public boolean canNoSlow() {
        if (!this.isEnabled()) return false;
        switch (mode.get().toLowerCase()) {
            case "vanilla":
                return true;

            default:
                return false;
        }
    }
}
