package com.idiotss.maps.mixin;

import com.idiotss.maps.AllItems;
import com.idiotss.maps.MapDrawingClient;
import com.idiotss.maps.item.DrawableMap;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;

@Mixin(ItemInHandRenderer.class)
public abstract class ItemInHandRendererMixin {
    @Shadow
    protected abstract void renderTwoHandedMap(PoseStack poseStack, MultiBufferSource buffer, int packedLight, float pitch, float equippedProgress, float swingProgress);

    @Shadow
    protected abstract void renderOneHandedMap(PoseStack poseStack, MultiBufferSource buffer, int packedLight, float equippedProgress, HumanoidArm hand, float swingProgress, ItemStack stack);

    @Shadow
    private ItemStack offHandItem;

    @Shadow
    @Final
    private static RenderType MAP_BACKGROUND;

    @Shadow
    @Final
    private static RenderType MAP_BACKGROUND_CHECKERBOARD;

    @Shadow
    @Final
    private Minecraft minecraft;

    @Inject(method = "renderArmWithItem",
            at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V"),
            cancellable = true)
    private void renderArmWithItem(
            AbstractClientPlayer player, float partialTicks, float pitch, InteractionHand hand, float swingProgress, ItemStack stack, float equippedProgress, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, CallbackInfo ci
    ) {
        poseStack.pushPose();
        boolean flag = hand == InteractionHand.MAIN_HAND;
        HumanoidArm humanoidarm = flag ? player.getMainArm() : player.getMainArm().getOpposite();
        if (stack.getItem() instanceof DrawableMap)
        {
            if (flag && offHandItem.isEmpty()) {
                renderTwoHandedMap(poseStack, buffer, combinedLight, pitch, equippedProgress, swingProgress);
            } else {
                renderOneHandedMap(poseStack, buffer, combinedLight, equippedProgress, humanoidarm, swingProgress, stack);
            }
            ci.cancel();
        }
    }

    @Inject(method = "renderMap", at = @At("HEAD"), cancellable = true)
    private void renderMap(PoseStack poseStack, MultiBufferSource buffer, int packedLight, ItemStack stack, CallbackInfo ci) {
        if (!stack.is(AllItems.DRAWABLE_MAP))
            return;

        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        poseStack.scale(0.38F, 0.38F, 0.38F);
        poseStack.translate(-0.5F, -0.5F, 0.0F);
        poseStack.scale(0.0078125F, 0.0078125F, 0.0078125F);
        MapItemSavedData mapitemsaveddata = DrawableMap.getSavedData(stack, minecraft.level);
        VertexConsumer vertexconsumer = buffer.getBuffer(mapitemsaveddata == null ? MAP_BACKGROUND : MAP_BACKGROUND_CHECKERBOARD);
        Matrix4f matrix4f = poseStack.last().pose();
        vertexconsumer.addVertex(matrix4f, -7.0F, 135.0F, 0.0F).setColor(-1).setUv(0.0F, 1.0F).setLight(packedLight);
        vertexconsumer.addVertex(matrix4f, 135.0F, 135.0F, 0.0F).setColor(-1).setUv(1.0F, 1.0F).setLight(packedLight);
        vertexconsumer.addVertex(matrix4f, 135.0F, -7.0F, 0.0F).setColor(-1).setUv(1.0F, 0.0F).setLight(packedLight);
        vertexconsumer.addVertex(matrix4f, -7.0F, -7.0F, 0.0F).setColor(-1).setUv(0.0F, 0.0F).setLight(packedLight);

        if (stack.getItem() instanceof DrawableMap map) {
            MapDrawingClient.getInstance().getMapRenderer().render(poseStack, buffer, stack.get(DataComponents.MAP_ID), mapitemsaveddata, false, packedLight);
        }

        ci.cancel();
    }
}
