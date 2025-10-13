package com.godlike.client

import com.godlike.TrinketRendersInitializer
import com.godlike.client.keybind.ModKeybinds
import com.godlike.client.networking.ModClientNetworking
import com.godlike.client.render.ControlsHudOverlay
import com.godlike.common.Godlike
import com.godlike.common.MOD_ID
import com.godlike.common.components.telekinesis
import com.godlike.common.items.ModItems
import com.godlike.common.items.ModModelPredicates
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.fabricmc.fabric.api.`object`.builder.v1.client.model.FabricModelPredicateProviderRegistry
import net.minecraft.resources.ResourceLocation
import net.minecraft.server.level.ServerPlayer

object GodlikeClient : ClientModInitializer {

	override fun onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.

		// Register keybinds
		ModKeybinds.registerKeybinds()

		// Start client ticking
		ClientTickHandler.start()

		// Register client networking
		ModClientNetworking.register()

		// Register HUD
		HudRenderCallback.EVENT.register(ControlsHudOverlay())

		// Register model predicates
		ModModelPredicates

		// Register trinket renderers
		TrinketRendersInitializer
	}
}