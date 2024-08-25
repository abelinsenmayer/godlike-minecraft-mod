package com.godlike.common.components

import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerPlayer
import java.util.concurrent.atomic.AtomicBoolean

const val booleanValue = "boolean-value"

class BooleanComponent(private val provider : Any) : AutoSyncedComponent {
    private var value = AtomicBoolean(false)
    override fun shouldSyncWith(player: ServerPlayer?): Boolean {
        // we only want to sync this data with the player that owns it
        // this reduces network traffic
        return player == provider
    }

    override fun readFromNbt(tag: CompoundTag) {
        setValue(tag.getBoolean(booleanValue))
    }

    override fun writeToNbt(tag: CompoundTag) {
        tag.putBoolean(booleanValue, getValue())
    }

    fun setValue(value: Boolean) {
        this.value.set(value)
        ModComponents.SELECTION_MODE.sync(provider)
    }

    fun getValue(): Boolean {
        return this.value.get()
    }

    fun toggle() {
        setValue(!getValue())
    }
}