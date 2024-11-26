package com.godlike.common.items

import com.godlike.common.Godlike.logger
import com.godlike.common.MOD_ID
import com.godlike.common.components.telekinesis
import net.fabricmc.fabric.api.`object`.builder.v1.client.model.FabricModelPredicateProviderRegistry
import net.fabricmc.fabric.mixin.`object`.builder.client.ModelPredicateProviderRegistryAccessor
import net.fabricmc.fabric.mixin.`object`.builder.client.ModelPredicateProviderRegistrySpecificAccessor
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player

object ModModelPredicates {
    init {
        logger.info("Registering model predicates")
        FabricModelPredicateProviderRegistry.register(
            ModItems.BASIC_TK_FOCUS,
            ResourceLocation(MOD_ID, "has_active_target")
        ) { stack, world, entity, seed ->
            if (entity is Player) {
                if (entity.telekinesis().activeTkTarget != null) {
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