package com.godlike.common.telekinesis

import com.godlike.common.Godlike.logger
import com.godlike.common.components.telekinesis
import com.godlike.common.util.*
import com.godlike.common.vs2.Vs2Util
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import org.joml.Vector3d
import org.valkyrienskies.core.api.ships.ServerShip
import org.valkyrienskies.mod.common.util.GameTickForceApplier
import kotlin.math.*

const val SHIP_FORCE_SCALAR = 80.0
const val SHIP_BRAKE_SCALAR = 6.0
const val SHIP_LAUNCH_SCALAR = 60.0
const val LAUNCH_VELOCITY_THRESHOLD = 10.0

class ShipTkTarget(
    override val level: Level,
    override var player: Player?,
    val shipId : Long,
) : TkTarget(level, player) {
    val ship : ServerShip
        get() {
            if (level !is ServerLevel) {
                throw IllegalStateException("Cannot get a ship on the client side")
            }
            return Vs2Util.getServerShipWorld(level).loadedShips.getById(shipId)!!
        }
    var disassemblyTickCountdown : Int = -1
    var stuckTicks : Int = 0

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

    override fun mass(): Double = ship.inertiaData.mass

    override fun velocity(): Vec3 = ship.velocity.toVec3()
    override fun aabb(): AABB = this.ship.worldAABB.toAABB()

    override fun tick() {
        super.tick()
        // Disassemble the ship once the TTL has expired, if there is one
        if (disassemblyTickCountdown > 0) {
            disassemblyTickCountdown--
            if (disassemblyTickCountdown == 0) {
                disassemble(ship, level as ServerLevel)
            }
        }
    }

    fun updateStuckTicks(pointer: Vec3) {
        val stuck = (!aabb().contains(pointer) && velocity().length() < 1.0)
        stuckTicks = max(0, if (stuck) stuckTicks + 1 else stuckTicks - velocity().length().toInt())
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
        // Apply more force if we have been stuck to overcome friction
        val stuckScalar = 1 + (stuckTicks / 20.0)

        // More massive objects should move more sluggishly
        val exponent = if (this.player != null) {
            player!!.telekinesis().tier.massScalarExponent
        } else {
            1.0
        }
        val massScalar = 2 / (1 + (2 * (mass() / (DIRT_MASS * 2.5))).pow(exponent))

        val shipPos = ship.transform.positionInWorld.toVec3()

        // Apply a force to the ship to move it towards the pointer
        val force = shipPos.subtract(pos).normalize().negate().scale(ship.inertiaData.mass * SHIP_FORCE_SCALAR * massScalar * stuckScalar)

        // "Brake" to slow down the ship based on how aligned its velocity vector is to the direction of the pointer
        val angle = Math.toDegrees(ship.velocity.angle(force.toVector3d()))
        val brakeAngleScalar = angle / 180 * SHIP_BRAKE_SCALAR
        val brakeVelocityScalar = max(log(ship.velocity.length() + 1, 10.0), 0.0)
        val brakeForce = ship.velocity.toVec3().negate().normalize().scale(ship.inertiaData.mass * SHIP_FORCE_SCALAR * brakeAngleScalar * brakeVelocityScalar * massScalar)

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

    /**
     * Apply forces to the ship to try and align it with the world axes.
     * TODO this is not functional. Alignment logic is correct, but the ship oscillates around the target axis.
     * TODO Implement critical damping to stop oscillations.
     */
    fun stabilizeAlign() {
        if (ship.omega.length() < 1e-6) return  // normalizing a zero-length vector can cause NaNs
        val kP = 6.0
        val omegaNorm = ship.omega.normalize(Vector3d())
        val moi = ship.inertiaData.momentOfInertiaTensor.transform(omegaNorm, Vector3d()).dot(omegaNorm)
        val kD = 2 * sqrt(kP * moi)  // TODO this can't be right, since kD ends up WAY too big

        // Find which axis is closest to the ship's current orientation
        val allAxisVectors = setOf(
            Vec3(1.0, 0.0, 0.0),
            Vec3(0.0, 1.0, 0.0),
            Vec3(0.0, 0.0, 1.0),
            Vec3(-1.0, 0.0, 0.0),
            Vec3(0.0, -1.0, 0.0),
            Vec3(0.0, 0.0, -1.0),
        )
        val rotation = ship.transform.shipToWorldRotation
        val facingForward = Vec3(0.0, 0.0, 1.0)
        val facingVector = rotation.transform(facingForward.toVector3d()).toVec3()
        val closestAxis = allAxisVectors.maxBy { facingVector.distanceTo(it) }
        logger.info("Closest axis: $closestAxis")

        // Torque for forward alignment
        val alignmentThreshold = 1e-6
        val crossProduct = closestAxis.cross(facingVector)
        val alignTorque = if (crossProduct.length() > alignmentThreshold) {
            crossProduct.normalize().scale(kP)
        } else {
            Vec3(0.0, 0.0, 0.0)
        }

        // Torque for roll alignment
        val rollTo = Vec3(0.0, 1.0, 0.0)
        val topFacingVec = rotation.transform(rollTo.toVector3d()).toVec3()
        val rollClosestAxis = allAxisVectors.maxBy { topFacingVec.distanceTo(it) }
        val rollCrossProduct = rollClosestAxis.cross(topFacingVec)
        val rollTorque = if (rollCrossProduct.length() > alignmentThreshold) {
            rollCrossProduct.normalize().scale(kP)
        } else {
            Vec3(0.0, 0.0, 0.0)
        }

        val dampingTorque = ship.omega.toVec3().negate().scale(kD)
        val totalTorque = alignTorque.add(rollTorque).add(dampingTorque)
        val torque = ship.inertiaData.momentOfInertiaTensor.transform(totalTorque.toVector3d()).toVec3()
        torqueApplier().applyInvariantTorque(torque.toVector3d())





        // Apply drag torque to the ship to slow down its rotation when it's close to aligned
//        val dragAxis = ship.inertiaData.momentOfInertiaTensor.transform(ship.omega.toVec3().toVector3d()).toVec3()
//        val dragTorque = dragAxis.scale(-ship.omega.length()).scale(10.0)
//        torqueApplier().applyInvariantTorque(dragTorque.toVector3d())

        // Roll the ship to orient it correctly
//        val rollAmount = closestAxis.second(rotation)
//        val rollVec = facingVector.normalize().scale(rollAmount).negate()
//        val rollTorque = ship.inertiaData.momentOfInertiaTensor.transform(rollVec.toVector3d()).toVec3().scale(6.0)
//        torqueApplier().applyInvariantTorque(rollTorque.toVector3d())


//        fun differenceRatio(a: Double, b: Double): Double {
//            val epsilon = 1e-10
//            val maxAbs = maxOf(abs(a), abs(b), epsilon)
//            return abs(a - b) / maxAbs
//        }

        // Transform the rotation to be relative to the closet axis so that we snap to it
//        var rotation = ship.transform.shipToWorldRotation
//
//        val allAxisQuaternions = setOf(
//            Quaterniond().rotateY(0.0) to { q: Quaterniondc -> q.rotateY(0.0, Quaterniond()) },
//            Quaterniond().rotateY(PI / 2) to { q: Quaterniondc -> q.rotateY(PI / 2, Quaterniond()) },
//            Quaterniond().rotateY(PI) to { q: Quaterniondc -> q.rotateY(PI, Quaterniond()) },
//            Quaterniond().rotateY(3 * PI / 2) to { q: Quaterniondc -> q.rotateY(3 * PI / 2, Quaterniond()) },
//            Quaterniond().rotateX(PI / 2) to { q: Quaterniondc -> q.rotateX(PI / 2, Quaterniond()) },
//            Quaterniond().rotateX(3 * PI / 2) to { q: Quaterniondc -> q.rotateX(3 * PI / 2, Quaterniond()) },
//        )
//        val closestAxis = allAxisQuaternions.maxBy { abs(it.first.angle() / rotation.angle()) }
//        logger.info("Closest axis: ${closestAxis.first.angle()}")
//
//        // Rotate `rotation` so it's relative to the closest axis
//        rotation = closestAxis.second(rotation)
//
//        // Apply torque to the ship to try and zero out its rotation on all axes
//        val torqueVec = Vec3(rotation.x(), rotation.y(), rotation.z()).scale(-2.0)
//        val torque = ship.inertiaData.momentOfInertiaTensor.transform(torqueVec.toVector3d()).toVec3()

//        val rotation = ship.transform.shipToWorldRotation
//
//        val allAxisQuaternions = setOf(
//            Quaterniond().rotateY(0.0) to { q: Quaterniondc -> q.rotateY(0.0, Quaterniond()) },
//            Quaterniond().rotateY(Math.PI / 2) to { q: Quaterniondc -> q.rotateY(Math.PI / 2, Quaterniond()) },
//            Quaterniond().rotateY(Math.PI) to { q: Quaterniondc -> q.rotateY(Math.PI, Quaterniond()) },
//            Quaterniond().rotateY(3 * Math.PI / 2) to { q: Quaterniondc -> q.rotateY(3 * Math.PI / 2, Quaterniond()) },
//            Quaterniond().rotateX(Math.PI / 2) to { q: Quaterniondc -> q.rotateX(Math.PI / 2, Quaterniond()) },
//            Quaterniond().rotateX(3 * Math.PI / 2) to { q: Quaterniondc -> q.rotateX(3 * Math.PI / 2, Quaterniond()) },
//        )

//        // Find the closest axis using dot product
//        val closestAxis = allAxisQuaternions.maxBy { it.first.dot(rotation) }
//        logger.info("Rotation: ${rotation.normalize(Quaterniond())}")
//        logger.info("Closest axis: ${closestAxis.first}")
//
//        // Compute the relative rotation to align with the closest axis
//        val relativeRotation = Quaterniond(rotation).mul(Quaterniond(closestAxis.first).invert())
//
//        // Calculate torque vector to zero out misalignment
//        val axis = Vector3d(relativeRotation.x(), relativeRotation.y(), relativeRotation.z()).normalize()
//        val angle = relativeRotation.angle()
//        val torqueVec = axis.mul(-angle * 2.0) // Apply torque proportional to misalignment
//
//        // Transform torque by moment of inertia tensor
//        val torque = ship.inertiaData.momentOfInertiaTensor.transform(torqueVec).toVec3()
//
//
//        // Scale torque based on how far rotation is from being zeroed out
////        val distanceScalar = max(0.01, min(differenceRatio(rotation.x(), 0.0), min(differenceRatio(rotation.y(), 0.0), differenceRatio(rotation.z(), 0.0))))
//
//        torqueApplier().applyInvariantTorque(torque.toVector3d())
//

    }

    override fun equals(other: Any?): Boolean {
        return other is ShipTkTarget && other.shipId == this.shipId
    }
}