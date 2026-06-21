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

@Mixin(targets = "net.minecraft.client.Options", remap = false)
public class OptionsModernMixin {
    private static java.lang.reflect.Field keyMappingsField = null;

    private static Object[] getKeyMappings(Object options) {
        if (keyMappingsField == null) {
            ToggleOffhand.LOGGER.info("Scanning fields in " + options.getClass().getName());
            for (java.lang.reflect.Field f : options.getClass().getDeclaredFields()) {
                Class<?> type = f.getType();
                if (type.isArray()) {
                    Class<?> comp = type.getComponentType();
                    if (comp.getName().endsWith(".KeyMapping") || comp.getName().endsWith(".class_304")) {
                        if (f.getName().equals("allKeys") || f.getName().equals("field_1839") || f.getName().equals("keyMappings") || f.getName().equals("keyBindings")) {
                            f.setAccessible(true);
                            keyMappingsField = f;
                            ToggleOffhand.LOGGER.info("Matched keyMappings field: " + f.getName());
                            break;
                        }
                    }
                }
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
        ToggleOffhand.LOGGER.info("OptionsModernMixin onInit starting...");
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
