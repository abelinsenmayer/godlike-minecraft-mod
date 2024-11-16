package com.godlike.common.telekinesis

import com.godlike.common.Godlike.logger
import com.godlike.common.vs2.Vs2Util
import dev.onyxstudios.cca.api.v3.component.tick.ServerTickingComponent
import net.minecraft.nbt.CompoundTag
import net.minecraft.nbt.ListTag
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.level.Level

const val UNINITIALIZED_TICKER_LIMIT_SECONDS = 30

/**
 * Ticking handler for telekinesis actions that aren't specific to a controlling player.
 * Used for effecting things that should happen after a ship leaves a player's telekinetic control.
 *
 * This component is designed to be used on the server side only.
 */
class TkTicker(private val level : Level) : ServerTickingComponent {
    val tickingTargets = mutableSetOf<TkTarget>()
    private var initialized = false
    private var uninitializedTicks = 0

    override fun serverTick() {
        if (level !is ServerLevel) return
        if (initialized) {
            tickingTargets.removeIf { !it.exists() }
            tickingTargets.forEach { it.tick() }
        } else {
            // We don't want to start ticking before all of our TK target ships are loaded
            val loadedShips = Vs2Util.getServerShipWorld(level).loadedShips
            val shipsInitialized = tickingTargets.filterIsInstance<ShipTkTarget>().all { loadedShips.getById(it.shipId) != null }
            if (shipsInitialized) {
                initialized = true
            } else {
                uninitializedTicks++
                if (uninitializedTicks / 20 > UNINITIALIZED_TICKER_LIMIT_SECONDS) {
                    logger.warn("Waited $UNINITIALIZED_TICKER_LIMIT_SECONDS seconds for all TK targets to initialize;" +
                            " giving up. Some targets may be removed from tick loop.")
                    initialized = true
                }
            }
        }
    }

    override fun readFromNbt(tag: CompoundTag) {
        level !is ServerLevel && return
        if (tag.contains("ticking-targets", 9)) {
            val listTag = tag.getList("ticking-targets", 10)
            for (i in 0..<listTag.size) {
                val compound = listTag.getCompound(i)
                val target = TkTarget.fromNbtAndLevel(compound, level)
                tickingTargets.add(target)
            }
        }
    }

    /**
     * Writes the ticking targets to NBT. Entity targets are NOT saved to NBT, as it's not necessary for them to persist
     * across world reloads, and it's impractical to get the same entity in a new world.
     */
    override fun writeToNbt(tag: CompoundTag) {
        level !is ServerLevel && return
        // REMOVE entity targets; they are not persisted across world save/load
        tickingTargets.removeIf { it is EntityTkTarget }
        val listTag = ListTag()
        tickingTargets.forEach {
            listTag.add(it.toNbt())
        }
        tag.put("ticking-targets", listTag)
    }
}