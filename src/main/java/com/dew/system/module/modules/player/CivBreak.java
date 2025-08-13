package com.dew.system.module.modules.player;

import com.dew.DewCommon;
import com.dew.system.event.events.PreMotionEvent;
import com.dew.system.event.events.Render3DEvent;
import com.dew.system.event.events.SendPacketEvent;
import com.dew.system.event.events.WorldLoadEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.settingsvalue.NumberValue;
import com.dew.utils.BlockUtil;
import com.dew.utils.PacketUtil;
import com.dew.utils.RenderUtil;
import net.minecraft.block.BlockAir;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.client.C07PacketPlayerDigging;
import net.minecraft.network.play.client.C0APacketAnimation;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.world.WorldSettings;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public class CivBreak extends Module {

    private static final NumberValue range = new NumberValue("Range", 3.0, 0.1, 6.0, 0.1);
    private static final NumberValue rotationSpeed = new NumberValue("Rotation Speed", 60.0, 0.0, 180.0, 5.0);
    private static final NumberValue breakDelay = new NumberValue("Break Delay", 1.0, 1.0, 10.0, 1.0);
    public boolean isBreaking = false;
    private BlockPos currentBlock = null;

    public CivBreak() {
        super("Civ Break", ModuleCategory.PLAYER, Keyboard.KEY_NONE, false, true, true);
    }

    @Override
    public void onDisable() {
        currentBlock = null;
        isBreaking = false;
    }

    @Override
    public void onLoadWorld(WorldLoadEvent event) {
        currentBlock = null;
        isBreaking = false;
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null || mc.playerController == null || mc.playerController.getCurrentGameType() == WorldSettings.GameType.ADVENTURE || mc.playerController.getCurrentGameType() == WorldSettings.GameType.SPECTATOR || currentBlock == null) return;

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

        double x = currentBlock.getX() - renderX;
        double y = currentBlock.getY() - renderY;
        double z = currentBlock.getZ() - renderZ;
        AxisAlignedBB bb = new AxisAlignedBB(
                x, y, z,
                x + 1, y + 1, z + 1
        );
        if (BlockUtil.getCenterDistance(currentBlock) > range.get() || mc.theWorld.getBlockState(currentBlock).getBlock() instanceof BlockAir) {
            RenderUtil.drawFilledBox(bb, 0f, 1f, 0f, 0.2f);
        } else {
            RenderUtil.drawFilledBox(bb, 1f, 0f, 0f, 0.2f);
        }

        GlStateManager.enableCull();
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    @Override
    public void onPreMotion(PreMotionEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null || mc.playerController == null || mc.playerController.getCurrentGameType() == WorldSettings.GameType.ADVENTURE || mc.playerController.getCurrentGameType() == WorldSettings.GameType.SPECTATOR || currentBlock == null || BlockUtil.getCenterDistance(currentBlock) > range.get() || mc.theWorld.getBlockState(currentBlock).getBlock() instanceof BlockAir) {
            isBreaking = false;
            return;
        }

        DewCommon.rotationManager.faceBlock(currentBlock, rotationSpeed.get().floatValue());
        isBreaking = true;

        if (mc.thePlayer.ticksExisted % breakDelay.get().intValue() == 0) {
            mc.thePlayer.swingItem();
            if (mc.thePlayer.capabilities.isCreativeMode) {
                PacketUtil.sendPacketAsSilent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.START_DESTROY_BLOCK, currentBlock, EnumFacing.DOWN));
            } else {
                for (int i = 0; i < 2; i++) {
                    PacketUtil.sendPacketAsSilent(new C07PacketPlayerDigging(C07PacketPlayerDigging.Action.STOP_DESTROY_BLOCK, currentBlock, EnumFacing.DOWN));
                }
            }
        }
    }

    @Override
    public void onSendPacket(SendPacketEvent event) {
        if (mc.thePlayer == null) return;

        Packet<?> packet = event.packet;

        if (packet instanceof C07PacketPlayerDigging && ((C07PacketPlayerDigging) packet).getStatus() == C07PacketPlayerDigging.Action.START_DESTROY_BLOCK) {
            currentBlock = ((C07PacketPlayerDigging) packet).getPosition();
        }
    }
}
