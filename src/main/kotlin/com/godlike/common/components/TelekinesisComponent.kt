package com.godlike.common.components

import com.godlike.common.vs2.Vs2Util
import dev.onyxstudios.cca.api.v3.component.Component
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent
import net.minecraft.nbt.CompoundTag
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
    val tkShipIds : MutableList<Long> = mutableListOf()  // TODO we may need to sync this on set
    var pointerDistance : Double = 0.0

    override fun shouldSyncWith(player: ServerPlayer?): Boolean {
        // we only want to sync this data with the player that owns it
        // this reduces network traffic and prevents other players from seeing the cursor positions
        return player == this.player
    }

    override fun readFromNbt(tag: CompoundTag) {
        tkShipIds.clear()
        tag.getLongArray(TK_TARGETS_KEY).forEach {
            tkShipIds.add(it)
        }
        pointerDistance = tag.getDouble(POINTER_DISTANCE_KEY)
    }

    override fun writeToNbt(tag: CompoundTag) {
        tkShipIds.toLongArray().let {
            tag.putLongArray(TK_TARGETS_KEY, it)
        }
        tag.putDouble(POINTER_DISTANCE_KEY, pointerDistance)
    }
}

fun Player.telekinesis(): TelekinesisComponent {
    return ModComponents.TELEKINESIS_DATA.get(this)
}