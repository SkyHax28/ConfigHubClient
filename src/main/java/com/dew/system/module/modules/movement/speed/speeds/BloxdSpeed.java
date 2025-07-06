package com.dew.system.module.modules.movement.speed.speeds;

import com.dew.system.event.events.PreMotionEvent;
import com.dew.system.event.events.PreUpdateEvent;
import com.dew.system.event.events.ReceivedPacketEvent;
import com.dew.system.module.modules.movement.speed.SpeedMode;
import com.dew.utils.MovementUtil;

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
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.thePlayer == null || mc.thePlayer.isInWater()) return;


    }

    @Override
    public void onPreMotion(PreMotionEvent event) {
    }

    @Override
    public void onReceivedPacket(ReceivedPacketEvent event) {
    }
}