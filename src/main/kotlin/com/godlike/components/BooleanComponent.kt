package com.godlike.components

import net.minecraft.nbt.NbtCompound
import net.minecraft.registry.RegistryWrapper
import net.minecraft.server.network.ServerPlayerEntity
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent
import java.util.concurrent.atomic.AtomicBoolean

const val booleanValue = "boolean-value"

class BooleanComponent(private val provider : Any) : AutoSyncedComponent {
    private var value = AtomicBoolean(false)
    override fun shouldSyncWith(player: ServerPlayerEntity?): Boolean {
        // we only want to sync this data with the player that owns it
        // this reduces network traffic
        return player == provider
    }

    override fun readFromNbt(tag: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup) {
        setValue(tag.getBoolean(booleanValue))
    }

    override fun writeToNbt(tag: NbtCompound, registryLookup: RegistryWrapper.WrapperLookup) {
        tag.putBoolean(booleanValue, getValue())
    }

    fun setValue(value: Boolean) {
        this.value.set(value)
        ModComponents.SELECTION_MODE.sync(provider)
    }

    fun getValue(): Boolean {
        return this.value.get()
    }

    /* ****** helper functions for setting the selection direction ****** */
    fun setVertical() {
        setValue(true)
    }

    fun setHorizontal() {
        setValue(false)
    }

    fun toggle() {
        setValue(!getValue())
    }
}