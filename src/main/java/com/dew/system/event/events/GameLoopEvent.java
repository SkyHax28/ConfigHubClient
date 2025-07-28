package com.dew.system.event.events;

import com.dew.system.event.EventArgument;
import com.dew.system.event.EventListener;

import java.util.Objects;

public class GameLoopEvent extends EventArgument {

    @Override
    public void call(EventListener listener) {
        Objects.requireNonNull(listener).onGameLoop(this);
    }
}