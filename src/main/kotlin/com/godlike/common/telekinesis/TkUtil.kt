package com.godlike.common.telekinesis

import com.godlike.common.components.Mode
import com.godlike.common.components.getMode
import com.godlike.common.components.setMode
import com.godlike.common.components.telekinesis
import com.godlike.common.items.KineticCoreItem
import com.godlike.common.items.TieredTkItem
import com.godlike.common.items.TkFocusTier
import com.godlike.common.items.TkStaffItem
import com.godlike.common.networking.ModNetworking
import com.godlike.common.networking.ResetDfsDepthPacket
import dev.emi.trinkets.api.TrinketsApi
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.player.Player
import kotlin.jvm.optionals.getOrNull

/**
 * Called every tick to update the player's TK tier based on their equipped items.
 */
fun Player.updateTkTierByInventory() {
    // Determine the player's TK tier based on their equipped TK items
    // Determine which item will control the player's TK abilities. Staff takes precedence over core.
    val mainHandItem = this.getItemBySlot(EquipmentSlot.MAINHAND).item
    val equippedStaff = if (mainHandItem is TkStaffItem) {
        mainHandItem
    } else {
        null
    }
    val equippedTkCore: KineticCoreItem? = TrinketsApi.getTrinketComponent(this).getOrNull()?.allEquipped?.firstOrNull { it.b.item is KineticCoreItem }?.b?.item as KineticCoreItem?

    val tkItem: TieredTkItem? = (equippedStaff ?: equippedTkCore)

    if (tkItem == null) {
        // No TK item equipped, this shouldn't happen since we only enter TK mode when equipping a TK item
        if (this.getMode() == Mode.TELEKINESIS) {
            this.setMode(Mode.NONE)
        }
        this.telekinesis().tier = TkFocusTier.NONE
        return
    }

    // Handle the case where the player's tier changed
    if (tkItem.tier != this.telekinesis().tier) {
        // Clear the player's current TK targets if they switch to a focus of a different tier
        this.telekinesis().clearTargets()
        this.setMode(Mode.NONE)

        // Apply item's constraints on player's TK abilities
        this.telekinesis().tier = tkItem.tier
        ModNetworking.CHANNEL.serverHandle(this).send(ResetDfsDepthPacket())
    }
}

/**
 * Returns true if the player should animate as though they are controlling a TK object.
 */
fun Player.shouldAnimateTk(): Boolean {
    return this.getMode() == Mode.TELEKINESIS && (this.telekinesis().activeTkTarget != null
            || this.telekinesis().getTkTargets().any { it.chargingLaunch })
}