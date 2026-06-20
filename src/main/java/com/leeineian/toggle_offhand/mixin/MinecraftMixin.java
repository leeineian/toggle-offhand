package com.leeineian.toggle_offhand.mixin;

import com.leeineian.toggle_offhand.ToggleOffhand;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Minecraft.class)
public class MinecraftMixin {
    @Inject(method = "handleKeybinds", at = @At("HEAD"))
    private void onHandleKeybinds(CallbackInfo ci) {
        ToggleOffhand.LOGGER.info("onHandleKeybinds called! keyMapping: {}", ToggleOffhand.keyMapping);
        if (ToggleOffhand.keyMapping != null) {
            // Log when key is pressed/detected
            if (ToggleOffhand.keyMapping.isDown()) {
                ToggleOffhand.LOGGER.info("ToggleOffhand keyMapping is down!");
            }
            while (ToggleOffhand.keyMapping.consumeClick()) {
                ToggleOffhand.LOGGER.info("ToggleOffhand keyMapping click consumed!");
                ToggleOffhand.doubleHands = !ToggleOffhand.doubleHands;
                ToggleOffhand.saveConfig(((Minecraft) (Object) this).gameDirectory);

                if (((Minecraft) (Object) this).player != null) {
                    Component component;
                    if (ToggleOffhand.doubleHands) {
                        component = Component.translatable("key.toggle_offhand").append(" : ").append(Component.translatable("options.on"));
                    } else {
                        component = Component.translatable("key.toggle_offhand").append(" : ").append(Component.translatable("options.off"));
                    }
                    ((Minecraft) (Object) this).player.sendOverlayMessage(component);
                }
            }
        }
    }
}
