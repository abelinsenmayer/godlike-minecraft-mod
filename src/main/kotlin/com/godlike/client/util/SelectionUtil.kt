package com.godlike.client.util

import com.godlike.common.components.ModComponents
import com.godlike.common.components.selection
import com.godlike.common.util.*
import com.godlike.common.vs2.Vs2Util
import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer
import net.minecraft.core.BlockPos
import net.minecraft.core.Vec3i
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.projectile.ProjectileUtil
import net.minecraft.world.phys.BlockHitResult
import net.minecraft.world.phys.EntityHitResult
import net.minecraft.world.phys.HitResult

/**
 * Selects the block, entity, or ship the player is looking at.
 * If they select a block, updates selection based on the block's position.
 */
fun selectRaycastTarget() {
    val client = Minecraft.getInstance()
    val player = client.player!!
    val camera = Minecraft.getInstance().cameraEntity!!

    val selection = player.selection()

    val hit = ProjectileUtil.getHitResultOnViewVector(camera, { _: Entity -> true }, MAX_RAYCAST_DISTANCE)
    when (hit.type) {
        HitResult.Type.BLOCK -> {
            // If this block is in a ship, we want to select the ship instead
            val ship = Vs2Util.getClientShipManagingPos(player.clientLevel, (hit as BlockHitResult).blockPos)
            if (ship != null) {
                selection.setSingleTarget(ship)
            } else {
                selection.setSingleTarget(hit.blockPos)
                if (selection.dfsDepth > 0) {
                    player.updatePreviewsFromPosition(hit.blockPos)
                }
            }
        }
        HitResult.Type.ENTITY -> {
            selection.setSingleTarget((hit as EntityHitResult).entity)
        }
        else -> {
            selection.clear()
        }
    }
}

/**
 * Returns true if the block the player is looking at is contiguous with the current selection.
 * Also returns true if they have no selection. Returns false if there is no selected block.
 */
fun LocalPlayer.isTargetContiguousWithSelection() : Boolean {
    val selection = this.selection()
    if (selection.selectedPositions.isEmpty()) {
        return true
    }
    if (selection.cursorTargetBlock == null) {
        return false
    }
    return isPosContiguousWith(selection.cursorTargetBlock!!, selection.selectedPositions)
}

/**
 * Returns true if the given position is contiguous with any of the positions in the given set.
 * TODO consider implementing this using dfs.
 */
fun isPosContiguousWith(pos: BlockPos, set: Set<BlockPos>) : Boolean {
    set.forEach { selectedPos ->
        if (pos.distManhattan(selectedPos) == 1) {
            return true
        }
    }
    return false
}

fun LocalPlayer.updatePreviewsFromPosition(pos: Vec3i) {
    val searchCondition = { position: Vec3i -> !this.clientLevel.getBlockState(BlockPos(position)).isAir &&
            !this.selection().selectedPositions.contains(position) }
    val found = blockPosDfs(pos, this.selection().dfsDepth, false, searchCondition)
    this.selection().previewPositions.clear()
    this.selection().previewPositions.addAll(found)
}

fun blockPosDfs(startPos: Vec3i, range: Int, manhattan: Boolean, condition: (Vec3i) -> Boolean) : Set<BlockPos> {
    return dfs(startPos, startPos, range, manhattan, mutableSetOf(), condition).map { BlockPos(it) }.toSet()
}

fun dfs(pos: Vec3i, origin: Vec3i, range: Int, manhattan: Boolean, visited: MutableSet<Vec3i>, condition: (Vec3i) -> Boolean) : Set<Vec3i> {
    val distance = if (manhattan) pos.distManhattan(origin).toDouble() else pos.distSqr(origin)
    if (distance >= range) {
        return visited
    }
    if (condition(pos)) {
        visited.add(pos)
    }
    pos.getNeighbors().filter { !visited.contains(it) }.filter { condition(it) }.forEach {
        dfs(it, origin, range, manhattan, visited, condition)
    }
    return visited
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

