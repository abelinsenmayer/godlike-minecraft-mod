package com.godlike.common.networking;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;

public record PickEntityToTkPacket(CompoundTag entityData) {
}
