package com.godlike.client.mixin;

import com.godlike.common.components.SelectionComponentKt;
import com.godlike.common.components.TelekinesisComponentKt;
import com.godlike.common.telekinesis.TkUtilKt;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.entity.player.PlayerRenderer;
import net.minecraft.world.InteractionHand;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
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
}
