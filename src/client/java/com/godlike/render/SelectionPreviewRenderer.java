package com.godlike.render;

import com.godlike.components.ModComponents;
import com.godlike.mixin.client.WorldRendererInvoker;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.WorldRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.shape.VoxelShape;
import net.minecraft.util.shape.VoxelShapes;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class SelectionPreviewRenderer {
    private static final Logger logger = LoggerFactory.getLogger("godlike");

    public static void renderPlayerCursors(VertexConsumer vertexConsumer) {
        // get cursors for the player
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) {
            return;
        }
        List<BlockPos> cursors = ModComponents.CURSORS.get(player).getPositions();

        // render a wireframe at each cursor
        for (BlockPos cursor : cursors) {
             renderWireframe(cursor, vertexConsumer);
        }
    }

    private static void renderWireframe(BlockPos pos, VertexConsumer vertexConsumer) {
        logger.info("Rendering wireframe at {}", pos.toString());
//        WorldRenderer.drawBox(vertexConsumer, pos.getX(), pos.getY(), pos.getZ(),
//                pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1,
//                255, 255, 255, 255);
//        WorldRenderer.drawShapeOutline(
//                new MatrixStack(),
//                vertexConsumer,
//                VoxelShapes.cuboid(pos.getX(), pos.getY(), pos.getZ(), pos.getX() + 1, pos.getY() + 1, pos.getZ() + 1),
//                0, 0, 0,
//                255, 255, 255,
//                255, true
//        );

        WorldRenderer worldRenderer = MinecraftClient.getInstance().worldRenderer;
//        ((WorldRendererInvoker) worldRenderer).drawBlockOutline(
//                new MatrixStack(),
//                vertexConsumer,
//                MinecraftClient.getInstance().player,
//                MinecraftClient.getInstance().getCameraEntity().getX(),
//                MinecraftClient.getInstance().getCameraEntity().getY(),
//                MinecraftClient.getInstance().getCameraEntity().getZ(),
//                pos,
//                MinecraftClient.getInstance().world.getBlockState(pos)
//        );
    }
}
