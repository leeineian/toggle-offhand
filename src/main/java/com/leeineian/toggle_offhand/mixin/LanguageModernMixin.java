package com.leeineian.toggle_offhand.mixin;

import com.leeineian.toggle_offhand.compat.ClientCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.locale.Language", remap = false)
public class LanguageModernMixin {
    @Inject(method = "inject", at = @At("RETURN"), require = 0)
    private static void onInject(net.minecraft.locale.Language language, CallbackInfo ci) {
        ClientCompat.injectTranslations(language);
    }
}
