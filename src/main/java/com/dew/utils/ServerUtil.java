package com.dew.utils;

import com.dew.IMinecraft;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiMainMenu;
import net.minecraft.client.gui.GuiMultiplayer;
import net.minecraft.client.multiplayer.GuiConnecting;
import net.minecraft.client.multiplayer.ServerData;

public class ServerUtil {
    private static final Minecraft mc = IMinecraft.mc;
    public static ServerData serverData;

    public static void connectToLastServer() {
        if (serverData == null) return;

        mc.displayGuiScreen(new GuiConnecting(new GuiMultiplayer(new GuiMainMenu()), mc, serverData));
    }
}
