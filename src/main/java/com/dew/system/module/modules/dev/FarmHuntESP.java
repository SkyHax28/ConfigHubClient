package com.dew.system.module.modules.dev;

import com.dew.system.event.events.LoadWorldEvent;
import com.dew.system.event.events.Render2DEvent;
import com.dew.system.event.events.Render3DEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.RenderGlobal;
import net.minecraft.entity.Entity;
import net.minecraft.entity.passive.EntityAnimal;
import org.lwjgl.input.Keyboard;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class FarmHuntESP extends Module {

    private final Map<UUID, double[]> spawnPositions = new HashMap<>();
    public FarmHuntESP() {
        super("Farm Hunt ESP", ModuleCategory.VISUAL, Keyboard.KEY_NONE, false, true, true);
    }

    @Override
    public void onDisable() {
        spawnPositions.clear();
    }

    @Override
    public void onLoadWorld(LoadWorldEvent event) {
        spawnPositions.clear();
    }

    @Override
    public void onRender2D(Render2DEvent event) {

    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (mc.theWorld == null || mc.thePlayer == null) return;

        final double DIST_THRESHOLD = 20.0D;

        for (Entity entity : mc.theWorld.loadedEntityList) {
            if (!(entity instanceof EntityAnimal)) continue;

            UUID id = entity.getUniqueID();

            spawnPositions.putIfAbsent(id, new double[]{entity.posX, entity.posY, entity.posZ});

            double[] origin = spawnPositions.get(id);
            if (origin == null) continue;

            double dx = entity.posX - origin[0];
            double dy = entity.posY - origin[1];
            double dz = entity.posZ - origin[2];
            double dist = Math.sqrt(dx * dx + dy * dy + dz * dz);

            if (dist >= DIST_THRESHOLD) {
                drawEntityBox(entity, 1.0f, 0.0f, 0.0f, event.partialTicks);
            }
        }
    }

    private void drawEntityBox(Entity entity, float r, float g, float b, float partialTicks) {
        double x = entity.lastTickPosX + (entity.posX - entity.lastTickPosX) * partialTicks - mc.getRenderManager().viewerPosX;
        double y = entity.lastTickPosY + (entity.posY - entity.lastTickPosY) * partialTicks - mc.getRenderManager().viewerPosY;
        double z = entity.lastTickPosZ + (entity.posZ - entity.lastTickPosZ) * partialTicks - mc.getRenderManager().viewerPosZ;

        GlStateManager.pushMatrix();
        GlStateManager.disableTexture2D();
        GlStateManager.enableBlend();
        GlStateManager.disableDepth();
        GlStateManager.tryBlendFuncSeparate(770, 771, 1, 0);
        GlStateManager.color(r, g, b, 0.5f);

        RenderGlobal.drawOutlinedBoundingBox(entity.getEntityBoundingBox().offset(-mc.getRenderManager().viewerPosX,
                -mc.getRenderManager().viewerPosY,
                -mc.getRenderManager().viewerPosZ), (int)(r*255), (int)(g*255), (int)(b*255), 255);

        GlStateManager.enableDepth();
        GlStateManager.disableBlend();
        GlStateManager.enableTexture2D();
        GlStateManager.popMatrix();
    }
}