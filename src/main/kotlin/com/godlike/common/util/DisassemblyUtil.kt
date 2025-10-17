package com.godlike.common.util

import com.godlike.common.Godlike.logger
import com.godlike.common.telekinesis.placement.transformVectorUsingPlacementDirection
import com.godlike.common.vs2.Vs2Util
import com.godlike.common.vs2.Vs2Util.toJOML
import com.godlike.common.vs2.Vs2Util.updateBlock
import com.google.common.collect.Sets
import net.minecraft.core.BlockPos
import net.minecraft.core.Direction
import net.minecraft.core.Vec3i
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.ChunkPos
import net.minecraft.world.level.block.Rotation
import net.minecraft.world.level.block.state.BlockState
import org.joml.AxisAngle4d
import org.joml.Matrix4d
import org.joml.Vector3d
import org.valkyrienskies.core.api.ships.ServerShip
import org.valkyrienskies.core.impl.networking.simple.sendToClient
import org.valkyrienskies.mod.common.networking.PacketRestartChunkUpdates
import org.valkyrienskies.mod.common.networking.PacketStopChunkUpdates
import kotlin.math.*

fun disassembleAt(ship: ServerShip, level: ServerLevel, centerPos: Vec3i, topFacing: Direction, frontFacing: Direction) {
    val rotation: Rotation = Rotation.NONE

    val shipToWorld = ship.transform.run {
        Matrix4d()
            .translate(-positionInShip.x(), -positionInShip.y(), -positionInShip.z())  // move the pos relative to the origin
            .translate(centerPos.toVec3().toVector3d())  // move the pos relative to the local we're placing
            .scale(shipToWorldScaling)
    }

//    fun transformToWorld(pos: Vector3d): Vector3d {
//        val positionInShip = ship.transform.positionInShip
//        val shipToWorldScaling = ship.transform.shipToWorldScaling
//        val eventualPosition = Vec3(positionInShip.x(), positionInShip.y(), positionInShip.z())
//            .add(-positionInShip.x(), -positionInShip.y(), -positionInShip.z())
//            .add(centerPos.toVec3())
//        val posRelativeToCenter = eventualPosition.subtract(centerPos.toVec3())
//        val translatedRelativePos = transformVectorUsingPlacementDirection(topFacing, frontFacing, posRelativeToCenter)
//        val matrix = Matrix4d()
//            .translate(-positionInShip.x(), -positionInShip.y(), -positionInShip.z())  // move the pos relative to the origin
//            .translate(centerPos.toVec3().toVector3d())  // move the pos relative to the local we're placing
//            .translate(translatedRelativePos.toVector3d())  // Account for rotation
//            .scale(shipToWorldScaling)
//        return pos.mulDirection(matrix)
//    }

    val alloc0 = Vector3d()

    val chunksToBeUpdated = mutableMapOf<ChunkPos, Pair<ChunkPos, ChunkPos>>()

    ship.activeChunksSet.forEach { chunkX, chunkZ ->
        chunksToBeUpdated[ChunkPos(chunkX, chunkZ)] =
            Pair(ChunkPos(chunkX, chunkZ), ChunkPos(chunkX, chunkZ))
    }

    val chunkPairs = chunksToBeUpdated.values.toList()
    val chunkPoses = chunkPairs.flatMap { it.toList() }
    val chunkPosesJOML = chunkPoses.map { toJOML(it) }

    // Send a list of all the chunks that we plan on updating to players, so that they
    // defer all updates until assembly is finished
    level.players().forEach { player ->
        PacketStopChunkUpdates(chunkPosesJOML).sendToClient(Vs2Util.playerWrapper(player))
    }

    val toUpdate = Sets.newHashSet<Triple<BlockPos, BlockPos, BlockState>>()

    ship.activeChunksSet.forEach { chunkX, chunkZ ->
        val chunk = level.getChunk(chunkX, chunkZ)
        for (sectionIndex in 0 until chunk.sections.size) {
            val section = chunk.sections[sectionIndex]

            if (section == null || section.hasOnlyAir()) continue

            val bottomY = sectionIndex shl 4

            for (x in 0..15) {
                for (y in 0..15) {
                    for (z in 0..15) {
                        val state = section.getBlockState(x, y, z)
                        if (state.isAir) continue

                        val realX = (chunkX shl 4) + x
                        val realY = bottomY + y + level.minBuildHeight
                        val realZ = (chunkZ shl 4) + z

                        val inWorldPos = shipToWorld.transformPosition(alloc0.set(realX + 0.5, realY + 0.5, realZ + 0.5)).floor()
                        val posToCenter = inWorldPos.toVec3().subtract(centerPos.toVec3())
                        val rotatedPosToCenter = transformVectorUsingPlacementDirection(topFacing, frontFacing, posToCenter)
                        val rotatedPos = centerPos.toVec3().add(rotatedPosToCenter)

                        val inWorldBlockPos = BlockPos(rotatedPos.x.toInt(), rotatedPos.y.toInt(), rotatedPos.z.toInt())
                        val inShipPos = BlockPos(realX, realY, realZ)

                        toUpdate.add(Triple(inShipPos, inWorldBlockPos, state))
                        Vs2Util.relocateBlock(level, inShipPos, inWorldBlockPos, false, null, rotation)
                    }
                }
            }
        }
    }
    // We update the blocks after they're set to prevent blocks from breaking
    for (triple in toUpdate) {
        updateBlock(level, triple.first, triple.second, triple.third)
    }

    Vs2Util.executeIf(
        level.server,
        { chunkPoses.all { Vs2Util.isTickingChunk(level, it) } },
    ) {
        level.players()
            .forEach { player -> PacketRestartChunkUpdates(chunkPosesJOML).sendToClient(Vs2Util.playerWrapper(player)) }
    }
}

// Adapted from Eureka disassembly code
// https://github.com/ValkyrienSkies/Eureka/blob/1.20.1/main/common/src/main/kotlin/org/valkyrienskies/eureka/util/ShipAssembler.kt
fun disassemble(ship: ServerShip, level: ServerLevel) {
    fun rotationFromAxisAngle(axis: AxisAngle4d): Rotation {
        if (axis.y.absoluteValue < 0.1) {
            // if the axis isn't Y, either we're tilted up/down (which should not happen often) or we haven't moved and it's
            // along the z axis with a magnitude of 0 for some reason. In these cases, we don't rotate.
            return Rotation.NONE
        }

        // normalize into counterclockwise rotation (i.e. positive y-axis, according to testing + right hand rule)
        if (axis.y.sign < 0.0) {
            axis.y = 1.0
            // the angle is always positive and < 2pi coming in
            axis.angle = 2.0 * PI - axis.angle
            axis.angle %= (2.0 * PI)
        }

        val eps = 0.001
        if (axis.angle < eps)
            return Rotation.NONE
        else if ((axis.angle - PI / 2.0).absoluteValue < eps)
            return Rotation.COUNTERCLOCKWISE_90
        else if ((axis.angle - PI).absoluteValue < eps)
            return Rotation.CLOCKWISE_180
        else if ((axis.angle - 3.0 * PI / 2.0).absoluteValue < eps)
            return Rotation.CLOCKWISE_90
        else {
            logger.warn("failed to convert $axis into a rotation")
            return Rotation.NONE
        }
    }

    fun roundToNearestMultipleOf(number: Double, multiple: Double) = multiple * round(number / multiple)

    fun snapRotation(direction: AxisAngle4d): AxisAngle4d {
        val x = abs(direction.x)
        val y = abs(direction.y)
        val z = abs(direction.z)
        val angle = roundToNearestMultipleOf(direction.angle, PI / 2)

        return if (x > y && x > z) {
            direction.set(angle, direction.x.sign, 0.0, 0.0)
        } else if (y > x && y > z) {
            direction.set(angle, 0.0, direction.y.sign, 0.0)
        } else {
            direction.set(angle, 0.0, 0.0, direction.z.sign)
        }
    }

    val rotation: Rotation = ship.transform.shipToWorldRotation
        .let(::AxisAngle4d)
        .let(::snapRotation)
        .let(::rotationFromAxisAngle)

    // ship's rotation rounded to nearest 90*
    val shipToWorld = ship.transform.run {
        Matrix4d()
            .translate(positionInWorld)
            .rotate(snapRotation(AxisAngle4d(shipToWorldRotation)))
            .scale(shipToWorldScaling)
            .translate(-positionInShip.x(), -positionInShip.y(), -positionInShip.z())
    }

    val alloc0 = Vector3d()

    val chunksToBeUpdated = mutableMapOf<ChunkPos, Pair<ChunkPos, ChunkPos>>()

    ship.activeChunksSet.forEach { chunkX, chunkZ ->
        chunksToBeUpdated[ChunkPos(chunkX, chunkZ)] =
            Pair(ChunkPos(chunkX, chunkZ), ChunkPos(chunkX, chunkZ))
    }

    val chunkPairs = chunksToBeUpdated.values.toList()
    val chunkPoses = chunkPairs.flatMap { it.toList() }
    val chunkPosesJOML = chunkPoses.map { toJOML(it) }

    // Send a list of all the chunks that we plan on updating to players, so that they
    // defer all updates until assembly is finished
    level.players().forEach { player ->
        PacketStopChunkUpdates(chunkPosesJOML).sendToClient(Vs2Util.playerWrapper(player))
    }

    val toUpdate = Sets.newHashSet<Triple<BlockPos, BlockPos, BlockState>>()

    ship.activeChunksSet.forEach { chunkX, chunkZ ->
        val chunk = level.getChunk(chunkX, chunkZ)
        for (sectionIndex in 0 until chunk.sections.size) {
            val section = chunk.sections[sectionIndex]

            if (section == null || section.hasOnlyAir()) continue

            val bottomY = sectionIndex shl 4

            for (x in 0..15) {
                for (y in 0..15) {
                    for (z in 0..15) {
                        val state = section.getBlockState(x, y, z)
                        if (state.isAir) continue

                        val realX = (chunkX shl 4) + x
                        val realY = bottomY + y + level.minBuildHeight
                        val realZ = (chunkZ shl 4) + z

                        val inWorldPos = shipToWorld.transformPosition(alloc0.set(realX + 0.5, realY + 0.5, realZ + 0.5)).floor()

                        val inWorldBlockPos = BlockPos(inWorldPos.x.toInt(), inWorldPos.y.toInt(), inWorldPos.z.toInt())
                        val inShipPos = BlockPos(realX, realY, realZ)

                        toUpdate.add(Triple(inShipPos, inWorldBlockPos, state))
                        Vs2Util.relocateBlock(level, inShipPos, inWorldBlockPos, false, null, rotation)
                    }
                }
            }
        }
    }
    // We update the blocks after they're set to prevent blocks from breaking
    for (triple in toUpdate) {
        updateBlock(level, triple.first, triple.second, triple.third)
    }

    Vs2Util.executeIf(
        level.server,
        { chunkPoses.all { Vs2Util.isTickingChunk(level, it) } },
    ) {
        level.players()
            .forEach { player -> PacketRestartChunkUpdates(chunkPosesJOML).sendToClient(Vs2Util.playerWrapper(player)) }
    }
}