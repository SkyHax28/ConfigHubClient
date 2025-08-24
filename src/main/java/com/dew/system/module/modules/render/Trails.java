package com.dew.system.module.modules.render;

import com.dew.system.event.events.Render3DEvent;
import com.dew.system.event.events.WorldEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.utils.Lerper;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.util.Vec3;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.util.Iterator;
import java.util.LinkedList;

public class Trails extends Module {

    private final LinkedList<Point> positions = new LinkedList<>();
    private static final long MAX_DURATION_MS = 1000;
    public Trails() {
        super("Trails", ModuleCategory.RENDER, Keyboard.KEY_NONE, false, false, true);
    }

    @Override
    public void onDisable() {
        positions.clear();
    }

    @Override
    public void onWorld(WorldEvent event) {
        positions.clear();
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        double bbMinYPrev = mc.thePlayer.prevPosY + (mc.thePlayer.getEntityBoundingBox().minY - mc.thePlayer.posY);
        double bbMinYNow = mc.thePlayer.getEntityBoundingBox().minY;

        Vec3 pos = new Vec3(
                Lerper.lerpDouble(mc.thePlayer.prevPosX, mc.thePlayer.posX, event.partialTicks),
                Lerper.lerpDouble(bbMinYPrev, bbMinYNow, event.partialTicks),
                Lerper.lerpDouble(mc.thePlayer.prevPosZ, mc.thePlayer.posZ, event.partialTicks)
        );

        Point last = positions.isEmpty() ? null : positions.getLast();
        if (last == null || last.x != pos.xCoord || last.y != pos.yCoord || last.z != pos.zCoord) {
            positions.add(new Point(pos.xCoord, pos.yCoord, pos.zCoord));
        }

        long now = System.currentTimeMillis();
        Iterator<Point> it = positions.iterator();
        while (it.hasNext()) {
            Point p = it.next();
            long elapsed = now - p.createTimeMillis;
            if (elapsed > MAX_DURATION_MS) {
                it.remove();
            }
        }

        GlStateManager.pushMatrix();
        GL11.glPushAttrib(GL11.GL_ALL_ATTRIB_BITS);

        mc.entityRenderer.disableLightmap();
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glLineWidth(2.0f);

        GL11.glBegin(GL11.GL_LINE_STRIP);

        double renderX = mc.getRenderManager().viewerPosX;
        double renderY = mc.getRenderManager().viewerPosY;
        double renderZ = mc.getRenderManager().viewerPosZ;

        for (Point p : positions) {
            long elapsed = now - p.createTimeMillis;
            float alpha = 0.9f - (float) elapsed / MAX_DURATION_MS * 0.9f;
            if (alpha < 0f) alpha = 0f;
            GL11.glColor4f(1f, 1f, 1f, alpha);
            GL11.glVertex3d(p.x - renderX, p.y - renderY + 0.01, p.z - renderZ);
        }

        GL11.glEnd();

        GL11.glPopAttrib();
        GlStateManager.popMatrix();
    }

    private static class Point {
        final double x, y, z;
        final long createTimeMillis;
        Point(double x, double y, double z) {
            this.x = x; this.y = y; this.z = z;
            this.createTimeMillis = System.currentTimeMillis();
        }
    }
}