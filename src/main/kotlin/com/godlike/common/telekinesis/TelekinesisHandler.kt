package com.godlike.common.telekinesis

import com.godlike.common.components.ModComponents
import com.godlike.client.fx.spawnTracerAtPoint
import com.godlike.client.fx.traceVectorFromPoint
import com.godlike.common.Godlike.logger
import com.godlike.common.networking.ModNetworking
import com.godlike.common.networking.TelekinesisControlsPacket
import com.godlike.common.networking.TracerParticlePacket
import com.godlike.common.util.*
import com.godlike.common.vs2.Vs2Util
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.projectile.Fireball
import net.minecraft.world.entity.projectile.SmallFireball
import net.minecraft.world.item.FireChargeItem
import org.valkyrienskies.core.api.ships.saveAttachment
import org.valkyrienskies.core.impl.hooks.VSEvents
import org.valkyrienskies.mod.common.util.GameTickForceApplier

const val TK_FORCE = 2000.0

fun createShipFromSelection(player: ServerPlayer) {
    val cursors = ModComponents.CURSORS.get(player).getPositions()
    if (cursors.isEmpty()) {
        return
    }
    val ship = Vs2Util.createShip(cursors, player.serverLevel())
    ship.saveAttachment(GameTickForceApplier())
    ModComponents.TELEKINESIS_DATA.get(player).tkTargets.clear()
    ModComponents.TELEKINESIS_DATA.get(player).tkTargets.add(ship)
}

fun handleTelekinesisControls(telekinesisControls: TelekinesisControlsPacket, player: ServerPlayer) {
    val tkData = ModComponents.TELEKINESIS_DATA.get(player)
    tkData.tkTargets.forEach { ship ->
        val forceApplier = ship.getAttachment(GameTickForceApplier::class.java)!!

        // Find where the player is looking at on the sphere defined by the ship's distance from them
        val eyePosition = player.position().add(0.0, 1.5, 0.0)
        val shipPos = ship.transform.positionInWorld.toVec3()
        val distanceFromPlayer = eyePosition.distanceTo(shipPos)
//        logger.info("Distance from player: $distanceFromPlayer")
        val pointer = findPointOnSphereAtRadius(eyePosition, distanceFromPlayer, telekinesisControls.playerLookDirection)



        // Apply a force to the ship to move it towards the pointer on the circumference of the sphere
        val playerToShipVec = shipPos.subtract(player.position()).normalize()
        val planeIntersect = vecIntersectsPerpendicularPlaneFromPoint(pointer, playerToShipVec, shipPos)
        val forceDirection = shipPos.subtract(planeIntersect).normalize().negate()

        ModNetworking.CHANNEL.serverHandle(player).send(
            TracerParticlePacket(shipPos.add(forceDirection.normalize()))
        )

        // Apply the force
        forceApplier.applyInvariantTorque(forceDirection.scale(TK_FORCE).toVector3d())
    }
}