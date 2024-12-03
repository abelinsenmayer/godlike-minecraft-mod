package com.godlike.client.keybind

import com.godlike.client.keybind.ModKeybinds.CHANGE_DFS_DISTANCE_TYPE
import com.godlike.client.keybind.ModKeybinds.LAUNCH_TK
import com.godlike.client.keybind.ModKeybinds.PICK_TO_TK
import com.godlike.client.keybind.ModKeybinds.PLACE_TK
import com.godlike.client.keybind.ModKeybinds.POINTER_PULL
import com.godlike.client.keybind.ModKeybinds.POINTER_PUSH
import com.godlike.client.keybind.ModKeybinds.SET_TK_HOVERING
import com.godlike.client.keybind.ModKeybinds.TOGGLE_TK_MODE
import com.godlike.client.util.*
import com.godlike.common.components.Mode
import com.godlike.common.components.getMode
import com.godlike.common.components.selection
import com.godlike.common.components.telekinesis
import com.godlike.common.networking.*
import com.godlike.common.networking.ModNetworking.CHANNEL
import com.godlike.common.telekinesis.EntityTkTarget
import com.godlike.common.telekinesis.LAUNCH_POINTER_DISTANCE
import com.godlike.common.telekinesis.ShipTkTarget
import com.godlike.common.telekinesis.getPointerAtDistance
import net.minecraft.client.Minecraft
import net.minecraft.world.phys.Vec3
import org.apache.commons.lang3.math.IEEE754rUtils

const val POINTER_DELTA_INCREMENT = 0.5

/**
 * Called every client tick to handle keybinds related to telekinesis.
 * Constructs a [TelekinesisControlsPacket] based on key inputs and send it to the server.
 */
fun sendTelekinesisTick() {
    val playerLookDirection = Minecraft.getInstance().cameraEntity!!.lookAngle

    var pointerDistanceDelta = 0.0
    if (Minecraft.getInstance().player!!.getMode() == Mode.TELEKINESIS) {
        // Only allow the player to change the pointer distance if they are in telekinesis mode;
        // we use these keybinds for other things in other modes.
        if (POINTER_PULL.isDown) {
            pointerDistanceDelta -= POINTER_DELTA_INCREMENT
        }
        if (POINTER_PUSH.isDown) {
            pointerDistanceDelta += POINTER_DELTA_INCREMENT
        }
    }
    val isRotating = ModKeybinds.ROTATE_TK.isDown

    CHANNEL.clientHandle().send(
        TelekinesisControlsPacket(playerLookDirection, pointerDistanceDelta, isRotating)
    )
}

/**
 * Called at the top of the client tick to handle input events. This means that it is called before any other
 * keybinds are processed.
 */
fun handleModInputEvents() {
    val client = Minecraft.getInstance()
    val player = client.player!!

    while (TOGGLE_TK_MODE.consumeClick() && !player.selection().clientChargingLaunch) {
        val currentMode = player.getMode()
        if (currentMode == Mode.TELEKINESIS) {
            CHANNEL.clientHandle().send(
                SetModePacket(Mode.NONE.name)
            )
        } else {
            CHANNEL.clientHandle().send(
                SetModePacket(Mode.TELEKINESIS.name)
            )
        }
    }

    while (PICK_TO_TK.consumeClick()) {
        if (player.getMode() == Mode.TELEKINESIS && !player.selection().clientChargingLaunch) {
            // If we are carrying something, drop it. Otherwise, pick up the block/entity/ship
            if (player.telekinesis().getTkTargets().isEmpty() || player.telekinesis().activeTkTarget == null) {
                val selection = player.selection()
                var didPick = false
                selection.cursorTargetBlock?.let {
                    val toTk = mutableListOf(it)
                    toTk.addAll(selection.previewPositions)
                    toTk.removeIf { pos -> !pos.isValidTkTargetFor(player) }
                    if (toTk.isNotEmpty()) {
                        CHANNEL.clientHandle().send(TkPositionsPacket(toTk))
                        didPick = true
                    }
                }
                selection.cursorTargetEntity?.let {
                    if (player.canTkEntity(it)) {
                        CHANNEL.clientHandle().send(PickEntityToTkPacket(it.id))
                        didPick = true
                    }
                }
                selection.cursorTargetShip?.let {
                    if (player.canTkShip(it)) {
                        CHANNEL.clientHandle().send(PickShipToTkPacket(it.id))
                        didPick = true
                    }
                }
                if (didPick) {
                    player.selection().clear()
                }
            } else {
                CHANNEL.clientHandle().send(DropTkPacket())
            }
        }
    }

    while (CHANGE_DFS_DISTANCE_TYPE.consumeClick() && !player.selection().clientChargingLaunch) {
        if (player.getMode() == Mode.TELEKINESIS) {
            val newType = when(player.selection().dfsDistanceType) {
                DfsDistanceType.CUBE -> DfsDistanceType.SPHERE
                DfsDistanceType.SPHERE -> DfsDistanceType.DIAMOND
                DfsDistanceType.DIAMOND -> DfsDistanceType.CUBE
            }
            player.selection().dfsDistanceType = newType
        }
    }

    while (SET_TK_HOVERING.consumeClick() && !player.selection().clientChargingLaunch) {
        if (player.getMode() == Mode.TELEKINESIS) {
            CHANNEL.clientHandle().send(HoverTkPacket(Minecraft.getInstance().cameraEntity!!.lookAngle))
        }
    }

    while (PLACE_TK.consumeClick() && !player.selection().clientChargingLaunch) {
        if (player.getMode() == Mode.TELEKINESIS) {
            CHANNEL.clientHandle().send(PlaceTkPacket())
        }
    }

    if (LAUNCH_TK.isDown && !player.selection().clientChargingLaunch) {
        CHANNEL.clientHandle().send(SetChargingLaunchPacket(true))
        player.selection().clientChargingLaunch = true
    } else if (!LAUNCH_TK.isDown && player.selection().clientChargingLaunch) {
        val aimingAtPosition = player.selection().getSelectionPosition()
        val aimingAtTarget = player.telekinesis().getTkTargets().find { target ->
            when(target) {
                is ShipTkTarget -> player.selection().cursorTargetShip?.id == target.shipId
                is EntityTkTarget -> player.selection().cursorTargetEntity?.id == target.entity.id
                else -> false
            }
        }
        val targetChargingLaunch = aimingAtTarget?.chargingLaunch ?: false

        // If the player is aiming at nothing or at a target charging launch, use pointer to aim
        val launchTargetPos: Vec3 = if (aimingAtPosition == null || targetChargingLaunch) {
            getPointerAtDistance(player, Minecraft.getInstance().cameraEntity!!.lookAngle, LAUNCH_POINTER_DISTANCE)
        } else {
            aimingAtPosition
        }

        CHANNEL.clientHandle().send(LaunchTkPacket(launchTargetPos))
        player.selection().clientChargingLaunch = false
    }

    while (player.telekinesis().activeTkTarget == null && POINTER_PUSH.consumeClick()) {
        player.selection().dfsDepth += 1
    }
    while (player.telekinesis().activeTkTarget == null && POINTER_PULL.consumeClick()) {
        player.selection().dfsDepth -= 1
    }
}