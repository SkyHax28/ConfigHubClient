package com.dew.system.module.modules.combat;

import com.dew.DewCommon;
import com.dew.system.event.events.*;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.settingsvalue.NumberValue;
import com.dew.utils.RenderUtil;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import org.lwjgl.input.Keyboard;
import org.lwjgl.opengl.GL11;

import java.util.*;

public class Backtrack extends Module {

    public Backtrack() {
        super("Backtrack", ModuleCategory.COMBAT, Keyboard.KEY_NONE, true, false, true);
    }

    public static final NumberValue ticksBack = new NumberValue("Ticks Back", 5.0, 1.0, 10.0, 1.0);

    private final Map<UUID, Deque<Vec3>> backtrackPositions = new HashMap<>();

    @Override
    public void onDisable() {
        backtrackPositions.clear();
    }

    @Override
    public void onWorld(WorldEvent event) {
        backtrackPositions.clear();
    }

    @Override
    public void onPreUpdate(PreUpdateEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null) return;

        for (EntityPlayer player : mc.theWorld.playerEntities) {
            if (player == mc.thePlayer || player.isDead) continue;

            Deque<Vec3> history = backtrackPositions.computeIfAbsent(player.getUniqueID(), k -> new LinkedList<>());

            history.addFirst(new Vec3(player.posX, player.posY, player.posZ));
            if (history.size() > ticksBack.get()) {
                history.removeLast();
            }
        }
    }

    @Override
    public void onRender3D(Render3DEvent event) {
        if (mc.thePlayer == null || mc.theWorld == null || !DewCommon.moduleManager.getModule(Aura.class).isEnabled() || DewCommon.moduleManager.getModule(Aura.class).target == null) return;

        double renderX = mc.getRenderManager().viewerPosX;
        double renderY = mc.getRenderManager().viewerPosY;
        double renderZ = mc.getRenderManager().viewerPosZ;

        GlStateManager.pushMatrix();
        GlStateManager.enableBlend();
        GlStateManager.disableTexture2D();
        GlStateManager.disableDepth();
        GlStateManager.depthMask(false);
        GlStateManager.disableLighting();
        GlStateManager.disableCull();
        GlStateManager.tryBlendFuncSeparate(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA, 1, 0);

        for (EntityPlayer player : mc.theWorld.playerEntities) {
            if (player == mc.thePlayer || player.isDead || DewCommon.moduleManager.getModule(Aura.class).target != player) continue;

            Deque<Vec3> history = backtrackPositions.get(player.getUniqueID());
            if (history == null || history.size() < 2) continue;

            LinkedList<Vec3> list = (LinkedList<Vec3>) history;

            Vec3 secondOldest = list.get(list.size() - 2);
            Vec3 oldest = list.get(list.size() - 1);

            Vec3 interpolated = interpolate(oldest, secondOldest, event.partialTicks);
            Vec3 renderVec = new Vec3(interpolated.xCoord - renderX, interpolated.yCoord - renderY, interpolated.zCoord - renderZ);

            AxisAlignedBB bb = player.getEntityBoundingBox().offset(renderVec.xCoord - player.posX, renderVec.yCoord - player.posY, renderVec.zCoord - player.posZ);

            RenderUtil.drawFilledBox(bb, player.hurtTime >= 3 ? 1f : 0f, player.hurtTime >= 3 ? 0f : 1f, 0f, 0.2f);
        }

        GlStateManager.enableCull();
        GlStateManager.depthMask(true);
        GlStateManager.enableDepth();
        GlStateManager.enableTexture2D();
        GlStateManager.disableBlend();
        GlStateManager.popMatrix();
    }

    private Vec3 interpolate(Vec3 prev, Vec3 next, float partialTicks) {
        double x = prev.xCoord + (next.xCoord - prev.xCoord) * partialTicks;
        double y = prev.yCoord + (next.yCoord - prev.yCoord) * partialTicks;
        double z = prev.zCoord + (next.zCoord - prev.zCoord) * partialTicks;
        return new Vec3(x, y, z);
    }
}
