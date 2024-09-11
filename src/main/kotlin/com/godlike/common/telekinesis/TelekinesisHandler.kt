package com.godlike.common.telekinesis

import com.godlike.common.Godlike.logger
import com.godlike.common.components.ModComponents
import com.godlike.common.components.telekinesis
import com.godlike.common.networking.ModNetworking
import com.godlike.common.networking.TelekinesisControlsPacket
import com.godlike.common.networking.TracerParticlePacket
import com.godlike.common.util.*
import com.godlike.common.vs2.Vs2Util
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.Vec3
import org.joml.Quaterniond
import org.joml.Vector3d
import org.valkyrienskies.core.api.ships.ServerShip
import org.valkyrienskies.mod.common.util.GameTickForceApplier
import java.lang.Math.toRadians
import kotlin.math.log
import kotlin.math.max

const val TK_SCALAR = 40.0
const val BRAKE_SCALAR = 5.0
const val ROTATION_POINT_OFFSET_DISTANCE = 2.0

fun createShipFromSelection(player: ServerPlayer) {
    val cursors = ModComponents.CURSORS.get(player).getPositions()
    if (cursors.isEmpty()) {
        return
    }
    val ship = Vs2Util.createShip(cursors, player.serverLevel())
    ModComponents.TELEKINESIS_DATA.get(player).clearShipIds()
    ModComponents.TELEKINESIS_DATA.get(player).addShipId(ship.id)

    // Set the pointer distance to the distance from the ship to the player's eyes
    ModComponents.TELEKINESIS_DATA.get(player).pointerDistance = ship.transform.positionInWorld.toVec3()
        .distanceTo(player.position().add(0.0, 1.5, 0.0))
}

fun pickBlockToTk(pos: BlockPos, player: ServerPlayer) {
    val ship = Vs2Util.createShip(listOf(pos), player.serverLevel())
    player.telekinesis().addShipId(ship.id)
    player.telekinesis().pointerDistance = ship.transform.positionInWorld.toVec3()
        .distanceTo(player.position().add(0.0, 1.5, 0.0))
}

fun pickEntityToTk(entity: Entity, player: ServerPlayer) {
    TODO("Not yet implemented")
}

fun pickShipToTk(ship: ServerShip, player: ServerPlayer) {
    player.telekinesis().addShipId(ship.id)
    player.telekinesis().pointerDistance = ship.transform.positionInWorld.toVec3()
        .distanceTo(player.position().add(0.0, 1.5, 0.0))
}

fun dropTk(player: ServerPlayer) {
    player.telekinesis().clearShipIds()
}

fun placeTk(player: ServerPlayer) {
    player.telekinesis().getShipIds().forEach { shipId ->
        val ship = Vs2Util.getServerShipWorld(player.serverLevel()).loadedShips.getById(shipId) ?: return
        disassemble(ship, player.serverLevel())
    }
    player.telekinesis().clearShipIds()
}

fun handleTelekinesisControls(telekinesisControls: TelekinesisControlsPacket, player: ServerPlayer) {
    // Update the pointer distance
    player.telekinesis().pointerDistance += telekinesisControls.pointerDistanceDelta

    // Move ships
    player.telekinesis().getShipIds().forEach { shipId ->
        val ship = Vs2Util.getServerShipWorld(player.serverLevel()).loadedShips.getById(shipId) ?: return

        // Find where the player is looking at on the sphere defined by the ship's distance from them
        val eyePosition = player.position().add(0.0, 1.5, 0.0)
        val shipPos = ship.transform.positionInWorld.toVec3()
        val pointerDistance = ModComponents.TELEKINESIS_DATA.get(player).pointerDistance
        val pointer = findPointOnSphereAtRadius(eyePosition, pointerDistance, telekinesisControls.playerLookDirection)
            .subtract(shipPos.subtract(eyePosition).normalize().scale(0.03))

        ModNetworking.CHANNEL.serverHandle(player).send(
            TracerParticlePacket(pointer)
        )

        if (telekinesisControls.rotating()) {
            ship.rotateToward(pointer, eyePosition, player)
        } else {
            ship.moveToward(pointer)
        }
    }
}

fun ServerShip.moveToward(pos: Vec3) {
    val forceApplier = this.getAttachment(GameTickForceApplier::class.java)!!
    val shipPos = this.transform.positionInWorld.toVec3()

    // Apply a force to the ship to move it towards the pointer
    val force = shipPos.subtract(pos).normalize().negate().scale(this.inertiaData.mass * TK_SCALAR)

    // Add enough force to counteract gravity on the ship
    val gravityNewtons = this.inertiaData.mass * 30
    val liftForce = Vec3(0.0, gravityNewtons, 0.0)

    // "Brake" to slow down the ship based on how aligned its velocity vector is to the direction of the pointer
    val angle = Math.toDegrees(this.velocity.angle(force.toVector3d()))
    val brakeAngleScalar = angle / 180 * BRAKE_SCALAR
    val brakeVelocityScalar = max(log(this.velocity.length() + 1, 10.0), 0.0)
    val brakeForce = this.velocity.toVec3().negate().normalize().scale(this.inertiaData.mass * TK_SCALAR * brakeAngleScalar * brakeVelocityScalar)

    // Reduce the force when we're very near the pointer to stop the ship from oscillating
    val distance = shipPos.distanceTo(pos)
    val distanceScalar = max(log(distance + 0.8, 10.0), 0.0)

    // Apply the force
    forceApplier.applyInvariantTorque(force.scale(distanceScalar).add(brakeForce).add(liftForce).toVector3d())
}

fun ServerShip.rotateToward(pos: Vec3, relativeTo: Vec3, player: ServerPlayer) {
    val forceApplier = this.getAttachment(GameTickForceApplier::class.java)!!

    // Add enough force to counteract gravity on the ship
    val gravityNewtons = this.inertiaData.mass * 30
    val liftForce = Vec3(0.0, gravityNewtons, 0.0)
    forceApplier.applyInvariantForce(liftForce.toVector3d())

    // Find the opposing points where we need to apply the forces in order to rotate toward the target position
    val shipPos = this.transform.positionInWorld.toVec3()
    val shipToPosVec = pos.subtract(shipPos).normalize().scale(ROTATION_POINT_OFFSET_DISTANCE)
    var point1 = shipPos.add(shipToPosVec)
    var point2 = shipPos.subtract(shipToPosVec)

    val rotateAround = shipToPosVec.toVector3d().rotateAxis(toRadians(90.0), relativeTo.x, relativeTo.y, relativeTo.z)
    val rel2Ship = relativeTo.subtract(shipPos)
    val v = rel2Ship.toVector3d().rotateAxis(toRadians(90.0), shipToPosVec.x, shipToPosVec.y, shipToPosVec.z)

    val force = rel2Ship.toVector3d().rotate(this.transform.shipToWorldRotation).normalize().toVec3().scale(TK_SCALAR).toVector3d()

    ModNetworking.CHANNEL.serverHandle(player).send(
        TracerParticlePacket(point1)
    )
    ModNetworking.CHANNEL.serverHandle(player).send(
        TracerParticlePacket(point2)
    )
    ModNetworking.CHANNEL.serverHandle(player).send(
        TracerParticlePacket(point1.add(force.toVec3().normalize()))
    )
    ModNetworking.CHANNEL.serverHandle(player).send(
        TracerParticlePacket(point2.add(force.toVec3().negate().normalize()))
    )

    forceApplier.applyInvariantForceToPos(force.toVec3().negate().toVector3d(), point1.toVector3d())
    forceApplier.applyInvariantForceToPos(force, point2.toVector3d())
}