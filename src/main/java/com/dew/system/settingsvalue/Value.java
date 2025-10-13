package com.dew.system.settingsvalue;

import com.dew.DewCommon;
import com.dew.system.module.modules.visual.Hud;

import java.util.function.Supplier;

public abstract class Value<T> {
    protected final String name;
    protected T value;
    protected Supplier<Boolean> visibleSupplier;

    public Value(String name, T defaultValue) {
        this(name, defaultValue, () -> true);
    }

    public Value(String name, T defaultValue, Supplier<Boolean> visibleSupplier) {
        this.name = name;
        this.value = defaultValue;
        this.visibleSupplier = visibleSupplier;
    }

    public String getName() {
        return name;
    }

    public T get() {
        return value;
    }

    public void set(T value) {
        this.value = value;
        if (DewCommon.moduleManager.getModule(Hud.class).isEnabled()) {
            DewCommon.moduleManager.getModule(Hud.class).markModuleListDirty();
        }
    }

    public boolean isVisible() {
        return this.visibleSupplier.get();
    }

    public void setVisibleCondition(Supplier<Boolean> visibleSupplier) {
        this.visibleSupplier = visibleSupplier;
    }
}