package com.godlike.components

import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerPlayer
import java.util.concurrent.atomic.AtomicLong

const val blockPos = "block-pos"

class BlockPosComponent(private val provider : Any) : AutoSyncedComponent {
    private val pos = AtomicLong(0L)

    override fun shouldSyncWith(player: ServerPlayer?): Boolean {
        // we only want to sync this data with the player that owns it
        // this reduces network traffic and prevents other players from the value
        return player == provider
    }

    override fun readFromNbt(tag: CompoundTag) {
        setPos(BlockPos.of(tag.getLong(blockPos)))
    }

    override fun writeToNbt(tag: CompoundTag) {
        tag.putLong(blockPos, pos.get())
    }

    fun setPos(pos: BlockPos) {
        this.pos.set(pos.asLong())
        ModComponents.TARGET_POSITION.sync(provider)
    }

    fun getPos(): BlockPos {
        return BlockPos.of(this.pos.get())
    }
}