package com.godlike.common.networking;

import net.minecraft.core.BlockPos;

import java.util.List;
import java.util.Set;

public record TkPositionsPacket(List<BlockPos> positions) {
}
