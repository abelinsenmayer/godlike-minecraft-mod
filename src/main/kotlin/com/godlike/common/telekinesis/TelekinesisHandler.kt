package com.godlike.common.telekinesis

import com.godlike.common.components.*
import com.godlike.common.networking.TelekinesisControlsPacket
import com.godlike.common.util.*
import com.godlike.common.vs2.Vs2Util
import net.minecraft.core.BlockPos
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.effect.MobEffectInstance
import net.minecraft.world.effect.MobEffects
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.Level
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
import org.valkyrienskies.core.api.ships.ServerShip

const val LAUNCH_POINTER_DISTANCE = 100.0
const val LAUNCH_BASE_DAMAGE = 2.0F

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
fun placeActiveTarget(player: ServerPlayer) {
    player.telekinesis().activeTkTarget?.let {
        it.place(player.serverLevel())
        player.telekinesis().removeTarget(it)
    }
}

fun placePlacementTargetAt(player: ServerPlayer, lookDirection: Vec3) {
    player.telekinesis().placementTarget?.let {
        val pointerPos = getAbsolutePointer(player, lookDirection).add(Vec3(1.0, 1.0, 1.0)).toVec3i()
        if (it.placeAt(
                player.serverLevel(),
                pointerPos,
                player.telekinesis().placementDirectionTop,
                player.telekinesis().placementDirectionFront
            )
        ) {
            player.telekinesis().removeTarget(it)
            player.telekinesis().placementTarget = null
            player.setMode(Mode.TELEKINESIS)
        }
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

fun getPointer(player: ServerPlayer, lookDirection: Vec3, target: TkTarget) : Vec3 {
    // Find where the player is looking at on the sphere defined by the target's distance from them
    val eyePosition = player.position().add(0.0, 1.5, 0.0)
    val pointerDistance = player.telekinesis().pointerDistance
    val pointer = findPointOnSphereAtRadius(eyePosition, pointerDistance, lookDirection)
        .subtract(target.pos().subtract(eyePosition).normalize().scale(0.03))
    return pointer
}

fun getAbsolutePointer(player: ServerPlayer, lookDirection: Vec3): Vec3 {
    val eyePosition = player.position().add(0.0, 1.5, 0.0)
    val pointerDistance = player.telekinesis().pointerDistance
    return eyePosition.add(lookDirection.x * pointerDistance, lookDirection.y * pointerDistance, lookDirection.z * pointerDistance)
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

    val pointerDistance = player.telekinesis().pointerDistance
    if (telekinesisControls.pointerDistanceDelta < 0) {
        player.telekinesis().pointerDistance =
            (pointerDistance + telekinesisControls.pointerDistanceDelta).coerceAtLeast(1.0)
    } else {
        player.telekinesis().pointerDistance =
            (pointerDistance + telekinesisControls.pointerDistanceDelta).coerceAtMost(player.telekinesis().tier.range)
    }

    player.telekinesis().getTkTargets().forEach { target ->
        // If not registered with ticker, add it
        player.serverLevel().getTkTicker().tickingTargets.add(target)

        var pointer = getPointer(player, telekinesisControls.playerLookDirection, target)

        target.addLiftForce()
        val isRotating = telekinesisControls.rotatingLeft || telekinesisControls.rotatingRight || 
                         telekinesisControls.rotatingUp || telekinesisControls.rotatingDown
        if (isRotating && target.hoverPos == null) {
            target.rotate(telekinesisControls.rotatingUp, telekinesisControls.rotatingDown, telekinesisControls.rotatingLeft, telekinesisControls.rotatingRight, player.position().add(0.0, 1.5, 0.0))
        }

        target.addRotationDrag()
        if (target.hoverPos != null) {
            target.moveToward(target.hoverPos!!)
        } else {
            // If we're moving a ship that's "stuck", try to unstick it by gradually snapping the pointer to an axis
            if (target is ShipTkTarget) {
                target.updateStuckTicks(pointer)
                val snapDistance = target.stuckTicks / 2
                val snapTo = target.getAxisPointers(target.pos().distanceTo(pointer))
                    .filter { it.distanceTo(pointer) < snapDistance }
                    .minByOrNull { it.distanceTo(pointer) }
                if (snapTo != null) {
                    pointer = snapTo
                }
            }
            target.moveToward(pointer)
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

fun ServerPlayer.handleTkMovementInputs() {
    val powers = this.telekinesis().tier.grantedPowers
    if (this.isCrouching && powers.contains(com.godlike.common.items.TkPower.SLOW_FALL)) {
        this.addEffect(MobEffectInstance(MobEffects.SLOW_FALLING, 5, 0, false, false, false))
    }

    if (this.telekinesis().isLevitating) {
        this.addEffect(MobEffectInstance(MobEffects.LEVITATION, 5, 2, false, false, false))
    }
}

fun ServerPlayer.addVelocity(v: Vec3) {
    this.push(v.x, v.y, v.z)
    this.hurtMarked = true
}