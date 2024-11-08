package com.godlike.common.vs2;


import com.godlike.common.telekinesis.TorqueForceApplier;
import kotlin.jvm.functions.Function0;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.joml.Vector2i;
import org.joml.Vector3i;
import org.joml.primitives.AABBdc;
import org.valkyrienskies.core.api.ships.ClientShip;
import org.valkyrienskies.core.api.ships.ServerShip;
import org.valkyrienskies.core.apigame.world.ClientShipWorldCore;
import org.valkyrienskies.core.apigame.world.ServerShipWorldCore;
import org.valkyrienskies.core.util.datastructures.DenseBlockPosSet;
import org.valkyrienskies.mod.common.assembly.ShipAssemblyKt;
import org.valkyrienskies.mod.common.VSGameUtilsKt;
import org.valkyrienskies.mod.common.util.MinecraftPlayer;
import org.valkyrienskies.mod.common.util.VectorConversionsMCKt;
import org.valkyrienskies.mod.util.RelocationUtilKt;

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

        ServerShip ship = ShipAssemblyKt.createNewShipWithBlocks(centerPos, denseSet, world);
        ship.saveAttachment(TorqueForceApplier.class, new TorqueForceApplier());
        return ship;
    }
    public static ServerShipWorldCore getServerShipWorld(ServerLevel world) {
        return VSGameUtilsKt.getShipObjectWorld(world);
    }

    public static ClientShipWorldCore getClientShipWorld(ClientLevel world) {
        return VSGameUtilsKt.getShipObjectWorld(world);
    }

    public static ClientShip getClientShipManagingPos(ClientLevel level, BlockPos pos) {
        return VSGameUtilsKt.getShipObjectManagingPos(level, pos);
    }

    public static ServerShip getServerShipManagingPos(ServerLevel level, BlockPos pos) {
        return VSGameUtilsKt.getShipObjectManagingPos(level, pos);
    }

    public static AABB toMinecraftAABB(AABBdc aabb) {
        return VectorConversionsMCKt.toMinecraft(aabb);
    }

    public static Vector2i toJOML(ChunkPos chunkPos) {
        return VectorConversionsMCKt.toJOML(chunkPos);
    }

    public static MinecraftPlayer playerWrapper(Player player) {
        return VSGameUtilsKt.getPlayerWrapper(player);
    }

    public static void executeIf(MinecraftServer server, Function0<Boolean> condition, Runnable toExecute) {
        VSGameUtilsKt.executeIf(server, condition, toExecute);
    }

    public static boolean isTickingChunk(ServerLevel level, ChunkPos pos) {
        return VSGameUtilsKt.isTickingChunk(level, pos);
    }

    public static void updateBlock(Level level, BlockPos fromPos, BlockPos toPos, BlockState toState) {
        RelocationUtilKt.updateBlock(level, fromPos, toPos, toState);
    }

    public static void relocateBlock(Level level, BlockPos fromPos, BlockPos toPos, boolean doUpdate, ServerShip ship, Rotation rotation) {
        RelocationUtilKt.relocateBlock(level, fromPos, toPos, doUpdate, ship, rotation);
    }
}
