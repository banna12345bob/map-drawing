package com.idiotss.maps.network.protocol;

import com.idiotss.maps.AllDataComponents;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.IPayloadContext;

public class ServerPayloadHandler {

    public static void handleDataOnMain(final MapItemDataPacket data, final IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        Level level = player.level();
        MapItemSavedData savedData = level.getMapData(data.mapId());
        if (savedData.locked)
            return;

        MapId mapId = data.mapId();
        if (data.locked()) {
            savedData = savedData.locked();
            mapId = level.getFreeMapId();
        }
        if (data.colorPatch().isPresent()) {
            savedData.colors = data.colorPatch().get().mapColors();
            PacketDistributor.sendToAllPlayers(new MapItemDataPacket(mapId, data.scale(), data.locked(), data.decorations(), data.colorPatch()));
        }

        level.setMapData(mapId, savedData);
        ItemStack stack = player.getItemInHand(InteractionHand.MAIN_HAND);
        stack.set(DataComponents.MAP_ID, mapId);
        if (data.locked())
            stack.set(AllDataComponents.MAP_AUTHOR, player.getName().getString());
    }
}
