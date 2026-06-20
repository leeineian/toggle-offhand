package com.leeineian.toggle_offhand.mixin;

import com.leeineian.toggle_offhand.ToggleOffhand;
import net.minecraft.client.KeyMapping;
import com.mojang.blaze3d.platform.InputConstants;
import org.lwjgl.glfw.GLFW;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraftforge.client.event.RegisterKeyMappingsEvent", remap = false)
public class RegisterKeyMappingsEventMixin {
    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(CallbackInfo ci) {
        try {
            if (ToggleOffhand.keyMapping == null) {
                ToggleOffhand.keyMapping = new KeyMapping(
                        "key.toggle_offhand",
                        InputConstants.Type.KEYSYM,
                        GLFW.GLFW_KEY_UNKNOWN,
                        KeyMapping.Category.MISC
                );
            }
            this.getClass().getMethod("register", KeyMapping.class).invoke(this, ToggleOffhand.keyMapping);
            ToggleOffhand.LOGGER.info("Successfully registered key mapping with Forge RegisterKeyMappingsEvent");
        } catch (Exception e) {
            ToggleOffhand.LOGGER.error("Failed to register key mapping with Forge RegisterKeyMappingsEvent: ", e);
        }
    }
}
