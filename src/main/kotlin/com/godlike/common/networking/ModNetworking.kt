package com.godlike.common.networking

import com.godlike.common.Godlike.logger
import com.godlike.common.MOD_ID
import com.godlike.common.components.ModComponents
import com.godlike.common.telekinesis.handleTelekinesisControls
import com.godlike.common.telekinesis.createShipFromSelection
import io.wispforest.owo.network.OwoNetChannel
import net.minecraft.resources.ResourceLocation

object ModNetworking {
    @JvmField
    val CHANNEL: OwoNetChannel = OwoNetChannel.create(ResourceLocation.tryBuild(MOD_ID, "main"))

    fun register() {
        logger.info("Registering network channel")

        // Server-bound packets
        CHANNEL.registerServerbound(ServerBoundPacket::class.java) { packet, ctx ->
            logger.info("Received message on server: ${packet.message}")
        }

        CHANNEL.registerServerbound(DoSelectionPacket::class.java) { packet, ctx ->
            ModComponents.CURSORS.get(ctx.player).addAllPositions(packet.cursorPreviews)
            ModComponents.CURSORS.get(ctx.player).addPosition(packet.targetPosition)
            ModComponents.CURSOR_ANCHORS.get(ctx.player).addPosition(packet.targetPosition)
        }

        CHANNEL.registerServerbound(TkSelectionPackage::class.java) { packet, ctx ->
            createShipFromSelection(ctx.player)
            ModComponents.CURSORS.get(ctx.player).clearPositions()
            ModComponents.CURSOR_ANCHORS.get(ctx.player).clearPositions()
        }

        CHANNEL.registerServerbound(TelekinesisControlsPacket::class.java) { packet, ctx ->
            handleTelekinesisControls(packet, ctx.player)
        }

        // Client-bound packets, deferred registration
        CHANNEL.registerClientboundDeferred(TracerParticlePacket::class.java)
    }
}