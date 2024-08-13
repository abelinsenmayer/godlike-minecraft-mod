package com.godlike.util

import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import kotlin.math.pow
import kotlin.math.sqrt

const val MAX_RAYCAST_DISTANCE = 40.0

fun BlockPos.toVec3d(): Vec3d = Vec3d(x.toDouble(), y.toDouble(), z.toDouble())

fun Vec3d.toBlockPos(): BlockPos = BlockPos(x.toInt(), y.toInt(), z.toInt())

/**
 * Given a direction vector, finds where that vector intersects the given y coordinate when extended from the origin.
 * If the point is out of range, it is clamped to the max distance away.
 */
fun vecIntersectWithY(origin: Vec3d, direction: Vec3d, y: Int): Vec3d {
    val factor = (y - origin.y) / direction.y
    var newX = (origin.x + direction.x * factor).toInt()
    var newZ = (origin.z + direction.z * factor).toInt()

    val distance = sqrt((newX - origin.x).pow(2.0) + (newZ - origin.z).pow(2.0))

    if (distance > MAX_RAYCAST_DISTANCE) {
        val ratio = MAX_RAYCAST_DISTANCE / distance
        newX = (origin.x + (newX - origin.x) * ratio).toInt()
        newZ = (origin.z + (newZ - origin.z) * ratio).toInt()
    }

    return Vec3d((newX).toDouble(), y.toDouble(), (newZ).toDouble())
}

/**
 * Given a direction vector, finds where that that vector either of two vertical planes centered on the intersection
 * point when extended from the origin.
 * If the point is out of range, it is clamped to the max distance away. If the direction vector would never intersect
 * either plane, returns the passed in intersection point.
 * If useMax is true, the intersection point further from the origin is used. Otherwise, uses the closer intersection.
 */
fun vecIntersectWithXOrZ(origin: Vec3d, direction: Vec3d, intersection: Vec3d, useMax: Boolean): Vec3d {
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
    val zPlaneIntersect = Vec3d((newX).toDouble(), newY.toDouble()-1, (intersection.z))

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
    val xPlaneIntersect = Vec3d((intersection.x), newY2.toDouble()-1, (newZ).toDouble())

    return if (useMax) {
        if (distance > distance2) zPlaneIntersect else xPlaneIntersect
    } else {
        if (distance < distance2) zPlaneIntersect else xPlaneIntersect
    }
}

fun getVolumeBetween(anchors: List<Vec3d>, target: Vec3d): List<Vec3d> {
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
    val positions = mutableListOf<Vec3d>()
    for (x in xMin.toInt()..xMax.toInt()) {
        for (y in yMin.toInt()..yMax.toInt()) {
            for (z in zMin.toInt()..zMax.toInt()) {
                positions.add(Vec3d(x.toDouble(), y.toDouble(), z.toDouble()))
            }
        }
    }
    return positions
}

/**
 * Gets the vertical plane of blocks with opposite corners at the given positions.
 */
fun getVerticalPlaneBetween(pos1: Vec3d, pos2: Vec3d): List<Vec3d> {
    val minY = minOf(pos1.y, pos2.y)
    val maxY = maxOf(pos1.y, pos2.y)
    val minX = minOf(pos1.x, pos2.x)
    val maxX = maxOf(pos1.x, pos2.x)
    val minZ = minOf(pos1.z, pos2.z)
    val maxZ = maxOf(pos1.z, pos2.z)

    val positions = mutableListOf<Vec3d>()
    for (y in minY.toInt()..maxY.toInt()) {
        for (x in minX.toInt()..maxX.toInt()) {
            for (z in minZ.toInt()..maxZ.toInt()) {
                positions.add(Vec3d(x.toDouble(), y.toDouble(), z.toDouble()))
            }
        }
    }
    return positions
}

/**
 * Gets the horizontal plane of blocks with opposite corners at the given positions.
 * The plane is at the y coordinate of pos1.
 */
fun getHorizontalPlaneBetween(pos1: Vec3d, pos2: Vec3d): List<Vec3d> {
    val minX = minOf(pos1.x, pos2.x)
    val maxX = maxOf(pos1.x, pos2.x)
    val minZ = minOf(pos1.z, pos2.z)
    val maxZ = maxOf(pos1.z, pos2.z)

    val positions = mutableListOf<Vec3d>()
    for (x in minX.toInt()..maxX.toInt()) {
        for (z in minZ.toInt()..maxZ.toInt()) {
            positions.add(Vec3d(x.toDouble(), pos1.y, z.toDouble()))
        }
    }
    return positions
}