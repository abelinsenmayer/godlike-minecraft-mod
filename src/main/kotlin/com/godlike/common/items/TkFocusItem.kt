package com.godlike.common.items

import com.godlike.common.components.Mode
import com.godlike.common.components.getMode
import com.godlike.common.components.setMode
import com.godlike.common.components.telekinesis
import com.godlike.common.networking.ModNetworking.CHANNEL
import com.godlike.common.networking.ResetDfsDepthPacket
import io.wispforest.owo.itemgroup.OwoItemSettings
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level

class TkFocusItem(
    val tier: TkFocusTier
) : Item(
    OwoItemSettings()
        .group(ModItems.GODLIKE_GROUP)
        .fireResistant()
        .stacksTo(1)
) {
    override fun getName(stack: ItemStack): Component {
        val color = when (tier) {
            TkFocusTier.SIMPLE -> ChatFormatting.WHITE
            TkFocusTier.ELEVATED -> ChatFormatting.DARK_GREEN
            TkFocusTier.MAGNIFICENT -> ChatFormatting.AQUA
            TkFocusTier.SUPREME -> ChatFormatting.LIGHT_PURPLE
            TkFocusTier.GODLIKE -> ChatFormatting.DARK_PURPLE
        }
        return Component.literal("${tier.name.toLowerCase().capitalize()} Telekinetic Focus").withStyle(color, ChatFormatting.ITALIC)

    }

    override fun appendHoverText(
        stack: ItemStack,
        level: Level?,
        tooltipComponents: MutableList<Component>,
        isAdvanced: TooltipFlag
    ) {
        tooltipComponents.add(Component.literal("Max radius: ${tier.selectionRadius} blocks").withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY))
        tooltipComponents.add(Component.literal("Range: ${tier.range.toInt()} blocks").withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY))
        tooltipComponents.add(Component.literal("Target max health: ${tier.maxHealth}").withStyle(ChatFormatting.ITALIC, ChatFormatting.GRAY))
    }
}

/**
 * Returns true if the player should animate as though they are controlling a TK object.
 */
fun Player.shouldAnimateTk(): Boolean {
    return this.getMode() == Mode.TELEKINESIS && (this.telekinesis().activeTkTarget != null
            || this.telekinesis().getTkTargets().any { it.chargingLaunch })
}

/**
 * Update the player's tk state based on the item they're holding.
 * When the player equips a telekinetic focus, switch to TK mode. When they unequip it, switch back to normal mode.
 */
fun ServerPlayer.updateTkStateByItem(item: ItemStack) {
    if (item.item is TkFocusItem && this.getMode() != Mode.TELEKINESIS) {
        this.setMode(Mode.TELEKINESIS)
    } else if (item.item !is TkFocusItem && this.getMode() == Mode.TELEKINESIS) {
        this.setMode(Mode.NONE)
    }

    if (item.item is TkFocusItem && (item.item as TkFocusItem).tier != this.telekinesis().tier) {
        // Clear the player's current TK targets if they switch to a focus of a different tier
        this.telekinesis().clearTargets()

        // Apply item's constraints on player's TK abilities
        val focusItem = item.item as TkFocusItem
        this.telekinesis().tier = focusItem.tier
        CHANNEL.serverHandle(this).send(ResetDfsDepthPacket())
    }
}