package com.dew.system.module.modules.movement.speed.speeds;

import com.dew.DewCommon;
import com.dew.system.event.events.*;
import com.dew.system.module.modules.movement.speed.SpeedMode;
import com.dew.system.module.modules.movement.speed.SpeedModule;
import com.dew.utils.MovementUtil;
import com.dew.utils.TimerUtil;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;

public class VulcanSpeed implements SpeedMode {

    private boolean wasTimer = false;
    private int ticks = 0;

    @Override
    public String getName() {
        return "Vulcan";
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
        TimerUtil.resetTimerSpeed();
        wasTimer = false;
        ticks = 0;
    }

    @Override
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.thePlayer == null || mc.thePlayer.isInWater()) return;

        MovementUtil.mcJumpNoBoost = true;

        ticks++;

        if (wasTimer) {
            TimerUtil.resetTimerSpeed();
            wasTimer = false;
        }

        mc.thePlayer.jumpMovementFactor = 0.0245f;

        if (!mc.thePlayer.onGround && ticks > 3 && mc.thePlayer.motionY > 0) {
            mc.thePlayer.motionY = -0.27;
        }

        float BASE_STRAFE_SPEED = 0.215f;
        if (MovementUtil.getSpeed() < BASE_STRAFE_SPEED && !mc.thePlayer.onGround) {
            MovementUtil.strafe(BASE_STRAFE_SPEED);
        }

        if (mc.thePlayer.onGround && MovementUtil.isMoving()) {
            ticks = 0;
            mc.thePlayer.jump();

            if (!mc.thePlayer.isAirBorne) {
                return;
            }

            float BOOSTED_TIMER = 1.20f;
            TimerUtil.setTimerSpeed(BOOSTED_TIMER);
            wasTimer = true;

            float currentSpeed = MovementUtil.getSpeed();
            float BOOSTED_STRAFE_SPEED = 0.48f;
            if (currentSpeed < BOOSTED_STRAFE_SPEED) {
                MovementUtil.strafe(BOOSTED_STRAFE_SPEED);
            } else {
                MovementUtil.strafe(currentSpeed * 0.985f);
            }
        } else if (!MovementUtil.isMoving()) {
            TimerUtil.resetTimerSpeed();
        }
    }

    @Override
    public void onPreMotion(PreMotionEvent event) {
    }

    @Override
    public void onMove(MoveEvent event) {
    }

    @Override
    public void onBlockBB(BlockBBEvent event) {
    }

    @Override
    public void onReceivedPacket(ReceivedPacketEvent event) {
    }
}