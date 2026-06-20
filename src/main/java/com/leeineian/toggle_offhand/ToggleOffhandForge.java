package com.leeineian.toggle_offhand;

import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;

@net.minecraftforge.fml.common.Mod("toggle_offhand")
public class ToggleOffhandForge {
    @SuppressWarnings("unchecked")
    public ToggleOffhandForge() {
        try {
            ToggleOffhand.LOGGER.info("Attempting reflective key mapping registration on Forge...");
            
            // Get FMLModContainer and its event bus
            Class<?> modListClass = Class.forName("net.minecraftforge.fml.ModList");
            Object modList = modListClass.getMethod("get").invoke(null);
            java.util.Optional<?> containerOpt = (java.util.Optional<?>) modListClass.getMethod("getModContainerById", String.class).invoke(modList, "toggle_offhand");
            
            if (containerOpt.isPresent()) {
                Object container = containerOpt.get(); // FMLModContainer
                Object eventBus = container.getClass().getMethod("getEventBus").invoke(container);
                
                // Get EventPriority enum and set priority to NORMAL
                Class<?> priorityClass = Class.forName("net.minecraftforge.eventbus.api.EventPriority");
                Object normalPriority = Enum.valueOf((Class<Enum>) priorityClass, "NORMAL");
                
                // Get RegisterKeyMappingsEvent class
                Class<?> eventClass = Class.forName("net.minecraftforge.client.event.RegisterKeyMappingsEvent");
                
                // Set up the listener callback
                java.util.function.Consumer<Object> handler = event -> {
                    try {
                        if (ToggleOffhand.keyMapping == null) {
                            ToggleOffhand.keyMapping = new KeyMapping(
                                    "key.toggle_offhand",
                                    InputConstants.Type.KEYSYM,
                                    GLFW.GLFW_KEY_UNKNOWN,
                                    KeyMapping.Category.MISC
                            );
                        }
                        event.getClass().getMethod("register", KeyMapping.class).invoke(event, ToggleOffhand.keyMapping);
                        ToggleOffhand.LOGGER.info("Registered keybind via Forge RegisterKeyMappingsEvent reflectively!");
                    } catch (Exception e) {
                        ToggleOffhand.LOGGER.error("Failed to register key mapping in event: ", e);
                    }
                };
                
                // Register the listener via eventBus.addListener(EventPriority, Class, Consumer)
                java.lang.reflect.Method addListenerMethod = eventBus.getClass().getMethod(
                        "addListener", 
                        priorityClass, 
                        Class.class, 
                        java.util.function.Consumer.class
                );
                addListenerMethod.invoke(eventBus, normalPriority, eventClass, handler);
                ToggleOffhand.LOGGER.info("Successfully registered reflective listener callback for RegisterKeyMappingsEvent");
            } else {
                ToggleOffhand.LOGGER.error("Could not find mod container for toggle_offhand to get event bus");
            }
        } catch (Exception e) {
            ToggleOffhand.LOGGER.error("Failed to register key mapping on Forge: ", e);
        }
    }
}
