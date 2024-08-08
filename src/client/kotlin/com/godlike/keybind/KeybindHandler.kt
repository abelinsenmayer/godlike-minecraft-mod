package com.godlike.keybind

import com.godlike.Godlike.logger
import com.godlike.components.ModComponents
import com.godlike.keybind.ModKeybinds.DO_SELECT
import com.godlike.keybind.ModKeybinds.TOGGLE_SELECTION_MODE
import com.godlike.keybind.ModKeybinds.TOGGLE_SELECT_DIRECTION
import com.godlike.networking.DoSelectionPacket
import com.godlike.networking.ModNetworking
import net.minecraft.client.MinecraftClient
import net.minecraft.text.Text
import org.lwjgl.glfw.GLFW

/**
 * Called at the top of the client tick to handle input events. This means that it is called before any other
 * keybinds are processed.
 */
fun handleModInputEvents() {
    val client = MinecraftClient.getInstance()

    while (TOGGLE_SELECTION_MODE.wasPressed()) {
        var mode = ModComponents.SELECTION_MODE.get(client.player!!).getValue()
        mode = !mode
        ModComponents.SELECTION_MODE.get(client.player!!).setValue(mode)

        val message = if (mode) "Selection mode enabled" else "Selection mode disabled"
        client.player!!.sendMessage(Text.literal(message), false)
    }

    while (TOGGLE_SELECT_DIRECTION.wasPressed()) {
        val inSelectionMode = ModComponents.SELECTION_MODE.get(client.player!!).getValue()
        if (inSelectionMode) {
            ModComponents.SELECTION_DIRECTION.get(client.player!!).toggle()
            client.player!!.sendMessage(
                Text.literal(
                    if (ModComponents.SELECTION_DIRECTION.get(client.player!!).getValue()) "Vertical selection"
                    else "Horizontal selection"
                ), false
            )
        }
    }

    while (DO_SELECT.wasPressed()) {
        logger.info("Do select key pressed")
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