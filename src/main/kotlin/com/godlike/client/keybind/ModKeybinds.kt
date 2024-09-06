package com.godlike.client.keybind

import com.mojang.blaze3d.platform.InputConstants
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper
import net.minecraft.client.KeyMapping
import org.lwjgl.glfw.GLFW
import org.slf4j.LoggerFactory

object ModKeybinds {
    private val logger = LoggerFactory.getLogger("godlike")

    val TOGGLE_TK_MODE: KeyMapping = KeyBindingHelper.registerKeyBinding(
        KeyMapping(
            "key.godlike.tk_mode",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_C,
            "category.godlike"
        )
    )

    val TOGGLE_SELECTION_MODE: KeyMapping = KeyBindingHelper.registerKeyBinding(
        KeyMapping(
            "key.godlike.selection_mode",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_S,
            "category.godlike"
        )
    )

    val TOGGLE_SELECT_VERTICAL: KeyMapping = KeyBindingHelper.registerKeyBinding(
        KeyMapping(
            "key.godlike.toggle_select_direction",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            "category.godlike"
        )
    )

    val TOGGLE_SELECT_FAR: KeyMapping = KeyBindingHelper.registerKeyBinding(
        KeyMapping(
            "key.godlike.toggle_select_depth",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_B,
            "category.godlike"
        )
    )

    val DO_SELECT: KeyMapping = KeyBindingHelper.registerKeyBinding(
        KeyMapping(
            "key.godlike.do_select",
            InputConstants.Type.MOUSE,
            GLFW.GLFW_MOUSE_BUTTON_RIGHT,
            "category.godlike"
        )
    )

    val TK_SELECTION: KeyMapping = KeyBindingHelper.registerKeyBinding(
        KeyMapping(
            "key.godlike.tk_selection",
            InputConstants.Type.MOUSE,
            GLFW.GLFW_MOUSE_BUTTON_LEFT,
            "category.godlike"
        )
    )

    val POINTER_PUSH: KeyMapping = KeyBindingHelper.registerKeyBinding(
        KeyMapping(
            "key.godlike.pointer_away",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_E,
            "category.godlike"
        )
    )

    val POINTER_PULL: KeyMapping = KeyBindingHelper.registerKeyBinding(
        KeyMapping(
            "key.godlike.pointer_towards",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_Q,
            "category.godlike"
        )
    )

    fun registerKeybinds() {
        logger.info("Registering keybinds")
        // noop, just ensures static initializer is run
    }
}