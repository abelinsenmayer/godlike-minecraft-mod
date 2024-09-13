package com.godlike.common.telekinesis

import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3

interface TkTarget {
    val player : Player
    var hoverPos : Vec3?

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

    fun pos() : Vec3

    fun moveToward(pos: Vec3)

    fun addLiftForce()

    fun rotateTowardPointer(pointer: Vec3, playerEyePos: Vec3)

    fun addRotationDrag()

    fun place(level : ServerLevel)

    fun toNbt() : CompoundTag
}