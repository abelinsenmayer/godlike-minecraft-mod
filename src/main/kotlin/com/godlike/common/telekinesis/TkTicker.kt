package com.godlike.common.telekinesis

import com.godlike.common.Godlike.logger


/**
 * Ticking handler for telekinesis actions that aren't specific to a controlling player.
 * Used for effecting things that should happen after a ship leaves a player's telekinetic control.
 * While a player is controlling a target, ticking is handled in
 */
object TkTicker {
    val tickingTargets = mutableSetOf<TkTarget>()

    fun tick() {
        tickingTargets.removeIf { !it.exists() }
        tickingTargets.forEach { it.tick() }
    }
}