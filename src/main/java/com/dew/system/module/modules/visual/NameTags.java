package com.dew.system.module.modules.visual;

import com.dew.DewCommon;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.settingsvalue.BooleanValue;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.input.Keyboard;

public class NameTags extends Module {
    private static final BooleanValue droppedItems = new BooleanValue("Dropped Items", false);
    public NameTags() {
        super("Name Tags", ModuleCategory.VISUAL, Keyboard.KEY_NONE, false, false, true);
    }

    public boolean shouldRender(Entity entity) {
        ShaderESP shaderEspModule = DewCommon.moduleManager.getModule(ShaderESP.class);
        return (entity instanceof EntityPlayer || droppedItems.get() && entity instanceof EntityItem) && !(entity instanceof EntityPlayerSP) && (!shaderEspModule.isEnabled() || shaderEspModule.isRenderNametagAndEnchantmentGlint());
    }
}