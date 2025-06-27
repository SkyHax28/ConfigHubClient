package com.dew.system.viapatcher;

import com.dew.IMinecraft;
import com.dew.system.event.events.ReceivedPacketEvent;
import com.dew.system.event.events.SendPacketEvent;
import com.viaversion.viabackwards.protocol.v1_11to1_10.Protocol1_11To1_10;
import com.viaversion.viarewind.protocol.v1_9to1_8.Protocol1_9To1_8;
import com.viaversion.viarewind.protocol.v1_9to1_8.storage.BossBarStorage;
import com.viaversion.viarewind.protocol.v1_9to1_8.storage.PlayerPositionTracker;
import com.viaversion.viaversion.api.Via;
import com.viaversion.viaversion.api.minecraft.BlockPosition;
import com.viaversion.viaversion.api.protocol.ProtocolManager;
import com.viaversion.viaversion.api.protocol.packet.PacketWrapper;
import com.viaversion.viaversion.api.protocol.remapper.PacketHandlers;
import com.viaversion.viaversion.api.protocol.version.ProtocolVersion;
import com.viaversion.viaversion.api.type.Types;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ClientboundPackets1_9;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ServerboundPackets1_8;
import com.viaversion.viaversion.protocols.v1_8to1_9.packet.ServerboundPackets1_9;
import de.florianmichael.vialoadingbase.ViaLoadingBase;
import net.minecraft.block.BlockAir;
import net.minecraft.client.Minecraft;
import net.minecraft.item.ItemEnderEye;
import net.minecraft.item.ItemEnderPearl;
import net.minecraft.item.ItemExpBottle;
import net.minecraft.item.ItemSnowball;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.*;

import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class PacketPatcher {

    private static final Minecraft mc = IMinecraft.mc;

    public static void handleFixedSendPackets(SendPacketEvent event) {
        Packet<?> packet = event.packet;

        if (event.isCancelled()) return;

        if (!mc.isSingleplayer() && ViaLoadingBase.getInstance().getTargetVersion().newerThan(ProtocolVersion.v1_8) && packet instanceof C0APacketAnimation) {
            event.cancel();
            PacketWrapper fixedC0A = PacketWrapper.create(ServerboundPackets1_9.SWING, Via.getManager().getConnectionManager().getConnections().iterator().next());
            fixedC0A.write(Types.VAR_INT, 0);
            fixedC0A.sendToServer(Protocol1_9To1_8.class);
        }

        if (!mc.isSingleplayer() && ViaLoadingBase.getInstance().getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_16) && (packet instanceof C08PacketPlayerBlockPlacement && IMinecraft.mc.theWorld.getBlockState(((C08PacketPlayerBlockPlacement) packet).getPosition()).getBlock() instanceof BlockAir && (((C08PacketPlayerBlockPlacement) packet).getStack().getItem() instanceof ItemSnowball || ((C08PacketPlayerBlockPlacement) packet).getStack().getItem() instanceof ItemEnderPearl || ((C08PacketPlayerBlockPlacement) packet).getStack().getItem() instanceof ItemEnderEye || ((C08PacketPlayerBlockPlacement) packet).getStack().getItem() instanceof ItemExpBottle || (((C08PacketPlayerBlockPlacement) packet).getStack().getMetadata() & 0x4000) != 0) || packet instanceof C0EPacketClickWindow && (((C0EPacketClickWindow) packet).getMode() == 4 || ((C0EPacketClickWindow) packet).getSlotId() == -999) || packet instanceof C07PacketPlayerDigging && IMinecraft.mc.thePlayer.getHeldItem() != null && (((C07PacketPlayerDigging) packet).getStatus() == C07PacketPlayerDigging.Action.DROP_ITEM || ((C07PacketPlayerDigging) packet).getStatus() == C07PacketPlayerDigging.Action.DROP_ALL_ITEMS))) {
            PacketWrapper swingPacket = PacketWrapper.create(ServerboundPackets1_9.SWING, Via.getManager().getConnectionManager().getConnections().iterator().next());
            swingPacket.write(Types.VAR_INT, 0);
            swingPacket.sendToServer(Protocol1_9To1_8.class);
        }

        if (!mc.isSingleplayer() && ViaLoadingBase.getInstance().getTargetVersion().newerThanOrEqualTo(ProtocolVersion.v1_11) && packet instanceof C08PacketPlayerBlockPlacement && ((C08PacketPlayerBlockPlacement) packet).getPlacedBlockDirection() != 255) {
            event.cancel();
            PacketWrapper fixedC08 = PacketWrapper.create(ServerboundPackets1_9.USE_ITEM_ON, Via.getManager().getConnectionManager().getConnections().iterator().next());
            fixedC08.write(Types.BLOCK_POSITION1_8, new BlockPosition(((C08PacketPlayerBlockPlacement) packet).getPosition().getX(), ((C08PacketPlayerBlockPlacement) packet).getPosition().getY(), ((C08PacketPlayerBlockPlacement) packet).getPosition().getZ()));
            fixedC08.write(Types.VAR_INT, ((C08PacketPlayerBlockPlacement) packet).getPlacedBlockDirection());
            fixedC08.write(Types.VAR_INT, 0);
            fixedC08.write(Types.FLOAT, ((C08PacketPlayerBlockPlacement) packet).getPlacedBlockOffsetX());
            fixedC08.write(Types.FLOAT, ((C08PacketPlayerBlockPlacement) packet).getPlacedBlockOffsetY());
            fixedC08.write(Types.FLOAT, ((C08PacketPlayerBlockPlacement) packet).getPlacedBlockOffsetZ());
            fixedC08.sendToServer(Protocol1_11To1_10.class);
        }
    }

    public static void handleFixedReceivePackets(ReceivedPacketEvent event) {
        Packet<?> packet = event.packet;

        if (event.isCancelled()) return;

    }

    private static final Queue<PacketWrapper> confirmations = new ConcurrentLinkedQueue<>();

    public static void applyNibblesPatches() {
        ProtocolManager protocolManager = Via.getManager().getProtocolManager();
        Protocol1_9To1_8 protocol1_9To1_8 = protocolManager.getProtocol(Protocol1_9To1_8.class);
        if (mc.isSingleplayer() || protocol1_9To1_8 == null) {
            return;
        }

        protocol1_9To1_8.registerClientbound(ClientboundPackets1_9.PLAYER_POSITION, new PacketHandlers() {
            @Override
            public void register() {
                map(Types.DOUBLE);
                map(Types.DOUBLE);
                map(Types.DOUBLE);
                map(Types.FLOAT);
                map(Types.FLOAT);
                map(Types.BYTE);
                handler(wrapper -> {
                    int id = wrapper.read(Types.VAR_INT);
                    PacketWrapper c = PacketWrapper.create(ServerboundPackets1_9.ACCEPT_TELEPORTATION, wrapper.user());
                    c.write(Types.VAR_INT, id);
                    confirmations.offer(c);

                    PlayerPositionTracker tracker = wrapper.user().get(PlayerPositionTracker.class);
                    if (tracker != null) {
                        tracker.setConfirmId(id);

                        byte flags = wrapper.get(Types.BYTE, 0);
                        double x = wrapper.get(Types.DOUBLE, 0);
                        double y = wrapper.get(Types.DOUBLE, 1);
                        double z = wrapper.get(Types.DOUBLE, 2);
                        float yaw = wrapper.get(Types.FLOAT, 0);
                        float pitch = wrapper.get(Types.FLOAT, 1);

                        wrapper.set(Types.BYTE, 0, (byte) 0);

                        if (flags != 0) {
                            if ((flags & 0x01) != 0) {
                                x += tracker.getPosX();
                                wrapper.set(Types.DOUBLE, 0, x);
                            }
                            if ((flags & 0x02) != 0) {
                                y += tracker.getPosY();
                                wrapper.set(Types.DOUBLE, 1, y);
                            }
                            if ((flags & 0x04) != 0) {
                                z += tracker.getPosZ();
                                wrapper.set(Types.DOUBLE, 2, z);
                            }
                            if ((flags & 0x08) != 0) {
                                yaw += tracker.getYaw();
                                wrapper.set(Types.FLOAT, 0, yaw);
                            }
                            if ((flags & 0x10) != 0) {
                                pitch += tracker.getPitch();
                                wrapper.set(Types.FLOAT, 1, pitch);
                            }
                        }

                        tracker.setPos(x, y, z);
                        tracker.setYaw(yaw);
                        tracker.setPitch(pitch);
                    }
                });
            }
        });

        protocol1_9To1_8.registerServerbound(ServerboundPackets1_8.MOVE_PLAYER_POS_ROT, wrapper -> {
            PacketWrapper c = confirmations.poll();
            if (c != null) {
                c.sendToServer(Protocol1_9To1_8.class);
            }

            double x = wrapper.passthrough(Types.DOUBLE);
            double y = wrapper.passthrough(Types.DOUBLE);
            double z = wrapper.passthrough(Types.DOUBLE);
            float yaw = wrapper.passthrough(Types.FLOAT);
            float pitch = wrapper.passthrough(Types.FLOAT);
            boolean onGround = wrapper.passthrough(Types.BOOLEAN);
            PlayerPositionTracker tracker = wrapper.user().get(PlayerPositionTracker.class);
            if (tracker != null) {
                tracker.sendAnimations();
                if (tracker.getConfirmId() != -1) {
                    if (
                            tracker.getPosX() == x &&
                                    tracker.getPosY() == y &&
                                    tracker.getPosZ() == z &&
                                    tracker.getYaw() == yaw &&
                                    tracker.getPitch() == pitch
                    ) {
                        tracker.setConfirmId(-1);
                    }
                } else {
                    tracker.setPos(x, y, z);
                    tracker.setYaw(yaw);
                    tracker.setPitch(pitch);
                    tracker.setOnGround(onGround);
                    BossBarStorage storage = wrapper.user().get(BossBarStorage.class);
                    if (storage != null) {
                        storage.updateLocation();
                    }
                }
            }
        });
    }
}
