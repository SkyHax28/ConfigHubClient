package com.dew.system.module.modules.movement.speed;

import com.dew.IMinecraft;
import com.dew.system.event.events.*;
import net.minecraft.client.Minecraft;

public interface SpeedMode {
    Minecraft mc = IMinecraft.mc;

    String getName();

    void onEnable();

    void onDisable();

    void onAttack(AttackEvent event);

    void onPreUpdate(PreUpdateEvent event);

    void onPreMotion(PreMotionEvent event);

    void onMove(MoveEvent event);

    void onBlockBB(BlockBBEvent event);

    void onReceivedPacket(ReceivedPacketEvent event);
}