package com.godlike.networking;

import net.minecraft.util.math.BlockPos;

import java.util.List;

public record DoSelectionPacket(List<BlockPos> cursorPreviews, BlockPos targetPosition) {
}
