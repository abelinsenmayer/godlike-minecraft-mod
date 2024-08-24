package com.godlike.render

import com.godlike.mixin.client.WorldRendererAccessor
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Camera
import net.minecraft.client.Minecraft
import net.minecraft.client.renderer.RenderType
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.shapes.Shapes

/**
 * Highlights the given position by rendering a cube outline around it.
 */
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