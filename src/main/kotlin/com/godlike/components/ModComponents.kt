package com.godlike.components

import com.godlike.MOD_ID
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.util.Identifier
import org.ladysnake.cca.api.v3.component.ComponentKey
import org.ladysnake.cca.api.v3.component.ComponentRegistry
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer
import org.slf4j.LoggerFactory

class ModComponents : EntityComponentInitializer {
    companion object {
        private val logger = LoggerFactory.getLogger("godlike")

        @JvmField
        val CURSORS: ComponentKey<BlockPosListComponent> = ComponentRegistry.getOrCreate(Identifier.of(MOD_ID, "cursors"), BlockPosListComponent::class.java)
        @JvmField
        val CURSOR_PREVIEWS: ComponentKey<BlockPosListComponent> = ComponentRegistry.getOrCreate(Identifier.of(MOD_ID, "cursor_previews"), BlockPosListComponent::class.java)
        @JvmField
        val CURSOR_ANCHORS: ComponentKey<BlockPosListComponent> = ComponentRegistry.getOrCreate(Identifier.of(MOD_ID, "cursor_anchors"), BlockPosListComponent::class.java)
        @JvmField
        val SELECTION_MODE: ComponentKey<BooleanComponent> = ComponentRegistry.getOrCreate(Identifier.of(MOD_ID, "selection_mode"), BooleanComponent::class.java)
        @JvmField
        val TARGET_POSITION: ComponentKey<BlockPosComponent> = ComponentRegistry.getOrCreate(Identifier.of(MOD_ID, "target_pos"), BlockPosComponent::class.java)

        val CURSOR_COMPONENT_TYPES = listOf(CURSORS, CURSOR_PREVIEWS, CURSOR_ANCHORS)
    }

    override fun registerEntityComponentFactories(registry: EntityComponentFactoryRegistry) {
        logger.info("Registering entity component factories")

        registry.registerFor(PlayerEntity::class.java, CURSORS) { e: PlayerEntity -> BlockPosListComponent(e) }
        registry.registerFor(PlayerEntity::class.java, SELECTION_MODE) { e: PlayerEntity -> BooleanComponent(e) }
        registry.registerFor(PlayerEntity::class.java, CURSOR_ANCHORS) { e: PlayerEntity -> BlockPosListComponent(e) }
        registry.registerFor(PlayerEntity::class.java, TARGET_POSITION) { e: PlayerEntity -> BlockPosComponent(e) }
        registry.registerFor(PlayerEntity::class.java, CURSOR_PREVIEWS) { e: PlayerEntity -> BlockPosListComponent(e) }
    }
}