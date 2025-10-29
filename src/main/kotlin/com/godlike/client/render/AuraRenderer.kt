package com.godlike.client.render

import com.godlike.common.MOD_ID
import com.godlike.common.util.negate
import com.godlike.common.util.toVector3d
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.Minecraft
import net.minecraft.client.player.AbstractClientPlayer
import net.minecraft.resources.ResourceLocation
import net.minecraft.util.Mth
import net.minecraft.world.phys.Vec3
import org.joml.Vector3d
import team.lodestar.lodestone.systems.rendering.rendeertype.RenderTypeToken
import java.lang.Math.toDegrees
import java.lang.Math.toRadians
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.sin

val AURA_TEXTURE: RenderTypeToken = RenderTypeToken.createToken(ResourceLocation("minecraft", "textures/block/campfire_fire.png"))

fun renderAuraForPlayer(player: AbstractClientPlayer, poseStack: PoseStack, tickDelta: Float) {
    poseStack.pushPose()

    val camera = Minecraft.getInstance().gameRenderer.mainCamera
    val playerPos = Vec3(
        player.xo + (player.x - player.xo) * tickDelta,
        player.yo + (player.y - player.yo) * tickDelta,
        player.zo + (player.z - player.zo) * tickDelta
    ).add(Vec3(0.0, 1.75, 0.0))

    val cameraToPlayer = playerPos.subtract(camera.position)

    val cameraAngleToPlayer = toDegrees(cameraToPlayer.normalize().toVector3d().angle(Vector3d(cameraToPlayer.normalize().x, 0.0, cameraToPlayer.normalize().z)))
    var auraHeight = if (cameraAngleToPlayer == 0.0) {
        1.5f
    } else {
        max(abs(sin(toRadians(cameraAngleToPlayer - 90)).toFloat() * 3.0f), 1.5f)
    }
    val auraWidth = 1.5f
    var auraPos = playerPos
    var auraRotation = 0.0f

    if (player.isFallFlying) {
        val ffTicks: Float = player.fallFlyingTicks.toFloat() + tickDelta
        val proportionRotated = Mth.clamp(ffTicks * ffTicks / 100.0f, 0.0f, 1.0f)  // Up to full rotation in 10 ticks

        // Lower the aura to the player's feet level as they rotate into fall-flight pose
        val offsetY = -1.75 * proportionRotated
        auraPos = playerPos.add(Vec3(0.0, offsetY, 0.0))

        // Change dimensions to keep the aura around the player
        auraHeight -= ((auraHeight - 1.5f) * proportionRotated)

        // Calculate which direction the player's body is now facing
        var playerBodyFacingVec = Vec3(0.0, 1.0, 0.0)
        val g: Float = player.fallFlyingTicks.toFloat() + tickDelta
        val h = Mth.clamp(g * g / 100.0f, 0.0f, 1.0f)
        if (!player.isAutoSpinAttack) {
            playerBodyFacingVec = playerBodyFacingVec.xRot(h * (-80.0f - toRadians(player.xRot.toDouble()).toFloat()))
        }
        val lerpedBodyRot = toRadians(Mth.rotLerp(tickDelta, player.yBodyRotO, player.yBodyRot).toDouble())
        playerBodyFacingVec = playerBodyFacingVec.yRot(-lerpedBodyRot.toFloat())

        // Testing cubes
//        renderCube(poseStack, playerPos.add(Vec3(0.0, -1.75, 0.0)), 1.0f, CIRCLE_TEXTURE, true)
//        renderCube(poseStack, playerPos.add(Vec3(0.0, -1.75, 0.0).add(playerBodyFacingVec.normalize().negate().scale(2.0))), 1.0f, CIRCLE_TEXTURE, true)

        // Aura should be longer/taller based on how different their body position is from the camera angle
        val camToPlayerFeet = camera.position.subtract(playerPos.add(Vec3(0.0, -1.75, 0.0))).negate()
        val cameraAngleToBody = toDegrees(playerBodyFacingVec.normalize().toVector3d().angle(camToPlayerFeet.normalize().toVector3d()))
        val heightPadding = max(0.0, sin(toRadians(cameraAngleToBody)) * 2 - 0.2)
        auraHeight += heightPadding.toFloat()

        // Rotate the aura poseStack around the camera vector based on how far the player is leaning off the Y axis
        val bodyAngleToVertical = playerBodyFacingVec.toVector3d().angle(Vector3d(0.0, 1.0, 0.0))
        auraRotation = -toDegrees(bodyAngleToVertical).toFloat()

        // Offset the aura position to the middle of their body based on the facing vec
        auraPos = auraPos.add(playerBodyFacingVec.normalize().negate().scale(1.0))
    }

    renderQuadFacingVector(
        poseStack,
        auraPos,
        auraWidth,
        auraHeight,
        cameraToPlayer,
        AURA_TEXTURE,
        doubleSided = true,
        rotationDegrees = auraRotation,
    )

    poseStack.popPose()
}
