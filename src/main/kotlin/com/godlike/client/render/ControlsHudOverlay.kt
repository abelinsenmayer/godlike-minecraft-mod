package com.godlike.client.render

import com.godlike.client.keybind.ModKeybinds
import com.godlike.client.keybind.ModKeybinds.TOGGLE_TK_MODE
import com.godlike.common.components.Mode
import com.godlike.common.components.getMode
import com.godlike.common.components.selection
import com.godlike.common.components.telekinesis
import com.godlike.common.telekinesis.EntityTkTarget
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback
import net.minecraft.client.KeyMapping
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.GuiGraphics
import net.minecraft.network.chat.Component
import net.minecraft.network.chat.MutableComponent

/**
 * A HUD overlay that displays the current controls for the player.
 */
@Suppress("unused")
class ControlsHudOverlay : HudRenderCallback {
    override fun onHudRender(drawContext: GuiGraphics?, tickDelta: Float) {
        if (drawContext == null || Minecraft.getInstance().player == null) {
            return
        }
        fun pw(percent: Int): Int = drawContext.guiWidth() * percent / 100
        fun ph(percent: Int): Int = drawContext.guiHeight() * percent / 100
        val font = Minecraft.getInstance().font
        val player = Minecraft.getInstance().player!!

        val toggleTkText = keyText(TOGGLE_TK_MODE, hold = false).append(" ").append(text("key.godlike.tk_mode"))
        drawContext.drawCenteredString(font, toggleTkText, pw(15), ph(90), 0xeeeeee)

        if (player.getMode() == Mode.TELEKINESIS) {
            if (!player.selection().clientChargingLaunch && player.selection().hasRaycastTarget) {
                val pickTkText = keyText(ModKeybinds.PICK_TO_TK, hold = false).append(" ").append(text("key.godlike.pick_to_tk"))
                drawContext.drawCenteredString(font, pickTkText, pw(65), ph(10), 0xeeeeee)

                val expandSelectionText = keyText(ModKeybinds.POINTER_PUSH, hold = false).append(" ").append(text("key.godlike.expand_selection"))
                drawContext.drawCenteredString(font, expandSelectionText, pw(65), ph(15), 0xeeeeee)

                val contractSelectionText = keyText(ModKeybinds.POINTER_PULL, hold = false).append(" ").append(text("key.godlike.contract_selection"))
                drawContext.drawCenteredString(font, contractSelectionText, pw(35), ph(15), 0xeeeeee)
            } else if (player.selection().clientChargingLaunch) {
                val launchTkText = text("hud.godlike.release").append(" ").append(text("key.godlike.launch_tk"))
                drawContext.drawCenteredString(font, launchTkText, pw(35), ph(10), 0xeeeeee)
            }

            if (player.telekinesis().activeTkTarget != null) {
                val dropText = keyText(ModKeybinds.PICK_TO_TK, hold = false).append(" ").append(text("key.godlike.drop_tk"))
                drawContext.drawCenteredString(font, dropText, pw(65), ph(10), 0xeeeeee)

                val chargeLaunchText = keyText(ModKeybinds.LAUNCH_TK, hold = true).append(" ").append(text("key.godlike.charge_launch_tk"))
                drawContext.drawCenteredString(font, chargeLaunchText, pw(25), ph(10), 0xeeeeee)

                val expandSelectionText = keyText(ModKeybinds.POINTER_PUSH, hold = false).append(" ").append(text("key.godlike.push_pointer"))
                drawContext.drawCenteredString(font, expandSelectionText, pw(65), ph(15), 0xeeeeee)

                val contractSelectionText = keyText(ModKeybinds.POINTER_PULL, hold = false).append(" ").append(text("key.godlike.pull_pointer"))
                drawContext.drawCenteredString(font, contractSelectionText, pw(35), ph(15), 0xeeeeee)

                val hoverText = keyText(ModKeybinds.SET_TK_HOVERING, hold = false).append(" ").append(text("key.godlike.set_tk_hovering"))
                drawContext.drawCenteredString(font, hoverText, pw(80), ph(50), 0xeeeeee)

                if (player.telekinesis().activeTkTarget !is EntityTkTarget) {
                    val rotateText = keyText(ModKeybinds.ROTATE_TK, hold = true).append(" ").append(text("key.godlike.rotate_tk"))
                    drawContext.drawCenteredString(font, rotateText, pw(15), ph(55), 0xeeeeee)

                    val placeText = keyText(ModKeybinds.PLACE_TK, hold = false).append(" ").append(text("key.godlike.place_tk"))
                    drawContext.drawCenteredString(font, placeText, pw(80), ph(55), 0xeeeeee)
                }
            }
        }
    }

    private fun text(langKey: String): MutableComponent {
        return Component.translatable(langKey)
    }

    private fun keyText(key: KeyMapping, hold: Boolean): MutableComponent {
        val component = Component.literal("[").append(key.translatedKeyMessage).append("]")
        if (hold) {
            component.append(" ").append(Component.translatable("hud.godlike.hold"))
        }
        return component
    }
}