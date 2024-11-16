package com.godlike.common.telekinesis

import com.godlike.common.Godlike.logger
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3

interface TkTarget {
    val level: Level
    var player : Player?
    var hoverPos : Vec3?
    var chargingLaunch : Boolean
    var isLaunching : Boolean

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

    fun pos() : Vec3

    fun moveToward(pos: Vec3)

    fun addLiftForce()

    fun rotateTowardPointer(pointer: Vec3, playerEyePos: Vec3)

    fun addRotationDrag()

    fun place(level : ServerLevel)

    fun launchToward(pos: Vec3)

    fun toNbt() : CompoundTag

    fun mass() : Double

    /**
     * Called every tick on the server side to update telekinesis targets.
     * Note that player TK controls are handled separately; this is for things that should happen every tick regardless
     * of controlling player.
     */
    fun tick()

    fun exists() : Boolean
}