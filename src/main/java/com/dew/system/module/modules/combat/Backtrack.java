package com.dew.system.module.modules.combat;

import com.dew.DewCommon;
import com.dew.system.event.EventPriority;
import com.dew.system.event.events.*;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.settingsvalue.NumberValue;
import com.dew.utils.*;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.ThreadQuickExitException;
import net.minecraft.network.play.INetHandlerPlayClient;
import net.minecraft.network.play.client.C02PacketUseEntity;
import net.minecraft.network.play.server.*;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.util.*;
import java.util.concurrent.CopyOnWriteArrayList;

public class Backtrack extends Module {
    private final List<Packet<INetHandlerPlayClient>> storagePackets = new CopyOnWriteArrayList<>();
    private final List<Entity> storageEntities = new CopyOnWriteArrayList<>();
    private final LinkedList<EntityPacketLoc> storageEntityMove = new LinkedList<>();
    private final Map<Integer, double[]> lastServerPos = new HashMap<>();

    private final Clock timer = new Clock();
    private Entity lastAttackedEntity = null;
    private long smoothPointer = System.nanoTime();
    private boolean needFreeze = false;
    private long freezeStartTime = 0L;

    private static final NumberValue detectionRange = new NumberValue("Detect Range", 8.0, 0.1, 10.0, 0.1);
    private static final NumberValue maxRange = new NumberValue("Max Track Range", 7.5, 0.1, 10.0, 0.1);
    private static final NumberValue minRange = new NumberValue("Min Allow Range", 1.5, 0.1, 10.0, 0.1);
    private static final NumberValue minDelay = new NumberValue("Min Delay", 700.0, 0.0, 1000.0, 10.0);

    public Backtrack() {
        super("Backtrack", ModuleCategory.COMBAT, Keyboard.KEY_NONE, false, true, true);
    }

    @Override
    public void onDisable() {
        storagePackets.clear();
        storageEntities.clear();
        storageEntityMove.clear();
        lastServerPos.clear();
        timer.reset();
        lastAttackedEntity = null;
        needFreeze = false;
        freezeStartTime = 0L;
    }

    @Override
    public EventPriority getPriority() {
        return EventPriority.LOWEST;
    }

    @Override
    public void onAttack(AttackEvent event) {
        Entity target = event.target;
        if (target != null) {
            this.lastAttackedEntity = target;
        }
    }

    @Override
    public void onSendPacket(SendPacketEvent event) {
        if (event != null && event.packet != null) {
            this.onPacket(null, event.packet);
        }
    }

    @Override
    public void onReceivedPacket(ReceivedPacketEvent event) {
        if (event != null && event.packet != null && !(event.packet instanceof S29PacketSoundEffect) && !(event.packet instanceof S28PacketEffect)) {
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
                    Entity entity = useEntity.getEntityFromWorld(mc.theWorld);
                    if (entity != null) {
                        this.lastAttackedEntity = entity;
                    }
                } catch (Exception ignored) {
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

            long elapsedTime = System.currentTimeMillis() - freezeStartTime;
            boolean minDelayPassed = elapsedTime >= minDelay.get();

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

                    AxisAlignedBB entityBB = new AxisAlignedBB(x - 0.4, y - 0.1F, z - 0.4, x + 0.4, y + 1.9, z + 0.4);
                    double range = AaBbUtil.getLookingTargetRange(entityBB, mc.thePlayer);

                    if (range == Double.MAX_VALUE) {
                        range = AaBbUtil.getNearestPointBB(mc.thePlayer.getPositionEyes(1F), entityBB).distanceTo(mc.thePlayer.getPositionEyes(1F)) + 0.075;
                    }

                    if (minDelayPassed) {
                        if (range <= minRange.get()) {
                            shouldRelease = true;
                            break;
                        }

                        if (entity == lastAttackedEntity && timer.hasTimePassed(100)) {
                            if (range >= minRange.get()) {
                                shouldRelease = true;
                                break;
                            }
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
    public void onRender3D(Render3DEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null || !needFreeze) return;

        try {
            for (Entity entity : storageEntities) {
                if (entity == null || entity.isDead || !(entity instanceof EntityPlayer)) continue;

                double prevX, prevY, prevZ;
                double x = entity.serverPosX / 32.0;
                double y = entity.serverPosY / 32.0;
                double z = entity.serverPosZ / 32.0;

                float partialTicks = event.partialTicks;

                double[] prev = lastServerPos.get(entity.getEntityId());
                if (prev != null) {
                    prevX = prev[0];
                    prevY = prev[1];
                    prevZ = prev[2];
                } else {
                    prevX = x;
                    prevY = y;
                    prevZ = z;
                }

                Vec3 fixedPos = Lerper.lerpVec3(new Vec3(prevX, prevY, prevZ), new Vec3(x, y, z), partialTicks);

                double renderX = fixedPos.xCoord - mc.getRenderManager().viewerPosX;
                double renderY = fixedPos.yCoord - mc.getRenderManager().viewerPosY;
                double renderZ = fixedPos.zCoord - mc.getRenderManager().viewerPosZ;

                AxisAlignedBB boundingBox = new AxisAlignedBB(
                        renderX - 0.4, renderY, renderZ - 0.4,
                        renderX + 0.4, renderY + 1.8, renderZ + 0.4
                );

                GlStateManager.pushMatrix();
                GlStateManager.enableBlend();
                GlStateManager.disableTexture2D();
                GlStateManager.disableDepth();
                GlStateManager.depthMask(false);
                GlStateManager.disableLighting();
                GlStateManager.disableCull();
                GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

                RenderUtil.drawFilledBox(boundingBox, 1f, 0f, 0f, 0.3f);

                GlStateManager.enableCull();
                GlStateManager.depthMask(true);
                GlStateManager.enableDepth();
                GlStateManager.enableTexture2D();
                GlStateManager.disableBlend();
                GlStateManager.popMatrix();

                lastServerPos.put(entity.getEntityId(), new double[]{x, y, z});
            }
        } catch (Exception ignored) {
        }
    }

    @Override
    public void onLoadWorld(LoadWorldEvent event) {
        try {
            lastAttackedEntity = null;
            storageEntities.clear();
            storagePackets.clear();
            storageEntityMove.clear();
            needFreeze = false;
            freezeStartTime = 0L;
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

            AxisAlignedBB afterBB = new AxisAlignedBB(x - 0.4, y - 0.1F, z - 0.4, x + 0.4, y + 1.9, z + 0.4);
            double afterRange = AaBbUtil.getNearestPointBB(mc.thePlayer.getPositionEyes(1f), afterBB).distanceTo(mc.thePlayer.getPositionEyes(1F));
            double beforeRange = AaBbUtil.distanceTo(mc.thePlayer.getPositionEyes(1f), entity.getEntityBoundingBox());

            if (beforeRange <= detectionRange.get()) {
                if (afterRange >= minRange.get() && afterRange <= maxRange.get() && afterRange > beforeRange + 0.02 && ((EntityPlayer) entity).hurtTime <= getCalculatedMaxHurtTime()) {
                    if (!needFreeze) {
                        timer.reset();
                        needFreeze = true;
                        smoothPointer = System.nanoTime();
                        freezeStartTime = System.currentTimeMillis();
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
            if (!DewCommon.moduleManager.getModule(Velocity.class).isEnabled() && packet instanceof S12PacketEntityVelocity && mc.theWorld.getEntityByID(((S12PacketEntityVelocity) packet).getEntityID()) == mc.thePlayer) {
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
            lastAttackedEntity = null;
            smoothPointer = System.nanoTime();
            freezeStartTime = 0L;
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
            freezeStartTime = 0L;
            storagePackets.clear();
            storageEntities.clear();
            synchronized (storageEntityMove) {
                storageEntityMove.clear();
            }
        }
    }

    private void doSmoothRelease() {
        try {
            Entity target = this.lastAttackedEntity;

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