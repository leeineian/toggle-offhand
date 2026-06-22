package com.leeineian.toggle_offhand.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.resources.language.ClientLanguage", remap = false)
public class ClientLanguageModernMixin {
    @Inject(method = "<init>", at = @At("RETURN"), require = 0)
    private void onInit(CallbackInfo ci) {
        com.leeineian.toggle_offhand.compat.ClientCompat.injectTranslations(this);
    }
}
