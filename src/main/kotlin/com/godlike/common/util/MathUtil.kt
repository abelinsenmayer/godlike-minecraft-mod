package com.godlike.common.util

import net.minecraft.core.Vec3i
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import org.joml.Vector3d
import org.joml.Vector3dc
import org.joml.primitives.AABBdc
import org.joml.primitives.AABBic
import kotlin.math.pow
import kotlin.math.sqrt

const val MAX_RAYCAST_DISTANCE = 80.0

fun AABBic.toAABB(): AABB {
    return AABB(minX().toDouble(), minY().toDouble(), minZ().toDouble(), maxX().toDouble(), maxY().toDouble(), maxZ().toDouble())
}

fun AABBdc.toAABB(): AABB {
    return AABB(minX(), minY(), minZ(), maxX(), maxY(), maxZ())
}

fun Vec3.toVector3d(): Vector3d {
    return Vector3d(x, y, z)
}

fun Vector3dc.toVec3(): Vec3 {
    return Vec3(x(), y(), z())
}

fun Vec3.negate(): Vec3 {
    return Vec3(-x, -y, -z)
}

fun Vec3i.toVec3(): Vec3 {
    return Vec3(x.toDouble(), y.toDouble(), z.toDouble())
}

fun Vec3.toVec3i(): Vec3i {
    return Vec3i(x.toInt(), y.toInt(), z.toInt())
}

fun Vec3i.getNeighbors() : List<Vec3i> {
    return listOf(
        Vec3i(x + 1, y, z),
        Vec3i(x - 1, y, z),
        Vec3i(x, y + 1, z),
        Vec3i(x, y - 1, z),
        Vec3i(x, y, z + 1),
        Vec3i(x, y, z - 1)
    )
}

/**
 * Given a point p and a vector v, finds where v intersects the plane perpendicular to v and passing
 * through the point t.
 */
fun vecIntersectsPerpendicularPlaneFromPoint(p: Vec3, v: Vec3, t: Vec3) : Vec3 {
    val tp = p.subtract(t)
    val lambda = tp.negate().dot(v) / v.dot(v)
    return p.add(v.scale(lambda))
}

/**
 * Given a sphere with the given center and radius, find a point on the sphere where the angle vector is pointing.
 */
fun findPointOnSphereAtRadius(center: Vec3, radius: Double, angle: Vec3) : Vec3 {
    val normAngle = angle.normalize()
    return normAngle.scale(radius).add(center)
}

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
fun vecIntersectWithXOrZ(origin: Vec3, direction: Vec3, intersection: Vec3, useMax: Boolean): Vec3 {
    // find where the direction vector intersects the Z plane
    val zFactor = (intersection.z - origin.z) / direction.z
    var newY = (origin.y + direction.y * zFactor)
    var newX = (origin.x + direction.x * zFactor)
    val distance = sqrt((newX - origin.x).pow(2.0) + (newY - origin.y).pow(2.0))

    if (distance > MAX_RAYCAST_DISTANCE) {
        val ratio = MAX_RAYCAST_DISTANCE / distance
        newX = (origin.x + (newX - origin.x) * ratio)
        newY = (origin.y + (newY - origin.y) * ratio)
    }
    val zPlaneIntersect = Vec3(newX, newY+1, intersection.z)

    // find where the direction vector intersects the X plane
    val xFactor = (intersection.x - origin.x) / direction.x
    var newZ = (origin.z + direction.z * xFactor)
    var newY2 = (origin.y + direction.y * xFactor)
    val distance2 = sqrt((newZ - origin.z).pow(2.0) + (newY2 - origin.y).pow(2.0))

    if (distance2 > MAX_RAYCAST_DISTANCE) {
        val ratio = MAX_RAYCAST_DISTANCE / distance2
        newZ = (origin.z + (newZ - origin.z) * ratio)
        newY2 = (origin.y + (newY2 - origin.y) * ratio)
    }
    val xPlaneIntersect = Vec3((intersection.x), newY2+1, newZ)

    return if (useMax) {
        if (distance > distance2) zPlaneIntersect else xPlaneIntersect
    } else {
        if (distance < distance2) zPlaneIntersect else xPlaneIntersect
    }
}

/**
 * Finds all positions in the volume which touches all anchor positions and the target position. The volume is always a
 * cuboid.
 *
 * @param anchors the positions which the volume must touch
 * @param target the position which the volume must extend to
 * @return a list of all positions in the volume
 */
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