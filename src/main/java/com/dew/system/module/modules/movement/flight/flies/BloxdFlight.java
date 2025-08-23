package com.dew.system.module.modules.movement.flight.flies;

import com.dew.system.event.events.*;
import com.dew.system.module.modules.movement.flight.FlightMode;
import com.dew.utils.BlinkUtil;
import net.minecraft.network.Packet;

public class BloxdFlight implements FlightMode {
    private int boostSpeed = 0;

    @Override
    public String getName() {
        return "Bloxd";
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onAttack(AttackEvent event) {
    }

    @Override
    public void onDisable() {
        boostSpeed = 0;
        BlinkUtil.sync(true, true);
        BlinkUtil.stopBlink();
    }

    @Override
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.thePlayer == null) {
        }


    }

    @Override
    public void onPreMotion(PreMotionEvent event) {
    }

    @Override
    public void onSendPacket(SendPacketEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        Packet<?> packet = event.packet;

    }

    @Override
    public void onReceivedPacket(ReceivedPacketEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        Packet<?> packet = event.packet;

    }
}
