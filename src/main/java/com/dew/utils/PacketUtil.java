package com.dew.utils;

import com.dew.IMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayServer;

import java.util.ArrayList;

public class PacketUtil {

    private static final Minecraft mc = IMinecraft.mc;

    public static ArrayList<Packet<?>> silentPackets = new ArrayList<>();

    public static void sendPacket(Packet<?> packet) {
        mc.getNetHandler().addToSendQueue(packet);
    }

    public static void sendPacketAsSilent(Packet<?> packet) {
        if (mc.getNetHandler() == null) return;

        silentPackets.add(packet);
        mc.getNetHandler().addToSendQueue(packet);
    }
}
