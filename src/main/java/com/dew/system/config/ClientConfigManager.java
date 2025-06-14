package com.dew.system.config;

import com.dew.DewCommon;
import com.dew.system.gui.ClickGuiState;
import com.dew.system.module.Module;
import com.dew.system.settingsvalue.*;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Queue;

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
    }
}