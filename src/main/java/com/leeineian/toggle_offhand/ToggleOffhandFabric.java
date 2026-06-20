package com.leeineian.toggle_offhand;

import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

public class ToggleOffhandFabric {
    public static void init() {
        try {
            ToggleOffhand.LOGGER.info("Initializing Toggle Offhand on Fabric reflectively...");
            
            // Get game directory using FabricLoader reflectively
            Class<?> loaderClass = Class.forName("net.fabricmc.loader.api.FabricLoader");
            Object loaderInstance = loaderClass.getMethod("getInstance").invoke(null);
            java.nio.file.Path gamePath = (java.nio.file.Path) loaderClass.getMethod("getGameDir").invoke(loaderInstance);
            java.io.File gameDir = gamePath.toFile();
            
            ToggleOffhand.loadConfig(gameDir);

            if (ToggleOffhand.keyMapping == null) {
                ToggleOffhand.keyMapping = new KeyMapping(
                        "key.toggle_offhand",
                        InputConstants.Type.KEYSYM,
                        GLFW.GLFW_KEY_UNKNOWN,
                        KeyMapping.Category.MISC
                );
            }
            
            // Register key binding via KeyBindingHelper reflectively
            Class<?> helperClass = Class.forName("net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper");
            java.lang.reflect.Method registerMethod = helperClass.getMethod("registerKeyBinding", KeyMapping.class);
            registerMethod.invoke(null, ToggleOffhand.keyMapping);
            ToggleOffhand.LOGGER.info("Registered keybind via Fabric KeyBindingHelper reflectively!");
        } catch (Exception e) {
            ToggleOffhand.LOGGER.error("Failed to initialize Fabric reflectively: ", e);
        }
    }
}
