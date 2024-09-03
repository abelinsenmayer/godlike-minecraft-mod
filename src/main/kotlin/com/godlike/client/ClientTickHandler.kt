package com.godlike.client

import com.godlike.client.keybind.doTelekinesisKeybindControls
import com.godlike.common.components.ModComponents
import com.godlike.client.util.showSelectionPreview
import com.godlike.common.components.Mode
import com.godlike.common.components.getMode
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

            val client = Minecraft.getInstance()
            client.player?.let { player ->
                // if the player is in selection mode, display a preview of their selection
                if (player.getMode() == Mode.SELECTING) {
                    showSelectionPreview(client)
                }

                if (!client.isPaused) {
                    doTelekinesisKeybindControls()
                }
            }
        })
    }
}