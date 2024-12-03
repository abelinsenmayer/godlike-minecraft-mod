package com.godlike.common.items

enum class TkFocusTier(
    val selectionRadius: Int,
    val range: Double,
    val maxHealth: Int,
) {
    SIMPLE(1,8.0, 10),
    ELEVATED(3, 24.0, 20),
    MAGNIFICENT(9, 48.0, 100),
    SUPREME(30, 128.0, 500),
    GODLIKE(1000, 256.0, 10000)
}