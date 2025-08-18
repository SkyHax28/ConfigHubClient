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
import com.dew.utils.PacketUtil;
import com.dew.utils.RenderUtil;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.settings.GameSettings;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.potion.Potion;
import net.minecraft.util.*;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.util.Comparator;
import java.util.HashSet;
import java.util.PriorityQueue;
import java.util.Set;

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
    public static final BooleanValue autoTellyEdgeSneak = new BooleanValue("Auto Telly Edge Sneak", false);
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
    private boolean doTellyInThisJump = true;
    private BlockPos lastPlacedPos = null;

    public Scaffold() {
        super("Scaffold", ModuleCategory.PLAYER, Keyboard.KEY_NONE, false, true, true);
    }

    private boolean canTower() {
        return Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode()) && MovementUtil.isBlockUnderPlayer(mc.thePlayer, 3, 2, false) && !MovementUtil.isBlockAbovePlayer(mc.thePlayer, 1, 0.3) && DewCommon.moduleManager.getModule(Hud.class).getTotalValidBlocksInHotbar() > 0;
    }

    private boolean shouldUpdateKeepYState() {
        return keepY == -1 || mc.thePlayer.hurtTime != 0 || mc.thePlayer.motionY <= -0.55 || !mode.get().equals("Telly") && (!DewCommon.moduleManager.getModule(SpeedModule.class).isEnabled() || Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode())) || mode.get().equals("Telly") && !doTellyInThisJump;
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
        } else {
            this.resetState();
        }
    }

    private void resetState() {
        if (originalSlot != -1 && mc.thePlayer != null) {
            mc.thePlayer.inventory.currentItem = originalSlot;
            mc.playerController.updateController();
        }
        doTellyInThisJump = true;
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
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        jumpTicks = mc.thePlayer.onGround ? 0 : jumpTicks + 1;
        delay++;

        this.doMainFunctions();
        this.tellyFunction();
        this.edgeCheck(false);
    }

    @Override
    public void onPreMotion(PreMotionEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        this.jumpCheck();
        this.edgeCheck(true);
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
        if (mc.thePlayer.onGround) {
            doTellyInThisJump = !Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode()) && !mc.thePlayer.isPotionActive(Potion.jump) && !mc.thePlayer.isPotionActive(Potion.moveSpeed) && !Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode()) && Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode()) && !MovementUtil.isDiagonal(6f) && !MovementUtil.isBlockAbovePlayer(mc.thePlayer, 1, 0.3);
        }

        boolean antiTelly = mode.get().equals("Telly") && !doTellyInThisJump && !Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode()) && MovementUtil.isDiagonal(6f);

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
                if (!this.shouldTellyDoNotPlaceBlocks() && !antiTelly && (DewCommon.rotationManager.isReturning() || !holdingBlock)) {
                    DewCommon.rotationManager.rotateToward((float) (MovementUtil.getDirection() - 180f), 83f, hypixelTellyBanFix.get() ? 30f : rotationSpeed.get().floatValue(), true);
                }
                break;

            default:
                break;
        }

        if (noSprint.get() || DewCommon.moduleManager.getModule(MoveFix.class).isEnabled() && Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode()) || mode.get().equals("Telly") && antiTelly) {
            mc.thePlayer.setSprinting(false);
        }

        if (shouldUpdateKeepYState() && (!antiTelly || keepY == -1)) {
            this.updateKeepY();
        }

        if (antiTelly) {
            if (jumpTicks <= 3) {
                if (jumpTicks <= 1) {
                    DewCommon.rotationManager.rotateToward((float) MovementUtil.getDirection(), 80f, 180f, true);
                    if (mc.thePlayer.posY > 0.0D && mc.thePlayer.onGround && !mc.thePlayer.isSprinting()) {
                        mc.thePlayer.jump();
                    }
                } else if (jumpTicks == 3) {
                    if (hypixelTellyBanFix.get()) {
                        DewCommon.rotationManager.rotateToward((float) (MovementUtil.getDirection() + 180f), 80f, 60f, true);
                    } else {
                        DewCommon.rotationManager.rotateToward((float) (MovementUtil.getDirection() + 180f), 80f, tellyPreRotationSpeed.get().floatValue(), true);
                    }
                }
                return;
            }
        }

        if (this.shouldTellyDoNotPlaceBlocks()) {
            return;
        }

        if (delay <= placeDelay.get().intValue()) return;

        if (holdingBlock && (mc.thePlayer.getHeldItem() == null || !(mc.thePlayer.getHeldItem().getItem() instanceof ItemBlock) || mc.thePlayer.getHeldItem().stackSize == 0)) {
            this.resetState();
            return;
        }

        if (keepY == -1) return;

        BlockPos below = new BlockPos(mc.thePlayer.posX, keepY - 1, mc.thePlayer.posZ);

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

        this.search(below);
    }

    private void search(BlockPos below) {
        if (!mc.theWorld.getBlockState(below).getBlock().isReplaceable(mc.theWorld, below)) return;

        PlaceResult result = tryPlaceBlock(below);
        if (result == PlaceResult.SUCCESS) {
            lastPlacedPos = below;
            holdingBlock = true;
            return;
        } else if (result == PlaceResult.FAIL_ROTATION) {
            return;
        }

        for (EnumFacing dir : EnumFacing.values()) {
            BlockPos neighbor = below.offset(dir);
            PlaceResult neighborResult = tryPlaceBlock(neighbor);

            if (neighborResult == PlaceResult.FAIL_ROTATION) return;

            if (neighborResult == PlaceResult.SUCCESS) {
                lastPlacedPos = neighbor;
                holdingBlock = true;
                return;
            }
        }

        int range = clutchRange.get().intValue();
        if (range > 1) {
            double maxDistanceSq = range * range;

            PriorityQueue<Node> openSet = new PriorityQueue<>(Comparator.comparingDouble(n -> n.fCost));
            Set<BlockPos> closedSet = new HashSet<>();

            openSet.add(new Node(below, 0, heuristic(below)));

            while (!openSet.isEmpty()) {
                Node current = openSet.poll();

                if (!closedSet.add(current.pos)) continue;

                if (mc.thePlayer.getDistanceSqToCenter(current.pos) > maxDistanceSq) continue;

                if (canPlaceAt(current.pos)) {
                    PlaceResult placeRes = tryPlaceBlock(current.pos);

                    if (placeRes == PlaceResult.FAIL_ROTATION) return;

                    if (placeRes == PlaceResult.SUCCESS) {
                        lastPlacedPos = current.pos;
                        holdingBlock = true;
                        return;
                    }
                }

                for (EnumFacing dir : EnumFacing.values()) {
                    BlockPos next = current.pos.offset(dir);

                    if (!closedSet.contains(next) && isAirLike(next)) {
                        double gCost = current.gCost + 1;
                        double hCost = heuristic(next);
                        openSet.add(new Node(next, gCost, hCost));
                    }
                }
            }
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
        if (this.shouldTellyDoNotPlaceBlocks()) {
            if (jumpTicks <= 1) {
                DewCommon.rotationManager.rotateToward((float) MovementUtil.getDirection(), 80f, 180f, true);
            } else if (jumpTicks == 3) {
                if (hypixelTellyBanFix.get()) {
                    DewCommon.rotationManager.faceBlockHypixelSafe(60f, false);
                } else {
                    DewCommon.rotationManager.rotateToward((float) (MovementUtil.getDirection() + 180f), 80f, tellyPreRotationSpeed.get().floatValue(), true);
                }
            }

            if (mc.thePlayer.onGround && isNearEdge() && !mc.thePlayer.isSprinting()) {
                keepY = (int) mc.thePlayer.posY + 1;
                mc.thePlayer.setSprinting(true);
                mc.thePlayer.jump();
            } else if (mc.thePlayer.posY > 0.0D && mc.thePlayer.onGround && (mc.thePlayer.isSprinting() || noSprint.get())) {
                mc.thePlayer.jump();
            }
        }
    }

    private void edgeCheck(boolean allowUnSneak) {
        if (edgeSafeMode.get().equals("Sneak") && this.isNearEdge() || this.shouldTellyAntiEdge()) {
            mc.gameSettings.keyBindSneak.setKeyDown(true);
            isSneaking = true;
        } else if (isSneaking && allowUnSneak) {
            mc.gameSettings.keyBindSneak.setKeyDown(false);
            isSneaking = false;
        }
    }

    private void jumpCheck() {
        if (!mc.thePlayer.onGround && !jumped) {
            jumped = true;
        }
        towerTicks = mc.thePlayer.onGround ? 0 : towerTicks + 1;
        this.towerFunction();
    }

    @Override
    public void onMove(MoveEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        this.edgeCheck(false);

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
    public void onLivingUpdate(LivingUpdateEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        this.edgeCheck(false);
    }

    @Override
    public void onTick(TickEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        this.edgeCheck(false);
    }

    @Override
    public void onPostUpdate(PostUpdateEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        this.edgeCheck(false);
    }

    @Override
    public void onPostMotion(PostMotionEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        this.edgeCheck(false);
    }

    private void updateKeepY() {
        if (keepY == -1 && (mc.thePlayer.hurtTime != 0 || mc.thePlayer.motionY <= -0.55)) {
            keepY = (int) mc.thePlayer.posY - 1;
        } else {
            keepY = (int) mc.thePlayer.posY;
        }
    }

    private boolean shouldTellyAntiEdge() {
        if (!autoTellyEdgeSneak.get()) return false;
        boolean hasAngleDiff = Math.abs(MovementUtil.getAngleDifference((float) MovementUtil.getDirection(), DewCommon.rotationManager.getClientYaw())) > 0.7F;
        return mode.get().equals("Telly") && (jumpTicks >= 7 || this.isNearEdge() && (Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode()) || !this.shouldTellyDoNotPlaceBlocks() && Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode()) || !MovementUtil.isDiagonal(12f) && mc.thePlayer.onGround && hasAngleDiff));
    }

    private boolean canPlaceAt(BlockPos pos) {
        if (!isAirLike(pos)) return false;

        AxisAlignedBB bb = new AxisAlignedBB(
                pos.getX(), pos.getY(), pos.getZ(),
                pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1
        );

        if (mc.thePlayer.getEntityBoundingBox().intersectsWith(bb)) return false;

        for (EnumFacing dir : EnumFacing.values()) {
            BlockPos support = pos.offset(dir);
            if (!isAirLike(support)) {
                return true;
            }
        }
        return false;
    }

    private boolean isAirLike(BlockPos pos) {
        Block block = mc.theWorld.getBlockState(pos).getBlock();

        if (block instanceof BlockAir) return true;
        if (block.isReplaceable(mc.theWorld, pos)) return true;

        Material mat = block.getMaterial();
        if (mat == Material.plants || mat == Material.vine || mat == Material.snow) return true;

        return block instanceof BlockSlab && !block.isFullCube() || block instanceof BlockFenceGate || block instanceof BlockLadder || block instanceof BlockWall;
    }

    private double heuristic(BlockPos pos) {
        return mc.thePlayer.getDistanceSqToCenter(pos);
    }

    private static class Node {
        final BlockPos pos;
        final double gCost;
        final double hCost;
        final double fCost;

        Node(BlockPos pos, double gCost, double hCost) {
            this.pos = pos;
            this.gCost = gCost;
            this.hCost = hCost;
            this.fCost = gCost + hCost;
        }
    }

    private boolean shouldTellyDoNotPlaceBlocks() {
        if (mode.get().equals("Telly")) {
            if (!doTellyInThisJump) return false;
            if (mc.thePlayer.hurtTime != 0 || mc.thePlayer.motionY <= -0.55) return false;
            if (towered) return true;
            return jumpTicks <= 3;
        }

        return false;
    }

    public boolean isNearEdge() {
        double px = mc.thePlayer.posX;
        double py = mc.thePlayer.posY;
        double pz = mc.thePlayer.posZ;

        int baseY = (int) Math.floor(py) - 1;

        double radius = 0.25;
        double step = Math.PI / 36;

        if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
            radius += 0.05;
        }

        for (double angle = 0; angle < Math.PI * 2; angle += step) {
            double checkX = px + Math.cos(angle) * radius;
            double checkZ = pz + Math.sin(angle) * radius;

            BlockPos pos = new BlockPos(checkX, baseY, checkZ);
            Block block = mc.theWorld.getBlockState(pos).getBlock();

            if (block.isReplaceable(mc.theWorld, pos) || !block.getMaterial().isSolid() || !block.isFullCube()) {
                return true;
            }
        }

        return false;
    }

    private static final EnumFacing[] FACINGS = EnumFacing.values();
    private static final float[][] FACING_VECTORS = new float[FACINGS.length][3];
    static {
        for (int i = 0; i < FACINGS.length; i++) {
            EnumFacing f = FACINGS[i];
            FACING_VECTORS[i][0] = f.getFrontOffsetX();
            FACING_VECTORS[i][1] = f.getFrontOffsetY();
            FACING_VECTORS[i][2] = f.getFrontOffsetZ();
        }
    }

    private EnumFacing facingFromRotation(float yaw, float pitch) {
        float cosPitch = MathHelper.cos(-pitch * 0.017453292F);
        float sinPitch = MathHelper.sin(-pitch * 0.017453292F);
        float cosYaw = MathHelper.cos(-yaw * 0.017453292F - (float)Math.PI);
        float sinYaw = MathHelper.sin(-yaw * 0.017453292F - (float)Math.PI);

        float lookX = sinYaw * cosPitch;
        float lookZ = cosYaw * cosPitch;

        float bestDot = -Float.MAX_VALUE;
        EnumFacing bestFacing = EnumFacing.NORTH;
        for (int i = 0; i < FACING_VECTORS.length; i++) {
            float dot = lookX * FACING_VECTORS[i][0] + sinPitch * FACING_VECTORS[i][1] + lookZ * FACING_VECTORS[i][2];
            if (dot > bestDot) {
                bestDot = dot;
                bestFacing = FACINGS[i];
            }
        }
        return bestFacing;
    }

    private PlaceResult tryPlaceBlock(BlockPos pos) {
        if (!holdingBlock) return PlaceResult.FAIL_OTHER;

        String modeValue = mode.get();
        float rotationSpeedVal = modeValue.equals("Telly") && hypixelTellyBanFix.get() ? 30f : rotationSpeed.get().floatValue();

        EnumFacing preferredFacing = facingFromRotation(
                mc.thePlayer.rotationYaw,
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
                boolean canPlace = DewCommon.rotationManager.faceBlockWithFacing(neighbor, opposite, rotationSpeedVal, true);
                if (!canPlace) return PlaceResult.FAIL_ROTATION;
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