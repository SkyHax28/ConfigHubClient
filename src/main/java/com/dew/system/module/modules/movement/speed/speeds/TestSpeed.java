package com.dew.system.module.modules.movement.speed.speeds;

import com.dew.system.event.events.PreUpdateEvent;
import com.dew.system.module.modules.movement.speed.SpeedMode;

public class TestSpeed implements SpeedMode {
    @Override
    public String getName() {
        return "Test";
    }

    @Override
    public void onEnable() {
    }

    @Override
    public void onDisable() {
    }

    @Override
    public void onPreUpdate(PreUpdateEvent event) {
    }
}
