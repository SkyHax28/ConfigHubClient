package com.dew.system.module.modules.player;

import com.dew.DewCommon;
import com.dew.system.event.events.PreUpdateEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.settingsvalue.BooleanValue;
import com.dew.system.settingsvalue.NumberValue;
import com.dew.utils.PacketUtil;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import org.lwjgl.input.Keyboard;

import java.util.Comparator;
import java.util.PriorityQueue;

public class Scaffold2 extends Module {

    private static final NumberValue rangeSetting = new NumberValue("Range", 3.0, 1.0, 6.0, 1.0);
    private static final NumberValue placeDelay = new NumberValue("Place Delay", 0.0, 0.0, 5.0, 1.0);
    private static final BooleanValue preferHighestStack = new BooleanValue("Prefer Highest Stack", true);
    private int delay = 0;
    private int lastBlockSlot = -1;
    private BlockPos lastPlaced = null;
    private int keepY = -1;
    public Scaffold2() {
        super("Scaffold2", ModuleCategory.PLAYER, Keyboard.KEY_NONE, false, true, true);
    }

    @Override
    public void onDisable() {
        keepY = -1;
        lastPlaced = null;
        lastBlockSlot = -1;
    }

    @Override
    public void onPreUpdate(PreUpdateEvent event) {
        delay++;

        if (mc.thePlayer == null || mc.theWorld == null) return;

        if (keepY == -1) keepY = (int) mc.thePlayer.posY;
        if (delay <= placeDelay.get().intValue()) return;

        BlockPos base = new BlockPos(mc.thePlayer.posX, keepY - 1, mc.thePlayer.posZ);

        BlockPos target = findBestPlacePos(base, rangeSetting.get().intValue());
        if (target == null) return;

        int slot = getValidBlockSlot();
        if (slot == -1) return;

        if (mc.thePlayer.inventory.currentItem != slot) {
            mc.thePlayer.inventory.currentItem = slot;
            mc.playerController.updateController();
        }

        for (EnumFacing facing : EnumFacing.values()) {
            BlockPos neighbor = target.offset(facing);
            IBlockState neighborState = mc.theWorld.getBlockState(neighbor);
            Block neighborBlock = neighborState.getBlock();

            if (!neighborBlock.canCollideCheck(neighborState, false)) continue;

            EnumFacing opposite = facing.getOpposite();
            Vec3 hitVec = new Vec3(
                    target.getX() + 0.5 + 0.5 * opposite.getFrontOffsetX(),
                    target.getY() + 0.5 + 0.5 * opposite.getFrontOffsetY(),
                    target.getZ() + 0.5 + 0.5 * opposite.getFrontOffsetZ()
            );

            if (mc.playerController.onPlayerRightClick(
                    mc.thePlayer,
                    mc.theWorld,
                    mc.thePlayer.inventory.getCurrentItem(),
                    target,
                    opposite,
                    hitVec
            )) {
                PacketUtil.sendPacket(new C0APacketAnimation());
                delay = 0;
                lastPlaced = target;
                break;
            }
        }
    }

    private BlockPos findBestPlacePos(BlockPos base, int range) {
        if (isValidPlaceTarget(base)) return base;

        for (EnumFacing dir : EnumFacing.HORIZONTALS) {
            BlockPos offset = base.offset(dir);
            if (isValidPlaceTarget(offset)) return offset;
        }

        PriorityQueue<BlockPos> queue = new PriorityQueue<>(Comparator.comparingDouble(pos -> mc.thePlayer.getDistanceSqToCenter(pos)));
        for (int dx = -range; dx <= range; dx++) {
            for (int dz = -range; dz <= range; dz++) {
                BlockPos offset = base.add(dx, 0, dz);
                if (mc.thePlayer.getDistanceSqToCenter(offset) > range * range) continue;
                if (isValidPlaceTarget(offset)) queue.add(offset);
            }
        }

        return queue.isEmpty() ? null : queue.poll();
    }

    private boolean isValidPlaceTarget(BlockPos pos) {
        if (!mc.theWorld.getBlockState(pos).getBlock().isReplaceable(mc.theWorld, pos)) return false;
        AxisAlignedBB box = new AxisAlignedBB(pos, pos.add(1, 1, 1));
        if (mc.thePlayer.getEntityBoundingBox().intersectsWith(box)) return false;

        for (EnumFacing facing : EnumFacing.values()) {
            if (!mc.theWorld.getBlockState(pos.offset(facing)).getBlock().isReplaceable(mc.theWorld, pos.offset(facing)))
                return true;
        }
        return false;
    }

    private int getValidBlockSlot() {
        int bestSlot = -1;
        int maxStackSize = -1;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
            if (stack == null || !(stack.getItem() instanceof ItemBlock)) continue;

            if (DewCommon.moduleManager.getModule(Scaffold.class).isInvalidBlock(stack)) continue;

            if (!preferHighestStack.get()) return i;

            if (stack.stackSize > maxStackSize) {
                maxStackSize = stack.stackSize;
                bestSlot = i;
            }
        }

        return bestSlot;
    }
}