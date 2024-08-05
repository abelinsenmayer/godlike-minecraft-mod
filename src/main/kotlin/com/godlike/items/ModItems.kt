package com.godlike.items

import com.godlike.MOD_ID
import net.minecraft.item.Item
import net.minecraft.registry.Registries
import net.minecraft.registry.Registry
import net.minecraft.util.Identifier
import org.slf4j.LoggerFactory

class ModItems {
    companion object {
        private val logger = LoggerFactory.getLogger("godlike")

        init {
            logger.info("Registering items")
        }

        private fun register(item: Item, id: String) : Item {
            val itemID = Identifier.of(MOD_ID, id)
            return Registry.register(Registries.ITEM, itemID, item)
        }

        // All items should be registered here
        val DEV_WAND = register(DevWand(), "dev_wand")
    }
}