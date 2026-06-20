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
            
            // Get RegisterKeyMappingsEvent class and its static BUS field
            Class<?> eventClass = Class.forName("net.minecraftforge.client.event.RegisterKeyMappingsEvent");
            Object eventBus = eventClass.getField("BUS").get(null);
            
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
                    ToggleOffhand.isForgeRegistered = true;
                    ToggleOffhand.LOGGER.info("Registered keybind via Forge RegisterKeyMappingsEvent reflectively!");
                } catch (Exception e) {
                    ToggleOffhand.LOGGER.error("Failed to register key mapping in event: ", e);
                }
            };
            
            // Register the listener via eventBus.addListener(Consumer)
            Class<?> eventBusClass = Class.forName("net.minecraftforge.eventbus.api.bus.EventBus");
            java.lang.reflect.Method addListenerMethod = eventBusClass.getMethod(
                    "addListener", 
                    java.util.function.Consumer.class
            );
            addListenerMethod.invoke(eventBus, handler);
            ToggleOffhand.LOGGER.info("Successfully registered reflective listener callback for RegisterKeyMappingsEvent");
            
            // Get ClientTickEvent$Post class and its static BUS field
            Class<?> tickEventClass = Class.forName("net.minecraftforge.event.TickEvent$ClientTickEvent$Post");
            Object tickEventBus = tickEventClass.getField("BUS").get(null);
            
            // Set up the client tick handler callback
            java.util.function.Consumer<Object> tickHandler = event -> {
                try {
                    if (ToggleOffhand.keyMapping != null) {
                        while (ToggleOffhand.keyMapping.consumeClick()) {
                            ToggleOffhand.doubleHands = !ToggleOffhand.doubleHands;
                            ToggleOffhand.saveConfig(net.minecraft.client.Minecraft.getInstance().gameDirectory);

                            if (net.minecraft.client.Minecraft.getInstance().player != null) {
                                net.minecraft.network.chat.Component component;
                                if (ToggleOffhand.doubleHands) {
                                    component = net.minecraft.network.chat.Component.translatable("key.toggle_offhand").append(" : ").append(net.minecraft.network.chat.Component.translatable("options.on"));
                                } else {
                                    component = net.minecraft.network.chat.Component.translatable("key.toggle_offhand").append(" : ").append(net.minecraft.network.chat.Component.translatable("options.off"));
                                }
                                net.minecraft.client.Minecraft.getInstance().player.sendOverlayMessage(component);
                            }
                        }
                    }
                } catch (Exception e) {
                    ToggleOffhand.LOGGER.error("Failed handling client tick for keypress: ", e);
                }
            };
            
            addListenerMethod.invoke(tickEventBus, tickHandler);
            ToggleOffhand.LOGGER.info("Successfully registered reflective listener callback for ClientTickEvent.Post");
        } catch (Exception e) {
            ToggleOffhand.LOGGER.error("Failed to register key mapping on Forge: ", e);
        }
    }
}
