package com.godlike.client.mixin;

import com.godlike.common.components.TelekinesisComponentKt;
import com.godlike.common.items.TkFocusItemKt;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.util.Mth;
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
        if (this.minecraft.player != null && this.minecraft.level != null && TkFocusItemKt.shouldAnimateTk(player)) {
            boolean hasTarget = TelekinesisComponentKt.telekinesis(player).getActiveTkTarget() != null;
            double bobAmplitude = hasTarget ? 0.04 : 0.005;
            float bobTime = hasTarget ? 25.0f : 8.0f;
            long gameTime = this.minecraft.level.getGameTime();
            float animationTimer = (gameTime + partialTicks) / bobTime;
            double bobHeight = Math.sin(animationTimer * Math.PI * 2) * bobAmplitude;
            poseStack.translate(0, bobHeight, 0);
        }
    }
}
