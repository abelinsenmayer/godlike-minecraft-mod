package com.godlike.client

import com.godlike.client.keybind.handleModInputEvents
import com.godlike.common.components.Mode
import com.godlike.common.components.getMode
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import org.slf4j.LoggerFactory

/**
 * This class is the mod's entrypoint for everything that has to happen every tick on the client side.
 */
object ClientTickHandler {
    private val logger = LoggerFactory.getLogger("godlike")

    fun start() {
        // This code will run every tick on the client side
        ClientTickEvents.START_CLIENT_TICK.register(ClientTickEvents.StartTick { client ->
            client.player?.let { player ->
                if (!client.isPaused) {
                    clientTelekinesisTick(player)
                }

                handleModInputEvents()
            }
        })
    }
}