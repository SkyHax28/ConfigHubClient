package com.dew.system.module.modules.render;

import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.settingsvalue.MultiSelectionValue;
import com.dew.system.settingsvalue.SelectionValue;
import org.lwjgl.input.Keyboard;

import java.util.Arrays;

public class Cape extends Module {

    public Cape() {
        super("Cape", ModuleCategory.RENDER, Keyboard.KEY_NONE, true, false, true);
    }
}
