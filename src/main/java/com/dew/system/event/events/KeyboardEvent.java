package com.dew.system.event.events;

import com.dew.system.event.EventArgument;
import com.dew.system.event.EventListener;

import java.util.Objects;

public class KeyboardEvent extends EventArgument {

    public final int key;
    public final boolean isPress;

    public KeyboardEvent(int key, boolean isPress) {
        this.key = key;
        this.isPress = isPress;
    }

    @Override
    public void call(EventListener listener) {
        Objects.requireNonNull(listener).onKeyboard(this);
    }
}