package com.dew.system.event.events;

import com.dew.system.event.EventArgument;
import com.dew.system.event.EventListener;
import net.minecraft.item.ItemStack;

import java.util.Objects;

public class ItemRenderEvent extends EventArgument {

    public ItemStack itemToRender;

    public ItemRenderEvent(ItemStack itemToRender) {
        this.itemToRender = itemToRender;
    }

    @Override
    public void call(EventListener listener) {
        Objects.requireNonNull(listener).onItemRender(this);
    }
}