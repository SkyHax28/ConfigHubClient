package com.dew.system.module.modules.player;

import com.dew.DewCommon;
import com.dew.system.event.events.Render3DEvent;
import com.dew.system.event.events.SendPacketEvent;
import com.dew.system.event.events.TickEvent;
import com.dew.system.event.events.WorldLoadEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.settingsvalue.BooleanValue;
import com.dew.system.settingsvalue.NumberValue;
import com.dew.utils.LogUtil;
import com.dew.utils.PacketUtil;
import com.dew.utils.RenderUtil;
import net.minecraft.block.Block;
import net.minecraft.block.BlockAir;
import net.minecraft.block.BlockBed;
import net.minecraft.block.material.Material;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.network.play.server.S29PacketSoundEffect;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.WorldSettings;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class Breaker extends Module {

    private static final NumberValue range = new NumberValue("Range", 3.0, 1.0, 6.0, 1.0);
    private static final NumberValue rotationSpeed = new NumberValue("Rotation Speed", 60.0, 0.0, 180.0, 5.0);
    private static final BooleanValue breakOpposite = new BooleanValue("Break Opposite", false);
    private static final BooleanValue autoWhitelist = new BooleanValue("Auto Whitelist", false);
    private final List<BlockPos> bedWhitelist = new ArrayList<>();
    public boolean isBreaking = false;
    private BlockPos currentTarget = null;

    public Breaker() {
        super("Breaker", ModuleCategory.PLAYER, Keyboard.KEY_NONE, false, true, true);
    }

    @Override
    public void onEnable() {
        if (autoWhitelist.get()) {
            LogUtil.printChat("Press the right control key or world switch to clear the bed whitelist");
        }
    }

    @Override
    public void onDisable() {
        this.resetState();
    }

    @Override
    public void onLoadWorld(WorldLoadEvent event) {
        this.resetState();
        bedWhitelist.clear();
    }

    private void resetState() {
        currentTarget = null;

        if (isBreaking) {
            if (DewCommon.moduleManager.getModule(AutoTool.class).isEnabled())
                DewCommon.moduleManager.getModule(AutoTool.class).autoToolManager.stop();
            isBreaking = false;
        }
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null || mc.playerController == null || mc.playerController.getCurrentGameType() == WorldSettings.GameType.ADVENTURE || mc.playerController.getCurrentGameType() == WorldSettings.GameType.SPECTATOR || currentTarget == null || !isBreaking) return;

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

        double x = currentTarget.getX() - renderX;
        double y = currentTarget.getY() - renderY;
        double z = currentTarget.getZ() - renderZ;
        AxisAlignedBB bb = new AxisAlignedBB(
                x, y, z,
                x + 1, y + 1, z + 1
        );
        RenderUtil.drawFilledBox(bb, 0f, 1f, 0f, 0.2f);

        GlStateManager.enableCull();
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    @Override
    public void onTick(TickEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null || mc.playerController == null || mc.playerController.getCurrentGameType() == WorldSettings.GameType.ADVENTURE || mc.playerController.getCurrentGameType() == WorldSettings.GameType.SPECTATOR || DewCommon.moduleManager.getModule(Scaffold.class).isEnabled()) return;

        if (autoWhitelist.get()) {
            if (!bedWhitelist.isEmpty()) {
                if (Keyboard.isKeyDown(Keyboard.KEY_RCONTROL)) {
                    bedWhitelist.clear();
                }
            } else {
                if (mc.thePlayer.ticksExisted % 50 == 0 || mc.thePlayer.ticksExisted <= 20) {
                    this.addNearestBedToWhitelist();
                }
            }
        }

        BlockPos playerPos = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY, mc.thePlayer.posZ);
        List<BlockPos> bedPositions = new ArrayList<>();

        for (int x = -range.get().intValue(); x <= range.get().intValue(); x++) {
            for (int y = -range.get().intValue(); y <= range.get().intValue(); y++) {
                for (int z = -range.get().intValue(); z <= range.get().intValue(); z++) {
                    BlockPos pos = playerPos.add(x, y, z);
                    IBlockState state = mc.theWorld.getBlockState(pos);
                    Block block = mc.theWorld.getBlockState(pos).getBlock();

                    if (isBedBlock(block) && state.getValue(BlockBed.PART) == BlockBed.EnumPartType.HEAD) {
                        if (!bedWhitelist.contains(pos) || !autoWhitelist.get()) {
                            bedPositions.add(pos);
                        }
                    }
                }
            }
        }

        if (bedPositions.isEmpty()) {
            this.resetState();
            return;
        }

        bedPositions.sort(Comparator.comparingDouble(pos -> pos.distanceSq(playerPos)));
        BlockPos bedPos = bedPositions.get(0);
        BlockPos target = bedPos;

        if (breakOpposite.get()) {
            boolean hasAir = false;
            BlockPos softestPos = null;
            float softestHardness = Float.MAX_VALUE;

            for (EnumFacing facing : EnumFacing.values()) {
                if (facing == EnumFacing.DOWN) continue;
                BlockPos adjacent = bedPos.offset(facing);
                Block adjBlock = mc.theWorld.getBlockState(adjacent).getBlock();

                if (adjBlock instanceof BlockAir) {
                    hasAir = true;
                    break;
                }

                if (!isBedBlock(adjBlock)) {
                    float hardness = adjBlock.getBlockHardness(mc.theWorld, adjacent);
                    if (hardness >= 0 && hardness < softestHardness) {
                        softestHardness = hardness;
                        softestPos = adjacent;
                    }
                }
            }

            if (!hasAir) {
                if (softestPos != null) {
                    target = softestPos;
                } else {
                    this.resetState();
                    return;
                }
            }
        }

        if (target.equals(currentTarget)) {
            breakBlock(target);
        } else {
            currentTarget = target;
            breakBlock(target);
        }
    }

    @Override
    public void onSendPacket(SendPacketEvent event) {
        if (mc.thePlayer == null || !isBreaking) return;

        Packet<?> packet = event.packet;

        if (packet instanceof C07PacketPlayerDigging && ((C07PacketPlayerDigging) packet).getStatus() == C07PacketPlayerDigging.Action.ABORT_DESTROY_BLOCK && ((C07PacketPlayerDigging) packet).getFacing() == EnumFacing.DOWN) {
            event.cancel();
        }
    }

    private boolean isBedBlock(Block block) {
        return block instanceof BlockBed;
    }

    private void breakBlock(BlockPos pos) {
        if (DewCommon.rotationManager.isRotating()) {
            EnumFacing facing = getClosestFacing(pos);
            if (DewCommon.moduleManager.getModule(AutoTool.class).isEnabled())
                DewCommon.moduleManager.getModule(AutoTool.class).autoToolManager.start(pos);
            this.sendClickBlockToController(pos, facing, true);
            isBreaking = true;
        }

        DewCommon.rotationManager.faceBlock(pos, rotationSpeed.get().floatValue(), true, mc.playerController.getBlockReachDistance());
    }

    private void sendClickBlockToController(BlockPos pos, EnumFacing facing, boolean leftClick) {
        if (!leftClick) {
            mc.leftClickCounter = 0;
        }

        if (mc.leftClickCounter <= 0 && !mc.thePlayer.isUsingItem()) {
            if (leftClick) {
                if (mc.theWorld.getBlockState(pos).getBlock().getMaterial() != Material.air && mc.playerController.onPlayerDamageBlock(pos, facing)) {
                    mc.effectRenderer.addBlockHitEffects(pos, facing);
                    mc.thePlayer.swingItem();
                }
            }
        }
    }

    private void addNearestBedToWhitelist() {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        BlockPos respawnPos = mc.thePlayer.getPosition();
        if (respawnPos == null) return;

        BlockPos nearestBed = null;
        double nearestDist = Double.MAX_VALUE;

        int hRadius = 10;
        int yRadius = 2;
        for (int x = -hRadius; x <= hRadius; x++) {
            for (int y = -yRadius; y <= yRadius; y++) {
                for (int z = -hRadius; z <= hRadius; z++) {
                    BlockPos pos = respawnPos.add(x, y, z);
                    IBlockState state = mc.theWorld.getBlockState(pos);
                    Block block = state.getBlock();

                    if (isBedBlock(block) && state.getValue(BlockBed.PART) == BlockBed.EnumPartType.HEAD) {
                        double dist = respawnPos.distanceSq(pos);
                        if (dist < nearestDist) {
                            nearestDist = dist;
                            nearestBed = pos;
                        }
                    }
                }
            }
        }

        if (nearestBed != null) {
            bedWhitelist.add(nearestBed);
            int x = nearestBed.getX();
            int y = nearestBed.getY();
            int z = nearestBed.getZ();
            int distance = (int) mc.thePlayer.getDistance(x, y, z);
            LogUtil.printChat("Whitelisted bed at " + x + " " + y + " " + z + " (" + distance + " blocks away)");
        }
    }

    private EnumFacing getClosestFacing(BlockPos blockPos) {
        double dx = mc.thePlayer.posX - (blockPos.getX() + 0.5);
        double dy = (mc.thePlayer.posY + mc.thePlayer.getEyeHeight()) - (blockPos.getY() + 0.5);
        double dz = mc.thePlayer.posZ - (blockPos.getZ() + 0.5);

        double absDx = Math.abs(dx);
        double absDy = Math.abs(dy);
        double absDz = Math.abs(dz);

        if (absDy >= absDx && absDy >= absDz) {
            return dy > 0 ? EnumFacing.UP : EnumFacing.DOWN;
        } else if (absDx >= absDy && absDx >= absDz) {
            return dx > 0 ? EnumFacing.EAST : EnumFacing.WEST;
        } else {
            return dz > 0 ? EnumFacing.SOUTH : EnumFacing.NORTH;
        }
    }
}
