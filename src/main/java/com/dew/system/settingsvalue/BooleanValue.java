package com.dew.system.settingsvalue;

import java.util.function.Supplier;

public class BooleanValue extends Value<Boolean> {
    public BooleanValue(String name, boolean defaultValue) {
        super(name, defaultValue);
    }

    public BooleanValue(String name, boolean defaultValue, Supplier<Boolean> visible) {
        super(name, defaultValue, visible);
    }

    public void toggle() {
        this.value = !this.value;
    }
}