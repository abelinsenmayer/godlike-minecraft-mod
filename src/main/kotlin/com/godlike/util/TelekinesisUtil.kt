package com.godlike.util

import com.godlike.components.ModComponents
import com.godlike.vs2.Vs2PhysicsUtil
import net.minecraft.server.level.ServerPlayer

fun physicsObjectFromSelection(player: ServerPlayer) {
    val cursors = ModComponents.CURSORS.get(player).getPositions()
    val ship = Vs2PhysicsUtil.makePhysicsObjectFromBlocks(cursors, player.serverLevel())
}