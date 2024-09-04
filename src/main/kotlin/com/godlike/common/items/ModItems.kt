package com.godlike.common.items

import com.godlike.common.MOD_ID
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import org.slf4j.LoggerFactory

class ModItems {
    companion object {
        private val logger = LoggerFactory.getLogger("godlike")

        init {
            logger.info("Registering items")
        }

        private fun register(item: Item, id: String) : Item {
            val itemID = ResourceLocation.tryBuild(MOD_ID, id)!!
            return Items.registerItem(itemID, item)
        }

        // All items should be registered here
        val DEV_WAND = register(DevWand(), "dev_wand")
    }
}