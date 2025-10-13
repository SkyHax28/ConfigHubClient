package com.dew.system.module.modules.other;

import com.dew.system.event.events.KeyPressableEvent;
import com.dew.system.event.events.TickEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import org.lwjgl.input.Keyboard;

public class FastPlace extends Module {

    public FastPlace() {
        super("Fast Place", ModuleCategory.OTHER, Keyboard.KEY_NONE, false, true, true);
    }

    @Override
    public void onKeyPressable(KeyPressableEvent event) {
        mc.rightClickDelayTimer = 0;
    }
}
