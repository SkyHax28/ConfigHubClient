package com.dew.system.module.modules.movement.speed.speeds;

import com.dew.DewCommon;
import com.dew.system.event.events.*;
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
    public void onAttack(AttackEvent event) {
    }

    @Override
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.thePlayer == null) return;

        MovementUtil.mcJumpNoBoost = true;

        if (DewCommon.moduleManager.getModule(SpeedModule.class).isAutoBhop() && mc.thePlayer.onGround && MovementUtil.isMoving())
            mc.thePlayer.jump();

        MovementUtil.strafe(DewCommon.moduleManager.getModule(SpeedModule.class).getSpeed());
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
