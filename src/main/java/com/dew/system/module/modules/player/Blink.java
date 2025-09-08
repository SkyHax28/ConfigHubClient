package com.dew.system.module.modules.player;

import com.dew.DewCommon;
import com.dew.system.event.events.WorldLoadEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.utils.BlinkUtil;
import org.lwjgl.input.Keyboard;

public class Blink extends Module {

    public Blink() {
        super("Blink", ModuleCategory.PLAYER, Keyboard.KEY_NONE, false, true, true);
    }

    @Override
    public void onEnable() {
        BlinkUtil.doBlink();
    }

    @Override
    public void onDisable() {
        BlinkUtil.sync(true, true);
        BlinkUtil.stopBlink();
    }

    @Override
    public void onLoadWorld(WorldLoadEvent event) {
        BlinkUtil.sync(true, true);
    }
}