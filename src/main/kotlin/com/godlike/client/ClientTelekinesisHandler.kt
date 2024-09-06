package com.godlike.client

import com.godlike.client.keybind.doTelekinesisKeybindControls
import com.godlike.client.util.selectRaycastTarget
import com.godlike.common.Godlike.logger
import com.godlike.common.components.telekinesis
import net.minecraft.client.player.LocalPlayer

/**
 * Handles all client side actions that need to happen every tick when the player is in telekinesis mode.
 */
fun clientTelekinesisTick(player : LocalPlayer) {
    if (player.telekinesis().tkShipIds.isEmpty()) {
        selectRaycastTarget()
    } else {
        doTelekinesisKeybindControls()
    }
}