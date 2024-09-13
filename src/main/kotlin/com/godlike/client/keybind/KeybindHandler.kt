package com.godlike.client.keybind

import com.godlike.common.networking.DoSelectionPacket
import com.godlike.common.networking.TkSelectionPackage
import com.godlike.client.keybind.ModKeybinds.DO_SELECT
import com.godlike.client.keybind.ModKeybinds.PICK_TO_TK
import com.godlike.client.keybind.ModKeybinds.PLACE_TK
import com.godlike.client.keybind.ModKeybinds.SET_TK_HOVERING
import com.godlike.client.keybind.ModKeybinds.TK_SELECTION
import com.godlike.client.keybind.ModKeybinds.TOGGLE_SELECT_FAR
import com.godlike.client.keybind.ModKeybinds.TOGGLE_SELECT_VERTICAL
import com.godlike.client.keybind.ModKeybinds.TOGGLE_TK_MODE
import com.godlike.common.Godlike.logger
import com.godlike.common.components.*
import com.godlike.common.networking.DropTkPacket
import com.godlike.common.networking.HoverTkPacket
import com.godlike.common.networking.ModNetworking.CHANNEL
import com.godlike.common.networking.PickBlockToTkPacket
import com.godlike.common.networking.PickEntityToTkPacket
import com.godlike.common.networking.PickShipToTkPacket
import com.godlike.common.networking.PlaceTkPacket
import com.godlike.common.networking.SetModePacket
import com.godlike.common.networking.TelekinesisControlsPacket
import net.minecraft.client.Minecraft
import net.minecraft.network.chat.Component

const val POINTER_DELTA_INCREMENT = 0.2

/**
 * Called every client tick to handle keybinds related to telekinesis.
 * Constructs a [TelekinesisControlsPacket] based on key inputs and send it to the server.
 */
fun doTelekinesisKeybindControls() {
    val playerLookDirection = Minecraft.getInstance().cameraEntity!!.lookAngle

    var pointerDistanceDelta = 0.0
    while (ModKeybinds.POINTER_PULL.consumeClick()) {
        pointerDistanceDelta -= POINTER_DELTA_INCREMENT
    }
    while (ModKeybinds.POINTER_PUSH.consumeClick()) {
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

    while (TOGGLE_TK_MODE.consumeClick()) {
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
        if (player.getMode() == Mode.TELEKINESIS) {
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
                    player.selection().isSelecting = false
                }
            } else {
                CHANNEL.clientHandle().send(DropTkPacket())
                player.selection().isSelecting = true
            }
        }
    }

    while (SET_TK_HOVERING.consumeClick()) {
        if (player.getMode() == Mode.TELEKINESIS) {
            CHANNEL.clientHandle().send(HoverTkPacket(Minecraft.getInstance().cameraEntity!!.lookAngle))
            player.selection().isSelecting = true
        }
    }

    while (PLACE_TK.consumeClick()) {
        if (player.getMode() == Mode.TELEKINESIS) {
            CHANNEL.clientHandle().send(PlaceTkPacket())
            player.selection().isSelecting = true
        }
    }

//    while (TOGGLE_SELECTION_MODE.consumeClick()) {
//        val currentMode = player.getMode()
//        if (currentMode == Mode.SELECTING) {
//            player.setMode(Mode.NONE)
//        } else {
//            player.setMode(Mode.SELECTING)
//        }
//    }

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
        // send a packet to the server to add the preview to their cursor selection
        if (player.getMode() == Mode.SELECTING) {
            CHANNEL.clientHandle().send(
                DoSelectionPacket(
                    ModComponents.CURSOR_PREVIEWS.get(client.player!!).getPositions(),
                    ModComponents.TARGET_POSITION.get(client.player!!).getPos()
                )
            )
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
}