package com.godlike.common.items

import com.godlike.common.components.ModComponents
import com.godlike.common.vs2.Vs2Util
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

class DevWand : Item(Properties()) {
    override fun use(world: Level, user: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        if (user.isShiftKeyDown) {
            val cursors = ModComponents.CURSORS.get(user).getPositions()

            if (!world.isClientSide) {
                Vs2Util.createShip(cursors, world as ServerLevel)
            }

            ModComponents.CURSORS.get(user).clearPositions()
            ModComponents.CURSOR_ANCHORS.get(user).clearPositions()
        }
        return super.use(world, user, hand)
    }
}