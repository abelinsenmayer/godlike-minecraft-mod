package com.godlike.common.telekinesis

import com.godlike.common.Godlike.logger
import com.godlike.common.util.disassemble
import com.godlike.common.util.negate
import com.godlike.common.util.toVec3
import com.godlike.common.util.toVector3d
import com.godlike.common.vs2.Vs2Util
import net.minecraft.client.player.LocalPlayer
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3
import org.joml.Vector3d
import org.valkyrienskies.core.api.ships.ServerShip
import org.valkyrienskies.mod.common.util.GameTickForceApplier
import kotlin.math.log
import kotlin.math.max

class ShipTkTarget(
    val shipId : Long,
    override val player: Player,
    override var hoverPos: Vec3? = null
) : TkTarget {
    val ship : ServerShip
        get() {
            if (player is LocalPlayer) {
                throw IllegalStateException("Cannot get a ship on the client side")
            }
            return Vs2Util.getServerShipWorld((player as ServerPlayer).serverLevel()).loadedShips.getById(shipId)!!
        }

    companion object {
        fun fromNbtAndPlayer(tag: CompoundTag, player: Player) : ShipTkTarget {
            val shipId = tag.getLong("shipId")
            val target = ShipTkTarget(shipId, player)
            if (tag.contains("anchorPos.x")) {
                target.hoverPos = Vec3(
                    tag.getDouble("anchorPos.x"),
                    tag.getDouble("anchorPos.y"),
                    tag.getDouble("anchorPos.z")
                )
            }
            return target
        }
    }

    override fun toNbt() : CompoundTag {
        val tag = CompoundTag()
        tag.putLong("shipId", shipId)
        hoverPos?.let {
            tag.putDouble("anchorPos.x", it.x)
            tag.putDouble("anchorPos.y", it.y)
            tag.putDouble("anchorPos.z", it.z)
        }
        return tag
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

    override fun pos(): Vec3 {
        return ship.transform.positionInWorld.toVec3()
    }

    override fun moveToward(pos: Vec3) {
        val shipPos = ship.transform.positionInWorld.toVec3()

        // Apply a force to the ship to move it towards the pointer
        val force = shipPos.subtract(pos).normalize().negate().scale(ship.inertiaData.mass * TK_SCALAR)

        // "Brake" to slow down the ship based on how aligned its velocity vector is to the direction of the pointer
        val angle = Math.toDegrees(ship.velocity.angle(force.toVector3d()))
        val brakeAngleScalar = angle / 180 * BRAKE_SCALAR
        val brakeVelocityScalar = max(log(ship.velocity.length() + 1, 10.0), 0.0)
        val brakeForce = ship.velocity.toVec3().negate().normalize().scale(ship.inertiaData.mass * TK_SCALAR * brakeAngleScalar * brakeVelocityScalar)

        // Reduce the force when we're very near the pointer to stop the ship from oscillating
        val distance = shipPos.distanceTo(pos)
        val distanceScalar = max(log(distance + 0.8, 10.0), 0.0)

        // Apply the force
        forceApplier().applyInvariantTorque(force.scale(distanceScalar).add(brakeForce).toVector3d())
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
        val torque = shipToPointer.cross(playerToShip).normalize().scale(ship.inertiaData.mass/10 * TK_SCALAR)

        torqueApplier().applyInvariantTorque(torque.toVector3d())
    }

    override fun addRotationDrag() {
        val dragForce = ship.omega.toVec3().scale(-ship.omega.length()).scale(TK_SCALAR * BRAKE_SCALAR)
        torqueApplier().applyInvariantTorque(dragForce.toVector3d())
    }
}