package com.idiotss.maps;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureAtlasSprite;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.client.resources.MapDecorationTextureManager;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.gui.map.MapDecorationRendererManager;

@OnlyIn(Dist.CLIENT)
public class MapRenderer implements AutoCloseable {
    private static final int WIDTH = 128;
    private static final int HEIGHT = 128;
    final TextureManager textureManager;
    final MapDecorationTextureManager decorationTextures;
    private final Int2ObjectMap<MapRenderer.MapInstance> maps = new Int2ObjectOpenHashMap<>();

    public MapRenderer(TextureManager textureManager, MapDecorationTextureManager decorationTextures) {
        this.textureManager = textureManager;
        this.decorationTextures = decorationTextures;
    }

    public void update(MapId mapId, MapItemSavedData mapData) {
        this.getOrCreateMapInstance(mapId, mapData).forceUpload();
    }

    public void render(PoseStack poseStack, MultiBufferSource buffer, MapId mapId, MapItemSavedData mapData, boolean active, int packedLight) {
        this.getOrCreateMapInstance(mapId, mapData).draw(poseStack, buffer, active, packedLight);
    }

    private MapRenderer.MapInstance getOrCreateMapInstance(MapId mapId, MapItemSavedData mapData) {
        return this.maps.compute(mapId.id(), (p_182563_, p_182564_) -> {
            if (p_182564_ == null) {
                return new MapRenderer.MapInstance(p_182563_, mapData);
            } else {
                p_182564_.replaceMapData(mapData);
                return (MapRenderer.MapInstance)p_182564_;
            }
        });
    }

    public void resetData() {
        for (MapInstance mapinstance : this.maps.values()) {
            mapinstance.close();
        }

        this.maps.clear();
    }

    @Override
    public void close() {
        this.resetData();
    }

    @OnlyIn(Dist.CLIENT)
    class MapInstance implements AutoCloseable {
        private MapItemSavedData data;
        private final DynamicTexture texture;
        private final RenderType renderType;
        private boolean requiresUpload = true;

        MapInstance(int id, MapItemSavedData data) {
            this.data = data;
            this.texture = new DynamicTexture(128, 128, true);
            ResourceLocation resourcelocation = MapRenderer.this.textureManager.register("map/" + id, this.texture);
            this.renderType = RenderType.text(resourcelocation);
        }

        void replaceMapData(MapItemSavedData data) {
            boolean flag = this.data != data;
            this.data = data;
            this.requiresUpload |= flag;
        }

        public void forceUpload() {
            this.requiresUpload = true;
        }

        private void updateTexture() {
            if (this.data == null)
                return;
            for (int i = 0; i < 128; i++) {
                for (int j = 0; j < 128; j++) {
                    int k = j + i * 128;
                    this.texture.getPixels().setPixelRGBA(j, i, MapColor.getColorFromPackedId(this.data.colors[k]));
                }
            }

            this.texture.upload();
        }

        private void draw(PoseStack poseStack, MultiBufferSource bufferSource, boolean active, int packedLight) {
            if (this.requiresUpload) {
                this.updateTexture();
                this.requiresUpload = false;
            }

            poseStack.pushPose();
            VertexConsumer front = bufferSource.getBuffer(this.renderType);
            PoseStack.Pose pose = poseStack.last();
            addVertex(front, pose, 0.0F, HEIGHT, -1.0F, 0.0F, 1.0F, packedLight, 0.0F, 0.0F, -1.0F);
            addVertex(front, pose, WIDTH, HEIGHT, -1.0F, 1.0F, 1.0F, packedLight, 0.0F, 0.0F, -1.0F);
            addVertex(front, pose, WIDTH, 0.0F, -1.0F, 1.0F, 0.0F, packedLight, 0.0F, 0.0F, -1.0F);
            addVertex(front, pose, 0.0F, 0.0F, -1.0F, 0.0F, 0.0F, packedLight, 0.0F, 0.0F, -1.0F);

            poseStack.popPose();

            if (this.data == null)
                return;

            int k = 1;
            for (MapDecoration mapdecoration : this.data.getDecorations()) {
                if (!active || mapdecoration.renderOnFrame()) {
                    if (MapDecorationRendererManager.render(mapdecoration, poseStack, bufferSource, data, MapRenderer.this.decorationTextures, active, packedLight, k)) {
                        k++;
                        continue;
                    }
                    poseStack.pushPose();
                    poseStack.translate(0.0F + (float)mapdecoration.x() / 2.0F + 64.0F, 0.0F + (float)mapdecoration.y() / 2.0F + 64.0F, -0.02F);
                    poseStack.mulPose(Axis.ZP.rotationDegrees((float)(mapdecoration.rot() * 360) / 16.0F));
                    poseStack.scale(4.0F, 4.0F, 3.0F);
                    poseStack.translate(-0.125F, 0.125F, 0.0F);
                    float f1 = -1.001F;
                    TextureAtlasSprite textureatlassprite = MapRenderer.this.decorationTextures.get(mapdecoration);
                    float f2 = textureatlassprite.getU0();
                    float f3 = textureatlassprite.getV0();
                    float f4 = textureatlassprite.getU1();
                    float f5 = textureatlassprite.getV1();
                    pose = poseStack.last();
                    VertexConsumer vertexconsumer1 = bufferSource.getBuffer(RenderType.text(textureatlassprite.atlasLocation()));
                    addVertex(vertexconsumer1, pose, -1.0F, 1.0F, (float)k * f1, f2, f3, packedLight, 0.0f, 0.0f, -1.f);
                    addVertex(vertexconsumer1, pose, 1.0F, 1.0F, (float)k * f1, f4, f3, packedLight, 0.0f, 0.0f, -1.f);
                    addVertex(vertexconsumer1, pose, 1.0F, -1.0F, (float)k * f1, f4, f5, packedLight, 0.0f, 0.0f, -1.f);
                    addVertex(vertexconsumer1, pose, -1.0F, -1.0F, (float)k * f1, f2, f5, packedLight, 0.0f, 0.0f, -1.f);
                    poseStack.popPose();
                    if (mapdecoration.name().isPresent()) {
                        Font font = Minecraft.getInstance().font;
                        Component component = mapdecoration.name().get();
                        float f6 = (float)font.width(component);
                        float f7 = Mth.clamp(25.0F / f6, 0.0F, 6.0F / 9.0F);
                        poseStack.pushPose();
                        poseStack.translate(
                                0.0F + (float)mapdecoration.x() / 2.0F + 64.0F - f6 * f7 / 2.0F, 0.0F + (float)mapdecoration.y() / 2.0F + 64.0F + 4.0F, -0.025F
                        );
                        poseStack.scale(f7, f7, 1.0F);
                        poseStack.translate(0.0F, 0.0F, -0.1F);
                        font.drawInBatch(
                                component, 0.0F, 0.0F, -1, false, poseStack.last().pose(), bufferSource, Font.DisplayMode.NORMAL, Integer.MIN_VALUE, packedLight
                        );
                        poseStack.popPose();
                    }

                    k++;
                }
            }
        }

        private static void addVertex(VertexConsumer vb, PoseStack.Pose pose, double x, double y, double z, float tx, float ty, int lightmap, float nx, float ny, float nz) {
            vb.addVertex(pose, (float) x, (float) y, (float) z)
                    .setColor(255, 255, 255, 255)
                    .setUv(tx, ty)
                    .setOverlay(OverlayTexture.NO_OVERLAY)
                    .setLight(lightmap)
                    .setNormal(pose, nx, ny, nz);
        }

        @Override
        public void close() {
            this.texture.close();
        }
    }
}
