package com.idiotss.maps;

import com.idiotss.maps.item.DrawableMap;
import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.RenderItemInFrameEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = MapDrawing.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = MapDrawing.MODID, value = Dist.CLIENT)
public class MapDrawingClient {
    public MapDrawingClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void renderMapInItemFrame(RenderItemInFrameEvent event)
    {
        if (event.getItemStack().getItem() instanceof DrawableMap map && map.getTextureLoc() != null) {
            event.getPoseStack().pushPose();
            event.getPoseStack().mulPose(Axis.ZP.rotationDegrees(180.0F));
            event.getPoseStack().scale(0.0078125F, 0.0078125F, 0.0078125F);
            event.getPoseStack().translate(-64.0F, -64.0F, 0.0F);
            event.getPoseStack().translate(0.0F, 0.0F, -1.0F);
            RenderSystem.setShaderTexture(0, map.getTextureLoc());
            VertexConsumer front = event.getMultiBufferSource().getBuffer(RenderType.entitySolid(map.getTextureLoc()));
            PoseStack.Pose pose = event.getPoseStack().last();
            addVertex(front, pose, 0.0F, 32.0F * 4, -1.0F, 0.0F, 1.0F, event.getPackedLight(), 0.0F, 0.0F, -1.0F);
            addVertex(front, pose, 32.0F * 4, 32.0F * 4, -1.0F, 1.0F, 1.0F, event.getPackedLight(), 0.0F, 0.0F, -1.0F);
            addVertex(front, pose, 32.0F * 4, 0.0F, -1.0F, 1.0F, 0.0F, event.getPackedLight(), 0.0F, 0.0F, -1.0F);
            addVertex(front, pose, 0.0F, 0.0F, -1.0F, 0.0F, 0.0F, event.getPackedLight(), 0.0F, 0.0F, -1.0F);

            event.getPoseStack().popPose();
            event.setCanceled(true);
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

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {}
}
