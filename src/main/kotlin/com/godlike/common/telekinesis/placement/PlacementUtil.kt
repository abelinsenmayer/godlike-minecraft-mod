package com.godlike.common.telekinesis.placement

import com.godlike.common.Godlike
import com.godlike.common.util.toVec3
import com.godlike.common.util.toVector3d
import net.minecraft.core.Direction
import net.minecraft.world.phys.Vec3
import org.joml.AxisAngle4d
import org.joml.Matrix3d
import org.joml.Quaterniond
import org.joml.Vector3d
import java.lang.Math.toRadians

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