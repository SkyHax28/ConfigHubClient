package com.dew.system.module.modules.visual;

import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.settingsvalue.MultiSelectionValue;
import org.lwjgl.input.Keyboard;

import java.util.Arrays;

public class Canceller extends Module {

    private static final MultiSelectionValue cancels = new MultiSelectionValue("Cancels", Arrays.asList("Fire Overlay", "Hurt Cam"), "Screen Bobbing", "Scoreboard", "Fire Overlay", "Hurt Cam");
    public Canceller() {
        super("Canceller", ModuleCategory.VISUAL, Keyboard.KEY_NONE, false, false, true);
    }

    public MultiSelectionValue getSelectedCancels() {
        return cancels;
    }
}
