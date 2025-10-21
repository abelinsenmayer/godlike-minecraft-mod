package com.godlike.common

import com.godlike.common.components.ModEntityComponents
import com.godlike.common.components.ModWorldComponents
import com.godlike.common.items.ModItems
import com.godlike.common.networking.ModNetworking
import net.fabricmc.api.ModInitializer
import org.slf4j.Logger
import org.slf4j.LoggerFactory

const val MOD_ID = "godlike"

object Godlike : ModInitializer {
	@JvmField
    val logger: Logger = LoggerFactory.getLogger("godlike")

	override fun onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		logger.info("Hello Fabric world!")

		// Register items
		ModItems

		// Register components
		ModEntityComponents()
		ModWorldComponents()

		// Initialize networking
		ModNetworking.register()

		// Register events
		ModEvents

		// Start server tick handler
		ServerTickHandler.start()
	}
}