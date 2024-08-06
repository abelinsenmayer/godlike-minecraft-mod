package com.godlike.keybind

import com.godlike.components.ModComponents
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import net.minecraft.text.Text
import org.lwjgl.glfw.GLFW
import org.slf4j.LoggerFactory

object ModKeybinds {
    private val logger = LoggerFactory.getLogger("godlike")

    fun registerKeybinds() {
        logger.info("Registering keybinds")

        val selectionModeKeybind = KeyBindingHelper.registerKeyBinding(
            KeyBinding(
                "key.godlike.selection_mode",
                InputUtil.Type.KEYSYM,
                GLFW.GLFW_KEY_C,
                "category.godlike"
            )
        )

        ClientTickEvents.END_CLIENT_TICK.register(ClientTickEvents.EndTick { client: MinecraftClient ->
            while (selectionModeKeybind.wasPressed()) {
                // toggle whether the player is in selection mode
                var mode = ModComponents.SELECTION_MODE.get(client.player!!).getValue()
                mode = !mode
                ModComponents.SELECTION_MODE.get(client.player!!).setValue(mode)

                val message = if (mode) "Selection mode enabled" else "Selection mode disabled"
                client.player!!.sendMessage(Text.literal(message), false)
            }
        })
    }
}