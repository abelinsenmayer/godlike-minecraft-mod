package com.godlike.common.networking

import com.godlike.common.Godlike.logger
import com.godlike.common.MOD_ID
import com.godlike.common.components.Mode
import com.godlike.common.components.setMode
import com.godlike.common.components.telekinesis
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

        CHANNEL.registerClientboundDeferred(ResetDfsDepthPacket::class.java)

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
            placeActiveTarget(ctx.player)
        }

        CHANNEL.registerServerbound(PrecisePlacementPacket::class.java) { packet, ctx ->
            placePlacementTargetAt(ctx.player, packet.playerLookDirection)
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

        CHANNEL.registerServerbound(PlacementPacket::class.java) { packet, ctx ->
            if (packet.starting) {
                ctx.player.telekinesis().placementTarget = ctx.player.telekinesis().activeTkTarget
                ctx.player.setMode(Mode.PLACEMENT)
            } else {
                ctx.player.telekinesis().placementTarget?.let { ctx.player.telekinesis().addTarget(it) }
                ctx.player.telekinesis().placementTarget = null
                ctx.player.setMode(Mode.TELEKINESIS)
            }
        }

        CHANNEL.registerServerbound(PlacementDirectionPacket::class.java) { packet, ctx ->
            ctx.player.telekinesis().placementDirectionTop = packet.topDirection
            ctx.player.telekinesis().placementDirectionFront = packet.frontDirection
        }

        CHANNEL.registerServerbound(LevitatingStatusPacket::class.java) { packet, ctx ->
            ctx.player.telekinesis().isLevitating = packet.levitating
        }

        CHANNEL.registerServerbound(LevitationStartPacket::class.java) { packet, ctx ->
            ctx.player.telekinesis().isLevitating = true
        }

        CHANNEL.registerServerbound(ElytraBoostPacket::class.java) { packet, ctx ->
            ctx.player.handleElytraBoost()
        }

        CHANNEL.registerServerbound(StopFallFlyingPacket::class.java) { packet, ctx ->
            ctx.player.stopFallFlying()
            ctx.player.abilities.flying = true
            ctx.player.onUpdateAbilities()
        }
    }
}