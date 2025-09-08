package com.dew.system.module.modules.render;

import com.dew.DewCommon;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.input.Keyboard;

public class NameTags extends Module {

    public NameTags() {
        super("Name Tags", ModuleCategory.RENDER, Keyboard.KEY_NONE, false, false, true);
    }

    public boolean shouldRender(Entity entity) {
        ESP ESPModule = DewCommon.moduleManager.getModule(ESP.class);
        return (entity instanceof EntityPlayer || entity instanceof EntityItem) && !(entity instanceof EntityPlayerSP) && (!ESPModule.isEnabled() || ESPModule.isRenderNametagAndEnchantmentGlint());
    }
}