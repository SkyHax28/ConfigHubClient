package com.dew.system.event.events;

import com.dew.system.event.EventArgument;
import com.dew.system.event.EventListener;
import java.util.Objects;
public class MoveInputEvent extends EventArgument {

    public float forward;
    public float strafe;
    public float lastForward;
    public float lastStrafe;
    public boolean jump;
    public boolean sneak;
    public double sneakMultiplier;

    public MoveInputEvent(float forward, float strafe, float lastForward, float lastStrafe, boolean jump, boolean sneak, double sneakMultiplier) {
        this.forward = forward;
        this.strafe = strafe;
        this.lastForward = lastForward;
        this.lastStrafe = lastStrafe;
        this.jump = jump;
        this.sneak = sneak;
        this.sneakMultiplier = sneakMultiplier;
    }

    @Override
    public void call(EventListener listener) {
        Objects.requireNonNull(listener).onMoveInput(this);
    }
}