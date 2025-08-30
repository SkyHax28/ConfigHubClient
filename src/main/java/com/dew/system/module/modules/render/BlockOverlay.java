package com.dew.system.module.modules.render;

import com.dew.system.event.events.Render3DEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.utils.Lerper;
import com.dew.utils.RenderUtil;
import net.minecraft.block.Block;
import net.minecraft.block.state.IBlockState;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class BlockOverlay extends Module {

    public BlockOverlay() {
        super("Block Overlay", ModuleCategory.RENDER, Keyboard.KEY_NONE, true, false, true);
    }

    public BlockPos getCurrentBlock() {
        if (mc.objectMouseOver == null || mc.objectMouseOver.getBlockPos() == null) {
            return null;
        }

        BlockPos blockPos = mc.objectMouseOver.getBlockPos();
        IBlockState state = mc.theWorld.getBlockState(blockPos);

        if (state.getBlock().canCollideCheck(state, false) && mc.theWorld.getWorldBorder().contains(blockPos)) {
            return blockPos;
        }

        return null;
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (mc.theWorld == null || getCurrentBlock() == null) return;

        Block block = mc.theWorld.getBlockState(getCurrentBlock()).getBlock();
        if (block == null) return;

        float partialTicks = event.partialTicks;

        float speed = 1800f;
        float time = (System.currentTimeMillis() % (int) speed) / speed;
        Color colorA = getColor(time, 0);
        Color color = new Color(colorA.getRed(), colorA.getGreen(), colorA.getBlue(), 40);
        Color colorB = new Color(colorA.getRed(), colorA.getGreen(), colorA.getBlue(), 240);

        GlStateManager.enableBlend();
        GlStateManager.tryBlendFuncSeparate(
                GL11.GL_SRC_ALPHA,
                GL11.GL_ONE_MINUS_SRC_ALPHA,
                GL11.GL_ONE,
                GL11.GL_ZERO
        );
        GL11.glLineWidth(2.0F);
        GlStateManager.disableTexture2D();
        GlStateManager.depthMask(false);

        block.setBlockBoundsBasedOnState(mc.theWorld, getCurrentBlock());

        double x = mc.thePlayer.lastTickPosX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * partialTicks;
        double y = mc.thePlayer.lastTickPosY + (mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * partialTicks;
        double z = mc.thePlayer.lastTickPosZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * partialTicks;

        AxisAlignedBB axisAlignedBB = block.getSelectedBoundingBox(mc.theWorld, getCurrentBlock())
                .expand(0.0020000000949949026D, 0.0020000000949949026D, 0.0020000000949949026D)
                .offset(-x, -y, -z);

        glColor(colorB);
        RenderUtil.drawSelectionBoundingBox(axisAlignedBB);
        glColor(color);
        RenderUtil.drawSelectionFilledBox(axisAlignedBB);

        GlStateManager.depthMask(true);
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.resetColor();
    }

    private void glColor(final Color color) {
        final float red = color.getRed() / 255F;
        final float green = color.getGreen() / 255F;
        final float blue = color.getBlue() / 255F;
        final float alpha = color.getAlpha() / 255F;

        GlStateManager.color(red, green, blue, alpha);
    }

    private Color getColor(float progress, float index) {
        float offset = index * 0.01f;
        progress = ((progress + offset) % 1.0f + 1.0f) % 1.0f;

        float segment = progress * 3;

        if (segment < 1) {
            return Lerper.lerpColor(new Color(0, 120, 255), new Color(0, 230, 255), segment);
        } else if (segment < 2) {
            float t = segment - 1;
            return Lerper.lerpColor(new Color(0, 230, 255), new Color(0, 180, 220), t);
        } else {
            float t = segment - 2;
            return Lerper.lerpColor(new Color(0, 180, 220), new Color(0, 120, 255), t);
        }
    }
}
