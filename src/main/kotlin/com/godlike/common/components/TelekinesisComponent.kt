package com.godlike.common.components

import com.godlike.common.Godlike.logger
import com.godlike.common.telekinesis.ShipTkTarget
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
    private val tkShips : MutableList<ShipTkTarget> = mutableListOf()
    var pointerDistance : Double = 0.0

    override fun shouldSyncWith(player: ServerPlayer?): Boolean {
        // we only want to sync this data with the player that owns it
        // this reduces network traffic and prevents other players from seeing the cursor positions
        return player == this.player
    }

    override fun readFromNbt(tag: CompoundTag) {
        tkShips.clear()
        pointerDistance = tag.getDouble(POINTER_DISTANCE_KEY)
        tag.getList(TK_TARGETS_KEY, 10).forEach {
            tkShips.add(ShipTkTarget.fromNbtAndPlayer(it as CompoundTag, player))
        }
        sync()
    }

    override fun writeToNbt(tag: CompoundTag) {
        tag.putDouble(POINTER_DISTANCE_KEY, pointerDistance)
        val listTag = ListTag()
        tkShips.forEach {
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
        return tkShips.any { it.hoverPos == null }
    }

    fun getShipTargets(): List<ShipTkTarget> {
        return tkShips.toList()
    }

    fun clearShipTargets() {
        tkShips.clear()
        sync()
    }

    fun addShipIdAsTarget(id: Long) {
        val target = tkShips.find { it.ship.id == id }
        if (target != null) {
            if (target.hoverPos != null) {
                logger.info("resetting hover pos")
                target.hoverPos = null
            }
            return
        }
        tkShips.add(ShipTkTarget(id, player))
        sync()
    }

    fun removeShipIdAsTarget(id: Long) {
        tkShips.removeIf { it.shipId == id }
        sync()
    }
}

fun Player.telekinesis(): TelekinesisComponent {
    return ModComponents.TELEKINESIS_DATA.get(this)
}