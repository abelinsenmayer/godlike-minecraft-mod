package com.godlike.common.components

import com.godlike.client.keybind.ModKeybinds
import com.godlike.client.mixin.KeyBindingMixin
import com.godlike.common.telekinesis.dropTk
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent
import net.minecraft.client.KeyMapping
import net.minecraft.client.player.LocalPlayer
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player

const val MODE_KEY = "mode"

/**
 * Stores the player's current "mode". Used for promoting keybinds and applying other logic conditionally.
 */
class ModeComponent(private val player : Player) : AutoSyncedComponent {
    private var _mode : Mode = Mode.NONE
    var mode : Mode
        get() = _mode
        set(value) {
            _mode = value
            ModEntityComponents.MODE.sync(player)
            if (player is LocalPlayer) {
                // player.sendSystemMessage(Component.literal("Mode set to $value"))
                this.mode.setKeybindsForMode()
            }
            if (value != Mode.SELECTING) {
                ModEntityComponents.CURSOR_PREVIEWS.get(player).clearPositions()
                ModEntityComponents.TARGET_POSITION.get(player).setPos(BlockPos(0, -3000, 0))
                if (player is LocalPlayer) {
                    player.selection().clear()
                }
            }
            if (value != Mode.TELEKINESIS && value != Mode.PLACEMENT && player is ServerPlayer) {
                dropTk(player)
            }
        }

    override fun shouldSyncWith(player: ServerPlayer?): Boolean {
        // we only want to sync this data with the player that owns it
        // this reduces network traffic and prevents other players from seeing the cursor positions
        return player == this.player
    }

    override fun readFromNbt(tag: CompoundTag) {
        this.mode = Mode.valueOf(tag.getString(MODE_KEY))
    }

    override fun writeToNbt(tag: CompoundTag) {
        tag.putString(MODE_KEY, mode.name)
    }
}

fun Player.setMode(mode: Mode) {
    ModEntityComponents.MODE.get(this).mode = mode
}

fun Player.getMode(): Mode {
    return ModEntityComponents.MODE.get(this).mode
}

enum class Mode(private val keybinds: List<KeyMapping>) {
    NONE(emptyList()),
    SELECTING(listOf(
        ModKeybinds.POINTER_PULL,
        ModKeybinds.POINTER_PUSH,
    )),
    TELEKINESIS(listOf(
        ModKeybinds.POINTER_PULL,
        ModKeybinds.POINTER_PUSH,
        ModKeybinds.PICK_TO_TK,
        ModKeybinds.ROTATE_TK,
        ModKeybinds.SET_TK_HOVERING,
        ModKeybinds.LAUNCH_TK,
        ModKeybinds.CHANGE_DFS_DISTANCE_TYPE,
        ModKeybinds.TOGGLE_PLACEMENT_MODE,
        ModKeybinds.PLACE_TK,
    )),
    PLACEMENT(listOf(
        ModKeybinds.POINTER_PULL,
        ModKeybinds.POINTER_PUSH,
        ModKeybinds.TOGGLE_PLACEMENT_MODE,
        ModKeybinds.PLACE_TK,
    ));

    /**
     * Promotes keybinds relevant to this mode to the top of the keybind map. Keybinds not relevant to the current mode
     * are removed from the map.
     */
    fun setKeybindsForMode() {
        // Remove keybinds from modes other than the active one, so they'll never be set
        val allBindings = KeyBindingMixin.getAllBindings()
        entries.forEach { mode ->
            if (mode != this) {
                mode.keybinds.forEach { keybind ->
                    allBindings.remove(keybind.name)
                }
            }
        }

        // Add this mode's keybinds to the active bindings
        val activeBindings = KeyBindingMixin.getKeyToBindings()
        KeyMapping.resetMapping()
        if (this.keybinds.isNotEmpty()) {
            this.keybinds.forEach { keybind ->
                activeBindings[(keybind as KeyBindingMixin).boundKey] = keybind
            }
        }
    }
}