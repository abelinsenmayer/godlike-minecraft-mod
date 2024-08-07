package com.godlike.render

import com.godlike.mixin.client.WorldRendererAccessor
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.render.Camera
import net.minecraft.client.render.RenderLayer
import net.minecraft.client.util.math.MatrixStack
import net.minecraft.util.math.BlockPos
import net.minecraft.util.shape.VoxelShapes

/**
 * Highlights the given position by rendering a cube outline around it.
 */
fun outlineBlockPos(
    targetPos: BlockPos, player: ClientPlayerEntity, camera: Camera, red: Float, green: Float, blue: Float, alpha: Float
) {
    val cameraPos = camera.pos
    val vertexConsumer =
        (MinecraftClient.getInstance().worldRenderer as WorldRendererAccessor).bufferBuilders.entityVertexConsumers.getBuffer(
            RenderLayer.getLines()
        )
    val shape = VoxelShapes.fullCube()

    WorldRendererAccessor.invokeDrawCuboidShapeOutline(
        MatrixStack(),
        vertexConsumer,
        shape,
        targetPos.x - cameraPos.getX(),
        targetPos.y - cameraPos.getY(),
        targetPos.z - cameraPos.getZ(),
        red, green, blue, alpha
    )
}