package com.godlike.client.util

import com.godlike.common.components.ModComponents
import com.godlike.common.components.selection
import com.godlike.common.util.*
import net.minecraft.client.Minecraft
import net.minecraft.core.BlockPos
import net.minecraft.core.Vec3i
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.projectile.ProjectileUtil
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult

fun selectRaycastTarget() {
    val client = Minecraft.getInstance()
    val player = client.player!!
    val camera = Minecraft.getInstance().cameraEntity!!

    val selection = player.selection()

    val hit = ProjectileUtil.getHitResultOnViewVector(camera, { _: Entity -> true }, MAX_RAYCAST_DISTANCE)
    when (hit.type) {
        HitResult.Type.BLOCK -> {
            selection.setSingleTarget((hit as BlockHitResult).blockPos)
        }
        HitResult.Type.ENTITY -> {
            selection.setSingleTarget((hit as EntityHitResult).entity)
        }
        else -> {
            // NOOP
        }
    }
}

/**
 * Called every tick on the client side when the player is in selection mode.
 * Updates the player's selection based on where they're looking and what they have already selected.
 */
fun showSelectionPreview(client: Minecraft) {
    val player = client.player!!
    val anchors = ModComponents.CURSOR_ANCHORS.get(player).getPositions()

    var targetPos : Vec3i = blockRaycastFromPlayer()
    ModComponents.TARGET_POSITION.get(player).setPos(BlockPos(targetPos))

    val camera = client.cameraEntity!!
    val cameraRotationVec = camera.getViewVector(1.0f)

    when (anchors.size) {
        0 -> {
            // no anchors, so we find the place the player is looking at
            targetPos = blockRaycastFromPlayer()
            ModComponents.TARGET_POSITION.get(player).setPos(targetPos)
        }
        1 -> {
            // one anchor, so we highlight the plane of blocks between the last anchor and where the player is looking
            val plane: List<Vec3i>
            if (ModComponents.SELECTING_VERTICAL.get(player).getValue()) {
                val selectingFar = ModComponents.SELECTING_FAR.get(player).getValue()
                targetPos = vecIntersectWithXOrZ(camera.position(), cameraRotationVec, anchors.last().toVec3(), selectingFar).toVec3i()
                ModComponents.TARGET_POSITION.get(player).setPos(BlockPos(targetPos))
                plane = getVerticalPlaneBetween(anchors.last(), targetPos)
            } else {
                targetPos = vecIntersectWithY(camera.position(), cameraRotationVec, anchors.last().y)
                ModComponents.TARGET_POSITION.get(player).setPos(BlockPos(targetPos))
                plane = getHorizontalPlaneBetween(anchors.last(), targetPos)
            }
            ModComponents.CURSOR_PREVIEWS.get(player).clearPositions()
            ModComponents.CURSOR_PREVIEWS.get(player).addAllPositions(plane.map { BlockPos(it) })
        }
        else -> {
            // multiple anchors, so we highlight the volume of blocks between the last anchor and where the player is looking
            val volume: List<BlockPos>
            if (ModComponents.SELECTING_VERTICAL.get(player).getValue()) {
                val selectingFar = ModComponents.SELECTING_FAR.get(player).getValue()
                targetPos = vecIntersectWithXOrZ(camera.position(), cameraRotationVec, anchors.last().toVec3(), selectingFar).toVec3i()
                // we only want to extend the selection on the y-axis, so we clamp the x and z values to the last anchor
                targetPos = Vec3i(anchors.last().x, targetPos.y, anchors.last().z)
                ModComponents.TARGET_POSITION.get(player).setPos(BlockPos(targetPos))
                volume = getVolumeBetween(anchors, targetPos).map { BlockPos(it) }
            } else {
                targetPos = vecIntersectWithY(camera.position(), cameraRotationVec, anchors.last().y)
                ModComponents.TARGET_POSITION.get(player).setPos(BlockPos(targetPos))
                volume = getVolumeBetween(anchors, targetPos).map { BlockPos(it) }
            }
            ModComponents.CURSOR_PREVIEWS.get(player).clearPositions()
            ModComponents.CURSOR_PREVIEWS.get(player).addAllPositions(volume)
        }
    }
}

