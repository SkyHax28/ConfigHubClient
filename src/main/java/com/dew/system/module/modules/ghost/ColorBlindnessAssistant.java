package com.dew.system.module.modules.ghost;

import com.dew.system.event.events.PreUpdateEvent;
import com.dew.system.event.events.Render3DEvent;
import com.dew.system.event.events.WorldLoadEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.utils.RenderUtil;
import net.minecraft.block.BlockAir;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

public class ColorBlindnessAssistant extends Module {

    private final List<BlockPos> renderBlocks = new ArrayList<>();
    private int targetColorMeta = -1;

    public ColorBlindnessAssistant() {
        super("Color Blindness Assistant", ModuleCategory.GHOST, Keyboard.KEY_NONE, false, true, true);
    }

    @Override
    public void onDisable() {
        this.resetState();
    }

    @Override
    public void onLoadWorld(WorldLoadEvent event) {
        this.resetState();
    }

    private void resetState() {
        renderBlocks.clear();
        targetColorMeta = -1;
    }

    @Override
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        ItemStack item = mc.thePlayer.inventory.getStackInSlot(8);

        if (item != null) {
            targetColorMeta = item.getMetadata();
        } else {
            targetColorMeta = -1;
        }

        if (targetColorMeta != -1) {
            renderBlocks.clear();

            int radius = 10;

            BlockPos playerPos = new BlockPos(mc.thePlayer);
            for (int x = -radius; x <= radius; x++) {
                for (int y = -3; y <= 3; y++) {
                    for (int z = -radius; z <= radius; z++) {
                        BlockPos pos = playerPos.add(x, y, z);
                        if (mc.theWorld.getBlockState(pos).getBlock() instanceof BlockAir) continue;
                        int meta = mc.theWorld.getBlockState(pos).getBlock().getMetaFromState(mc.theWorld.getBlockState(pos));
                        if (meta == targetColorMeta) {
                            renderBlocks.add(pos);
                        }
                    }
                }
            }
        } else {
            renderBlocks.clear();
        }
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (targetColorMeta == -1 || renderBlocks.isEmpty()) return;

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

        for (BlockPos pos : renderBlocks) {
            double x = pos.getX() - mc.getRenderManager().viewerPosX;
            double y = pos.getY() - mc.getRenderManager().viewerPosY;
            double z = pos.getZ() - mc.getRenderManager().viewerPosZ;

            AxisAlignedBB box = new AxisAlignedBB(x, y, z, x + 1, y + 1, z + 1);
            RenderUtil.drawFilledBox(box, 0f, 0f, 0f, 1.0f);
        }

        GlStateManager.enableCull();
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }
}
