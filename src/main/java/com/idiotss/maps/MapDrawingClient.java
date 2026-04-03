package com.idiotss.maps;

import com.idiotss.maps.item.DrawableMap;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
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
    private MapRenderer mapRenderer;
    private static MapDrawingClient instance;

    public MapDrawingClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
        instance = this;
    }

    public static MapDrawingClient getInstance() {
        return instance;
    }

    public MapRenderer getMapRenderer() {
        return mapRenderer;
    }

    @SubscribeEvent
    static void renderMapInItemFrame(RenderItemInFrameEvent event)
    {
        if (event.getItemStack().getItem() instanceof DrawableMap map) {
            event.getPoseStack().pushPose();
            event.getPoseStack().mulPose(Axis.ZP.rotationDegrees(180.0F));
            event.getPoseStack().scale(0.0078125F, 0.0078125F, 0.0078125F);
            event.getPoseStack().translate(-64.0F, -64.0F, 0.0F);
            event.getPoseStack().translate(0.0F, 0.0F, -1.0F);
            getInstance().getMapRenderer().render(event.getPoseStack(), event.getMultiBufferSource(),
                    event.getItemStack().get(DataComponents.MAP_ID), DrawableMap.getSavedData(event.getItemStack(), event.getItemFrameEntity().level()), true, event.getPackedLight());
            event.getPoseStack().popPose();
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        getInstance().mapRenderer = new MapRenderer(Minecraft.getInstance().getTextureManager(), Minecraft.getInstance().getMapDecorationTextures());
    }
}
