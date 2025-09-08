package com.dew.system.module.modules.render;

import com.dew.DewCommon;
import com.dew.system.event.events.Render3DEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.module.modules.combat.FastBow;
import com.dew.system.settingsvalue.BooleanValue;
import com.dew.utils.RenderUtil;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.item.*;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.BlockPos;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.Vec3;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.awt.*;
import java.util.List;

public class Trajectories extends Module {
    private static final BooleanValue sheepWars = new BooleanValue("Sheep Wars", false);
    public Trajectories() {
        super("Trajectories", ModuleCategory.RENDER, Keyboard.KEY_NONE, true, false, true);
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        ItemStack heldItem = mc.thePlayer.getHeldItem();
        if (heldItem == null) return;

        Item item = heldItem.getItem();

        Vec3 finalHitVec = null;
        float motionFactor = 1.5F;
        float motionSlowdown = 0.99F;
        float gravity;
        float size;

        float partialTicks = event.partialTicks;

        if (sheepWars.get() && item instanceof ItemSkull) {
            gravity = 0.05F;
            size = 0.3F;

            float useTicks = 18;

            float power = useTicks / 20.0F;
            power = (power * power + power * 2.0F) / 3.0F;

            motionFactor = power * 3.0F;
        } else if (item instanceof ItemBow) {
            if (!mc.thePlayer.isUsingItem()) return;

            gravity = 0.05F;
            size = 0.3F;

            int maxUseDuration = heldItem.getMaxItemUseDuration();
            int count = DewCommon.moduleManager.getModule(FastBow.class).isEnabled() ? 25 : mc.thePlayer.getItemInUseCount();
            float useTicks = (float) (maxUseDuration - count) + partialTicks;

            float power = useTicks / 20.0F;
            power = (power * power + power * 2.0F) / 3.0F;

            if (power < 0.1F) return;
            if (power > 1.0F) power = 1.0F;

            motionFactor = power * 3.0F;
        } else if (item instanceof ItemFishingRod) {
            gravity = 0.04F;
            size = 0.25F;
            motionSlowdown = 0.92F;
        } else if (item instanceof ItemPotion && ItemPotion.isSplash(heldItem.getMetadata())) {
            gravity = 0.05F;
            size = 0.25F;
            motionFactor = 0.5F;
        } else if (item instanceof ItemSnowball || item instanceof ItemEnderPearl || item instanceof ItemEgg) {
            gravity = 0.03F;
            size = 0.25F;
        } else {
            return;
        }

        float yaw = DewCommon.rotationManager.getInterpolatedYaw(partialTicks);
        float pitch = DewCommon.rotationManager.getInterpolatedPitch(partialTicks);
        float yawRad = (float) Math.toRadians(yaw);
        float pitchRad = (float) Math.toRadians(pitch);

        double playerX = mc.thePlayer.lastTickPosX + (mc.thePlayer.posX - mc.thePlayer.lastTickPosX) * partialTicks;
        double playerY = mc.thePlayer.lastTickPosY + (mc.thePlayer.posY - mc.thePlayer.lastTickPosY) * partialTicks;
        double playerZ = mc.thePlayer.lastTickPosZ + (mc.thePlayer.posZ - mc.thePlayer.lastTickPosZ) * partialTicks;

        double posX = playerX - Math.cos(yawRad) * 0.16D;
        double posY = playerY + mc.thePlayer.getEyeHeight() - 0.1D;
        double posZ = playerZ - Math.sin(yawRad) * 0.16D;

        double motionX = -Math.sin(yawRad) * Math.cos(pitchRad);
        double motionY = -Math.sin(pitchRad);
        double motionZ = Math.cos(yawRad) * Math.cos(pitchRad);

        double dist = Math.sqrt(motionX * motionX + motionY * motionY + motionZ * motionZ);
        motionX = (motionX / dist) * motionFactor;
        motionY = (motionY / dist) * motionFactor;
        motionZ = (motionZ / dist) * motionFactor;

        GlStateManager.pushMatrix();
        enableGL();

        double renderX = mc.getRenderManager().viewerPosX;
        double renderY = mc.getRenderManager().viewerPosY;
        double renderZ = mc.getRenderManager().viewerPosZ;

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer buffer = tessellator.getWorldRenderer();
        buffer.begin(GL11.GL_LINE_STRIP, DefaultVertexFormats.POSITION);

        float speed = 1800f;
        float time = (System.currentTimeMillis() % (int) speed) / speed;
        Color colorA = RenderUtil.getThemeColor(time, 0);
        Color color = new Color(colorA.getRed(), colorA.getGreen(), colorA.getBlue(), 80);

        while (posY > 0.0) {
            Vec3 current = new Vec3(posX, posY, posZ);
            Vec3 next = new Vec3(posX + motionX, posY + motionY, posZ + motionZ);
            MovingObjectPosition landing = mc.theWorld.rayTraceBlocks(current, next, false, true, false);
            if (landing != null) {
                Vec3 hitVec = landing.hitVec;
                finalHitVec = hitVec;
                buffer.pos(hitVec.xCoord - renderX, hitVec.yCoord - renderY, hitVec.zCoord - renderZ).endVertex();
                break;
            }

            buffer.pos(posX - renderX, posY - renderY, posZ - renderZ).endVertex();

            posX += motionX;
            posY += motionY;
            posZ += motionZ;

            AxisAlignedBB arrowBox = new AxisAlignedBB(posX - size, posY - size, posZ - size,
                    posX + size, posY + size, posZ + size).addCoord(motionX, motionY, motionZ).expand(1.0D, 1.0D, 1.0D);

            List<Entity> entityList = mc.theWorld.getEntitiesWithinAABBExcludingEntity(mc.thePlayer, arrowBox);
            for (Entity e : entityList) {
                if (e.canBeCollidedWith()) {
                    AxisAlignedBB bb = e.getEntityBoundingBox().expand(size, size, size);
                    MovingObjectPosition intercept = bb.calculateIntercept(current, next);
                    if (intercept != null) {
                        Vec3 hitVec = intercept.hitVec;
                        finalHitVec = hitVec;
                        buffer.pos(hitVec.xCoord - renderX, hitVec.yCoord - renderY, hitVec.zCoord - renderZ).endVertex();
                        tessellator.draw();
                        double boxSize = 0.2D;
                        AxisAlignedBB box = new AxisAlignedBB(
                                finalHitVec.xCoord - boxSize - renderX,
                                finalHitVec.yCoord - boxSize - renderY,
                                finalHitVec.zCoord - boxSize - renderZ,
                                finalHitVec.xCoord + boxSize - renderX,
                                finalHitVec.yCoord + boxSize - renderY,
                                finalHitVec.zCoord + boxSize - renderZ
                        );

                        RenderUtil.glColor(color);
                        RenderUtil.drawSelectionFilledBox(box);
                        disableGL();
                        GlStateManager.popMatrix();
                        return;
                    }
                }
            }

            if (mc.theWorld.getBlockState(new BlockPos(posX, posY, posZ)).getBlock().getMaterial() == Material.water) {
                motionX *= 0.6D;
                motionY *= 0.6D;
                motionZ *= 0.6D;
            } else {
                motionX *= motionSlowdown;
                motionY *= motionSlowdown;
                motionZ *= motionSlowdown;
            }

            motionY -= gravity;
        }

        tessellator.draw();
        if (finalHitVec != null) {
            double boxSize = 0.2D;
            AxisAlignedBB box = new AxisAlignedBB(
                    finalHitVec.xCoord - boxSize - renderX,
                    finalHitVec.yCoord - boxSize - renderY,
                    finalHitVec.zCoord - boxSize - renderZ,
                    finalHitVec.xCoord + boxSize - renderX,
                    finalHitVec.yCoord + boxSize - renderY,
                    finalHitVec.zCoord + boxSize - renderZ
            );

            RenderUtil.glColor(color);
            RenderUtil.drawSelectionFilledBox(box);
        }
        disableGL();
        GlStateManager.popMatrix();
    }

    private void enableGL() {
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glLineWidth(3.5F);
        GlStateManager.color(0F, 1F, 1F, 1F);
    }

    private void disableGL() {
        GL11.glPopAttrib();
    }
}