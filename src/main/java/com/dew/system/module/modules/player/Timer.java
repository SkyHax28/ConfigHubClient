package com.dew.system.module.modules.player;

import com.dew.system.event.events.TickEvent;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.settingsvalue.NumberValue;
import com.dew.system.settingsvalue.SelectionValue;
import com.dew.utils.TimerUtil;
import org.lwjgl.input.Keyboard;

public class Timer extends Module {

    private static final NumberValue speed = new NumberValue("Speed", 2.0, 0.1, 10.0, 0.1);
    public Timer() {
        super("Timer", ModuleCategory.PLAYER, Keyboard.KEY_NONE, false, true, true);
    }

    @Override
    public void onDisable() {
        TimerUtil.resetTimerSpeed();
    }

    @Override
    public void onTick(TickEvent event) {
        TimerUtil.setTimerSpeed(speed.get().floatValue());
    }
}
