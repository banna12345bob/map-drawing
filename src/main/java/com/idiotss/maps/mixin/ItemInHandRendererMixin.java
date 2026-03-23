package com.idiotss.maps.mixin;

import com.idiotss.maps.MapDrawing;
import com.idiotss.maps.item.DrawableMap;
import com.mojang.blaze3d.platform.NativeImage;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.ItemInHandRenderer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.HumanoidArm;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.awt.*;
import java.util.List;

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
    @Unique
    private int[] map_drawing$pixels;

    @Inject(method = "renderArmWithItem", at = @At(value = "INVOKE", target = "Lcom/mojang/blaze3d/vertex/PoseStack;pushPose()V"), cancellable = true)
    private void renderArmWithItem(
            AbstractClientPlayer player, float partialTicks, float pitch, InteractionHand hand, float swingProgress, ItemStack stack, float equippedProgress, PoseStack poseStack, MultiBufferSource buffer, int combinedLight, CallbackInfo ci
    ) {
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

    private int getPixelAt(int x, int y) {
        return (this.map_drawing$pixels == null || map_drawing$pixels.length == 0) ? 0xFFF9FFFE : this.map_drawing$pixels[y * 32 + x];
    }

    private int swapColor(int color) {
        int i = (color & 16711680) >> 16;
        int j = (color & '\uff00') >> 8;
        int k = (color & 255);
        return k << 16 | j << 8 | i | 0xff000000;
    }

    @Inject(method = "renderMap", at = @At("HEAD"), cancellable = true)
    private void renderMap(PoseStack poseStack, MultiBufferSource buffer, int packedLight, ItemStack stack, CallbackInfo ci) {
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(180.0F));
        poseStack.scale(0.38F, 0.38F, 0.38F);
        poseStack.translate(-0.5F, -0.5F, 0.0F);
        poseStack.scale(0.0078125F, 0.0078125F, 0.0078125F);
        List<Integer> savedData = DrawableMap.getSavedData(stack);
        if (savedData != null) {
            map_drawing$pixels = DrawableMap.getSavedData(stack).stream().mapToInt(i -> i).toArray();
        }
        VertexConsumer vertexconsumer = buffer.getBuffer(map_drawing$pixels == null ? MAP_BACKGROUND : MAP_BACKGROUND_CHECKERBOARD);
        Matrix4f matrix4f = poseStack.last().pose();
        vertexconsumer.addVertex(matrix4f, -7.0F, 135.0F, 0.0F).setColor(-1).setUv(0.0F, 1.0F).setLight(packedLight);
        vertexconsumer.addVertex(matrix4f, 135.0F, 135.0F, 0.0F).setColor(-1).setUv(1.0F, 1.0F).setLight(packedLight);
        vertexconsumer.addVertex(matrix4f, 135.0F, -7.0F, 0.0F).setColor(-1).setUv(1.0F, 0.0F).setLight(packedLight);
        vertexconsumer.addVertex(matrix4f, -7.0F, -7.0F, 0.0F).setColor(-1).setUv(0.0F, 0.0F).setLight(packedLight);

        DynamicTexture texture = new DynamicTexture(32, 32, true);
        ResourceLocation location = Minecraft.getInstance().getTextureManager().register("canvas/test", texture);

        NativeImage image = texture.getPixels();
        if (image != null) {
            for (int y = 0; y < 32; y++) {
                for (int x = 0; x < 32; x++) {
                    // For some reason in the format 0xAABBGGRR so we have to swap it
                    image.setPixelRGBA(x, y, swapColor(getPixelAt(x, y)));
                }
            }

            texture.upload();
        }

        poseStack.pushPose();
        RenderSystem.setShaderTexture(0, location);
        VertexConsumer front = buffer.getBuffer(RenderType.entitySolid(location));
        PoseStack.Pose pose = poseStack.last();
        addVertex(front, pose, 0.0F, 32.0F * 4, -1.0F, 0.0F, 1.0F, packedLight, 0.0F, 0.0F, -1.0F);
        addVertex(front, pose, 32.0F * 4, 32.0F * 4, -1.0F, 1.0F, 1.0F, packedLight, 0.0F, 0.0F, -1.0F);
        addVertex(front, pose, 32.0F * 4, 0.0F, -1.0F, 1.0F, 0.0F, packedLight, 0.0F, 0.0F, -1.0F);
        addVertex(front, pose, 0.0F, 0.0F, -1.0F, 0.0F, 0.0F, packedLight, 0.0F, 0.0F, -1.0F);

        poseStack.popPose();

        ci.cancel();
    }

    private void addVertex(VertexConsumer vb, PoseStack.Pose pose, double x, double y, double z, float tx, float ty, int lightmap, float nx, float ny, float nz) {
        vb.addVertex(pose, (float) x, (float) y, (float) z)
                .setColor(255, 255, 255, 255)
                .setUv(tx, ty)
                .setOverlay(OverlayTexture.NO_OVERLAY)
                .setLight(lightmap)
                .setNormal(pose, nx, ny, nz);
    }
}
