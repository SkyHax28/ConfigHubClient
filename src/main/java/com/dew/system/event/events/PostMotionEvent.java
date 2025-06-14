package com.dew.system.event.events;

import com.dew.system.event.EventArgument;
import com.dew.system.event.EventListener;

import java.util.Objects;

public class PostMotionEvent extends EventArgument {

    public final double x;
    public final double y;
    public final double z;
    public final float yaw;
    public final float pitch;
    public final boolean onGround;

    public PostMotionEvent(double x, double y, double z, float yaw, float pitch, boolean onGround) {
        this.x = x;
        this.y = y;
        this.z = z;
        this.yaw = yaw;
        this.pitch = pitch;
        this.onGround = onGround;
    }

    @Override
    public void call(EventListener listener) {
        Objects.requireNonNull(listener).onPostMotion(this);
    }
}