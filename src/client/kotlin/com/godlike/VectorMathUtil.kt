package com.godlike

import net.minecraft.client.MinecraftClient
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d
import net.minecraft.world.RaycastContext
import kotlin.math.pow
import kotlin.math.sqrt

fun BlockPos.toVec3d(): Vec3d = Vec3d(x.toDouble(), y.toDouble(), z.toDouble())

fun Vec3d.toBlockPos(): BlockPos = BlockPos(x.toInt(), y.toInt(), z.toInt())

fun blockRaycastFromPlayer(): BlockPos {
    val client = MinecraftClient.getInstance()
    val cameraEntity = MinecraftClient.getInstance().cameraEntity!!
    val vec3d: Vec3d = cameraEntity.getCameraPosVec(1.0f)
    val vec3d2: Vec3d = cameraEntity.getRotationVec(1.0f)
    val vec3d3 = vec3d.add(vec3d2.x * MAX_RAYCAST_DISTANCE, vec3d2.y * MAX_RAYCAST_DISTANCE, vec3d2.z * MAX_RAYCAST_DISTANCE)
    val hitResult = cameraEntity.world.raycast(
        RaycastContext(
            vec3d,
            vec3d3,
            RaycastContext.ShapeType.COLLIDER,
            RaycastContext.FluidHandling.ANY,
            cameraEntity
        )
    )
    // get the block position the player is looking at
    val pos = hitResult.pos
    var blockPos = BlockPos(pos.x.toInt()-1, pos.y.toInt(), pos.z.toInt()-1)
    // if the position is air, we need to offset it in the direction we're looking to get the solid block
    if (client.world!!.getBlockState(blockPos).isAir) {
        blockPos = blockPos.offset(hitResult.side.opposite)
    }
    return blockPos;
}

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

    return Vec3d((newX-1).toDouble(), y.toDouble(), (newZ-1).toDouble())
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
    val zPlaneIntersect = Vec3d((newX).toDouble(), newY.toDouble(), (intersection.z))

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
    val xPlaneIntersect = Vec3d((intersection.x), newY2.toDouble(), (newZ).toDouble())

    return if (useMax) {
        if (distance > distance2) zPlaneIntersect else xPlaneIntersect
    } else {
        if (distance < distance2) zPlaneIntersect else xPlaneIntersect
    }
}

/**
 * Extends the vertical plane of blocks between the given anchors to include the target position.
 */
fun extendVerticalPlaneTo(anchors: List<Vec3d>, target: Vec3d): List<Vec3d> {
    val positions = mutableListOf<Vec3d>()
    for (anchor in anchors) {
        val plane = getVerticalPlaneBetween(anchor, target)
        positions.addAll(plane)
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
 * Expands the horizontal plane of blocks between the given anchors to include the target position.
 */
fun expandHorizontalPlaneTo(anchors: List<Vec3d>, target: Vec3d): List<Vec3d> {
    val positions = mutableListOf<Vec3d>()
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