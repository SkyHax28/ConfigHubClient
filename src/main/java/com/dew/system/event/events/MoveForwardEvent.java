package com.dew.system.event.events;

import com.dew.system.event.EventArgument;
import com.dew.system.event.EventListener;

import java.util.Objects;

public class MoveForwardEvent extends EventArgument {
    public boolean reset;
    public MoveForwardEvent(boolean reset){
        this.reset = reset;
    }
    @Override
    public void call(EventListener listener) {
        Objects.requireNonNull(listener).onMoveForward(this);
    }
}
