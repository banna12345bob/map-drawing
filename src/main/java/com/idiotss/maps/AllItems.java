package com.idiotss.maps;

import com.idiotss.maps.item.DrawableMap;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.MapDecorations;
import net.minecraft.world.item.component.MapItemColor;

import static com.idiotss.maps.MapDrawing.REGISTRATE;

public class AllItems {
    public static final ItemEntry<Item> EXAMPLE_ITEM =
            REGISTRATE.item("example_item", Item::new)
                    .properties(p -> p.food(new FoodProperties.Builder().alwaysEdible().nutrition(1).saturationModifier(2f).build()))
                    .register();

    public static final ItemEntry<DrawableMap> DRAWABLE_MAP =
            REGISTRATE.item("drawable_map", DrawableMap::new)
                    .properties(p -> p.stacksTo(1).component(DataComponents.MAP_COLOR, MapItemColor.DEFAULT).component(DataComponents.MAP_DECORATIONS, MapDecorations.EMPTY))
                    .register();

    public static void load() {}
}
