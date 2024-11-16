package com.godlike.common.telekinesis

import com.godlike.common.components.ModEntityComponents
import com.godlike.common.components.getTkTicker
import com.godlike.common.components.telekinesis
import com.godlike.common.networking.ModNetworking
import com.godlike.common.networking.TelekinesisControlsPacket
import com.godlike.common.networking.TracerParticlePacket
import com.godlike.common.util.*
import com.godlike.common.vs2.Vs2Util
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3
import org.valkyrienskies.core.api.ships.ServerShip

const val LAUNCH_POINTER_DISTANCE = 100.0
const val LAUNCH_DAMAGE = 4.0F

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
    player.telekinesis().addEntityAsTkTarget(entity)
    player.telekinesis().pointerDistance = entity.position().add(0.0, entity.boundingBox.ysize / 2, 0.0)
        .distanceTo(player.position().add(0.0, 1.5, 0.0))
}

/**
 * Add ship to the player's telekinesis targets. If the ship already a target of theirs and is hovering, stop hovering.
 */
fun pickShipToTk(ship: ServerShip, player: ServerPlayer) {
    player.telekinesis().addShipIdAsTarget(ship.id)
    player.telekinesis().pointerDistance = ship.transform.positionInWorld.toVec3()
        .distanceTo(player.position().add(0.0, 1.5, 0.0))
}

/**
 * Create a ship from the given set of positions and add it to the player's telekinesis targets.
 */
fun tkPositions(positions: Set<BlockPos>, player: ServerPlayer) {
    val ship = Vs2Util.createShip(positions, player.serverLevel())
    player.telekinesis().addShipIdAsTarget(ship.id)
    player.telekinesis().pointerDistance = ship.transform.positionInWorld.toVec3()
        .distanceTo(player.position().add(0.0, 1.5, 0.0))
}

/**
 * Drop the player's currently-active telekinesis target.
 */
fun dropTk(player: ServerPlayer) {
    player.telekinesis().activeTkTarget?.let { player.telekinesis().removeTarget(it) }
}

/**
 * Place the player's currently-active telekinesis target.
 */
fun placeTk(player: ServerPlayer) {
    val toPlace = player.telekinesis().getTkTargets().filter { it.hoverPos == null }
    toPlace.forEach { target ->
        target.place(player.serverLevel())
        player.telekinesis().removeTarget(target)
    }
}

/**
 * Hover the player's currently-active telekinesis target.
 */
fun hoverTk(player: ServerPlayer, lookDirection: Vec3) {
    player.telekinesis().activeTkTarget?.hoverPos = getPointer(player, lookDirection, player.telekinesis().activeTkTarget!!)
    player.telekinesis().activeTkTarget = null
}

fun setChargingLaunch(player: ServerPlayer, isCharging: Boolean) {
    if (isCharging) {
        player.telekinesis().getTkTargets().filter { it.hoverPos == null }.forEach { target ->
            target.chargingLaunch = true
        }
        hoverTk(player, player.lookAngle)
    }
}

fun launchTk(player: ServerPlayer, targetedPosition: Vec3) {
    val toLaunch = player.telekinesis().getTkTargets().filter { it.chargingLaunch }
    toLaunch.forEach { target ->
        target.chargingLaunch = false
        target.hoverPos = null
        target.launchToward(targetedPosition)
        player.telekinesis().removeTarget(target)
        target.isLaunching = true
    }
}

/**
 * Attempt to dislodge the player's currently-active telekinesis target from where it's stuck.
 * Only applies to ship targets.
 */
fun unstickTk(player: ServerPlayer) {
    player.telekinesis().activeTkTarget?.let { target ->
        target !is ShipTkTarget && return
        (target as ShipTkTarget).unstick(getPointer(player, player.lookAngle, target))
    }
}

fun getPointer(player: ServerPlayer, lookDirection: Vec3, target: TkTarget) : Vec3 {
    // Find where the player is looking at on the sphere defined by the target's distance from them
    val eyePosition = player.position().add(0.0, 1.5, 0.0)
    val pointerDistance = ModEntityComponents.TELEKINESIS_DATA.get(player).pointerDistance
    val pointer = findPointOnSphereAtRadius(eyePosition, pointerDistance, lookDirection)
        .subtract(target.pos().subtract(eyePosition).normalize().scale(0.03))
    return pointer
}

fun getPointerAtDistance(player: Player, lookDirection: Vec3, distance: Double) : Vec3 {
    val eyePosition = player.position().add(0.0, 1.5, 0.0)
    return findPointOnSphereAtRadius(eyePosition, distance, lookDirection)
}

/**
 * Called every tick on the server side to update telekinesis targets. Handles controls from the client and every-tick
 * updates to the targets (e.g. hovering).
 */
fun serverTelekinesisTick(telekinesisControls: TelekinesisControlsPacket, player: ServerPlayer) {
    player.clearNonexistentTargets()

    player.telekinesis().pointerDistance += telekinesisControls.pointerDistanceDelta

    player.telekinesis().getTkTargets().forEach { target ->
        // If not registered with ticker, add it
        player.serverLevel().getTkTicker().tickingTargets.add(target)

        val pointer = getPointer(player, telekinesisControls.playerLookDirection, target)
        if (player.telekinesis().activeTkTarget != null) {
            ModNetworking.CHANNEL.serverHandle(player).send(TracerParticlePacket(pointer))
        }

        target.addLiftForce()
        if (telekinesisControls.rotating()) {
            target.rotateTowardPointer(pointer, player.position().add(0.0, 1.5, 0.0))
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

fun ServerPlayer.clearNonexistentTargets() {
    this.telekinesis().getTkTargets().forEach { target ->
        if (!target.exists()) {
            this.telekinesis().removeTarget(target)
        }
    }
}