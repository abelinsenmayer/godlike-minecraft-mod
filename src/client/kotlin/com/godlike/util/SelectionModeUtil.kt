package com.godlike.util

import com.godlike.components.ModComponents
import com.godlike.keybind.ModKeybinds.SELECTION_MODE_KEYBINDS
import com.godlike.mixin.client.KeyBindingMixin
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer
import net.minecraft.core.BlockPos
import net.minecraft.core.Vec3i
import net.minecraft.network.chat.Component

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
 * It displays a preview of the selection the player is making.
 */
fun showSelectionPreview(client: Minecraft) {
    val player = client.player!!
    val anchors = ModComponents.CURSOR_ANCHORS.get(player).getPositions()

    when (anchors.size) {
        0 -> {
            // no anchors, so we find the place the player is looking at
            val targetPos = blockRaycastFromPlayer()
            ModComponents.TARGET_POSITION.get(player).setPos(targetPos)
        }
        1 -> {
            // one anchor, so we highlight the plane of blocks between the last anchor and where the player is looking
            val camera = client.cameraEntity!!
            val cameraRotationVec = camera.getViewVector(1.0f)
            val cameraPositionVec = camera.getViewVector(1.0f)
            val plane: List<Vec3i>

            if (ModComponents.SELECTING_VERTICAL.get(player).getValue()) {
                val selectingFar = ModComponents.SELECTING_FAR.get(player).getValue()
                val targetPos = vecIntersectWithXOrZ(cameraPositionVec, cameraRotationVec, anchors.last(), selectingFar)
                ModComponents.TARGET_POSITION.get(player).setPos(BlockPos(targetPos))
                plane = getVerticalPlaneBetween(anchors.last(), targetPos)
            } else {
                val targetPos = vecIntersectWithY(cameraPositionVec, cameraRotationVec, anchors.last().y)
                ModComponents.TARGET_POSITION.get(player).setPos(BlockPos(targetPos))
                plane = getHorizontalPlaneBetween(anchors.last(), targetPos)
            }
            ModComponents.CURSOR_PREVIEWS.get(player).clearPositions()
            ModComponents.CURSOR_PREVIEWS.get(player).addAllPositions(plane.map { BlockPos(it) })
        }
        else -> {
            // multiple anchors, so we highlight the volume of blocks between the last anchor and where the player is looking
            val camera = client.cameraEntity!!
            val cameraRotationVec = camera.getViewVector(1.0f)
            val cameraPositionVec = camera.getViewVector(1.0f)
            val volume: List<BlockPos>

            if (ModComponents.SELECTING_VERTICAL.get(player).getValue()) {
                val selectingFar = ModComponents.SELECTING_FAR.get(player).getValue()
                var targetPos = vecIntersectWithXOrZ(cameraPositionVec, cameraRotationVec, anchors.last(), selectingFar)
                // we only want to extend the selection on the y-axis, so we clamp the x and z values to the last anchor
                targetPos = Vec3i(anchors.last().x, targetPos.y, anchors.last().z)
                ModComponents.TARGET_POSITION.get(player).setPos(BlockPos(targetPos))
                volume = getVolumeBetween(anchors, targetPos).map { BlockPos(it) }
            } else {
                val targetPos = vecIntersectWithY(cameraPositionVec, cameraRotationVec, anchors.last().y)
                ModComponents.TARGET_POSITION.get(player).setPos(BlockPos(targetPos))
                volume = getVolumeBetween(anchors, targetPos).map { BlockPos(it) }
            }
            ModComponents.CURSOR_PREVIEWS.get(player).clearPositions()
            ModComponents.CURSOR_PREVIEWS.get(player).addAllPositions(volume)
        }
    }
}

