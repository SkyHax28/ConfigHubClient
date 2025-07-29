package com.dew.utils.shader;

import com.dew.IMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.shader.Framebuffer;

import java.awt.*;

import static org.lwjgl.opengl.GL11.*;
import static org.lwjgl.opengl.GL20.glUseProgram;

public abstract class FramebufferShader extends Shader {

    public static final Minecraft mc = IMinecraft.mc;

    private static Framebuffer framebuffer;

    protected float red, green, blue, alpha = 1F;
    protected float radius = 2F;
    protected float quality = 1F;

    private boolean entityShadows;

    public FramebufferShader(final String fragmentShader) {
        super(fragmentShader);
    }

    public void startDraw(final float partialTicks) {
        GlStateManager.enableAlpha();
        GlStateManager.pushMatrix();

        framebuffer = setupFrameBuffer(framebuffer);
        framebuffer.framebufferClear();
        framebuffer.bindFramebuffer(true);

        entityShadows = mc.gameSettings.entityShadows;
        mc.gameSettings.entityShadows = false;

        mc.entityRenderer.setupCameraTransform(partialTicks, 0);
    }

    public void stopDraw(final Color color, final float radius, final float quality) {
        mc.gameSettings.entityShadows = entityShadows;
        glEnable(GL_BLEND);
        glBlendFunc(GL_SRC_ALPHA, GL_ONE_MINUS_SRC_ALPHA);
        mc.getFramebuffer().bindFramebuffer(true);

        red = color.getRed() / 255F;
        green = color.getGreen() / 255F;
        blue = color.getBlue() / 255F;
        alpha = color.getAlpha() / 255F;
        this.radius = radius;
        this.quality = quality;

        mc.entityRenderer.disableLightmap();
        RenderHelper.disableStandardItemLighting();

        startShader();
        mc.entityRenderer.setupOverlayRendering();
        drawFramebuffer(framebuffer);
        stopShader();

        mc.entityRenderer.disableLightmap();

        GlStateManager.popMatrix();
        GlStateManager.popAttrib();
    }

    public Framebuffer setupFrameBuffer(Framebuffer frameBuffer) {
        if (frameBuffer == null || frameBuffer.framebufferWidth != mc.displayWidth || frameBuffer.framebufferHeight != mc.displayHeight) {
            if (frameBuffer != null) {
                frameBuffer.deleteFramebuffer();
            }
            frameBuffer = new Framebuffer(mc.displayWidth, mc.displayHeight, true);
        }
        return frameBuffer;
    }

    public void drawFramebuffer(final Framebuffer framebuffer) {
        ScaledResolution scaledResolution = new ScaledResolution(mc);
        int width = scaledResolution.getScaledWidth();
        int height = scaledResolution.getScaledHeight();

        glBindTexture(GL_TEXTURE_2D, framebuffer.framebufferTexture);
        glBegin(GL_QUADS);
        glTexCoord2f(0, 1); glVertex2f(0, 0);
        glTexCoord2f(0, 0); glVertex2f(0, height);
        glTexCoord2f(1, 0); glVertex2f(width, height);
        glTexCoord2f(1, 1); glVertex2f(width, 0);
        glEnd();
    }
}