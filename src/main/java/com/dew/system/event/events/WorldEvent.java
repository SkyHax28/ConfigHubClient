package com.dew.system.event.events;

import com.dew.system.event.EventArgument;
import com.dew.system.event.EventListener;

import java.util.Objects;

public class WorldEvent extends EventArgument {
    public String ip;
    public int port;
    public WorldEvent(String ip, int port){
        this.ip = ip;
        this.port = port;
    }

    @Override
    public void call(EventListener listener) {
        Objects.requireNonNull(listener).onWorld(this);
    }
}