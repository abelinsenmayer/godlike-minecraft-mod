package com.godlike.items

import com.godlike.Godlike.logger
import com.godlike.components.ModComponents
import com.godlike.util.toVec3d
import net.minecraft.entity.EntityType
import net.minecraft.entity.FallingBlockEntity
import net.minecraft.entity.player.PlayerEntity
import net.minecraft.item.Item
import net.minecraft.item.ItemStack
import net.minecraft.particle.ParticleEffect
import net.minecraft.particle.ParticleTypes
import net.minecraft.sound.SoundEvents
import net.minecraft.state.property.Properties
import net.minecraft.util.Hand
import net.minecraft.util.TypedActionResult
import net.minecraft.world.World
import net.minecraft.world.explosion.ExplosionBehavior

class DevWand : Item(Settings()) {
    override fun use(world: World, user: PlayerEntity, hand: Hand?): TypedActionResult<ItemStack> {
        if (user.isSneaking) {
            val cursors = ModComponents.CURSORS.get(user).getPositions()
            val center = cursors.map { it.toVec3d() }.reduce { acc, vec -> acc.add(vec) }.multiply(1.0 / cursors.size)
            val maxDistance = cursors.maxOfOrNull { it.toVec3d().distanceTo(center) } ?: 0.0

            for (cursor in cursors) {
                // turn blocks into "falling block" entities and launch them
                val fallingBlockEntity = FallingBlockEntity.spawnFromBlock(world, cursor, world.getBlockState(cursor))
                fallingBlockEntity.setHurtEntities(2.0F, 40)

                val distance = cursor.toVec3d().distanceTo(center)
                val fromPlayer = cursor.toVec3d().subtract(user.pos).normalize().multiply(2.0)
                val velocity = cursor.toVec3d()
                    .subtract(center)
                    .normalize()
                    .add(0.0, 0.5, 0.0)
                    .multiply(1.0)
                    .add(fromPlayer)
                    .multiply(maxDistance / distance)

                fallingBlockEntity.velocity = velocity
                fallingBlockEntity.velocityModified = true
            }

//            world.createExplosion(
//                user,
//                world.damageSources.explosion(user, user),
//                ExplosionBehavior(),
//                center.x,
//                center.y,
//                center.z,
//                8.0f,
//                false,
//                World.ExplosionSourceType.NONE,
//                ParticleTypes.EXPLOSION,
//                ParticleTypes.EXPLOSION_EMITTER,
//                SoundEvents.ENTITY_GENERIC_EXPLODE
//            )

            for (i in 0..100) {
                val x = center.x + (Math.random() - 0.5) * 8
                val y = center.y + (Math.random() - 0.5) * 8
                val z = center.z + (Math.random() - 0.5) * 8
                val particle = ParticleTypes.ASH
                world.addParticle(particle, x, y, z, 0.0, 0.0, 0.0)
            }

            ModComponents.CURSORS.get(user).clearPositions()
            ModComponents.CURSOR_ANCHORS.get(user).clearPositions()
        }
        return super.use(world, user, hand)
    }
}