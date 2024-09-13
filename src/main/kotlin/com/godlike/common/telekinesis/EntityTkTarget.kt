package com.godlike.common.telekinesis

import com.godlike.common.util.negate
import com.godlike.common.util.toVector3d
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3
import kotlin.math.log
import kotlin.math.max

const val ENTITY_FORCE_SCALAR = 0.1
const val ENTITY_BRAKE_SCALAR = 5.0

class EntityTkTarget(
    override val player: Player,
    private val entityId: Int,
    override var hoverPos: Vec3? = null
) : TkTarget {
    val entity : Entity
        get() {
            return player.level().getEntity(entityId)!!
        }

    override fun pos(): Vec3 {
        return entity.position().add(0.0, entity.boundingBox.ysize / 2, 0.0)
    }

    private fun addForce(force: Vec3) {
        entity.push(force.x, force.y, force.z)
    }

    fun Entity.mass(): Double {
        return entity.boundingBox.size
    }

    override fun moveToward(pos: Vec3) {

        // Apply a force to the entity to move it towards the pointer
        val force = pos().subtract(pos).normalize().negate().scale(ENTITY_FORCE_SCALAR)

        // "Brake" to slow down the entity based on how aligned its velocity vector is to the direction of the pointer
        val angle = Math.toDegrees(entity.deltaMovement.toVector3d().angle(force.toVector3d()))
        val brakeAngleScalar = angle / 180 * ENTITY_BRAKE_SCALAR
        val brakeVelocityScalar = max(log(entity.deltaMovement.length() + 1, 10.0), 0.0)
        val brakeForce = entity.deltaMovement.negate().normalize().scale(ENTITY_FORCE_SCALAR * brakeAngleScalar * brakeVelocityScalar)

        // Reduce the force when we're very near the pointer to stop the entity from oscillating
        val distance = pos().distanceTo(pos)
        val distanceScalar = max(log(distance + 0.8, 10.0), 0.0)

        // Apply the force
        addForce(force.scale(distanceScalar).add(brakeForce))
    }

    override fun addLiftForce() {
        entity.fallDistance = entity.deltaMovement.length().toFloat() * 4
        addForce(Vec3(0.0, 0.08, 0.0))
    }

    override fun rotateTowardPointer(pointer: Vec3, playerEyePos: Vec3) {
        // TODO
    }

    override fun addRotationDrag() {
        // TODO
    }

    override fun place(level: ServerLevel) {
        // TODO: set riding entity!!
    }

    override fun toNbt(): CompoundTag {
        val tag = CompoundTag()
        tag.putInt("entityId", entityId)
        hoverPos?.let {
            tag.putDouble("hoverPos.x", it.x)
            tag.putDouble("hoverPos.y", it.y)
            tag.putDouble("hoverPos.z", it.z)
        }
        return tag
    }
}