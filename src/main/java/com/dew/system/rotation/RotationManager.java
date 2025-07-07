package com.dew.system.rotation;

import com.dew.DewCommon;
import com.dew.IMinecraft;
import com.dew.utils.LogUtil;
import com.dew.utils.MovementUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.*;

import java.util.Random;

public class RotationManager {
    private final Minecraft mc = IMinecraft.mc;

    private float clientYaw, clientPitch;
    private float prevClientYaw, prevClientPitch;

    private long lastRotationUpdate = 0L;
    private boolean isRotating = false;
    private boolean isReturning = false;

    private static final long ROTATION_TIMEOUT = 300L;
    private Random random = new Random();

    public RotationManager() {
        this.clientYaw = 114514f;
        this.clientPitch = 114514f;
        this.prevClientYaw = 114514f;
        this.prevClientPitch = 114514f;

        LogUtil.infoLog("init rotationManager");
    }

    public void tick() {
        if (isRotating) {
            if (System.currentTimeMillis() - lastRotationUpdate > ROTATION_TIMEOUT) {
                isRotating = false;
                isReturning = true;
            } else {
                this.updateRotations();
                return;
            }
        }

        if (isReturning && mc.thePlayer != null) {
            float targetYaw = mc.thePlayer.rotationYaw;
            float targetPitch = mc.thePlayer.rotationPitch;

            float yawDiff = MathHelper.wrapAngleTo180_float(targetYaw - this.clientYaw);
            float pitchDiff = MathHelper.wrapAngleTo180_float(targetPitch - this.clientPitch);

            float returnSpeed = 30.0f;
            yawDiff = MathHelper.clamp_float(yawDiff, -returnSpeed, returnSpeed);
            pitchDiff = MathHelper.clamp_float(pitchDiff, -returnSpeed, returnSpeed);

            yawDiff += (random.nextFloat() - 0.5f) * 2.0f;
            pitchDiff += (random.nextFloat() - 0.5f) * 2.0f;

            this.prevClientYaw = this.clientYaw;
            this.prevClientPitch = this.clientPitch;

            this.clientYaw += applyGCDFix(yawDiff);
            this.clientPitch += applyGCDFix(pitchDiff);

            if (Math.abs(yawDiff) < 1f && Math.abs(pitchDiff) < 1f) {
                this.clientYaw = targetYaw;
                this.clientPitch = targetPitch;
                isReturning = false;
            }
        } else if (mc.thePlayer != null) {
            this.prevClientYaw = this.clientYaw;
            this.prevClientPitch = this.clientPitch;

            this.clientYaw = mc.thePlayer.rotationYaw;
            this.clientPitch = mc.thePlayer.rotationPitch;
        }
    }

    public void resetRotationsInstantly() {
        if (isRotating) {
            isRotating = false;
            isReturning = false;

            if (mc.thePlayer != null) {
                this.clientYaw = mc.thePlayer.rotationYaw;
                this.clientPitch = mc.thePlayer.rotationPitch;
                this.prevClientYaw = this.clientYaw;
                this.prevClientPitch = this.clientPitch;
            }
        }
    }

    private void updateRotations() {
        this.prevClientYaw = this.clientYaw;
        this.prevClientPitch = this.clientPitch;
    }

    private void setRotations(float yaw, float pitch) {
        this.clientYaw = applyGCDFix(yaw);
        this.clientPitch = MathHelper.clamp_float(applyGCDFix(pitch), -90.0f, 90.0f);
        onRotationUpdated();
    }

    private float getGCDValue() {
        double sensitivity = mc.gameSettings.mouseSensitivity;
        double f = sensitivity * 0.6F + 0.2F;
        return (float) (f * f * f * 8.0);
    }

    private float applyGCDFix(float delta) {
        float gcd = getGCDValue();
        return Math.round(delta / gcd) * gcd;
    }

    public void facePosition(double x, double y, double z) {
        float[] rotations = getRotationsTo(x, y, z);
        setRotations(rotations[0], rotations[1]);
    }

    public boolean faceEntity(Entity entity, float rotationSpeed) {
        float[] rotations = getRotationsTo(entity.posX, entity.posY + (entity.getEyeHeight() / 2.0), entity.posZ);

        float targetYaw = rotations[0];
        float targetPitch = rotations[1];

        float currentPitch = getClientPitch();

        rotateToward(targetYaw, currentPitch, rotationSpeed);

        if (canHitEntityAtRotation(entity, getClientYaw(), getClientPitch())) {
            return true;
        }

        rotateToward(targetYaw, targetPitch, rotationSpeed);
        return canHitEntityAtRotation(entity, getClientYaw(), getClientPitch());
    }

    public void faceBlock(BlockPos pos, float rotationSpeed) {
        double x = pos.getX() + 0.5;
        double y = pos.getY() + 0.5;
        double z = pos.getZ() + 0.5;
        float[] rotations = getRotationsTo(x, y, z);
        rotateToward(rotations[0], rotations[1], rotationSpeed);
    }

    public boolean faceBlockWithFacing(BlockPos pos, EnumFacing facing, float rotationSpeed) {
        Vec3 hitVec = new Vec3(
                pos.getX() + 0.5,
                pos.getY() + 0.5,
                pos.getZ() + 0.5
        ).addVector(
                facing.getFrontOffsetX() * 0.499D,
                facing.getFrontOffsetY() * 0.499D,
                facing.getFrontOffsetZ() * 0.499D
        );

        float[] rotations = getRotationsTo(hitVec.xCoord, hitVec.yCoord, hitVec.zCoord);
        rotateToward(rotations[0], rotations[1], rotationSpeed);
        return canHitBlockAtRotation(pos, getClientYaw(), getClientPitch());
    }

    public void faceBlockHypixelSafe(float rotationSpeed) {
        rotateToward(snapToHypYaw((float) MovementUtil.getDirection()), 80f, rotationSpeed);
    }

    private float snapToHypYaw(float yaw) {
        float snappedBase = Math.round(yaw / 45.0f) * 45.0f;

        float lowerOffset;
        float upperOffset;

        if (Math.abs(snappedBase % 90.0f) < 0.001f) {
            lowerOffset = 112f;
            upperOffset = 112f;
        } else {
            lowerOffset = 142f;
            upperOffset = 142f;
            MovementUtil.strafe(0.01f);
        }

        float lowerCandidate = snappedBase - lowerOffset;
        float upperCandidate = snappedBase + upperOffset;

        return Math.abs(yaw - lowerCandidate) <= Math.abs(upperCandidate - yaw) ? lowerCandidate : upperCandidate;
    }

    public void rotateToward(float targetYaw, float targetPitch, float rotationSpeed) {
        rotationSpeed += (random.nextFloat() - 0.5f) * 10.0f;

        float yawDiff = MathHelper.wrapAngleTo180_float(targetYaw - this.clientYaw);
        float pitchDiff = MathHelper.wrapAngleTo180_float(targetPitch - this.clientPitch);

        yawDiff = MathHelper.clamp_float(yawDiff, -rotationSpeed, rotationSpeed);
        pitchDiff = MathHelper.clamp_float(pitchDiff, -rotationSpeed, rotationSpeed);

        setRotations(this.clientYaw + yawDiff, this.clientPitch + pitchDiff);
    }

    public boolean canHitEntityAtRotation(Entity target, float yaw, float pitch) {
        double reach = mc.playerController.getBlockReachDistance();

        Vec3 eyePos = mc.thePlayer.getPositionEyes(1.0f);
        Vec3 lookVec = getLookVecFromRotations(yaw, pitch);
        Vec3 reachVec = eyePos.addVector(lookVec.xCoord * reach, lookVec.yCoord * reach, lookVec.zCoord * reach);

        AxisAlignedBB box = target.getEntityBoundingBox().expand(0.1, 0.1, 0.1);
        MovingObjectPosition hit = box.calculateIntercept(eyePos, reachVec);

        return hit != null;
    }

    public boolean canHitBlockAtRotation(BlockPos targetPos, float yaw, float pitch) {
        double reach = mc.playerController.getBlockReachDistance();

        Vec3 eyePos = mc.thePlayer.getPositionEyes(1.0f);
        Vec3 lookVec = getLookVecFromRotations(yaw, pitch);
        Vec3 reachVec = eyePos.addVector(lookVec.xCoord * reach, lookVec.yCoord * reach, lookVec.zCoord * reach);

        MovingObjectPosition hit = mc.theWorld.rayTraceBlocks(eyePos, reachVec, false, false, false);

        return hit != null && hit.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && hit.getBlockPos().equals(targetPos);
    }

    private Vec3 getLookVecFromRotations(float yaw, float pitch) {
        float yawRad = (float) Math.toRadians(yaw);
        float pitchRad = (float) Math.toRadians(pitch);

        float x = -MathHelper.cos(pitchRad) * MathHelper.sin(yawRad);
        float y = -MathHelper.sin(pitchRad);
        float z =  MathHelper.cos(pitchRad) * MathHelper.cos(yawRad);

        return new Vec3(x, y, z);
    }

    public float[] getRotationsTo(double x, double y, double z) {
        Vec3 eyePos = mc.thePlayer.getPositionEyes(1.0F);

        double diffX = x - eyePos.xCoord;
        double diffY = y - eyePos.yCoord;
        double diffZ = z - eyePos.zCoord;

        double distXZ = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ);

        float yaw = (float) (Math.atan2(diffZ, diffX) * (180D / Math.PI)) - 90F;
        float pitch = (float) -(Math.atan2(diffY, distXZ) * (180D / Math.PI));

        yaw = mc.thePlayer.rotationYaw + MathHelper.wrapAngleTo180_float(yaw - mc.thePlayer.rotationYaw);
        pitch = mc.thePlayer.rotationPitch + MathHelper.wrapAngleTo180_float(pitch - mc.thePlayer.rotationPitch);

        return new float[]{yaw, pitch};
    }

    public float getInterpolatedYaw(float partialTicks) {
        return prevClientYaw + (clientYaw - prevClientYaw) * partialTicks;
    }

    public float getInterpolatedPitch(float partialTicks) {
        return prevClientPitch + (clientPitch - prevClientPitch) * partialTicks;
    }

    private void onRotationUpdated() {
        this.lastRotationUpdate = System.currentTimeMillis();
        this.isRotating = true;
    }

    public boolean isRotating() {
        return DewCommon.handleEvents.canRotation() && (isRotating || isReturning);
    }

    public boolean isReturning() {
        return DewCommon.handleEvents.canRotation() && !isRotating && isReturning;
    }

    public float getClientYaw() {
        return clientYaw;
    }

    public float getClientPitch() {
        return clientPitch;
    }

    public float getPrevClientYaw() {
        return prevClientYaw;
    }

    public float getPrevClientPitch() {
        return prevClientPitch;
    }
}