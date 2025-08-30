package com.dew.system.module.modules.render;

import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.settingsvalue.BooleanValue;
import com.dew.system.settingsvalue.MultiSelectionValue;
import com.dew.system.settingsvalue.NumberValue;
import com.dew.system.settingsvalue.SelectionValue;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.input.Keyboard;

import java.util.Arrays;

public class NoRender extends Module {

    private static final MultiSelectionValue cancels = new MultiSelectionValue("Cancels", Arrays.asList("Fire Overlay"), "Screen Bobbing", "Scoreboard", "Fire Overlay", "Hurt Cam", "ThirdPerson Crosshair");
    public NoRender() {
        super("No Render", ModuleCategory.RENDER, Keyboard.KEY_NONE, false, false, true);
    }

    public MultiSelectionValue getSelectedCancels() {
        return cancels;
    }
}
