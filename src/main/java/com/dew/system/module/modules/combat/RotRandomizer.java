package com.dew.system.module.modules.combat;

import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.settingsvalue.BooleanValue;
import com.dew.system.settingsvalue.NumberValue;
import org.lwjgl.input.Keyboard;

public class RotRandomizer extends Module {
    private static final BooleanValue rotationSpeed = new BooleanValue("Rotation Speed", false);
    private static final BooleanValue yawJitter = new BooleanValue("Yaw Jitter", false);
    private static final BooleanValue pitchJitter = new BooleanValue("Pitch Jitter", false);
    public RotRandomizer() {
        super("Rot Randomizer", ModuleCategory.RENDER, Keyboard.KEY_NONE, false, false, false);
    }

    public boolean isRotationSpeed() {
        return rotationSpeed.get();
    }

    public boolean isYawJitter() {
        return yawJitter.get();
    }

    public boolean isPitchJitter() {
        return pitchJitter.get();
    }
}