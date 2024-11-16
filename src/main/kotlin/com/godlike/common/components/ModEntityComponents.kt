package com.godlike.common.components

import com.godlike.common.Godlike.logger
import com.godlike.common.MOD_ID
import dev.onyxstudios.cca.api.v3.component.ComponentKey
import dev.onyxstudios.cca.api.v3.component.ComponentRegistry
import dev.onyxstudios.cca.api.v3.entity.EntityComponentFactoryRegistry
import dev.onyxstudios.cca.api.v3.entity.EntityComponentInitializer
import net.minecraft.client.player.LocalPlayer
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.player.Player

class ModEntityComponents : EntityComponentInitializer {
    companion object {
        @JvmField
        val CURSORS: ComponentKey<BlockPosListComponent> = ComponentRegistry.getOrCreate(ResourceLocation.tryBuild(
            MOD_ID, "cursors")!!, BlockPosListComponent::class.java)
        @JvmField
        val CURSOR_PREVIEWS: ComponentKey<BlockPosListComponent> = ComponentRegistry.getOrCreate(ResourceLocation.tryBuild(
            MOD_ID, "cursor_previews")!!, BlockPosListComponent::class.java)
        @JvmField
        val TARGET_POSITION: ComponentKey<BlockPosComponent> = ComponentRegistry.getOrCreate(ResourceLocation.tryBuild(
            MOD_ID, "target_pos")!!, BlockPosComponent::class.java)

        @JvmField
        val TELEKINESIS_DATA: ComponentKey<TelekinesisComponent> = ComponentRegistry.getOrCreate(ResourceLocation.tryBuild(
            MOD_ID, "telekinesis_data")!!, TelekinesisComponent::class.java)

        @JvmField
        val MODE: ComponentKey<ModeComponent> = ComponentRegistry.getOrCreate(ResourceLocation.tryBuild(
            MOD_ID, "mode")!!, ModeComponent::class.java)
        @JvmField
        val SELECTION_DATA: ComponentKey<SelectionComponent> = ComponentRegistry.getOrCreate(ResourceLocation.tryBuild(
            MOD_ID, "selection_data")!!, SelectionComponent::class.java)

        val CURSOR_COMPONENT_TYPES = listOf(CURSORS, CURSOR_PREVIEWS)
    }

    override fun registerEntityComponentFactories(registry: EntityComponentFactoryRegistry) {
        logger.info("Registering entity component factories")

        // Common components
        registry.registerFor(Player::class.java, CURSORS) { e: Player -> BlockPosListComponent(e) }
        registry.registerFor(Player::class.java, TARGET_POSITION) { e: Player -> BlockPosComponent(e) }
        registry.registerFor(Player::class.java, CURSOR_PREVIEWS) { e: Player -> BlockPosListComponent(e) }
        registry.registerFor(Player::class.java, MODE) { e: Player -> ModeComponent(e) }
        registry.registerFor(Player::class.java, TELEKINESIS_DATA) { e: Player -> TelekinesisComponent(e) }

        // Client-only components
        registry.registerFor(LocalPlayer::class.java, SELECTION_DATA) { e: LocalPlayer -> SelectionComponent(e) }
    }
}