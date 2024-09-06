package com.godlike.common.networking

import com.godlike.common.Godlike.logger
import com.godlike.common.MOD_ID
import com.godlike.common.components.ModComponents
import com.godlike.common.components.Mode
import com.godlike.common.components.setMode
import com.godlike.common.telekinesis.handleTelekinesisControls
import com.godlike.common.telekinesis.createShipFromSelection
import com.godlike.common.telekinesis.pickBlockToTk
import com.godlike.common.telekinesis.pickEntityToTk
import io.wispforest.owo.network.OwoNetChannel
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.EntityType

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

        CHANNEL.registerServerbound(SetModePacket::class.java) { packet, ctx ->
            ctx.player.setMode(Mode.valueOf(packet.modeName))
        }

        CHANNEL.registerServerbound(PickBlockToTkPacket::class.java) { packet, ctx ->
            pickBlockToTk(packet.pos, ctx.player)
        }

        CHANNEL.registerServerbound(PickEntityToTkPacket::class.java) { packet, ctx ->
            val entity = EntityType.create(packet.entityData, ctx.player.level()).orElse(null)
            pickEntityToTk(entity, ctx.player)
        }

        // Client-bound packets, deferred registration
        CHANNEL.registerClientboundDeferred(TracerParticlePacket::class.java)
    }
}