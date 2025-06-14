package com.dew.system.command;

import com.dew.IMinecraft;
import net.minecraft.client.Minecraft;

public abstract class Command {
    public final Minecraft mc = IMinecraft.mc;

    private final String name;

    public Command(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public abstract void execute(String[] args);
    public abstract String getUsage();
}