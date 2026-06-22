package com.leeineian.toggle_offhand.mixin;

import com.leeineian.toggle_offhand.ToggleOffhand;
import com.leeineian.toggle_offhand.compat.ClientCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.Minecraft", remap = false)
public class MinecraftModernMixin {
    private boolean hasWarned = false;

    @Inject(method = {"tick", "m_91398_", "method_1574"}, remap = false, require = 0, expect = 0, at = @At("HEAD"))
    private void onTick(CallbackInfo ci) {
        Object player = ClientCompat.getPlayer(this);
        if (player != null) {
            if (com.leeineian.toggle_offhand.compat.VersionMappings.hasErrors() && !hasWarned) {
                hasWarned = true;
                Object msg = ClientCompat.createComponent("§c[Toggle Offhand] Warning: Compatibility mappings failed! Check log.");
                if (msg != null) {
                    ClientCompat.sendOverlay(player, msg);
                }
            }
        }

        if (ToggleOffhand.keyMapping != null && ClientCompat.consumeClick(ToggleOffhand.keyMapping)) {
            ToggleOffhand.LOGGER.info("Detected toggle offhand key click!");
            ToggleOffhand.doubleHands = !ToggleOffhand.doubleHands;
            ToggleOffhand.saveConfig(ClientCompat.getGameDirectory(this));

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
