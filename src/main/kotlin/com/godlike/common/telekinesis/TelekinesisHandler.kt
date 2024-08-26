package com.godlike.common.telekinesis

import com.godlike.common.Godlike.logger
import com.godlike.common.components.ModComponents
import com.godlike.common.networking.TelekinesisControlsPacket
import com.godlike.common.vs2.Vs2Util
import net.minecraft.server.level.ServerPlayer
import org.joml.Vector3d
import org.valkyrienskies.core.api.ships.saveAttachment
import org.valkyrienskies.mod.common.util.GameTickForceApplier

fun physicsObjectFromSelection(player: ServerPlayer) {
    val cursors = ModComponents.CURSORS.get(player).getPositions()
    if (cursors.isEmpty()) {
        return
    }
    val ship = Vs2Util.createShip(cursors, player.serverLevel())
    ship.saveAttachment(GameTickForceApplier())
    ModComponents.TELEKINESIS_DATA.get(player).tkTargets.clear()
    ModComponents.TELEKINESIS_DATA.get(player).tkTargets.add(ship)
}

fun handleTelekinesisControls(telekinesisControls: TelekinesisControlsPacket, player: ServerPlayer) {
    val tkData = ModComponents.TELEKINESIS_DATA.get(player)
    tkData.tkTargets.forEach { ship ->
        val forceApplier = ship.getAttachment(GameTickForceApplier::class.java)
        forceApplier?.applyInvariantTorque(Vector3d(0.0, 0.0, 1000.0))

    }
}