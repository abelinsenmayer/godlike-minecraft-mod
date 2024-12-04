package com.godlike.common.items

enum class TkFocusTier(
    val selectionRadius: Int,
    val range: Double,
    val maxHealth: Int,
    val massScalarExponent: Double,
) {
    SIMPLE(1,8.0, 10, 1.0),
    ELEVATED(3, 24.0, 20, 0.5),
    MAGNIFICENT(9, 48.0, 100, 0.3),
    SUPREME(30, 128.0, 500, 0.2),
    GODLIKE(100, 256.0, 10000, 0.05)
}