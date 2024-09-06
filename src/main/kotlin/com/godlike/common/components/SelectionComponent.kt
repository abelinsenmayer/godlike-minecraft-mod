package com.godlike.common.components

import com.godlike.client.render.setEntityGlowing
import dev.onyxstudios.cca.api.v3.component.Component
import net.minecraft.client.player.LocalPlayer
import net.minecraft.core.BlockPos
import net.minecraft.nbt.CompoundTag
import net.minecraft.world.entity.Entity
import net.minecraft.world.entity.EntityType

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

//    val selectedPositions : MutableList<BlockPos> = mutableListOf()
//    val selectedPositionAnchors : MutableList<BlockPos> = mutableListOf()

    override fun readFromNbt(tag: CompoundTag) {
        this.cursorTargetEntity = tag.getCompound(CURSOR_TARGET_ENTITY).let {
            EntityType.create(it, player.level()).orElse(null)
        }
        tag.getLong(CURSOR_TARGET_BLOCK).let { cursorTargetBlock = BlockPos.of(it) }
    }

    override fun writeToNbt(tag: CompoundTag) {
        this.cursorTargetEntity?.let {
            val entityTag = CompoundTag()
            it.save(entityTag)
            tag.put(CURSOR_TARGET_ENTITY, entityTag)
        }
        this.cursorTargetBlock?.let { tag.putLong(CURSOR_TARGET_BLOCK, it.asLong()) }
    }

    fun clear() {
        cursorTargetBlock = null
        cursorTargetEntity = null
    }

    fun setSingleTarget(target: Any) {
        when (target) {
            is BlockPos -> {
                cursorTargetEntity = null
                cursorTargetBlock = target
            }
            is Entity -> {
                cursorTargetBlock = null
                cursorTargetEntity = target
            }
            else -> throw IllegalArgumentException("Invalid target type: $target")
        }

    }
}

fun LocalPlayer.selection() = ModComponents.SELECTION_DATA.get(this)