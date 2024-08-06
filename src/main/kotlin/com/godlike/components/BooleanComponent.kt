package com.godlike.components

import kotlinx.atomicfu.AtomicBoolean
import kotlinx.atomicfu.TraceBase
import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.RegistryWrapper
import net.minecraft.server.network.ServerPlayerEntity
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent

const val booleanValue = "boolean-value"

class BooleanComponent(private val provider : Any) : AutoSyncedComponent {
    private var value = false
    override fun shouldSyncWith(player: ServerPlayerEntity?): Boolean {
        // we only want to sync this data with the player that owns it
        // this reduces network traffic
        return player == provider
    }

    override fun readFromNbt(tag: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup) {
        value = tag.getBoolean(booleanValue)
    }

    override fun writeToNbt(tag: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup) {
        tag.putBoolean(booleanValue, value)
    }

    fun setValue(value: Boolean) {
        this.value = value
        ModComponents.SELECTION_MODE.sync(provider)
    }

    fun getValue(): Boolean {
        return value
    }
}