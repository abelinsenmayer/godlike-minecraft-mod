package com.godlike.common.items

import com.godlike.common.MOD_ID
import io.wispforest.owo.itemgroup.Icon
import io.wispforest.owo.itemgroup.OwoItemGroup
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.item.Item
import net.minecraft.world.item.Items
import org.slf4j.LoggerFactory
import java.util.function.Supplier

object ModItems {
    private val logger = LoggerFactory.getLogger("godlike")

    private fun register(item: Item, id: String) : Item {
        val itemID = ResourceLocation.tryBuild(MOD_ID, id)!!
        return Items.registerItem(itemID, item)
    }

    // Item groups
    val GODLIKE_GROUP: OwoItemGroup = OwoItemGroup.builder(
        ResourceLocation(MOD_ID, "godlike_group"),
        Supplier<Icon> { Icon.of(Items.BOW.defaultInstance) }
    ).build()

    // All items should be registered here
    val DEV_WAND = register(DevWand(), "dev_wand")
    val BASIC_TK_FOCUS = register(TkFocusItem(TkFocusTier.BASIC), "basic_tk_focus")

    init {
        logger.info("Registering items")
        GODLIKE_GROUP.initialize()
    }
}