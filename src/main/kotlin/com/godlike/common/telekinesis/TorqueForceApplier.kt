package com.godlike.common.telekinesis

import org.joml.Vector3dc
import org.valkyrienskies.core.api.ships.PhysShip
import org.valkyrienskies.core.api.ships.ServerShip
import org.valkyrienskies.core.impl.api.ShipForcesInducer
import org.valkyrienskies.core.impl.util.pollUntilEmpty
import java.util.concurrent.ConcurrentLinkedQueue
import org.valkyrienskies.mod.common.util.GameTickForceApplier

/**
 * Since [GameTickForceApplier] does not support torques, we need to implement our own force applier just for that.
 */
class TorqueForceApplier : ShipForcesInducer {
    private val invTorques = ConcurrentLinkedQueue<Vector3dc>()

    override fun applyForces(physShip: PhysShip) {
        invTorques.pollUntilEmpty(physShip::applyInvariantTorque)
    }

    fun applyInvariantTorque(torque: Vector3dc) {
        invTorques.add(torque)
    }
}

fun ServerShip.getTorqueForceApplier(): TorqueForceApplier {
    var applier = this.getAttachment(TorqueForceApplier::class.java)
    if (applier == null) {
        this.saveAttachment(TorqueForceApplier::class.java, TorqueForceApplier())
        applier = this.getAttachment(TorqueForceApplier::class.java)!!
    }
    return applier
}