package com.godlike.common.telekinesis

import com.godlike.common.Godlike
import com.godlike.common.Godlike.logger
import com.godlike.common.util.negate
import com.godlike.common.util.toVector3d
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import kotlin.math.log
import kotlin.math.max

const val ENTITY_FORCE_SCALAR = 0.15
const val ENTITY_BRAKE_SCALAR = 6.0
const val ENTITY_LAUNCH_SCALAR = 45.0
const val DELTA_MOVEMENT_TO_VELOCITY_SCALAR = 20.0
const val DIRT_MASS = 1220.0

class EntityTkTarget(
    override val level: Level,
    override var player: Player?,
    val entityId: Int
) : TkTarget(level, player) {
    val entity : Entity
        get() {
            return level.getEntity(entityId)!!
        }

    override fun pos(): Vec3 {
        return entity.position().add(0.0, entity.boundingBox.ysize / 2, 0.0)
    }

    private fun addForce(force: Vec3) {
        entity.push(force.x, force.y, force.z)
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
        val antiGravForce = if (entity.type == EntityType.ITEM) {
            Vec3(0.0, 0.04, 0.0)
        } else {
            Vec3(0.0, 0.08, 0.0)
        }
        addForce(antiGravForce)
    }

    override fun rotateTowardPointer(pointer: Vec3, playerEyePos: Vec3) {
        // NOOP for entity
    }

    override fun addRotationDrag() {
        // NOOP for entity
    }

    override fun place(level: ServerLevel) {
        // NOOP for entity
    }

    override fun launchToward(pos: Vec3) {
        val entityToPos = pos.subtract(pos())
        val force = entityToPos.normalize().scale(ENTITY_FORCE_SCALAR * ENTITY_LAUNCH_SCALAR)
        addForce(force)
    }

    override fun toNbt(): CompoundTag {
        val tag = CompoundTag()
        tag.putInt("entityId", entityId)
        hoverPos?.let {
            tag.putDouble("hoverPos.x", it.x)
            tag.putDouble("hoverPos.y", it.y)
            tag.putDouble("hoverPos.z", it.z)
        }
        tag.putBoolean("chargingLaunch", chargingLaunch)
        tag.putBoolean("isLaunching", isLaunching)
        return tag
    }

    override fun mass(): Double {
        return if (entity is LivingEntity) {
            ((entity as LivingEntity).maxHealth / 10) * DIRT_MASS
        } else {
            DIRT_MASS
        }
    }

    override fun velocity(): Vec3  = entity.deltaMovement.scale(DELTA_MOVEMENT_TO_VELOCITY_SCALAR)
    override fun aabb(): AABB = entity.boundingBox

    /**
     * Called every tick on the server side to update telekinesis targets.
     * Note that player TK controls are handled separately; this is for things that should happen every tick regardless
     * of controlling player.
     */
    override fun tick() {
        if (isLaunching) {
            launchingTick()
        }
    }

    override fun exists(): Boolean {
        return try {
            entity
            true
        } catch (e: NullPointerException) {
            false
        }
    }

    override fun equals(other: Any?): Boolean {
        return other is EntityTkTarget && other.entityId == entityId
    }
}