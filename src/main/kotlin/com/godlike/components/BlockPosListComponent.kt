package com.godlike.components

import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerPlayer
import java.util.*

const val positions = "block-pos-list"

/**
 * A component that stores a list of BlockPos.
 * The implementation is thread-safe and synced between client and server.
 */
class BlockPosListComponent(private val provider : Any) : AutoSyncedComponent {
    private val positions : MutableList<BlockPos> = Collections.synchronizedList(mutableListOf())

    override fun shouldSyncWith(player: ServerPlayer?): Boolean {
        // we only want to sync this data with the player that owns it
        // this reduces network traffic and prevents other players from seeing the cursor positions
        return player == provider
    }

    override fun readFromNbt(tag: CompoundTag) {
        clearPositions()
        addAllPositions(tag.getLongArray(com.godlike.components.positions).map { BlockPos.of(it) })
    }

    override fun writeToNbt(tag: CompoundTag) {
        synchronized(positions) {
            positions.map { it.asLong() }.toLongArray().let {
                tag.putLongArray(com.godlike.components.positions, it)
            }
        }
    }

    fun addPosition(pos: BlockPos) {
        synchronized(positions) {
            positions.add(pos)
            ModComponents.CURSOR_COMPONENT_TYPES.forEach { it.sync(provider) }
        }
    }

    fun addAllPositions(posList: List<BlockPos>) {
        synchronized(positions) {
            positions.addAll(posList)
            ModComponents.CURSOR_COMPONENT_TYPES.forEach { it.sync(provider) }
        }
    }

    fun getPositions(): List<BlockPos> {
        synchronized(positions) {
            return positions.toList()
        }
    }

    fun clearPositions() {
        synchronized(positions) {
            positions.clear()
            ModComponents.CURSOR_COMPONENT_TYPES.forEach { it.sync(provider) }
        }
    }

    fun removePosition(pos: BlockPos) {
        synchronized(positions) {
            positions.remove(pos)
            ModComponents.CURSOR_COMPONENT_TYPES.forEach { it.sync(provider) }
        }
    }
}