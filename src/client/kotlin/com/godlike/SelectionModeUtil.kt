package com.godlike

import com.godlike.components.ModComponents
import net.minecraft.client.MinecraftClient
import net.minecraft.util.hit.HitResult
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.RaycastContext
import net.minecraft.world.RaycastContext.FluidHandling
import net.minecraft.world.RaycastContext.ShapeType

const val MAX_RAYCAST_DISTANCE = 40.0

/**
 * Called every tick on the client side when the player is in selection mode.
 * It displays a preview of the selection the player is making.
 */
fun showSelectionPreview(client: MinecraftClient) {
    // TODO: instead of making a raycast, find the block the player is looking at based on their existing selection.
    // TODO: this way it can go through walls...

    // TODO extract raycast logic to a separate function

//    val hitResult = client.cameraEntity!!.raycast(MAX_RAYCAST_DISTANCE, 1.0f, true)
    val cameraEntity = client.cameraEntity!!
    val vec3d: Vec3d = cameraEntity.getCameraPosVec(1.0f)
    val vec3d2: Vec3d = cameraEntity.getRotationVec(1.0f)
    val vec3d3 = vec3d.add(vec3d2.x * MAX_RAYCAST_DISTANCE, vec3d2.y * MAX_RAYCAST_DISTANCE, vec3d2.z * MAX_RAYCAST_DISTANCE)
    val hitResult = cameraEntity.world.raycast(
        RaycastContext(
            vec3d,
            vec3d3,
            ShapeType.COLLIDER,
            FluidHandling.ANY,
            cameraEntity
        )
    )

    val player = client.player!!
    if (hitResult.type != HitResult.Type.BLOCK) {
        return
    }
    // get the block position the player is looking at
    val pos = hitResult.pos
    var blockPos = BlockPos(pos.x.toInt()-1, pos.y.toInt(), pos.z.toInt()-1)
    // if the position is air, we need to offset it in the direction we're looking to get the solid block
    if (client.world!!.getBlockState(blockPos).isAir) {
        blockPos = blockPos.offset(hitResult.side.opposite)
    }

    // set the looked-at block as the player's targeted position
    ModComponents.TARGET_POSITION.get(player).setPos(blockPos)

    val anchors = ModComponents.CURSOR_ANCHORS.get(player).getPositions()
    if (anchors.isNotEmpty()) {
        // highlight the plane of blocks between the existing anchors and the target position
        val plane = expandHorizontalPlaneTo(ModComponents.CURSOR_ANCHORS.get(player).getPositions(), blockPos)
        ModComponents.CURSOR_PREVIEWS.get(player).clearPositions()
        ModComponents.CURSOR_PREVIEWS.get(player).addAllPositions(plane)
    }
}

/**
 * Expands the horizontal plane of blocks between the given anchors to include the target position.
 */
fun expandHorizontalPlaneTo(anchors: List<BlockPos>, target: BlockPos): List<BlockPos> {
    val positions = mutableListOf<BlockPos>()
    for (anchor in anchors) {
        val plane = getHorizontalPlaneBetween(anchor, target)
        positions.addAll(plane)
    }
    return positions
}

/**
 * Gets the horizontal plane of blocks with opposite corners at the given positions.
 * The plane is at the y coordinate of pos1.
 */
fun getHorizontalPlaneBetween(pos1: BlockPos, pos2: BlockPos): List<BlockPos> {
    val minX = minOf(pos1.x, pos2.x)
    val maxX = maxOf(pos1.x, pos2.x)
    val minZ = minOf(pos1.z, pos2.z)
    val maxZ = maxOf(pos1.z, pos2.z)

    val positions = mutableListOf<BlockPos>()
    for (x in minX..maxX) {
        for (z in minZ..maxZ) {
            positions.add(BlockPos(x, pos1.y, z))
        }
    }
    return positions
}