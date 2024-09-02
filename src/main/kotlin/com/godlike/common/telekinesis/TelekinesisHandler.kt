package com.godlike.common.telekinesis

import com.godlike.common.Godlike.logger
import com.godlike.common.components.ModComponents
import com.godlike.common.networking.ModNetworking
import com.godlike.common.networking.TelekinesisControlsPacket
import com.godlike.common.networking.TracerParticlePacket
import com.godlike.common.util.*
import com.godlike.common.vs2.Vs2Util
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.phys.Vec3
import org.joml.Vector3d
import org.joml.Vector3dc
import org.valkyrienskies.core.api.ships.saveAttachment
import org.valkyrienskies.mod.common.util.GameTickForceApplier

const val TK_FORCE = 10000.0

fun createShipFromSelection(player: ServerPlayer) {
    val cursors = ModComponents.CURSORS.get(player).getPositions()
    if (cursors.isEmpty()) {
        return
    }
    val ship = Vs2Util.createShip(cursors, player.serverLevel())
////    ship.saveAttachment(GameTickForceApplier())
    ModComponents.TELEKINESIS_DATA.get(player).tkShipIds.clear()
    ModComponents.TELEKINESIS_DATA.get(player).tkShipIds.add(ship.id)
}

fun handleTelekinesisControls(telekinesisControls: TelekinesisControlsPacket, player: ServerPlayer) {
//    Vs2Util.getServerShipWorld(player.serverLevel()).loadedShips.forEach { ship ->
//        val forceApplier = ship.getAttachment(GameTickForceApplier::class.java)!!
//        forceApplier.applyInvariantForce(Vector3d(TK_FORCE, 0.0, 0.0))
//    }

    val tkData = ModComponents.TELEKINESIS_DATA.get(player)
    tkData.tkShipIds.forEach { shipId ->
        val ship = Vs2Util.getServerShipWorld(player.serverLevel()).loadedShips.getById(shipId) ?: return
        val forceApplier = ship.getAttachment(GameTickForceApplier::class.java)!!

        // Find where the player is looking at on the sphere defined by the ship's distance from them
        val eyePosition = player.position().add(0.0, 1.5, 0.0)
        val shipPos = ship.transform.positionInWorld.toVec3()
        val distanceFromPlayer = eyePosition.distanceTo(shipPos)
        val pointer = findPointOnSphereAtRadius(eyePosition, distanceFromPlayer, telekinesisControls.playerLookDirection)
            .subtract(shipPos.subtract(eyePosition).normalize().scale(0.03))

        // Apply a force to the ship to move it towards the pointer
        val playerToShipVec = shipPos.subtract(player.position()).normalize()
        val forceDirection = shipPos.subtract(pointer).normalize().negate()

        ModNetworking.CHANNEL.serverHandle(player).send(
            TracerParticlePacket(shipPos.add(forceDirection.normalize()))
        )

        // Add enough force to counteract gravity on the ship
        val antiGravForce = Vec3(0.0, ship.inertiaData.mass * 9.81, 0.0).scale(TK_FORCE)

        // Apply the force
        forceApplier.applyInvariantTorque(forceDirection.scale(TK_FORCE).add(antiGravForce).toVector3d())
    }
}