package com.godlike.client.networking

import com.godlike.common.components.selection
import com.godlike.common.networking.ModNetworking.CHANNEL
import com.godlike.common.networking.ResetDfsDepthPacket

object ModClientNetworking {
    fun register() {
        CHANNEL.registerClientbound(ResetDfsDepthPacket::class.java) { _, ctx ->
            ctx.player().selection().dfsDepth = 0
        }
    }
}