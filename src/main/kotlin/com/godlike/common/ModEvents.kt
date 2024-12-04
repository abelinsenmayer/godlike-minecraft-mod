package com.godlike.common

import com.godlike.common.Godlike.logger
import com.godlike.common.components.Mode
import com.godlike.common.components.getMode
import com.godlike.common.components.telekinesis
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents
import net.fabricmc.fabric.api.entity.event.v1.ServerLivingEntityEvents.ALLOW_DEATH
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents.ENTITY_LOAD
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents.ENTITY_UNLOAD
import net.minecraft.world.entity.player.Player

object ModEvents {
    init {
        ALLOW_DEATH.register(ServerLivingEntityEvents.AllowDeath { entity, damageSource, amount ->
            // When the player dies, drop all TK targets
            if (entity is Player && entity.getMode() == Mode.TELEKINESIS) {
                entity.telekinesis().clearTargets()
            }
            true
        })

        ENTITY_UNLOAD.register(ServerEntityEvents.Unload { entity, serverWorld ->
            // When the player unloads, drop all TK targets
            if (entity is Player && entity.getMode() == Mode.TELEKINESIS) {
                entity.telekinesis().clearTargets()
            }
        })

        ENTITY_LOAD.register(ServerEntityEvents.Load { entity, serverWorld ->
            // When the player loads in, drop all TK targets.
            // This redundancy ensures that things will be dropped correctly in single-player.
            if (entity is Player && entity.getMode() == Mode.TELEKINESIS) {
                entity.telekinesis().clearTargets()
            }
        })
    }
}