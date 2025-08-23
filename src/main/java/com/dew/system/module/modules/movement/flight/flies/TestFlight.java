package com.dew.system.module.modules.movement.flight.flies;

import com.dew.system.event.events.*;
import com.dew.system.module.modules.movement.flight.FlightMode;

public class TestFlight implements FlightMode {
    @Override
    public String getName() {
        return "Test";
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
    }

    @Override
    public void onSendPacket(SendPacketEvent event) {
    }

    @Override
    public void onReceivedPacket(ReceivedPacketEvent event) {
    }
}
