package com.dew.system.module.modules.movement.speed.speeds;

import com.dew.system.event.events.PreMotionEvent;
import com.dew.system.event.events.PreUpdateEvent;
import com.dew.system.event.events.ReceivedPacketEvent;
import com.dew.system.module.modules.movement.speed.SpeedMode;

public class TestSpeed implements SpeedMode {
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
    public void onPreUpdate(PreUpdateEvent event) {
    }

    @Override
    public void onPreMotion(PreMotionEvent event) {
    }

    @Override
    public void onReceivedPacket(ReceivedPacketEvent event) {
    }
}
