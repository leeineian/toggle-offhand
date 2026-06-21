package com.leeineian.toggle_offhand.mixin;

import com.leeineian.toggle_offhand.ToggleOffhand;
import com.leeineian.toggle_offhand.compat.ClientCompat;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Coerce;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(targets = "net.minecraft.class_759")
public abstract class ItemInHandRendererLegacyMixin {
    private static java.lang.reflect.Method renderPlayerArmMethod = null;

    private void invokeRenderPlayerArm(Object instance, Object poseStack, Object buffer, int combinedLight, float equippedProgress, float swingProgress, Object side) {
        if (renderPlayerArmMethod == null) {
            try {
                for (java.lang.reflect.Method m : instance.getClass().getDeclaredMethods()) {
                    Class<?>[] types = m.getParameterTypes();
                    if (types.length == 6 &&
                        (types[0].getName().endsWith(".PoseStack") || types[0].getName().endsWith(".class_4587")) &&
                        (types[1].getName().endsWith(".MultiBufferSource") || types[1].getName().endsWith(".class_4597")) &&
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

    private void handleLegacyRender(Object abstractClientPlayer, Object interactionHand, Object itemStack, Object poseStack, Object bufferSource, int combinedLight, float equippedProgress, float swingProgress) {
        if (ToggleOffhand.doubleHands) {
            boolean mainHand = interactionHand != null && interactionHand.toString().equals("MAIN_HAND");
            Object mainHandItem = ClientCompat.getItemStackItem(ClientCompat.getMainHandItem(abstractClientPlayer));
            String mainHandItemId = ClientCompat.getItemId(mainHandItem);
            Object mainArm = ClientCompat.getMainArm(abstractClientPlayer);
            Object offArm = mainHand ? mainArm : ClientCompat.getOppositeArm(mainArm);
            if (ClientCompat.isItemStackEmpty(itemStack) && !"minecraft:filled_map".equals(mainHandItemId) && (!mainHand && !ClientCompat.isEntityInvisible(abstractClientPlayer))) {
                this.invokeRenderPlayerArm(this, poseStack, bufferSource, combinedLight, equippedProgress, swingProgress, offArm);
            }
        }
    }

    @Inject(method = "renderArmWithItem", remap = false, require = 0, expect = 0, at = @At(value = "INVOKE", target = "Lnet/minecraft/class_4587;method_22903()V", shift = At.Shift.AFTER))
    private void renderArmWithItem(@Coerce Object abstractClientPlayer, float f, float g, @Coerce Object interactionHand, float swingProgress, @Coerce Object itemStack, float equippedProgress, @Coerce Object poseStack, @Coerce Object bufferSource, int combinedLight, CallbackInfo ci) {
        this.handleLegacyRender(abstractClientPlayer, interactionHand, itemStack, poseStack, bufferSource, combinedLight, equippedProgress, swingProgress);
    }

    @Inject(method = {"method_3228", "method_22920"}, remap = false, require = 0, expect = 0, at = @At(value = "INVOKE", target = "Lnet/minecraft/class_4587;method_22903()V", shift = At.Shift.AFTER))
    private void renderArmWithItemIntermediary(@Coerce Object abstractClientPlayer, float f, float g, @Coerce Object interactionHand, float swingProgress, @Coerce Object itemStack, float equippedProgress, @Coerce Object poseStack, @Coerce Object bufferSource, int combinedLight, CallbackInfo ci) {
        this.handleLegacyRender(abstractClientPlayer, interactionHand, itemStack, poseStack, bufferSource, combinedLight, equippedProgress, swingProgress);
    }
}
