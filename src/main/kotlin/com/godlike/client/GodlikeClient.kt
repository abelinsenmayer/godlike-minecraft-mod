package com.godlike.client

import com.godlike.client.ClientTickHandler
import com.godlike.client.keybind.ModKeybinds
import com.godlike.client.networking.ModClientNetworking
import com.godlike.client.render.ControlsHudOverlay
import net.fabricmc.api.ClientModInitializer
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback

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
	}
}