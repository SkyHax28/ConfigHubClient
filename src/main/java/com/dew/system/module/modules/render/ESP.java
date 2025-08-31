package com.dew.system.module.modules.render;

import com.dew.system.event.events.Render2DEvent;
import com.dew.system.event.events.WorldLoadEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.settingsvalue.SelectionValue;
import com.dew.utils.shader.FramebufferShader;
import com.dew.utils.shader.GlowShader;
import com.dew.utils.shader.OutlineShader;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.client.renderer.culling.Frustum;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.input.Keyboard;

import java.awt.*;

public class ESP extends Module {

    private static final SelectionValue mode = new SelectionValue("Mode", "Glow", "Glow", "Outline");
    private boolean renderNametagAndEnchantmentGlint = true;
    private final ICamera frustum = new Frustum();
    public ESP() {
        super("ESP", ModuleCategory.RENDER, Keyboard.KEY_NONE, false, false, true);
    }

    private boolean shouldRender(Entity entity) {
        return entity instanceof EntityPlayer && (!(entity instanceof EntityPlayerSP) || mc.gameSettings.thirdPersonView != 0);
    }

    public boolean isRenderNametagAndEnchantmentGlint() {
        return this.renderNametagAndEnchantmentGlint;
    }

    @Override
    public void onDisable() {
        renderNametagAndEnchantmentGlint = true;
    }

    @Override
    public void onLoadWorld(WorldLoadEvent event) {
        renderNametagAndEnchantmentGlint = true;
    }

    @Override
    public void onRender2D(Render2DEvent event) {
        final boolean glow = "Glow".equals(mode.get());
        final FramebufferShader shader = glow ? GlowShader.GLOW_SHADER : OutlineShader.OUTLINE_SHADER;

        shader.startDraw(event.partialTicks);

        renderNametagAndEnchantmentGlint = false;
        try {
            Entity view = mc.getRenderViewEntity();
            if (view != null) {
                double x = view.lastTickPosX + (view.posX - view.lastTickPosX) * mc.timer.renderPartialTicks;
                double y = view.lastTickPosY + (view.posY - view.lastTickPosY) * mc.timer.renderPartialTicks;
                double z = view.lastTickPosZ + (view.posZ - view.lastTickPosZ) * mc.timer.renderPartialTicks;
                frustum.setPosition(x, y, z);
            }

            for (EntityPlayer player : mc.theWorld.playerEntities) {
                if (shouldRender(player) && frustum.isBoundingBoxInFrustum(player.getEntityBoundingBox())) {
                    mc.getRenderManager().renderEntityStatic(player, mc.timer.renderPartialTicks, true);
                }
            }
        } finally {
            renderNametagAndEnchantmentGlint = true;
        }

        // 各パラメータを事前に決定
        float radius = glow ? 3.5f : 1.5f;
        float intensity = glow ? 0.4f : 0.8f;

        shader.stopDraw(Color.WHITE, radius, intensity);
    }
}