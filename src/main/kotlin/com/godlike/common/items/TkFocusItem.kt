package com.godlike.common.items

import com.godlike.common.Godlike.logger
import com.godlike.common.components.Mode
import com.godlike.common.components.getMode
import com.godlike.common.components.setMode
import io.wispforest.owo.itemgroup.OwoItemSettings
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.UseAnim

class TkFocusItem(
    val tier: TkFocusTier
) : Item(
    OwoItemSettings()
        .group(ModItems.GODLIKE_GROUP)
        .fireResistant()
        .stacksTo(1)
) {
    override fun getUseAnimation(stack: ItemStack): UseAnim {
         return UseAnim.BOW
    }

    override fun canBeDepleted(): Boolean {
        return false
    }
}

/**
 * Update the player's tk state based on the item they're holding.
 * When the player equips a telekinetic focus, switch to TK mode. When they unequip it, switch back to normal mode.
 */
fun ServerPlayer.updateTkStateByItem(item: ItemStack) {
    if (item.item is TkFocusItem && this.getMode() != Mode.TELEKINESIS) {
        this.setMode(Mode.TELEKINESIS)
        logger.info("Switching to TK mode")
    } else if (item.item !is TkFocusItem && this.getMode() == Mode.TELEKINESIS) {
        this.setMode(Mode.NONE)
        logger.info("Switching to normal mode")
    }
}