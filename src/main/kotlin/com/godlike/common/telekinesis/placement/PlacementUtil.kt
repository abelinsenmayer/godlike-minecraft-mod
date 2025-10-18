package com.godlike.common.telekinesis.placement

import com.godlike.common.components.telekinesis
import com.godlike.common.util.toVec3
import com.godlike.common.util.toVec3i
import com.godlike.common.util.toVector3d
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.Vec3i
import net.minecraft.world.entity.player.Player
import net.minecraft.world.level.Level
import net.minecraft.world.level.block.state.BlockState
import net.minecraft.world.phys.Vec3
import org.joml.primitives.AABBic
import org.valkyrienskies.core.api.ships.Ship

const val PLACEMENT_MAX_DISTANCE_FROM_SHIP_MULTIPLIER = 5.0

/**
 * Rotate the vector to reorient the given position based on the top and front directions of an object.
 * Used for transforming block positions in a larger structure so that the entire structure ends up rotated.
 */
fun transformVectorUsingPlacementDirection(
    topDirection: Direction,
    frontDirection: Direction,
    vec: Vec3
): Vec3 {
    val top = topDirection.normal.toVec3()
    val front = frontDirection.normal.toVec3()

    // Use right-handed basis: right = top × front
    val right = top.cross(front).normalize()

    val v1 = right.toVector3d().mul(vec.x)
    val v2 = top.toVector3d().mul(vec.y)
    val v3 = front.toVector3d().mul(vec.z)
    val result = v1.add(v2).add(v3)
    return result.toVec3()
}

enum class Direction2D {
    UP,
    DOWN,
    LEFT,
    RIGHT
}

fun Direction.upRelativeTo(relativeTo: Direction) : Direction {
    return when (this) {
        Direction.UP -> relativeTo
        Direction.DOWN -> relativeTo.opposite
        relativeTo -> Direction.DOWN
        relativeTo.opposite -> Direction.UP
        else -> this
    }
}

fun Direction.downRelativeTo(relativeTo: Direction) : Direction {
    return when (this) {
        Direction.UP -> relativeTo.opposite
        Direction.DOWN -> relativeTo
        relativeTo -> Direction.UP
        relativeTo.opposite -> Direction.DOWN
        else -> this
    }
}

fun Direction.clockwiseSafe() : Direction {
    return when (this) {
        Direction.UP -> this
        Direction.DOWN -> this
        Direction.NORTH -> Direction.EAST
        Direction.SOUTH -> Direction.WEST
        Direction.EAST -> Direction.SOUTH
        Direction.WEST -> Direction.NORTH
    }
}

fun Direction.counterclockwiseSafe() : Direction {
    return when (this) {
        Direction.UP -> this
        Direction.DOWN -> this
        Direction.NORTH -> Direction.WEST
        Direction.SOUTH -> Direction.EAST
        Direction.EAST -> Direction.NORTH
        Direction.WEST -> Direction.SOUTH
    }
}

fun rotateRelativeToFacing(topDirection: Direction, frontDirection: Direction, lookingDirection: Direction, rotateDirection: Direction2D): Pair<Direction, Direction> {
    val newDirection = when (rotateDirection) {
        Direction2D.UP -> Pair(topDirection.upRelativeTo(lookingDirection), frontDirection.upRelativeTo(lookingDirection))
        Direction2D.DOWN -> Pair(topDirection.downRelativeTo(lookingDirection), frontDirection.downRelativeTo(lookingDirection))
        Direction2D.LEFT -> Pair(topDirection.counterclockwiseSafe(), frontDirection.counterclockwiseSafe())
        Direction2D.RIGHT -> Pair(topDirection.clockwiseSafe(), frontDirection.clockwiseSafe())
    }
    return newDirection
}

/**
 * Get all non-air blocks in the given ship. Note that the returned positions will be in the shipyard.
 * If excludeSurroundedBlocks is true, only blocks that are adjacent to at least one air block are included.
 */
fun getBlocksInShipAt(ship: Ship, level: Level, excludeSurroundedBlocks: Boolean = false): HashMap<Vec3i, BlockState> {
    val shipBlocks = HashMap<Vec3i, BlockState>()
    val shipAABB = ship.shipAABB ?: return shipBlocks
    for (x in shipAABB.minX()..shipAABB.maxX()) {
        for (y in shipAABB.minY()..shipAABB.maxY()) {
            for (z in shipAABB.minZ()..shipAABB.maxZ()) {
                val blockPos = BlockPos(x, y, z)
                val blockState = level.getBlockState(blockPos)

                fun isSurroundedByNotAir(pos: Vec3i): Boolean {
                    for (dx in -1..1) {
                        for (dy in -1..1) {
                            for (dz in -1..1) {
                                if (dx == 0 && dy == 0 && dz == 0) {
                                    continue
                                }
                                val neighborPos = pos.subtract(Vec3i(-dx, -dy, -dz))
                                val neighborState = level.getBlockState(BlockPos(neighborPos))
                                if (neighborState.isAir) {
                                    return false
                                }
                            }
                        }
                    }
                    return true
                }

                if (!blockState.isAir && (!excludeSurroundedBlocks || !isSurroundedByNotAir(Vec3i(x, y, z)))) {
                    shipBlocks[blockPos] = blockState
                }
            }
        }
    }
    return shipBlocks
}

fun getPosRelativeToPointer(pos: Vec3i, pointerPos: Vec3, centerOfAABB: Vec3, player: Player): Vec3i =
    getPosRelativeToPointer(
        pos,
        pointerPos,
        centerOfAABB,
        player.telekinesis().placementDirectionTop,
        player.telekinesis().placementDirectionFront
    )

fun getPosRelativeToPointer(pos: Vec3i, pointerPos: Vec3, centerOfAABB: Vec3, topDirection: Direction, frontDirection: Direction): Vec3i {
    var posToCenter = centerOfAABB.subtract(pos.toVec3())
    posToCenter = transformVectorUsingPlacementDirection(
        topDirection,
        frontDirection,
        posToCenter
    )
    return pointerPos.subtract(posToCenter).toVec3i()
}

fun tooFarForPlacement(pos1: Vec3, pos2: Vec3, shipSizeCornerToCorner: Double): Boolean {
    return pos1.subtract(pos2).length() > PLACEMENT_MAX_DISTANCE_FROM_SHIP_MULTIPLIER * shipSizeCornerToCorner
}

fun AABBic.getCornerToCornerSize(): Double {
    return Vec3i(maxX() - minX(), maxY() - minY(), maxZ() - minZ()).toVec3().length()
}