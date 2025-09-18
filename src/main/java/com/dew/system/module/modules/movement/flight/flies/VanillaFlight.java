package com.dew.system.module.modules.movement.flight.flies;

import com.dew.DewCommon;
import com.dew.system.event.events.*;
import com.dew.system.module.modules.movement.flight.FlightMode;
import com.dew.system.module.modules.movement.flight.FlightModule;
import com.dew.utils.MovementUtil;

public class VanillaFlight implements FlightMode {
    @Override
    public String getName() {
        return "Vanilla";
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
        if (mc.thePlayer != null) {
            MovementUtil.stopMovingSlowly();
            MovementUtil.stopYMotion();
        }
    }

    @Override
    public void onAttack(AttackEvent event) {
    }

    @Override
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.thePlayer == null) return;

        double yMotion = 0;
        if (mc.gameSettings.keyBindJump.isKeyDown())
            yMotion += DewCommon.moduleManager.getModule(FlightModule.class).getVerticalSpeed();
        if (mc.gameSettings.keyBindSneak.isKeyDown())
            yMotion -= DewCommon.moduleManager.getModule(FlightModule.class).getVerticalSpeed();

        MovementUtil.strafe(DewCommon.moduleManager.getModule(FlightModule.class).getHorizontalSpeed());
        mc.thePlayer.motionY = yMotion;
    }

    @Override
    public void onPreMotion(PreMotionEvent event) {
    }

    @Override
    public void onBlockBB(BlockBBEvent event) {
    }

    @Override
    public void onSendPacket(SendPacketEvent event) {
    }

    @Override
    public void onReceivedPacket(ReceivedPacketEvent event) {
    }
}
