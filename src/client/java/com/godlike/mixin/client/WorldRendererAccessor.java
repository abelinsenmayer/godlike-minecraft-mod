package com.godlike.mixin.client;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.LevelRenderer;
import net.minecraft.client.renderer.RenderBuffers;
import net.minecraft.world.phys.AABB;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(LevelRenderer.class)
public interface WorldRendererAccessor {
    @Accessor("renderBuffers")
    RenderBuffers getBufferBuilders();

    @Invoker("renderLineBox")
    static void invokeDrawCuboidShapeOutline(PoseStack matrices, VertexConsumer vertexConsumer, AABB shape, float red, float green, float blue, float alpha) {
        throw new IllegalStateException("Mixin didn't apply");
    }
}
