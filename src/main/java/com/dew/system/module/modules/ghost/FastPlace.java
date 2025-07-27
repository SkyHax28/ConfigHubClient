package com.dew.system.module.modules.ghost;

import com.dew.system.event.events.*;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import org.lwjgl.input.Keyboard;

public class FastPlace extends Module {

    public FastPlace() {
        super("Fast Place", ModuleCategory.GHOST, Keyboard.KEY_NONE, false, true, true);
    }

    @Override
    public void onTick(TickEvent event) {
        mc.rightClickDelayTimer = 0;
    }
}
