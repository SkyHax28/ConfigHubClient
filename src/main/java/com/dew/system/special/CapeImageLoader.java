package com.dew.system.special;

import com.dew.IMinecraft;
import com.dew.utils.Base64Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IImageBuffer;
import net.minecraft.client.renderer.ThreadDownloadImageData;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.util.ResourceLocation;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CapeImageLoader {

    private static final Minecraft mc = IMinecraft.mc;

    private static final String LIST_URL = Base64Util.decodeToString("aHR0cHM6Ly9uYXR0b2dyZWF0YXBpLnBhZ2VzLmRldi9kZXcvY2FwZXMvdXNlcnMudHh0");
    private static final String CAPE_BASE = Base64Util.decodeToString("aHR0cHM6Ly9uYXR0b2dyZWF0YXBpLnBhZ2VzLmRldi9kZXcvY2FwZXMvaW1hZ2VzLw==");

    private static final Map<String, List<ResourceLocation>> capeMap = new HashMap<>();
    private static final Map<String, Integer> capeIndexMap = new HashMap<>();
    private static final Map<String, Long> lastSwitchTimeMap = new HashMap<>();
    private static final Map<String, Long> capeDelayMap = new HashMap<>();

    private static final OkHttpClient client = new OkHttpClient();

    public static ResourceLocation getCape(String username) {
        List<ResourceLocation> capes = capeMap.get(username);
        if (capes == null || capes.isEmpty()) {
            return null;
        }

        long currentTime = System.currentTimeMillis();
        long lastSwitch = lastSwitchTimeMap.getOrDefault(username, 0L);
        long delay = capeDelayMap.getOrDefault(username, 1000L);
        int index = capeIndexMap.getOrDefault(username, 0);

        if (currentTime - lastSwitch >= delay) {
            index = (index + 1) % capes.size();
            capeIndexMap.put(username, index);
            lastSwitchTimeMap.put(username, currentTime);
        }

        return capes.get(index);
    }

    public static void loadAllCapes() {
        new Thread(() -> {
            capeMap.clear();
            capeIndexMap.clear();
            lastSwitchTimeMap.clear();
            capeDelayMap.clear();

            try {
                Request request = new Request.Builder()
                        .url(LIST_URL)
                        .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful() || response.body() == null) {
                        System.err.println("Failed to fetch cape list: " + response);
                        return;
                    }

                    String data = response.body().string().trim();
                    if (!data.isEmpty()) {
                        String[] entries = data.split("~~--~~");
                        for (String entry : entries) {
                            String[] parts = entry.split(":");
                            if (parts.length >= 3) {
                                String username = parts[0].trim();
                                long delay;
                                try {
                                    delay = Long.parseLong(parts[1].trim());
                                } catch (Exception e) {
                                    delay = 1000L;
                                }

                                String[] capeFiles = parts[2].split(",");

                                List<ResourceLocation> userCapes = new ArrayList<>();
                                for (String capeFile : capeFiles) {
                                    capeFile = capeFile.trim();
                                    if (!capeFile.isEmpty()) {
                                        String capeUrl = CAPE_BASE + capeFile;
                                        ResourceLocation capeLoc = loadCape(username + "_" + capeFile, capeUrl);
                                        if (capeLoc != null) {
                                            userCapes.add(capeLoc);
                                        }
                                    }
                                }

                                if (!userCapes.isEmpty()) {
                                    capeMap.put(username, userCapes);
                                    capeIndexMap.put(username, 0);
                                    lastSwitchTimeMap.put(username, System.currentTimeMillis());
                                    capeDelayMap.put(username, delay);
                                }
                            } else if (parts.length == 2) {
                                String username = parts[0].trim();
                                String[] capeFiles = parts[1].split(",");

                                List<ResourceLocation> userCapes = new ArrayList<>();
                                for (String capeFile : capeFiles) {
                                    capeFile = capeFile.trim();
                                    if (!capeFile.isEmpty()) {
                                        String capeUrl = CAPE_BASE + capeFile;
                                        ResourceLocation capeLoc = loadCape(username + "_" + capeFile, capeUrl);
                                        if (capeLoc != null) {
                                            userCapes.add(capeLoc);
                                        }
                                    }
                                }

                                if (!userCapes.isEmpty()) {
                                    capeMap.put(username, userCapes);
                                    capeIndexMap.put(username, 0);
                                    lastSwitchTimeMap.put(username, System.currentTimeMillis());
                                    capeDelayMap.put(username, 1000L);
                                }
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
            Request request = new Request.Builder()
                    .url(url)
                    .build();

            try (Response response = client.newCall(request).execute()) {
                if (!response.isSuccessful() || response.body() == null) {
                    System.err.println("Failed to load cape: " + url + " - " + response);
                    return null;
                }

                InputStream inputStream = response.body().byteStream();
                BufferedImage image = ImageIO.read(inputStream);
                if (image == null) {
                    System.err.println("Invalid image for cape: " + url);
                    return null;
                }

                ResourceLocation rl = new ResourceLocation("dew_capes/" + username);

                mc.addScheduledTask(() -> {
                    try {
                        DynamicTexture dynTex = new DynamicTexture(image);
                        mc.getTextureManager().loadTexture(rl, dynTex);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                });

                return rl;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}