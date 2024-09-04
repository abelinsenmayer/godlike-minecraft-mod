package com.godlike.client.networking

import com.godlike.client.fx.spawnTracerAtPoint
import com.godlike.common.networking.ModNetworking.CHANNEL
import com.godlike.common.networking.TracerParticlePacket

object ModClientNetworking {
    fun register() {
        // Register client-bound packets
        CHANNEL.registerClientbound(TracerParticlePacket::class.java) { packet, ctx ->
            spawnTracerAtPoint(packet.pos, ctx.player().level())
        }
    }
}