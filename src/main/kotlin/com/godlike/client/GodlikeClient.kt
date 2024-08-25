package com.godlike.client

import com.godlike.client.ClientTickHandler
import com.godlike.client.keybind.ModKeybinds
import net.fabricmc.api.ClientModInitializer

object GodlikeClient : ClientModInitializer {

	override fun onInitializeClient() {
		// This entrypoint is suitable for setting up client-specific logic, such as rendering.

		// Register keybinds
		ModKeybinds.registerKeybinds()

		// Start client ticking
		ClientTickHandler.start()
	}
}