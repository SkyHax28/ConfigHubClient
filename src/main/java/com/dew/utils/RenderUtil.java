package com.dew.utils;

import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.WorldRenderer;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.AxisAlignedBB;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class RenderUtil {

    public static void glColor(int red, int green, int blue, int alpha) {
        GL11.glColor4f(red / 255f, green / 255f, blue / 255f, alpha / 255f);
    }

    public static void glColor(Color color) {
        glColor(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha());
    }

    public Color getRainbowColor(float speed, float offset, float saturation, float brightness, int alpha) {
        float hue = ((System.currentTimeMillis() % (int)(speed * 1000)) / (speed * 1000)) + offset;
        hue %= 1.0f;
        Color baseColor = Color.getHSBColor(hue, saturation, brightness);
        return new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), alpha);
    }

    public static void drawFilledBox(AxisAlignedBB bb, float r, float g, float b, float a) {
        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer wr = tessellator.getWorldRenderer();
        wr.begin(GL11.GL_QUADS, DefaultVertexFormats.POSITION_COLOR);

        wr.pos(bb.minX, bb.minY, bb.minZ).color(r, g, b, a).endVertex();
        wr.pos(bb.maxX, bb.minY, bb.minZ).color(r, g, b, a).endVertex();
        wr.pos(bb.maxX, bb.minY, bb.maxZ).color(r, g, b, a).endVertex();
        wr.pos(bb.minX, bb.minY, bb.maxZ).color(r, g, b, a).endVertex();

        wr.pos(bb.minX, bb.maxY, bb.minZ).color(r, g, b, a).endVertex();
        wr.pos(bb.maxX, bb.maxY, bb.minZ).color(r, g, b, a).endVertex();
        wr.pos(bb.maxX, bb.maxY, bb.maxZ).color(r, g, b, a).endVertex();
        wr.pos(bb.minX, bb.maxY, bb.maxZ).color(r, g, b, a).endVertex();

        wr.pos(bb.minX, bb.minY, bb.minZ).color(r, g, b, a).endVertex();
        wr.pos(bb.minX, bb.maxY, bb.minZ).color(r, g, b, a).endVertex();
        wr.pos(bb.maxX, bb.maxY, bb.minZ).color(r, g, b, a).endVertex();
        wr.pos(bb.maxX, bb.minY, bb.minZ).color(r, g, b, a).endVertex();

        wr.pos(bb.maxX, bb.minY, bb.minZ).color(r, g, b, a).endVertex();
        wr.pos(bb.maxX, bb.maxY, bb.minZ).color(r, g, b, a).endVertex();
        wr.pos(bb.maxX, bb.maxY, bb.maxZ).color(r, g, b, a).endVertex();
        wr.pos(bb.maxX, bb.minY, bb.maxZ).color(r, g, b, a).endVertex();

        wr.pos(bb.maxX, bb.minY, bb.maxZ).color(r, g, b, a).endVertex();
        wr.pos(bb.maxX, bb.maxY, bb.maxZ).color(r, g, b, a).endVertex();
        wr.pos(bb.minX, bb.maxY, bb.maxZ).color(r, g, b, a).endVertex();
        wr.pos(bb.minX, bb.minY, bb.maxZ).color(r, g, b, a).endVertex();

        wr.pos(bb.minX, bb.minY, bb.maxZ).color(r, g, b, a).endVertex();
        wr.pos(bb.minX, bb.maxY, bb.maxZ).color(r, g, b, a).endVertex();
        wr.pos(bb.minX, bb.maxY, bb.minZ).color(r, g, b, a).endVertex();
        wr.pos(bb.minX, bb.minY, bb.minZ).color(r, g, b, a).endVertex();

        tessellator.draw();
    }

    public static void drawOutlinedBoundingBox(AxisAlignedBB bb, float red, float green, float blue, float alpha) {
        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.disableLighting();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);
        GlStateManager.color(red, green, blue, alpha);

        Tessellator tessellator = Tessellator.getInstance();
        WorldRenderer renderer = tessellator.getWorldRenderer();
        renderer.begin(GL11.GL_LINES, DefaultVertexFormats.POSITION);

        addBoundingBoxLines(renderer, bb);

        tessellator.draw();

        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.enableLighting();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private static void addBoundingBoxLines(WorldRenderer renderer, AxisAlignedBB bb) {
        double minX = bb.minX, minY = bb.minY, minZ = bb.minZ;
        double maxX = bb.maxX, maxY = bb.maxY, maxZ = bb.maxZ;

        renderer.pos(minX, minY, minZ).endVertex();
        renderer.pos(maxX, minY, minZ).endVertex();

        renderer.pos(maxX, minY, minZ).endVertex();
        renderer.pos(maxX, minY, maxZ).endVertex();

        renderer.pos(maxX, minY, maxZ).endVertex();
        renderer.pos(minX, minY, maxZ).endVertex();

        renderer.pos(minX, minY, maxZ).endVertex();
        renderer.pos(minX, minY, minZ).endVertex();

        renderer.pos(minX, maxY, minZ).endVertex();
        renderer.pos(maxX, maxY, minZ).endVertex();

        renderer.pos(maxX, maxY, minZ).endVertex();
        renderer.pos(maxX, maxY, maxZ).endVertex();

        renderer.pos(maxX, maxY, maxZ).endVertex();
        renderer.pos(minX, maxY, maxZ).endVertex();

        renderer.pos(minX, maxY, maxZ).endVertex();
        renderer.pos(minX, maxY, minZ).endVertex();

        renderer.pos(minX, minY, minZ).endVertex();
        renderer.pos(minX, maxY, minZ).endVertex();

        renderer.pos(maxX, minY, minZ).endVertex();
        renderer.pos(maxX, maxY, minZ).endVertex();

        renderer.pos(maxX, minY, maxZ).endVertex();
        renderer.pos(maxX, maxY, maxZ).endVertex();

        renderer.pos(minX, minY, maxZ).endVertex();
        renderer.pos(minX, maxY, maxZ).endVertex();
    }
}
