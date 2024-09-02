package com.godlike.client.util

import com.godlike.common.components.ModComponents
import net.minecraft.client.Minecraft

/**
 * Called every tick on the client side when the player is in selection mode.
 * Updates the player's selection based on where they're looking and what they have already selected.
 */
fun showSelectionPreview(client: Minecraft) {
    val player = client.player!!
    val anchors = ModComponents.CURSOR_ANCHORS.get(player).getPositions()

    val targetPos = blockRaycastFromPlayer()
    ModComponents.TARGET_POSITION.get(player).setPos(targetPos)

//    when (anchors.size) {
//        0 -> {
//            // no anchors, so we find the place the player is looking at
//            val targetPos = blockRaycastFromPlayer()
//            ModComponents.TARGET_POSITION.get(player).setPos(targetPos)
//        }
//        1 -> {
//            // one anchor, so we highlight the plane of blocks between the last anchor and where the player is looking
//            val camera = client.cameraEntity!!
//            val cameraRotationVec = camera.getViewVector(1.0f)
//            val cameraPositionVec = camera.getViewVector(1.0f)
//            val plane: List<Vec3i>
//
//            if (ModComponents.SELECTING_VERTICAL.get(player).getValue()) {
//                val selectingFar = ModComponents.SELECTING_FAR.get(player).getValue()
//                val targetPos = vecIntersectWithXOrZ(cameraPositionVec, cameraRotationVec, anchors.last(), selectingFar)
//                ModComponents.TARGET_POSITION.get(player).setPos(BlockPos(targetPos))
//                plane = getVerticalPlaneBetween(anchors.last(), targetPos)
//            } else {
//                val targetPos = vecIntersectWithY(cameraPositionVec, cameraRotationVec, anchors.last().y)
//                ModComponents.TARGET_POSITION.get(player).setPos(BlockPos(targetPos))
//                plane = getHorizontalPlaneBetween(anchors.last(), targetPos)
//            }
//            ModComponents.CURSOR_PREVIEWS.get(player).clearPositions()
//            ModComponents.CURSOR_PREVIEWS.get(player).addAllPositions(plane.map { BlockPos(it) })
//        }
//        else -> {
//            // multiple anchors, so we highlight the volume of blocks between the last anchor and where the player is looking
//            val camera = client.cameraEntity!!
//            val cameraRotationVec = camera.getViewVector(1.0f)
//            val cameraPositionVec = camera.getViewVector(1.0f)
//            val volume: List<BlockPos>
//
//            if (ModComponents.SELECTING_VERTICAL.get(player).getValue()) {
//                val selectingFar = ModComponents.SELECTING_FAR.get(player).getValue()
//                var targetPos = vecIntersectWithXOrZ(cameraPositionVec, cameraRotationVec, anchors.last(), selectingFar)
//                // we only want to extend the selection on the y-axis, so we clamp the x and z values to the last anchor
//                targetPos = Vec3i(anchors.last().x, targetPos.y, anchors.last().z)
//                ModComponents.TARGET_POSITION.get(player).setPos(BlockPos(targetPos))
//                volume = getVolumeBetween(anchors, targetPos).map { BlockPos(it) }
//            } else {
//                val targetPos = vecIntersectWithY(cameraPositionVec, cameraRotationVec, anchors.last().y)
//                ModComponents.TARGET_POSITION.get(player).setPos(BlockPos(targetPos))
//                volume = getVolumeBetween(anchors, targetPos).map { BlockPos(it) }
//            }
//            ModComponents.CURSOR_PREVIEWS.get(player).clearPositions()
//            ModComponents.CURSOR_PREVIEWS.get(player).addAllPositions(volume)
//        }
//    }
}

