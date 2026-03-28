package com.idiotss.maps;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import it.unimi.dsi.fastutil.ints.Int2ObjectMap;
import it.unimi.dsi.fastutil.ints.Int2ObjectOpenHashMap;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import java.util.List;

@OnlyIn(Dist.CLIENT)
public class MapRenderer implements AutoCloseable {
    private static final int WIDTH = 128;
    private static final int HEIGHT = 128;
    private final Int2ObjectMap<MapInstance> maps = new Int2ObjectOpenHashMap<>();
    final TextureManager textureManager;

    MapRenderer(TextureManager textureManager) {
        this.textureManager = textureManager;
    }

    public void update(MapId mapId, List<Integer> pixels) {
        this.getOrCreateMapInstance(mapId, pixels).forceUpload();
    }

    public void render(PoseStack poseStack, MultiBufferSource buffer, MapId mapId, List<Integer> mapData, boolean active, int packedLight) {
        this.getOrCreateMapInstance(mapId, mapData).draw(poseStack, buffer, active, packedLight);
    }

    private MapRenderer.MapInstance getOrCreateMapInstance(MapId mapId, List<Integer> pixels) {
        return this.maps.compute(mapId.id(), (id, mapInstance) -> {
            if (mapInstance == null) {
                return new MapInstance(id, pixels);
            } else {
                mapInstance.updatePixels(pixels);
                return mapInstance;
            }
        });
    }

    @Override
    public void close() throws Exception {
        for (MapInstance maprenderer$mapinstance : this.maps.values()) {
            maprenderer$mapinstance.close();
        }

        this.maps.clear();
    }

    private class MapInstance implements AutoCloseable {
        private int[] pixels;
        private final DynamicTexture texture;
        private final RenderType renderType;
        private boolean requiresUpload = true;

        MapInstance(int id, List<Integer> pixels) {
            this.pixels = pixels.stream().mapToInt(i -> i).toArray();
            this.texture = new DynamicTexture(WIDTH, HEIGHT, true);
            ResourceLocation resourceLocation = MapRenderer.this.textureManager.register("drawable_map/" + id, this.texture);
            this.renderType = RenderType.text(resourceLocation);
        }

        void updatePixels(List<Integer> pixels) {
            int[] tempPixels = pixels.stream().mapToInt(i -> i).toArray();
            boolean flag = this.pixels != tempPixels;
            this.pixels = tempPixels;
            this.requiresUpload |= flag;
        }

        void forceUpload() {
            this.requiresUpload = true;
        }

        private int getPixelAt(int x, int y) {
            return (this.pixels == null || pixels.length == 0) ? 0xFFF9FFFE : this.pixels[y * HEIGHT + x];
        }

        private static int swapColor(int color) {
            int i = (color & 16711680) >> 16;
            int j = (color & '\uff00') >> 8;
            int k = (color & 255);
            return k << 16 | j << 8 | i | 0xff000000;
        }

        private void updateTexture() {
            for (int y = 0; y < HEIGHT; y++) {
                for (int x = 0; x < WIDTH; x++) {
                    // For some reason in the format 0xAABBGGRR so we have to swap it
                    this.texture.getPixels().setPixelRGBA(x, y, swapColor(getPixelAt(x, y)));
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
        public void close() throws Exception {
            texture.close();
        }
    }
}
