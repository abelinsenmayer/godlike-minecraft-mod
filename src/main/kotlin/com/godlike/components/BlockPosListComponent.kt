package com.godlike.components

import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.RegistryWrapper
import net.minecraft.util.math.BlockPos
import org.ladysnake.cca.api.v3.component.Component

const val positions = "block-pos-list"

/**
 * A component that stores a list of BlockPos
 */
class BlockPosListComponent : Component {
    private val positions : MutableList<BlockPos> = mutableListOf()

    override fun readFromNbt(tag: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup) {
        tag.getLongArray(com.godlike.components.positions).forEach {
            positions.add(BlockPos.fromLong(it))
        }
    }

    override fun writeToNbt(tag: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup) {
        this.positions.map { it.asLong() }.toLongArray().let {
            tag.putLongArray(com.godlike.components.positions, it)
        }
    }

    fun addPosition(pos: BlockPos) {
        positions.add(pos)
    }

    fun addAllPositions(posList: List<BlockPos>) {
        positions.addAll(posList)
    }

    fun getPositions(): List<BlockPos> {
        return positions
    }

    fun clearPositions() {
        positions.clear()
    }

    fun removePosition(pos: BlockPos) {
        positions.remove(pos)
    }
}