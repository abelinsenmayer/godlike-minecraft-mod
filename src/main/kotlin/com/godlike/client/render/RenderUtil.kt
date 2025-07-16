package com.godlike.client.render

import com.godlike.client.mixin.EntityInvoker
import com.godlike.client.mixin.WorldRendererAccessor
import com.godlike.client.util.DfsDistanceType
import com.godlike.client.util.canTkShip
import com.godlike.common.MOD_ID
import com.godlike.common.components.selection
import com.godlike.common.components.telekinesis
import com.godlike.common.telekinesis.EntityTkTarget
import com.godlike.common.telekinesis.ShipTkTarget
import com.godlike.common.util.toAABB
import com.godlike.common.util.toVec3
import com.godlike.common.vs2.Vs2Util
import com.mojang.blaze3d.vertex.DefaultVertexFormat
import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.blaze3d.vertex.VertexFormat
import com.mojang.math.Axis
import me.emafire003.dev.coloredglowlib.ColoredGlowLibMod
import net.minecraft.client.Camera
import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer
import net.minecraft.client.renderer.LevelRenderer
import net.minecraft.client.renderer.RenderStateShard
import net.minecraft.client.renderer.RenderType
import net.minecraft.core.BlockPos
import net.minecraft.resources.ResourceLocation
import net.minecraft.world.entity.Entity
import net.minecraft.world.phys.AABB
import net.minecraft.world.phys.Vec3
import net.minecraft.world.phys.shapes.Shapes
import org.joml.Vector3d
import org.joml.Vector3dc
import org.valkyrienskies.core.api.ships.ClientShip
import org.valkyrienskies.mod.common.VSClientGameUtils.transformRenderWithShip
import team.lodestar.lodestone.registry.client.LodestoneRenderTypeRegistry
import team.lodestar.lodestone.registry.client.LodestoneShaderRegistry
import team.lodestar.lodestone.systems.rendering.StateShards.NORMAL_TRANSPARENCY
import team.lodestar.lodestone.systems.rendering.VFXBuilders
import team.lodestar.lodestone.systems.rendering.rendeertype.RenderTypeProvider
import team.lodestar.lodestone.systems.rendering.rendeertype.RenderTypeToken

val HIGHLIGHT_CUBE_TEXTURE: RenderTypeToken = RenderTypeToken.createToken(ResourceLocation(MOD_ID, "textures/render/highlight_cube.png"))
val TEST_TEXTURE: RenderTypeToken = RenderTypeToken.createToken(ResourceLocation(MOD_ID, "textures/render/uv_test.png"))

val TRIANGLE_ADDITIVE_TEXTURE = RenderTypeProvider { token: RenderTypeToken ->
    LodestoneRenderTypeRegistry.createGenericRenderType(
        "triangle_additive_texture",
        DefaultVertexFormat.POSITION_COLOR_TEX_LIGHTMAP,
        VertexFormat.Mode.TRIANGLES,
        LodestoneRenderTypeRegistry.builder()
            .setShaderState(LodestoneShaderRegistry.LODESTONE_TEXTURE)
            .setTransparencyState(NORMAL_TRANSPARENCY)
            .setCullState(RenderStateShard.CULL)
            .setLightmapState(RenderStateShard.LIGHTMAP)
            .setTextureState(token.get())
    )
}

fun LocalPlayer.getPointer() : Vec3 {
    val eyePosition = this.position().add(0.0, 1.5, 0.0)
    val pointerDistance = this.telekinesis().pointerDistance
    val lookDirection = this.lookAngle
    return eyePosition.add(lookDirection.x * pointerDistance, lookDirection.y * pointerDistance, lookDirection.z * pointerDistance)
}

/**
 * Renders a pointer at the player's telekinesis aiming position
 */
fun renderPointer(player: LocalPlayer, poseStack: PoseStack) {
    val pointerPos = player.getPointer()
    val targetAabb: AABB? = player.telekinesis().activeTkTarget?.let {
        when (it) {
            is ShipTkTarget -> Vs2Util.getClientShipWorld(player.clientLevel).loadedShips.getById(it.shipId)?.shipAABB?.toAABB()
            is EntityTkTarget -> player.clientLevel.getEntity(it.entityId)?.boundingBox
            else -> null
        }
    }
    val targetSize = if (targetAabb != null) {
        maxOf(targetAabb.maxX - targetAabb.minX, targetAabb.maxY - targetAabb.minY).toFloat()
    } else {
        1.0f
    }
    renderSpinningCubes(poseStack, pointerPos, targetSize)
}

/**
 * Renders a cube at the given position with the given size.
 *
 * @param poseStack The PoseStack to render the cube with
 * @param center The center of the cube
 * @param size The size of the cube
 * @param texture The texture to render the cube with
 * @param renderInterior Whether the cube should be visible from the inside
 * @param rotateXDegrees The degrees to rotate the cube around the X axis
 * @param rotateYDegrees The degrees to rotate the cube around the Y axis
 * @param rotateZDegrees The degrees to rotate the cube around the Z axis
 */
fun renderCube(
    poseStack: PoseStack,
    center: Vec3,
    size: Float,
    texture: RenderTypeToken,
    renderInterior: Boolean = false,
    rotateXDegrees: Float = 0f,
    rotateYDegrees: Float = 0f,
    rotateZDegrees: Float = 0f
) {
    val camera = Minecraft.getInstance().gameRenderer.mainCamera
    val renderPos = center.subtract(camera.position)
    poseStack.pushPose()
    poseStack.translate(renderPos.x, renderPos.y, renderPos.z)
    poseStack.mulPose(Axis.XP.rotationDegrees(rotateXDegrees))
    poseStack.mulPose(Axis.YP.rotationDegrees(rotateYDegrees))
    poseStack.mulPose(Axis.ZP.rotationDegrees(rotateZDegrees))
    val lwh = (size / 2).toDouble()

    val faces = mutableListOf(
        Vec3(0.0, 0.0, lwh) to Axis.YP.rotationDegrees(0f),
        Vec3(0.0, 0.0, -lwh) to Axis.YP.rotationDegrees(180f),
        Vec3(-lwh, 0.0, 0.0) to Axis.YP.rotationDegrees(-90f),
        Vec3(lwh, 0.0, 0.0) to Axis.YP.rotationDegrees(90f),
        Vec3(0.0, lwh, 0.0) to Axis.XP.rotationDegrees(-90f),
        Vec3(0.0, -lwh, 0.0) to Axis.XP.rotationDegrees(90f),
    )
    if (renderInterior) {
        faces.addAll(listOf(
            Vec3(0.0, 0.0, lwh) to Axis.YP.rotationDegrees(180f),
            Vec3(0.0, 0.0, -lwh) to Axis.YP.rotationDegrees(0f),
            Vec3(-lwh, 0.0, 0.0) to Axis.YP.rotationDegrees(90f),
            Vec3(lwh, 0.0, 0.0) to Axis.YP.rotationDegrees(-90f),
            Vec3(0.0, lwh, 0.0) to Axis.XP.rotationDegrees(90f),
            Vec3(0.0, -lwh, 0.0) to Axis.XP.rotationDegrees(-90f),
        ))
    }

    faces.forEach { (offset, rotation) ->
        poseStack.pushPose()
        poseStack.translate(offset.x, offset.y, offset.z)
        poseStack.mulPose(rotation)
        VFXBuilders.createWorld()
            .setRenderType(LodestoneRenderTypeRegistry.TRANSPARENT_TEXTURE.applyAndCache(texture))
            .renderQuad(poseStack, lwh.toFloat(), lwh.toFloat())
        poseStack.popPose()
    }
    poseStack.popPose()
}

/**
 * Renders a pair of spinning cubes at the given position (similar to an End crystal).
 */
fun renderSpinningCubes(poseStack: PoseStack, center: Vec3, size: Float = 1.0f) {
    val timeForOneRotation = 2.0f // seconds
    val time = Minecraft.getInstance().level?.gameTime?.toFloat() ?: 0f
    val rotation = (time / 20f) % timeForOneRotation / timeForOneRotation * 360f
    renderCube(poseStack, center, size, HIGHLIGHT_CUBE_TEXTURE,true, -45f, 45f + rotation, 0f)
    renderCube(poseStack, center, size, HIGHLIGHT_CUBE_TEXTURE, true, 0f, 0f + rotation, 45f)
}

/**
 * Renders a sphere at the given position with the given size.
 *
 * @param poseStack The PoseStack to render the sphere with
 * @param center The center of the sphere
 * @param size The size of the sphere
 * @param texture The texture to render the sphere with
 */
fun renderSphere(poseStack: PoseStack, center: Vec3, size: Float, texture: RenderTypeToken) {
    val camera = Minecraft.getInstance().gameRenderer.mainCamera
    val renderPos = center.subtract(camera.position)
    poseStack.pushPose()
    poseStack.translate(renderPos.x, renderPos.y, renderPos.z)
    VFXBuilders.createWorld()
        .setRenderType(TRIANGLE_ADDITIVE_TEXTURE.applyAndCache(texture))
        .renderSphere(poseStack, size / 2, 16, 16)
    poseStack.popPose()
}

/**
 * Makes an entity glow.
 *
 * @param entity The entity to make glow
 * @param glowing Whether the entity should glow
 * @param colorHex The color of the glow in hex format. If not provided, the entity will glow white.
 */
fun setEntityGlowing(entity: Entity, glowing: Boolean, colorHex: String? = null) {
    val color = colorHex ?: "#ffffff"
    val glowApi = ColoredGlowLibMod.getAPI()!!
    if (glowing) {
        glowApi.setColor(entity, color)
    } else {
        glowApi.clearColor(entity, true)
    }
    (entity as EntityInvoker).invokeSetSharedFlag(6, glowing)
}

// Note: Entities are highlighted automatically because we set them glowing
fun highlightSelections(poseStack: PoseStack, camera: Camera) {
    val player = Minecraft.getInstance().player!!
    val selection = player.selection()

    selection.cursorTargetShip?.let {
        // If the ship is too big for the player to TK, render the outline in red
        if (!player.canTkShip(it)) {
            outlineShip(it, poseStack, camera, 1.0f, 0.0f, 0.0f, 1.0f)
        } else {
            outlineShip(it, poseStack, camera, 1.0f, 1.0f, 1.0f, 1.0f)
        }
    }
    selection.cursorTargetBlock?.let {
        outlineBlockPos(it, poseStack, camera, 1f, 1f, 1f, 0.5f)
    }
    selection.previewPositions.forEach { outlineBlockPos(it, poseStack, camera, 1f, 1f, 1f, 0.5f) }
}

fun highlightSelectedArea(player: LocalPlayer, poseStack: PoseStack) {
    player.selection().cursorTargetBlock ?: return
    val highlightPosition = player.selection().cursorTargetBlock!!.toVec3().add(0.5, 0.5, 0.5)
    val dfsDepth = player.selection().dfsDepth
    val highlightSize = (if (dfsDepth <= 1) 1 else dfsDepth * 2 - 1).toFloat()

    if (player.selection().dfsDistanceType == DfsDistanceType.CUBE) {
        renderCube(poseStack, highlightPosition, highlightSize, HIGHLIGHT_CUBE_TEXTURE,true)
    } else {
        renderSphere(poseStack, highlightPosition, highlightSize, HIGHLIGHT_CUBE_TEXTURE)
    }

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