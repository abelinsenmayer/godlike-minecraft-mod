package com.godlike.networking;


import net.minecraft.core.BlockPos;

import java.util.List;

public record DoSelectionPacket(List<BlockPos> cursorPreviews, BlockPos targetPosition) {
}
