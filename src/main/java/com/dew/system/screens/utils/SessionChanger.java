package com.dew.system.screens.utils;

import net.minecraft.util.Session;
import static com.dew.IMinecraft.mc;

public class SessionChanger {
    public static Session previousSession;
    public static void setSession(String username, String uuid, String token, String type) {
        Session newSession = new Session(username, uuid, token, type);
        mc.session = newSession;
    }
}
