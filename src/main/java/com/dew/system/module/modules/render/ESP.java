package com.dew.system.module.modules.render;

import com.dew.system.event.events.Render3DEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.utils.RenderUtil;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

public class ESP extends Module {

    public ESP() {
        super("ESP", ModuleCategory.RENDER, Keyboard.KEY_NONE, false, false, true);
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        for (EntityPlayer player : mc.theWorld.playerEntities) {
            if (player instanceof EntityPlayerSP || player.isInvisible()) continue;

            double x = player.lastTickPosX + (player.posX - player.lastTickPosX) * event.partialTicks - mc.getRenderManager().viewerPosX;
            double y = player.lastTickPosY + (player.posY - player.lastTickPosY) * event.partialTicks - mc.getRenderManager().viewerPosY;
            double z = player.lastTickPosZ + (player.posZ - player.lastTickPosZ) * event.partialTicks - mc.getRenderManager().viewerPosZ;

            AxisAlignedBB bb = player.getEntityBoundingBox().offset(-player.posX, -player.posY, -player.posZ)
                    .offset(x, y, z);

            RenderUtil.drawOutlinedBoundingBox(bb, 1f, 1f, 1f, 0.2f);

            GlStateManager.pushMatrix();
            GlStateManager.enableBlend();
            GlStateManager.disableTexture2D();
            GlStateManager.disableDepth();
            GlStateManager.depthMask(false);
            GlStateManager.disableLighting();
            GlStateManager.disableCull();
            GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

            RenderUtil.drawFilledBox(bb, 1f, 1f, 1f, 0.15f);

            GlStateManager.enableCull();
            GlStateManager.depthMask(true);
            GlStateManager.enableDepth();
            GlStateManager.enableTexture2D();
            GlStateManager.disableBlend();
            GlStateManager.popMatrix();
        }
    }
}