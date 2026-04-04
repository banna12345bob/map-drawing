package com.idiotss.maps.network.protocol;

import com.idiotss.maps.MapDrawing;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;

import javax.annotation.Nullable;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

public record ServerboundMapItemDataPacket(
        MapId mapId, byte scale, boolean locked, Optional<List<MapDecoration>> decorations, Optional<MapItemSavedData.MapPatch> colorPatch
) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<ServerboundMapItemDataPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(MapDrawing.MODID, "map_item_data"));

    public static final StreamCodec<RegistryFriendlyByteBuf, ServerboundMapItemDataPacket> STREAM_CODEC = StreamCodec.composite(
            MapId.STREAM_CODEC,
            ServerboundMapItemDataPacket::mapId,
            ByteBufCodecs.BYTE,
            ServerboundMapItemDataPacket::scale,
            ByteBufCodecs.BOOL,
            ServerboundMapItemDataPacket::locked,
            MapDecoration.STREAM_CODEC.apply(ByteBufCodecs.list()).apply(ByteBufCodecs::optional),
            ServerboundMapItemDataPacket::decorations,
            MapItemSavedData.MapPatch.STREAM_CODEC,
            ServerboundMapItemDataPacket::colorPatch,
            ServerboundMapItemDataPacket::new
    );

    public ServerboundMapItemDataPacket(
            MapId mapId, byte scale, boolean locked, @Nullable Collection<MapDecoration> decorations, @Nullable MapItemSavedData.MapPatch colorPatch
    ) {
        this(mapId, scale, locked, decorations != null ? Optional.of(List.copyOf(decorations)) : Optional.empty(), Optional.ofNullable(colorPatch));
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
