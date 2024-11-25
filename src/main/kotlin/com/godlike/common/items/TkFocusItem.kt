package com.godlike.common.items

import io.wispforest.owo.itemgroup.OwoItemSettings
import net.minecraft.world.item.Item

class TkFocusItem(
    val tier: TkFocusTier
) : Item(
    OwoItemSettings()
        .group(ModItems.GODLIKE_GROUP)
        .fireResistant()
        .stacksTo(1)
) {

}