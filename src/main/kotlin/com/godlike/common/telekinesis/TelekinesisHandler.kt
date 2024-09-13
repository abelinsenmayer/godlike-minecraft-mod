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
import org.valkyrienskies.core.api.ships.ServerShip

const val TK_SCALAR = 40.0
const val BRAKE_SCALAR = 5.0

fun createShipFromSelection(player: ServerPlayer) {
    val cursors = ModComponents.CURSORS.get(player).getPositions()
    if (cursors.isEmpty()) {
        return
    }
    val ship = Vs2Util.createShip(cursors, player.serverLevel())
    ModComponents.TELEKINESIS_DATA.get(player).clearTargets()
    ModComponents.TELEKINESIS_DATA.get(player).addShipIdAsTarget(ship.id)

    // Set the pointer distance to the distance from the ship to the player's eyes
    ModComponents.TELEKINESIS_DATA.get(player).pointerDistance = ship.transform.positionInWorld.toVec3()
        .distanceTo(player.position().add(0.0, 1.5, 0.0))
}

/**
 * Turn the block at the given position into a ship and add it to the player's telekinesis targets.
 */
fun pickBlockToTk(pos: BlockPos, player: ServerPlayer) {
    val ship = Vs2Util.createShip(listOf(pos), player.serverLevel())
    player.telekinesis().addShipIdAsTarget(ship.id)
    player.telekinesis().pointerDistance = ship.transform.positionInWorld.toVec3()
        .distanceTo(player.position().add(0.0, 1.5, 0.0))
}

/**
 * Add entity to the player's telekinesis targets. If the entity already a target of theirs and is hovering, stop hovering.
 */
fun pickEntityToTk(entity: Entity, player: ServerPlayer) {
    TODO("Not yet implemented")
}

/**
 * Add ship to the player's telekinesis targets. If the ship already a target of theirs and is hovering, stop hovering.
 */
fun pickShipToTk(ship: ServerShip, player: ServerPlayer) {
    player.telekinesis().addShipIdAsTarget(ship.id)
    player.telekinesis().pointerDistance = ship.transform.positionInWorld.toVec3()
        .distanceTo(player.position().add(0.0, 1.5, 0.0))
    player.telekinesis().sync()
}

/**
 * Drop all telekinesis targets that aren't hovering.
 */
fun dropTk(player: ServerPlayer) {
    player.telekinesis().removeTargetsWhere { it.hoverPos == null }
}

/**
 * Place all telekinesis targets that aren't hovering.
 */
fun placeTk(player: ServerPlayer) {
    val toPlace = player.telekinesis().getShipTargets().filter { it.hoverPos == null }
    toPlace.forEach { target ->
        target.place(player.serverLevel())
        player.telekinesis().removeTarget(target)
    }
}

/**
 * Hover all telekinesis targets that aren't hovering.
 */
fun hoverTk(player: ServerPlayer, lookDirection: Vec3) {
    player.telekinesis().getShipTargets().forEach { target ->
        if (target.hoverPos == null) {
            target.hoverPos = getPointer(player, lookDirection, target)
        }
    }
    player.telekinesis().sync()
}

fun getPointer(player: ServerPlayer, lookDirection: Vec3, target: TkTarget) : Vec3 {
    // Find where the player is looking at on the sphere defined by the target's distance from them
    val eyePosition = player.position().add(0.0, 1.5, 0.0)
    val pointerDistance = ModComponents.TELEKINESIS_DATA.get(player).pointerDistance
    val pointer = findPointOnSphereAtRadius(eyePosition, pointerDistance, lookDirection)
        .subtract(target.pos().subtract(eyePosition).normalize().scale(0.03))
    return pointer
}

fun tickTelekinesisControls(telekinesisControls: TelekinesisControlsPacket, player: ServerPlayer) {
    // Update the pointer distance
    player.telekinesis().pointerDistance += telekinesisControls.pointerDistanceDelta

    player.telekinesis().getShipTargets().forEach { target ->
        if (target is ShipTkTarget) {
            // If the target's ship doesn't exist (e.g. because it was broken or unloaded), remove it from the list
            try {
                target.ship
            } catch (e: NullPointerException) {
                player.telekinesis().removeShipIdAsTarget(target.shipId)
                return@forEach
            }
        }

        // Move the target
        val pointer = getPointer(player, telekinesisControls.playerLookDirection, target)
        if (player.telekinesis().hasNonHoveringTarget()) {
            ModNetworking.CHANNEL.serverHandle(player).send(TracerParticlePacket(pointer))
        }
        val eyePosition = player.position().add(0.0, 1.5, 0.0)

        target.addLiftForce()
        if (telekinesisControls.rotating()) {
            target.rotateTowardPointer(pointer, eyePosition)
        } else {
            target.addRotationDrag()
            if (target.hoverPos != null) {
                target.moveToward(target.hoverPos!!)
            } else {
                target.moveToward(pointer)
            }
        }
    }
}