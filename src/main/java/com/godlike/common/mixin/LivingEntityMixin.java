package com.godlike.common.mixin;

import com.godlike.common.Godlike;
import com.godlike.common.components.TelekinesisComponentKt;
import com.godlike.common.items.TkPower;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(LivingEntity.class)
public abstract class LivingEntityMixin extends Entity implements Attackable {
    public LivingEntityMixin(EntityType<?> entityType, Level level) {
        super(entityType, level);
    }

    @Unique
    private boolean shouldKeepFallFlying = false;

    @Inject(method = "updateFallFlying", at = @At("HEAD"))
    void injectUpdateFallFlyingHead(CallbackInfo ci) {
        LivingEntity self = (LivingEntity)(Object)this;
        if (!(self instanceof ServerPlayer)) {
            return;
        }

        // Store the player's fallFlying state so that we can reference it at the tail of the method
        // (the real implementation overwrites it in the middle)
        this.shouldKeepFallFlying = this.getSharedFlag(7);
    }

    @Inject(method = "updateFallFlying", at = @At("TAIL"))
    void injectUpdateFallFlyingTail(CallbackInfo ci) {
        LivingEntity self = (LivingEntity)(Object)this;
        if (!(self instanceof ServerPlayer)) {
            return;
        }

        // If the player is fall-flying and has the ELYTRA_BOOST power, keep them fall-flying even if they aren't wearing an elytra
        if (this.shouldKeepFallFlying && !self.onGround() && !self.isPassenger() && !self.hasEffect(MobEffects.LEVITATION)) {
            boolean hasElytraBoostPower = TelekinesisComponentKt.telekinesis((Player)self).getTier().getGrantedPowers().contains(TkPower.ELYTRA_BOOST);
            if (hasElytraBoostPower) {
                if (!self.level().isClientSide) {
                    ((Player) self).startFallFlying();
                }
            }
        }
    }
}
