package com.godlike.client.render.items

import com.mojang.blaze3d.vertex.PoseStack
import com.mojang.math.Axis
import dev.emi.trinkets.api.SlotReference
import dev.emi.trinkets.api.client.TrinketRenderer
import net.minecraft.client.Minecraft
import net.minecraft.client.model.EntityModel
import net.minecraft.client.model.HumanoidModel
import net.minecraft.client.model.PlayerModel
import net.minecraft.client.player.AbstractClientPlayer
import net.minecraft.client.renderer.MultiBufferSource
import net.minecraft.client.renderer.entity.ItemRenderer
import net.minecraft.client.renderer.texture.OverlayTexture
import net.minecraft.world.entity.LivingEntity
import net.minecraft.world.item.ItemDisplayContext
import net.minecraft.world.item.ItemStack

class KineticCoreTrinketRenderer : TrinketRenderer {
    override fun render(
        itemStack: ItemStack?,
        slotReference: SlotReference?,
        entityModel: EntityModel<out LivingEntity>?,
        poseStack: PoseStack?,
        multiBufferSource: MultiBufferSource?,
        light: Int,
        livingEntity: LivingEntity?,
        v: Float,
        v1: Float,
        v2: Float,
        v3: Float,
        v4: Float,
        v5: Float
    ) {
        if (entityModel !is PlayerModel<*> || livingEntity !is AbstractClientPlayer || poseStack == null || multiBufferSource == null || itemStack == null) {
            return
        }

        poseStack.pushPose()

        val itemRenderer: ItemRenderer = Minecraft.getInstance().itemRenderer
        TrinketRenderer.followBodyRotations(livingEntity, entityModel as HumanoidModel<LivingEntity>)
        TrinketRenderer.translateToRightArm(poseStack, entityModel as PlayerModel<AbstractClientPlayer>, livingEntity)

        poseStack.translate(-0.1f, -0.5f, 0.0f)
        poseStack.mulPose(Axis.YP.rotationDegrees(-90.0f))
        poseStack.scale(0.6f, 0.6f, 0.6f)

        itemRenderer.renderStatic(
            livingEntity,
            itemStack,
            ItemDisplayContext.THIRD_PERSON_RIGHT_HAND,
            false,
            poseStack,
            multiBufferSource,
            livingEntity.level(),
            light,
            OverlayTexture.NO_OVERLAY,
            livingEntity.getId()
        )
        poseStack.scale(1.6667f, 1.6667f, 1.6667f)
        poseStack.mulPose(Axis.YP.rotationDegrees(120.0f))

        poseStack.popPose()
    }
}