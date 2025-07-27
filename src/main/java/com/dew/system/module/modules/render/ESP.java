package com.dew.system.module.modules.render;

import com.dew.system.event.events.Render2DEvent;
import com.dew.system.event.events.WorldEvent;
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

    private boolean renderNameTags = true;

    private boolean shouldRender(Entity entity) {
        return entity instanceof EntityPlayer && (!(entity instanceof EntityPlayerSP) || mc.gameSettings.thirdPersonView != 0);
    }

    public boolean isRenderNameTags() {
        return this.renderNameTags;
    }

    @Override
    public void onDisable() {
        renderNameTags = true;
    }

    @Override
    public void onWorld(WorldEvent event) {
        renderNameTags = true;
    }

    @Override
    public void onRender2D(Render2DEvent event) {
        final FramebufferShader shader = mode.get().equals("Glow") ? GlowShader.GLOW_SHADER : OutlineShader.OUTLINE_SHADER;

        shader.startDraw(event.partialTicks);

        renderNameTags = false;

        try {
            for (final Entity entity : mc.theWorld.loadedEntityList) {
                if (!this.shouldRender(entity)) continue;
                mc.getRenderManager().renderEntityStatic(entity, mc.timer.renderPartialTicks, true);
            }
        } catch (final Exception ignored) {
        }

        renderNameTags = true;

        shader.stopDraw(this.getRainbowColor(3f, 0f, 0.6f, 0.9f, 255), mode.get().equals("Glow") ? 3.5f : 1.5f, mode.get().equals("Glow") ? 0.4f : 0.8f);
    }

    private Color getRainbowColor(float speed, float offset, float saturation, float brightness, int alpha) {
        float hue = ((System.currentTimeMillis() % (int)(speed * 1000)) / (speed * 1000)) + offset;
        hue %= 1.0f;
        Color baseColor = Color.getHSBColor(hue, saturation, brightness);
        return new Color(baseColor.getRed(), baseColor.getGreen(), baseColor.getBlue(), alpha);
    }
}