package com.idiotss.maps;

import com.tterrag.registrate.Registrate;
import org.slf4j.Logger;

import com.mojang.logging.LogUtils;

import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
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
    }

    // You can use SubscribeEvent and let the Event Bus discover methods to call
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("HELLO from server starting");
    }
}
