package com.godlike.common.telekinesis

import com.godlike.common.Godlike
import com.godlike.common.util.toAABB
import net.minecraft.core.Direction
import net.minecraft.core.Vec3i
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.sounds.SoundEvents
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3

abstract class TkTarget(
    open val level: Level,
    open var player : Player?
) {
    var hoverPos : Vec3? = null
    var chargingLaunch : Boolean = false
    private val entitiesHitThisLaunch = mutableSetOf<Entity>()
    private var launchStillnessTicks = 0
    var isLaunching: Boolean = false
        set(value) {
            field = value
            entitiesHitThisLaunch.clear()
            launchStillnessTicks = 0
        }

    companion object {
        fun fromNbtAndPlayer(tag: CompoundTag, player: Player) : TkTarget {
            val target = if (tag.contains("shipId")) {
                ShipTkTarget(player.level(), player, tag.getLong("shipId"))
            } else if (tag.contains("entityId")) {
                EntityTkTarget(player.level(), player, tag.getInt("entityId"))
            } else {
                throw IllegalArgumentException("Invalid TkTarget NBT tag")
            }
            loadNbt(tag, target)
            return target
        }

        fun fromNbtAndLevel(tag: CompoundTag, level: Level) : TkTarget {
            val target = if (tag.contains("shipId")) {
                ShipTkTarget(level, null, tag.getLong("shipId"))
            } else if (tag.contains("entityId")) {
                EntityTkTarget(level, null, tag.getInt("entityId"))
            } else {
                throw IllegalArgumentException("Invalid TkTarget NBT tag")
            }
            loadNbt(tag, target)
            return target
        }

        private fun loadNbt(tag: CompoundTag, target: TkTarget) {
            if (tag.contains("hoverPos.x")) {
                target.hoverPos = Vec3(
                    tag.getDouble("hoverPos.x"),
                    tag.getDouble("hoverPos.y"),
                    tag.getDouble("hoverPos.z")
                )
            }
            target.chargingLaunch = tag.getBoolean("chargingLaunch")
            target.isLaunching = tag.getBoolean("isLaunching")
            if (target is ShipTkTarget) {
                target.disassemblyTickCountdown = tag.getInt("disassemblyTickCountdown")
            }
        }
    }

    abstract fun pos() : Vec3

    abstract fun moveToward(pos: Vec3)

    abstract fun addLiftForce()

    abstract fun rotateTowardPointer(pointer: Vec3, playerEyePos: Vec3)

    abstract fun rotate(up: Boolean, down: Boolean, left: Boolean, right: Boolean, playerEyePos: Vec3)

    abstract fun addRotationDrag()

    abstract fun place(level : ServerLevel)

    abstract fun placeAt(level: ServerLevel, pos: Vec3i, topFacing: Direction, frontFacing: Direction): Boolean

    abstract fun launchToward(pos: Vec3)

    abstract fun toNbt() : CompoundTag

    abstract fun mass() : Double

    abstract fun velocity() : Vec3

    abstract fun aabb() : AABB

    open fun tick() {
        if (isLaunching) {
            launchingTick()
        }
    }

    abstract fun exists() : Boolean

    open fun launchingTick() {
        // Stop "launching" if the ship's velocity has dropped sufficiently for the enough consecutive ticks
        if (velocity().length() < LAUNCH_VELOCITY_THRESHOLD) {
            launchStillnessTicks++
            if (launchStillnessTicks > 5) {
                Godlike.logger.info("stopping launch, v=${velocity().length()}")
                this.isLaunching = false
            }
        } else {
            launchStillnessTicks = 0
        }

        // Damage entities in the ship's path
        val hitBox = aabb().inflate(1.0)
        val damage = mass() / DIRT_MASS * LAUNCH_BASE_DAMAGE
        this.level.getEntities(null, hitBox).filter { !entitiesHitThisLaunch.contains(it) }.forEach { entity ->
            entitiesHitThisLaunch.add(entity)
            if (entity is LivingEntity) {
                entity.knockback(0.5, entity.position().x - pos().x, entity.position().z - pos().z)
            }
            entity.hurt(entity.damageSources().flyIntoWall(), damage.toFloat())
            entity.playSound(
                SoundEvents.ANVIL_LAND,
                1.0f,
                0.0f
            )
        }
    }

    /**
     * Get the set of pointers that are dist blocks away in every axis direction (up, down, +x, -x, +z, -z).a
     * Directions are based on world axis; the target's rotation is irrelevant.
     */
    fun getAxisPointers(dist : Double) : Set<Vec3> {
        val pos = pos()
        return setOf(
            pos.add(0.0, aabb().ysize / 2 + dist, 0.0),
            pos.add(0.0, -aabb().ysize / 2 - dist, 0.0),
            pos.add(aabb().xsize / 2 + dist, 0.0, 0.0),
            pos.add(-aabb().xsize / 2 - dist, 0.0, 0.0),
            pos.add(0.0, 0.0, aabb().zsize / 2 + dist),
            pos.add(0.0, 0.0, -aabb().zsize / 2 - dist)
        )
    }
}