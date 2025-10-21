package com.godlike.common.items

import net.minecraft.ChatFormatting

enum class TkFocusTier(
    val selectionRadius: Int,
    val range: Double,
    val maxHealth: Int,
    val massScalarExponent: Double,
    val grantedPowers: Set<TkPower>,
) {
    NONE(0, 0.0, 0, 0.0, emptySet()),
    SIMPLE(1, 8.0, 10, 1.0, emptySet()),
    ELEVATED(3, 24.0, 20, 0.5, setOf(TkPower.SLOW_FALL)),
    MAGNIFICENT(9, 48.0, 100, 0.3, setOf(TkPower.SLOW_FALL, TkPower.LEVITATION)),
    SUPREME(30, 128.0, 500, 0.2, setOf(TkPower.SLOW_FALL, TkPower.FLIGHT)),
    GODLIKE(100, 256.0, 10000, 0.05, setOf(TkPower.SLOW_FALL, TkPower.FLIGHT, TkPower.ELYTRA_BOOST));

    fun getTextColor() = when (this) {
        SIMPLE -> ChatFormatting.WHITE
        ELEVATED -> ChatFormatting.DARK_GREEN
        MAGNIFICENT -> ChatFormatting.AQUA
        SUPREME -> ChatFormatting.LIGHT_PURPLE
        GODLIKE -> ChatFormatting.DARK_PURPLE
        NONE -> ChatFormatting.WHITE
    }
}

enum class TkPower {
    SLOW_FALL,
    LEVITATION,
    FLIGHT,
    ELYTRA_BOOST,
}