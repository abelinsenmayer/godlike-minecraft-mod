package com.godlike.common.items

import com.godlike.common.telekinesis.DIRT_MASS

enum class TkFocusTier(
    val massMax: Double,
    val selectionRadiusMax: Int,
    val range: Double,
) {
    BASIC(DIRT_MASS * 2, 1,15.0);
}