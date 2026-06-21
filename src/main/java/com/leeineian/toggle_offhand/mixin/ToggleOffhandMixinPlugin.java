package com.leeineian.toggle_offhand.mixin;

import org.objectweb.asm.tree.ClassNode;
import org.spongepowered.asm.mixin.extensibility.IMixinConfigPlugin;
import org.spongepowered.asm.mixin.extensibility.IMixinInfo;

import java.util.List;
import java.util.Set;

public class ToggleOffhandMixinPlugin implements IMixinConfigPlugin {
    private static final boolean IS_MOJMAP_RUNTIME;
    private static final boolean IS_1212_OR_NEWER;
    static {
        boolean mojMap = false;
        try {
            if (ToggleOffhandMixinPlugin.class.getClassLoader().getResource("net/minecraft/class_310.class") == null) {
                mojMap = true;
            }
        } catch (Throwable t) {
            mojMap = true;
        }
        IS_MOJMAP_RUNTIME = mojMap;

        boolean is1212 = false;
        try {
            ClassLoader loader = ToggleOffhandMixinPlugin.class.getClassLoader();
            if (loader.getResource("net/minecraft/client/renderer/SubmitNodeCollector.class") != null ||
                loader.getResource("net/minecraft/class_9769.class") != null) {
                is1212 = true;
            }
        } catch (Throwable t) {
            // ignore
        }
        IS_1212_OR_NEWER = is1212;
    }

    @Override
    public void onLoad(String mixinPackage) {}

    @Override
    public String getRefMapperConfig() {
        return null;
    }

    @Override
    public boolean shouldApplyMixin(String targetClassName, String mixinClassName) {
        // --- MOJMAP MIXINS ---
        if (mixinClassName.endsWith("MinecraftModernMixin")) {
            return IS_MOJMAP_RUNTIME;
        }
        if (mixinClassName.endsWith("OptionsModernMixin")) {
            return IS_MOJMAP_RUNTIME;
        }
        if (mixinClassName.endsWith("LanguageModernMixin")) {
            return IS_MOJMAP_RUNTIME;
        }
        if (mixinClassName.endsWith("ClientLanguageModernMixin")) {
            return IS_MOJMAP_RUNTIME;
        }
        if (mixinClassName.endsWith("ItemInHandRendererModernMixin")) {
            return IS_MOJMAP_RUNTIME && IS_1212_OR_NEWER;
        }
        if (mixinClassName.endsWith("ItemInHandRendererLegacyModernMixin")) {
            return IS_MOJMAP_RUNTIME && !IS_1212_OR_NEWER;
        }

        // --- INTERMEDIARY (LEGACY) MIXINS ---
        if (mixinClassName.endsWith("MinecraftMixin")) {
            return !IS_MOJMAP_RUNTIME;
        }
        if (mixinClassName.endsWith("OptionsMixin")) {
            return !IS_MOJMAP_RUNTIME;
        }
        if (mixinClassName.endsWith("LanguageMixin")) {
            return !IS_MOJMAP_RUNTIME;
        }
        if (mixinClassName.endsWith("ClientLanguageMixin")) {
            return !IS_MOJMAP_RUNTIME;
        }
        if (mixinClassName.endsWith("ItemInHandRendererMixin")) {
            return !IS_MOJMAP_RUNTIME && IS_1212_OR_NEWER;
        }
        if (mixinClassName.endsWith("ItemInHandRendererLegacyMixin")) {
            return !IS_MOJMAP_RUNTIME && !IS_1212_OR_NEWER;
        }

        return true;
    }

    @Override
    public void acceptTargets(Set<String> myTargets, Set<String> otherTargets) {}

    @Override
    public List<String> getMixins() {
        return null;
    }

    @Override
    public void preApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}

    @Override
    public void postApply(String targetClassName, ClassNode targetClass, String mixinClassName, IMixinInfo mixinInfo) {}
}
