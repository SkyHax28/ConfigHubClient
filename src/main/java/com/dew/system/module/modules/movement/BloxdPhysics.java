package com.dew.system.module.modules.movement;

import com.dew.system.event.events.ReceivedPacketEvent;
import com.dew.system.event.events.StrafeEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.utils.LogUtil;
import com.dew.utils.MovementUtil;
import com.dew.utils.TimerUtil;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S3FPacketCustomPayload;
import org.lwjgl.input.Keyboard;

public class BloxdPhysics extends Module {

    public static boolean canSpider = false;
    private final double DELTA = 1.0 / 30.0;
    private final double GRAVITY_Y = -10.0;
    private final double JUMP_MOTION = 0.41999998688697815;
    private final Vector3 impulseVector = new Vector3(0, 0, 0);
    private final Vector3 forceVector = new Vector3(0, 0, 0);
    private final Vector3 velocityVector = new Vector3(0, 0, 0);
    private final Vector3 gravityVector = new Vector3(0, GRAVITY_Y, 0);
    private int groundTicks = 0;
    private int bhopJumps = 0;
    private long knockbackEndTime = 0;
    private boolean wasClimbing = false;
    public BloxdPhysics() {
        super("Bloxd Physics", ModuleCategory.MOVEMENT, Keyboard.KEY_NONE, false, true, true);
    }

    @Override
    public void onDisable() {
        groundTicks = 0;
        bhopJumps = 0;
        knockbackEndTime = 0;
        wasClimbing = false;
        TimerUtil.resetTimerSpeed();
    }

    @Override
    public void onStrafe(StrafeEvent event) {
        if (mc.thePlayer == null) return;

        if (mc.thePlayer.onGround && velocityVector.y < 0) {
            velocityVector.set(0, 0, 0);
        }

        if (mc.thePlayer.onGround && mc.thePlayer.motionY == JUMP_MOTION) {
            bhopJumps = Math.min(bhopJumps + 1, 3);
            impulseVector.add(0, 8, 0);
        }

        boolean moving = Math.abs(event.forward) > 0 || Math.abs(event.strafe) > 0;
        if (mc.thePlayer.isCollidedHorizontally && moving && (!mc.thePlayer.onGround || mc.thePlayer.isOnLadder())) {
            velocityVector.set(0, 8, 0);
            wasClimbing = true;
        } else if (wasClimbing) {
            velocityVector.set(0, 0, 0);
            wasClimbing = false;
        }

        groundTicks = mc.thePlayer.onGround ? groundTicks + 1 : 0;
        if (groundTicks > 5) bhopJumps = 0;

        double speed = knockbackEndTime > System.currentTimeMillis() ? 1.0
                : mc.thePlayer.isUsingItem() ? 0.06 : 0.26 + 0.025 * bhopJumps;

        if (knockbackEndTime > System.currentTimeMillis()) {
            LogUtil.printChat("You can fly now");
            double yMotion = 0;
            if (mc.gameSettings.keyBindJump.isKeyDown())
                yMotion += 1.1f;
            if (mc.gameSettings.keyBindSneak.isKeyDown())
                yMotion -= 1.1f;

            MovementUtil.strafe(1.7f);
            mc.thePlayer.motionY = yMotion;
            TimerUtil.setTimerSpeed(2f);
            return;
        } else {
            TimerUtil.setTimerSpeed(1.5f);
        }

        Vector3 move = getMoveDirection(event.forward, event.strafe, mc.thePlayer.rotationYaw, speed);

        event.cancel();
        mc.thePlayer.motionX = move.x;
        mc.thePlayer.motionZ = move.z;
        mc.thePlayer.motionY = (mc.theWorld.getChunkFromBlockCoords(mc.thePlayer.getPosition()).isLoaded() || mc.thePlayer.posY <= 0) ? computeMotion().y * DELTA : 0;
    }

    @Override
    public void onReceivedPacket(ReceivedPacketEvent event) {
        Packet<?> packet = event.packet;

        if (packet instanceof S3FPacketCustomPayload && "bloxd:resyncphysics".equals(((S3FPacketCustomPayload) packet).getChannelName())) {
            ByteBuf data = ((S3FPacketCustomPayload) packet).getBufferData();
            impulseVector.set(0, 0, 0);
            forceVector.set(0, 0, 0);
            velocityVector.set(data.readFloat(), data.readFloat(), data.readFloat());
            bhopJumps = 0;
        } else if (packet instanceof S12PacketEntityVelocity && mc.thePlayer != null && mc.theWorld != null && mc.theWorld.getEntityByID(((S12PacketEntityVelocity) packet).getEntityID()) == mc.thePlayer) {
            knockbackEndTime = System.currentTimeMillis() + 1300;
        }
    }

    private Vector3 getMoveDirection(float forward, float strafe, float yaw, double speed) {
        double rad = Math.toRadians(yaw);
        double sin = Math.sin(rad);
        double cos = Math.cos(rad);

        double dist = Math.sqrt(forward * forward + strafe * strafe);
        if (dist > 1) {
            forward /= (float) dist;
            strafe /= (float) dist;
        }

        double x = strafe * cos - forward * sin;
        double z = forward * cos + strafe * sin;

        return new Vector3(x * speed, 0, z * speed);
    }

    private Vector3 computeMotion() {
        double invMass = 1.0;

        forceVector.multiply(invMass);
        forceVector.add(gravityVector);
        forceVector.multiply(2.0);

        impulseVector.multiply(invMass);
        forceVector.multiply(DELTA);
        impulseVector.add(forceVector);

        velocityVector.add(impulseVector);

        forceVector.set(0, 0, 0);
        impulseVector.set(0, 0, 0);

        return velocityVector;
    }

    public static class Vector3 {
        public double x, y, z;

        public Vector3(double x, double y, double z) {
            set(x, y, z);
        }

        public Vector3 set(double x, double y, double z) {
            this.x = x;
            this.y = y;
            this.z = z;
            return this;
        }

        public Vector3 add(double x, double y, double z) {
            this.x += x;
            this.y += y;
            this.z += z;
            return this;
        }

        public Vector3 add(Vector3 other) {
            return add(other.x, other.y, other.z);
        }

        public void multiply(double scalar) {
            this.x *= scalar;
            this.y *= scalar;
            this.z *= scalar;
        }
    }
}