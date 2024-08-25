package com.godlike.components

import com.godlike.vs2.Vs2Util
import dev.onyxstudios.cca.api.v3.component.Component
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerPlayer
import org.valkyrienskies.core.api.ships.ServerShip
import org.valkyrienskies.core.api.ships.Ship

const val TK_TARGETS_KEY = "telekinesis-targets"

/**
 * A component that stores data about a player's telekinesis targets and settings.
 * Designed to be used exclusively on the server side. If a client-side action should be taken based on this data, the
 * server should send a packet.
 */
class TelekinesisComponent(private val player: ServerPlayer) : Component {
    val tkTargets : MutableList<ServerShip> = mutableListOf()

    override fun readFromNbt(tag: CompoundTag) {
        val shipWorld = Vs2Util.getServerShipWorld(player.serverLevel())
        tkTargets.clear()
        tag.getLongArray(TK_TARGETS_KEY).map { shipWorld.loadedShips.getById(it) as ServerShip}.let {
            tkTargets.addAll(it)
        }
    }

    override fun writeToNbt(tag: CompoundTag) {
        tkTargets.map { it.id }.toLongArray().let {
            tag.putLongArray(TK_TARGETS_KEY, it)
        }
    }
}