package com.leeineian.toggle_offhand.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.client.resources.language.ClientLanguage", remap = false)
public class ClientLanguageModernMixin {
    @Inject(method = "getOrDefault", at = @At("HEAD"), cancellable = true, require = 0)
    private void onGetOrDefault(String key, String defaultValue, CallbackInfoReturnable<String> cir) {
        if ("key.toggle_offhand".equals(key)) {
            String translated = com.leeineian.toggle_offhand.compat.ClientCompat.getTranslation(key);
            if (translated != null) {
                cir.setReturnValue(translated);
            }
        }
    }
}
