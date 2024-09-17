package com.godlike.client.keybind

import com.godlike.common.networking.DoSelectionPacket
import com.godlike.common.networking.TkSelectionPackage
import com.godlike.client.keybind.ModKeybinds.DO_SELECT
import com.godlike.client.keybind.ModKeybinds.LAUNCH_TK
import com.godlike.client.keybind.ModKeybinds.PICK_TO_TK
import com.godlike.client.keybind.ModKeybinds.PLACE_TK
import com.godlike.client.keybind.ModKeybinds.SET_TK_HOVERING
import com.godlike.client.keybind.ModKeybinds.TK_SELECTION
import com.godlike.client.keybind.ModKeybinds.TOGGLE_SELECTION_MODE
import com.godlike.client.keybind.ModKeybinds.TOGGLE_SELECT_FAR
import com.godlike.client.keybind.ModKeybinds.TOGGLE_SELECT_VERTICAL
import com.godlike.client.keybind.ModKeybinds.TOGGLE_TK_MODE
import com.godlike.common.components.*
import com.godlike.common.networking.DropTkPacket
import com.godlike.common.networking.HoverTkPacket
import com.godlike.common.networking.LaunchTkPacket
import com.godlike.common.networking.ModNetworking.CHANNEL
import com.godlike.common.networking.PickBlockToTkPacket
import com.godlike.common.networking.PickEntityToTkPacket
import com.godlike.common.networking.PickShipToTkPacket
import com.godlike.common.networking.PlaceTkPacket
import com.godlike.common.networking.SetChargingLaunchPacket
import com.godlike.common.networking.SetModePacket
import com.godlike.common.networking.TelekinesisControlsPacket
import com.godlike.common.telekinesis.LAUNCH_POINTER_DISTANCE
import com.godlike.common.telekinesis.getPointerAtDistance
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component
import net.minecraft.world.phys.Vec3

const val POINTER_DELTA_INCREMENT = 0.5

/**
 * Called every client tick to handle keybinds related to telekinesis.
 * Constructs a [TelekinesisControlsPacket] based on key inputs and send it to the server.
 */
fun doTelekinesisKeybindControls() {
    val playerLookDirection = Minecraft.getInstance().cameraEntity!!.lookAngle

    var pointerDistanceDelta = 0.0
    if (ModKeybinds.POINTER_PULL.isDown) {
        pointerDistanceDelta -= POINTER_DELTA_INCREMENT
    }
    if (ModKeybinds.POINTER_PUSH.isDown) {
        pointerDistanceDelta += POINTER_DELTA_INCREMENT
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
        if (currentMode == Mode.TELEKINESIS || currentMode == Mode.SELECTING) {
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
            if (player.telekinesis().getTkTargets().isEmpty() || !player.telekinesis().hasNonHoveringTarget()) {
                val selection = player.selection()
                var didPick = false
                selection.cursorTargetBlock?.let {
                    CHANNEL.clientHandle().send(PickBlockToTkPacket(it))
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
                    player.selection().doRaycast = false
                }
            } else {
                CHANNEL.clientHandle().send(DropTkPacket())
                player.selection().doRaycast = true
            }
        }
    }

    while (SET_TK_HOVERING.consumeClick() && !player.selection().clientChargingLaunch) {
        if (player.getMode() == Mode.TELEKINESIS) {
            CHANNEL.clientHandle().send(HoverTkPacket(Minecraft.getInstance().cameraEntity!!.lookAngle))
            player.selection().doRaycast = true
        }
    }

    while (PLACE_TK.consumeClick() && !player.selection().clientChargingLaunch) {
        if (player.getMode() == Mode.TELEKINESIS) {
            CHANNEL.clientHandle().send(PlaceTkPacket())
            player.selection().doRaycast = true
        }
    }

    while (TOGGLE_SELECTION_MODE.consumeClick()) {
        val currentMode = player.getMode()
        if (currentMode == Mode.SELECTING) {
            player.setMode(Mode.TELEKINESIS)
        } else if (currentMode == Mode.TELEKINESIS) {
            player.setMode(Mode.SELECTING)
        }
    }

    while (TOGGLE_SELECT_VERTICAL.consumeClick()) {
        if (player.getMode() == Mode.SELECTING) {
            ModComponents.SELECTING_VERTICAL.get(client.player!!).toggle()
            client.player!!.sendSystemMessage(
                Component.literal(
                    if (ModComponents.SELECTING_VERTICAL.get(client.player!!).getValue()) "Vertical selection"
                    else "Horizontal selection"
                )
            )
        }
    }

    while (TOGGLE_SELECT_FAR.consumeClick()) {
        if (player.getMode() == Mode.SELECTING) {
            ModComponents.SELECTING_FAR.get(client.player!!).toggle()
            client.player!!.sendSystemMessage(
                Component.literal(
                    if (ModComponents.SELECTING_FAR.get(client.player!!).getValue()) "Selecting far"
                    else "Selecting near"
                )
            )
        }
    }

    while (DO_SELECT.consumeClick()) {
        if (player.getMode() == Mode.SELECTING) {
            player.selection().cursorTargetBlock?.let {
                player.selection().selectedPositions.add(it)
            }
        }
    }

    while (TK_SELECTION.consumeClick()) {
        // send a packet to the server to create a physics object from the cursor selection
        if (player.getMode() == Mode.SELECTING) {
            CHANNEL.clientHandle().send(
                TkSelectionPackage()
            )
            client.player!!.setMode(Mode.TELEKINESIS)
        }
    }

    if (LAUNCH_TK.isDown && !player.selection().clientChargingLaunch) {
        CHANNEL.clientHandle().send(SetChargingLaunchPacket(true))
        player.selection().doRaycast = true
        player.selection().clientChargingLaunch = true
    } else if (!LAUNCH_TK.isDown && player.selection().clientChargingLaunch) {
        val launchTargetPos: Vec3 = player.selection().getSelectionPosition() ?:
            getPointerAtDistance(player, Minecraft.getInstance().cameraEntity!!.lookAngle, LAUNCH_POINTER_DISTANCE)
        CHANNEL.clientHandle().send(LaunchTkPacket(launchTargetPos))
        player.selection().doRaycast = true
        player.selection().clientChargingLaunch = false
    }
}