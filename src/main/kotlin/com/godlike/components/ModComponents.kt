package com.godlike.components

import com.godlike.MOD_ID
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Identifier
import org.ladysnake.cca.api.v3.component.ComponentKey
import org.ladysnake.cca.api.v3.component.ComponentRegistry
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer

class ModComponents : EntityComponentInitializer {
    companion object {
        @JvmField
        val CURSORS: ComponentKey<BlockPosListComponent> = ComponentRegistry.getOrCreate(Identifier.of(MOD_ID, "cursors"), BlockPosListComponent::class.java)
    }

    override fun registerEntityComponentFactories(registry: EntityComponentFactoryRegistry) {
        registry.registerFor(PlayerEntity::class.java, CURSORS) { e: PlayerEntity -> BlockPosListComponent(e) }
    }
}