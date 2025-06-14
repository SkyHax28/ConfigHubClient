package com.dew.system.event.events;

import com.dew.system.event.EventArgument;
import com.dew.system.event.EventListener;

import java.util.Objects;

public class Render2DEvent extends EventArgument {

    public final float partialTicks;

    public Render2DEvent(float partialTicks) {
        this.partialTicks = partialTicks;
    }

    @Override
    public void call(EventListener listener) {
        Objects.requireNonNull(listener).onRender2D(this);
    }
}