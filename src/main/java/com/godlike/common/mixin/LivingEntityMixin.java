package com.godlike.common.mixin;

import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static com.godlike.common.util.KineticDamageUtilKt.*;
import static com.godlike.common.util.MathUtilKt.toVec3i;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {
    /**
     * Injects into the aiStep method of LivingEntity to add a check for horizontal collision.
     * This effectively adds kinetic damage at all times as though the entity is flying using an eltrya.
     * Also applies kinetic damage when colliding with other entities.
     */
    @Inject(method = "aiStep", at = @At("HEAD"))
    private void injectAiStep(CallbackInfo ci) {
        LivingEntity thisAsAccessor = (LivingEntity)(Object)this;
        if (!thisAsAccessor.level().isClientSide) {
            float damage;
            if (thisAsAccessor.horizontalCollision) {
                damage = kineticDamage(thisAsAccessor);
            } else {
                List<Entity> collidingEntities = findCollidingEntities(thisAsAccessor);
                if (!collidingEntities.isEmpty()) {
                    damage = kineticDamage(thisAsAccessor);
                    collidingEntities.forEach(entity -> {
                        if (entity instanceof LivingEntity) {
                            entity.hurt(thisAsAccessor.damageSources().flyIntoWall(), damage);
                            entity.playSound(damage > 4 ? SoundEvents.GENERIC_BIG_FALL : SoundEvents.GENERIC_SMALL_FALL, 1.0F, 1.0F);
                        }
                    });
                } else {
                    damage = 0.0F;
                }
            }

            if (damage > 0.0F) {
                thisAsAccessor.hurt(thisAsAccessor.damageSources().flyIntoWall(), damage);
                thisAsAccessor.playSound(damage > 4 ? SoundEvents.GENERIC_BIG_FALL : SoundEvents.GENERIC_SMALL_FALL, 1.0F, 1.0F);
            }
        }
    }
}
