package com.godlike.client.render

import com.godlike.client.mixin.EntityInvoker
import com.godlike.client.mixin.WorldRendererAccessor
import com.godlike.client.util.isTargetContiguousWithSelection
import com.godlike.common.Godlike.logger
import com.godlike.common.components.Mode
import com.godlike.common.components.getMode
import com.godlike.common.components.selection
import com.godlike.common.components.telekinesis
import com.godlike.common.util.maxSize
import com.google.common.primitives.Doubles.max
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Camera
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.LevelRenderer
import net.minecraft.client.renderer.OutlineBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.shapes.Shapes
import org.joml.Vector3d
import org.joml.Vector3dc
import org.valkyrienskies.core.api.ships.ClientShip
import org.valkyrienskies.mod.common.VSClientGameUtils.transformRenderWithShip
import java.awt.Color

fun setEntityGlowing(entity: Entity, glowing: Boolean) {
    (entity as EntityInvoker).invokeSetSharedFlag(6, glowing)
}

// Note: Entities are highlighted automatically because we set them glowing
fun highlightSelections(poseStack: PoseStack, camera: Camera, outlineBufferSource: OutlineBufferSource) {
    val player = Minecraft.getInstance().player!!
    val selection = player.selection()

    selection.cursorTargetShip?.let {
        // If the ship is too big for the player to TK, render the outline in red
        if (it.worldAABB.maxSize() > (player.telekinesis().tier.selectionRadius * 2 + 1)) {
            outlineShip(it, poseStack, camera, 1.0f, 0.0f, 0.0f, 1.0f)
        } else {
            outlineShip(it, poseStack, camera, 1.0f, 1.0f, 1.0f, 1.0f)
        }
    }
    selection.cursorTargetBlock?.let {
        outlineBlockPos(it, poseStack, camera, Color(255, 255, 255))
    }
    selection.previewPositions.forEach { outlineBlockPos(it, poseStack, camera, 1f, 1f, 1f, 0.5f) }
}

fun outlineShip(
    ship: ClientShip, poseStack: PoseStack, camera: Camera, red: Float, green: Float, blue: Float, alpha: Float
) {
    val cameraPos = camera.position
    val vertexConsumer =
        (Minecraft.getInstance().levelRenderer as WorldRendererAccessor).bufferBuilders.bufferSource().getBuffer(
            RenderType.lines()
        )

    // Render the ship's voxel AABB
    ship.shipAABB?.let { shipAABB ->
        poseStack.pushPose()
        val centerOfAABB: Vector3dc = shipAABB.center(Vector3d())

        // Offset the AABB by -[centerOfAABB] to fix floating point errors.
        val shipVoxelAABBAfterOffset =
            AABB(
                shipAABB.minX() - centerOfAABB.x(),
                shipAABB.minY() - centerOfAABB.y(),
                shipAABB.minZ() - centerOfAABB.z(),
                shipAABB.maxX() - centerOfAABB.x(),
                shipAABB.maxY() - centerOfAABB.y(),
                shipAABB.maxZ() - centerOfAABB.z()
            )

        // Offset the transform of the AABB by [centerOfAABB] to account for [shipVoxelAABBAfterOffset]
        // being offset by -[centerOfAABB].
        transformRenderWithShip(
            ship.renderTransform,
            poseStack,
            centerOfAABB.x(), centerOfAABB.y(), centerOfAABB.z(),
            cameraPos.x, cameraPos.y, cameraPos.z
        )

        LevelRenderer
            .renderLineBox(
                poseStack,
                vertexConsumer,
                shipVoxelAABBAfterOffset,
                red, green, blue, alpha
            )
        poseStack.popPose()
    }
}

fun outlineBlockPos(targetPos: BlockPos, poseStack: PoseStack, camera: Camera, color: Color) = outlineBlockPos(
    targetPos, poseStack, camera,
    color.red.toFloat() / 255f,
    color.green.toFloat() / 255f,
    color.blue.toFloat() / 255f,
    color.alpha.toFloat() / 255f
)

fun outlineBlockPos(
    targetPos: BlockPos, poseStack: PoseStack, camera: Camera, red: Float, green: Float, blue: Float, alpha: Float
) {
    val cameraPos = camera.position
    val vertexConsumer =
        (Minecraft.getInstance().levelRenderer as WorldRendererAccessor).bufferBuilders.bufferSource().getBuffer(
            RenderType.lines()
        )

    WorldRendererAccessor.renderShape(
        poseStack,
        vertexConsumer,
        Shapes.block(),
        targetPos.x - cameraPos.x(),
        targetPos.y - cameraPos.y(),
        targetPos.z - cameraPos.z(),
        red, green, blue, alpha
    )
}