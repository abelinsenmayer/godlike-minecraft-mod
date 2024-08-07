package com.godlike.mixin.client;

import com.godlike.components.ModComponents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.render.*;
import net.minecraft.util.math.BlockPos;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.List;

import static com.godlike.render.RenderUtilKt.highlightBlockPos;

@Mixin(WorldRenderer.class)
public abstract class WorldRendererMixin {
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
        ClientPlayerEntity player = MinecraftClient.getInstance().player;
        if (player == null) {
            return;
        }

        // highlight cursors
        List<BlockPos> cursors = ModComponents.CURSORS.get(player).getPositions();
        for (BlockPos cursor : cursors) {
            highlightBlockPos(cursor, player, camera, 245f, 40f, 145f, 1.0f);
        }

        // highlight selection previews
        List<BlockPos> previews = ModComponents.CURSOR_PREVIEWS.get(player).getPositions();
        for (BlockPos preview : previews) {
            highlightBlockPos(preview, player, camera, 0f, 0f, 0f, 1.0f);
        }

        // highlight the block the player is targeting
        BlockPos targetPos = ModComponents.TARGET_POSITION.get(player).getPos();
        highlightBlockPos(targetPos, player, camera, 30f, 254f, 245f, 1.0f);
    }
}
