package com.godlike.common.telekinesis

import com.godlike.common.Godlike.logger
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3

interface TkTarget {
    val player : Player
    var hoverPos : Vec3?
    var chargingLaunch : Boolean

    companion object {
        fun fromNbtAndPlayer(tag: CompoundTag, player: Player) : TkTarget {
            val target = if (tag.contains("shipId")) {
                ShipTkTarget(tag.getLong("shipId"), player)
            } else if (tag.contains("entityId")) {
                EntityTkTarget(player, tag.getInt("entityId"))
            } else {
                throw IllegalArgumentException("Invalid TkTarget NBT tag")
            }
            if (tag.contains("hoverPos.x")) {
                target.hoverPos = Vec3(
                    tag.getDouble("hoverPos.x"),
                    tag.getDouble("hoverPos.y"),
                    tag.getDouble("hoverPos.z")
                )
            }
            target.chargingLaunch = tag.getBoolean("chargingLaunch")
            return target
        }
    }

    fun pos() : Vec3

    fun moveToward(pos: Vec3)

    fun addLiftForce()

    fun rotateTowardPointer(pointer: Vec3, playerEyePos: Vec3)

    fun addRotationDrag()

    fun place(level : ServerLevel)

    fun launchToward(pos: Vec3)

    fun toNbt() : CompoundTag
}