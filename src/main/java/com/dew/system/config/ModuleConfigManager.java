package com.dew.system.config;

import com.dew.DewCommon;
import com.dew.IMinecraft;
import com.dew.system.module.Module;
import com.dew.system.settingsvalue.*;

import java.io.*;
import java.util.*;

public class ModuleConfigManager {
    private final File configFolder;

    public ModuleConfigManager() {
        this.configFolder = new File(DewCommon.BASE_CFG_DIR, "module-configs");
        if (!configFolder.exists()) {
            configFolder.mkdirs();
        }
    }

    public void save(String configName, Queue<Module> modules) {
        File configFile = new File(configFolder, configName + ".properties");
        Properties props = new Properties();

        for (Module module : modules) {
            props.setProperty(module.name + ".enabled", String.valueOf(module.isEnabled()));
            for (Value<?> value : module.getValues()) {
                props.setProperty(module.name + "." + value.getName(), serialize(value));
            }
        }

        try (FileWriter writer = new FileWriter(configFile)) {
            props.store(writer, "Module Config - " + configName);
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
            String enabledKey = module.name + ".enabled";
            String enabledStr = props.getProperty(enabledKey);

            for (Value<?> value : module.getValues()) {
                String key = module.name + "." + value.getName();
                String str = props.getProperty(key);
                if (str != null) {
                    deserialize(value, str);
                }
            }

            if (enabledStr != null) {
                boolean enabled = Boolean.parseBoolean(enabledStr);
                module.setState(enabled);
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

    private String serialize(Value<?> value) {
        if (value instanceof MultiSelectionValue) {
            return String.join(",", ((MultiSelectionValue) value).get());
        }
        return String.valueOf(value.get());
    }

    @SuppressWarnings("unchecked")
    private void deserialize(Value<?> value, String str) {
        try {
            if (value instanceof BooleanValue) {
                ((Value<Boolean>) value).set(Boolean.parseBoolean(str));
            } else if (value instanceof NumberValue) {
                ((Value<Double>) value).set(Double.parseDouble(str));
            } else if (value instanceof SelectionValue) {
                if (((SelectionValue) value).getOptions().contains(str)) {
                    ((Value<String>) value).set(str);
                }
            } else if (value instanceof MultiSelectionValue) {
                String[] all = str.split(",");
                MultiSelectionValue msv = (MultiSelectionValue) value;
                List<String> valid = new ArrayList<>();
                for (String s : all) {
                    if (msv.getOptions().contains(s)) {
                        valid.add(s);
                    }
                }
                ((Value<List<String>>) value).set(valid);
            } else {
                ((Value<Object>) value).set(str);
            }
        } catch (Exception e) {
            System.err.println("Failed to load value: " + value.getName());
            e.printStackTrace();
        }
    }
}