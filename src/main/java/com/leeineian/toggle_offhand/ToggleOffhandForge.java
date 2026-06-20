package com.leeineian.toggle_offhand;

import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.eventbus.api.IEventBus;

@net.minecraftforge.fml.common.Mod("toggle_offhand")
public class ToggleOffhandForge {
    public ToggleOffhandForge(IEventBus modEventBus) {
        modEventBus.register(ToggleOffhandForge.class);
    }

    @SubscribeEvent
    public static void onRegisterKeyMappings(RegisterKeyMappingsEvent event) {
        if (ToggleOffhand.keyMapping == null) {
            ToggleOffhand.keyMapping = new KeyMapping(
                    "key.toggle_offhand",
                    InputConstants.Type.KEYSYM,
                    GLFW.GLFW_KEY_UNKNOWN,
                    KeyMapping.Category.MISC
            );
        }
        event.register(ToggleOffhand.keyMapping);
        ToggleOffhand.LOGGER.info("Registered keybind via Forge RegisterKeyMappingsEvent subscriber");
    }
}
