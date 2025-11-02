package com.dew.system.module.modules.player;

import com.dew.DewCommon;
import com.dew.system.event.EventPriority;
import com.dew.system.event.events.*;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.module.modules.exploit.SafetySwitchv2000;
import com.dew.system.module.modules.movement.MoveFix;
import com.dew.system.module.modules.movement.speed.SpeedModule;
import com.dew.system.module.modules.visual.Hud;
import com.dew.system.settingsvalue.BooleanValue;
import com.dew.system.settingsvalue.NumberValue;
import com.dew.system.settingsvalue.SelectionValue;
import com.dew.utils.MovementUtil;
import com.dew.utils.PacketUtil;
import com.dew.utils.RenderUtil;
import net.minecraft.block.*;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.potion.Potion;
import net.minecraft.util.*;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.util.*;

public class Scaffold extends Module {

    private static final SelectionValue mode = new SelectionValue("Mode", "Normal", "Normal", "Telly", "Hypixel");
    private static final SelectionValue swingMode = new SelectionValue("Swing Mode", "Packet", "Normal", "Packet");
    private static final SelectionValue rotationMode = new SelectionValue("Rotation Mode", "Normal", () -> mode.get().equals("Normal") || mode.get().equals("Telly"), "OFF", "Normal", "Snap");
    private static final BooleanValue simpleRotator = new BooleanValue("Simple Rotator", false, () -> mode.get().equals("Normal") || mode.get().equals("Telly"));
    private static final NumberValue antiPlaceUntil = new NumberValue("Anti Place Until", 3.0, 1.0, 6.0, 1.0, () -> mode.get().equals("Telly"));
    private static final NumberValue tellyOneJumpRotation = new NumberValue("Telly 1 Jump Rotation", 0.0, 0.0, 180.0, 5.0, () -> mode.get().equals("Telly"));
    private static final NumberValue tellyTwoJumpRotation = new NumberValue("Telly 2 Jump Rotation", 10.0, 0.0, 180.0, 5.0, () -> mode.get().equals("Telly"));
    private static final NumberValue tellyThreeJumpRotation = new NumberValue("Telly 3 Jump Rotation", 35.0, 0.0, 180.0, 5.0, () -> mode.get().equals("Telly"));
    private static final NumberValue rotationSpeed = new NumberValue("Rotation Speed", 60.0, 0.0, 180.0, 5.0, () -> mode.get().equals("Normal") || mode.get().equals("Telly"));
    private static final NumberValue clutchRange = new NumberValue("Clutch Range", 3.0, 1.0, 6.0, 1.0);
    private static final NumberValue expandRange = new NumberValue("Expand Range", 0.0, 0.0, 5.0, 1.0);
    private static final NumberValue placeDelay = new NumberValue("Place Delay", 0.0, 0.0, 3.0, 1.0, () -> mode.get().equals("Normal") || mode.get().equals("Telly"));
    private static final SelectionValue towerMode = new SelectionValue("Tower Mode", "OFF", "OFF", "Vanilla", "Hypixel");
    private static final SelectionValue onlyTowerWhen = new SelectionValue("Only Tower When", "Always", () -> towerMode.get().equals("Vanilla"), "Always", "Standing", "Moving");
    private static final SelectionValue edgeSafeMode = new SelectionValue("Edge Safe Mode", "OFF", () -> mode.get().equals("Normal") || mode.get().equals("Hypixel"), "OFF", "Safewalk", "Ground Safewalk", "Sneak");
    private static final BooleanValue noRotationHitCheck = new BooleanValue("No Rotation Hit Check", false, () -> !rotationMode.get().equals("OFF"));
    private static final BooleanValue preferHighestStack = new BooleanValue("Prefer Highest Stack", true);
    private static final BooleanValue noSprint = new BooleanValue("No Sprint", false);
    private static final BooleanValue autoPlaceHackPlacer = new BooleanValue("Auto Place Hack Placer", false);
    private static final BooleanValue renderBlock = new BooleanValue("Render Block", false);
    private static final BooleanValue keepYJump = new BooleanValue("Keep Y Jump", false, () -> mode.get().equals("Normal"));
    private static final BooleanValue diagonalSafer = new BooleanValue("Diagonal Safer", false, () -> mode.get().equals("Telly"));
    private static final BooleanValue andromeda = new BooleanValue("Andromeda", false, () -> mode.get().equals("Normal"));
    private static final BooleanValue antiBackSprint = new BooleanValue("Anti Back Sprint", false, () -> (mode.get().equals("Normal") || mode.get().equals("Telly")) && simpleRotator.get());
    private BlockData blockData;
    private final List<Block> invalid = Arrays.asList(Blocks.anvil, Blocks.air, Blocks.water, Blocks.fire, Blocks.flowing_water, Blocks.lava, Blocks.flowing_lava, Blocks.chest, Blocks.anvil, Blocks.enchanting_table, Blocks.chest, Blocks.ender_chest, Blocks.gravel);
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
    private boolean needSnapRotationReset = false;
    private boolean andromed = false;
    public Scaffold() {
        super("Scaffold", ModuleCategory.PLAYER, Keyboard.KEY_NONE, false, true, true);
    }

    public boolean isNoSprint() {
        return noSprint.get();
    }

    public int getOriginalSlot() {
        return originalSlot;
    }

    private boolean canTower() {
        return (onlyTowerWhen.get().equals("Always") || onlyTowerWhen.get().equals("Standing") && !MovementUtil.isMoving() || onlyTowerWhen.get().equals("Moving") && MovementUtil.isMoving()) && Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode()) && MovementUtil.isBlockUnderPlayer(mc.thePlayer, 2, 1, false) && !MovementUtil.isBlockAbovePlayer(mc.thePlayer, 1, 0.3) && DewCommon.moduleManager.getModule(Hud.class).getTotalValidBlocksInHotbar() > 0;
    }

    private boolean shouldUpdateKeepYState() {
        return keepY == -1 || mc.thePlayer.onGround || mc.thePlayer.hurtTime != 0 || mc.thePlayer.motionY <= -0.55 || !mode.get().equals("Telly") && (!DewCommon.moduleManager.getModule(SpeedModule.class).isEnabled() && !keepYJump.get() || mc.thePlayer.onGround || Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode())) || mode.get().equals("Telly") && !doTellyInThisJump;
    }

    @Override
    public EventPriority getPriority() {
        return EventPriority.HIGHEST;
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
    public void onLoadWorld(LoadWorldEvent event) {
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
        needSnapRotationReset = false;
        andromed = false;
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
    public void onLivingUpdate(LivingUpdateEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        this.doMainFunctions(true);
    }

    @Override
    public void onPreUpdate(PreUpdateEvent event) {
        if (!autoPlaceHackPlacer.get()) {
            this.doMainFunctions(false);
        }

        if (mc.thePlayer == null || mc.theWorld == null) return;

        jumpTicks = mc.thePlayer.onGround ? 0 : jumpTicks + 1;
        delay++;

        this.tellyFunction();
    }

    @Override
    public void onDrawBlockSelection(DrawBlockSelectionEvent event) {
        if (autoPlaceHackPlacer.get()) {
            this.doMainFunctions(false);
        }
    }

    @Override
    public void onPreMotion(PreMotionEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        this.jumpCheck();
        this.edgeCheck();
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null || lastPlacedPos == null || !renderBlock.get()) return;

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

    private float getTellyRotSpeed() {
        switch (jumpTicks) {
            case 1: return tellyOneJumpRotation.get().floatValue();
            case 2: return tellyTwoJumpRotation.get().floatValue();
            case 3: return tellyThreeJumpRotation.get().floatValue();
        }
        return rotationSpeed.get().floatValue();
    }

    private void doMainFunctions(boolean isLivingUpdateEvent) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        boolean antiTelly = mode.get().equals("Telly") && !doTellyInThisJump && !Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode());

        if (!isLivingUpdateEvent) {
            if (MovementUtil.isBlockAbovePlayer(mc.thePlayer, 1, 0.2)) {
                andromed = false;
            }

            if (needSnapRotationReset) {
                DewCommon.rotationManager.resetRotationsInstantly();
                needSnapRotationReset = false;
            }

            if (mc.thePlayer.onGround) {
                doTellyInThisJump = !Keyboard.isKeyDown(mc.gameSettings.keyBindSneak.getKeyCode()) && !mc.thePlayer.isPotionActive(Potion.jump) && !Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode()) && Keyboard.isKeyDown(mc.gameSettings.keyBindForward.getKeyCode()) && (!MovementUtil.isDiagonal(6f) || !diagonalSafer.get()) && !MovementUtil.isBlockAbovePlayer(mc.thePlayer, 1, 0.3);
            }

            switch (mode.get().toLowerCase()) {
                case "normal":
                    if (!rotationMode.get().equals("OFF") && !rotationMode.get().equals("Snap") && (DewCommon.rotationManager.isReturning() || !holdingBlock)) {
                        DewCommon.rotationManager.rotateToward((float) (MovementUtil.getDirection() - 180f) + (antiBackSprint.get() ? 80f : 0f), 83f, rotationSpeed.get().floatValue(), true);
                    }
                    break;

                case "hypixel":
                    if (mc.thePlayer.ticksExisted % 2 == 0) {
                        DewCommon.rotationManager.faceBlockHypixelSafe(180f, true, !rotationMode.get().equals("OFF"));
                    }
                    break;

                case "telly":
                    if (!rotationMode.get().equals("OFF") && !rotationMode.get().equals("Snap") && (!this.shouldTellyDoNotPlaceBlocks() && !antiTelly && (DewCommon.rotationManager.isReturning() || !holdingBlock))) {
                        DewCommon.rotationManager.rotateToward((float) (MovementUtil.getDirection() - 180f), 83f, rotationSpeed.get().floatValue(), true);
                    }
                    break;

                default:
                    break;
            }
        }

        if (isLivingUpdateEvent) {
            if (noSprint.get() || DewCommon.moduleManager.getModule(MoveFix.class).isEnabled() && Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode()) || mode.get().equals("Telly") && antiTelly) {
                mc.thePlayer.setSprinting(false);
            }
            return;
        }

        if (shouldUpdateKeepYState() && (!antiTelly || keepY == -1)) {
            this.updateKeepY();
        }

        if (mode.get().equals("Normal") && keepYJump.get() && mc.thePlayer.onGround && !Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode())) {
            mc.thePlayer.jump();
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

    private List<BlockPos> bresenhamLine(int x0, int z0, int x1, int z1) {
        List<BlockPos> result = new ArrayList<>();

        int dx = Math.abs(x1 - x0);
        int dz = Math.abs(z1 - z0);

        int sx = x0 < x1 ? 1 : -1;
        int sz = z0 < z1 ? 1 : -1;

        int err = dx - dz;

        int x = x0;
        int z = z0;

        while (true) {
            result.add(new BlockPos(x, 0, z));
            if (x == x1 && z == z1) break;

            int e2 = 2 * err;
            if (e2 > -dz) {
                err -= dz;
                x += sx;
            }
            if (e2 < dx) {
                err += dx;
                z += sz;
            }
        }

        return result;
    }

    private void search(BlockPos below) {
        int playerY = (int) mc.thePlayer.posY - 1;

        int maxExpand = expandRange.get().intValue();
        if (maxExpand > 0 && MovementUtil.isMoving() && !towered) {
            float yaw = (float) MovementUtil.getDirection();
            double dirX = -Math.sin(Math.toRadians(yaw));
            double dirZ =  Math.cos(Math.toRadians(yaw));

            int targetX = (int) Math.round(dirX * maxExpand);
            int targetZ = (int) Math.round(dirZ * maxExpand);

            List<BlockPos> line = bresenhamLine(0, 0, targetX, targetZ);

            for (BlockPos offset : line) {
                BlockPos expandPos = below.add(offset.getX(), 0, offset.getZ());

                if (expandPos.getY() > playerY && !andromed) continue;
                if (!mc.theWorld.getBlockState(expandPos).getBlock().isReplaceable(mc.theWorld, expandPos)) continue;

                PlaceResult expandResult = tryPlaceBlock(expandPos);
                if (expandResult == PlaceResult.SUCCESS) {
                    lastPlacedPos = expandPos;
                    holdingBlock = true;
                    return;
                } else if (expandResult == PlaceResult.FAIL_ROTATION) {
                    return;
                }
            }
        }

        if (!mc.theWorld.getBlockState(below).getBlock().isReplaceable(mc.theWorld, below) || below.getY() > playerY && !andromed) return;

        PlaceResult result = tryPlaceBlock(below);
        if (result == PlaceResult.SUCCESS) {
            lastPlacedPos = below;
            holdingBlock = true;
            return;
        } else if (result == PlaceResult.FAIL_ROTATION) {
            return;
        }

        for (EnumFacing dir : EnumFacing.values()) {
            if (dir == EnumFacing.DOWN) continue;
            BlockPos neighbor = below.offset(dir);
            if (neighbor.getY() > playerY && !andromed) continue;
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
                if (current.pos.getY() > playerY && !andromed) continue;

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
                    if (dir == EnumFacing.DOWN) continue;
                    BlockPos next = current.pos.offset(dir);

                    if (next.getY() > playerY && !andromed) continue;

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
                        mc.thePlayer.motionY = 1;
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
            if (!rotationMode.get().equals("OFF")) {
                if (jumpTicks <= 0) {
                    DewCommon.rotationManager.rotateToward((float) MovementUtil.getDirection(), 80f, 180f, true);
                } else if (jumpTicks <= antiPlaceUntil.get().intValue()) {
                    DewCommon.rotationManager.rotateToward((float) (MovementUtil.getDirection() + 180f) + (antiBackSprint.get() ? 80f : 0f), 80f, this.getTellyRotSpeed(), true);
                }
            }

            if (mc.thePlayer.posY > 0.0D && mc.thePlayer.onGround && this.isNearEdge() && mc.thePlayer.isSprinting() && !Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode())) {
                mc.thePlayer.jump();
            }
        }
    }

    private void edgeCheck() {
        if (edgeSafeMode.get().equals("Sneak") && this.isNearEdge() && !mode.get().equals("Telly") || mode.get().equals("Telly") && mc.thePlayer.onGround && this.isNearEdge() && !doTellyInThisJump) {
            mc.gameSettings.keyBindSneak.setKeyDown(true);
            isSneaking = true;
        } else if (isSneaking) {
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

        if (!mode.get().equals("Telly")) {
            if (edgeSafeMode.get().equals("Safewalk")) {
                event.isSafeWalk = true;
            } else if (edgeSafeMode.get().equals("Ground Safewalk")) {
                event.isSafeWalk = true;
                event.onlySafeOnGround = true;
            }
        }
    }

    private void updateKeepY() {
        if (keepY == -1 && (mc.thePlayer.hurtTime != 0 || mc.thePlayer.motionY <= -0.55)) {
            keepY = (int) (mc.thePlayer.posY - 1.5);
        } else {
            keepY = (int) mc.thePlayer.posY;

            if (andromeda.get() && andromed) {
                keepY += 2;
            }
        }
    }

    private boolean canPlaceAt(BlockPos pos) {
        if (!isAirLike(pos)) return false;

        AxisAlignedBB bb = new AxisAlignedBB(
                pos.getX(), pos.getY(), pos.getZ(),
                pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1
        );

        if (mc.thePlayer.getEntityBoundingBox().intersectsWith(bb)) return false;

        for (EnumFacing dir : EnumFacing.values()) {
            if (dir == EnumFacing.DOWN) continue;
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
            return jumpTicks <= antiPlaceUntil.get().intValue();
        }

        return false;
    }

    public boolean isNearEdge() {
        double px = mc.thePlayer.posX;
        double py = mc.thePlayer.posY;
        double pz = mc.thePlayer.posZ;

        int baseY = (int) Math.floor(py) - 1;

        double radius = 0.35;
        double step = Math.PI / 36;

        if (mc.thePlayer.isPotionActive(Potion.moveSpeed)) {
            radius += 0.15;
        }

        if (!MovementUtil.isBlockUnderPlayer(mc.thePlayer, 1, 0.1, false)) {
            for (double angle = 0; angle < Math.PI * 2; angle += step) {
                double checkX = px + Math.cos(angle) * radius;
                double checkZ = pz + Math.sin(angle) * radius;

                BlockPos pos = new BlockPos(checkX, baseY, checkZ);
                Block block = mc.theWorld.getBlockState(pos).getBlock();

                if (block.isReplaceable(mc.theWorld, pos) || !block.getMaterial().isSolid() || !block.isFullCube()) {
                    return true;
                }
            }
        }

        return false;
    }

    private PlaceResult tryPlaceBlock(BlockPos pos) {
        if (!holdingBlock) return PlaceResult.FAIL_OTHER;

        String modeValue = mode.get();
        float rotationSpeedVal = rotationSpeed.get().floatValue();

        blockData = this.getBlockData(pos);

        if (blockData == null) return PlaceResult.FAIL_OTHER;
        if (needSnapRotationReset) return PlaceResult.FAIL_ROTATION;

        if (!rotationMode.get().equals("OFF")) {
            if (modeValue.equals("Normal") || modeValue.equals("Telly")) {
                boolean canPlace = DewCommon.rotationManager.faceBlockWithFacing(blockData.position, blockData.face, rotationSpeedVal, simpleRotator.get(), true, antiBackSprint.get());
                if (!noRotationHitCheck.get() && !canPlace) return PlaceResult.FAIL_ROTATION;
            }
        }

        ItemStack itemstack = mc.thePlayer.inventory.getCurrentItem();
        if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, itemstack, blockData.position, blockData.face, new Vec3(blockData.position.getX() + Math.random(), blockData.position.getY() + Math.random(), blockData.position.getZ() + Math.random()))) {
            if (swingMode.get().equals("Normal")) {
                mc.thePlayer.swingItem();
            } else {
                PacketUtil.sendPacket(new C0APacketAnimation());
            }
            if (itemstack != null) {
                if (itemstack.stackSize == 0) {
                    mc.thePlayer.inventory.mainInventory[mc.thePlayer.inventory.currentItem] = null;
                } else if (mc.playerController.isInCreativeMode()) {
                    mc.entityRenderer.itemRenderer.resetEquippedProgress();
                }
            }
            delay = 0;
            if (!mode.get().equals("Hypixel") && rotationMode.get().equals("Snap") && DewCommon.rotationManager.isRotating()) {
                needSnapRotationReset = true;
            }
            if (andromed) {
                if (MovementUtil.isBlockAbovePlayer(mc.thePlayer, 1, 0.2)) {
                    andromed = false;
                }
            } else {
                if (MovementUtil.isBlockUnderPlayer(mc.thePlayer, 1, 0.2, false)) {
                    andromed = true;
                }
            }
            return PlaceResult.SUCCESS;
        }

        return PlaceResult.FAIL_OTHER;
    }

    private class BlockData {
        public BlockPos position;
        public EnumFacing face;

        public BlockData(BlockPos position, EnumFacing face) {
            this.position = position;
            this.face = face;
        }
    }

    private BlockData getBlockData(BlockPos pos) {
        OffsetAndFacing[] offsets = {
                new OffsetAndFacing(0, -1, 0, EnumFacing.UP),
                new OffsetAndFacing(-1, 0, 0, EnumFacing.EAST),
                new OffsetAndFacing(1, 0, 0, EnumFacing.WEST),
                new OffsetAndFacing(0, 0, -1, EnumFacing.SOUTH),
                new OffsetAndFacing(0, 0, 1, EnumFacing.NORTH)
        };

        for (OffsetAndFacing offset : offsets) {
            BlockData result = checkBlock(pos, offset.x, offset.y, offset.z, offset.facing);
            if (result != null) return result;
        }

        if (!andromed) {
            BlockPos[] neighbors = {
                    pos.add(-1, 0, 0),
                    pos.add(1, 0, 0),
                    pos.add(0, 0, -1),
                    pos.add(0, 0, 1)
            };

            OffsetAndFacing[] horizontalOffsets = {
                    new OffsetAndFacing(-1, 0, 0, EnumFacing.EAST),
                    new OffsetAndFacing(1, 0, 0, EnumFacing.WEST),
                    new OffsetAndFacing(0, 0, -1, EnumFacing.SOUTH),
                    new OffsetAndFacing(0, 0, 1, EnumFacing.NORTH)
            };

            for (BlockPos neighbor : neighbors) {
                for (OffsetAndFacing offset : horizontalOffsets) {
                    BlockData result = checkBlock(neighbor, offset.x, offset.y, offset.z, offset.facing);
                    if (result != null) return result;
                }
            }
        }

        return null;
    }

    private BlockData checkBlock(BlockPos base, int dx, int dy, int dz, EnumFacing facing) {
        BlockPos targetPos = base.add(dx, dy, dz);
        if (!invalid.contains(mc.theWorld.getBlockState(targetPos).getBlock())) {
            return new BlockData(targetPos, facing);
        }
        return null;
    }

    private static class OffsetAndFacing {
        final int x, y, z;
        final EnumFacing facing;

        OffsetAndFacing(int x, int y, int z, EnumFacing facing) {
            this.x = x;
            this.y = y;
            this.z = z;
            this.facing = facing;
        }
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