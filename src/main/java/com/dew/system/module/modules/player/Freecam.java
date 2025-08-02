package com.dew.system.module.modules.player;

import com.dew.system.event.events.*;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.mojang.authlib.GameProfile;
import net.minecraft.client.entity.EntityOtherPlayerMP;
import net.minecraft.entity.Entity;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C0BPacketEntityAction;
import net.minecraft.util.Vec3;
import org.lwjgl.input.Keyboard;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class Freecam extends Module {

    private final Map<Integer, Vec3> movementKeys = new HashMap<>();
    private EntityOtherPlayerMP freeCamEntity = null;
    private double camX, camY, camZ;
    private long lastRenderTime = System.nanoTime();
    private boolean loadable = false;
    public Freecam() {
        super("Freecam", ModuleCategory.PLAYER, Keyboard.KEY_F8, false, true, true);
    }

    @Override
    public void onEnable() {
        mc.renderGlobal.loadRenderers();
    }

    @Override
    public void onDisable() {
        if (mc.thePlayer != null) {
            mc.setRenderViewEntity(mc.thePlayer);
        }
        freeCamEntity = null;
        loadable = false;
        mc.renderGlobal.loadRenderers();
        movementKeys.clear();
    }

    @Override
    public void onLoadWorld(WorldLoadEvent event) {
        this.setState(false);
    }

    @Override
    public void onAttack(AttackEvent event) {
        if (freeCamEntity == null) return;

        Entity target = event.target;

        if (target != null && target == mc.thePlayer) {
            event.cancel();
        }
    }

    private void resetPlayerStateFirst() {
        mc.gameSettings.keyBindForward.setKeyDown(false);
        mc.gameSettings.keyBindBack.setKeyDown(false);
        mc.gameSettings.keyBindRight.setKeyDown(false);
        mc.gameSettings.keyBindLeft.setKeyDown(false);
        mc.gameSettings.keyBindJump.setKeyDown(false);
        mc.gameSettings.keyBindSprint.setKeyDown(false);
        mc.thePlayer.motionX = 0f;
        mc.thePlayer.motionY = 0f;
        mc.thePlayer.motionZ = 0f;
    }

    @Override
    public void onSendPacket(SendPacketEvent event) {
        if (mc.thePlayer == null || freeCamEntity == null) return;

        Packet<?> packet = event.packet;

        if (packet instanceof C0BPacketEntityAction && (((C0BPacketEntityAction) packet).getAction() == C0BPacketEntityAction.Action.STOP_SPRINTING || ((C0BPacketEntityAction) packet).getAction() == C0BPacketEntityAction.Action.START_SPRINTING)) {
            event.cancel();
        }
    }

    @Override
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        if (!loadable && freeCamEntity == null) {
            if (mc.thePlayer.isSprinting()) {
                mc.thePlayer.sendStopSprintingPacket();
            }
            this.resetPlayerStateFirst();
            loadable = true;
            return;
        }

        if (loadable && freeCamEntity != null) return;

        this.resetPlayerStateFirst();

        movementKeys.clear();
        movementKeys.put(Keyboard.KEY_W, new Vec3(0, 0, -1));
        movementKeys.put(Keyboard.KEY_S, new Vec3(0, 0, 1));
        movementKeys.put(Keyboard.KEY_A, new Vec3(-1, 0, 0));
        movementKeys.put(Keyboard.KEY_D, new Vec3(1, 0, 0));
        movementKeys.put(Keyboard.KEY_SPACE, new Vec3(0, 1, 0));
        movementKeys.put(Keyboard.KEY_LSHIFT, new Vec3(0, -1, 0));

        camX = mc.thePlayer.posX;
        camY = mc.thePlayer.posY;
        camZ = mc.thePlayer.posZ;

        freeCamEntity = new EntityOtherPlayerMP(mc.theWorld, new GameProfile(UUID.randomUUID(), "454545810san"));
        freeCamEntity.copyLocationAndAnglesFrom(mc.thePlayer);
        freeCamEntity.inventory.copyInventory(mc.thePlayer.inventory);
        freeCamEntity.inventoryContainer = new ContainerPlayer(freeCamEntity.inventory, !freeCamEntity.worldObj.isRemote, freeCamEntity);

        mc.setRenderViewEntity(freeCamEntity);
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        long now = System.nanoTime();
        float deltaTime = (now - lastRenderTime) / 1_000_000_000.0f;
        lastRenderTime = now;

        if (freeCamEntity != null) {
            Vec3 motion = new Vec3(0, 0, 0);
            for (Map.Entry<Integer, Vec3> entry : movementKeys.entrySet()) {
                if (Keyboard.isKeyDown(entry.getKey())) {
                    motion = motion.add(entry.getValue());
                }
            }

            double length = motion.lengthVector();
            if (length > 0) {
                motion = new Vec3(motion.xCoord / length, motion.yCoord / length, motion.zCoord / length);
            }

            float yawRad = (float) Math.toRadians(mc.thePlayer.rotationYaw);
            double sin = Math.sin(yawRad);
            double cos = Math.cos(yawRad);

            double dx = -motion.xCoord * cos + motion.zCoord * sin;
            double dz = -motion.xCoord * sin - motion.zCoord * cos;
            double dy = motion.yCoord;

            double speed = deltaTime * 30;

            camX += dx * speed;
            camY += dy * speed;
            camZ += dz * speed;

            freeCamEntity.setPosition(camX, camY, camZ);
            freeCamEntity.prevPosX = camX;
            freeCamEntity.prevPosY = camY;
            freeCamEntity.prevPosZ = camZ;
            freeCamEntity.lastTickPosX = camX;
            freeCamEntity.lastTickPosY = camY;
            freeCamEntity.lastTickPosZ = camZ;

            freeCamEntity.rotationYaw = mc.thePlayer.rotationYaw;
            freeCamEntity.rotationPitch = mc.thePlayer.rotationPitch;
            freeCamEntity.prevRotationYaw = mc.thePlayer.rotationYaw;
            freeCamEntity.prevRotationPitch = mc.thePlayer.rotationPitch;

            mc.thePlayer.renderArmYaw = freeCamEntity.rotationYaw;
            mc.thePlayer.renderArmPitch = freeCamEntity.rotationPitch;
            mc.thePlayer.prevRenderArmYaw = freeCamEntity.rotationYaw;
            mc.thePlayer.prevRenderArmPitch = freeCamEntity.rotationPitch;

            freeCamEntity.noClip = true;

            freeCamEntity.inventory.copyInventory(mc.thePlayer.inventory);
        }
    }
}
