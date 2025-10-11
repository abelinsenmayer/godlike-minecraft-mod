package com.godlike.common.items

import com.godlike.common.components.*
import dev.emi.trinkets.api.SlotReference
import dev.emi.trinkets.api.TrinketItem
import io.wispforest.owo.itemgroup.OwoItemSettings
import net.minecraft.ChatFormatting
import net.minecraft.network.chat.Component
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.TooltipFlag
import net.minecraft.world.level.Level

class KineticCoreItem(
    override val tier: TkFocusTier
) : TrinketItem(
    OwoItemSettings()
        .group(ModItems.GODLIKE_GROUP)
        .fireResistant()
        .stacksTo(1)
), TieredTkItem {
    override fun getName(stack: ItemStack): Component {
        return Component.literal("${tier.name.toLowerCase().capitalize()} Telekinetic Core")
            .withStyle(tier.getTextColor(), ChatFormatting.ITALIC)
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