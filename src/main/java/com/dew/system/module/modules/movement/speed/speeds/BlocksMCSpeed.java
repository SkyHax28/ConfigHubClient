package com.dew.system.module.modules.movement.speed.speeds;

import com.dew.system.event.events.PreUpdateEvent;
import com.dew.system.module.modules.movement.speed.SpeedMode;
import com.dew.utils.MovementUtil;

public class BlocksMCSpeed implements SpeedMode {
    @Override
    public String getName() {
        return "BlocksMC";
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
        if (mc.thePlayer == null || mc.thePlayer.isInWater()) return;

        MovementUtil.mcJumpNoBoost = true;

        if (MovementUtil.isMoving()) {
            if (mc.thePlayer.onGround) {
                mc.thePlayer.jump();
                MovementUtil.strafe((float) Math.max(0.47f + MovementUtil.getSpeedEffect() * 0.1, MovementUtil.getBaseMoveSpeed(0.2873)));
            } else {
                MovementUtil.strafe(MovementUtil.getSpeed());
            }
        }
    }
}