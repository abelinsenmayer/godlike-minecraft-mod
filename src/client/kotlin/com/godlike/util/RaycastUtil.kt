package com.godlike.util

import com.godlike.Godlike.logger
import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.core.Vec3i
import net.minecraft.world.level.ClipContext
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3
import javax.sound.sampled.Clip

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