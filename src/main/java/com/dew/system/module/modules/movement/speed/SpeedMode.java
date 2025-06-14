package com.dew.system.module.modules.movement.speed;

import com.dew.IMinecraft;
import com.dew.system.event.events.PreUpdateEvent;
import net.minecraft.client.Minecraft;

public interface SpeedMode {
    Minecraft mc = IMinecraft.mc;
    String getName();
    void onEnable();
    void onDisable();
    void onPreUpdate(PreUpdateEvent event);
}