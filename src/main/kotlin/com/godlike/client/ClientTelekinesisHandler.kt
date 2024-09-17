package com.godlike.client

import com.godlike.client.keybind.doTelekinesisKeybindControls
import com.godlike.client.util.selectRaycastTarget
import com.godlike.common.Godlike.logger
import com.godlike.common.components.Mode
import com.godlike.common.components.getMode
import com.godlike.common.components.selection
import net.minecraft.client.player.LocalPlayer

/**
 * Handles all client side actions that need to happen every tick for telekinesis "stuff".
 */
fun clientTelekinesisTick(player : LocalPlayer) {
    if (player.selection().doRaycast) {
        selectRaycastTarget()
    }
    if (player.getMode() == Mode.TELEKINESIS) {
        doTelekinesisKeybindControls()
    }
}