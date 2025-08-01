package com.dew.system.event.events;

import com.dew.system.event.EventArgument;
import com.dew.system.event.EventListener;

import java.util.Objects;

public class TablistPlayerNameFetchEvent extends EventArgument {
    public String name;
    public TablistPlayerNameFetchEvent(String name){
        this.name = name;
    }
    @Override
    public void call(EventListener listener) {
        Objects.requireNonNull(listener).onTablistPlayerNameFetch(this);
    }
}
