package com.godlike.common.telekinesis

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
import org.joml.Vector3d
import org.valkyrienskies.core.api.ships.ServerShip
import org.valkyrienskies.mod.common.util.GameTickForceApplier
import java.lang.Math.toRadians
import kotlin.math.log
import kotlin.math.max

const val TK_SCALAR = 40.0
const val BRAKE_SCALAR = 5.0
const val ROTATION_POINT_OFFSET_DISTANCE = 3.0

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

        ship.addLiftForce()
        if (telekinesisControls.rotating()) {
            ship.rotateTowardPointer(pointer, eyePosition)
        } else {
            ship.moveToward(pointer)
            ship.addRotationDrag()
        }
    }
}

fun ServerShip.addLiftForce() {
    val forceApplier = this.getAttachment(GameTickForceApplier::class.java)!!

    // Add enough force to counteract gravity on the ship
    val gravityNewtons = this.inertiaData.mass * 30
    val liftForce = Vector3d(0.0, gravityNewtons, 0.0)

    forceApplier.applyInvariantForce(liftForce)
}

fun ServerShip.moveToward(pos: Vec3) {
    val forceApplier = this.getAttachment(GameTickForceApplier::class.java)!!
    val shipPos = this.transform.positionInWorld.toVec3()

    // Apply a force to the ship to move it towards the pointer
    val force = shipPos.subtract(pos).normalize().negate().scale(this.inertiaData.mass * TK_SCALAR)

    // "Brake" to slow down the ship based on how aligned its velocity vector is to the direction of the pointer
    val angle = Math.toDegrees(this.velocity.angle(force.toVector3d()))
    val brakeAngleScalar = angle / 180 * BRAKE_SCALAR
    val brakeVelocityScalar = max(log(this.velocity.length() + 1, 10.0), 0.0)
    val brakeForce = this.velocity.toVec3().negate().normalize().scale(this.inertiaData.mass * TK_SCALAR * brakeAngleScalar * brakeVelocityScalar)

    // Reduce the force when we're very near the pointer to stop the ship from oscillating
    val distance = shipPos.distanceTo(pos)
    val distanceScalar = max(log(distance + 0.8, 10.0), 0.0)

    // Apply the force
    forceApplier.applyInvariantTorque(force.scale(distanceScalar).add(brakeForce).toVector3d())
}

fun ServerShip.rotateTowardPointer(pointer: Vec3, playerEyePos: Vec3) {
    val torqueForceApplier = this.getTorqueForceApplier()

    val shipPos = this.transform.positionInWorld.toVec3()
    val shipToPointer = shipPos.subtract(pointer)
    val playerToShip = playerEyePos.subtract(shipPos)
    val torque = shipToPointer.cross(playerToShip).normalize().scale(this.inertiaData.mass/10 * TK_SCALAR)

    torqueForceApplier.applyInvariantTorque(torque.toVector3d())
}

fun ServerShip.addRotationDrag() {
    val torqueForceApplier = this.getTorqueForceApplier()
//    val brakeVelocityScalar = max(log(this.omega.length() + 1, 10.0), 0.0)
    val dragForce = this.omega.toVec3().scale(-this.omega.length()).scale(TK_SCALAR * BRAKE_SCALAR)
    torqueForceApplier.applyInvariantTorque(dragForce.toVector3d())
}