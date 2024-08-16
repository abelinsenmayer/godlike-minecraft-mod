package com.godlike.vs2;

import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.math.BlockPos;
import org.joml.Vector3i;
import org.valkyrienskies.core.util.datastructures.DenseBlockPosSet;
import org.valkyrienskies.mod.common.assembly.ShipAssemblyKt;

import java.util.Collection;

public class ShipUtil {
    public static void assembleShipFromPositions(Collection<BlockPos> positions, ServerWorld world) {
        int xAvg = positions.stream().mapToInt(BlockPos::getX).sum() / positions.size();
        int yAvg = positions.stream().mapToInt(BlockPos::getY).sum() / positions.size();
        int zAvg = positions.stream().mapToInt(BlockPos::getZ).sum() / positions.size();
        BlockPos centerPos = new BlockPos(xAvg, yAvg, zAvg);

        DenseBlockPosSet denseSet = new DenseBlockPosSet();
        positions.forEach(pos -> denseSet.add(new Vector3i(pos.getX(), pos.getY(), pos.getZ())));

        ShipAssemblyKt.createNewShipWithBlocks(centerPos, denseSet, world);
    }
}
