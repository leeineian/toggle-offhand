package com.leeineian.toggle_offhand.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(targets = "net.minecraft.class_1078")
public class ClientLanguageMixin {
    @Inject(method = {"getOrDefault", "method_4679", "method_146"}, at = @At("HEAD"), cancellable = true, require = 0)
    private void onGetOrDefault(String key, String defaultValue, CallbackInfoReturnable<String> cir) {
        if ("key.toggle_offhand".equals(key)) {
            String translated = com.leeineian.toggle_offhand.compat.ClientCompat.getTranslation(key);
            if (translated != null) {
                cir.setReturnValue(translated);
            }
        }
    }

    @Inject(method = {"get", "method_48307"}, at = @At("HEAD"), cancellable = true, require = 0)
    private void onGet(String key, CallbackInfoReturnable<String> cir) {
        if ("key.toggle_offhand".equals(key)) {
            String translated = com.leeineian.toggle_offhand.compat.ClientCompat.getTranslation(key);
            if (translated != null) {
                cir.setReturnValue(translated);
            }
        }
    }
}
