package com.leeineian.toggle_offhand.mixin;

import com.leeineian.toggle_offhand.compat.ClientCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import org.spongepowered.asm.mixin.injection.Coerce;

@Mixin(targets = "net.minecraft.class_2477")
public class LanguageMixin {
    @Inject(method = {"inject", "method_128", "method_29427"}, at = @At("RETURN"), require = 0)
    private static void onInject(@Coerce Object language, CallbackInfo ci) {
        ClientCompat.injectTranslations(language);
    }
}
