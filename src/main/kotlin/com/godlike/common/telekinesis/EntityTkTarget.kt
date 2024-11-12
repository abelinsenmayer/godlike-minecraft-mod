package com.godlike.common.telekinesis

import com.godlike.common.Godlike
import com.godlike.common.components.telekinesis
import com.godlike.common.util.negate
import com.godlike.common.util.toAABB
import com.godlike.common.util.toVector3d
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3
import kotlin.math.log
import kotlin.math.max

const val ENTITY_FORCE_SCALAR = 0.1
const val ENTITY_BRAKE_SCALAR = 5.0
const val ENTITY_LAUNCH_SCALAR = 45.0
const val ENTITY_LAUNCH_VELOCITY_THRESHOLD = 0.4

class EntityTkTarget(
    override val player: Player,
    private val entityId: Int,
    override var hoverPos: Vec3? = null,
    override var chargingLaunch: Boolean = false
) : TkTarget {
    val entity : Entity
        get() {
            return player.level().getEntity(entityId)!!
        }
    private var launchStillnessTicks = 0
    override var isLaunching: Boolean = false
        set(value) {
            field = value
            launchStillnessTicks = 0
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
        return entity.boundingBox.ysize * entity.boundingBox.xsize * entity.boundingBox.zsize
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
    }

    private fun launchingTick() {
        // Stop "launching" if the ship's velocity has dropped sufficiently for the enough consecutive ticks
        if (this.entity.deltaMovement.length() < ENTITY_LAUNCH_VELOCITY_THRESHOLD) {
            launchStillnessTicks++
            if (launchStillnessTicks > 5) {
                Godlike.logger.info("stopping launch, v=${this.entity.deltaMovement.length()}")
                this.isLaunching = false
            }
        } else {
            launchStillnessTicks = 0
        }

        // Damage entities in the target's path
        val hitBox = this.entity.boundingBox.inflate(0.5) ?: return
        this.player.level().getEntities(this.entity, hitBox).forEach { hitEntity ->
            hitEntity.hurt(hitEntity.damageSources().flyIntoWall(), LAUNCH_DAMAGE)
            hitEntity.playSound(
                if (LAUNCH_DAMAGE > 4) SoundEvents.GENERIC_BIG_FALL else SoundEvents.GENERIC_SMALL_FALL,
                1.0f,
                1.0f
            )
            Godlike.logger.info("Hit for $LAUNCH_DAMAGE damage at velocity ${this.entity.deltaMovement.length()}.")
        }

        // Damage the target (only once per tick)
        entity.hurt(entity.damageSources().flyIntoWall(), LAUNCH_DAMAGE)
        entity.playSound(
            if (LAUNCH_DAMAGE > 4) SoundEvents.GENERIC_BIG_FALL else SoundEvents.GENERIC_SMALL_FALL,
            1.0f,
            1.0f
        )
        Godlike.logger.info("Hit for $LAUNCH_DAMAGE damage at velocity ${this.entity.deltaMovement.length()}.")
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