package com.godlike.client.keybind

import com.godlike.common.networking.DoSelectionPacket
import com.godlike.common.networking.ModNetworking
import com.godlike.common.networking.TkSelectionPackage
import com.godlike.client.keybind.ModKeybinds.DO_SELECT
import com.godlike.client.keybind.ModKeybinds.TK_SELECTION
import com.godlike.client.keybind.ModKeybinds.TOGGLE_SELECTION_MODE
import com.godlike.client.keybind.ModKeybinds.TOGGLE_SELECT_FAR
import com.godlike.client.keybind.ModKeybinds.TOGGLE_SELECT_VERTICAL
import com.godlike.client.keybind.ModKeybinds.TOGGLE_TK_MODE
import com.godlike.common.components.*
import com.godlike.common.networking.ModNetworking.CHANNEL
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

    ModNetworking.CHANNEL.clientHandle().send(
        TelekinesisControlsPacket(playerLookDirection, pointerDistanceDelta)
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
            ModNetworking.CHANNEL.clientHandle().send(
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
            ModNetworking.CHANNEL.clientHandle().send(
                TkSelectionPackage()
            )
            client.player!!.setMode(Mode.TELEKINESIS)
        }
    }
}