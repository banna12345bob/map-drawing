package com.idiotss.maps.network.protocol;

import com.idiotss.maps.MapDrawing;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ServerPayloadHandler {

    public static void handleDataOnMain(final ServerboundMapItemDataPacket data, final IPayloadContext context) {
        Level level = context.player().level();
        MapDrawing.LOGGER.info("Handle data, {}", data.locked());
    }
}
