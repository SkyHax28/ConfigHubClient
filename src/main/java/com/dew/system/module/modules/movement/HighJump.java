package com.dew.system.module.modules.movement;

import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.settingsvalue.NumberValue;
import com.dew.system.settingsvalue.SelectionValue;
import org.lwjgl.input.Keyboard;

public class HighJump extends Module {

    private static final SelectionValue mode = new SelectionValue("Mode", "Vanilla", "Vanilla");
    private static final NumberValue multiplier = new NumberValue("Multiplier", 2.5, 0.1, 10.0, 0.1, () -> mode.get().equals("Vanilla"));

    public HighJump() {
        super("High Jump", ModuleCategory.MOVEMENT, Keyboard.KEY_NONE, false, true, true);
    }

    public String getMode() {
        return mode.get();
    }

    public float getMultiplier() {
        return multiplier.get().floatValue();
    }

    @Override
    public String tag() {
        return mode.get();
    }
}
