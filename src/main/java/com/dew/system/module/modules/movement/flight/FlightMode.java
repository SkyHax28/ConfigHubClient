package com.dew.system.module.modules.movement.flight;

import com.dew.IMinecraft;
import com.dew.system.event.events.PreMotionEvent;
import com.dew.system.event.events.PreUpdateEvent;
import com.dew.system.event.events.ReceivedPacketEvent;
import com.dew.system.event.events.SendPacketEvent;
import net.minecraft.client.Minecraft;

public interface FlightMode {
    Minecraft mc = IMinecraft.mc;

    String getName();

    void onEnable();

    void onDisable();

    void onPreUpdate(PreUpdateEvent event);

    void onPreMotion(PreMotionEvent event);

    void onSendPacket(SendPacketEvent event);

    void onReceivedPacket(ReceivedPacketEvent event);
}