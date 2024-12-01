package com.godlike.common.items

enum class TkFocusTier(
    val selectionRadius: Int,
    val range: Double,
) {
    SIMPLE(1,8.0),
    ELEVATED(5, 24.0),
    MAGNIFICENT(15, 48.0),
    SUPREME(30, 128.0),
    GODLIKE(1000, 256.0)
}