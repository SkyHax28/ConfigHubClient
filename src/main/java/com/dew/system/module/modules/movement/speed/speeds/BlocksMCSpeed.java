package com.dew.system.module.modules.movement.speed.speeds;

import com.dew.DewCommon;
import com.dew.system.event.events.*;
import com.dew.system.module.modules.movement.speed.SpeedMode;
import com.dew.system.module.modules.movement.speed.SpeedModule;
import com.dew.utils.MovementUtil;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;

public class BlocksMCSpeed implements SpeedMode {
    @Override
    public String getName() {
        return "BlocksMC";
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onAttack(AttackEvent event) {
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
                MovementUtil.strafe((float) Math.max(0.41f + MovementUtil.getSpeedEffect() * 0.1, MovementUtil.getBaseMoveSpeed(0.2873)));
            } else {
                MovementUtil.strafe(MovementUtil.getSpeed());
            }
        }
    }

    @Override
    public void onPreMotion(PreMotionEvent event) {
    }

    @Override
    public void onMove(MoveEvent event) {
    }

    @Override
    public void onReceivedPacket(ReceivedPacketEvent event) {
        if (mc.thePlayer == null) return;

        Packet<?> packet = event.packet;

        if (packet instanceof S08PacketPlayerPosLook) {
            DewCommon.moduleManager.getModule(SpeedModule.class).setState(false);
        }
    }
}