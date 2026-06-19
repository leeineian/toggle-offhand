package com.leeineian.toggle_offhand.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import com.leeineian.toggle_offhand.ToggleOffhand;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
    @Shadow
    protected abstract void renderPlayerArm(PoseStack pPoseStack, SubmitNodeCollector pBuffer, int pCombinedLight, float pEquippedProgress, float pSwingProgress, HumanoidArm pSide);

    @Inject(method = "submitArmWithItem", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V", shift = At.Shift.AFTER))
    private void submitArmWithItem(AbstractClientPlayer abstractClientPlayer, float f, float g, InteractionHand interactionHand, float swingProgress, ItemStack itemStack, float equippedProgress, PoseStack poseStack, SubmitNodeCollector submitNodeCollector, int combinedLight, CallbackInfo ci) {
        if (ToggleOffhand.doubleHands) {
            boolean mainHand = interactionHand == InteractionHand.MAIN_HAND;
            Item mainHandItem = abstractClientPlayer.getMainHandItem().getItem();
            String mainHandItemId = BuiltInRegistries.ITEM.getKey(mainHandItem).toString();
            HumanoidArm offArm = mainHand ? abstractClientPlayer.getMainArm() : abstractClientPlayer.getMainArm().getOpposite();
            if (itemStack.isEmpty() && !"minecraft:filled_map".equals(mainHandItemId) && (!mainHand && !abstractClientPlayer.isInvisible())) {
                this.renderPlayerArm(poseStack, submitNodeCollector, combinedLight, equippedProgress, swingProgress, offArm);
            }
        }
    }
}
