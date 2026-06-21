package com.leeineian.toggle_offhand.compat;

public class ForgeCompat {
    public static void register() {
        // Key mapping and keypress handling are registered globally via Mixins 
        // on net.minecraft.client.Options and net.minecraft.client.Minecraft,
        // which works transparently across Fabric, Quilt, Forge, and NeoForge loaders.
    }
}
