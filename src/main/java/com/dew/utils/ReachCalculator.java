package com.dew.utils;

import net.minecraft.entity.Entity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.util.List;

public class ReachCalculator {

    private static final double EPS = 1e-6;

    public static boolean canReachToAttack(Entity attacker, Entity target, double maxReach, boolean throughWalls) {
        if (attacker == null || target == null) return false;
        if (attacker.worldObj == null) return false;

        World world = attacker.worldObj;

        Vec3 eyePos = getEntityEyePos(attacker);

        AxisAlignedBB targetBB = target.getEntityBoundingBox();
        Vec3 closestPoint = getClosestPointOnAABB(targetBB, eyePos);

        double dx = closestPoint.xCoord - eyePos.xCoord;
        double dy = closestPoint.yCoord - eyePos.yCoord;
        double dz = closestPoint.zCoord - eyePos.zCoord;
        double sqDist = dx * dx + dy * dy + dz * dz;

        if (sqDist <= (maxReach + EPS ) * (maxReach + EPS)) {
            double dist = Math.sqrt(sqDist);

            if (!throughWalls) {
                MovingObjectPosition blockHit = world.rayTraceBlocks(eyePos, closestPoint, false, true, false);
                if (blockHit != null) {
                    double bx = blockHit.hitVec.xCoord - eyePos.xCoord;
                    double by = blockHit.hitVec.yCoord - eyePos.yCoord;
                    double bz = blockHit.hitVec.zCoord - eyePos.zCoord;
                    double blockDist = Math.sqrt(bx * bx + by * by + bz * bz);
                    if (blockDist + EPS < dist) {
                        return false;
                    }
                }
            }

            AxisAlignedBB rayBB = new AxisAlignedBB(
                    Math.min(eyePos.xCoord, closestPoint.xCoord),
                    Math.min(eyePos.yCoord, closestPoint.yCoord),
                    Math.min(eyePos.zCoord, closestPoint.zCoord),
                    Math.max(eyePos.xCoord, closestPoint.xCoord),
                    Math.max(eyePos.yCoord, closestPoint.yCoord),
                    Math.max(eyePos.zCoord, closestPoint.zCoord)
            ).expand(1.0, 1.0, 1.0);

            List<Entity> entities = world.getEntitiesWithinAABBExcludingEntity(attacker, rayBB);
            for (Entity entity : entities) {
                if (entity == target) continue;
                if (!entity.canBeCollidedWith()) continue;

                AxisAlignedBB entityBoundingBox = entity.getEntityBoundingBox();
                MovingObjectPosition intercept = entityBoundingBox.calculateIntercept(eyePos, closestPoint);
                if (intercept == null) continue;

                double ix = intercept.hitVec.xCoord - eyePos.xCoord;
                double iy = intercept.hitVec.yCoord - eyePos.yCoord;
                double iz = intercept.hitVec.zCoord - eyePos.zCoord;
                double idist = Math.sqrt(ix * ix + iy * iy + iz * iz);
                if (idist + EPS < dist) {
                    return false;
                }
            }

            return true;
        }

        return false;
    }

    private static Vec3 getEntityEyePos(Entity e) {
        double x = e.posX;
        double y = e.posY + e.getEyeHeight();
        double z = e.posZ;
        return new Vec3(x, y, z);
    }

    private static Vec3 getClosestPointOnAABB(AxisAlignedBB aabb, Vec3 point) {
        double x = clamp(point.xCoord, aabb.minX, aabb.maxX);
        double y = clamp(point.yCoord, aabb.minY, aabb.maxY);
        double z = clamp(point.zCoord, aabb.minZ, aabb.maxZ);
        return new Vec3(x, y, z);
    }

    private static double clamp(double v, double min, double max) {
        if (v < min) return min;
        return Math.min(v, max);
    }
}
