package com.godlike.components

import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.RegistryWrapper
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.BlockPos
import org.ladysnake.cca.api.v3.component.Component
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent
import org.slf4j.LoggerFactory
import java.util.*

const val positions = "block-pos-list"

/**
 * A component that stores a list of BlockPos.
 * The implementation is thread-safe and synced between client and server.
 */
class BlockPosListComponent(private val provider : Any) : AutoSyncedComponent {
    private val positions : MutableList<BlockPos> = Collections.synchronizedList(mutableListOf())

    override fun shouldSyncWith(player: ServerPlayerEntity?): Boolean {
        // we only want to sync this data with the player that owns it
        // this reduces network traffic and prevents other players from seeing the cursor positions
        return player == provider
    }

    override fun readFromNbt(tag: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup) {
        clearPositions()
        addAllPositions(tag.getLongArray(com.godlike.components.positions).map { BlockPos.fromLong(it) })
    }

    override fun writeToNbt(tag: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup) {
        synchronized(positions) {
            positions.map { it.asLong() }.toLongArray().let {
                tag.putLongArray(com.godlike.components.positions, it)
            }
        }
    }

    fun addPosition(pos: BlockPos) {
        synchronized(positions) {
            positions.add(pos)
            ModComponents.CURSORS.sync(provider)
            ModComponents.CURSOR_PREVIEWS.sync(provider)
        }
    }

    fun addAllPositions(posList: List<BlockPos>) {
        synchronized(positions) {
            positions.addAll(posList)
            ModComponents.CURSORS.sync(provider)
            ModComponents.CURSOR_PREVIEWS.sync(provider)
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
            ModComponents.CURSORS.sync(provider)
            ModComponents.CURSOR_PREVIEWS.sync(provider)
        }
    }

    fun removePosition(pos: BlockPos) {
        synchronized(positions) {
            positions.remove(pos)
            ModComponents.CURSORS.sync(provider)
            ModComponents.CURSOR_PREVIEWS.sync(provider)
        }
    }
}