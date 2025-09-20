package com.dew.system.module.modules.combat;

import com.dew.DewCommon;
import com.dew.system.event.EventPriority;
import com.dew.system.event.events.*;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.settingsvalue.BooleanValue;
import com.dew.utils.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.ThreadQuickExitException;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.server.*;
import net.minecraft.util.AxisAlignedBB;
import org.lwjgl.input.Keyboard;

import java.util.LinkedList;
import java.util.List;
import java.util.Iterator;
import java.util.concurrent.CopyOnWriteArrayList;

public class Backtrack extends Module {
    private static final BooleanValue resetOnVelocity = new BooleanValue("Reset On Velocity", true);
    private static final BooleanValue resetOnLagging = new BooleanValue("Reset On Lagging", true);

    private final List<Packet<INetHandlerPlayClient>> storagePackets = new CopyOnWriteArrayList<>();
    private final List<Entity> storageEntities = new CopyOnWriteArrayList<>();
    private final LinkedList<EntityPacketLoc> storageEntityMove = new LinkedList<>();

    private final Clock timer = new Clock();
    private Entity attacked;
    private long smoothPointer = System.nanoTime();
    private boolean needFreeze = false;

    public Backtrack() {
        super("Backtrack", ModuleCategory.COMBAT, Keyboard.KEY_NONE, false, true, true);
    }

    @Override
    public EventPriority getPriority() {
        return EventPriority.HIGHEST;
    }

    @Override
    public void onSendPacket(SendPacketEvent event) {
        if (event != null && event.packet != null) {
            this.onPacket(null, event.packet);
        }
    }

    @Override
    public void onReceivedPacket(ReceivedPacketEvent event) {
        if (event != null && event.packet != null) {
            this.onPacket(event, event.packet);
        }
    }

    public void onPacket(ReceivedPacketEvent receivedPacketEvent, Packet<?> packet) {
        if (mc == null || mc.thePlayer == null || mc.theWorld == null || packet == null) {
            return;
        }

        try {
            if (receivedPacketEvent == null) {
                handleSentPacket(packet);
            } else {
                handleReceivedPacket(receivedPacketEvent, packet);
            }
        } catch (Exception e) {
            releasePackets();
        }
    }

    private void handleSentPacket(Packet<?> packet) {
        if (packet instanceof C02PacketUseEntity) {
            C02PacketUseEntity useEntity = (C02PacketUseEntity) packet;
            if (useEntity.getAction() == C02PacketUseEntity.Action.ATTACK && needFreeze) {
                try {
                    attacked = useEntity.getEntityFromWorld(mc.theWorld);
                } catch (Exception e) {
                    attacked = null;
                }
            }
        }
    }

    private void handleReceivedPacket(ReceivedPacketEvent receivedPacketEvent, Packet<?> packet) {
        ServerPacketStorage storage = new ServerPacketStorage((Packet<INetHandlerPlayClient>) packet);

        if (packet instanceof S14PacketEntity) {
            handleEntityPacket(receivedPacketEvent, (S14PacketEntity) packet);
        } else if (packet instanceof S18PacketEntityTeleport) {
            handleTeleportPacket(receivedPacketEvent, (S18PacketEntityTeleport) packet);
        } else {
            handleOtherServerPackets(receivedPacketEvent, packet, storage);
        }
    }

    @Override
    public void onPostMotion(PostMotionEvent event) {
        if (mc == null || mc.thePlayer == null || !needFreeze) return;

        try {
            doSmoothRelease();

            if (!storageEntities.isEmpty()) {
                boolean shouldRelease = false;

                for (Iterator<Entity> iterator = storageEntities.iterator(); iterator.hasNext(); ) {
                    Entity entity = iterator.next();

                    if (entity == null || entity.isDead) {
                        iterator.remove();
                        continue;
                    }

                    double x = entity.serverPosX / 32.0;
                    double y = entity.serverPosY / 32.0;
                    double z = entity.serverPosZ / 32.0;

                    AxisAlignedBB entityBB = new AxisAlignedBB(x - 0.4F, y - 0.1F, z - 0.4F, x + 0.4F, y + 1.9F, z + 0.4F);
                    double range = AaBbUtil.getLookingTargetRange(entityBB, mc.thePlayer);

                    if (range == Double.MAX_VALUE) {
                        range = AaBbUtil.getNearestPointBB(mc.thePlayer.getPositionEyes(1F), entityBB).distanceTo(mc.thePlayer.getPositionEyes(1F)) + 0.075;
                    }

                    if (range <= 2.9) {
                        shouldRelease = true;
                        break;
                    }

                    if (entity == attacked && timer.hasTimePassed(100)) {
                        if (range >= 2.9) {
                            shouldRelease = true;
                            break;
                        }
                    }
                }

                if (shouldRelease) releasePackets();
            }
        } catch (Exception e) {
            releasePackets();
        }
    }

    @Override
    public void onLoadWorld(LoadWorldEvent event) {
        try {
            attacked = null;
            storageEntities.clear();
            storagePackets.clear();
            storageEntityMove.clear();
            needFreeze = false;
        } catch (Exception e) {
            System.err.println("Error clearing backtrack data: " + e.getMessage());
        }
    }

    private void handleEntityPacket(ReceivedPacketEvent event, S14PacketEntity packet) {
        try {
            Entity entity = packet.getEntity(mc.theWorld);
            if (!(entity instanceof EntityPlayer)) return;

            entity.serverPosX += packet.func_149062_c();
            entity.serverPosY += packet.func_149061_d();
            entity.serverPosZ += packet.func_149064_e();

            double x = entity.serverPosX / 32.0;
            double y = entity.serverPosY / 32.0;
            double z = entity.serverPosZ / 32.0;

            AxisAlignedBB afterBB = new AxisAlignedBB(x - 0.4F, y - 0.1F, z - 0.4F, x + 0.4F, y + 1.9F, z + 0.4F);
            double afterRange = AaBbUtil.getNearestPointBB(mc.thePlayer.getPositionEyes(1f), afterBB).distanceTo(mc.thePlayer.getPositionEyes(1F));
            double beforeRange = AaBbUtil.distanceTo(mc.thePlayer.getPositionEyes(1f), entity.getEntityBoundingBox());

            if (beforeRange <= 3.2) {
                if (afterRange >= 2.9 && afterRange <= 5.0 && afterRange > beforeRange + 0.02 && ((EntityPlayer) entity).hurtTime <= getCalculatedMaxHurtTime()) {
                    if (!needFreeze) {
                        timer.reset();
                        needFreeze = true;
                        smoothPointer = System.nanoTime();
                    }
                    if (!storageEntities.contains(entity)) storageEntities.add(entity);
                    event.cancel();
                    synchronized (storageEntityMove) {
                        storageEntityMove.add(new EntityPacketLoc(entity, x, y, z));
                    }
                    return;
                }
            } else {
                if (afterRange <= beforeRange && needFreeze) {
                    releasePackets();
                }
            }

            if (needFreeze) {
                if (!storageEntities.contains(entity)) storageEntities.add(entity);
                synchronized (storageEntityMove) {
                    storageEntityMove.add(new EntityPacketLoc(entity, x, y, z));
                }
                event.cancel();
                return;
            }

            float yaw = packet.func_149060_h() ? (packet.func_149066_f() * 360F) / 256F : entity.rotationYaw;
            float pitch = packet.func_149060_h() ? (packet.func_149063_g() * 360F) / 256F : entity.rotationPitch;
            entity.setPositionAndRotation2(x, y, z, yaw, pitch, 3, false);
            entity.onGround = packet.getOnGround();

            event.cancel();
        } catch (Exception e) {
            releasePackets();
        }
    }

    private void handleTeleportPacket(ReceivedPacketEvent event, S18PacketEntityTeleport packet) {
        try {
            Entity entity = mc.theWorld.getEntityByID(packet.getEntityId());
            if (!(entity instanceof EntityPlayer)) return;

            entity.serverPosX = packet.getX();
            entity.serverPosY = packet.getY();
            entity.serverPosZ = packet.getZ();

            double x = entity.serverPosX / 32.0;
            double y = entity.serverPosY / 32.0;
            double z = entity.serverPosZ / 32.0;
            float yaw = (packet.getYaw() * 360F) / 256F;
            float pitch = (packet.getPitch() * 360F) / 256F;

            if (!needFreeze) {
                if (Math.abs(entity.posX - x) < 0.03125 && Math.abs(entity.posY - y) < 0.015625 && Math.abs(entity.posZ - z) < 0.03125) {
                    entity.setPositionAndRotation2(entity.posX, entity.posY, entity.posZ, yaw, pitch, 3, true);
                } else {
                    entity.setPositionAndRotation2(x, y, z, yaw, pitch, 3, true);
                }
                entity.onGround = packet.getOnGround();
            } else {
                synchronized (storageEntityMove) {
                    storageEntityMove.add(new EntityPacketLoc(entity, x, y, z));
                }
            }
            event.cancel();
        } catch (Exception e) {
            releasePackets();
        }
    }

    private void handleOtherServerPackets(ReceivedPacketEvent event, Packet<?> packet, ServerPacketStorage storage) {
        try {
            if ((packet instanceof S12PacketEntityVelocity && resetOnVelocity.get()) || (packet instanceof S08PacketPlayerPosLook && resetOnLagging.get())) {
                storagePackets.add(storage.packet);
                event.cancel();
                releasePackets();
                return;
            }

            if (needFreeze && !event.isCancelled()) {
                if (packet instanceof S19PacketEntityStatus && ((S19PacketEntityStatus) packet).getOpCode() == 2) {
                    return;
                }
                storagePackets.add(storage.packet);
                event.cancel();
            }
        } catch (Exception e) {
            releasePackets();
        }
    }

    private void releasePackets() {
        try {
            attacked = null;
            smoothPointer = System.nanoTime();
            INetHandlerPlayClient netHandler = mc.getNetHandler();

            if (storagePackets.isEmpty()) return;

            while (!storagePackets.isEmpty()) {
                Packet<INetHandlerPlayClient> packet = storagePackets.remove(0);
                try {
                    if (!PacketUtil.silentPackets.contains(packet)) {
                        packet.processPacket(netHandler);
                    }
                } catch (ThreadQuickExitException ignored) {
                }
            }

            while (!storageEntities.isEmpty()) {
                Entity entity = storageEntities.remove(0);
                if (!entity.isDead) {
                    double x = (double) entity.serverPosX / 32.0;
                    double y = (double) entity.serverPosY / 32.0;
                    double z = (double) entity.serverPosZ / 32.0;
                    entity.setPosition(x, y, z);
                }
            }

            synchronized (storageEntityMove) {
                storageEntityMove.clear();
            }

            needFreeze = false;
        } catch (Exception e) {
            needFreeze = false;
            storagePackets.clear();
            storageEntities.clear();
            synchronized (storageEntityMove) {
                storageEntityMove.clear();
            }
        }
    }

    private void doSmoothRelease() {
        try {
            Entity target = null;
            try {
                if (DewCommon.moduleManager != null) {
                    Aura auraModule = DewCommon.moduleManager.getModule(Aura.class);
                    if (auraModule != null) {
                        target = auraModule.target;
                    }
                }
            } catch (Exception ignored) {
            }

            boolean found = false;
            long bestTimeStamp = Math.max(smoothPointer, System.nanoTime() - 200 * 1_000_000);

            if (target != null) {
                synchronized (storageEntityMove) {
                    for (EntityPacketLoc loc : storageEntityMove) {
                        if (target == loc.entity) {
                            found = true;
                            double width = loc.entity.width / 2.0;
                            double height = loc.entity.height;
                            AxisAlignedBB bb = AaBbUtil.expands(
                                    new AxisAlignedBB(
                                            loc.x - width, loc.y, loc.z - width,
                                            loc.x + width, loc.y + height, loc.z + width
                                    ),
                                    0.1
                            );

                            double range = AaBbUtil.distanceTo(mc.thePlayer.getPositionEyes(1f), bb);
                            if (range < 2.9 || AaBbUtil.distanceTo(mc.thePlayer.getPositionEyes(3f), bb) < 2.8) {
                                bestTimeStamp = Math.max(bestTimeStamp, loc.time);
                            }
                        }
                    }
                }
            }

            if (!found) {
                releasePackets();
            }
        } catch (Exception e) {
            releasePackets();
        }
    }

    private int getCalculatedMaxHurtTime() {
        return 6;
    }

    private static class ServerPacketStorage {
        final Packet<INetHandlerPlayClient> packet;

        ServerPacketStorage(Packet<INetHandlerPlayClient> packet) {
            this.packet = packet;
        }
    }

    private static class EntityPacketLoc {
        final Entity entity;
        final double x, y, z;
        final long time = System.nanoTime();

        EntityPacketLoc(Entity entity, double x, double y, double z) {
            this.entity = entity;
            this.x = x;
            this.y = y;
            this.z = z;
        }
    }
}