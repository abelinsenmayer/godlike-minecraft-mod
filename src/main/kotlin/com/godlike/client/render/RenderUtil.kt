package com.godlike.client.render

import com.godlike.client.mixin.EntityInvoker
import com.godlike.client.mixin.WorldRendererAccessor
import com.godlike.common.components.selection
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

fun setEntityGlowing(entity: Entity, glowing: Boolean) {
    (entity as EntityInvoker).invokeSetSharedFlag(6, glowing)
}

// Note: Entities are highlighted automatically because we set them glowing
fun highlightSelections(poseStack: PoseStack, camera: Camera, outlineBufferSource: OutlineBufferSource) {
    val selection = Minecraft.getInstance().player!!.selection()
    selection.cursorTargetBlock?.let {
        outlineBlockPos(it, poseStack, camera, 1f, 1f, 1f, 1.0f)
    }
    selection.cursorTargetShip?.let {
        outlineShip(it, poseStack, camera, 100f, 100f, 100f, 1.0f)
    }
    selection.selectedPositions.forEach { outlineBlockPos(it, poseStack, camera, 0f, 0f, 1f, 1.0f) }
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
                1.0f, 1.0f, 1.0f, 1.0f
            )
        poseStack.popPose()
    }
}

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