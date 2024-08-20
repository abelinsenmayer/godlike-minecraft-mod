package com.godlike.items

import com.godlike.components.ModComponents
import com.godlike.vs2.ShipUtil
import net.minecraft.server.level.ServerLevel
import net.minecraft.world.InteractionHand
import net.minecraft.world.InteractionResultHolder
import net.minecraft.world.entity.player.Player
import net.minecraft.world.item.Item
import net.minecraft.world.item.ItemStack
import net.minecraft.world.level.Level

class DevWand : Item(Properties()) {
    override fun use(world: Level, user: Player, hand: InteractionHand): InteractionResultHolder<ItemStack> {
        if (user.isShiftKeyDown) {
            val cursors = ModComponents.CURSORS.get(user).getPositions()

            if (!world.isClientSide) {
                ShipUtil.assembleShipFromPositions(cursors, world as ServerLevel)
            }

            ModComponents.CURSORS.get(user).clearPositions()
            ModComponents.CURSOR_ANCHORS.get(user).clearPositions()
        }
        return super.use(world, user, hand)
    }
}








// TODO move this elsewhere, it's just here so I don't forget how to implement it
//fun blockExplosion(blocks : Collection<BlockPos>, world: World, user: PlayerEntity) {
//    val center = blocks.map { it.toVec3d() }.reduce { acc, vec -> acc.add(vec) }.multiply(1.0 / blocks.size)
//    val maxDistance = blocks.maxOfOrNull { it.toVec3d().distanceTo(center) } ?: 0.0
//
//    for (cursor in blocks) {
//        // turn blocks into "falling block" entities and launch them
//        val fallingBlockEntity = FallingBlockEntity.spawnFromBlock(world, cursor, world.getBlockState(cursor))
//        fallingBlockEntity.setHurtEntities(2.0F, 40)
//
//        val distance = cursor.toVec3d().distanceTo(center)
//        val fromPlayer = cursor.toVec3d().subtract(user.pos).normalize().multiply(2.0)
//        val velocity = cursor.toVec3d()
//            .subtract(center)
//            .normalize()
//            .add(0.0, 0.5, 0.0)
//            .multiply(1.0)
//            .add(fromPlayer)
//            .multiply(maxDistance / distance)
//
//        fallingBlockEntity.velocity = velocity
//        fallingBlockEntity.velocityModified = true
//    }
//
//    for (i in 0..100) {
//        val x = center.x + (Math.random() - 0.5) * 8
//        val y = center.y + (Math.random() - 0.5) * 8
//        val z = center.z + (Math.random() - 0.5) * 8
//        val particle = ParticleTypes.ASH
//        world.addParticle(particle, x, y, z, 0.0, 0.0, 0.0)
//    }
//}