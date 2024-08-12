package com.godlike.util

import net.minecraft.client.MinecraftClient
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.RaycastContext

fun blockRaycastFromPlayer(): BlockPos {
    val client = MinecraftClient.getInstance()
    val cameraEntity = MinecraftClient.getInstance().cameraEntity!!
    val vec3d: Vec3d = cameraEntity.getCameraPosVec(1.0f)
    val vec3d2: Vec3d = cameraEntity.getRotationVec(1.0f)
    val vec3d3 = vec3d.add(vec3d2.x * MAX_RAYCAST_DISTANCE, vec3d2.y * MAX_RAYCAST_DISTANCE, vec3d2.z * MAX_RAYCAST_DISTANCE)
    val hitResult = cameraEntity.world.raycast(
        RaycastContext(
            vec3d,
            vec3d3,
            RaycastContext.ShapeType.COLLIDER,
            RaycastContext.FluidHandling.ANY,
            cameraEntity
        )
    )
    // get the block position the player is looking at
    val pos = hitResult.pos
    var blockPos = BlockPos(pos.x.toInt(), pos.y.toInt(), pos.z.toInt())
    // if the position is air, we need to offset it in the direction we're looking to get the solid block
    if (client.world!!.getBlockState(blockPos).isAir) {
        blockPos = blockPos.offset(hitResult.side.opposite)
    }
    return blockPos;
}