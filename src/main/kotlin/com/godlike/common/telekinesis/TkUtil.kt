package com.godlike.common.telekinesis

import com.godlike.common.components.Mode
import com.godlike.common.components.getMode
import com.godlike.common.components.setMode
import com.godlike.common.components.telekinesis
import com.godlike.common.items.KineticCoreItem
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
    // Determine what TK items the player has equipped
    val mainHandItem = this.getItemBySlot(EquipmentSlot.MAINHAND).item
    val equippedStaff = if (mainHandItem is TkStaffItem) {
        mainHandItem
    } else {
        null
    }
    val equippedTkCore: KineticCoreItem? = TrinketsApi.getTrinketComponent(this).getOrNull()?.allEquipped?.firstOrNull { it.b.item is KineticCoreItem }?.b?.item as KineticCoreItem?

    fun setTierIfNecessary(tier: TkFocusTier) {
        if (tier != this.telekinesis().tier) {
            // Clear the player's current TK targets if they switch to a focus of a different tier
            this.telekinesis().clearTargets()
            this.setMode(Mode.NONE)

            // Apply item's constraints on player's TK abilities
            this.telekinesis().tier = tier
            ModNetworking.CHANNEL.serverHandle(this).send(ResetDfsDepthPacket())
        }
    }

    // Determine the player's TK tier and mode based on their equipped items
    if (equippedTkCore != null) {
        // If the player has a telekinetic core equipped, it overrides any staff they might be holding
        setTierIfNecessary(equippedTkCore.tier)
    } else if (equippedStaff != null) {
        // If the player has no core and is holding a staff, set them to tk mode
        if (this.getMode() == Mode.NONE) {
            this.setMode(Mode.TELEKINESIS)
        }
        setTierIfNecessary(equippedStaff.tier)
    } else {
        // If the player has no core and is not holding a staff, take them out of TK mode
        if (this.getMode() == Mode.TELEKINESIS) {
            this.setMode(Mode.NONE)
        }
        this.telekinesis().tier = TkFocusTier.NONE
    }
}

/**
 * Returns true if the player should animate as though they are controlling a TK object.
 */
fun Player.shouldAnimateTk(): Boolean {
    return this.getMode() == Mode.TELEKINESIS && (this.telekinesis().activeTkTarget != null
            || this.telekinesis().getTkTargets().any { it.chargingLaunch })
}