package com.godlike.client.util

import com.godlike.common.Godlike.logger
import com.godlike.common.components.selection
import com.godlike.common.util.*
import com.godlike.common.vs2.Vs2Util
import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer
import net.minecraft.core.BlockPos
import net.minecraft.core.Vec3i
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.entity.player.Player
import net.minecraft.world.entity.projectile.ProjectileUtil
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.Blocks
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

fun BlockPos.isValidTkTargetFor(player: Player) : Boolean {
    val state = player.level().getBlockState(this)
    return !(state.isAir
            || state.block == Blocks.BEDROCK
            || state.block == Blocks.BARRIER
            || state.block == Blocks.END_PORTAL_FRAME
            || state.block == Blocks.END_PORTAL
            || state.block == Blocks.END_GATEWAY
            || state.block == Blocks.COMMAND_BLOCK
            || state.block == Blocks.STRUCTURE_BLOCK
            || state.block == Blocks.JIGSAW
            || state.block == Blocks.REPEATING_COMMAND_BLOCK
            || state.block == Blocks.CHAIN_COMMAND_BLOCK
            || state.block == Blocks.NETHER_PORTAL)
}

fun Entity.isValidTkTargetFor(): Boolean {
    return !(this.isSpectator
            || this.type == EntityType.WITHER
            || this.type == EntityType.ENDER_DRAGON
            || this.type == EntityType.END_CRYSTAL
            || this.type == EntityType.TEXT_DISPLAY)
}
