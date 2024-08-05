package com.godlike

import com.godlike.components.ModComponents
import com.godlike.items.ModItems
import net.fabricmc.api.ModInitializer
import org.slf4j.LoggerFactory

const val MOD_ID = "godlike"

object Godlike : ModInitializer {
    private val logger = LoggerFactory.getLogger("godlike")

	override fun onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		logger.info("Hello Fabric world!")

		// Register items
		ModItems()

		// Register components
		ModComponents()
	}
}