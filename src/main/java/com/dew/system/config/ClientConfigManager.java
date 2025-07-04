package com.dew.system.config;

import com.dew.DewCommon;
import com.dew.system.gui.ClickGuiState;
import com.dew.system.module.Module;
import com.dew.system.module.ModuleCategory;
import com.dew.system.settingsvalue.*;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.*;

public class ClientConfigManager {

    public ClientConfigManager() {
        this.load();
    }

    public void save() {
        File configFile = new File(DewCommon.BASE_CFG_DIR, "client.properties");
        Properties props = new Properties();

        props.setProperty("ClickGui.x", String.valueOf(ClickGuiState.x));
        props.setProperty("ClickGui.y", String.valueOf(ClickGuiState.y));
        props.setProperty("ClickGui.width", String.valueOf(ClickGuiState.width));
        props.setProperty("ClickGui.height", String.valueOf(ClickGuiState.height));

        props.setProperty("ClickGui.animatedX", String.valueOf(ClickGuiState.animatedX));
        props.setProperty("ClickGui.animatedY", String.valueOf(ClickGuiState.animatedY));
        props.setProperty("ClickGui.animatedWidth", String.valueOf(ClickGuiState.animatedWidth));
        props.setProperty("ClickGui.animatedHeight", String.valueOf(ClickGuiState.animatedHeight));

        for (Map.Entry<ModuleCategory, ClickGuiState.WindowState> entry : ClickGuiState.windowStates.entrySet()) {
            ModuleCategory category = entry.getKey();
            ClickGuiState.WindowState state = entry.getValue();
            String keyBase = "Category." + category.name();

            props.setProperty(keyBase + ".x", String.valueOf(state.x));
            props.setProperty(keyBase + ".y", String.valueOf(state.y));
            props.setProperty(keyBase + ".open", String.valueOf(state.open));
            props.setProperty(keyBase + ".scroll", String.valueOf(state.scrollOffset));
        }

        try (FileWriter writer = new FileWriter(configFile)) {
            props.store(writer, "Client Config");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void load() {
        File configFile = new File(DewCommon.BASE_CFG_DIR, "client.properties");
        if (!configFile.exists()) return;

        Properties props = new Properties();
        try (FileReader reader = new FileReader(configFile)) {
            props.load(reader);
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        ClickGuiState.x = Integer.parseInt(props.getProperty("ClickGui.x"));
        ClickGuiState.y = Integer.parseInt(props.getProperty("ClickGui.y"));
        ClickGuiState.width = Integer.parseInt(props.getProperty("ClickGui.width"));
        ClickGuiState.height = Integer.parseInt(props.getProperty("ClickGui.height"));

        ClickGuiState.animatedX = Float.parseFloat(props.getProperty("ClickGui.animatedX"));
        ClickGuiState.animatedY = Float.parseFloat(props.getProperty("ClickGui.animatedY"));
        ClickGuiState.animatedWidth = Float.parseFloat(props.getProperty("ClickGui.animatedWidth"));
        ClickGuiState.animatedHeight = Float.parseFloat(props.getProperty("ClickGui.animatedHeight"));

        for (ModuleCategory category : ModuleCategory.values()) {
            String keyBase = "Category." + category.name();

            int x = Integer.parseInt(props.getProperty(keyBase + ".x", "10"));
            int y = Integer.parseInt(props.getProperty(keyBase + ".y", "10"));
            boolean open = Boolean.parseBoolean(props.getProperty(keyBase + ".open", "true"));
            int scroll = Integer.parseInt(props.getProperty(keyBase + ".scroll", "0"));

            ClickGuiState.windowStates.put(category, new ClickGuiState.WindowState(x, y, open, scroll));
        }
    }
}