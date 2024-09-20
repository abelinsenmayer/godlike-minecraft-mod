package com.godlike.client

import com.godlike.client.keybind.handleModInputEvents
import com.godlike.client.keybind.sendTelekinesisTick
import com.godlike.client.util.isTargetContiguousWithSelection
import com.godlike.client.util.selectRaycastTarget
import com.godlike.common.components.Mode
import com.godlike.common.components.getMode
import com.godlike.common.components.selection
import com.godlike.common.components.telekinesis
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.player.LocalPlayer
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

    /**
     * Handles all client side actions that need to happen every tick for telekinesis "stuff".
     */
    private fun clientTelekinesisTick(player : LocalPlayer) {
        if (player.telekinesis().activeTkTarget == null && player.getMode() == Mode.TELEKINESIS) {
            selectRaycastTarget()
        }

        if (player.telekinesis().getTkTargets().isNotEmpty()) {
            sendTelekinesisTick()
        }
    }
}