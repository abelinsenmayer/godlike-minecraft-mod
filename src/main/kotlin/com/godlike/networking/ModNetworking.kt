package com.godlike.networking

import com.godlike.Godlike.logger
import com.godlike.MOD_ID
import com.godlike.components.ModComponents
import io.wispforest.owo.network.OwoNetChannel
import net.minecraft.util.Identifier

object ModNetworking {
    @JvmField
    val CHANNEL: OwoNetChannel = OwoNetChannel.create(Identifier.of(MOD_ID, "main"))

    fun register() {
        logger.info("Registering network channel")
        CHANNEL.registerServerbound(ServerBoundPacket::class.java) { packet, ctx ->
            logger.info("Received message on server: ${packet.message}")
        }

        CHANNEL.registerServerbound(DoSelectionPacket::class.java) { packet, ctx ->
            ModComponents.CURSORS.get(ctx.player).addAllPositions(packet.cursorPreviews)
            ModComponents.CURSOR_ANCHORS.get(ctx.player).addPosition(packet.targetPosition)
        }
    }
}