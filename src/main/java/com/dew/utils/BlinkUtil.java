package com.dew.utils;

import com.dew.IMinecraft;
import com.sun.javafx.geom.Vec3d;
import net.minecraft.client.Minecraft;
import net.minecraft.network.Packet;
import net.minecraft.network.play.INetHandlerPlayServer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BlinkUtil {
    private static final Minecraft mc = IMinecraft.mc;

    private static Double prevYMotion = null;
    private static boolean isStarted = false;
    public static boolean limiter = false;
    public static boolean blinking = false;

    private static final List<Packet<?>> packets = Collections.synchronizedList(new ArrayList<>());
    private static final List<Vec3d> positions = Collections.synchronizedList(new ArrayList<>());

    public static void addPacket(Packet<?> packet) {
        packets.add(packet);
    }

    public static void doBlink() {
        blinking = true;

        if (prevYMotion == null && mc.thePlayer != null) {
            prevYMotion = mc.thePlayer.motionY;
        }

        if (!isStarted && mc.thePlayer != null) {
            synchronized (positions) {
                positions.add(new Vec3d(mc.thePlayer.posX, mc.thePlayer.getEntityBoundingBox().minY + mc.thePlayer.getEyeHeight() / 2, mc.thePlayer.posZ));
                positions.add(new Vec3d(mc.thePlayer.posX, mc.thePlayer.getEntityBoundingBox().minY, mc.thePlayer.posZ));
            }
            isStarted = true;
            return;
        }

        if (mc.thePlayer != null) {
            synchronized (positions) {
                positions.add(new Vec3d(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ));
            }
        }
    }

    public static void sync(boolean blinkSync, boolean noSyncResetPos) {
        if (blinkSync) {
            try {
                limiter = true;
                while (!packets.isEmpty()) {
                    PacketUtil.sendPacket(packets.remove(0));
                }
            } catch (Exception ignored) {
            } finally {
                limiter = false;
            }
            synchronized (positions) {
                positions.clear();
            }
        } else {
            try {
                limiter = true;
                packets.clear();
            } catch (Exception ignored) {
            } finally {
                limiter = false;
            }

            if (noSyncResetPos && mc.thePlayer != null) {
                synchronized (positions) {
                    if (!positions.isEmpty() && positions.size() > 1) {
                        mc.thePlayer.setPosition(positions.get(1).x, positions.get(1).y, positions.get(1).z);
                    }
                }

                if (prevYMotion != null) {
                    mc.thePlayer.motionY = prevYMotion;
                }
            }
        }
    }

    public static void stopBlink() {
        synchronized (positions) {
            positions.clear();
        }
        prevYMotion = null;
        isStarted = false;
        blinking = false;
    }
}
