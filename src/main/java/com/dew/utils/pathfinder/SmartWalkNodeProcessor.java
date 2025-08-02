package com.dew.utils.pathfinder;

import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.init.Blocks;
import net.minecraft.pathfinding.PathPoint;
import net.minecraft.util.BlockPos;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.pathfinder.WalkNodeProcessor;

public class SmartWalkNodeProcessor extends WalkNodeProcessor {

    public static int evaluatePosition(IBlockAccess world, Entity entity, int x, int y, int z, int sizeX, int sizeY, int sizeZ, boolean avoidWater, boolean breakDoors, boolean enterDoors) {
        boolean encounteredSpecialBlock = false;

        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();

        for (int dx = x; dx < x + sizeX; dx++) {
            for (int dy = y; dy < y + sizeY; dy++) {
                for (int dz = z; dz < z + sizeZ; dz++) {
                    pos.set(dx, dy, dz);
                    Block block = world.getBlockState(pos).getBlock();

                    if (block.getMaterial() != Material.air) {
                        if (block instanceof BlockDoor) {
                            if (!enterDoors && block.getMaterial() == Material.wood) {
                                return 0;
                            }
                            encounteredSpecialBlock = true;
                            continue;
                        }

                        if (block == Blocks.water || block == Blocks.flowing_water) {
                            if (avoidWater) return -1;
                            encounteredSpecialBlock = true;
                            continue;
                        }

                        if (block.getMaterial() == Material.lava && !entity.isInLava()) {
                            return -2;
                        }

                        if (block instanceof BlockRailBase) {
                            Block below = world.getBlockState(pos.down()).getBlock();
                            if (!(below instanceof BlockRailBase)) return -3;
                            continue;
                        }

                        if (!block.isPassable(world, pos)) {
                            if (block instanceof BlockFence || block instanceof BlockFenceGate || block instanceof BlockWall)
                                return -3;
                            if (block == Blocks.trapdoor || block == Blocks.iron_trapdoor) return -4;
                            if (block.getMaterial().isSolid()) return 0;
                        }
                    }
                }
            }
        }

        return encounteredSpecialBlock ? 2 : 1;
    }

    @Override
    public void initProcessor(IBlockAccess worldIn, Entity entityIn) {
        super.initProcessor(worldIn, entityIn);
        this.setAvoidsWater(true);
        this.setCanSwim(true);
        this.setEnterDoors(true);
        this.setBreakDoors(false);
    }

    @Override
    public int findPathOptions(PathPoint[] pathOptions, Entity entityIn, PathPoint currentPoint, PathPoint targetPoint, float maxDistance) {
        int options = 0;
        int canStepUp = this.getVerticalOffset(entityIn, currentPoint.xCoord, currentPoint.yCoord + 1, currentPoint.zCoord) == 1 ? 1 : 0;

        PathPoint north = getSafePoint(entityIn, currentPoint.xCoord, currentPoint.yCoord, currentPoint.zCoord - 1, canStepUp);
        PathPoint south = getSafePoint(entityIn, currentPoint.xCoord, currentPoint.yCoord, currentPoint.zCoord + 1, canStepUp);
        PathPoint west = getSafePoint(entityIn, currentPoint.xCoord - 1, currentPoint.yCoord, currentPoint.zCoord, canStepUp);
        PathPoint east = getSafePoint(entityIn, currentPoint.xCoord + 1, currentPoint.yCoord, currentPoint.zCoord, canStepUp);

        if (north != null && !north.visited && north.distanceTo(targetPoint) < maxDistance)
            pathOptions[options++] = north;
        if (south != null && !south.visited && south.distanceTo(targetPoint) < maxDistance)
            pathOptions[options++] = south;
        if (west != null && !west.visited && west.distanceTo(targetPoint) < maxDistance) pathOptions[options++] = west;
        if (east != null && !east.visited && east.distanceTo(targetPoint) < maxDistance) pathOptions[options++] = east;

        return options;
    }

    private PathPoint getSafePoint(Entity entity, int x, int y, int z, int stepHeight) {
        int verticalCheck = getVerticalOffset(entity, x, y, z);

        if (verticalCheck == 2) {
            return this.openPoint(x, y, z);
        }

        if (verticalCheck == 1) {
            return this.openPoint(x, y, z);
        }

        if (stepHeight > 0 && verticalCheck != -3 && verticalCheck != -4 && getVerticalOffset(entity, x, y + stepHeight, z) == 1) {
            return this.openPoint(x, y + stepHeight, z);
        }

        return null;
    }

    private int getVerticalOffset(Entity entity, int x, int y, int z) {
        return SmartWalkNodeProcessor.evaluatePosition(this.blockaccess, entity, x, y, z, this.entitySizeX, this.entitySizeY, this.entitySizeZ, this.avoidsWater, this.canBreakDoors, this.canEnterDoors);
    }
}