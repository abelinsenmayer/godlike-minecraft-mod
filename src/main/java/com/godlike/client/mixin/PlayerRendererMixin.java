package com.godlike.client.mixin;

import com.godlike.client.render.AuraRendererKt;
import com.godlike.common.Godlike;
import com.godlike.common.components.SelectionComponentKt;
import com.godlike.common.components.TelekinesisComponentKt;
import com.godlike.common.telekinesis.TkUtilKt;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.layers.ArrowLayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerRenderer.class)
public abstract class PlayerRendererMixin {
    @Inject(at = @At("HEAD"), method = "getArmPose", cancellable = true)
    private static void getArmPose(AbstractClientPlayer player, InteractionHand hand, CallbackInfoReturnable<HumanoidModel.ArmPose> cir) {
        // If the player has an active telekinesis target, pose their arm
        if (TkUtilKt.shouldAnimateTk(player)) {
            cir.setReturnValue(HumanoidModel.ArmPose.BOW_AND_ARROW);
            if (player instanceof LocalPlayer && SelectionComponentKt.selection((LocalPlayer)player).getClientChargingLaunch()) {
                cir.setReturnValue(HumanoidModel.ArmPose.THROW_SPEAR);
            }
        }
    }

    @Inject(at = @At("TAIL"), method = "render*")
    public void injectRender(AbstractClientPlayer entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight, CallbackInfo ci) {
        AuraRendererKt.renderAuraForPlayer(entity, poseStack, partialTicks);
        PlayerRenderer renderer = (PlayerRenderer)(Object)this;
//        AuraRendererKt.testRender(entity, poseStack, partialTicks, renderer.getModel(), packedLight, buffer, renderer, entityYaw);
    }

//    @Inject(at = @At("TAIL"), method = "<init>(Lnet/minecraft/client/renderer/entity/EntityRendererProvider$Context;Z)V")
//    private void init(EntityRendererProvider.Context context, boolean useSlimModel, CallbackInfo info) {
//        PlayerRenderer renderer = (PlayerRenderer)(Object)this;
//        ((LivingEntityRendererInvoker)renderer).addLayer(new ArrowLayer<>(context, renderer));
//    }
}
