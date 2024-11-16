package com.godlike.common.telekinesis

import com.godlike.common.Godlike.logger
import com.godlike.common.util.*
import com.godlike.common.vs2.Vs2Util
import net.minecraft.client.player.LocalPlayer
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import org.joml.Vector3d
import org.valkyrienskies.core.api.ships.ServerShip
import org.valkyrienskies.mod.common.util.GameTickForceApplier
import kotlin.math.log
import kotlin.math.max
import net.minecraft.core.Vec3i
import org.joml.primitives.AABBd

const val SHIP_FORCE_SCALAR = 40.0
const val SHIP_BRAKE_SCALAR = 5.0
const val SHIP_LAUNCH_SCALAR = 60.0
const val SHIP_LAUNCH_VELOCITY_THRESHOLD = 10.0

class ShipTkTarget(
    override val level: Level,
    override var player: Player?,
    val shipId : Long,
    override var hoverPos: Vec3? = null,
    override var chargingLaunch: Boolean = false,
) : TkTarget {
    val ship : ServerShip
        get() {
            if (level !is ServerLevel) {
                throw IllegalStateException("Cannot get a ship on the client side")
            }
            return Vs2Util.getServerShipWorld(level).loadedShips.getById(shipId)!!
        }
    var disassemblyTickCountdown : Int = -1
    private var launchStillnessTicks = 0
    override var isLaunching: Boolean = false
        set(value) {
            field = value
            launchStillnessTicks = 0
        }

    override fun toNbt() : CompoundTag {
        val tag = CompoundTag()
        tag.putLong("shipId", shipId)
        hoverPos?.let {
            tag.putDouble("hoverPos.x", it.x)
            tag.putDouble("hoverPos.y", it.y)
            tag.putDouble("hoverPos.z", it.z)
        }
        tag.putBoolean("chargingLaunch", chargingLaunch)
        tag.putBoolean("isLaunching", isLaunching)
        tag.putInt("disassemblyTickCountdown", disassemblyTickCountdown)
        return tag
    }

    override fun mass(): Double {
        return ship.inertiaData.mass
    }

    /**
     * Called every tick on the server side to update telekinesis targets.
     * Note that player TK controls are handled separately; this is for things that should happen every tick regardless
     * of controlling player.
     */
    override fun tick() {
        if (isLaunching) {
            launchingTick()
        }
        // Disassemble the ship once the TTL has expired, if there is one
        if (disassemblyTickCountdown > 0) {
            disassemblyTickCountdown--
            if (disassemblyTickCountdown == 0) {
                disassemble(ship, level as ServerLevel)
            }
        }
    }

    override fun exists(): Boolean {
        return try {
            ship
            true
        } catch (e: NullPointerException) {
            false
        }
    }

    private fun forceApplier() : GameTickForceApplier {
        return this.ship.getAttachment(GameTickForceApplier::class.java)!!
    }

    private fun torqueApplier() : TorqueForceApplier {
        return this.ship.getTorqueForceApplier()
    }

    override fun place(level : ServerLevel) {
        disassemble(ship, level)
    }

    override fun launchToward(pos: Vec3) {
        val shipPos = ship.transform.positionInWorld.toVec3()
        val shipToPos = pos.subtract(shipPos)
        val launchForce = shipToPos.normalize().scale(ship.inertiaData.mass * SHIP_FORCE_SCALAR * SHIP_LAUNCH_SCALAR)
        forceApplier().applyInvariantForce(launchForce.toVector3d())
    }

    override fun pos(): Vec3 {
        return ship.transform.positionInWorld.toVec3()
    }

    override fun moveToward(pos: Vec3) {
        val shipPos = ship.transform.positionInWorld.toVec3()

        // Apply a force to the ship to move it towards the pointer
        val force = shipPos.subtract(pos).normalize().negate().scale(ship.inertiaData.mass * SHIP_FORCE_SCALAR)

        // "Brake" to slow down the ship based on how aligned its velocity vector is to the direction of the pointer
        val angle = Math.toDegrees(ship.velocity.angle(force.toVector3d()))
        val brakeAngleScalar = angle / 180 * SHIP_BRAKE_SCALAR
        val brakeVelocityScalar = max(log(ship.velocity.length() + 1, 10.0), 0.0)
        val brakeForce = ship.velocity.toVec3().negate().normalize().scale(ship.inertiaData.mass * SHIP_FORCE_SCALAR * brakeAngleScalar * brakeVelocityScalar)

        // Reduce the force when we're very near the pointer to stop the ship from oscillating
        val distance = shipPos.distanceTo(pos)
        val distanceScalar = max(log(distance + 0.8, 10.0), 0.0)

        // Apply the force
        forceApplier().applyInvariantForce(force.scale(distanceScalar).add(brakeForce).toVector3d())
    }

    override fun addLiftForce() {
        // Add enough force to counteract gravity on the ship
        val gravityNewtons = ship.inertiaData.mass * 30
        val liftForce = Vector3d(0.0, gravityNewtons, 0.0)
        forceApplier().applyInvariantForce(liftForce)
    }

    override fun rotateTowardPointer(pointer: Vec3, playerEyePos: Vec3) {
        val shipPos = ship.transform.positionInWorld.toVec3()
        val shipToPointer = shipPos.subtract(pointer)
        val playerToShip = playerEyePos.subtract(shipPos)
        val torqueAxis = shipToPointer.cross(playerToShip).normalize()
        val torque = ship.inertiaData.momentOfInertiaTensor.transform(torqueAxis.toVector3d()).toVec3().scale(6.0)

        torqueApplier().applyInvariantTorque(torque.toVector3d())
    }

    override fun addRotationDrag() {
        val dragAxis = ship.inertiaData.momentOfInertiaTensor.transform(ship.omega.toVec3().toVector3d()).toVec3()
        val dragTorque = dragAxis.scale(-ship.omega.length()).scale(6.0)
        torqueApplier().applyInvariantTorque(dragTorque.toVector3d())
    }

    private fun launchingTick() {
        // Stop "launching" if the ship's velocity has dropped sufficiently for the enough consecutive ticks
        if (this.ship.velocity.length() < SHIP_LAUNCH_VELOCITY_THRESHOLD) {
            launchStillnessTicks++
            if (launchStillnessTicks > 5) {
                logger.info("stopping launch, v=${this.ship.velocity.length()}")
                this.isLaunching = false
            }
        } else {
            launchStillnessTicks = 0
        }

        // Damage entities in the ship's path
        val hitBox = this.ship.worldAABB.toAABB().inflate(1.0)
        this.level.getEntities(null, hitBox).forEach { entity ->
            entity.hurt(entity.damageSources().flyIntoWall(), LAUNCH_DAMAGE)
            entity.playSound(
                if (LAUNCH_DAMAGE > 4) SoundEvents.GENERIC_BIG_FALL else SoundEvents.GENERIC_SMALL_FALL,
                1.0f,
                1.0f
            )
            logger.info("Hit for $LAUNCH_DAMAGE damage at velocity ${this.ship.velocity.length()}.")
        }

        /* TODO explosions are commented out until we can do more work to make them work properly
        // Create explosions for block collisions
        val level = this.level
        val collidedPositions = BlockPos.betweenClosedStream(hitBox)
        collidedPositions.forEach { pos ->
            // Don't collide with air
            val state = level.getBlockState(pos)
            if (state.isAir) {
                return@forEach
            }
            // Don't collide with itself
            val shipForPos = Vs2Util.getServerShipManagingPos((player as ServerPlayer).serverLevel(), pos)
            if (shipForPos == this.ship) {
                logger.info("Ignoring self-collision at $pos")
                return@forEach
            }

            this.level.explode(
                this.player,
                this.player.damageSources().flyIntoWall(),
                ExplosionDamageCalculator(),
                pos.toVec3(),
                0.5f,
                false,
                Level.ExplosionInteraction.BLOCK
            )
        }*/
    }

    /**
     * Attempt to dislodge the ship from a place it's trapped. Searches for an axis direction where the ship can move
     * unobstructed, and adds a force in that direction. If multiple directions are unobstructed, the ship will move in
     * the direction closest to the controlling player's pointer.
     * TODO not yet implemented
     * @return true if the ship could successfully move in any direction to try and dislodge itself.
     */
    fun unstick(pointer: Vec3) : Boolean {
        logger.warn("Unstick not yet implemented")
        return false
    }

    override fun equals(other: Any?): Boolean {
        return other is ShipTkTarget && other.shipId == this.shipId
    }
}