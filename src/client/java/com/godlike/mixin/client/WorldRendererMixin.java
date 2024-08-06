package com.godlike.mixin.client;

import com.godlike.components.ModComponents;
import net.minecraft.block.BlockState;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.shape.VoxelShape;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static com.godlike.render.SelectionPreviewRenderer.renderPlayerCursors;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {
    @Shadow @Final private BufferBuilderStorage bufferBuilders;

    @Shadow protected abstract void drawBlockOutline(MatrixStack matrices, VertexConsumer vertexConsumer, Entity entity, double cameraX, double cameraY, double cameraZ, BlockPos pos, BlockState state);

    @Shadow
    private static void drawCuboidShapeOutline(MatrixStack matrices, VertexConsumer vertexConsumer, VoxelShape shape, double offsetX, double offsetY, double offsetZ, float red, float green, float blue, float alpha) {

    }

    @Inject(at = @At("TAIL"), method = "render")
    private void render(
            RenderTickCounter tickCounter,
            boolean renderBlockOutline,
            Camera camera,
            GameRenderer gameRenderer,
            LightmapTextureManager lightmapTextureManager,
            Matrix4f matrix4f,
            Matrix4f matrix4f2,
            CallbackInfo ci
    ) {
        // get cursors for the player
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) {
            return;
        }
        List<BlockPos> cursors = ModComponents.CURSORS.get(player).getPositions();

        // render a wireframe at each cursor
        for (BlockPos cursor : cursors) {
            Vec3d cameraPos = camera.getPos();

            drawCuboidShapeOutline(
                    new MatrixStack(),
                    this.bufferBuilders.getEntityVertexConsumers().getBuffer(RenderLayer.getLines()),
                    player.getWorld().getBlockState(cursor).getOutlineShape(player.getWorld(), cursor),
                    cursor.getX() - cameraPos.getX(),
                    cursor.getY() - cameraPos.getY(),
                    cursor.getZ() - cameraPos.getZ(),
                    245f, 40f, 145f, 1.0f
            );
        }
    }
}
