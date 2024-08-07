package com.godlike.networking;

import net.minecraft.util.math.BlockPos;

import java.util.List;

public record BlockPosListPacket(List<BlockPos> blockPosList) {
}
