package com.dew.system.module.modules.movement;

import com.dew.system.gui.ClickGuiScreen;
import com.dew.system.gui.NewClickGuiScreen;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.settingsvalue.SelectionValue;
import net.minecraft.client.gui.GuiChat;
import net.minecraft.client.gui.inventory.GuiChest;
import net.minecraft.client.gui.inventory.GuiContainer;
import org.lwjgl.input.Keyboard;

public class InvMove extends Module {

    private static final SelectionValue mode = new SelectionValue("Mode", "Normal", "Normal", "No Chests", "Save Keys");

    public InvMove() {
        super("Inv Move", ModuleCategory.MOVEMENT, Keyboard.KEY_NONE, false, true, true);
    }

    @Override
    public String tag() {
        return mode.get();
    }

    public boolean canMoveFreely() {
        return mc.currentScreen instanceof ClickGuiScreen || mc.currentScreen instanceof NewClickGuiScreen || this.isEnabled() && !(mc.currentScreen instanceof GuiChat) && (!mode.get().equals("No Chests") || !(mc.currentScreen instanceof GuiChest)) && (!mode.get().equals("Save Keys") || !(mc.currentScreen instanceof GuiContainer));
    }
}
