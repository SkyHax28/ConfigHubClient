package com.dew.system.event.events;

import com.dew.system.event.EventArgument;
import com.dew.system.event.EventListener;
import net.minecraft.entity.Entity;

import java.util.Objects;

public class AttackEvent extends EventArgument {

    public Entity target;

    public AttackEvent(Entity target) {
        this.target = target;
    }

    @Override
    public void call(EventListener listener) {
        Objects.requireNonNull(listener).onAttack(this);
    }
}