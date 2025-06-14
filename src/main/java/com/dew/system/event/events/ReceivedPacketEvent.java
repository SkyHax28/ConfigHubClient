package com.dew.system.event.events;

import com.dew.system.event.EventArgument;
import com.dew.system.event.EventListener;
import net.minecraft.network.Packet;

import java.util.Objects;

public class ReceivedPacketEvent extends EventArgument {

    public final Packet packet;

    public ReceivedPacketEvent(Packet packet) {
        this.packet = packet;
    }

    @Override
    public void call(EventListener listener) {
        Objects.requireNonNull(listener).onReceivedPacket(this);
    }
}