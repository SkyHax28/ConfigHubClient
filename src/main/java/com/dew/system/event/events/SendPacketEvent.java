package com.dew.system.event.events;

import com.dew.system.event.EventArgument;
import com.dew.system.event.EventListener;
import net.minecraft.network.Packet;

import java.util.Objects;

public class SendPacketEvent extends EventArgument {

    public Packet<?> packet;

    public SendPacketEvent(Packet<?> packet) {
        this.packet = packet;
    }

    @Override
    public void call(EventListener listener) {
        Objects.requireNonNull(listener).onSendPacket(this);
    }
}