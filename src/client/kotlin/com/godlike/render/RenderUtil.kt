package com.godlike.render

import com.godlike.Godlike.logger
import com.godlike.components.ModComponents
import com.godlike.mixin.client.WorldRendererAccessor
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Camera
import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer
import net.minecraft.client.renderer.RenderType
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.phys.shapes.Shapes
import org.joml.Vector3f
import team.lodestar.lodestone.registry.client.LodestoneRenderTypeRegistry
import team.lodestar.lodestone.systems.rendering.VFXBuilders
import team.lodestar.lodestone.systems.rendering.rendeertype.RenderTypeToken


fun renderCubeAt(poseStack: PoseStack, pos: BlockPos, partialTicks: Float) {

    poseStack.pushPose()
    poseStack.translate(pos.x.toDouble(), pos.y.toDouble(), pos.z.toDouble())

    val height = 1f
    val width = 1f
    val renderType = LodestoneRenderTypeRegistry.TRANSPARENT_TEXTURE.applyAndCache(RenderTypeToken.createToken(
        ResourceLocation.tryBuild("godlike", "textures/render/uv_test.png")))
    val positions = arrayOf(
        Vector3f(-width, height, width),
        Vector3f(width, height, width),
        Vector3f(width, height, -width),
        Vector3f(-width, height, -width),
    )

    VFXBuilders.createWorld()
        .setRenderType(renderType)
        .setColor(1.0f, 1.0f, 1.0f, 1.0f)
        .renderSphere(poseStack, 1f, 1, 1)

//    VFXBuilders.createWorld()
//        .setRenderType(renderType)
//        .setColor(1.0f, 1.0f, 1.0f, 1.0f)
//        .renderQuad(
//            poseStack,
//            positions,
//            1f
//        )

    poseStack.popPose()
}

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