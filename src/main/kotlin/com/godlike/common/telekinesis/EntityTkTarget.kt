package com.godlike.common.telekinesis

import com.godlike.common.Godlike.logger
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3

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
        return entity.position()
    }

    override fun moveToward(pos: Vec3) {
        // TODO
    }

    private fun addForce(force: Vec3) {
        entity.push(force.x, force.y, force.z)
    }

    override fun addLiftForce() {
        addForce(Vec3(0.0, 0.1, 0.0))
    }

    override fun rotateTowardPointer(pointer: Vec3, playerEyePos: Vec3) {
        // TODO
    }

    override fun addRotationDrag() {
        // TODO
    }

    override fun place(level: ServerLevel) {
        // TODO
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