package com.dew.system.module.modules.movement.speed.speeds;

import com.dew.system.event.events.PreUpdateEvent;
import com.dew.system.module.modules.movement.speed.SpeedMode;
import com.dew.system.module.modules.movement.speed.SpeedModule;
import com.dew.utils.MovementUtil;

public class VanillaSpeed implements SpeedMode {
    @Override
    public String getName() {
        return "Vanilla";
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
        MovementUtil.mcJumpNoBoost = false;
        if (mc.thePlayer != null) {
            MovementUtil.stopMovingSlowly();
        }
    }

    @Override
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.thePlayer == null) return;

        MovementUtil.mcJumpNoBoost = true;

        if (SpeedModule.autoBHop.get() && mc.thePlayer.onGround && MovementUtil.isMoving())
            mc.thePlayer.jump();

        MovementUtil.strafe(SpeedModule.speed.get());
    }
}
