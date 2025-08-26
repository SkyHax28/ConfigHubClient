package com.dew.system.special;

import com.dew.utils.Base64Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IImageBuffer;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;

import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

public class CapeImageLoader {

    private static final String LIST_URL = Base64Util.decodeToString("aHR0cHM6Ly9uYXR0b2dyZWF0YXBpLnBhZ2VzLmRldi9kZXcvY2FwZXMvdXNlcnMudHh0");
    private static final String CAPE_BASE = Base64Util.decodeToString("aHR0cHM6Ly9uYXR0b2dyZWF0YXBpLnBhZ2VzLmRldi9kZXcvY2FwZXMvaW1hZ2VzLw==");

    private static final Map<String, ResourceLocation> capeMap = new HashMap<>();

    public static ResourceLocation getCape(String username) {
        return capeMap.getOrDefault(username, null);
    }

    public static void loadAllCapes() {
        new Thread(() -> {
            capeMap.clear();
            try {
                URL url = new URL(LIST_URL);
                BufferedReader reader = new BufferedReader(new InputStreamReader(url.openStream()));
                StringBuilder sb = new StringBuilder();
                String line;
                while ((line = reader.readLine()) != null) {
                    sb.append(line);
                }
                reader.close();

                String data = sb.toString().trim();
                if (!data.isEmpty()) {
                    String[] entries = data.split("~~--~~");
                    for (String entry : entries) {
                        String[] parts = entry.split(":");
                        if (parts.length == 2) {
                            String username = parts[0].trim();
                            String capeFile = parts[1].trim();

                            String capeUrl = CAPE_BASE + capeFile;
                            ResourceLocation capeLoc = loadCape(username, capeUrl);

                            if (capeLoc != null) {
                                capeMap.put(username, capeLoc);
                            }
                        }
                    }
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }).start();
    }

    private static ResourceLocation loadCape(String username, String url) {
        try {
            ResourceLocation rl = new ResourceLocation("dew_capes/" + username);

            TextureManager textureManager = Minecraft.getMinecraft().getTextureManager();

            IImageBuffer buffer = new IImageBuffer() {
                @Override
                public BufferedImage parseUserSkin(BufferedImage image) {
                    return image;
                }

                @Override
                public void skinAvailable() {
                }
            };

            ThreadDownloadImageData texture = new ThreadDownloadImageData(null, url, null, buffer);
            textureManager.loadTexture(rl, texture);

            return rl;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}