package com.godlike.util

import com.godlike.Godlike.logger
import com.godlike.components.ModComponents
import com.godlike.keybind.ModKeybinds.SELECTION_MODE_KEYBINDS
import com.godlike.mixin.client.KeyBindingMixin
import com.godlike.render.renderCubeAt
import com.mojang.blaze3d.vertex.PoseStack
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer
import net.minecraft.core.BlockPos
import net.minecraft.core.Vec3i
import net.minecraft.network.chat.Component
import net.minecraft.world.level.block.Blocks
import net.minecraft.world.level.block.state.BlockState
import org.joml.Vector3f
import team.lodestar.lodestone.handlers.GhostBlockHandler
import team.lodestar.lodestone.registry.client.LodestoneRenderTypeRegistry
import team.lodestar.lodestone.registry.common.particle.LodestoneParticleRegistry
import team.lodestar.lodestone.systems.easing.Easing
import team.lodestar.lodestone.systems.particle.builder.WorldParticleBuilder
import team.lodestar.lodestone.systems.particle.data.GenericParticleData
import team.lodestar.lodestone.systems.particle.data.color.ColorParticleData
import team.lodestar.lodestone.systems.particle.data.spin.SpinParticleData
import team.lodestar.lodestone.systems.rendering.VFXBuilders
import team.lodestar.lodestone.systems.rendering.ghost.GhostBlockOptions
import team.lodestar.lodestone.systems.rendering.ghost.GhostBlockRenderer
import java.awt.Color

fun LocalPlayer.toggleSelectionMode() {
    // set the player to selection mode
    var mode = ModComponents.SELECTION_MODE.get(this).getValue()
    mode = !mode
    ModComponents.SELECTION_MODE.get(this).setValue(mode)
    val message = if (mode) "Selection mode enabled" else "Selection mode disabled"
    this.sendSystemMessage(Component.literal(message))

    if (mode) {
        // promote selection keybinds to the top of the keybind map
        val bindingMap = KeyBindingMixin.getKeyToBindings()
        SELECTION_MODE_KEYBINDS.forEach { keybind ->
            bindingMap[(keybind as KeyBindingMixin).boundKey] = keybind
        }
    } else {
        // restore the keybinds to their original positions
        KeyMapping.resetMapping()
        ModComponents.CURSOR_PREVIEWS.get(this).clearPositions()
        ModComponents.TARGET_POSITION.get(this).setPos(BlockPos(0, -3000, 0))
    }
}

/**
 * Called every tick on the client side when the player is in selection mode.
 * Updates the player's selection based on where they're looking and what they have already selected.
 */
fun showSelectionPreview(client: Minecraft) {
    val player = client.player!!
    val anchors = ModComponents.CURSOR_ANCHORS.get(player).getPositions()

    val targetPos = blockRaycastFromPlayer()
    logger.info("Raycast found position: $targetPos")
    ModComponents.TARGET_POSITION.get(player).setPos(targetPos)

//    when (anchors.size) {
//        0 -> {
//            // no anchors, so we find the place the player is looking at
//            val targetPos = blockRaycastFromPlayer()
//            ModComponents.TARGET_POSITION.get(player).setPos(targetPos)
//        }
//        1 -> {
//            // one anchor, so we highlight the plane of blocks between the last anchor and where the player is looking
//            val camera = client.cameraEntity!!
//            val cameraRotationVec = camera.getViewVector(1.0f)
//            val cameraPositionVec = camera.getViewVector(1.0f)
//            val plane: List<Vec3i>
//
//            if (ModComponents.SELECTING_VERTICAL.get(player).getValue()) {
//                val selectingFar = ModComponents.SELECTING_FAR.get(player).getValue()
//                val targetPos = vecIntersectWithXOrZ(cameraPositionVec, cameraRotationVec, anchors.last(), selectingFar)
//                ModComponents.TARGET_POSITION.get(player).setPos(BlockPos(targetPos))
//                plane = getVerticalPlaneBetween(anchors.last(), targetPos)
//            } else {
//                val targetPos = vecIntersectWithY(cameraPositionVec, cameraRotationVec, anchors.last().y)
//                ModComponents.TARGET_POSITION.get(player).setPos(BlockPos(targetPos))
//                plane = getHorizontalPlaneBetween(anchors.last(), targetPos)
//            }
//            ModComponents.CURSOR_PREVIEWS.get(player).clearPositions()
//            ModComponents.CURSOR_PREVIEWS.get(player).addAllPositions(plane.map { BlockPos(it) })
//        }
//        else -> {
//            // multiple anchors, so we highlight the volume of blocks between the last anchor and where the player is looking
//            val camera = client.cameraEntity!!
//            val cameraRotationVec = camera.getViewVector(1.0f)
//            val cameraPositionVec = camera.getViewVector(1.0f)
//            val volume: List<BlockPos>
//
//            if (ModComponents.SELECTING_VERTICAL.get(player).getValue()) {
//                val selectingFar = ModComponents.SELECTING_FAR.get(player).getValue()
//                var targetPos = vecIntersectWithXOrZ(cameraPositionVec, cameraRotationVec, anchors.last(), selectingFar)
//                // we only want to extend the selection on the y-axis, so we clamp the x and z values to the last anchor
//                targetPos = Vec3i(anchors.last().x, targetPos.y, anchors.last().z)
//                ModComponents.TARGET_POSITION.get(player).setPos(BlockPos(targetPos))
//                volume = getVolumeBetween(anchors, targetPos).map { BlockPos(it) }
//            } else {
//                val targetPos = vecIntersectWithY(cameraPositionVec, cameraRotationVec, anchors.last().y)
//                ModComponents.TARGET_POSITION.get(player).setPos(BlockPos(targetPos))
//                volume = getVolumeBetween(anchors, targetPos).map { BlockPos(it) }
//            }
//            ModComponents.CURSOR_PREVIEWS.get(player).clearPositions()
//            ModComponents.CURSOR_PREVIEWS.get(player).addAllPositions(volume)
//        }
//    }
}

/**
 * Displays VFX for the player's selection.
 */
fun displaySelection(player : LocalPlayer) {
    val previews = ModComponents.CURSOR_PREVIEWS.get(player).getPositions()
    val targetPos = ModComponents.TARGET_POSITION.get(player).getPos()
    val cursors = ModComponents.CURSORS.get(player).getPositions()

//    renderCubeAt(targetPos)

//    val startingColor = Color(100, 0, 100)
//    val endingColor = Color(0, 100, 200)
//    WorldParticleBuilder.create(LodestoneParticleRegistry.WISP_PARTICLE)
//        .setScaleData(GenericParticleData.create(2.0f, 0f).build())
//        .setTransparencyData(GenericParticleData.create(0.75f, 0.25f).build())
//        .setColorData(ColorParticleData.create(startingColor, endingColor).setCoefficient(1.4f).setEasing(Easing.BOUNCE_IN_OUT).build())
//        .setSpinData(SpinParticleData.create(0.2f, 0.4f).setSpinOffset((player.level().gameTime * 0.2f) % 6.28f).setEasing(
//            Easing.QUARTIC_IN).build())
//        .setLifetime(40)
//        .addMotion(0.0, 0.01, 0.0)
//        .enableNoClip()
//        .spawn(player.level(), targetPos.x.toDouble(), targetPos.y + 2.0, targetPos.z.toDouble())

}

