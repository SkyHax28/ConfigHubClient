package com.dew.system.module.modules.render;

import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.input.Keyboard;

public class Chams extends Module {

    public Chams() {
        super("Chams", ModuleCategory.RENDER, Keyboard.KEY_NONE, false, false, true);
    }

    public boolean shouldRender(Entity entity) {
        return entity instanceof EntityPlayer;
    }
}