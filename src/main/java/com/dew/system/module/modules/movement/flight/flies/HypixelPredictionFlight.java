package com.dew.system.module.modules.movement.flight.flies;

import com.dew.system.event.events.*;
import com.dew.system.module.modules.movement.flight.FlightMode;
import com.dew.utils.LogUtil;

public class HypixelPredictionFlight implements FlightMode {

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
    }

    @Override
    public void onPreMotion(PreMotionEvent event) {
        if (mc.thePlayer == null) return;

        LogUtil.printChat("NOT WORKING! OH NO!");
    }

    @Override
    public void onSendPacket(SendPacketEvent event) {
    }

    @Override
    public void onReceivedPacket(ReceivedPacketEvent event) {
    }
}
