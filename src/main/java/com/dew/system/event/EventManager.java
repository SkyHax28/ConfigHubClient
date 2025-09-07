package com.dew.system.event;

import com.dew.utils.LogUtil;

import java.util.Comparator;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class EventManager {

    private final Set<EventListener> listenerRegistry;

    public EventManager() {
        this.listenerRegistry = ConcurrentHashMap.newKeySet();
        LogUtil.infoLog("init eventManager");
    }

    public void call(final EventArgument argument) {
        listenerRegistry.stream()
                .sorted(Comparator.comparingInt(l -> l.getPriority().ordinal()))
                .forEach(argument::call);
    }

    public void register(final EventListener listener) {
        listenerRegistry.add(listener);
    }

    public void unregister(final EventListener listener) {
        listenerRegistry.remove(listener);
    }
}
