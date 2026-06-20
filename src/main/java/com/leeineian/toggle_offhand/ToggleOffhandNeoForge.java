package com.leeineian.toggle_offhand;

import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

@net.neoforged.fml.common.Mod("toggle_offhand")
public class ToggleOffhandNeoForge {
    public ToggleOffhandNeoForge(Object modEventBus) {
        try {
            ToggleOffhand.LOGGER.info("Registering key mapping reflectively on NeoForge...");
            
            Class<?> eventClass = Class.forName("net.neoforged.neoforge.client.event.RegisterKeyMappingsEvent");
            
            java.util.function.Consumer<Object> handler = event -> {
                try {
                    ToggleOffhand.loadConfig(net.minecraft.client.Minecraft.getInstance().gameDirectory);
                    if (ToggleOffhand.keyMapping == null) {
                        ToggleOffhand.keyMapping = new KeyMapping(
                                "key.toggle_offhand",
                                InputConstants.Type.KEYSYM,
                                GLFW.GLFW_KEY_UNKNOWN,
                                KeyMapping.Category.MISC
                        );
                    }
                    event.getClass().getMethod("register", KeyMapping.class).invoke(event, ToggleOffhand.keyMapping);
                    ToggleOffhand.LOGGER.info("Registered keybind via NeoForge RegisterKeyMappingsEvent reflectively!");
                } catch (Exception e) {
                    ToggleOffhand.LOGGER.error("Failed to register key mapping on NeoForge in event: ", e);
                }
            };
            
            java.lang.reflect.Method addListenerMethod = modEventBus.getClass().getMethod(
                    "addListener",
                    Class.class,
                    java.util.function.Consumer.class
            );
            addListenerMethod.invoke(modEventBus, eventClass, handler);
            ToggleOffhand.LOGGER.info("Successfully registered reflective listener callback for NeoForge RegisterKeyMappingsEvent");
        } catch (Exception e) {
            ToggleOffhand.LOGGER.error("Failed to register key mapping on NeoForge: ", e);
        }
    }
}
