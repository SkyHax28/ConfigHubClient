package com.dew.system.module.modules.player;

import com.dew.DewCommon;
import com.dew.system.event.events.*;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.module.modules.movement.speed.SpeedModule;
import com.dew.system.settingsvalue.BooleanValue;
import com.dew.system.settingsvalue.NumberValue;
import com.dew.system.settingsvalue.SelectionValue;
import com.dew.utils.LogUtil;
import com.dew.utils.MovementUtil;
import com.dew.utils.PacketUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.potion.Potion;
import net.minecraft.util.*;
import org.lwjgl.input.Keyboard;

import java.util.*;

public class Scaffold extends Module {

    public Scaffold() {
        super("Scaffold", ModuleCategory.PLAYER, Keyboard.KEY_NONE, false, true, true);
    }

    private static final SelectionValue mode = new SelectionValue("Mode", "Normal", "Normal", "Telly", "Hypixel");
    private static final SelectionValue towerMode = new SelectionValue("Tower Mode", "OFF", "OFF", "Vanilla", "Hypixel");
    private static final NumberValue clutchRange = new NumberValue("Clutch Range", 3.0, 1.0, 5.0, 1.0);
    private static final NumberValue rotationSpeed = new NumberValue("Rotation Speed", 60.0, 0.0, 180.0, 10.0, () -> mode.get().equals("Normal") || mode.get().equals("Telly"));
    private static final NumberValue placeDelay = new NumberValue("Place Delay", 0.0, 0.0, 10.0, 1.0, () -> mode.get().equals("Normal") || mode.get().equals("Telly"));
    private static final BooleanValue noHitCheck = new BooleanValue("No Hit Check", false, () -> mode.get().equals("Normal") || mode.get().equals("Telly"));
    private static final SelectionValue edgeSafeMode = new SelectionValue("Edge Safe Mode", "OFF", "OFF", "Safewalk", "Sneak");
    public static final BooleanValue noSprint = new BooleanValue("No Sprint", false);

    private double keepY = -1;
    private int originalSlot = -1;
    public boolean holdingBlock = false;
    private boolean isSneaking = false;

    private int jumpTicks = 0;
    private int towerTicks = 0;
    private boolean hypGroundCheck = false;
    private boolean towered = false;

    private int delay = 0;

    private boolean checked = false;

    private boolean canTower() {
        return Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode()) && !DewCommon.moduleManager.getModule(SpeedModule.class).isEnabled() && MovementUtil.isBlockUnderPlayer(mc.thePlayer, 3, 1.2, false) && !MovementUtil.isBlockAbovePlayer(mc.thePlayer, 1) && holdingBlock;
    }

    private boolean shouldUpdateKeepYState() {
        return keepY == -1 || !DewCommon.moduleManager.getModule(SpeedModule.class).isEnabled() && !mode.get().equals("Telly") && !mode.get().equals("Hypixel") || Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode());
    }

    @Override
    public void onDisable() {
        this.resetState();
        keepY = -1.0;
    }

    @Override
    public void onWorld(WorldEvent event) {
        this.setState(false);
    }

    private void resetState() {
        if (originalSlot != -1 && mc.thePlayer != null)
            mc.thePlayer.inventory.currentItem = originalSlot;
        originalSlot = -1;
        delay = 0;
        holdingBlock = false;
        checked = false;
        if (isSneaking) {
            mc.gameSettings.keyBindSneak.setKeyDown(false);
            isSneaking = false;
        }
        jumpTicks = 0;
        towerTicks = 0;
        hypGroundCheck = false;
        if (mode.get().equals("Hypixel")) {
            MovementUtil.mcJumpNoBoost = false;
        }
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
    public void onRender2D(Render2DEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        int totalBlocks = getTotalValidBlocksInHotbar();
        String display = totalBlocks + " blocks";

        ScaledResolution sr = new ScaledResolution(mc);
        int x = sr.getScaledWidth() / 2 + 20;
        int y = sr.getScaledHeight() / 2;

        GlStateManager.pushMatrix();
        RenderHelper.disableStandardItemLighting();
        mc.bitFontRendererObj.drawStringWithShadow(display, x, y, 0xFFFFFF);
        GlStateManager.popMatrix();
    }

    private void strafeWithCorrectHypPotMath(float speed) {
        if (!MovementUtil.isMoving()) return;
        if (mc.thePlayer.isPotionActive(Potion.moveSpeed))
            MovementUtil.strafe(speed + ((mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() + 1f) * mc.thePlayer.getActivePotionEffect(Potion.moveSpeed).getAmplifier() == 0 ? 0.036f : 0.12f));
        else MovementUtil.strafe(speed);
    }

    @Override
    public void onItemRender(ItemRenderEvent event) {
        if (originalSlot != -1) {
            event.itemToRender = mc.thePlayer.inventory.getStackInSlot(originalSlot);
        }
    }

    @Override
    public void onTick(TickEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        jumpTicks = mc.thePlayer.onGround ? 0 : jumpTicks + 1;
        delay++;

        this.doMainFunctions();
    }

    @Override
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        this.tellyFunction();

        towerTicks = mc.thePlayer.onGround ? 0 : towerTicks + 1;

        this.towerFunction();
    }

    @Override
    public void onPreMotion(PreMotionEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

    }

    private void doMainFunctions() {
        if (edgeSafeMode.get().equals("Sneak") && this.isNearEdge()) {
            mc.gameSettings.keyBindSneak.setKeyDown(true);
            isSneaking = true;
        } else if (isSneaking) {
            mc.gameSettings.keyBindSneak.setKeyDown(false);
            isSneaking = false;
        }

        if (!DewCommon.rotationManager.isRotating()) {
            checked = false;
        }

        switch (mode.get().toLowerCase()) {
            case "normal":
                if (!holdingBlock) {
                    DewCommon.rotationManager.setRotations((float) (MovementUtil.getDirection() - 180f), 83f);
                }
                break;

            case "hypixel":
                MovementUtil.mcJumpNoBoost = true;
                if (mc.thePlayer.onGround && MovementUtil.isMoving()) {
                    if (mc.thePlayer.onGround && mc.thePlayer.posY > 0.0D && !DewCommon.moduleManager.getModule(SpeedModule.class).isEnabled()) {
                        mc.thePlayer.jump();
                        this.strafeWithCorrectHypPotMath(0.472f);
                    }
                } else {
                    DewCommon.rotationManager.faceBlockHypixelSafe(90f);
                }
                break;

            default:
                break;
        }

        if (noSprint.get()) {
            mc.thePlayer.setSprinting(false);
        }

        if (shouldUpdateKeepYState()) {
            keepY = mc.thePlayer.posY;
        }

        if (this.isTellying()) {
            return;
        }

        if (delay <= placeDelay.get()) return;

        if (holdingBlock && (mc.thePlayer.getHeldItem() == null || !(mc.thePlayer.getHeldItem().getItem() instanceof ItemBlock) || mc.thePlayer.getHeldItem().stackSize == 0)) {
            this.resetState();
            return;
        }

        if (keepY == -1) return;

        BlockPos below = new BlockPos(mc.thePlayer.posX, keepY - 1.0, mc.thePlayer.posZ);

        if (!mc.theWorld.getBlockState(below).getBlock().isReplaceable(mc.theWorld, below)) return;

        int blockSlot = getValidBlockSlot(below);
        if (blockSlot == -1) return;

        if (!holdingBlock) {
            originalSlot = mc.thePlayer.inventory.currentItem;
            mc.thePlayer.inventory.currentItem = blockSlot;
            holdingBlock = true;
        }

        Boolean placed = null;

        if (!mode.get().equals("Hypixel") || !mc.thePlayer.onGround) {
            placed = placeBlockScaffold(below);

            if (!placed) {
                EnumFacing[] facings = getFullPrioritizedFacings(mc.thePlayer.motionY > 0, mc.thePlayer.isSprinting());

                for (EnumFacing dir : facings) {
                    BlockPos neighbor = below.offset(dir);
                    if (placeBlockScaffold(neighbor)) {
                        placed = true;
                        break;
                    }
                }
            }

            Set<BlockPos> visited = new HashSet<>();

            if (!placed && clutchRange.get() > 1) {
                List<BlockPos> searchOrder = new ArrayList<>();

                int range = clutchRange.get().intValue();

                for (int x = -range; x <= range; x++) {
                    for (int y = 0; y >= -range; y--) {
                        for (int z = -range; z <= range; z++) {
                            BlockPos pos = below.add(x, y, z);
                            if (mc.thePlayer.getDistanceSqToCenter(pos) <= range * range) {
                                searchOrder.add(pos);
                            }
                        }
                    }
                }

                searchOrder.sort(Comparator.comparingDouble(pos -> mc.thePlayer.getDistanceSqToCenter(pos)));

                for (BlockPos target : searchOrder) {
                    if (visited.contains(target)) continue;
                    visited.add(target);

                    if (!mc.theWorld.getBlockState(target).getBlock().isReplaceable(mc.theWorld, target)) continue;
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

                    if (!hasSupport) continue;

                    if (placeBlockScaffold(target)) {
                        placed = true;
                        break;
                    }
                }
            }
        }

        if (placed != null && placed) {
            holdingBlock = true;
        }
    }

    private void tellyFunction() {
        if (mode.get().equals("Telly") && !towered && !Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode())) {
            if (jumpTicks <= 2 || this.isBlockVeryCloseUnderPlayer()) {
                DewCommon.rotationManager.resetRotationsInstantly();
                if (mc.thePlayer.onGround && mc.thePlayer.posY > 0.0D) {
                    mc.thePlayer.jump();
                    keepY = mc.thePlayer.posY;
                }
            }
        }
    }

    private void towerFunction() {
        if (this.canTower() && !towerMode.get().equals("OFF")) {
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
                        if (mc.thePlayer.onGround) {
                            hypGroundCheck = true;
                        }

                        if (hypGroundCheck) {
                            switch (towerTicks % 3) {
                                case 0:
                                    this.strafeWithCorrectHypPotMath(0.2f);
                                    mc.thePlayer.motionY = 0.42;
                                    break;

                                case 1:
                                    mc.thePlayer.motionY = 0.33;
                                    break;

                                case 2:
                                    double targetY = Math.floor(mc.thePlayer.posY) + 1.0;
                                    mc.thePlayer.motionY = targetY - mc.thePlayer.posY;
                                    break;
                            }
                        }

                        if (towerTicks >= 18) {
                            hypGroundCheck = false;
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

    private boolean isTellying() {
        return mode.get().equals("Telly") && !towered && !Keyboard.isKeyDown(mc.gameSettings.keyBindJump.getKeyCode()) && (jumpTicks <= 2 || this.isBlockVeryCloseUnderPlayer());
    }

    private EnumFacing[] getFullPrioritizedFacings(boolean jumping, boolean sprinting) {
        return new EnumFacing[]{EnumFacing.DOWN, EnumFacing.NORTH, EnumFacing.SOUTH, EnumFacing.EAST, EnumFacing.WEST, EnumFacing.UP};
    }

    private boolean isBlockVeryCloseUnderPlayer() {
        double minY = mc.thePlayer.getEntityBoundingBox().minY;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                double checkX = mc.thePlayer.posX + dx;
                double checkZ = mc.thePlayer.posZ + dz;

                for (double offsetY = 0.0; offsetY <= 0.2; offsetY += 0.05) {
                    BlockPos pos = new BlockPos(
                            Math.floor(checkX),
                            Math.floor(minY - offsetY),
                            Math.floor(checkZ)
                    );

                    Block block = mc.theWorld.getBlockState(pos).getBlock();
                    if (block != Blocks.air) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    private boolean isNearEdge() {
        double x = mc.thePlayer.posX;
        double z = mc.thePlayer.posZ;
        int y = (int) Math.floor(mc.thePlayer.posY) - 1;

        double expand = 0.15;
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
        if (pitch > 45) return EnumFacing.DOWN;
        if (pitch < -45) return EnumFacing.UP;

        int direction = MathHelper.floor_double((yaw * 4.0F / 360.0F) + 0.5D) & 3;
        switch (direction) {
            case 0: return EnumFacing.SOUTH;
            case 1: return EnumFacing.WEST;
            case 2: return EnumFacing.NORTH;
            case 3: return EnumFacing.EAST;
        }
        return EnumFacing.UP;
    }

    private boolean placeBlockScaffold(BlockPos pos) {
        if (!holdingBlock) return false;

        float yaw = DewCommon.rotationManager.getClientYaw();
        float pitch = DewCommon.rotationManager.getClientPitch();

        EnumFacing preferredFacing = facingFromRotation(yaw, pitch);

        List<EnumFacing> facings = new ArrayList<>(Arrays.asList(EnumFacing.values()));
        facings.remove(preferredFacing);
        facings.add(0, preferredFacing);

        for (EnumFacing facing : facings) {
            if (!holdingBlock) return false;

            BlockPos neighbor = pos.offset(facing);
            IBlockState state = mc.theWorld.getBlockState(neighbor);
            Block block = state.getBlock();

            if (!block.canCollideCheck(state, false)) continue;

            EnumFacing opposite = facing.getOpposite();
            Vec3 hitVec = new Vec3(
                    neighbor.getX() + 0.5 + 0.5 * opposite.getFrontOffsetX(),
                    neighbor.getY() + 0.5 + 0.5 * opposite.getFrontOffsetY(),
                    neighbor.getZ() + 0.5 + 0.5 * opposite.getFrontOffsetZ()
            );

            if (mode.get().equals("Normal") || mode.get().equals("Telly")) {
                boolean canPlace = DewCommon.rotationManager.faceBlockWithFacing(neighbor, opposite, rotationSpeed.get().floatValue());
                if (!canPlace && !noHitCheck.get()) continue;
            }

            if (!checked) {
                checked = true;
                return true;
            } else if (mc.playerController.onPlayerRightClick(mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem(), neighbor, opposite, hitVec)) {
                mc.thePlayer.swingItem();
                int i = mc.thePlayer.getHeldItem() != null ? mc.thePlayer.getHeldItem().stackSize : 0;
                if (mc.thePlayer.getHeldItem().stackSize != 0 && (mc.thePlayer.getHeldItem().stackSize != i || mc.playerController.isInCreativeMode())) {
                    mc.entityRenderer.itemRenderer.resetEquippedProgress();
                }
                delay = 0;
                return true;
            }
        }
        return false;
    }

    private int getTotalValidBlocksInHotbar() {
        int total = 0;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
            if (stack == null || !(stack.getItem() instanceof ItemBlock) || stack.stackSize == 0) continue;

            Item item = stack.getItem();
            Block block = ((ItemBlock) item).getBlock();

            if (item == Item.getItemFromBlock(Blocks.chest) ||
                    item == Item.getItemFromBlock(Blocks.ender_chest) ||
                    item == Item.getItemFromBlock(Blocks.trapped_chest) ||
                    block.getMaterial().isReplaceable()) {
                continue;
            }

            if (block instanceof BlockFalling) {
                BlockPos belowBelow = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 2, mc.thePlayer.posZ);
                if (mc.theWorld.isAirBlock(belowBelow)) continue;
            }

            total += stack.stackSize;
        }
        return total;
    }

    private int getValidBlockSlot(BlockPos below) {
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.thePlayer.inventory.getStackInSlot(i);
            if (stack == null || !(stack.getItem() instanceof ItemBlock) || stack.stackSize == 0 || stack.getItem() == Item.getItemFromBlock(Blocks.chest) || stack.getItem() == Item.getItemFromBlock(Blocks.ender_chest) || stack.getItem() == Item.getItemFromBlock(Blocks.trapped_chest)) continue;

            Block block = ((ItemBlock) stack.getItem()).getBlock();

            if (block.getMaterial().isReplaceable()) continue;
            if (block instanceof BlockFalling) {
                BlockPos belowBelow = below.down();
                if (mc.theWorld.isAirBlock(belowBelow)) continue;
            }

            return i;
        }
        return -1;
    }
}
