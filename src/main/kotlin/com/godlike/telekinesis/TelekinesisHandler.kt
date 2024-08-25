package com.godlike.telekinesis

import com.godlike.components.ModComponents
import com.godlike.networking.TelekinesisControlsPacket
import com.godlike.vs2.Vs2Util
import net.minecraft.server.level.ServerPlayer
import org.joml.Vector3d
import org.valkyrienskies.mod.common.util.GameTickForceApplier
import kotlin.random.Random

fun physicsObjectFromSelection(player: ServerPlayer) {
    val cursors = ModComponents.CURSORS.get(player).getPositions()
    val ship = Vs2Util.makePhysicsObjectFromBlocks(cursors, player.serverLevel())
    ModComponents.TELEKINESIS_DATA.get(player).tkTargets.add(ship)
    val forceApplier = ship.getAttachment(GameTickForceApplier::class.java)!!
    forceApplier.applyInvariantForce(Vector3d(Random.nextDouble(-10.0, 10.0), Random.nextDouble(-10.0, 10.0), Random.nextDouble(-10.0, 10.0)))
}

fun handleTelekinesisControls(telekinesisControls: TelekinesisControlsPacket, player: ServerPlayer) {
    val tkData = ModComponents.TELEKINESIS_DATA.get(player)
    tkData.tkTargets.forEach { ship ->
        // force applier may not yet have been assigned to the ship, so we must check for null
        val forceApplier = ship.getAttachment(GameTickForceApplier::class.java)

        forceApplier?.applyInvariantForce(
            Vector3d(
                Random.nextDouble(-10.0, 10.0),
                Random.nextDouble(-10.0, 10.0),
                Random.nextDouble(-10.0, 10.0)
            )
        )
    }
}