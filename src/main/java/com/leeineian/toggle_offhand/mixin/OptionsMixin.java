package com.leeineian.toggle_offhand.mixin;

import com.leeineian.toggle_offhand.ToggleOffhand;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.io.File;
import java.util.Arrays;

@Mixin(Options.class)
public class OptionsMixin {
    @Shadow
    public KeyMapping[] keyMappings;

    @Inject(method = "<init>", at = @At("RETURN"))
    private void onInit(Minecraft minecraft, File file, CallbackInfo ci) {
        if (ToggleOffhand.keyMapping == null) {
            ToggleOffhand.init(minecraft);
        } else {
            ToggleOffhand.loadConfig(minecraft.gameDirectory);
        }
        KeyMapping[] original = this.keyMappings;
        KeyMapping[] copy = Arrays.copyOf(original, original.length + 1);
        copy[original.length] = ToggleOffhand.keyMapping;
        this.keyMappings = copy;
    }
}
