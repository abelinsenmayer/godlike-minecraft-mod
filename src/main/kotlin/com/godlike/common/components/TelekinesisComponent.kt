package com.godlike.common.components

import com.godlike.common.items.*
import com.godlike.common.networking.ModNetworking
import com.godlike.common.networking.ResetDfsDepthPacket
import com.godlike.common.telekinesis.EntityTkTarget
import com.godlike.common.telekinesis.ShipTkTarget
import com.godlike.common.telekinesis.TkTarget
import dev.emi.trinkets.api.TrinketsApi
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent
import net.minecraft.client.player.LocalPlayer
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EquipmentSlot
import net.minecraft.world.entity.player.Player
import kotlin.jvm.optionals.getOrNull

const val TK_TARGETS_KEY = "telekinesis-targets"
const val POINTER_DISTANCE_KEY = "pointer-distance"

/**
 * A component that stores data about a player's telekinesis targets and settings.
 * This component is used on the client and server side, but be mindful of what data you read/write on each side as some
 * properties have assumptions about which side their accessed on.
 */
class TelekinesisComponent(private val player: Player) : AutoSyncedComponent {
    private val tkTargets : MutableList<TkTarget> = mutableListOf()
    var pointerDistance : Double = 0.0
        set(value) {
            field = value
            sync()
        }
    var activeTkTarget : TkTarget? = null
        set(value) {
            if (!player.level().isClientSide) {
                // if we already have an active target, make it hover and promote our new target to active
                if (value != null && field != null && field!!.hoverPos == null) {
                    field!!.hoverPos = field!!.pos()
                }
            } else if (value != null) {
                (player as LocalPlayer).selection().clear()
            }
            field = value
            sync()
        }
    var tier: TkFocusTier = TkFocusTier.SIMPLE
        set(value) {
            field = value
            sync()
        }

    override fun shouldSyncWith(player: ServerPlayer?): Boolean {
        // we only want to sync this data with the player that owns it
        // this reduces network traffic and prevents other players from seeing the cursor positions
        return player == this.player
    }

    override fun readFromNbt(tag: CompoundTag) {
        tkTargets.clear()
        pointerDistance = tag.getDouble(POINTER_DISTANCE_KEY)
        if (tag.contains(TK_TARGETS_KEY, 9)) {
            val listTag = tag.getList(TK_TARGETS_KEY, 10)
            for (i in 0..<listTag.size) {
                val compound = listTag.getCompound(i)
                val target = TkTarget.fromNbtAndPlayer(compound, player)
                tkTargets.add(target)
            }
        }
        if (tag.contains("activeTkTarget")) {
            activeTkTarget = TkTarget.fromNbtAndPlayer(tag.getCompound("activeTkTarget"), player)
        } else {
            activeTkTarget = null
        }
        tier = tag.getString("tier").let {
            if (it == null || it.isEmpty()) {
                return@let TkFocusTier.SIMPLE
            }
            TkFocusTier.valueOf(it)
        }
        sync()
    }

    override fun writeToNbt(tag: CompoundTag) {
        tag.putDouble(POINTER_DISTANCE_KEY, pointerDistance)
        val listTag = ListTag()
        tkTargets.forEach {
            listTag.add(it.toNbt())
        }
        tag.put(TK_TARGETS_KEY, listTag)
        activeTkTarget?.let { tag.put("activeTkTarget", it.toNbt()) }
        tag.putString("tier", tier.name)
    }

    private fun sync() {
        if (player is ServerPlayer) {
            ModEntityComponents.TELEKINESIS_DATA.sync(player)
        }
    }

    fun getTkTargets(): List<TkTarget> {
        return tkTargets.toList()
    }

    /**
     * Adds a target to the player's telekinesis targets and promotes it to the active target.
     * Also adds the target TkTicker's tickingTargets set.
     */
    fun addTarget(target: TkTarget) {
        if (target is ShipTkTarget) {
            // Set ship TK target to place as block after a delay
            target.disassemblyTickCountdown = -1
        }
        val existing = tkTargets.find { it == target }
        if (existing != null) {
            target.hoverPos = null
        } else {
            tkTargets.add(target)
        }
        activeTkTarget = target
    }

    fun removeTarget(target: TkTarget) {
        if (target is ShipTkTarget) {
            // Set ship TK target to place as block after a delay
            target.disassemblyTickCountdown = 200
        }
        if (activeTkTarget == target) {
            activeTkTarget = null
        }
        target.hoverPos = null
        target.player = null
        tkTargets.remove(target)
        sync()
    }

    fun removeTargetsWhere(predicate: (TkTarget) -> Boolean) {
        tkTargets.filter { predicate(it) }.forEach { removeTarget(it) }
    }

    fun addShipIdAsTarget(id: Long) {
        val existing = tkTargets.find { it is ShipTkTarget && it.ship.id == id }
            ?: player.level().getTkTicker().tickingTargets.find { it is ShipTkTarget && it.ship.id == id }
        if (existing != null) {
            existing.player = player
            addTarget(existing)
        } else {
            addTarget(ShipTkTarget(player.level(), player, id))
        }
    }

    fun addEntityAsTkTarget(entity: Entity) {
        val existing = tkTargets.find { it is EntityTkTarget && it.entity == entity }
        if (existing != null) {
            addTarget(existing)
        } else {
            addTarget(EntityTkTarget(player.level(), player, entity.id))
        }
    }

    fun removeShipIdAsTarget(id: Long) {
        tkTargets.filter { it is ShipTkTarget && it.shipId == id }.forEach { removeTarget(it) }
        sync()
    }

    fun clearTargets() {
        val toRemove = tkTargets.toList()
        toRemove.forEach { removeTarget(it) }
        activeTkTarget?.let { removeTarget(it) }
        sync()
    }
}

fun Player.telekinesis(): TelekinesisComponent {
    return ModEntityComponents.TELEKINESIS_DATA.get(this)
}

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