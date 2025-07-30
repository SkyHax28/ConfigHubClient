package com.dew.utils;

import com.dew.DewCommon;
import com.dew.IMinecraft;
import com.google.gson.JsonObject;
import net.minecraft.client.Minecraft;
import net.minecraft.util.IChatComponent;

public class LogUtil {

    private static final Minecraft mc = IMinecraft.mc;

    public static void printChat(String message) {
        if (mc.thePlayer == null) return;
        IChatComponent convertedString = textToIChatComponentString("> " + message);
        mc.thePlayer.addChatMessage(convertedString);
    }

    public static void infoLog(String message) {
        System.out.println(message);
    }

    private static IChatComponent textToIChatComponentString(String text) {
        final JsonObject jsonObject = new JsonObject();
        jsonObject.addProperty("text", text);
        return IChatComponent.Serializer.jsonToComponent(jsonObject.toString());
    }
}
