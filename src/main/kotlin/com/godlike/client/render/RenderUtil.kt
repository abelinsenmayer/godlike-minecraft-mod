package com.godlike.client.render

import com.godlike.client.mixin.WorldRendererAccessor
import com.godlike.common.components.selection
import com.godlike.client.mixin.EntityInvoker
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Camera
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.OutlineBufferSource
import net.minecraft.client.renderer.RenderType
import net.minecraft.core.BlockPos
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.shapes.Shapes

fun setEntityGlowing(entity: Entity, glowing: Boolean) {
    (entity as EntityInvoker).invokeSetSharedFlag(6, glowing)
}

fun highlightSelectionTarget(poseStack: PoseStack, camera: Camera, outlineBufferSource: OutlineBufferSource) {
    val selection = Minecraft.getInstance().player!!.selection()
    selection.cursorTargetBlock?.let {
        outlineBlockPos(it, poseStack, camera, 100f, 100f, 100f, 1.0f)
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