package com.godlike.client

import com.godlike.client.keybind.handleModInputEvents
import com.godlike.client.keybind.tickLevitationStateUpdates
import com.godlike.client.keybind.handleTkControlInputs
import com.godlike.client.render.doPlacementFxRenderTick
import com.godlike.client.render.highlightSelectedArea
import com.godlike.client.render.doTkFxRenderTick
import com.godlike.client.util.selectRaycastTarget
import com.godlike.common.components.Mode
import com.godlike.common.components.getMode
import com.godlike.common.components.telekinesis
import com.godlike.common.items.TkFocusTier
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents
import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer

/**
 * This class is the mod's entrypoint for everything that has to happen every tick on the client side.
 */
object ClientTickHandler {
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

        WorldRenderEvents.AFTER_ENTITIES.register(WorldRenderEvents.AfterEntities { context ->
            Minecraft.getInstance().player?.let{ player ->
                context.consumers() ?: return@AfterEntities
                highlightSelectedArea(player, context.matrixStack())
                if (player.telekinesis().activeTkTarget != null) {
                    doTkFxRenderTick(player, context.matrixStack())
                }
                if (player.telekinesis().placementTarget != null) {
                    doPlacementFxRenderTick(player, context.matrixStack())
                }
//                renderPlayerAuras(player.clientLevel, context.matrixStack(), context.tickDelta())
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
            handleTkControlInputs()
        }

        if (player.telekinesis().tier != TkFocusTier.NONE) {
            tickLevitationStateUpdates()
        }
    }
}