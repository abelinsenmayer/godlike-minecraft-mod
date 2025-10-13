package com.godlike

import com.godlike.client.render.items.KineticCoreTrinketRenderer
import com.godlike.common.items.ModItems
import dev.emi.trinkets.api.client.TrinketRendererRegistry

object TrinketRendersInitializer {
    init {
        TrinketRendererRegistry.registerRenderer(ModItems.SIMPLE_KINETIC_CORE, KineticCoreTrinketRenderer())
        TrinketRendererRegistry.registerRenderer(ModItems.ELEVATED_KINETIC_CORE, KineticCoreTrinketRenderer())
        TrinketRendererRegistry.registerRenderer(ModItems.MAGNIFICENT_KINETIC_CORE, KineticCoreTrinketRenderer())
        TrinketRendererRegistry.registerRenderer(ModItems.SUPREME_KINETIC_CORE, KineticCoreTrinketRenderer())
        TrinketRendererRegistry.registerRenderer(ModItems.GODLIKE_KINETIC_CORE, KineticCoreTrinketRenderer())
    }
}