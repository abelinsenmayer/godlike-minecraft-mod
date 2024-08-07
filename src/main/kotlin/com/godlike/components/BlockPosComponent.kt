package com.godlike.components

import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.RegistryWrapper
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.util.math.BlockPos
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent
import java.util.concurrent.atomic.AtomicLong

const val blockPos = "block-pos"

class BlockPosComponent(private val provider : Any) : AutoSyncedComponent {
    private val pos = AtomicLong(0L)

    override fun shouldSyncWith(player: ServerPlayerEntity?): Boolean {
        // we only want to sync this data with the player that owns it
        // this reduces network traffic and prevents other players from the value
        return player == provider
    }

    override fun readFromNbt(tag: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup) {
        setPos(BlockPos.fromLong(tag.getLong(blockPos)))
    }

    override fun writeToNbt(tag: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup) {
        tag.putLong(blockPos, pos.get())
    }

    fun setPos(pos: BlockPos) {
        this.pos.set(pos.asLong())
        ModComponents.TARGET_POSITION.sync(provider)
    }

    fun getPos(): BlockPos {
        return BlockPos.fromLong(this.pos.get())
    }
}