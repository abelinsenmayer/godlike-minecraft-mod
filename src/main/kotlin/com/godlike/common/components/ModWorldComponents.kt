package com.godlike.common.components

import com.godlike.common.MOD_ID
import com.godlike.common.telekinesis.TkTicker
import dev.onyxstudios.cca.api.v3.component.ComponentKey
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry
import dev.onyxstudios.cca.api.v3.world.WorldComponentFactoryRegistry
import dev.onyxstudios.cca.api.v3.world.WorldComponentInitializer
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.level.Level

class ModWorldComponents : WorldComponentInitializer {
    companion object {
        @JvmField
        val WORLD_TK_TICKER: ComponentKey<TkTicker> = ComponentRegistry.getOrCreate(
            ResourceLocation.tryBuild(
                MOD_ID, "world_tk_ticker")!!, TkTicker::class.java)
    }

    override fun registerWorldComponentFactories(registry: WorldComponentFactoryRegistry) {
        registry.register(WORLD_TK_TICKER) { level: Level -> TkTicker(level) }
    }
}

fun Level.getTkTicker(): TkTicker {
    return ModWorldComponents.WORLD_TK_TICKER.get(this)
}