package com.leeineian.toggle_offhand.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.leeineian.toggle_offhand.ToggleOffhand;
import com.leeineian.toggle_offhand.compat.ClientCompat;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.client.renderer.ItemInHandRenderer", remap = false)
public abstract class ItemInHandRendererModernMixin {
    private static java.lang.reflect.Method renderPlayerArmMethod = null;

    private void invokeRenderPlayerArm(Object instance, PoseStack poseStack, Object buffer, int combinedLight, float equippedProgress, float swingProgress, HumanoidArm side) {
        if (renderPlayerArmMethod == null) {
            try {
                for (java.lang.reflect.Method m : instance.getClass().getDeclaredMethods()) {
                    Class<?>[] types = m.getParameterTypes();
                    if (types.length == 6 &&
                        (types[0].getName().endsWith(".PoseStack") || types[0].getName().endsWith(".class_4587")) &&
                        (types[1].getName().endsWith(".SubmitNodeCollector") || types[1].getName().endsWith(".class_9769")) &&
                        types[2] == int.class &&
                        types[3] == float.class &&
                        types[4] == float.class &&
                        (types[5].getName().endsWith(".HumanoidArm") || types[5].getName().endsWith(".class_1306"))) {
                        m.setAccessible(true);
                        renderPlayerArmMethod = m;
                        break;
                    }
                }
            } catch (Exception e) {
                ToggleOffhand.LOGGER.error("Failed to find renderPlayerArm method reflectively", e);
            }
        }
        if (renderPlayerArmMethod != null) {
            try {
                renderPlayerArmMethod.invoke(instance, poseStack, buffer, combinedLight, equippedProgress, swingProgress, side);
            } catch (Exception e) {
                ToggleOffhand.LOGGER.error("Failed to invoke renderPlayerArm reflectively: ", e);
            }
        }
    }

    private void handleModernRender(AbstractClientPlayer abstractClientPlayer, InteractionHand interactionHand, ItemStack itemStack, PoseStack poseStack, Object submitNodeCollector, int combinedLight, float equippedProgress, float swingProgress) {
        if (ToggleOffhand.doubleHands) {
            boolean mainHand = interactionHand == InteractionHand.MAIN_HAND;
            Item mainHandItem = abstractClientPlayer.getMainHandItem().getItem();
            String mainHandItemId = ClientCompat.getItemId(mainHandItem);
            HumanoidArm offArm = mainHand ? abstractClientPlayer.getMainArm() : abstractClientPlayer.getMainArm().getOpposite();
            if (itemStack.isEmpty() && !"minecraft:filled_map".equals(mainHandItemId) && (!mainHand && !abstractClientPlayer.isInvisible())) {
                this.invokeRenderPlayerArm(this, poseStack, submitNodeCollector, combinedLight, equippedProgress, swingProgress, offArm);
            }
        }
    }

    @Inject(method = {"submitArmWithItem", "m_319808_"}, remap = false, require = 0, expect = 0, at = {
        @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V", shift = At.Shift.AFTER),
        @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;m_85836_()V", shift = At.Shift.AFTER)
    })
    private void submitArmWithItem(AbstractClientPlayer abstractClientPlayer, float f, float g, InteractionHand interactionHand, float swingProgress, ItemStack itemStack, float equippedProgress, PoseStack poseStack, @Coerce Object submitNodeCollector, int combinedLight, CallbackInfo ci) {
        this.handleModernRender(abstractClientPlayer, interactionHand, itemStack, poseStack, submitNodeCollector, combinedLight, equippedProgress, swingProgress);
    }

    @Inject(method = {"renderArmWithItem", "m_109371_"}, remap = false, require = 0, expect = 0, at = {
        @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V", shift = At.Shift.AFTER),
        @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;m_85836_()V", shift = At.Shift.AFTER)
    })
    private void renderArmWithItem(AbstractClientPlayer abstractClientPlayer, float f, float g, InteractionHand interactionHand, float swingProgress, ItemStack itemStack, float equippedProgress, PoseStack poseStack, @Coerce Object submitNodeCollector, int combinedLight, CallbackInfo ci) {
        this.handleModernRender(abstractClientPlayer, interactionHand, itemStack, poseStack, submitNodeCollector, combinedLight, equippedProgress, swingProgress);
    }

    @Inject(method = {"submitArmWithItem", "m_319808_"}, remap = false, require = 0, expect = 0, cancellable = true, at = @At("HEAD"))
    private void onSubmitArmWithItem(AbstractClientPlayer abstractClientPlayer, float f, float g, InteractionHand interactionHand, float swingProgress, ItemStack itemStack, float equippedProgress, PoseStack poseStack, @Coerce Object submitNodeCollector, int combinedLight, CallbackInfo ci) {
        if (!ToggleOffhand.doubleHands && interactionHand == InteractionHand.OFF_HAND) {
            ci.cancel();
        }
    }

    @Inject(method = {"renderArmWithItem", "m_109371_"}, remap = false, require = 0, expect = 0, cancellable = true, at = @At("HEAD"))
    private void onRenderArmWithItem(AbstractClientPlayer abstractClientPlayer, float f, float g, InteractionHand interactionHand, float swingProgress, ItemStack itemStack, float equippedProgress, PoseStack poseStack, @Coerce Object submitNodeCollector, int combinedLight, CallbackInfo ci) {
        if (!ToggleOffhand.doubleHands && interactionHand == InteractionHand.OFF_HAND) {
            ci.cancel();
        }
    }
}
