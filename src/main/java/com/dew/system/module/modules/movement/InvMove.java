package com.dew.system.module.modules.movement;

import com.dew.system.gui.ClickGuiScreen;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.settingsvalue.BooleanValue;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.inventory.GuiChest;
import org.lwjgl.input.Keyboard;

public class InvMove extends Module {

    public InvMove() {
        super("Inv Move", ModuleCategory.MOVEMENT, Keyboard.KEY_NONE, false, true, true);
    }

    private static final BooleanValue noChests = new BooleanValue("No Chests", false);

    public boolean canMoveFreely() {
        return mc.currentScreen instanceof ClickGuiScreen || this.isEnabled() && !(mc.currentScreen instanceof GuiChat) && (!noChests.get() || !(mc.currentScreen instanceof GuiChest));
    }
}
