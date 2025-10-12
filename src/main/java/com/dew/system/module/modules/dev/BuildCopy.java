package com.dew.system.module.modules.dev;

import com.dew.system.event.events.PreUpdateEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.utils.LogUtil;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import org.lwjgl.input.Keyboard;

import java.util.HashMap;
import java.util.Map;

public class BuildCopy extends Module {

    private Map<BlockPos, IBlockState> copiedBlocks = new HashMap<>();
    private Map<BlockPos, IBlockState> currentBlocks = new HashMap<>();
    private boolean isCopied = false;
    private boolean isRestoring = false;
    private int currentIndex = 0;
    private BlockPos[] changedPositions = null;
    private int tickDelay = 0;

    public BuildCopy() {
        super("Build Copy", ModuleCategory.PLAYER, Keyboard.KEY_NONE, false, true, true);
    }

    @Override
    public void onEnable() {
        if (!isCopied) {
            copyBlocks();
            LogUtil.printChat("§aブロックをコピーしました！");
        } else {
            startRestoration();
            LogUtil.printChat("§a復元を開始します...");
        }
    }

    @Override
    public void onDisable() {
        if (isRestoring) {
            isRestoring = false;
            LogUtil.printChat("§c復元を中断しました");
        }
    }

    @Override
    public void onPreUpdate(PreUpdateEvent event) {
        if (isRestoring && changedPositions != null) {
            if (tickDelay > 0) {
                tickDelay--;
                return;
            }

            if (currentIndex < changedPositions.length) {
                BlockPos pos = changedPositions[currentIndex];
                IBlockState targetState = copiedBlocks.get(pos);

                if (targetState != null && placeBlock(pos, targetState)) {
                    currentIndex++;
                }
            } else {
                isRestoring = false;
                isCopied = false;
                copiedBlocks.clear();
                currentBlocks.clear();
                LogUtil.printChat("§a復元が完了しました！");
                this.toggleState();
            }
        }
    }

    private void copyBlocks() {
        EntityPlayer player = mc.thePlayer;
        BlockPos playerPos = player.getPosition();

        copiedBlocks.clear();
        currentBlocks.clear();

        for (int x = -10; x <= 10; x++) {
            for (int y = -10; y <= 10; y++) {
                for (int z = -10; z <= 10; z++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    IBlockState state = mc.theWorld.getBlockState(pos);

                    if (state.getBlock().getMaterial().isSolid()) {
                        copiedBlocks.put(pos, state);
                        currentBlocks.put(pos, state);
                    }
                }
            }
        }

        isCopied = true;
    }

    private void startRestoration() {
        Map<BlockPos, IBlockState> changed = new HashMap<>();

        for (Map.Entry<BlockPos, IBlockState> entry : copiedBlocks.entrySet()) {
            BlockPos pos = entry.getKey();
            IBlockState originalState = entry.getValue();
            IBlockState currentState = mc.theWorld.getBlockState(pos);

            if (!currentState.equals(originalState)) {
                changed.put(pos, originalState);
            }
        }

        changedPositions = changed.keySet().toArray(new BlockPos[0]);
        currentIndex = 0;
        isRestoring = true;
        tickDelay = 0;

        if (changedPositions.length == 0) {
            LogUtil.printChat("§c変更されたブロックがありません");
            this.toggleState();
        }
    }

    private boolean placeBlock(BlockPos pos, IBlockState targetState) {
        EntityPlayerSP player = mc.thePlayer;

        Block targetBlock = targetState.getBlock();

        int slot = findBlockInHotbar(targetBlock);
        if (slot == -1) {
            return false;
        }

        if (!mc.theWorld.getEntitiesWithinAABBExcludingEntity(null, targetBlock.getCollisionBoundingBox(mc.theWorld, pos, targetState)).isEmpty()) {
            return false;
        }

        player.inventory.currentItem = slot;
        mc.playerController.updateController();

        EnumFacing facing = getPlacementFacing(pos);
        if (facing == null) {
            return false;
        }

        BlockPos placeAgainst = pos.offset(facing.getOpposite());

        lookAtBlockFace(placeAgainst, facing);

        if (mc.objectMouseOver != null && mc.objectMouseOver.getBlockPos() != null && mc.objectMouseOver.getBlockPos().equals(placeAgainst) && mc.objectMouseOver.sideHit == facing) {
            mc.rightClickMouse();
            return true;
        }

        return false;
    }

    private int findBlockInHotbar(Block targetBlock) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
            if (stack != null && stack.getItem() instanceof ItemBlock) {
                ItemBlock itemBlock = (ItemBlock) stack.getItem();
                if (itemBlock.getBlock() == targetBlock) {
                    return i;
                }
            }
        }

        return -1;
    }

    private EnumFacing getPlacementFacing(BlockPos pos) {
        for (EnumFacing facing : EnumFacing.values()) {
            BlockPos neighbor = pos.offset(facing.getOpposite());
            IBlockState neighborState = mc.theWorld.getBlockState(neighbor);

            if (neighborState.getBlock().getMaterial().isSolid()) {
                return facing;
            }
        }

        return EnumFacing.UP;
    }

    private void lookAtBlockFace(BlockPos placeAgainst, EnumFacing facing) {
        EntityPlayer player = mc.thePlayer;

        double x = placeAgainst.getX() + 0.5 + facing.getFrontOffsetX() * 0.5;
        double y = placeAgainst.getY() + 0.5 + facing.getFrontOffsetY() * 0.5;
        double z = placeAgainst.getZ() + 0.5 + facing.getFrontOffsetZ() * 0.5;

        double diffX = x - player.posX;
        double diffY = y - (player.posY + player.getEyeHeight());
        double diffZ = z - player.posZ;

        double dist = Math.sqrt(diffX * diffX + diffZ * diffZ);

        float yaw = (float) (Math.atan2(diffZ, diffX) * 180.0 / Math.PI) - 90.0f;
        float pitch = (float) -(Math.atan2(diffY, dist) * 180.0 / Math.PI);

        player.rotationYaw = yaw;
        player.rotationPitch = pitch;
    }
}