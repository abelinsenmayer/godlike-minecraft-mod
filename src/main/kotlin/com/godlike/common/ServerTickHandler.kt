package com.godlike.common

import com.godlike.common.components.telekinesis
import com.godlike.common.items.TkFocusTier
import com.godlike.common.telekinesis.handleTkMovementInputs
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents

object ServerTickHandler {
    fun start() {
        ServerTickEvents.START_SERVER_TICK.register { server ->
            server.playerList.players.forEach { player ->

            }
        }
    }
}