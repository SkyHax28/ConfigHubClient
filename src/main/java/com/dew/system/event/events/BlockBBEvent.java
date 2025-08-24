package com.dew.system.event.events;

import com.dew.system.event.EventArgument;
import com.dew.system.event.EventListener;
import net.minecraft.block.Block;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;

import java.util.Objects;

public class BlockBBEvent extends EventArgument {

    public BlockPos blockPos;
    public Block block;
    public AxisAlignedBB boundingBox;

    public BlockBBEvent(BlockPos blockPos, Block block, AxisAlignedBB boundingBox) {
        this.blockPos = blockPos;
        this.block = block;
        this.boundingBox = boundingBox;
    }

    @Override
    public void call(EventListener listener) {
        Objects.requireNonNull(listener).onBlockBB(this);
    }
}