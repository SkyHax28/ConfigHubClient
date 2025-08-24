package com.dew.system.module.modules.movement.speed.speeds;

import com.dew.system.event.events.*;
import com.dew.system.module.modules.movement.speed.SpeedMode;

public class BloxdSpeed implements SpeedMode {
    @Override
    public String getName() {
        return "Bloxd";
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {

    }

    @Override
    public void onAttack(AttackEvent event) {
    }

    @Override
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.thePlayer == null || mc.thePlayer.isInWater()) {
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