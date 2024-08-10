package com.godlike.keybind

import com.godlike.components.ModComponents
import com.godlike.keybind.ModKeybinds.DO_SELECT
import com.godlike.keybind.ModKeybinds.TOGGLE_SELECTION_MODE
import com.godlike.keybind.ModKeybinds.TOGGLE_SELECT_FAR
import com.godlike.keybind.ModKeybinds.TOGGLE_SELECT_VERTICAL
import com.godlike.networking.DoSelectionPacket
import com.godlike.networking.ModNetworking
import com.godlike.toggleSelectionMode
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text

/**
 * Called at the top of the client tick to handle input events. This means that it is called before any other
 * keybinds are processed.
 */
fun handleModInputEvents() {
    val client = MinecraftClient.getInstance()

    while (TOGGLE_SELECTION_MODE.wasPressed()) {
        client.player!!.toggleSelectionMode()
    }

    while (TOGGLE_SELECT_VERTICAL.wasPressed()) {
        val inSelectionMode = ModComponents.SELECTION_MODE.get(client.player!!).getValue()
        if (inSelectionMode) {
            ModComponents.SELECTING_VERTICAL.get(client.player!!).toggle()
            client.player!!.sendMessage(
                Text.literal(
                    if (ModComponents.SELECTING_VERTICAL.get(client.player!!).getValue()) "Vertical selection"
                    else "Horizontal selection"
                ), false
            )
        }
    }

    while (TOGGLE_SELECT_FAR.wasPressed()) {
        val inSelectionMode = ModComponents.SELECTION_MODE.get(client.player!!).getValue()
        if (inSelectionMode) {
            ModComponents.SELECTING_FAR.get(client.player!!).toggle()
            client.player!!.sendMessage(
                Text.literal(
                    if (ModComponents.SELECTING_FAR.get(client.player!!).getValue()) "Selecting far"
                    else "Selecting near"
                ), false
            )
        }
    }

    while (DO_SELECT.wasPressed()) {
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
}