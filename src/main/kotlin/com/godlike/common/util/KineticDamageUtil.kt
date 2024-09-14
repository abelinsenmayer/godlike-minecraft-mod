package com.godlike.common.util

import com.godlike.common.vs2.Vs2Util
import net.minecraft.server.level.ServerLevel
import net.minecraft.tags.EntityTypeTags
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import org.joml.Vector3d
import org.joml.primitives.AABBd
import org.valkyrienskies.core.api.ships.LoadedServerShip
import org.valkyrienskies.core.api.ships.ServerShip

const val KINETIC_DAMAGE_MULTIPLIER = 8.0

/**
 * Calculates kinetic damage for an entity hitting any stationary block.
 */
fun LivingEntity.kineticDamage() : Float {
    if (this.immuneToKineticDamage()) {
        return 0.0F
    }
    return velocityToKineticDamage(this.deltaMovement.length()).toFloat()
}

/**
 * Calculates kinetic damage for an entity hitting a ship.
 * TODO update this to account for ship mass
 */
fun LivingEntity.kineticDamageShipCollision(other: ServerShip) : Float {
    if (this.immuneToKineticDamage()) {
        return 0.0F
    }
    return velocityToKineticDamage(other.velocity.toVec3().length()/20.0).toFloat()
}

fun velocityToKineticDamage(velocity: Double) : Double {
    return 0.0.coerceAtLeast(velocity * KINETIC_DAMAGE_MULTIPLIER - KINETIC_DAMAGE_MULTIPLIER)
}

fun LivingEntity.immuneToKineticDamage() : Boolean {
    return type.`is`(EntityTypeTags.FALL_DAMAGE_IMMUNE)
}

fun Entity.findCollidingEntities() : List<Entity> {
    return this.level().getEntities(this, this.boundingBox)
}

/**
 * Gets all ships that this entity is colliding with. Only works on the server side.
 */
fun Entity.getCollidingServerShips() : List<LoadedServerShip> {
    return if (!this.level().isClientSide) {
        val bb = this.boundingBox.inflate(0.5, 0.5, 0.5)
        Vs2Util.getServerShipWorld(this.level() as ServerLevel).loadedShips.getIntersecting(AABBd(
            Vector3d(bb.minX, bb.minY, bb.minZ),
            Vector3d(bb.maxX, bb.maxY, bb.maxZ)
        )).toList()
    }
    else emptyList()
}