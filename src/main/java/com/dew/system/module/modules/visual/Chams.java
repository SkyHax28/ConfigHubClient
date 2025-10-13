package com.dew.system.module.modules.visual;

import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.settingsvalue.BooleanValue;
import net.minecraft.client.entity.EntityPlayerSP;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.input.Keyboard;

public class Chams extends Module {

    private static final BooleanValue tileEntities = new BooleanValue("Tile Entities", false);
    public Chams() {
        super("Chams", ModuleCategory.VISUAL, Keyboard.KEY_NONE, false, false, true);
    }

    public boolean shouldRender(Entity entity) {
        return entity instanceof EntityPlayer && (!(entity instanceof EntityPlayerSP) || mc.gameSettings.thirdPersonView != 0);
    }

    public boolean doRenderTileEntities() {
        return tileEntities.get();
    }
}