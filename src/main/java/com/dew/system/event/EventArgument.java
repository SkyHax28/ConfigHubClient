package com.dew.system.event;

public abstract class EventArgument {
    private boolean cancelled = false;

    protected EventArgument() {
    }

    public void cancel() {
        this.cancelled = true;
    }

    public boolean isCancelled() {
        return cancelled;
    }

    public abstract void call(EventListener listener);
}