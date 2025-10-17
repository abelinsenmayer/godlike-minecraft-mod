package com.godlike.common.networking;

import net.minecraft.core.Direction;

public record PlacementDirectionPacket(Direction topDirection, Direction frontDirection) {
}
