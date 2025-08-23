package com.dew.system.module.modules.movement.flight;

import com.dew.IMinecraft;
import com.dew.system.event.events.*;
import net.minecraft.client.Minecraft;

public interface FlightMode {
    Minecraft mc = IMinecraft.mc;

    String getName();

    void onEnable();

    void onDisable();

    void onAttack(AttackEvent event);

    void onPreUpdate(PreUpdateEvent event);

    void onPreMotion(PreMotionEvent event);

    void onSendPacket(SendPacketEvent event);

    void onReceivedPacket(ReceivedPacketEvent event);
}