package com.godlike.util

import net.minecraft.core.Vec3i
import net.minecraft.world.phys.Vec3
import kotlin.math.pow
import kotlin.math.sqrt

const val MAX_RAYCAST_DISTANCE = 40.0

/**
 * Given a direction vector, finds where that vector intersects the given y coordinate when extended from the origin.
 * If the point is out of range, it is clamped to the max distance away.
 */
fun vecIntersectWithY(origin: Vec3, direction: Vec3, y: Int): Vec3i {
    val factor = (y - origin.y) / direction.y
    var newX = (origin.x + direction.x * factor).toInt()
    var newZ = (origin.z + direction.z * factor).toInt()

    val distance = sqrt((newX - origin.x).pow(2.0) + (newZ - origin.z).pow(2.0))

    if (distance > MAX_RAYCAST_DISTANCE) {
        val ratio = MAX_RAYCAST_DISTANCE / distance
        newX = (origin.x + (newX - origin.x) * ratio).toInt()
        newZ = (origin.z + (newZ - origin.z) * ratio).toInt()
    }

    return Vec3i(newX, y, newZ)
}

/**
 * Given a direction vector, finds where that that vector either of two vertical planes centered on the intersection
 * point when extended from the origin.
 * If the point is out of range, it is clamped to the max distance away. If the direction vector would never intersect
 * either plane, returns the passed in intersection point.
 * If useMax is true, the intersection point further from the origin is used. Otherwise, uses the closer intersection.
 */
fun vecIntersectWithXOrZ(origin: Vec3, direction: Vec3, intersection: Vec3i, useMax: Boolean): Vec3i {
    // find where the direction vector intersects the Z plane
    val zFactor = (intersection.z - origin.z) / direction.z
    var newY = (origin.y + direction.y * zFactor).toInt()
    var newX = (origin.x + direction.x * zFactor).toInt()
    val distance = sqrt((newX - origin.x).pow(2.0) + (newY - origin.y).pow(2.0))

    if (distance > MAX_RAYCAST_DISTANCE) {
        val ratio = MAX_RAYCAST_DISTANCE / distance
        newX = (origin.x + (newX - origin.x) * ratio).toInt()
        newY = (origin.y + (newY - origin.y) * ratio).toInt()
    }
    val zPlaneIntersect = Vec3i(newX, newY-1, intersection.z)

    // find where the direction vector intersects the X plane
    val xFactor = (intersection.x - origin.x) / direction.x
    var newZ = (origin.z + direction.z * xFactor).toInt()
    var newY2 = (origin.y + direction.y * xFactor).toInt()
    val distance2 = sqrt((newZ - origin.z).pow(2.0) + (newY2 - origin.y).pow(2.0))

    if (distance2 > MAX_RAYCAST_DISTANCE) {
        val ratio = MAX_RAYCAST_DISTANCE / distance2
        newZ = (origin.z + (newZ - origin.z) * ratio).toInt()
        newY2 = (origin.y + (newY2 - origin.y) * ratio).toInt()
    }
    val xPlaneIntersect = Vec3i((intersection.x), newY2-1, newZ)

    return if (useMax) {
        if (distance > distance2) zPlaneIntersect else xPlaneIntersect
    } else {
        if (distance < distance2) zPlaneIntersect else xPlaneIntersect
    }
}

fun getVolumeBetween(anchors: List<Vec3i>, target: Vec3i): List<Vec3i> {
    // find the bounds of the volume created by the target and the anchors
    val sortedByX = anchors.sortedBy { it.x }
    val xMin = minOf(sortedByX.first().x, target.x)
    val xMax = maxOf(sortedByX.last().x, target.x)

    val sortedByZ = anchors.sortedBy { it.z }
    val zMin = minOf(sortedByZ.first().z, target.z)
    val zMax = maxOf(sortedByZ.last().z, target.z)

    val sortedByY = anchors.sortedBy { it.y }
    val yMin = minOf(sortedByY.first().y, target.y)
    val yMax = maxOf(sortedByY.last().y, target.y)

    // collect all the positions in the volume
    val positions = mutableListOf<Vec3i>()
    for (x in xMin..xMax) {
        for (y in yMin..yMax) {
            for (z in zMin..zMax) {
                positions.add(Vec3i(x, y, z))
            }
        }
    }
    return positions
}

/**
 * Gets the vertical plane of blocks with opposite corners at the given positions.
 */
fun getVerticalPlaneBetween(pos1: Vec3i, pos2: Vec3i): List<Vec3i> {
    val minY = minOf(pos1.y, pos2.y)
    val maxY = maxOf(pos1.y, pos2.y)
    val minX = minOf(pos1.x, pos2.x)
    val maxX = maxOf(pos1.x, pos2.x)
    val minZ = minOf(pos1.z, pos2.z)
    val maxZ = maxOf(pos1.z, pos2.z)

    val positions = mutableListOf<Vec3i>()
    for (y in minY..maxY) {
        for (x in minX..maxX) {
            for (z in minZ..maxZ) {
                positions.add(Vec3i(x, y, z))
            }
        }
    }
    return positions
}

/**
 * Gets the horizontal plane of blocks with opposite corners at the given positions.
 * The plane is at the y coordinate of pos1.
 */
fun getHorizontalPlaneBetween(pos1: Vec3i, pos2: Vec3i): List<Vec3i> {
    val minX = minOf(pos1.x, pos2.x)
    val maxX = maxOf(pos1.x, pos2.x)
    val minZ = minOf(pos1.z, pos2.z)
    val maxZ = maxOf(pos1.z, pos2.z)

    val positions = mutableListOf<Vec3i>()
    for (x in minX..maxX) {
        for (z in minZ..maxZ) {
            positions.add(Vec3i(x, pos1.y, z))
        }
    }
    return positions
}