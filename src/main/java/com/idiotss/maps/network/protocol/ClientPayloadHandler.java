package com.idiotss.maps.network.protocol;

import com.idiotss.maps.MapDrawingClient;
import com.idiotss.maps.MapRenderer;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ClientPayloadHandler {

    public static void handleDataOnMain(final MapItemDataPacket data, final IPayloadContext context) {
        MapRenderer maprenderer = MapDrawingClient.getInstance().getMapRenderer();
        MapId mapid = data.mapId();
        LocalPlayer player = (LocalPlayer) context.player();
        MapItemSavedData mapitemsaveddata = player.clientLevel.getMapData(mapid);
        if (mapitemsaveddata == null) {
            mapitemsaveddata = MapItemSavedData.createForClient(data.scale(), data.locked(), player.level().dimension());
            player.clientLevel.overrideMapData(mapid, mapitemsaveddata);
        }

        data.applyToMap(mapitemsaveddata);
        maprenderer.update(mapid, mapitemsaveddata);
    }
}
