package com.leeineian.toggle_offhand;

import com.leeineian.toggle_offhand.compat.ClientCompat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.util.Properties;

public class ToggleOffhand {
    public static final String MOD_ID = "toggle_offhand";
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

    public static boolean doubleHands = true;
    public static Object keyMapping;

    public static Object createKeyMapping(String name, int keycode, String category) {
        return ClientCompat.createKeyMapping(name, keycode, category);
    }

    public static boolean consumeClick(Object mapping) {
        return ClientCompat.consumeClick(mapping);
    }

    public static void loadConfig(File gameDirectory) {
        if (gameDirectory == null) return;
        File configFile = new File(new File(gameDirectory, "config"), "toggle_offhand.properties");
        if (configFile.exists()) {
            Properties props = new Properties();
            try (FileReader reader = new FileReader(configFile)) {
                props.load(reader);
                doubleHands = Boolean.parseBoolean(props.getProperty("doubleHands", "true"));
            } catch (Exception e) {
                LOGGER.error("Failed to load config: ", e);
            }
        } else {
            saveConfig(gameDirectory);
        }
    }

    public static void saveConfig(File gameDirectory) {
        if (gameDirectory == null) return;
        File configDir = new File(gameDirectory, "config");
        if (!configDir.exists()) {
            configDir.mkdirs();
        }
        File configFile = new File(configDir, "toggle_offhand.properties");
        Properties props = new Properties();
        props.setProperty("doubleHands", String.valueOf(doubleHands));
        try (FileWriter writer = new FileWriter(configFile)) {
            props.store(writer, "Toggle Offhand Configuration");
        } catch (Exception e) {
            LOGGER.error("Failed to save config: ", e);
        }
    }

    public static void injectTranslations() {
        ClientCompat.injectTranslations();
    }

    public static String getTranslation(String key) {
        return ClientCompat.getTranslation(key);
    }
}
