package com.dew.system.module.modules.player;

import com.dew.DewCommon;
import com.dew.system.event.events.*;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.module.modules.exploit.SafetySwitchv2000;
import com.dew.system.module.modules.movement.speed.SpeedModule;
import com.dew.system.module.modules.render.Hud;
import com.dew.system.settingsvalue.BooleanValue;
import com.dew.system.settingsvalue.NumberValue;
import com.dew.system.settingsvalue.SelectionValue;
import com.dew.utils.MovementUtil;
import com.dew.utils.PacketUtil;
import com.dew.utils.RenderUtil;
import net.minecraft.block.*;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.potion.Potion;
import net.minecraft.util.*;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.util.Comparator;
import java.util.PriorityQueue;

public class Scaffold extends Module {

    private static final SelectionValue mode = new SelectionValue("Mode", "Normal", "Normal", "Telly", "Hypixel");
    public static final BooleanValue hypixelTellyBanFix = new BooleanValue("Hypixel Telly Ban Fix", false, () -> mode.get().equals("Telly"));
    private static final SelectionValue towerMode = new SelectionValue("Tower Mode", "OFF", "OFF", "Vanilla", "Hypixel");
    private static final NumberValue rotationSpeed = new NumberValue("Rotation Speed", 60.0, 0.0, 180.0, 5.0, () -> mode.get().equals("Normal") || mode.get().equals("Telly") && !hypixelTellyBanFix.get());
    private static final NumberValue tellyPreRotationSpeed = new NumberValue("Telly Pre Rotation Speed", 35.0, 0.0, 180.0, 5.0, () -> mode.get().equals("Telly") && !hypixelTellyBanFix.get());
    private static final BooleanValue tellyDiagonalDegrees = new BooleanValue("Telly Diagonal Degrees", false, () -> mode.get().equals("Telly"));
    private static final NumberValue clutchRange = new NumberValue("Clutch Range", 3.0, 1.0, 5.0, 1.0);
    private static final NumberValue placeDelay = new NumberValue("Place Delay", 0.0, 0.0, 3.0, 1.0, () -> mode.get().equals("Normal") || mode.get().equals("Telly"));
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
        return keepY == -1 || !DewCommon.moduleManager.getModule(SpeedModule.class).isEnabled() && !mode.get().equals("Telly") || Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode());
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
        if (mc.thePlayer != null) {
            jumpTicks = mc.thePlayer.onGround ? 0 : jumpTicks + 1;
        }
        delay++;

        this.doMainFunctions();
    }

    @Override
    public void onPreMotion(PreMotionEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        if (!mc.thePlayer.onGround && !jumped) {
            jumped = true;
        }

        towerTicks = mc.thePlayer.onGround ? 0 : towerTicks + 1;

        this.towerFunction();
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

    private void doMainFunctions() {
        if (mode.get().equals("Telly") && mc.thePlayer.onGround && keepY == -1 && !mc.thePlayer.isPotionActive(Potion.jump)) {
            mc.thePlayer.setSprinting(false);
            if (mc.thePlayer.posY > 0.0D) {
                mc.thePlayer.jump();
            }
            this.updateKeepY(1);
            return;
        }

        if (mode.get().equals("Telly") && !mc.thePlayer.isPotionActive(Potion.moveSpeed) && !mc.thePlayer.isPotionActive(Potion.jump) && !towered && !Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode()) && Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode()) && !MovementUtil.isBlockAbovePlayer(mc.thePlayer, 1)) {
            if (jumpTicks <= 3) {
                if (jumpTicks == 0 || jumpTicks == 1 || jumpTicks == 2) {
                    float yaw = tellyDiagonalDegrees.get() ? DewCommon.rotationManager.tellySwap((float) MovementUtil.getDirection()) : (float) MovementUtil.getDirection();
                    DewCommon.rotationManager.rotateToward(yaw, 80f, 180f);
                }

                if (mc.thePlayer.posY > 0.0D && (mc.thePlayer.isSprinting() || noSprint.get()) && jumpTicks == 0 || this.isTellyEdge() && mc.thePlayer.onGround) {
                    mc.thePlayer.jump();
                }

                if (jumpTicks == 0) {
                    this.updateKeepY(0);
                    return;
                }
            }
        }

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
                    DewCommon.rotationManager.rotateToward((float) (MovementUtil.getDirection() - 180f), 83f, rotationSpeed.get().floatValue());
                }
                break;

            case "hypixel":
                if (mc.thePlayer.ticksExisted % 2 == 0) {
                    DewCommon.rotationManager.faceBlockHypixelSafe(180f, true);
                }
                break;

            case "telly":
                if ((mc.thePlayer.isPotionActive(Potion.moveSpeed) || mc.thePlayer.isPotionActive(Potion.jump)) && (DewCommon.rotationManager.isReturning() || !holdingBlock)) {
                    DewCommon.rotationManager.rotateToward((float) (MovementUtil.getDirection() - 180f), 83f, hypixelTellyBanFix.get() ? 60f : tellyPreRotationSpeed.get().floatValue());
                }
                break;

            default:
                break;
        }

        if (noSprint.get() || mode.get().equals("Telly") && (Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode()) || !Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode()) || mc.thePlayer.isPotionActive(Potion.moveSpeed) || mc.thePlayer.isPotionActive(Potion.jump) || MovementUtil.isBlockAbovePlayer(mc.thePlayer, 1))) {
            mc.thePlayer.setSprinting(false);
        }

        BlockPos below = new BlockPos(mc.thePlayer.posX, keepY - 1.0, mc.thePlayer.posZ);

        if (this.isTelly() && jumpTicks != 0) {
            this.searchAndPlaceBlockAndRotation(below, tellyPreRotationSpeed.get().floatValue(), false);
            return;
        }

        if (shouldUpdateKeepYState()) {
            this.updateKeepY(0);
        }

        if (delay <= placeDelay.get().intValue()) return;

        if (holdingBlock && (mc.thePlayer.inventory.getCurrentItem() == null || !(mc.thePlayer.inventory.getCurrentItem().getItem() instanceof ItemBlock) || mc.thePlayer.inventory.getCurrentItem().stackSize == 0)) {
            this.resetState();
            return;
        }

        if (keepY == -1) return;

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

        String modeValue = mode.get();

        float rotSpeed;
        boolean isTelly = modeValue.equals("Telly");
        boolean isJumping = Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode());
        boolean isNotMovingForward = !Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode());
        boolean hasSpeedPotion = mc.thePlayer.isPotionActive(Potion.moveSpeed);
        boolean hasJumpPotion = mc.thePlayer.isPotionActive(Potion.jump);
        boolean blockAbove = MovementUtil.isBlockAbovePlayer(mc.thePlayer, 1);
        boolean banFixEnabled = hypixelTellyBanFix.get();

        boolean conditionA = isTelly && (isJumping || isNotMovingForward || hasSpeedPotion || hasJumpPotion || blockAbove);

        if (conditionA && banFixEnabled) {
            rotSpeed = 60f;
        } else if (conditionA) {
            rotSpeed = tellyPreRotationSpeed.get().floatValue();
        } else if (isTelly && banFixEnabled) {
            rotSpeed = 30f;
        } else {
            rotSpeed = rotationSpeed.get().floatValue();
        }

        this.searchAndPlaceBlockAndRotation(below, rotSpeed, true);
    }

    private void searchAndPlaceBlockAndRotation(BlockPos below, float rotSpeed, boolean doPlace) {
        Boolean placed = null;

        PlaceResult result = tryPlaceBlockAndRotation(below, rotSpeed, doPlace);

        if (result == PlaceResult.SUCCESS) {
            lastPlacedPos = below;
            placed = true;
        } else if (result == PlaceResult.FAIL_ROTATION) {
            return;
        } else {
            for (EnumFacing dir : EnumFacing.values()) {
                BlockPos neighbor = below.offset(dir);
                PlaceResult neighborResult = tryPlaceBlockAndRotation(neighbor, rotSpeed, doPlace);

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

                    IBlockState state = mc.theWorld.getBlockState(target);
                    Block block = state.getBlock();
                    if (!block.isReplaceable(mc.theWorld, target)) continue;

                    AxisAlignedBB targetBB = new AxisAlignedBB(
                            target.getX(), target.getY(), target.getZ(),
                            target.getX() + 1, target.getY() + 1, target.getZ() + 1
                    );

                    if (mc.thePlayer.getEntityBoundingBox().intersectsWith(targetBB)) continue;

                    boolean hasSupport = false;
                    for (EnumFacing dir : EnumFacing.values()) {
                        if (dir == EnumFacing.DOWN) continue;
                        BlockPos support = target.offset(dir);
                        if (!mc.theWorld.getBlockState(support).getBlock().isReplaceable(mc.theWorld, support)) {
                            hasSupport = true;
                            break;
                        }
                    }

                    if (hasSupport) {
                        searchQueue.add(target);
                    }
                }
            }

            while (!searchQueue.isEmpty()) {
                BlockPos target = searchQueue.poll();
                PlaceResult clutchResult = tryPlaceBlockAndRotation(target, rotSpeed, doPlace);

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

    private void updateKeepY(int distance) {
        keepY = (int) mc.thePlayer.posY + distance;
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

    private boolean isTelly() {
        return mode.get().equals("Telly") && !mc.thePlayer.isPotionActive(Potion.moveSpeed) && !mc.thePlayer.isPotionActive(Potion.jump) && !towered && !Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode()) && Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode()) && !MovementUtil.isBlockAbovePlayer(mc.thePlayer, 1) && jumpTicks <= 3;
    }

    public boolean isTellyEdge() {
        double x = mc.thePlayer.posX;
        double z = mc.thePlayer.posZ;
        int y = keepY - 1;

        double expand = 0.2;
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

    private PlaceResult tryPlaceBlockAndRotation(BlockPos pos, float rotSpeed, boolean doPlace) {
        if (!holdingBlock) return PlaceResult.FAIL_OTHER;

        String modeValue = mode.get();
        EnumFacing preferredFacing = facingFromRotation(
                DewCommon.rotationManager.getClientYaw(),
                DewCommon.rotationManager.getClientPitch()
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
                boolean canPlace = DewCommon.rotationManager.faceBlockWithFacing(neighbor, opposite, rotSpeed);
                if (!canPlace) return PlaceResult.FAIL_ROTATION;
            }

            if (!doPlace) {
                return PlaceResult.FAIL_ROTATION;
            }

            if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.inventory.getCurrentItem(), neighbor, opposite, hitVec)) {
                PacketUtil.sendPacket(new C0APacketAnimation());
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
