package com.dew.utils;

import com.dew.IMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

public class WingsManager extends ModelBase {

    private final Minecraft mc = IMinecraft.mc;
    private final ResourceLocation wingTexture;

    private final ModelRenderer leftWing;
    private final ModelRenderer leftWingTip;
    private final ModelRenderer rightWing;
    private final ModelRenderer rightWingTip;

    public WingsManager() {
        this.setTextureOffset("wing.bone", 0, 0);
        this.setTextureOffset("wing.skin", -10, 8);
        this.setTextureOffset("wingtip.bone", 0, 5);
        this.setTextureOffset("wingtip.skin", -10, 18);

        leftWing = new ModelRenderer(this, "wing");
        leftWing.setTextureSize(30, 30);
        leftWing.setRotationPoint(2.0F, 0.0F, 0.0F);
        leftWing.mirror = true;
        leftWing.addBox("bone", 0.0F, -1.0F, -1.0F, 10, 2, 2);
        leftWing.addBox("skin", 0.0F, 0.0F, 0.5F, 10, 0, 10);

        leftWingTip = new ModelRenderer(this, "wingtip");
        leftWingTip.setTextureSize(30, 30);
        leftWingTip.setRotationPoint(10.0F, 0.0F, 0.0F);
        leftWingTip.mirror = true;
        leftWingTip.addBox("bone", 0.0F, -0.5F, -0.5F, 10, 1, 1);
        leftWingTip.addBox("skin", 0.0F, 0.0F, 0.5F, 10, 0, 10);
        leftWing.addChild(leftWingTip);

        rightWing = new ModelRenderer(this, "wing");
        rightWing.setTextureSize(30, 30);
        rightWing.setRotationPoint(-2.0F, 0.0F, 0.0F);
        rightWing.addBox("bone", -10.0F, -1.0F, -1.0F, 10, 2, 2);
        rightWing.addBox("skin", -10.0F, 0.0F, 0.5F, 10, 0, 10);

        rightWingTip = new ModelRenderer(this, "wingtip");
        rightWingTip.setTextureSize(30, 30);
        rightWingTip.setRotationPoint(-10.0F, 0.0F, 0.0F);
        rightWingTip.addBox("bone", -10.0F, -0.5F, -0.5F, 10, 1, 1);
        rightWingTip.addBox("skin", -10.0F, 0.0F, 0.5F, 10, 0, 10);
        rightWing.addChild(rightWingTip);

        wingTexture = new ResourceLocation("minecraft", "dew/wing.png");
    }

    public void renderWings(float partialTicks) {
        double scale = 1.0;
        double rotation = interpolate(
                mc.thePlayer.prevRenderYawOffset,
                mc.thePlayer.renderYawOffset,
                partialTicks
        );

        GL11.glPushMatrix();

        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);

        GL11.glScaled(-scale, -scale, scale);
        GL11.glRotated(180.0 + rotation, 0.0, 1.0, 0.0);
        GL11.glTranslated(0.0, mc.thePlayer.isSneaking() ? -1.325 / scale : -1.45 / scale, 0.0);
        GL11.glTranslated(0.0, 0.0, 0.2 / scale);

        GL11.glColor3f(1F, 1F, 1F);
        mc.getTextureManager().bindTexture(wingTexture);

        applyWingRotations(partialTicks);

        leftWing.render(0.0625F);
        rightWing.render(0.0625F);

        GL11.glDepthMask(true);
        GL11.glEnable(GL11.GL_DEPTH_TEST);

        GL11.glColor3f(1F, 1F, 1F);
        GL11.glPopMatrix();
    }

    private void applyWingRotations(float partialTicks) {
        float time = (mc.thePlayer.ticksExisted + partialTicks) / 5.0F;
        float flap = (float) Math.sin(time) * 0.5F;

        float baseX = (float) Math.toRadians(-15.0);

        double motionSq = mc.thePlayer.motionX * mc.thePlayer.motionX + mc.thePlayer.motionZ * mc.thePlayer.motionZ;
        float moveLerp = Math.min(1.0f, (float) motionSq * 100.0f);
        float baseY = (float) Math.toRadians(Lerper.lerp(25.0f, 25f, moveLerp));

        float tipZ = (float) Math.toRadians(-45.0 + Math.sin(time + 1) * 15.0);

        if (mc.thePlayer.isSneaking()) {
            baseY += (float) Math.toRadians(10.0);
        }

        leftWing.rotateAngleX = baseX + flap * 0.1F;
        leftWing.rotateAngleY = -baseY - flap * 0.05F;
        leftWing.rotateAngleZ = (float) Math.toRadians(-25f);
        leftWingTip.rotateAngleZ = -tipZ;

        rightWing.rotateAngleX = baseX + flap * 0.1F;
        rightWing.rotateAngleY = baseY + flap * 0.05F;
        rightWing.rotateAngleZ = -((float) Math.toRadians(-25f));
        rightWingTip.rotateAngleZ = tipZ;
    }

    private double interpolate(float yaw1, float yaw2, float percent) {
        double result = (yaw1 + (yaw2 - yaw1) * percent) % 360.0;
        return result < 0.0 ? result + 360.0 : result;
    }
}