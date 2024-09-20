package com.godlike.common.components

import com.godlike.client.render.setEntityGlowing
import com.godlike.common.util.toVec3
import dev.onyxstudios.cca.api.v3.component.Component
import net.minecraft.client.player.LocalPlayer
import net.minecraft.core.BlockPos
import net.minecraft.core.Vec3i
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType
import net.minecraft.world.phys.Vec3
import org.valkyrienskies.core.api.ships.ClientShip

const val CURSOR_TARGET_BLOCK = "cursor_target_block"
const val CURSOR_TARGET_ENTITY = "cursor_target_entity"

const val MAX_DFS_DEPTH = 200

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
    var clientChargingLaunch : Boolean = false  // TODO move this out of this component
    var selectedPositions: MutableSet<BlockPos> = mutableSetOf()
    var previewPositions: MutableSet<BlockPos> = mutableSetOf()
    var selectionIsContiguous: Boolean = false
    var dfsDepth: Int = 0
        set(value) {
            if (value <= MAX_DFS_DEPTH) {
                field = value
            }
        }

    override fun readFromNbt(tag: CompoundTag) {
        this.cursorTargetEntity = tag.getCompound(CURSOR_TARGET_ENTITY).let {
            EntityType.create(it, player.level()).orElse(null)
        }
        tag.getLong(CURSOR_TARGET_BLOCK).let { cursorTargetBlock = BlockPos.of(it) }
        clientChargingLaunch = tag.getBoolean("clientChargingLaunch")
        selectedPositions = tag.getLongArray("selectedPositions").map { BlockPos.of(it) }.toMutableSet()
        previewPositions = tag.getLongArray("previewPositions").map { BlockPos.of(it) }.toMutableSet()
        selectionIsContiguous = tag.getBoolean("selectionIsContiguous")
        dfsDepth = tag.getInt("dfsDepth")
    }

    override fun writeToNbt(tag: CompoundTag) {
        this.cursorTargetEntity?.let {
            val entityTag = CompoundTag()
            it.save(entityTag)
            tag.put(CURSOR_TARGET_ENTITY, entityTag)
        }
        this.cursorTargetBlock?.let { tag.putLong(CURSOR_TARGET_BLOCK, it.asLong()) }
        tag.putBoolean("clientChargingLaunch", clientChargingLaunch)
        tag.putLongArray("selectedPositions", selectedPositions.map { it.asLong() }.toLongArray())
        tag.putLongArray("previewPositions", previewPositions.map { it.asLong() }.toLongArray())
        tag.putBoolean("selectionIsContiguous", selectionIsContiguous)
        tag.putInt("dfsDepth", dfsDepth)
    }

    fun clear() {
        cursorTargetBlock = null
        cursorTargetEntity = null
        cursorTargetShip = null
        selectedPositions.clear()
        previewPositions.clear()
        dfsDepth = 0
    }

    fun setSingleTarget(target: Any) {
        cursorTargetBlock = null
        cursorTargetEntity = null
        cursorTargetShip = null
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

fun LocalPlayer.selection(): SelectionComponent = ModComponents.SELECTION_DATA.get(this)