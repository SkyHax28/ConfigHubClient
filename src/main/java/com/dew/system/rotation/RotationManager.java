package com.dew.system.rotation;

import com.dew.DewCommon;
import com.dew.IMinecraft;
import com.dew.system.event.events.PostMotionEvent;
import com.dew.utils.LogUtil;
import com.dew.utils.MovementUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.util.*;
import org.bouncycastle.crypto.prng.RandomGenerator;
import org.bouncycastle.crypto.prng.VMPCRandomGenerator;

public class RotationManager {
    private static final long ROTATION_TIMEOUT = 600L;
    private final Minecraft mc = IMinecraft.mc;
    private float clientYaw, clientPitch;
    private float prevClientYaw, prevClientPitch;
    private float prevRenderYaw, renderYaw;
    private float prevRenderHeadPitch, renderHeadPitch;
    private long lastRotationUpdate = 0L;
    private boolean isRotating = false;
    private boolean isReturning = false;
    private final RandomGenerator randomGenerator;

    public RotationManager() {
        this.clientYaw = 114514f;
        this.clientPitch = 114514f;
        this.prevClientYaw = 114514f;
        this.prevClientPitch = 114514f;
        this.renderYaw = 114514f;
        this.renderHeadPitch = 114514f;
        this.prevRenderYaw = 114514f;
        this.prevRenderHeadPitch = 114514f;

        randomGenerator = new VMPCRandomGenerator();
        byte[] seed = System.nanoTime() < 0 ? new byte[8] : new byte[16];
        randomGenerator.addSeedMaterial(seed);

        LogUtil.infoLog("init rotationManager");
    }

    public void tick() {
        if (isRotating) {
            if (System.currentTimeMillis() - lastRotationUpdate > 0) {
                this.updateRotations();
            }

            if (System.currentTimeMillis() - lastRotationUpdate > ROTATION_TIMEOUT) {
                isRotating = false;
                isReturning = true;
            } else {
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

            yawDiff += getSecureRandom() * 0.3f;
            pitchDiff += getSecureRandom() * 0.3f;

            if (Math.abs(yawDiff) < 1f && Math.abs(pitchDiff) < 15f) {
                this.updateRotations();

                mc.thePlayer.prevRenderArmYaw = this.clientYaw;
                mc.thePlayer.renderArmYaw = this.clientYaw;

                mc.thePlayer.rotationYaw = this.clientYaw;

                this.clientYaw = mc.thePlayer.rotationYaw;
                this.clientPitch = mc.thePlayer.rotationPitch;

                isReturning = false;
            } else {
                this.updateRotations();

                this.clientYaw += applyGCDFix(yawDiff);
                this.clientPitch += applyGCDFix(pitchDiff);
            }
        } else if (mc.thePlayer != null) {
            this.updateRotations();

            this.clientYaw = mc.thePlayer.rotationYaw;
            this.clientPitch = mc.thePlayer.rotationPitch;
        }
    }

    public void postMotionVisualTick() {
        if (!this.isRotating()) {
            if (mc.thePlayer == null) {
                prevRenderHeadPitch = 0f;
                renderHeadPitch = 0f;
                prevRenderYaw = 0f;
                renderYaw = 0f;
            } else {
                prevRenderHeadPitch = renderHeadPitch;
                renderHeadPitch = mc.thePlayer.rotationPitch;
                prevRenderYaw = renderYaw;
                renderYaw = mc.thePlayer.rotationYaw;
            }
            return;
        }

        prevRenderHeadPitch = renderHeadPitch;
        renderHeadPitch = this.getClientPitch();
        prevRenderYaw = renderYaw;
        renderYaw = this.getClientYaw();
    }

    private float getSecureRandom() {
        byte[] randomBytes = new byte[4];
        randomGenerator.nextBytes(randomBytes);
        return (float) ((randomBytes[0] & 0xFF) / 255.0);
    }

    public void resetRotationsInstantly() {
        if (isRotating) {
            isRotating = false;
            isReturning = false;

            if (mc.thePlayer != null) {
                this.updateRotations();

                this.clientYaw = mc.thePlayer.rotationYaw;
                this.clientPitch = mc.thePlayer.rotationPitch;
            }
        }
    }

    public void setRotationsInstantly(float yaw, float pitch) {
        this.updateRotations();

        this.clientYaw = yaw;
        this.clientPitch = pitch;
    }

    private void updateRotations() {
        this.prevClientYaw = this.clientYaw;
        this.prevClientPitch = this.clientPitch;
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

    public void facePosition(double x, double y, double z, float rotationSpeed) {
        float[] rotations = getRotationsTo(x, y, z);
        rotateToward(rotations[0], rotations[1], rotationSpeed);
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
        Vec3 eyePos = mc.thePlayer.getPositionEyes(1.0f);

        Vec3 facePoint = new Vec3(
                pos.getX() + 0.5 + facing.getFrontOffsetX() * 0.501,
                pos.getY() + 0.5 + facing.getFrontOffsetY() * 0.501,
                pos.getZ() + 0.5 + facing.getFrontOffsetZ() * 0.501
        );

        double dx = facePoint.xCoord - eyePos.xCoord;
        double dy = facePoint.yCoord - eyePos.yCoord;
        double dz = facePoint.zCoord - eyePos.zCoord;

        double distXZ = Math.sqrt(dx * dx + dz * dz);

        float targetYaw = (float) Math.toDegrees(Math.atan2(dz, dx)) - 90.0f;
        float targetPitch = (float) -Math.toDegrees(Math.atan2(dy, distXZ));

        rotateToward(targetYaw, targetPitch, rotationSpeed);
        return canHitBlockAtRotation(pos, facing, getClientYaw(), getClientPitch());
    }

    public void faceBlockHypixelSafe(float rotationSpeed, boolean slowdown) {
        rotateToward(snapToHypYaw((float) MovementUtil.getDirection(), slowdown), 80f, rotationSpeed);
    }

    public float tellySwap(float yaw) {
        float snappedBase = Math.round(yaw / 45.0f) * 45.0f;

        float lowerOffset;
        float upperOffset;

        if (Math.abs(snappedBase % 90.0f) < 0.001f) {
            lowerOffset = 0f;
            upperOffset = 0f;
        } else {
            lowerOffset = 45f;
            upperOffset = 45f;
        }

        float lowerCandidate = snappedBase - lowerOffset;
        float upperCandidate = snappedBase + upperOffset;

        return Math.abs(yaw - lowerCandidate) <= Math.abs(upperCandidate - yaw) ? lowerCandidate : upperCandidate;
    }

    private float snapToHypYaw(float yaw, boolean slowdown) {
        float snappedBase = Math.round(yaw / 45.0f) * 45.0f;

        float lowerOffset;
        float upperOffset;

        if (Math.abs(snappedBase % 90.0f) < 0.001f) {
            lowerOffset = 111f;
            upperOffset = 111f;
        } else {
            lowerOffset = 137f;
            upperOffset = 137f;
            if (slowdown) {
                MovementUtil.strafe(0.01f);
            }
        }

        float lowerCandidate = snappedBase - lowerOffset;
        float upperCandidate = snappedBase + upperOffset;

        return Math.abs(yaw - lowerCandidate) <= Math.abs(upperCandidate - yaw) ? lowerCandidate : upperCandidate;
    }

    public void rotateToward(float targetYaw, float targetPitch, float rotationSpeed) {
        float randomDelta = getSecureRandom() * 3f;
        float adjustedSpeed = rotationSpeed + randomDelta;

        float yawDiff = MathHelper.wrapAngleTo180_float(targetYaw - this.clientYaw);
        float pitchDiff = MathHelper.wrapAngleTo180_float(targetPitch - this.clientPitch);

        yawDiff += getSecureRandom() * 1.5f;
        pitchDiff += getSecureRandom() * 1.5f;

        yawDiff = MathHelper.clamp_float(yawDiff, -adjustedSpeed, adjustedSpeed);
        pitchDiff = MathHelper.clamp_float(pitchDiff, -adjustedSpeed, adjustedSpeed);

        yawDiff = applyGCDFix(yawDiff);
        pitchDiff = applyGCDFix(pitchDiff);

        this.updateRotations();

        this.clientYaw = this.clientYaw + yawDiff;
        this.clientPitch = MathHelper.clamp_float(this.clientPitch + pitchDiff, -90.0f, 90.0f);
        this.lastRotationUpdate = System.currentTimeMillis();
        this.isRotating = true;
    }

    public boolean canHitEntityFromPlayer(Entity target, double reach, boolean throughWalls) {
        if (mc.thePlayer == null || mc.theWorld == null || target == null) return false;

        Vec3 eyePos = mc.thePlayer.getPositionEyes(1.0f);
        AxisAlignedBB targetBox = target.getEntityBoundingBox().expand(0.1, 0.1, 0.1);

        Vec3 targetCenter = new Vec3(
                (targetBox.minX + targetBox.maxX) / 2.0,
                (targetBox.minY + targetBox.maxY) / 2.0,
                (targetBox.minZ + targetBox.maxZ) / 2.0
        );

        double distance = eyePos.distanceTo(targetCenter);
        if (distance > reach) return false;

        if (distance < 0.9 && mc.objectMouseOver != null && mc.objectMouseOver.typeOfHit == MovingObjectPosition.MovingObjectType.ENTITY) return true;

        MovingObjectPosition blockHit = mc.theWorld.rayTraceBlocks(eyePos, targetCenter, false, true, false);
        if (!throughWalls && blockHit != null && blockHit.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK) {
            return false;
        }

        MovingObjectPosition entityHit = targetBox.calculateIntercept(eyePos, targetCenter);
        return entityHit != null;
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

    public boolean canHitBlockAtRotation(BlockPos targetPos, EnumFacing facing, float yaw, float pitch) {
        double reach = mc.playerController.getBlockReachDistance();

        Vec3 eyePos = mc.thePlayer.getPositionEyes(1.0f);
        Vec3 lookVec = getLookVecFromRotations(yaw, pitch);
        Vec3 reachVec = eyePos.addVector(lookVec.xCoord * reach, lookVec.yCoord * reach, lookVec.zCoord * reach);

        MovingObjectPosition hit = mc.theWorld.rayTraceBlocks(eyePos, reachVec, false, false, false);

        return hit != null && hit.typeOfHit == MovingObjectPosition.MovingObjectType.BLOCK && hit.getBlockPos().equals(targetPos) && hit.sideHit == facing;
    }

    private Vec3 getLookVecFromRotations(float yaw, float pitch) {
        float yawRad = (float) Math.toRadians(yaw);
        float pitchRad = (float) Math.toRadians(pitch);

        float x = -MathHelper.cos(pitchRad) * MathHelper.sin(yawRad);
        float y = -MathHelper.sin(pitchRad);
        float z = MathHelper.cos(pitchRad) * MathHelper.cos(yawRad);

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

    public float[] getAngleDifferenceTo(Entity entity) {
        if (mc.thePlayer == null || entity == null) return new float[]{0f, 0f};

        Vec3 eyePos = mc.thePlayer.getPositionEyes(1.0F);

        double x = entity.posX;
        double y = entity.posY + entity.getEyeHeight() / 2.0;
        double z = entity.posZ;

        double diffX = x - eyePos.xCoord;
        double diffY = y - eyePos.yCoord;
        double diffZ = z - eyePos.zCoord;

        double distXZ = MathHelper.sqrt_double(diffX * diffX + diffZ * diffZ);

        float targetYaw = (float) (Math.atan2(diffZ, diffX) * (180D / Math.PI)) - 90F;
        float targetPitch = (float) -(Math.atan2(diffY, distXZ) * (180D / Math.PI));

        float currentYaw = mc.thePlayer.rotationYaw;
        float currentPitch = mc.thePlayer.rotationPitch;

        float yawDiff = MathHelper.wrapAngleTo180_float(targetYaw - currentYaw);
        float pitchDiff = MathHelper.wrapAngleTo180_float(targetPitch - currentPitch);

        return new float[]{Math.abs(yawDiff), Math.abs(pitchDiff)};
    }

    public float getInterpolatedYaw(float partialTicks) {
        return prevRenderYaw + (renderYaw - prevRenderYaw) * partialTicks;
    }

    public float getInterpolatedPitch(float partialTicks) {
        return prevRenderHeadPitch + (renderHeadPitch - prevRenderHeadPitch) * partialTicks;
    }

    public boolean isRotating() {
        return isRotating || isReturning;
    }

    public boolean isReturning() {
        return !isRotating && isReturning;
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