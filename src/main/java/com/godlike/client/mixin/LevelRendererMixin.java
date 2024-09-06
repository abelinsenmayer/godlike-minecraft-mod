package com.godlike.client.mixin;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderBuffers;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import static com.godlike.client.render.RenderUtilKt.highlightSelectionTarget;

@Mixin(LevelRenderer.class)
public abstract class LevelRendererMixin {
    @Shadow @Final private RenderBuffers renderBuffers;

    @Inject(at = @At("HEAD"), method = "renderLevel")
    private void renderLevel(
            PoseStack poseStack, float partialTick, long finishNanoTime, boolean renderBlockOutline, Camera camera,
            GameRenderer gameRenderer, LightTexture lightTexture, Matrix4f projectionMatrix, CallbackInfo ci
    ) {
        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) {
            return;
        }

        // highlight target position or entity
        highlightSelectionTarget(poseStack, camera, this.renderBuffers.outlineBufferSource());

//        // highlight the block the player is targeting
//        BlockPos targetPos = ModComponents.TARGET_POSITION.get(player).getPos();
//        outlineBlockPos(targetPos, poseStack, camera, 30f, 254f, 245f, 0.5f);
//
//        // highlight selection previews
//        List<BlockPos> previews = ModComponents.CURSOR_PREVIEWS.get(player).getPositions();
//        for (BlockPos preview : previews) {
//            outlineBlockPos(preview, poseStack, camera, 245f, 40f, 145f, 0.3f);
//        }
//
//        // highlight cursors
//        List<BlockPos> cursors = ModComponents.CURSORS.get(player).getPositions();
//        for (BlockPos cursor : cursors) {
//            outlineBlockPos(cursor, poseStack, camera, 93f, 245f, 148f, 0.5f);
//        }
    }
}
