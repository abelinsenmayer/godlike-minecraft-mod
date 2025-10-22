package com.godlike.common

import com.godlike.common.components.telekinesis
import com.godlike.common.items.TkFocusTier
import com.godlike.common.telekinesis.applyAbilitiesForTkTier
import com.godlike.common.telekinesis.handleTkMovementInputs
import com.godlike.common.telekinesis.updateTkTierByInventory
import net.fabricmc.fabric.api.entity.event.v1.ServerPlayerEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerEntityEvents
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents
import net.minecraft.world.entity.player.Player

object ServerTickHandler {
    fun start() {
        ServerTickEvents.START_SERVER_TICK.register { server ->
            server.playerList.players.forEach { player ->

            }
        }

        ServerPlayerEvents.AFTER_RESPAWN.register { server, player, _ ->
            player.updateTkTierByInventory()
        }

        ServerEntityEvents.ENTITY_LOAD.register { entity, level ->
            if (entity !is Player) {
                return@register
            }

            entity.applyAbilitiesForTkTier()
        }
    }
}