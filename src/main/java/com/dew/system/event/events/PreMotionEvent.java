package com.dew.system.event.events;

import com.dew.system.event.EventArgument;
import com.dew.system.event.EventListener;

import java.util.Objects;

public class PreMotionEvent extends EventArgument {

    public double x;
    public double y;
    public double z;
    public float yaw;
    public float pitch;
    public boolean onGround;

    public PreMotionEvent(double x, double y, double z, float yaw, float pitch, boolean onGround) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.onGround = onGround;
    }

    @Override
    public void call(EventListener listener) {
        Objects.requireNonNull(listener).onPreMotion(this);
    }
}