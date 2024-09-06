package com.godlike.common.components

import com.godlike.client.keybind.ModKeybinds
import com.godlike.client.mixin.KeyBindingMixin
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent
import net.minecraft.client.KeyMapping
import net.minecraft.client.player.LocalPlayer
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.network.chat.Component
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
            ModComponents.MODE.sync(player)
            if (player is LocalPlayer) {
                player.sendSystemMessage(Component.literal("Mode set to $value"))
                this.mode.promoteKeybindsForMode()
            }
            if (value != Mode.SELECTING) {
                ModComponents.CURSOR_PREVIEWS.get(player).clearPositions()
                ModComponents.TARGET_POSITION.get(player).setPos(BlockPos(0, -3000, 0))
            }
            if (value != Mode.TELEKINESIS) {
                player.telekinesis().tkShipIds.clear()
                if (player is LocalPlayer) {
                    player.selection().clear()
                }
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
    ModComponents.MODE.get(this).mode = mode
}

fun Player.getMode(): Mode {
    return ModComponents.MODE.get(this).mode
}

enum class Mode(val keybinds: List<KeyMapping>) {
    NONE(emptyList()),
    SELECTING(listOf(
        ModKeybinds.TOGGLE_SELECT_VERTICAL,
        ModKeybinds.DO_SELECT,
        ModKeybinds.TOGGLE_SELECT_FAR,
        ModKeybinds.TK_SELECTION,
    )),
    TELEKINESIS(listOf(
        ModKeybinds.POINTER_PULL,
        ModKeybinds.POINTER_PUSH,
    ));

    /**
     * Promotes keybinds relevant to this mode to the top of the keybind map.
     */
    fun promoteKeybindsForMode() {
        val bindingMap = KeyBindingMixin.getKeyToBindings()
        KeyMapping.resetMapping()
        if (this.keybinds.isNotEmpty()) {
            this.keybinds.forEach { keybind ->
                bindingMap[(keybind as KeyBindingMixin).boundKey] = keybind
            }
        }
    }
}