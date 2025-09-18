package com.dew.utils;

import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;

public class AaBbUtil {

    public static AxisAlignedBB expands(AxisAlignedBB box, double v) {
        return expands(box, v, true, true);
    }

    public static AxisAlignedBB expands(AxisAlignedBB box, double v, boolean modifyYDown, boolean modifyYUp) {
        return new AxisAlignedBB(
                box.minX - v,
                box.minY - (modifyYDown ? v : 0.0),
                box.minZ - v,
                box.maxX + v,
                box.maxY + (modifyYUp ? v : 0.0),
                box.maxZ + v
        );
    }

    public static Vec3 getNearestPointBB(Vec3 eye, AxisAlignedBB box) {
        double[] origin = { eye.xCoord, eye.yCoord, eye.zCoord };
        double[] destMins = { box.minX, box.minY, box.minZ };
        double[] destMaxs = { box.maxX, box.maxY, box.maxZ };

        for (int i = 0; i < 3; i++) {
            if (origin[i] > destMaxs[i]) {
                origin[i] = destMaxs[i];
            } else if (origin[i] < destMins[i]) {
                origin[i] = destMins[i];
            }
        }

        return new Vec3(origin[0], origin[1], origin[2]);
    }

    public static double getLookingTargetRange(AxisAlignedBB box, EntityPlayerSP thePlayer, double range) {
        Vec3 eyes = thePlayer.getPositionEyes(1.0F);
        Vec3 lookVec = thePlayer.getLook(1.0F);
        Vec3 target = eyes.addVector(lookVec.xCoord * range, lookVec.yCoord * range, lookVec.zCoord * range);

        MovingObjectPosition movingObj = box.calculateIntercept(eyes, target);
        if (movingObj == null) {
            return Double.MAX_VALUE;
        }

        return movingObj.hitVec.distanceTo(eyes);
    }

    public static double getLookingTargetRange(AxisAlignedBB box, EntityPlayerSP thePlayer) {
        return getLookingTargetRange(box, thePlayer, 6.0);
    }

    public static double distanceTo(Vec3 vec, AxisAlignedBB box) {
        Vec3 pos = getNearestPointBB(vec, box);

        double xDist = Math.abs(pos.xCoord - vec.xCoord);
        double yDist = Math.abs(pos.yCoord - vec.yCoord);
        double zDist = Math.abs(pos.zCoord - vec.zCoord);

        return Math.sqrt(xDist * xDist + yDist * yDist + zDist * zDist);
    }
}
