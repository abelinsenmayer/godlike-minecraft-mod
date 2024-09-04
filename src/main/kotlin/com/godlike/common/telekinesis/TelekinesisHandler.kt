package com.godlike.common.telekinesis

import com.godlike.common.Godlike.logger
import com.godlike.common.components.ModComponents
import com.godlike.common.components.telekinesis
import com.godlike.common.networking.ModNetworking
import com.godlike.common.networking.TelekinesisControlsPacket
import com.godlike.common.networking.TracerParticlePacket
import com.godlike.common.util.*
import com.godlike.common.vs2.Vs2Util
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.phys.Vec3
import org.valkyrienskies.mod.common.util.GameTickForceApplier
import kotlin.math.log
import kotlin.math.max

const val TK_SCALAR = 40.0
const val BRAKE_SCALAR = 5.0

fun createShipFromSelection(player: ServerPlayer) {
    val cursors = ModComponents.CURSORS.get(player).getPositions()
    if (cursors.isEmpty()) {
        return
    }
    val ship = Vs2Util.createShip(cursors, player.serverLevel())
    ModComponents.TELEKINESIS_DATA.get(player).tkShipIds.clear()
    ModComponents.TELEKINESIS_DATA.get(player).tkShipIds.add(ship.id)

    // Set the pointer distance to the distance from the ship to the player's eyes
    ModComponents.TELEKINESIS_DATA.get(player).pointerDistance = ship.transform.positionInWorld.toVec3()
        .distanceTo(player.position().add(0.0, 1.5, 0.0))
}

fun handleTelekinesisControls(telekinesisControls: TelekinesisControlsPacket, player: ServerPlayer) {
    player.telekinesis().pointerDistance += telekinesisControls.pointerDistanceDelta

    // Move ships towards the pointer
    player.telekinesis().tkShipIds.forEach { shipId ->
        val ship = Vs2Util.getServerShipWorld(player.serverLevel()).loadedShips.getById(shipId) ?: return
        val forceApplier = ship.getAttachment(GameTickForceApplier::class.java)!!

        // Find where the player is looking at on the sphere defined by the ship's distance from them
        val eyePosition = player.position().add(0.0, 1.5, 0.0)
        val shipPos = ship.transform.positionInWorld.toVec3()
        val pointerDistance = ModComponents.TELEKINESIS_DATA.get(player).pointerDistance
        val pointer = findPointOnSphereAtRadius(eyePosition, pointerDistance, telekinesisControls.playerLookDirection)
            .subtract(shipPos.subtract(eyePosition).normalize().scale(0.03))

        // Apply a force to the ship to move it towards the pointer
        val force = shipPos.subtract(pointer).normalize().negate().scale(ship.inertiaData.mass * TK_SCALAR)

        ModNetworking.CHANNEL.serverHandle(player).send(
            TracerParticlePacket(pointer)
        )

        // Add enough force to counteract gravity on the ship
        val gravityNewtons = ship.inertiaData.mass * 30
        val liftForce = Vec3(0.0, gravityNewtons, 0.0)

        // "Brake" to slow down the ship based on how aligned its velocity vector is to the direction of the pointer
        val angle = Math.toDegrees(ship.velocity.angle(force.toVector3d()))
        val brakeAngleScalar = angle / 180 * BRAKE_SCALAR
        val brakeVelocityScalar = max(log(ship.velocity.length() + 1, 10.0), 0.0)
        val brakeForce = ship.velocity.toVec3().negate().normalize().scale(ship.inertiaData.mass * TK_SCALAR * brakeAngleScalar * brakeVelocityScalar)

        // Reduce the force when we're very near the pointer to stop the ship from oscillating
        val distance = shipPos.distanceTo(pointer)
        val distanceScalar = max(log(distance + 0.8, 10.0), 0.0)

        // Apply the force
        forceApplier.applyInvariantTorque(force.scale(distanceScalar).add(brakeForce).add(liftForce).toVector3d())
    }
}