package com.dew.system.module.modules.movement.flight.flies;

import com.dew.system.event.events.PreUpdateEvent;
import com.dew.system.event.events.ReceivedPacketEvent;
import com.dew.system.event.events.SendPacketEvent;
import com.dew.system.module.modules.movement.flight.FlightMode;
import com.dew.utils.*;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;
import net.minecraft.network.play.server.S08PacketPlayerPosLook;
import net.minecraft.network.status.server.S01PacketPong;

public class HypixelPredictionFlight implements FlightMode {
    @Override
    public String getName() {
        return "Hypixel Prediction";
    }

    private int boostSpeed = 0;

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
        boostSpeed = 0;
        BlinkUtil.sync(true, true);
        BlinkUtil.stopBlink();
    }

    @Override
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.thePlayer == null) return;

        boostSpeed++;

        if (boostSpeed >= 5) {
            BlinkUtil.sync(true, true);
            boostSpeed = 0;
        } else {
            BlinkUtil.doBlink();
            LogUtil.printChat("bruh" + boostSpeed);
        }
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

        if (packet instanceof S08PacketPlayerPosLook) {
            LogUtil.printChat("Crym");
        }
    }
}
