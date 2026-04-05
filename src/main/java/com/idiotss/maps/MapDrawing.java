package com.idiotss.maps;

import com.idiotss.maps.data.ModdedRecipeProvider;
import com.idiotss.maps.network.protocol.ClientPayloadHandler;
import com.idiotss.maps.network.protocol.ServerPayloadHandler;
import com.idiotss.maps.network.protocol.MapItemDataPacket;
import com.tterrag.registrate.Registrate;
import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.data.event.GatherDataEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

import java.util.concurrent.CompletableFuture;

@Mod(MapDrawing.MODID)
public class MapDrawing {
    public static final String MODID = "map_drawing";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final Registrate REGISTRATE = Registrate.create(MODID);

    public MapDrawing(IEventBus modEventBus, ModContainer modContainer) {
        AllDataComponents.register(modEventBus);

        AllLang.load();
        AllItems.load();

        modEventBus.addListener(MapDrawing::onPayloadHandlerEvent);
        modEventBus.addListener(EventPriority.LOWEST, MapDrawing::onGatherDataEvent);
    }

    @SubscribeEvent
    public static void onGatherDataEvent(final GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        CompletableFuture<HolderLookup.Provider> lookupProvider = event.getLookupProvider();

        generator.addProvider(
                event.includeServer(),
                new ModdedRecipeProvider(output, lookupProvider)
        );
    }

    @SubscribeEvent
    public static void onPayloadHandlerEvent(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1")
                .executesOn(HandlerThread.MAIN);
        registrar.playBidirectional(
            MapItemDataPacket.TYPE,
            MapItemDataPacket.STREAM_CODEC,
            new DirectionalPayloadHandler<>(
                ClientPayloadHandler::handleDataOnMain,
                ServerPayloadHandler::handleDataOnMain
            )
        );
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("HELLO from server starting");
    }
}
