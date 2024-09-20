package com.godlike.client.keybind

import com.godlike.client.keybind.ModKeybinds.LAUNCH_TK
import com.godlike.client.keybind.ModKeybinds.PICK_TO_TK
import com.godlike.client.keybind.ModKeybinds.PLACE_TK
import com.godlike.client.keybind.ModKeybinds.POINTER_PULL
import com.godlike.client.keybind.ModKeybinds.POINTER_PUSH
import com.godlike.client.keybind.ModKeybinds.SET_TK_HOVERING
import com.godlike.client.keybind.ModKeybinds.TOGGLE_TK_MODE
import com.godlike.common.components.Mode
import com.godlike.common.components.getMode
import com.godlike.common.components.selection
import com.godlike.common.components.telekinesis
import com.godlike.common.networking.*
import com.godlike.common.networking.ModNetworking.CHANNEL
import com.godlike.common.telekinesis.LAUNCH_POINTER_DISTANCE
import com.godlike.common.telekinesis.getPointerAtDistance
import net.minecraft.client.Minecraft
import net.minecraft.world.phys.Vec3

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
                    CHANNEL.clientHandle().send(TkPositionsPacket(toTk))
                    didPick = true
                }
                selection.cursorTargetEntity?.let {
                    CHANNEL.clientHandle().send(PickEntityToTkPacket(it.id))
                    didPick = true
                }
                selection.cursorTargetShip?.let {
                    CHANNEL.clientHandle().send(PickShipToTkPacket(it.id))
                    didPick = true
                }
                if (didPick) {
                    player.selection().clear()
                }
            } else {
                CHANNEL.clientHandle().send(DropTkPacket())
            }
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

//    while (TOGGLE_SELECTION_MODE.consumeClick()) {
//        val currentMode = player.getMode()
//        if (currentMode == Mode.SELECTING) {
//            player.setMode(Mode.TELEKINESIS)
//            player.selection().dfsDepth = 0
//        } else if (currentMode == Mode.TELEKINESIS) {
//            player.setMode(Mode.SELECTING)
//        }
//    }

//    while (DO_SELECT.consumeClick()) {
//        if (player.getMode() == Mode.SELECTING && player.selection().selectionIsContiguous) {
//            player.selection().cursorTargetBlock?.let {
//                player.selection().selectedPositions.add(it)
//            }
//            player.selection().selectedPositions.addAll(player.selection().previewPositions)
//            player.selection().dfsDepth = 0
//        }
//    }

//    while (TK_SELECTION.consumeClick()) {
//        // send a packet to the server to create a physics object from the cursor selection
//        if (player.getMode() == Mode.SELECTING && player.selection().selectedPositions.isNotEmpty()) {
//            CHANNEL.clientHandle().send(
//                TkPositionsPacket(player.selection().previewPositions.toList())
//            )
//        }
//        player.selection().dfsDepth = 0
//    }

    if (LAUNCH_TK.isDown && !player.selection().clientChargingLaunch) {
        CHANNEL.clientHandle().send(SetChargingLaunchPacket(true))
        player.selection().clientChargingLaunch = true
    } else if (!LAUNCH_TK.isDown && player.selection().clientChargingLaunch) {
        val launchTargetPos: Vec3 = player.selection().getSelectionPosition() ?:
            getPointerAtDistance(player, Minecraft.getInstance().cameraEntity!!.lookAngle, LAUNCH_POINTER_DISTANCE)
        CHANNEL.clientHandle().send(LaunchTkPacket(launchTargetPos))
        player.selection().clientChargingLaunch = false
    }

    if (player.telekinesis().activeTkTarget == null && POINTER_PUSH.isDown) {
        player.selection().dfsDepth++
    }
    if (player.telekinesis().activeTkTarget == null && POINTER_PULL.isDown) {
        player.selection().dfsDepth--
    }
}