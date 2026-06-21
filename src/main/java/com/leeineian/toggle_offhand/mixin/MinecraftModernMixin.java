package com.leeineian.toggle_offhand.mixin;

import com.leeineian.toggle_offhand.ToggleOffhand;
import com.leeineian.toggle_offhand.compat.ClientCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.Minecraft", remap = false)
public class MinecraftModernMixin {
    @Inject(method = "handleKeybinds", require = 0, expect = 0, at = @At("HEAD"))
    private void onHandleKeybinds(CallbackInfo ci) {
        if (ToggleOffhand.keyMapping != null) {
            while (ClientCompat.consumeClick(ToggleOffhand.keyMapping)) {
                ToggleOffhand.doubleHands = !ToggleOffhand.doubleHands;
                ToggleOffhand.saveConfig(ClientCompat.getGameDirectory(this));

                Object player = ClientCompat.getPlayer(this);
                if (player != null) {
                    String title = ClientCompat.getTranslation("key.toggle_offhand");
                    String state = ClientCompat.getTranslation(ToggleOffhand.doubleHands ? "options.on" : "options.off");
                    String message = (title != null ? title : "Toggle Offhand") + " : " + (state != null ? state : (ToggleOffhand.doubleHands ? "ON" : "OFF"));
                    
                    Object component = ClientCompat.createComponent(message);
                    if (component != null) {
                        ClientCompat.sendOverlay(player, component);
                    }
                }
            }
        }
    }
}
