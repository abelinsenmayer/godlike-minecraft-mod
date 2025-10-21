package com.godlike.common.util

import net.minecraft.world.entity.Entity
import net.minecraft.world.level.ClipContext
import net.minecraft.world.level.Level
import net.minecraft.world.phys.HitResult
import net.minecraft.world.phys.Vec3

fun blockRayCast(level: Level, fromPos: Vec3, toPos: Vec3, collidingEntity: Entity): HitResult {
    val vec3: Vec3 = fromPos.add(toPos)
    val hitResult: HitResult =
        level.clip(ClipContext(fromPos, vec3, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, collidingEntity))
    return hitResult
}