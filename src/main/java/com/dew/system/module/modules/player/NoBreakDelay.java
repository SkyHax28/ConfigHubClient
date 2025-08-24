package com.dew.system.module.modules.player;

import com.dew.system.event.events.PreUpdateEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import org.lwjgl.input.Keyboard;

public class NoBreakDelay extends Module {

    public NoBreakDelay() {
        super("No Break Delay", ModuleCategory.PLAYER, Keyboard.KEY_NONE, false, false, true);
    }

    @Override
    public void onPreUpdate(PreUpdateEvent event) {
        mc.playerController.blockHitDelay = 0;
    }
}
