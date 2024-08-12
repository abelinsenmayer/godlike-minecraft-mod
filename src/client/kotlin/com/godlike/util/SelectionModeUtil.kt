package com.godlike.util

import com.godlike.components.ModComponents
import com.godlike.keybind.ModKeybinds.SELECTION_MODE_KEYBINDS
import com.godlike.mixin.client.KeyBindingMixin
import net.minecraft.client.MinecraftClient
import net.minecraft.client.network.ClientPlayerEntity
import net.minecraft.client.option.KeyBinding
import net.minecraft.text.Text
import net.minecraft.util.math.BlockPos
import net.minecraft.util.math.Vec3d

fun ClientPlayerEntity.toggleSelectionMode() {
    // set the player to selection mode
    var mode = ModComponents.SELECTION_MODE.get(this).getValue()
    mode = !mode
    ModComponents.SELECTION_MODE.get(this).setValue(mode)
    val message = if (mode) "Selection mode enabled" else "Selection mode disabled"
    this.sendMessage(Text.literal(message), false)

    if (mode) {
        // promote selection keybinds to the top of the keybind map
        val bindingMap = KeyBindingMixin.getKeyToBindings()
        SELECTION_MODE_KEYBINDS.forEach { keybind ->
            bindingMap[(keybind as KeyBindingMixin).boundKey] = keybind
        }
    } else {
        // restore the keybinds to their original positions
        KeyBinding.updateKeysByCode()
    }
}

/**
 * Called every tick on the client side when the player is in selection mode.
 * It displays a preview of the selection the player is making.
 */
fun showSelectionPreview(client: MinecraftClient) {
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
            val cameraRotationVec = camera.getRotationVec(1.0f)
            val cameraPositionVec = camera.getCameraPosVec(1.0f)
            val plane: List<BlockPos>

            if (ModComponents.SELECTING_VERTICAL.get(player).getValue()) {
                val selectingFar = ModComponents.SELECTING_FAR.get(player).getValue()
                val targetPos = vecIntersectWithXOrZ(cameraPositionVec, cameraRotationVec, anchors.last().toVec3d(), selectingFar)
                ModComponents.TARGET_POSITION.get(player).setPos(targetPos.toBlockPos())
                plane = getVerticalPlaneBetween(anchors.last().toVec3d(), targetPos).map { it.toBlockPos() }
            } else {
                val targetPos = vecIntersectWithY(cameraPositionVec, cameraRotationVec, anchors.last().y)
                ModComponents.TARGET_POSITION.get(player).setPos(targetPos.toBlockPos())
                plane = getHorizontalPlaneBetween(anchors.last().toVec3d(), targetPos).map { it.toBlockPos() }
            }
            ModComponents.CURSOR_PREVIEWS.get(player).clearPositions()
            ModComponents.CURSOR_PREVIEWS.get(player).addAllPositions(plane)
        }
        else -> {
            // multiple anchors, so we highlight the volume of blocks between the last anchor and where the player is looking
            val camera = client.cameraEntity!!
            val cameraRotationVec = camera.getRotationVec(1.0f)
            val cameraPositionVec = camera.getCameraPosVec(1.0f)
            val volume: List<BlockPos>

            if (ModComponents.SELECTING_VERTICAL.get(player).getValue()) {
                val selectingFar = ModComponents.SELECTING_FAR.get(player).getValue()
                var targetPos = vecIntersectWithXOrZ(cameraPositionVec, cameraRotationVec, anchors.last().toVec3d(), selectingFar)
                // we only want to extend the selection on the y-axis, so we clamp the x and z values to the last anchor
                targetPos = Vec3d(anchors.last().x.toDouble(), targetPos.y, anchors.last().z.toDouble())
                ModComponents.TARGET_POSITION.get(player).setPos(targetPos.toBlockPos())
                volume = getVolumeBetween(anchors.map { it.toVec3d() }, targetPos,).map { it.toBlockPos() }
            } else {
                val targetPos = vecIntersectWithY(cameraPositionVec, cameraRotationVec, anchors.last().y)
                ModComponents.TARGET_POSITION.get(player).setPos(targetPos.toBlockPos())
                volume = getVolumeBetween(anchors.map { it.toVec3d() }, targetPos).map { it.toBlockPos() }
            }
            ModComponents.CURSOR_PREVIEWS.get(player).clearPositions()
            ModComponents.CURSOR_PREVIEWS.get(player).addAllPositions(volume)
        }
    }
}

