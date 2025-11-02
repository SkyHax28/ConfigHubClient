package com.dew.system.module.modules.other;

import com.dew.DewCommon;
import com.dew.system.event.EventPriority;
import com.dew.system.event.events.PreUpdateEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.settingsvalue.NumberValue;
import org.lwjgl.input.Keyboard;

public class SpinBot extends Module {
    private float yaw = 0f;
    private static final NumberValue yawSpeed = new NumberValue("Yaw Speed", 20.0, -175.0, 175.0, 5.0);
    private static final NumberValue pitch = new NumberValue("Pitch", 90.0, -90.0, 90.0, 5.0);
    public SpinBot() {
        super("Spin Bot", ModuleCategory.OTHER, Keyboard.KEY_NONE, false, true, true);
    }

    @Override
    public EventPriority getPriority() {
        return EventPriority.LOWEST;
    }

    @Override
    public void onDisable() {
        yaw = 0f;
    }

    @Override
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        yaw += yawSpeed.get().intValue();
        DewCommon.rotationManager.rotateToward(yaw, pitch.get().intValue(), 180f, false);
    }
}
