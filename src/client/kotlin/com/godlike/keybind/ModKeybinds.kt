package com.godlike.keybind

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.MinecraftClient
import net.minecraft.client.option.KeyBinding
import net.minecraft.client.util.InputUtil
import org.lwjgl.glfw.GLFW
import org.slf4j.LoggerFactory

object ModKeybinds {
    private val logger = LoggerFactory.getLogger("godlike")

    val TOGGLE_SELECTION_MODE: KeyBinding = KeyBindingHelper.registerKeyBinding(
        KeyBinding(
            "key.godlike.selection_mode",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_C,
            "category.godlike"
        )
    )

    val TOGGLE_SELECT_DIRECTION: KeyBinding = KeyBindingHelper.registerKeyBinding(
        KeyBinding(
            "key.godlike.toggle_select_direction",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            "category.godlike"
        )
    )

    val DO_SELECT: KeyBinding = KeyBindingHelper.registerKeyBinding(
        KeyBinding(
            "key.godlike.do_select",
            InputUtil.Type.MOUSE,
            GLFW.GLFW_MOUSE_BUTTON_RIGHT,
            "category.godlike"
        )
    )

    fun registerKeybinds() {
        logger.info("Registering keybinds")
        // noop, just ensures static initializer is run
    }
}