package com.godlike.common.items

import com.godlike.common.Godlike.logger
import com.godlike.common.components.ModComponents
import com.godlike.common.vs2.Vs2Util
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResult
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.item.context.UseOnContext
import net.minecraft.world.level.Level

class DevWand : Item(Properties()) {
    override fun use(world: Level, user: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
//        if (world.isClientSide) {
//            user.push(0.0, 10.0, 0.0)
//        }
        return super.use(world, user, hand)
    }

    override fun interactLivingEntity(
        stack: ItemStack,
        player: Player,
        interactionTarget: LivingEntity,
        usedHand: InteractionHand
    ): InteractionResult {
        logger.info("Interacting with entity: $interactionTarget")
        if (!player.level().isClientSide) {
            interactionTarget.push(0.0, 1.0, 0.0)
        }

        return super.interactLivingEntity(stack, player, interactionTarget, usedHand)
    }
}