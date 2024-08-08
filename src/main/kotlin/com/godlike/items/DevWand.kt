package com.godlike.items

import com.godlike.components.ModComponents
import net.minecraft.block.Blocks
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.item.ItemUsageContext
import net.minecraft.util.ActionResult
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World
import org.slf4j.LoggerFactory

class DevWand : Item(Settings()) {
    override fun use(world: World, user: PlayerEntity, hand: Hand?): TypedActionResult<ItemStack> {
        if (user.isSneaking) {
            val cursors = ModComponents.CURSORS.get(user).getPositions()
            if (!world.isClient) {
                for (cursor in cursors) {
                    world.setBlockState(cursor, Blocks.DIAMOND_BLOCK.defaultState)
                    ModComponents.CURSORS.get(user).clearPositions()
                    ModComponents.CURSOR_ANCHORS.get(user).clearPositions()
                }
            }
        }
        return super.use(world, user, hand)
    }

    override fun useOnBlock(context: ItemUsageContext): ActionResult {
        if (!context.world.isClient) {
            context.player?.let {
                if (!it.isSneaking) {
                    ModComponents.CURSORS.get(it).addPosition(context.blockPos)
                }
            }
        }
        return super.useOnBlock(context)
    }
}