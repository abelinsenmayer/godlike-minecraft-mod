package com.godlike.common.vs2;


import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.phys.AABB;
import org.joml.Vector3i;
import org.joml.primitives.AABBdc;
import org.valkyrienskies.core.api.ships.ClientShip;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.apigame.world.ServerShipWorldCore;
import org.valkyrienskies.core.impl.datastructures.DenseBlockPosSet;
import org.valkyrienskies.mod.common.assembly.ShipAssemblyKt;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;

import java.util.Collection;

public class Vs2Util {
    public static ServerShip createShip(Collection<BlockPos> positions, ServerLevel world) {
        if (positions.isEmpty()) {
            return null;
        }

        int xAvg = positions.stream().mapToInt(BlockPos::getX).sum() / positions.size();
        int yAvg = positions.stream().mapToInt(BlockPos::getY).sum() / positions.size();
        int zAvg = positions.stream().mapToInt(BlockPos::getZ).sum() / positions.size();
        BlockPos centerPos = new BlockPos(xAvg, yAvg, zAvg);

        DenseBlockPosSet denseSet = new DenseBlockPosSet();
        positions.forEach(pos -> denseSet.add(new Vector3i(pos.getX(), pos.getY(), pos.getZ())));

        return ShipAssemblyKt.createNewShipWithBlocks(centerPos, denseSet, world);
    }

    public static ServerShipWorldCore getServerShipWorld(ServerLevel world) {
        return VSGameUtilsKt.getShipObjectWorld(world);
    }

    public static ClientShip getClientShipManagingPos(ClientLevel level, BlockPos pos) {
        return VSGameUtilsKt.getShipObjectManagingPos(level, pos);
    }

    public static AABB toMinecraftAABB(AABBdc aabb) {
        return VectorConversionsMCKt.toMinecraft(aabb);
    }
}
