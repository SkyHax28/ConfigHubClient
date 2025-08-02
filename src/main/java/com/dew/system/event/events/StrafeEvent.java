package com.dew.system.event.events;

import com.dew.system.event.EventArgument;
import com.dew.system.event.EventListener;

import java.util.Objects;

public class StrafeEvent extends EventArgument {

    public float forward;
    public float strafe;
    public float friction;
    public float moveYaw;

    public StrafeEvent(float forward, float strafe, float friction, float moveYaw) {
        this.forward = forward;
        this.strafe = strafe;
        this.friction = friction;
        this.moveYaw = moveYaw;
    }

    @Override
    public void call(EventListener listener) {
        Objects.requireNonNull(listener).onStrafe(this);
    }
}