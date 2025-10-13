package com.godlike.client.mixin;

import com.godlike.common.components.SelectionComponentKt;
import com.godlike.common.items.TkStaffItem;
import com.godlike.common.telekinesis.TkUtilKt;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
    @Shadow @Final
    private Minecraft minecraft;

    @Shadow
    protected abstract void applyItemArmTransform(PoseStack poseStack, HumanoidArm hand, float equippedProg);

    @Inject(at = @At("HEAD"), method = "renderArmWithItem")
    private void renderArmWithItem(
            AbstractClientPlayer player,
            float partialTicks,
            float pitch,
            InteractionHand hand,
            float swingProgress,
            ItemStack stack,
            float equippedProgress,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int combinedLight,
            CallbackInfo ci
    ) {
        // If the player has an active tk target, animate their hand
        if (this.minecraft.player != null && this.minecraft.level != null && TkUtilKt.shouldAnimateTk(player) && player instanceof LocalPlayer) {
            boolean chargingLaunch = SelectionComponentKt.selection((LocalPlayer)player).getClientChargingLaunch();
            boolean holdingStaff = player.getItemInHand(InteractionHand.MAIN_HAND).getItem() instanceof TkStaffItem;
            double bobAmplitude = chargingLaunch ? 0.005 : 0.04;
            float bobTime = chargingLaunch ? 8.0f : 25.0f;
            long gameTime = this.minecraft.level.getGameTime();
            float animationTimer = (gameTime + partialTicks) / bobTime;
            double bobHeight = Math.sin(animationTimer * Math.PI * 2) * bobAmplitude;
            poseStack.translate(chargingLaunch ? 0 : bobHeight/2, bobHeight, chargingLaunch ? 0 : bobHeight/2);
            if (chargingLaunch) {
                poseStack.translate(0, holdingStaff ? 1.0 : 0.3, 0);
            }
        }
    }
}
