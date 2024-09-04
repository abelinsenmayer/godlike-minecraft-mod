package com.godlike.client.fx

import net.minecraft.world.level.Level
import net.minecraft.world.phys.Vec3
import team.lodestar.lodestone.registry.common.particle.LodestoneParticleRegistry
import team.lodestar.lodestone.systems.easing.Easing
import team.lodestar.lodestone.systems.particle.builder.WorldParticleBuilder
import team.lodestar.lodestone.systems.particle.data.GenericParticleData
import team.lodestar.lodestone.systems.particle.data.color.ColorParticleData
import team.lodestar.lodestone.systems.particle.data.spin.SpinParticleData
import java.awt.Color


fun traceVectorFromPoint(origin: Vec3, direction: Vec3, level: Level) {
    var ptr = origin
    val numPoints = direction.length() / 0.1
    val increment = direction.normalize().scale(0.1)
    for (i in 0..numPoints.toInt()) {
        ptr = ptr.add(increment)
        spawnTracerParticle(ptr, level)
    }
}

fun spawnTracerAtPoint(pos: Vec3, level: Level) {
    spawnTracerParticle(pos, level)
}

fun spawnTracerParticle(pos: Vec3, level: Level) {
    val startingColor = Color(100, 0, 100)
    val endingColor = Color(0, 100, 200)
    WorldParticleBuilder.create(LodestoneParticleRegistry.WISP_PARTICLE)
        .setScaleData(GenericParticleData.create(0.5f, 0f).build())
        .setTransparencyData(GenericParticleData.create(0.75f, 0.25f).build())
        .setColorData(
            ColorParticleData.create(startingColor, endingColor).setCoefficient(1.4f).setEasing(Easing.BOUNCE_IN_OUT)
                .build()
        )
        .setSpinData(
            SpinParticleData.create(0.2f, 0.4f).setSpinOffset((level.gameTime * 0.2f) % 6.28f)
                .setEasing(Easing.QUARTIC_IN).build()
        )
        .setLifetime(40)
        .addMotion(0.0, 0.01, 0.0)
        .enableNoClip()
        .spawn(level, pos.x, pos.y, pos.z)

}