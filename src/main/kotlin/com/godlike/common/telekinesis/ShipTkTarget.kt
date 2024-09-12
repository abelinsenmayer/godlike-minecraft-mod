package com.godlike.common.telekinesis

import com.godlike.common.util.toNbt
import com.godlike.common.vs2.Vs2Util
import net.minecraft.client.player.LocalPlayer
import net.minecraft.nbt.CompoundTag
import net.minecraft.server.level.ServerPlayer
import net.minecraft.world.entity.player.Player
import net.minecraft.world.phys.Vec3
import org.valkyrienskies.core.api.ships.ServerShip
import org.valkyrienskies.mod.common.util.GameTickForceApplier

class ShipTkTarget(
    private val shipId : Long,
    val player: Player
) {
    var anchorPos : Vec3? = null
    val ship : ServerShip
        get() {
            if (player is LocalPlayer) {
                throw IllegalStateException("Cannot get a ship on the client side")
            }
            return Vs2Util.getServerShipWorld((player as ServerPlayer).serverLevel()).loadedShips.getById(shipId)!!
        }

    companion object {
        fun fromNbtAndPlayer(tag: CompoundTag, player: Player) : ShipTkTarget {
            val shipId = tag.getLong("shipId")
            val target = ShipTkTarget(shipId, player)
            if (tag.contains("anchorPos")) {
                target.anchorPos = Vec3(tag.getDouble("anchorPos.x"), tag.getDouble("anchorPos.y"), tag.getDouble("anchorPos.z"))
            }
            return target
        }
    }

    fun toNbt() : CompoundTag {
        val tag = CompoundTag()
        tag.putLong("shipId", shipId)
        anchorPos?.let {
            tag.putDouble("anchorPos.x", it.x)
            tag.putDouble("anchorPos.y", it.y)
            tag.putDouble("anchorPos.z", it.z)
        }
        return tag
    }

    fun forceApplier() : GameTickForceApplier {
        return this.ship.getAttachment(GameTickForceApplier::class.java)!!
    }

    fun torqueApplier() : TorqueForceApplier {
        return this.ship.getTorqueForceApplier()
    }
}