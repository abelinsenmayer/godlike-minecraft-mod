package com.godlike.common.components

import com.godlike.common.telekinesis.EntityTkTarget
import com.godlike.common.telekinesis.ShipTkTarget
import com.godlike.common.telekinesis.TkTarget
import com.godlike.common.telekinesis.TkTicker
import dev.onyxstudios.cca.api.v3.component.sync.AutoSyncedComponent
import net.minecraft.client.player.LocalPlayer
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.player.Player

const val TK_TARGETS_KEY = "telekinesis-targets"
const val POINTER_DISTANCE_KEY = "pointer-distance"

/**
 * A component that stores data about a player's telekinesis targets and settings.
 * Designed to be used exclusively on the server side. If a client-side action should be taken based on this data, the
 * server should send a packet.
 */
class TelekinesisComponent(private val player: Player) : AutoSyncedComponent {
    private val tkTargets : MutableList<TkTarget> = mutableListOf()
    var pointerDistance : Double = 0.0
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
}

fun Player.telekinesis(): TelekinesisComponent {
    return ModEntityComponents.TELEKINESIS_DATA.get(this)
}