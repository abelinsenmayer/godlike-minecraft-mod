package com.godlike.common.items

import com.godlike.common.Godlike.logger
import com.godlike.common.MOD_ID
import net.fabricmc.fabric.api.`object`.builder.v1.client.model.FabricModelPredicateProviderRegistry
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Player

object ModModelPredicates {
    init {
        logger.info("Registering model predicates")
        FabricModelPredicateProviderRegistry.register(
            ModItems.SIMPLE_TK_FOCUS,
            ResourceLocation(MOD_ID, "has_active_target")
        ) { stack, world, entity, seed ->
            if (entity is Player) {
                if (entity.shouldAnimateTk()) {
                    1.0f
                } else {
                    0.0f
                }
            } else {
                0.0f
            }
        }
    }
}