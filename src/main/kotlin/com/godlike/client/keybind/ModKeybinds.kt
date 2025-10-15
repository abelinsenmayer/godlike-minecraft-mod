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

    val PICK_TO_TK: KeyMapping = KeyBindingHelper.registerKeyBinding(
        KeyMapping(
            "key.godlike.pick_to_tk",
            InputConstants.Type.MOUSE,
            GLFW.GLFW_MOUSE_BUTTON_RIGHT,
            "category.godlike"
        )
    )

    val PLACE_TK: KeyMapping = KeyBindingHelper.registerKeyBinding(
        KeyMapping(
            "key.godlike.place_tk",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_G,
            "category.godlike"
        )
    )

    val ROTATE_TK_LEFT: KeyMapping = KeyBindingHelper.registerKeyBinding(
        KeyMapping(
            "key.godlike.rotate_tk_left",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_LEFT,
            "category.godlike"
        )
    )

    val ROTATE_TK_RIGHT: KeyMapping = KeyBindingHelper.registerKeyBinding(
        KeyMapping(
            "key.godlike.rotate_tk_right",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_RIGHT,
            "category.godlike"
        )
    )

    val ROTATE_TK_UP: KeyMapping = KeyBindingHelper.registerKeyBinding(
        KeyMapping(
            "key.godlike.rotate_tk_up",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_UP,
            "category.godlike"
        )
    )

    val ROTATE_TK_DOWN: KeyMapping = KeyBindingHelper.registerKeyBinding(
        KeyMapping(
            "key.godlike.rotate_tk_down",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_DOWN,
            "category.godlike"
        )
    )

    val LAUNCH_TK: KeyMapping = KeyBindingHelper.registerKeyBinding(
        KeyMapping(
            "key.godlike.launch_tk",
            InputConstants.Type.MOUSE,
            GLFW.GLFW_MOUSE_BUTTON_LEFT,
            "category.godlike"
        )
    )

    val SET_TK_HOVERING: KeyMapping = KeyBindingHelper.registerKeyBinding(
        KeyMapping(
            "key.godlike.set_tk_hovering",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_H,
            "category.godlike"
        )
    )

//    val UNSTICK_TK: KeyMapping = KeyBindingHelper.registerKeyBinding(
//        KeyMapping(
//            "key.godlike.unstick_tk",
//            InputConstants.Type.KEYSYM,
//            GLFW.GLFW_KEY_B,
//            "category.godlike"
//        )
//    )

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

    val CHANGE_DFS_DISTANCE_TYPE: KeyMapping = KeyBindingHelper.registerKeyBinding(
        KeyMapping(
            "key.godlike.change_dfs_distance_type",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_V,
            "category.godlike"
        )
    )

    val TOGGLE_PLACEMENT_MODE: KeyMapping = KeyBindingHelper.registerKeyBinding(
        KeyMapping(
            "key.godlike.toggle_placement_mode",
            InputConstants.Type.KEYSYM,
            GLFW.GLFW_KEY_T,
            "category.godlike"
        )
    )

    fun registerKeybinds() {
        logger.info("Registering keybinds")
        // noop, just ensures static initializer is run
    }
}