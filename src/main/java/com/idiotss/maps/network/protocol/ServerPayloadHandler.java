package com.idiotss.maps.network.protocol;

import com.idiotss.maps.AllDataComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ServerPayloadHandler {

    public static void handleDataOnMain(final ServerboundMapItemDataPacket data, final IPayloadContext context) {
        Level level = context.player().level();
        MapItemSavedData savedData = level.getMapData(data.mapId());
        if (data.locked()) {
            savedData = savedData.locked();
        }

        MapId mapId = level.getFreeMapId();
        level.setMapData(mapId, savedData);
        ItemStack stack = context.player().getItemInHand(InteractionHand.MAIN_HAND);
        stack.set(DataComponents.MAP_ID, mapId);
        if (data.locked())
            stack.set(AllDataComponents.MAP_AUTHOR, context.player().getName().getString());
    }
}
