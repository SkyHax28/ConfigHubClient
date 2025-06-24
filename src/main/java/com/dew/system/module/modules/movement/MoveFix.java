package com.dew.system.module.modules.movement;

import com.dew.DewCommon;
import com.dew.system.event.events.StrafeEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.rotation.RotationManager;
import com.dew.utils.MovementUtil;
import org.lwjgl.input.Keyboard;

public class MoveFix extends Module {
    public MoveFix() {
        super("Move Fix", ModuleCategory.MOVEMENT, Keyboard.KEY_NONE, false, true, true);
    }

    @Override
    public void onStrafe(StrafeEvent event) {
        RotationManager rotationManager = DewCommon.rotationManager;
        if (mc.thePlayer == null) return;

        if (rotationManager.isRotating()) {
            event.cancel();
            MovementUtil.silentRotationStrafe(event, rotationManager.getClientYaw());
        }
    }
}
