package com.godlike.common.mixin;

import com.godlike.common.items.TkFocusItem;
import com.godlike.common.items.TkFocusItemKt;
import io.github.fabricators_of_create.porting_lib.attributes.extensions.PlayerAttributesExtensions;
import io.github.fabricators_of_create.porting_lib.entity.extensions.PlayerExtension;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerMixin extends LivingEntity implements PlayerAttributesExtensions, PlayerExtension {

    @Shadow public abstract ItemStack getItemBySlot(EquipmentSlot slot);

    protected PlayerMixin(EntityType<? extends LivingEntity> entityType, Level level) {
        super(entityType, level);
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void injectTick(CallbackInfo ci) {
        if (this.level().isClientSide) {
            return;
        }

        TkFocusItemKt.updateTkStateByItem((ServerPlayer)(Object)this, this.getItemBySlot(EquipmentSlot.MAINHAND));
    }
}
