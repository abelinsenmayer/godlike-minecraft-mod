package com.godlike.common.networking

import com.godlike.common.Godlike.logger
import com.godlike.common.MOD_ID
import com.godlike.common.components.ModComponents
import com.godlike.common.components.Mode
import com.godlike.common.components.setMode
import com.godlike.common.telekinesis.*
import com.godlike.common.vs2.Vs2Util
import io.wispforest.owo.network.OwoNetChannel
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import org.valkyrienskies.core.api.ships.ServerShip

object ModNetworking {
    @JvmField
    val CHANNEL: OwoNetChannel = OwoNetChannel.create(ResourceLocation.tryBuild(MOD_ID, "main"))

    fun register() {
        logger.info("Registering network channel")

        // Client-bound packets, deferred registration
        CHANNEL.registerClientboundDeferred(TracerParticlePacket::class.java)

        CHANNEL.registerClientboundDeferred(StartSelectingPacket::class.java)

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
            tickTelekinesisControls(packet, ctx.player)
        }

        CHANNEL.registerServerbound(SetModePacket::class.java) { packet, ctx ->
            ctx.player.setMode(Mode.valueOf(packet.modeName))
        }

        CHANNEL.registerServerbound(PickBlockToTkPacket::class.java) { packet, ctx ->
            pickBlockToTk(packet.pos, ctx.player)
        }

        CHANNEL.registerServerbound(PickEntityToTkPacket::class.java) { packet, ctx ->
            ctx.player().level().getEntity(packet.entityId)?.let { entity ->
                pickEntityToTk(entity, ctx.player)
            }
        }

        CHANNEL.registerServerbound(PickShipToTkPacket::class.java) { packet, ctx ->
            val ship = Vs2Util.getServerShipWorld(ctx.player.serverLevel()).loadedShips.getById(packet.shipId)
            ship?.let { pickShipToTk(it as ServerShip, ctx.player) }
        }

        CHANNEL.registerServerbound(DropTkPacket::class.java) { packet, ctx ->
            dropTk(ctx.player)
        }

        CHANNEL.registerServerbound(PlaceTkPacket::class.java) { packet, ctx ->
            placeTk(ctx.player)
        }

        CHANNEL.registerServerbound(HoverTkPacket::class.java) { packet, ctx ->
            hoverTk(ctx.player, packet.playerLookDirection)
        }
    }
}