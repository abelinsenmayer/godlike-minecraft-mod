package com.godlike.common.components

import com.godlike.common.vs2.Vs2Util
import dev.onyxstudios.cca.api.v3.component.Component
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerPlayer

const val TK_TARGETS_KEY = "telekinesis-targets"

/**
 * A component that stores data about a player's telekinesis targets and settings.
 * Designed to be used exclusively on the server side. If a client-side action should be taken based on this data, the
 * server should send a packet.
 */
class TelekinesisComponent(private val player: ServerPlayer) : Component {
    val tkShipIds : MutableList<Long> = mutableListOf()

    override fun readFromNbt(tag: CompoundTag) {
        tkShipIds.clear()
        tag.getLongArray(TK_TARGETS_KEY).forEach {
            tkShipIds.add(it)
        }
    }

    override fun writeToNbt(tag: CompoundTag) {
        tkShipIds.toLongArray().let {
            tag.putLongArray(TK_TARGETS_KEY, it)
        }
    }
}