package com.godlike.render

import com.godlike.mixin.client.WorldRendererAccessor
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Camera
import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer
import net.minecraft.client.renderer.RenderType
import net.minecraft.core.BlockPos
import net.minecraft.world.phys.AABB

/**
 * Highlights the given position by rendering a cube outline around it.
 */
fun outlineBlockPos(
    targetPos: BlockPos, player: LocalPlayer, camera: Camera, red: Float, green: Float, blue: Float, alpha: Float
) {
    val cameraPos = camera.position
    val vertexConsumer =
        (Minecraft.getInstance().levelRenderer as WorldRendererAccessor).bufferBuilders.outlineBufferSource().getBuffer(
            RenderType.LINES
        )
    WorldRendererAccessor.invokeDrawCuboidShapeOutline(
        PoseStack(),
        vertexConsumer,
        AABB(
            targetPos.x - cameraPos.x,
            targetPos.y - cameraPos.y,
            targetPos.z - cameraPos.z,
            targetPos.x + 1 - cameraPos.x,
            targetPos.y + 1 - cameraPos.y,
            targetPos.z + 1 - cameraPos.z
        ),
        red, green, blue, alpha
    )
}