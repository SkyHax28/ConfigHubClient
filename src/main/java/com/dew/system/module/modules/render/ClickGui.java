package com.dew.system.module.modules.render;

import com.dew.system.event.events.KeyboardEvent;
import com.dew.system.gui.ClickGuiScreen;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.settingsvalue.SelectionValue;
import org.lwjgl.input.Keyboard;

public class ClickGui extends Module {

    public ClickGui() {
        super("Click Gui", ModuleCategory.RENDER, Keyboard.KEY_RSHIFT, false, false, false);
    }
}
