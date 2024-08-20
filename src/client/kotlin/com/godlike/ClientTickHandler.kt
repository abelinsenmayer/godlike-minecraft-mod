package com.godlike

import com.godlike.components.ModComponents
import com.godlike.util.showSelectionPreview
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.minecraft.client.Minecraft
import org.slf4j.LoggerFactory

/**
 * This class is the mod's entrypoint for everything that has to happen every tick on the client side.
 */
object ClientTickHandler {
    private val logger = LoggerFactory.getLogger("godlike")

    fun start() {
        // This code will run every tick on the client side
        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick {
            // if the player is in selection mode, display a preview of their selection
            val client = Minecraft.getInstance()
            client.player?.let {
                val inSelectionMode = ModComponents.SELECTION_MODE.get(client.player!!).getValue()
                if (inSelectionMode) {
                    showSelectionPreview(client)
                }
            }
        })
    }
}