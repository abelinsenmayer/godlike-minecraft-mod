package com.godlike.mixin.client;

import com.godlike.Godlike;
import com.godlike.components.ModComponents;
import com.godlike.render.RenderUtilKt;
import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.Blocks;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import team.lodestar.lodestone.systems.rendering.ghost.GhostBlockOptions;
import team.lodestar.lodestone.systems.rendering.ghost.GhostBlockRenderer;

import java.util.List;

import static com.godlike.render.RenderUtilKt.outlineBlockPos;
import static com.godlike.render.RenderUtilKt.renderCubeAt;

@Mixin(LevelRenderer.class)
public abstract class WorldRendererMixin {
    @Inject(at = @At("TAIL"), method = "renderLevel")
    private void renderLevel(
            PoseStack poseStack, float partialTick, long finishNanoTime, boolean renderBlockOutline, Camera camera,
            GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projectionMatrix, CallbackInfo ci
    ) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

//        RenderUtilKt.highlightPos(poseStack, ModComponents.TARGET_POSITION.get(player).getPos());

        // highlight the block the player is targeting
        BlockPos targetPos = ModComponents.TARGET_POSITION.get(player).getPos();
//        renderCubeAt(poseStack, new BlockPos((int)player.position().x, (int)player.position().y, (int)player.position().z), partialTick);

        outlineBlockPos(targetPos, poseStack, camera, 30f, 254f, 245f, 0.5f);
//
//        // highlight selection previews
//        List<BlockPos> previews = ModComponents.CURSOR_PREVIEWS.get(player).getPositions();
//        for (BlockPos preview : previews) {
//            outlineBlockPos(preview, player, camera, 0f, 0f, 0f, 1.0f);
//        }
//
//        // highlight cursors
//        List<BlockPos> cursors = ModComponents.CURSORS.get(player).getPositions();
//        for (BlockPos cursor : cursors) {
//            outlineBlockPos(cursor, player, camera, 245f, 40f, 145f, 1.0f);
//        }
    }
}
