package com.godlike.common.networking;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public record LaunchTkPacket(Vec3 targetedPosition) {
}
