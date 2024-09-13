package com.godlike.common.components

import com.godlike.common.Godlike.logger
import com.godlike.common.telekinesis.ShipTkTarget
import com.godlike.common.telekinesis.TkTarget
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player

const val TK_TARGETS_KEY = "telekinesis-targets"
const val POINTER_DISTANCE_KEY = "pointer-distance"

/**
 * A component that stores data about a player's telekinesis targets and settings.
 * Designed to be used exclusively on the server side. If a client-side action should be taken based on this data, the
 * server should send a packet.
 */
class TelekinesisComponent(private val player: Player) : AutoSyncedComponent {
    private val tkTargets : MutableList<TkTarget> = mutableListOf()
    var pointerDistance : Double = 0.0

    override fun shouldSyncWith(player: ServerPlayer?): Boolean {
        // we only want to sync this data with the player that owns it
        // this reduces network traffic and prevents other players from seeing the cursor positions
        return player == this.player
    }

    override fun readFromNbt(tag: CompoundTag) {
        tkTargets.clear()
        pointerDistance = tag.getDouble(POINTER_DISTANCE_KEY)
        tag.getList(TK_TARGETS_KEY, 10).forEach {
            tkTargets.add(ShipTkTarget.fromNbtAndPlayer(it as CompoundTag, player))
        }
        sync()
    }

    override fun writeToNbt(tag: CompoundTag) {
        tag.putDouble(POINTER_DISTANCE_KEY, pointerDistance)
        val listTag = ListTag()
        tkTargets.forEach {
            listTag.add(it.toNbt())
        }
        tag.put(TK_TARGETS_KEY, listTag)
    }

    fun sync() {
        if (player is ServerPlayer) {
            ModComponents.TELEKINESIS_DATA.sync(player)
        }
    }

    fun hasNonHoveringTarget(): Boolean {
        return tkTargets.any { it.hoverPos == null }
    }

    fun getShipTargets(): List<TkTarget> {
        return tkTargets.toList()
    }

    fun clearTargets() {
        tkTargets.clear()
        sync()
    }

    fun addTarget(target: TkTarget) {
        tkTargets.add(target)
        sync()
    }

    fun removeTarget(target: TkTarget) {
        tkTargets.remove(target)
        sync()
    }

    fun removeTargetsWhere(predicate: (TkTarget) -> Boolean) {
        tkTargets.removeIf(predicate)
        sync()
    }

    fun addShipIdAsTarget(id: Long) {
        val target = tkTargets.find { it is ShipTkTarget && it.ship.id == id }
        if (target != null) {
            if (target.hoverPos != null) {
                target.hoverPos = null
            }
            return
        }
        tkTargets.add(ShipTkTarget(id, player))
        sync()
    }

    fun removeShipIdAsTarget(id: Long) {
        tkTargets.removeIf { it is ShipTkTarget && it.shipId == id }
        sync()
    }
}

fun Player.telekinesis(): TelekinesisComponent {
    return ModComponents.TELEKINESIS_DATA.get(this)
}