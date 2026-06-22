package com.leeineian.toggle_offhand.mixin;

import com.leeineian.toggle_offhand.ToggleOffhand;
import com.leeineian.toggle_offhand.compat.ClientCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.util.Arrays;

@Mixin(targets = "net.minecraft.class_315")
public class OptionsMixin {
    private static java.lang.reflect.Field keyMappingsField = null;

    private static Object[] getKeyMappings(Object options) {
        if (keyMappingsField == null) {
            Class<?> current = options.getClass();
            outerLoop:
            while (current != null && current != Object.class) {
                ToggleOffhand.LOGGER.info("Scanning fields in class: " + current.getName());
                for (java.lang.reflect.Field f : current.getDeclaredFields()) {
                    Class<?> type = f.getType();
                    if (type.isArray()) {
                        Class<?> comp = type.getComponentType();
                        if (comp.getName().endsWith(".KeyMapping") || comp.getName().endsWith(".class_304")) {
                            f.setAccessible(true);
                            try {
                                Object[] arr = (Object[]) f.get(options);
                                if (arr != null && arr.length > 30) {
                                    keyMappingsField = f;
                                    ToggleOffhand.LOGGER.info("Matched keyMappings field: " + f.getName() + " of length " + arr.length + " in class " + current.getName());
                                    break outerLoop;
                                }
                            } catch (Exception e) {
                                ToggleOffhand.LOGGER.error("Failed to check field " + f.getName() + " length: ", e);
                            }
                        }
                    }
                }
                current = current.getSuperclass();
            }
        }
        if (keyMappingsField != null) {
            try {
                Object[] arr = (Object[]) keyMappingsField.get(options);
                ToggleOffhand.LOGGER.info("Current keyMappings array length: " + (arr != null ? arr.length : "null"));
                return arr;
            } catch (Exception e) {
                ToggleOffhand.LOGGER.error("Failed to get keyMappings reflectively: ", e);
            }
        } else {
            ToggleOffhand.LOGGER.warn("keyMappings field was not found!");
        }
        return null;
    }

    private static void setKeyMappings(Object options, Object[] mappings) {
        if (keyMappingsField != null) {
            try {
                keyMappingsField.set(options, mappings);
                ToggleOffhand.LOGGER.info("Successfully set new keyMappings array of length: " + mappings.length);
            } catch (Exception e) {
                ToggleOffhand.LOGGER.error("Failed to set keyMappings reflectively: ", e);
            }
        }
    }

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(@Coerce Object minecraft, File file, CallbackInfo ci) {
        ToggleOffhand.LOGGER.info("OptionsMixin onInit starting...");
        File gameDir = ClientCompat.getGameDirectory(minecraft);
        ClientCompat.gameDirectory = gameDir;
        ClientCompat.injectTranslations();
        if (ToggleOffhand.keyMapping == null) {
            ToggleOffhand.LOGGER.info("Initializing Toggle Offhand keybind...");
            ToggleOffhand.loadConfig(gameDir);
            ToggleOffhand.keyMapping = ClientCompat.createKeyMapping("key.toggle_offhand", -1, "key.categories.misc");
            ToggleOffhand.LOGGER.info("Created key mapping: " + ToggleOffhand.keyMapping);
        } else {
            ToggleOffhand.loadConfig(gameDir);
        }

        // Load key from options.txt
        if (gameDir != null) {
            File optionsFile = new File(gameDir, "options.txt");
            if (optionsFile.exists()) {
                try (java.io.BufferedReader reader = new java.io.BufferedReader(new java.io.FileReader(optionsFile))) {
                    String line;
                    while ((line = reader.readLine()) != null) {
                        if (line.startsWith("key_key.toggle_offhand:")) {
                            String keyStr = line.substring(line.indexOf(":") + 1).trim();
                            ClientCompat.setKeyFromString(ToggleOffhand.keyMapping, keyStr);
                            break;
                        }
                    }
                } catch (Exception e) {
                    ToggleOffhand.LOGGER.error("Failed to load key from options.txt: ", e);
                }
            }
        }
        
        try {
            Object[] original = getKeyMappings(this);
            if (original != null) {
                Object[] copy = Arrays.copyOf(original, original.length + 1);
                copy[original.length] = ToggleOffhand.keyMapping;
                setKeyMappings(this, copy);
            }
        } catch (Exception e) {
            ToggleOffhand.LOGGER.error("Failed to append key mapping reflectively: ", e);
        }
    }
}
