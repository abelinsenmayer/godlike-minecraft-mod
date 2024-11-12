package com.godlike.common

import com.godlike.common.telekinesis.TkTicker
import net.fabricmc.fabric.api.event.lifecycle.v1.ServerTickEvents

/**
 * This class is the mod's entrypoint for everything that has to happen every tick on the server side.
 */
object ServerTickHandler {
    fun start() {
        ServerTickEvents.START_SERVER_TICK.register(ServerTickEvents.StartTick { server ->
            TkTicker.tick()
        })
    }
}