package net.minecraft.client.renderer.entity.layers;

import com.dew.DewCommon;
import com.dew.IMinecraft;
import com.dew.system.module.modules.combat.AutoPot;
import com.dew.system.module.modules.player.AutoTool;
import com.dew.system.module.modules.player.Scaffold;
import com.dew.system.module.modules.visual.ItemAnimations;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.model.ModelBiped;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.block.model.ItemCameraTransforms;
import net.minecraft.client.renderer.entity.RendererLivingEntity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Items;
import net.minecraft.item.*;

import java.util.UUID;

import static net.minecraft.client.renderer.GlStateManager.rotate;
import static net.minecraft.client.renderer.GlStateManager.translate;

public class LayerHeldItem implements LayerRenderer<EntityLivingBase>
{
    private final RendererLivingEntity<?> livingEntityRenderer;

    public LayerHeldItem(RendererLivingEntity<?> livingEntityRendererIn)
    {
        this.livingEntityRenderer = livingEntityRendererIn;
    }

    public void doRenderLayer(EntityLivingBase entitylivingbaseIn, float p_177141_2_, float p_177141_3_, float partialTicks, float p_177141_5_, float p_177141_6_, float p_177141_7_, float scale)
    {
        ItemStack itemstack = entitylivingbaseIn.getHeldItem();

        if (IMinecraft.mc.theWorld.getPlayerEntityByUUID(entitylivingbaseIn.getUniqueID()) instanceof EntityPlayerSP) {
            if (DewCommon.moduleManager.getModule(Scaffold.class).isEnabled() && DewCommon.moduleManager.getModule(Scaffold.class).getOriginalSlot() != -1) {
                itemstack = IMinecraft.mc.thePlayer.inventory.getStackInSlot(DewCommon.moduleManager.getModule(Scaffold.class).getOriginalSlot());
            } else if (DewCommon.moduleManager.getModule(AutoPot.class).isEnabled() && DewCommon.moduleManager.getModule(AutoPot.class).getOriginalSlot() != -1) {
                itemstack = IMinecraft.mc.thePlayer.inventory.getStackInSlot(DewCommon.moduleManager.getModule(AutoPot.class).getOriginalSlot());
            } else if (DewCommon.moduleManager.getModule(AutoTool.class).isEnabled() && DewCommon.moduleManager.getModule(AutoTool.class).getOriginalSlot() != -1) {
                itemstack = IMinecraft.mc.thePlayer.inventory.getStackInSlot(DewCommon.moduleManager.getModule(AutoTool.class).getOriginalSlot());
            }
        }

        if (itemstack != null)
        {
            GlStateManager.pushMatrix();

            if (this.livingEntityRenderer.getMainModel().isChild)
            {
                float f = 0.5F;
                GlStateManager.translate(0.0F, 0.625F, 0.0F);
                GlStateManager.rotate(-20.0F, -1.0F, 0.0F, 0.0F);
                GlStateManager.scale(f, f, f);
            }

            final UUID uuid = entitylivingbaseIn.getUniqueID();
            final EntityPlayer entityplayer = IMinecraft.mc.theWorld.getPlayerEntityByUUID(uuid);

            boolean shouldSwordBlock = DewCommon.moduleManager.getModule(ItemAnimations.class).isEnabled() && DewCommon.moduleManager.getModule(ItemAnimations.class).getOldAnimations().isSelected("Third Person") && entityplayer != null && (entityplayer.isBlocking() || entityplayer instanceof EntityPlayerSP && (itemstack.getItem() instanceof ItemSword && DewCommon.moduleManager.getModule(ItemAnimations.class).isVisualBlocking() || DewCommon.moduleManager.getModule(ItemAnimations.class).shouldForceBlock(entityplayer)));

            if (shouldSwordBlock) {
                if (entitylivingbaseIn.isSneaking()) {
                    ((ModelBiped)this.livingEntityRenderer.getMainModel()).postRenderArm(0.0325F);
                    translate(-0.58F, 0.3F, -0.2F);
                    rotate(-24390f, 137290f, -2009900f, -2054900f);
                } else {
                    ((ModelBiped)this.livingEntityRenderer.getMainModel()).postRenderArm(0.0325F);
                    translate(-0.48F, 0.2F, -0.2F);
                    rotate(-24390f, 137290f, -2009900f, -2054900f);
                }
            } else {
                ((ModelBiped)this.livingEntityRenderer.getMainModel()).postRenderArm(0.0625F);
            }

            GlStateManager.translate(-0.0625F, 0.4375F, 0.0625F);

            if (entitylivingbaseIn instanceof EntityPlayer && ((EntityPlayer)entitylivingbaseIn).fishEntity != null)
            {
                itemstack = new ItemStack(Items.fishing_rod, 0);
            }

            Item item = itemstack.getItem();
            Minecraft minecraft = Minecraft.getMinecraft();

            if (item instanceof ItemBlock && Block.getBlockFromItem(item).getRenderType() == 2)
            {
                GlStateManager.translate(0.0F, 0.1875F, -0.3125F);
                GlStateManager.rotate(20.0F, 1.0F, 0.0F, 0.0F);
                GlStateManager.rotate(45.0F, 0.0F, 1.0F, 0.0F);
                float f1 = 0.375F;
                GlStateManager.scale(-f1, -f1, f1);
            }

            if (entitylivingbaseIn.isSneaking())
            {
                GlStateManager.translate(0.0F, 0.203125F, 0.0F);
            }

            if (DewCommon.moduleManager.getModule(ItemAnimations.class).isEnabled() && DewCommon.moduleManager.getModule(ItemAnimations.class).getOldAnimations().isSelected("Third Person") && !IMinecraft.mc.getRenderItem().shouldRenderItemIn3D(itemstack) && !(itemstack.getItem() instanceof ItemSkull || itemstack.getItem() instanceof ItemBanner || itemstack.getItem() instanceof ItemFishingRod || itemstack.getItem() instanceof ItemCarrotOnAStick || shouldSwordBlock)) {
                scale = 1.5F * 0.625F;
                if (itemstack.getItem() instanceof ItemBow) {
                    GlStateManager.rotate(-12.0F, 0.0f, 1.0f, 0.0f);
                    GlStateManager.rotate(-7.0F, 1.0f, 0.0f, 0.0f);
                    GlStateManager.rotate(10.0F, 0.0f, 0.0f, 1.0f);
                    GlStateManager.rotate(1.0F, 0.0f, 1.0f, 0.0f);
                    GlStateManager.rotate(-4.5F, 1.0f, 0.0f, 0.0f);
                    GlStateManager.rotate(-1.5F, 0.0f, 0.0f, 1.0f);
                    GlStateManager.translate(0.022F, -0.01F, -0.108F);
                    GlStateManager.scale(scale, scale, scale);
                } else if (itemstack.getItem().isFull3D()) {
                    if (entitylivingbaseIn.getHeldItem().getItem().shouldRotateAroundWhenRendering()) {
                        GlStateManager.rotate(180.0F, 0.0F, 0.0F, 1.0F);
                    }
                    GlStateManager.scale(scale / 0.85F, scale / 0.85F, scale / 0.85F);
                    GlStateManager.rotate(-2.4F, 0.0F, 1.0F, 0.0F);
                    GlStateManager.rotate(-20.0F, 1.0F, 0.0F, 0.0F);
                    GlStateManager.rotate(4.5F, 0.0F, 0.0F, 1.0F);
                    GlStateManager.translate(-0.013F, 0.01F, 0.125F);
                } else {
                    scale = 1.5F * 0.375F;
                    GlStateManager.scale(scale / 0.55, scale / 0.55, scale / 0.55);
                    GlStateManager.rotate(-195.0F, 0.0F, 1.0F, 0.0F);
                    GlStateManager.rotate(-168.0F, 1.0F, 0.0F, 0.0F);
                    GlStateManager.rotate(15.0F, 0.0F, 0.0F, 1.0F);
                    GlStateManager.translate(-0.047F, -0.28F, 0.038F);
                }
            }

            minecraft.getItemRenderer().renderItem(entitylivingbaseIn, itemstack, ItemCameraTransforms.TransformType.THIRD_PERSON);
            GlStateManager.popMatrix();
        }
    }

    public boolean shouldCombineTextures()
    {
        return false;
    }
}
