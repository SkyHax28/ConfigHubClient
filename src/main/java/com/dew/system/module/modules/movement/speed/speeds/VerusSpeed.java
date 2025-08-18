package com.dew.system.module.modules.movement.speed.speeds;

import com.dew.DewCommon;
import com.dew.system.event.events.PreMotionEvent;
import com.dew.system.event.events.PreUpdateEvent;
import com.dew.system.event.events.ReceivedPacketEvent;
import com.dew.system.module.modules.movement.speed.SpeedMode;
import com.dew.system.module.modules.movement.speed.SpeedModule;
import com.dew.utils.MovementUtil;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.potion.Potion;

public class VerusSpeed implements SpeedMode {
    @Override
    public String getName() {
        return "Verus";
    }

    @Override
    public void onEnable() {
        MovementUtil.mcJumpNoBoost = true;
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

        if (!mc.thePlayer.onGround) {
            if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
                MovementUtil.strafe(0.42f);
            } else {
                MovementUtil.strafe(0.32f);
            }
        } else if (MovementUtil.isMoving()) {
            MovementUtil.strafe(MovementUtil.getSpeed());
            mc.thePlayer.jump();
        }
    }

    @Override
    public void onPreMotion(PreMotionEvent event) {
    }

    @Override
    public void onReceivedPacket(ReceivedPacketEvent event) {
    }
}