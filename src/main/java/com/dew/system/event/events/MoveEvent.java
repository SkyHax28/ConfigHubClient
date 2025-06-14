package com.dew.system.event.events;

import com.dew.system.event.EventArgument;
import com.dew.system.event.EventListener;

import java.util.Objects;

public class MoveEvent extends EventArgument {

    public double x;
    public double y;
    public double z;
    public boolean isSafeWalk = false;

    public MoveEvent(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    @Override
    public void call(EventListener listener) {
        Objects.requireNonNull(listener).onMove(this);
    }
}