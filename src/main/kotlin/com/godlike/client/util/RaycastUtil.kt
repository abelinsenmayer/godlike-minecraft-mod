package com.godlike.client.util

import com.godlike.common.util.MAX_RAYCAST_DISTANCE
import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult

fun blockRaycastFromPlayer(): BlockPos {
    val camera = Minecraft.getInstance().cameraEntity!!
    val hit = camera.pick(MAX_RAYCAST_DISTANCE, 1.0f, true)
    return if (hit.type == HitResult.Type.BLOCK) {
        (hit as BlockHitResult).blockPos
    } else {
        // TODO we don't want to return BlockPos.ZERO, but we need to handle this case
        BlockPos.ZERO
    }

//    // if the position is air, we need to offset it in the direction we're looking to get the solid block
//    if (client.level!!.getBlockState(blockPos).isAir) {
//        val step = hitResult.direction.opposite.step()
//        blockPos = blockPos.offset(Vec3i(step.x.toInt(), step.y.toInt(), step.z.toInt()))
//    }
//    return blockPos;
}