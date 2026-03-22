package com.idiotss.maps.item;

import com.idiotss.maps.AllItems;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.ComplexItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;

import java.util.function.Consumer;

public class DrawableMap extends ComplexItem {
    public DrawableMap(Properties properties) {
        super(properties);
    }

    private static MapId createNewSavedData(Level level, int x, int z, int scale, boolean trackingPosition, boolean unlimitedTracking, ResourceKey<Level> dimension) {
        MapItemSavedData mapitemsaveddata = MapItemSavedData.createFresh((double)x, (double)z, (byte)scale, trackingPosition, unlimitedTracking, dimension);
        MapId mapid = level.getFreeMapId();
        level.setMapData(mapid, mapitemsaveddata);
        return mapid;
    }

    public static ItemStack create(Level level, int levelX, int levelZ, byte scale, boolean trackingPosition, boolean unlimitedTracking) {
        ItemStack itemstack = new ItemStack(AllItems.DRAWABLE_MAP.asItem());
        MapId mapid = createNewSavedData(level, levelX, levelZ, scale, trackingPosition, unlimitedTracking, level.dimension());
        itemstack.set(DataComponents.MAP_ID, mapid);
        return itemstack;
    }
}
