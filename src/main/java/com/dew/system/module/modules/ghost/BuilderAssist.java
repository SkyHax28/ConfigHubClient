package com.dew.system.module.modules.ghost;

import com.dew.DewCommon;
import com.dew.system.event.events.KeyPressableEvent;
import com.dew.system.event.events.KeyboardEvent;
import com.dew.system.event.events.LoadWorldEvent;
import com.dew.system.event.events.PreUpdateEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.module.modules.player.Scaffold;
import com.dew.utils.LogUtil;
import com.dew.utils.PacketUtil;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class BuilderAssist extends Module {
    private Map<BlockPos, IBlockState> savedChunkBlocks = new HashMap<>();
    private boolean hasSnapshot = false;
    private int lastChunkX = Integer.MAX_VALUE;
    private int lastChunkZ = Integer.MAX_VALUE;
    private List<BlockPos> pendingPlacements = new ArrayList<>();
    private int placementTick = 0;
    private final int PLACEMENT_DELAY = 2;

    // 復元用の追加マップ（復元中に参照するため）
    private Map<BlockPos, IBlockState> restorationMap = new HashMap<>();

    // アイテムスロット管理用
    private int originalSlot = -1;
    private boolean needSlotReset = false;
    private int slotResetTick = 0;

    // 回転待機用
    private BlockPos currentTargetPos = null;
    private EnumFacing currentTargetFacing = null;
    private boolean waitingForRotation = false;

    public BuilderAssist() {
        super("Builder Assist", ModuleCategory.GHOST, Keyboard.KEY_NONE, false, true, true);
    }

    @Override
    public void onDisable() {
        this.resetState();
    }

    @Override
    public void onLoadWorld(LoadWorldEvent event) {
        this.resetState();
    }

    private void resetState() {
        savedChunkBlocks.clear();
        restorationMap.clear();
        hasSnapshot = false;
        lastChunkX = Integer.MAX_VALUE;
        lastChunkZ = Integer.MAX_VALUE;
        pendingPlacements.clear();
        placementTick = 0;
        originalSlot = -1;
        needSlotReset = false;
        slotResetTick = 0;
        currentTargetPos = null;
        currentTargetFacing = null;
        waitingForRotation = false;
    }

    @Override
    public void onKeyboard(KeyboardEvent event) {
        if (event.key == Keyboard.KEY_RCONTROL && event.isPress) {
            if (!hasSnapshot) {
                saveCurrentChunk();
            } else {
                restoreChunk();
            }
        }
    }

    @Override
    public void onKeyPressable(KeyPressableEvent event) {
        // アイテムスロットリセット処理
        if (needSlotReset && slotResetTick <= 0) {
            if (originalSlot != -1 && mc.thePlayer != null) {
                mc.thePlayer.inventory.currentItem = originalSlot;
                mc.playerController.updateController();
                originalSlot = -1;
            }
            needSlotReset = false;
            slotResetTick = 0;
        }

        if (slotResetTick > 0) {
            slotResetTick--;
        }

        if (!pendingPlacements.isEmpty() && placementTick <= 0 && !needSlotReset) {
            BlockPos pos = pendingPlacements.remove(0);

            // 距離チェック (3ブロック以内だけ attemptBlockPlacement)
            if (mc.thePlayer.getDistanceSq(pos) <= 3 * 3) {
                attemptBlockPlacement(pos);
                placementTick = PLACEMENT_DELAY;
            } else {
                // 遠い場合は末尾に回す
                pendingPlacements.add(pos);
            }
        }

        if (placementTick > 0) {
            placementTick--;
        }

        if (pendingPlacements.isEmpty() && !restorationMap.isEmpty() && !needSlotReset) {
            restorationMap.clear();
            LogUtil.printChat("§a復元が完了しました！");
        }
    }

    private void saveCurrentChunk() {
        EntityPlayer player = mc.thePlayer;
        World world = mc.theWorld;

        if (player == null || world == null) return;

        savedChunkBlocks.clear();

        int radius = 20;
        int px = (int) player.posX;
        int py = (int) player.posY;
        int pz = (int) player.posZ;

        for (int x = px - radius; x <= px + radius; x++) {
            for (int y = Math.max(0, py - radius); y <= Math.min(255, py + radius); y++) {
                for (int z = pz - radius; z <= pz + radius; z++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    IBlockState state = world.getBlockState(pos);
                    if (state != null) {
                        savedChunkBlocks.put(pos, state);
                    }
                }
            }
        }

        hasSnapshot = true;
        LogUtil.printChat("§a周囲20ブロックのスナップショットを保存しました！ (" + savedChunkBlocks.size() + " blocks)");
    }

    private void restoreChunk() {
        EntityPlayer player = mc.thePlayer;
        World world = mc.theWorld;

        if (player == null || world == null || !hasSnapshot) return;

        pendingPlacements.clear();
        restorationMap.clear();
        int changedBlocks = 0;

        for (Map.Entry<BlockPos, IBlockState> entry : savedChunkBlocks.entrySet()) {
            BlockPos pos = entry.getKey();
            IBlockState savedState = entry.getValue();
            IBlockState currentState = world.getBlockState(pos);

            if (!savedState.equals(currentState)) {
                if (hasBlockInHotbar(savedState.getBlock())) {
                    pendingPlacements.add(pos);
                    restorationMap.put(pos, savedState);
                    changedBlocks++;
                }
            }
        }

        if (changedBlocks > 0) {
            final EntityPlayer playerFinal = player;
            final World worldFinal = world;

            pendingPlacements.sort((a, b) -> {
                boolean aHasNeighbor = hasSolidNeighbor(worldFinal, a);
                boolean bHasNeighbor = hasSolidNeighbor(worldFinal, b);
                if (aHasNeighbor && !bHasNeighbor) return -1;
                if (!aHasNeighbor && bHasNeighbor) return 1;

                int yCompare = Integer.compare(a.getY(), b.getY());
                if (yCompare != 0) return yCompare;

                double distA = playerFinal.getDistanceSq(a);
                double distB = playerFinal.getDistanceSq(b);
                return Double.compare(distA, distB);
            });

            LogUtil.printChat("§e" + changedBlocks + "個のブロックの復元を開始します...");
        }

        if (changedBlocks > 0) {
            LogUtil.printChat("§e" + changedBlocks + "個のブロックの復元を開始します...");
        } else {
            LogUtil.printChat("§a変更されたブロックが見つからないか、必要なブロックがホットバーにありません。");
        }

        hasSnapshot = false;
        savedChunkBlocks.clear();
    }

    private boolean hasSolidNeighbor(World world, BlockPos pos) {
        for (EnumFacing facing : EnumFacing.values()) {
            BlockPos neighbor = pos.offset(facing);
            IBlockState state = world.getBlockState(neighbor);
            Block block = state.getBlock();

            if (!block.getMaterial().isReplaceable()) {
                return true;
            }
        }
        return false;
    }

    private boolean hasBlockInHotbar(Block block) {
        EntityPlayer player = mc.thePlayer;
        if (player == null) return false;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (stack != null && stack.getItem() instanceof ItemBlock) {
                ItemBlock itemBlock = (ItemBlock) stack.getItem();
                if (itemBlock.getBlock() == block) {
                    return true;
                }
            }
        }
        return false;
    }

    private void attemptBlockPlacement(BlockPos pos) {
        EntityPlayer player = mc.thePlayer;
        World world = mc.theWorld;

        if (player == null || world == null) return;

        IBlockState targetState = restorationMap.get(pos);
        if (targetState == null) {
            LogUtil.printChat("targetState is null for pos: " + pos);
            return;
        }

        Block targetBlock = targetState.getBlock();

        ItemStack blockItem = null;
        int slotIndex = -1;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = player.inventory.getStackInSlot(i);
            if (stack != null && stack.getItem() instanceof ItemBlock) {
                ItemBlock itemBlock = (ItemBlock) stack.getItem();
                if (itemBlock.getBlock() == targetBlock) {
                    blockItem = stack;
                    slotIndex = i;
                    break;
                }
            }
        }

        if (blockItem == null) {
            LogUtil.printChat("No block item found for: " + targetBlock.getUnlocalizedName());
            return;
        }

        originalSlot = player.inventory.currentItem;
        player.inventory.currentItem = slotIndex;
        mc.playerController.updateController();

        PlaceResult result = tryPlaceBlockAt(pos);
        switch (result) {
            case SUCCESS:
                LogUtil.printChat("§aPlace success at " + pos);
                break;

            case FAIL_ROTATION:
            case FAIL_REACH:
                LogUtil.printChat("§eUnreachable, postponing placement at " + pos);
                pendingPlacements.add(pendingPlacements.size(), pos);
                break;

            case FAIL_OTHER:
                LogUtil.printChat("§eRetrying placement at " + pos + " (" + result + ")");
                pendingPlacements.add(pos);
                break;
        }

        needSlotReset = true;
        slotResetTick = 1;
    }

    private enum PlaceResult {
        SUCCESS,
        FAIL_ROTATION,
        FAIL_REACH,
        FAIL_OTHER
    }

    private PlaceResult tryPlaceBlockAt(BlockPos pos) {
        EnumFacing preferredFacing = facingFromRotation(mc.thePlayer.rotationYaw);

        EnumFacing[] orderedFacings = new EnumFacing[6];
        orderedFacings[0] = preferredFacing;
        int idx = 1;
        for (EnumFacing facing : EnumFacing.values()) {
            if (facing != preferredFacing) {
                orderedFacings[idx++] = facing;
            }
        }

        for (EnumFacing facing : orderedFacings) {
            BlockPos neighbor = pos.offset(facing);

            IBlockState state = mc.theWorld.getBlockState(neighbor);
            Block block = state.getBlock();

            if (!block.canCollideCheck(state, false)) continue;
            if (block.getMaterial().isReplaceable()) continue;

            EnumFacing opposite = facing.getOpposite();

            double hitX = neighbor.getX() + 0.5 + 0.5 * opposite.getFrontOffsetX();
            double hitY = neighbor.getY() + 0.5 + ((float) Math.random()) * 0.44F;
            double hitZ = neighbor.getZ() + 0.5 + 0.5 * opposite.getFrontOffsetZ();
            Vec3 hitVec = new Vec3(hitX, hitY, hitZ);

            boolean canRotate = DewCommon.rotationManager.faceBlockWithFacing(neighbor, opposite, 60f, false, true);
            if (!canRotate) {
                return PlaceResult.FAIL_ROTATION;
            }

            if (mc.thePlayer.getDistanceSq(hitX, hitY, hitZ) > 3 * 3) {
                return PlaceResult.FAIL_REACH;
            }

            ItemStack itemstack = mc.thePlayer.inventory.getCurrentItem();
            if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, itemstack, neighbor, opposite, hitVec)) {
                PacketUtil.sendPacket(new C0APacketAnimation());

                if (itemstack != null) {
                    if (itemstack.stackSize == 0) {
                        mc.thePlayer.inventory.mainInventory[mc.thePlayer.inventory.currentItem] = null;
                    } else if (mc.playerController.isInCreativeMode()) {
                        mc.entityRenderer.itemRenderer.resetEquippedProgress();
                    }
                }

                return PlaceResult.SUCCESS;
            }
        }

        return PlaceResult.FAIL_OTHER;
    }

    private EnumFacing facingFromRotation(float yaw) {
        yaw = yaw % 360;
        if (yaw < 0) yaw += 360;

        if (yaw >= 315 || yaw < 45) {
            return EnumFacing.SOUTH;
        } else if (yaw >= 45 && yaw < 135) {
            return EnumFacing.WEST;
        } else if (yaw >= 135 && yaw < 225) {
            return EnumFacing.NORTH;
        } else {
            return EnumFacing.EAST;
        }
    }
}