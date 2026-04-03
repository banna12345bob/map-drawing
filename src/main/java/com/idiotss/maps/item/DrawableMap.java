package com.idiotss.maps.item;

import com.idiotss.maps.AllItems;
import com.idiotss.maps.screens.MapScreen;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.Packet;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ComplexItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.MapPostProcessing;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.NotNull;

import javax.annotation.Nullable;
import java.util.List;

public class DrawableMap extends ComplexItem {
    public DrawableMap(Properties properties) {
        super(properties);
    }

    @Nullable
    public static MapItemSavedData getSavedData(@Nullable MapId mapId, Level level) {
        return mapId == null ? null : level.getMapData(mapId);
    }

    @Nullable
    public static MapItemSavedData getSavedData(ItemStack stack, Level level) {
        Item map = stack.getItem();
        if(map instanceof DrawableMap) {
            return ((DrawableMap)map).getCustomMapData(stack, level);
        }
        return null;
    }

    @Nullable
    protected MapItemSavedData getCustomMapData(ItemStack stack, Level level) {
        MapId mapid = stack.get(DataComponents.MAP_ID);
        return getSavedData(mapid, level);
    }

    public static ItemStack create(Level level, int levelX, int levelZ) {
        ItemStack itemstack = new ItemStack(AllItems.DRAWABLE_MAP.asItem());
        MapId mapid = createNewSavedData(level, levelX, levelZ, level.dimension());
        itemstack.set(DataComponents.MAP_ID, mapid);
        return itemstack;
    }

    private static MapId createNewSavedData(
            Level level, int x, int z, ResourceKey<Level> dimension
    ) {
        MapItemSavedData mapitemsaveddata = MapItemSavedData.createFresh((double)x, (double)z, (byte) (int) (byte) 0, true, false, dimension);
        MapId mapid = level.getFreeMapId();
        level.setMapData(mapid, mapitemsaveddata);
        return mapid;
    }

    @NotNull
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, @NotNull Player player, @NotNull InteractionHand usedHand) {
//        Some garbage testing data
//        for (int x = 0; x < 128; x++) {
//            for (int y = 0; y < 128; y++) {
//                level.getMapData(player.getItemInHand(usedHand).get(DataComponents.MAP_ID)).setColor(x, y, MapColor.COLOR_BROWN.getPackedId(MapColor.Brightness.NORMAL));
//            }
//        }
        if (level.isClientSide()) {
            Minecraft.getInstance().setScreen(new MapScreen(player.getItemInHand(usedHand), Component.translatable("block.mapdrawer.example_block"), level));
        }
        return super.use(level, player, usedHand);
    }

    @Override
    public void onCraftedPostProcess(ItemStack stack, Level level) {
        MapPostProcessing mappostprocessing = stack.remove(DataComponents.MAP_POST_PROCESSING);
        if (mappostprocessing != null) {
            switch (mappostprocessing) {
                case LOCK:
                    break;
                case SCALE:
                    scaleMap(stack, level);
            }
        }
    }

    private static void scaleMap(ItemStack stack, Level level) {
        MapItemSavedData mapitemsaveddata = getSavedData(stack, level);
        if (mapitemsaveddata != null) {
            MapId mapid = level.getFreeMapId();
            level.setMapData(mapid, mapitemsaveddata.scaled());
            stack.set(DataComponents.MAP_ID, mapid);
        }
    }

    @Override
    public void inventoryTick(@NotNull ItemStack stack, Level level, @NotNull Entity entity, int slotId, boolean isSelected) {
        if (!level.isClientSide) {
            MapItemSavedData mapitemsaveddata = getSavedData(stack, level);
            if (mapitemsaveddata != null) {
                if (entity instanceof Player player) {
                    mapitemsaveddata.tickCarriedBy(player, stack);
                }
            }
        }
    }

    @Nullable
    @Override
    public Packet<?> getUpdatePacket(ItemStack stack, @NotNull Level level, @NotNull Player player) {
        MapId mapid = stack.get(DataComponents.MAP_ID);
        MapItemSavedData mapitemsaveddata = getSavedData(mapid, level);
        return mapitemsaveddata != null ? mapitemsaveddata.getUpdatePacket(mapid, player) : null;
    }

    @Override
    public void appendHoverText(ItemStack stack, @NotNull TooltipContext context, @NotNull List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        MapId mapid = stack.get(DataComponents.MAP_ID);
        MapItemSavedData mapitemsaveddata = mapid != null ? context.mapData(mapid) : null;
        MapPostProcessing mappostprocessing = stack.get(DataComponents.MAP_POST_PROCESSING);

        if (tooltipFlag.isAdvanced()) {
            if (mapitemsaveddata != null) {
                if (mappostprocessing == null) {
                    tooltipComponents.add(Component.translatable("filled_map.id", mapid.id()).withStyle(ChatFormatting.GRAY));
                }

                int i = mappostprocessing == MapPostProcessing.SCALE ? 1 : 0;
                int j = Math.min(mapitemsaveddata.scale + i, 4);
                tooltipComponents.add(Component.translatable("filled_map.scale", 1 << j).withStyle(ChatFormatting.GRAY));
                tooltipComponents.add(Component.translatable("filled_map.level", j, 4).withStyle(ChatFormatting.GRAY));
            } else {
                tooltipComponents.add(Component.translatable("filled_map.unknown").withStyle(ChatFormatting.GRAY));
            }
        }
    }
}
