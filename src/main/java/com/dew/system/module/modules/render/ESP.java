package com.dew.system.module.modules.render;

import com.dew.system.event.events.Render2DEvent;
import com.dew.system.event.events.WorldEvent;
import com.dew.system.event.events.WorldLoadEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.settingsvalue.SelectionValue;
import com.dew.utils.shader.FramebufferShader;
import com.dew.utils.shader.GlowShader;
import com.dew.utils.shader.OutlineShader;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.input.Keyboard;

import java.awt.*;

public class ESP extends Module {

    public ESP() {
        super("ESP", ModuleCategory.RENDER, Keyboard.KEY_NONE, false, false, true);
    }

    private static final SelectionValue mode = new SelectionValue("Mode", "Glow", "Glow", "Outline");

    private boolean renderNametagAndEnchantmentGlint = true;

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
        final FramebufferShader shader = mode.get().equals("Glow") ? GlowShader.GLOW_SHADER : OutlineShader.OUTLINE_SHADER;

        shader.startDraw(event.partialTicks);

        renderNametagAndEnchantmentGlint = false;

        try {
            for (final Entity entity : mc.theWorld.loadedEntityList) {
                if (!this.shouldRender(entity)) continue;
                mc.getRenderManager().renderEntityStatic(entity, mc.timer.renderPartialTicks, true);
            }
        } catch (final Exception ignored) {
        }

        renderNametagAndEnchantmentGlint = true;

        shader.stopDraw(Color.WHITE, mode.get().equals("Glow") ? 3.5f : 1.5f, mode.get().equals("Glow") ? 0.4f : 0.8f);
    }
}