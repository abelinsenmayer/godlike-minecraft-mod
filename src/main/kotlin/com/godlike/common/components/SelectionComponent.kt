package com.godlike.common.components

import com.godlike.client.render.setEntityGlowing
import com.godlike.common.telekinesis.LAUNCH_POINTER_DISTANCE
import com.godlike.common.telekinesis.getPointerAtDistance
import com.godlike.common.util.toVec3
import dev.onyxstudios.cca.api.v3.component.Component
import net.minecraft.client.Minecraft
import net.minecraft.client.player.LocalPlayer
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.phys.Vec3
import org.valkyrienskies.core.api.ships.ClientShip

const val CURSOR_TARGET_BLOCK = "cursor_target_block"
const val CURSOR_TARGET_ENTITY = "cursor_target_entity"

/**
 * This component is used to store the player's current selection data.
 * Note that it is written and read on the client side; you should not use this component on the server side.
 */
class SelectionComponent(private val player : LocalPlayer) : Component {
    var cursorTargetBlock : BlockPos? = null
    var cursorTargetEntity : Entity? = null
        set(it) {
            // Stop highlighting the old selection, if present
            field?.let { old -> setEntityGlowing(old, false) }
            field = it
            // Set the entity as glowing to highlight it
            field?.let { new -> setEntityGlowing(new, true) }
        }
    var cursorTargetShip : ClientShip? = null
    var isSelecting = false
    var clientChargingLaunch : Boolean = false  // TODO move this out of this component

    override fun readFromNbt(tag: CompoundTag) {
        this.cursorTargetEntity = tag.getCompound(CURSOR_TARGET_ENTITY).let {
            EntityType.create(it, player.level()).orElse(null)
        }
        tag.getLong(CURSOR_TARGET_BLOCK).let { cursorTargetBlock = BlockPos.of(it) }
        clientChargingLaunch = tag.getBoolean("clientChargingLaunch")
    }

    override fun writeToNbt(tag: CompoundTag) {
        this.cursorTargetEntity?.let {
            val entityTag = CompoundTag()
            it.save(entityTag)
            tag.put(CURSOR_TARGET_ENTITY, entityTag)
        }
        this.cursorTargetBlock?.let { tag.putLong(CURSOR_TARGET_BLOCK, it.asLong()) }
        tag.putBoolean("clientChargingLaunch", clientChargingLaunch)
    }

    fun clear() {
        cursorTargetBlock = null
        cursorTargetEntity = null
        cursorTargetShip = null
    }

    fun setSingleTarget(target: Any) {
        clear()
        when (target) {
            is BlockPos -> {
                cursorTargetBlock = target
            }
            is Entity -> {
                cursorTargetEntity = target
            }
            is ClientShip -> {
                cursorTargetShip = target
            }
            else -> throw IllegalArgumentException("Invalid target type: $target")
        }
    }

    fun getSelectionPosition() : Vec3? = if (player.selection().cursorTargetEntity != null) {
        player.selection().cursorTargetEntity!!.position()
    } else if (player.selection().cursorTargetBlock != null) {
        player.selection().cursorTargetBlock!!.toVec3()
    } else if (player.selection().cursorTargetShip != null) {
        player.selection().cursorTargetShip!!.transform.positionInWorld.toVec3()
    } else {
        null
    }
}

fun LocalPlayer.selection() = ModComponents.SELECTION_DATA.get(this)