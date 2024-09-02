package com.godlike.client.keybind

import com.godlike.common.components.ModComponents
import com.godlike.common.networking.DoSelectionPacket
import com.godlike.common.networking.ModNetworking
import com.godlike.common.networking.TkSelectionPackage
import com.godlike.client.keybind.ModKeybinds.DO_SELECT
import com.godlike.client.keybind.ModKeybinds.TK_SELECTION
import com.godlike.client.keybind.ModKeybinds.TOGGLE_SELECTION_MODE
import com.godlike.client.keybind.ModKeybinds.TOGGLE_SELECT_FAR
import com.godlike.client.keybind.ModKeybinds.TOGGLE_SELECT_VERTICAL
import com.godlike.client.util.toggleSelectionMode
import com.godlike.common.networking.TelekinesisControlsPacket
import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer
import net.minecraft.network.chat.Component

/**
 * Called every client tick to handle keybinds related to telekinesis.
 * Constructs a [TelekinesisControlsPacket] based on key inputs and send it to the server.
 */
fun doTelekinesisKeybindControls() {
    val playerLookDirection = Minecraft.getInstance().cameraEntity!!.lookAngle

    ModNetworking.CHANNEL.clientHandle().send(
        TelekinesisControlsPacket(playerLookDirection)
    )
}

/**
 * Called at the top of the client tick to handle input events. This means that it is called before any other
 * keybinds are processed.
 */
fun handleModInputEvents() {
    val client = Minecraft.getInstance()

    while (TOGGLE_SELECTION_MODE.consumeClick()) {
        client.player!!.toggleSelectionMode()
    }

    while (TOGGLE_SELECT_VERTICAL.consumeClick()) {
        val inSelectionMode = ModComponents.SELECTION_MODE.get(client.player!!).getValue()
        if (inSelectionMode) {
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
        val inSelectionMode = ModComponents.SELECTION_MODE.get(client.player!!).getValue()
        if (inSelectionMode) {
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
        val inSelectionMode = ModComponents.SELECTION_MODE.get(client.player!!).getValue()
        if (inSelectionMode) {
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
        val inSelectionMode = ModComponents.SELECTION_MODE.get(client.player!!).getValue()
        if (inSelectionMode) {
            ModNetworking.CHANNEL.clientHandle().send(
                TkSelectionPackage()
            )
            client.player!!.toggleSelectionMode()
        }
    }
}