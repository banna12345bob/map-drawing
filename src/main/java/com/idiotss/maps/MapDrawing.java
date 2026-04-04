package com.idiotss.maps;

import com.idiotss.maps.network.protocol.ServerPayloadHandler;
import com.idiotss.maps.network.protocol.ServerboundMapItemDataPacket;
import com.tterrag.registrate.Registrate;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.handling.MainThreadPayloadHandler;
import net.neoforged.neoforge.network.registration.HandlerThread;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

@Mod(MapDrawing.MODID)
public class MapDrawing {
    public static final String MODID = "map_drawing";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final Registrate REGISTRATE = Registrate.create(MODID);

    public MapDrawing(IEventBus modEventBus, ModContainer modContainer) {
        AllDataComponents.register(modEventBus);

        // We have to load the creative tab first otherwise it won't load correctly
//        AllBlocks.load();
        AllItems.load();

//        modContainer.registerConfig(ModConfig.Type.COMMON, Config.SPEC);
        modEventBus.addListener(MapDrawing::onPayloadHandlerEvent);
    }

    @SubscribeEvent
    public static void onPayloadHandlerEvent(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1")
                .executesOn(HandlerThread.MAIN);
        registrar.playBidirectional(
            ServerboundMapItemDataPacket.TYPE,
            ServerboundMapItemDataPacket.STREAM_CODEC,
            new DirectionalPayloadHandler<>(
                null,
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
