package com.godlike.common.networking

import com.godlike.common.Godlike.logger
import com.godlike.common.MOD_ID
import com.godlike.common.components.Mode
import com.godlike.common.components.setMode
import com.godlike.common.telekinesis.*
import com.godlike.common.vs2.Vs2Util
import io.wispforest.owo.network.OwoNetChannel
import net.minecraft.resources.ResourceLocation
import org.valkyrienskies.core.api.ships.ServerShip

object ModNetworking {
    @JvmField
    val CHANNEL: OwoNetChannel = OwoNetChannel.create(ResourceLocation.tryBuild(MOD_ID, "main"))

    fun register() {
        logger.info("Registering network channel")

        // Client-bound packets, deferred registration
        CHANNEL.registerClientboundDeferred(TracerParticlePacket::class.java)

        // Server-bound packets
        CHANNEL.registerServerbound(ServerBoundPacket::class.java) { packet, ctx ->
            logger.info("Received message on server: ${packet.message}")
        }

        CHANNEL.registerServerbound(TkPositionsPacket::class.java) { packet, ctx ->
            ctx.player.setMode(Mode.TELEKINESIS)
            tkPositions(packet.positions.toSet(), ctx.player)
        }

        CHANNEL.registerServerbound(TelekinesisControlsPacket::class.java) { packet, ctx ->
            serverTelekinesisTick(packet, ctx.player)
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

        CHANNEL.registerServerbound(SetChargingLaunchPacket::class.java) { packet, ctx ->
            setChargingLaunch(ctx.player, packet.chargingLaunch)
        }

        CHANNEL.registerServerbound(LaunchTkPacket::class.java) { packet, ctx ->
            launchTk(ctx.player, packet.targetedPosition)
        }

        CHANNEL.registerServerbound(UnstickTkPacket::class.java) { packet, ctx ->
            unstickTk(ctx.player)
        }
    }
}