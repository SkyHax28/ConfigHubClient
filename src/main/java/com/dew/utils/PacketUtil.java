package com.dew.utils;

import com.dew.IMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.network.NetworkPlayerInfo;
import net.minecraft.network.Packet;

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

    public static int getCurrentPingOrMinusOne() {
        if (mc.getNetHandler() != null && mc.thePlayer != null) {
            NetworkPlayerInfo info = mc.getNetHandler().getPlayerInfo(mc.thePlayer.getUniqueID());
            if (info != null) {
                return info.getResponseTime();
            }
        }
        return -1;
    }

    public static void processPacketClientSide(Packet<?> packet) {
        try {
            mc.getNetHandler().getNetworkManager().channelRead0(null, packet);
        } catch (Exception ignored) {
        }
    }
}
