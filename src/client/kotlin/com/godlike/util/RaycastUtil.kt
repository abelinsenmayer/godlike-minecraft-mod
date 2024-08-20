package com.godlike.util

import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.core.Vec3i
import net.minecraft.world.level.ClipContext
import net.minecraft.world.phys.Vec3

fun blockRaycastFromPlayer(): BlockPos {
    val client = Minecraft.getInstance()
    val cameraEntity = client.cameraEntity!!
    val vec3d: Vec3 = cameraEntity.getViewVector(1.0f)
    val vec3d2: Vec3 = cameraEntity.getViewVector(1.0f)
    val vec3d3 = vec3d.add(vec3d2.x * MAX_RAYCAST_DISTANCE, vec3d2.y * MAX_RAYCAST_DISTANCE, vec3d2.z * MAX_RAYCAST_DISTANCE)


    val hitResult = cameraEntity.level().clip(
        ClipContext(
            vec3d,
            vec3d3,
            ClipContext.Block.COLLIDER,
            ClipContext.Fluid.ANY,
            cameraEntity
        )
    )
    // get the block position the player is looking at
    val pos = hitResult.blockPos
    var blockPos = BlockPos(pos.x, pos.y, pos.z)
    // if the position is air, we need to offset it in the direction we're looking to get the solid block
    if (client.level!!.getBlockState(blockPos).isAir) {
        val step = hitResult.direction.opposite.step()
        blockPos = blockPos.offset(Vec3i(step.x.toInt(), step.y.toInt(), step.z.toInt()))
    }
    return blockPos;
}