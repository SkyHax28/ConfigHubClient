package com.dew.system.event.events;

import com.dew.system.event.EventArgument;
import com.dew.system.event.EventListener;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.util.IChatComponent;

import java.util.Objects;

public class GuiDisconnectedEvent extends EventArgument {
    public GuiScreen parentScreen;
    public String reasonLocalizedKey;
    public IChatComponent chatComponent;

    public GuiDisconnectedEvent(GuiScreen parentScreen, String reasonLocalizedKey, IChatComponent chatComponent) {
        this.parentScreen = parentScreen;
        this.reasonLocalizedKey = reasonLocalizedKey;
        this.chatComponent = chatComponent;
    }

    @Override
    public void call(EventListener listener) {
        Objects.requireNonNull(listener).onGuiDisconnected(this);
    }
}
