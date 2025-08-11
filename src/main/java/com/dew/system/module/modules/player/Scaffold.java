package com.dew.system.module.modules.player;

import com.dew.DewCommon;
import com.dew.system.event.events.*;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.module.modules.exploit.SafetySwitchv2000;
import com.dew.system.module.modules.movement.MoveFix;
import com.dew.system.module.modules.movement.speed.SpeedModule;
import com.dew.system.module.modules.render.Hud;
import com.dew.system.settingsvalue.BooleanValue;
import com.dew.system.settingsvalue.NumberValue;
import com.dew.system.settingsvalue.SelectionValue;
import com.dew.utils.MovementUtil;
import com.dew.utils.RenderUtil;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.*;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.util.Comparator;
import java.util.PriorityQueue;

public class Scaffold extends Module {

    private static final SelectionValue mode = new SelectionValue("Mode", "Normal", "Normal", "Telly", "Hypixel");
    public static final BooleanValue hypixelTellyBanFix = new BooleanValue("Hypixel Telly Ban Fix", false, () -> mode.get().equals("Telly"));
    private static final NumberValue tellyPreRotationSpeed = new NumberValue("Telly Pre Rotation Speed", 35.0, 0.0, 180.0, 5.0, () -> mode.get().equals("Telly") && !hypixelTellyBanFix.get());
    private static final NumberValue rotationSpeed = new NumberValue("Rotation Speed", 60.0, 0.0, 180.0, 5.0, () -> mode.get().equals("Normal") || mode.get().equals("Telly") && !hypixelTellyBanFix.get());
    private static final NumberValue clutchRange = new NumberValue("Clutch Range", 3.0, 1.0, 5.0, 1.0);
    private static final NumberValue placeDelay = new NumberValue("Place Delay", 0.0, 0.0, 3.0, 1.0, () -> mode.get().equals("Normal") || mode.get().equals("Telly"));
    private static final SelectionValue towerMode = new SelectionValue("Tower Mode", "OFF", "OFF", "Vanilla", "Hypixel");
    private static final SelectionValue edgeSafeMode = new SelectionValue("Edge Safe Mode", "OFF", "OFF", "Safewalk", "Sneak");
    public static final BooleanValue preferHighestStack = new BooleanValue("Prefer Highest Stack", true);
    public static final BooleanValue noSprint = new BooleanValue("No Sprint", false);
    private final EnumFacing[] facingsArray = EnumFacing.values();
    public boolean holdingBlock = false;
    public boolean jumped = false;
    private int keepY = -1;
    private int originalSlot = -1;
    private boolean isSneaking = false;
    private int jumpTicks = 0;
    private int towerTicks = 0;
    private boolean hypGroundCheck = false;
    private boolean towered = false;
    private int delay = 0;
    private BlockPos lastPlacedPos = null;

    public Scaffold() {
        super("Scaffold", ModuleCategory.PLAYER, Keyboard.KEY_NONE, false, true, true);
    }

    private boolean canTower() {
        return Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode()) && MovementUtil.isBlockUnderPlayer(mc.thePlayer, 3, 2, false) && !MovementUtil.isBlockAbovePlayer(mc.thePlayer, 1, 0.3) && DewCommon.moduleManager.getModule(Hud.class).getTotalValidBlocksInHotbar() > 0;
    }

    private boolean shouldUpdateKeepYState() {
        return keepY == -1 || !DewCommon.moduleManager.getModule(SpeedModule.class).isEnabled() && !mode.get().equals("Telly") || Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode()) && (!MovementUtil.isBlockUnderPlayer(mc.thePlayer, 1, false) || !mode.get().equals("Telly"));
    }

    @Override
    public String tag() {
        return mode.get();
    }

    @Override
    public void onDisable() {
        this.resetState();
        keepY = -1;
        lastPlacedPos = null;
    }

    @Override
    public void onLoadWorld(WorldLoadEvent event) {
        if (DewCommon.moduleManager.getModule(SafetySwitchv2000.class).isEnabled()) {
            this.setState(false);
        }
    }

    private void resetState() {
        if (originalSlot != -1 && mc.thePlayer != null) {
            mc.thePlayer.inventory.currentItem = originalSlot;
            mc.playerController.updateController();
        }
        originalSlot = -1;
        delay = 0;
        holdingBlock = false;
        if (isSneaking) {
            mc.gameSettings.keyBindSneak.setKeyDown(false);
            isSneaking = false;
        }
        jumpTicks = 0;
        towerTicks = 0;
        hypGroundCheck = false;
        jumped = false;
        if (towered) {
            if (towerMode.get().equals("Vanilla")) {
                MovementUtil.stopYMotion();
            }

            MovementUtil.mcJumpNoBoost = false;
            hypGroundCheck = false;
            towered = false;
        }
    }

    @Override
    public void onMove(MoveEvent event) {
        if (edgeSafeMode.get().equals("Safewalk")) {
            event.isSafeWalk = true;
        }
    }

    @Override
    public void onItemRender(ItemRenderEvent event) {
        if (originalSlot != -1) {
            event.itemToRender = mc.thePlayer.inventory.getStackInSlot(originalSlot);
        }
    }

    @Override
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        jumpTicks = mc.thePlayer.onGround ? 0 : jumpTicks + 1;
        delay++;

        this.doMainFunctions();
        this.tellyFunction();
    }

    @Override
    public void onPreMotion(PreMotionEvent event) {
        this.jumpCheck();
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null || lastPlacedPos == null) return;

        double renderX = mc.getRenderManager().viewerPosX;
        double renderY = mc.getRenderManager().viewerPosY;
        double renderZ = mc.getRenderManager().viewerPosZ;

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

        double x = lastPlacedPos.getX() - renderX;
        double y = lastPlacedPos.getY() - renderY;
        double z = lastPlacedPos.getZ() - renderZ;
        AxisAlignedBB bb = new AxisAlignedBB(
                x, y, z,
                x + 1, y + 1, z + 1
        );
        RenderUtil.drawFilledBox(bb, 1f, 0f, 0f, 0.2f);

        GlStateManager.enableCull();
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private void jumpCheck() {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        if (!mc.thePlayer.onGround && !jumped) {
            jumped = true;
        }

        towerTicks = mc.thePlayer.onGround ? 0 : towerTicks + 1;

        this.towerFunction();
    }

    private void updateKeepY() {
        keepY = (int) mc.thePlayer.posY;
    }

    private void doMainFunctions() {
        if (edgeSafeMode.get().equals("Sneak") && this.isNearEdge()) {
            mc.gameSettings.keyBindSneak.setKeyDown(true);
            isSneaking = true;
        } else if (isSneaking) {
            mc.gameSettings.keyBindSneak.setKeyDown(false);
            isSneaking = false;
        }

        switch (mode.get().toLowerCase()) {
            case "normal":
                if (DewCommon.rotationManager.isReturning() || !holdingBlock) {
                    DewCommon.rotationManager.rotateToward((float) (MovementUtil.getDirection() - 180f), 83f, rotationSpeed.get().floatValue(), true);
                }
                break;

            case "hypixel":
                if (mc.thePlayer.ticksExisted % 2 == 0) {
                    DewCommon.rotationManager.faceBlockHypixelSafe(180f, true);
                }
                break;

            case "telly":
                if (mc.thePlayer.isPotionActive(Potion.moveSpeed) && (DewCommon.rotationManager.isReturning() || !holdingBlock)) {
                    DewCommon.rotationManager.rotateToward((float) (MovementUtil.getDirection() - 180f), 83f, hypixelTellyBanFix.get() ? 30f : rotationSpeed.get().floatValue(), true);
                }
                break;

            default:
                break;
        }

        if (noSprint.get() || DewCommon.moduleManager.getModule(MoveFix.class).isEnabled() && Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode())) {
            mc.thePlayer.setSprinting(false);
        }

        if (this.isTelly() && !Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode())) {
            return;
        }

        if (shouldUpdateKeepYState()) {
            this.updateKeepY();
        }

        if (delay <= placeDelay.get().intValue()) return;

        if (holdingBlock && (mc.thePlayer.getHeldItem() == null || !(mc.thePlayer.getHeldItem().getItem() instanceof ItemBlock) || mc.thePlayer.getHeldItem().stackSize == 0)) {
            this.resetState();
            return;
        }

        if (keepY == -1) return;

        BlockPos below = new BlockPos(mc.thePlayer.posX, keepY - 1, mc.thePlayer.posZ);

        if (!mc.theWorld.getBlockState(below).getBlock().isReplaceable(mc.theWorld, below)) return;

        int blockSlot = getValidBlockSlot();
        if (blockSlot == -1) return;

        if (!holdingBlock || mc.thePlayer.inventory.currentItem != blockSlot) {
            if (!holdingBlock) {
                originalSlot = mc.thePlayer.inventory.currentItem;
            }
            mc.thePlayer.inventory.currentItem = blockSlot;
            mc.playerController.updateController();
            holdingBlock = true;
        }

        Boolean placed = null;

        PlaceResult result = tryPlaceBlock(below);

        if (result == PlaceResult.SUCCESS) {
            lastPlacedPos = below;
            placed = true;
        } else if (result == PlaceResult.FAIL_ROTATION) {
            return;
        } else {
            for (EnumFacing dir : getFullPrioritizedFacings()) {
                BlockPos neighbor = below.offset(dir);
                PlaceResult neighborResult = tryPlaceBlock(neighbor);

                if (neighborResult == PlaceResult.FAIL_ROTATION) return;

                if (neighborResult == PlaceResult.SUCCESS) {
                    lastPlacedPos = neighbor;
                    placed = true;
                    break;
                }
            }
        }

        int range = clutchRange.get().intValue();
        if (!Boolean.TRUE.equals(placed) && range > 1) {
            double maxDistanceSq = range * range;

            PriorityQueue<BlockPos> searchQueue = new PriorityQueue<>(
                    Comparator.comparingDouble(pos -> mc.thePlayer.getDistanceSqToCenter(pos))
            );

            for (int dx = -range; dx <= range; dx++) {
                for (int dz = -range; dz <= range; dz++) {
                    BlockPos target = below.add(dx, 0, dz);
                    if (mc.thePlayer.getDistanceSqToCenter(target) > maxDistanceSq) continue;

                    AxisAlignedBB targetBB = new AxisAlignedBB(
                            target.getX(), target.getY(), target.getZ(),
                            target.getX() + 1, target.getY() + 1, target.getZ() + 1
                    );

                    if (mc.thePlayer.getEntityBoundingBox().intersectsWith(targetBB)) continue;

                    searchQueue.add(target);
                }
            }

            while (!searchQueue.isEmpty()) {
                BlockPos target = searchQueue.poll();
                PlaceResult clutchResult = tryPlaceBlock(target);

                if (clutchResult == PlaceResult.FAIL_ROTATION) return;

                if (clutchResult == PlaceResult.SUCCESS) {
                    lastPlacedPos = target;
                    placed = true;
                    break;
                }
            }
        }

        if (Boolean.TRUE.equals(placed)) {
            holdingBlock = true;
        }
    }

    private void towerFunction() {
        if (this.canTower() && !towerMode.get().equals("OFF") || towerMode.get().equals("Hypixel") && hypGroundCheck && !DewCommon.moduleManager.getModule(SpeedModule.class).isEnabled()) {
            MovementUtil.mcJumpNoBoost = true;

            switch (towerMode.get().toLowerCase()) {
                case "vanilla":
                    if (MovementUtil.isMoving()) {
                        mc.thePlayer.motionY = 0.5;
                    } else {
                        mc.thePlayer.motionY = 0.8;
                        MovementUtil.stopMovingQuickly();
                    }
                    break;

                case "hypixel":
                    if (MovementUtil.isMoving()) {
                        hypGroundCheck = true;

                        switch (towerTicks) {
                            case 1:
                                MovementUtil.strafe(0.2f);
                                mc.thePlayer.motionY = 0.39F;
                                break;

                            case 3:
                                mc.thePlayer.motionY -= 0.1309F;
                                break;

                            case 4:
                                mc.thePlayer.motionY -= 0.20F;
                                hypGroundCheck = false;
                                break;
                        }
                    }
                    break;
            }

            towered = true;
        } else if (towered) {
            if (towerMode.get().equals("Vanilla")) {
                MovementUtil.stopYMotion();
            }

            MovementUtil.mcJumpNoBoost = false;
            hypGroundCheck = false;
            towered = false;
        }
    }

    private void tellyFunction() {
        if (mode.get().equals("Telly") && !mc.thePlayer.isPotionActive(Potion.moveSpeed) && !towered && !Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode()) && Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode()) && !MovementUtil.isBlockAbovePlayer(mc.thePlayer, 1)) {
            if (jumpTicks <= 3) {
                if (jumpTicks == 0 || jumpTicks == 1 || jumpTicks == 2) {
                    DewCommon.rotationManager.rotateToward((float) MovementUtil.getDirection(), 80f, 180f, true);
                } else {
                    if (hypixelTellyBanFix.get()) {
                        DewCommon.rotationManager.faceBlockHypixelSafe(60f, false);
                    } else {
                        DewCommon.rotationManager.rotateToward((float) (MovementUtil.getDirection() + 180f), 80f, tellyPreRotationSpeed.get().floatValue(), true);
                    }
                }

                if (mc.thePlayer.posY > 0.0D && (mc.thePlayer.isSprinting() || noSprint.get()) && jumpTicks == 0) {
                    mc.thePlayer.jump();
                    this.updateKeepY();
                }
            }
        }
    }

    private boolean isTelly() {
        return mode.get().equals("Telly") && !mc.thePlayer.isPotionActive(Potion.moveSpeed) && !towered && !Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode()) && Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode()) && !MovementUtil.isBlockAbovePlayer(mc.thePlayer, 1) && jumpTicks <= 3;
    }

    public boolean isNearEdge() {
        double x = mc.thePlayer.posX;
        double z = mc.thePlayer.posZ;
        int y = (int) Math.floor(mc.thePlayer.posY) - 1;

        double expand = mc.thePlayer.isPotionActive(Potion.moveSpeed) ? 0.26 : 0.15;
        for (double dx = -expand; dx <= expand; dx += expand) {
            for (double dz = -expand; dz <= expand; dz += expand) {
                if (dx == 0 && dz == 0) continue;

                BlockPos pos = new BlockPos(x + dx, y, z + dz);
                if (!mc.theWorld.getBlockState(pos).getBlock().isFullBlock()) {
                    return true;
                }
            }
        }
        return false;
    }

    private EnumFacing facingFromRotation(float yaw, float pitch) {
        if (pitch >= 80.0F) return EnumFacing.DOWN;
        if (pitch <= -80.0F) return EnumFacing.UP;

        int direction = MathHelper.floor_float((yaw / 90.0F) + 0.5F) & 3;

        switch (direction) {
            case 0:
                return EnumFacing.SOUTH;
            case 1:
                return EnumFacing.WEST;
            case 3:
                return EnumFacing.EAST;
            case 2:
            default:
                return EnumFacing.NORTH;
        }
    }

    private EnumFacing[] getFullPrioritizedFacings() {
        return new EnumFacing[]{EnumFacing.DOWN, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.EAST, EnumFacing.WEST, EnumFacing.UP};
    }

    private PlaceResult tryPlaceBlock(BlockPos pos) {
        if (!holdingBlock) return PlaceResult.FAIL_OTHER;

        String modeValue = mode.get();
        float rotationSpeedVal = modeValue.equals("Telly") && hypixelTellyBanFix.get() ? 30f : rotationSpeed.get().floatValue();

        EnumFacing preferredFacing = facingFromRotation(
                mc.thePlayer.rotationYaw,
                mc.thePlayer.rotationPitch
        );

        EnumFacing[] orderedFacings = new EnumFacing[6];
        orderedFacings[0] = preferredFacing;
        int idx = 1;
        for (EnumFacing facing : facingsArray) {
            if (facing != preferredFacing) orderedFacings[idx++] = facing;
        }

        for (EnumFacing facing : orderedFacings) {
            BlockPos neighbor = pos.offset(facing);
            IBlockState state = mc.theWorld.getBlockState(neighbor);
            Block block = state.getBlock();

            if (!block.canCollideCheck(state, false)) continue;

            EnumFacing opposite = facing.getOpposite();

            double hitX = neighbor.getX() + 0.5 + 0.5 * opposite.getFrontOffsetX();
            double hitY = neighbor.getY() + 0.5 + 0.5 * opposite.getFrontOffsetY();
            double hitZ = neighbor.getZ() + 0.5 + 0.5 * opposite.getFrontOffsetZ();
            Vec3 hitVec = new Vec3(hitX, hitY, hitZ);

            if (modeValue.equals("Normal") || modeValue.equals("Telly")) {
                boolean canPlace = DewCommon.rotationManager.faceBlockWithFacing(neighbor, opposite, rotationSpeedVal, !mode.get().equals("Telly") || !hypixelTellyBanFix.get());
                if (!canPlace) return PlaceResult.FAIL_ROTATION;
            }

            ItemStack itemstack = mc.thePlayer.inventory.getCurrentItem();
            if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, itemstack, neighbor, opposite, hitVec)) {
                mc.thePlayer.swingItem();
                if (itemstack != null) {
                    if (itemstack.stackSize == 0) {
                        mc.thePlayer.inventory.mainInventory[mc.thePlayer.inventory.currentItem] = null;
                    } else if (mc.playerController.isInCreativeMode()) {
                        mc.entityRenderer.itemRenderer.resetEquippedProgress();
                    }
                }
                delay = 0;
                return PlaceResult.SUCCESS;
            }
        }

        return PlaceResult.FAIL_OTHER;
    }

    private int getValidBlockSlot() {
        if (!preferHighestStack.get()) {
            for (int i = 0; i < 9; i++) {
                ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
                if (this.isInvalidBlock(stack)) continue;
                return i;
            }
            return -1;
        } else {
            int bestSlot = -1;
            int maxStackSize = -1;
            for (int i = 0; i < 9; i++) {
                ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
                if (this.isInvalidBlock(stack)) continue;
                if (stack.stackSize > maxStackSize) {
                    maxStackSize = stack.stackSize;
                    bestSlot = i;
                }
            }
            return bestSlot;
        }
    }

    public boolean isInvalidBlock(ItemStack stack) {
        if (stack == null || !(stack.getItem() instanceof ItemBlock) || stack.stackSize == 0) {
            return true;
        }

        Block block = ((ItemBlock) stack.getItem()).getBlock();

        if (block instanceof BlockGlass || block instanceof BlockStainedGlass || block instanceof BlockIce || block instanceof BlockPackedIce) {
            return false;
        }

        return !block.isFullBlock() || block instanceof BlockChest || block instanceof BlockEnderChest || block instanceof BlockWorkbench || block instanceof BlockFurnace || block instanceof BlockAnvil || block instanceof BlockFenceGate || block instanceof BlockTrapDoor || block instanceof BlockDoor || block.getMaterial().isReplaceable() || block instanceof BlockFalling || block instanceof BlockSlab || block instanceof BlockStairs || block instanceof BlockPane || block instanceof BlockWall || block instanceof BlockSign || block instanceof BlockButton || block instanceof BlockPressurePlate;
    }

    private enum PlaceResult {
        SUCCESS, FAIL_ROTATION, FAIL_OTHER
    }
}