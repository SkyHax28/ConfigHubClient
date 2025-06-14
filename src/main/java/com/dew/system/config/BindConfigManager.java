package com.dew.system.config;

import com.dew.DewCommon;
import com.dew.system.module.Module;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.Queue;

public class BindConfigManager {
    private final File configFolder;

    public BindConfigManager() {
        this.configFolder = new File(DewCommon.BASE_CFG_DIR, "bind-configs");
        if (!configFolder.exists()) {
            configFolder.mkdirs();
        }
    }

    public void save(String configName, Queue<Module> modules) {
        File configFile = new File(configFolder, configName + ".properties");
        Properties props = new Properties();

        for (Module module : modules) {
            props.setProperty(module.name, String.valueOf(module.key));
        }

        try (FileWriter writer = new FileWriter(configFile)) {
            props.store(writer, "Bind Config - " + configName);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public boolean load(String configName, Queue<Module> modules) {
        File configFile = new File(configFolder, configName + ".properties");
        if (!configFile.exists()) return false;

        Properties props = new Properties();
        try (FileReader reader = new FileReader(configFile)) {
            props.load(reader);
        } catch (IOException e) {
            e.printStackTrace();
            return false;
        }

        for (Module module : modules) {
            String str = props.getProperty(module.name);
            if (str != null) {
                try {
                    module.key = Integer.parseInt(str);
                } catch (NumberFormatException e) {
                    System.err.println("Invalid key for module " + module.name);
                }
            }
        }

        return true;
    }

    public List<String> getConfigNames() {
        List<String> configNames = new ArrayList<>();
        File[] files = configFolder.listFiles((dir, name) -> name.endsWith(".properties"));

        if (files != null) {
            for (File file : files) {
                String name = file.getName();
                if (name.endsWith(".properties")) {
                    configNames.add(name.substring(0, name.length() - ".properties".length()));
                }
            }
        }

        return configNames;
    }
}
