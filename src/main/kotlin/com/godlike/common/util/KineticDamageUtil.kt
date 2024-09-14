package com.godlike.common.util

import net.minecraft.tags.EntityTypeTags
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.LivingEntity
import org.valkyrienskies.core.api.ships.ServerShip

const val KINETIC_DAMAGE_MULTIPLIER = 3.0
const val VELOCITY_DAMAGE_THRESHOLD = 1.0

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
    val collisionVelocity = this.deltaMovement.subtract(other.velocity.toVec3())
    return velocityToKineticDamage(collisionVelocity.length()).toFloat()
}

fun velocityToKineticDamage(velocity: Double) : Double {
    return 0.0.coerceAtLeast(velocity * KINETIC_DAMAGE_MULTIPLIER - VELOCITY_DAMAGE_THRESHOLD)
}

fun LivingEntity.immuneToKineticDamage() : Boolean {
    return type.`is`(EntityTypeTags.FALL_DAMAGE_IMMUNE)
}

fun Entity.findCollidingEntities() : List<Entity> {
    return this.level().getEntities(this, this.boundingBox)

//    val startVec = this.position()
//    val endVec = this.position().add(this.deltaMovement)
//    val hitResult = ProjectileUtil.getEntityHitResult(
//        this.level(),
//        this,
//        startVec,
//        endVec,
//        boundingBox.expandTowards(this.deltaMovement)
//    ) { _: Entity -> true }
//    return hitResult?.entity
}