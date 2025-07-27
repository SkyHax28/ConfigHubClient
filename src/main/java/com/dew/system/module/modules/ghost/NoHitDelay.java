package com.dew.system.module.modules.ghost;

import com.dew.system.event.events.TickEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import org.lwjgl.input.Keyboard;

public class NoHitDelay extends Module {

    public NoHitDelay() {
        super("No Hit Delay", ModuleCategory.GHOST, Keyboard.KEY_NONE, false, true, true);
    }

    @Override
    public void onTick(TickEvent event) {
        mc.leftClickCounter = 0;
    }
}
