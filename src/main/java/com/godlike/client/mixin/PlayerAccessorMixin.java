package com.godlike.client.mixin;

import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(Player.class)
public interface PlayerAccessorMixin {
    @Accessor("jumpTriggerTime")
    int getJumpTriggerTime();
    @Accessor("jumpTriggerTime")
    void setJumpTriggerTime(int jumpTriggerTime);
}
