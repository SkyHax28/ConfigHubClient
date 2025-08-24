package com.dew.system.module.modules.movement.speed.speeds;

import com.dew.system.event.events.*;
import com.dew.system.module.modules.movement.speed.SpeedMode;
import com.dew.utils.LogUtil;

public class HypixelPredictionSpeed implements SpeedMode {
    @Override
    public String getName() {
        return "Hypixel Prediction";
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
        if (mc.thePlayer == null) return;

        LogUtil.printChat("NOT WORKING! OH NO!");
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