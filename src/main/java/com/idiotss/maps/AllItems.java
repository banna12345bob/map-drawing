package com.idiotss.maps;

import com.idiotss.maps.item.DrawableMap;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.level.saveddata.maps.MapId;

import static com.idiotss.maps.MapDrawing.REGISTRATE;

public class AllItems {
    public static final ItemEntry<DrawableMap> DRAWABLE_MAP =
            REGISTRATE.item("drawable_map", DrawableMap::new)
                    .properties(p -> p
                            .component(DataComponents.MAP_ID, new MapId(0))
                            .component(DataComponents.MAP_POST_PROCESSING, null)
                            .component(AllDataComponents.MAP_AUTHOR, "")
                    )
                    .register();

    public static void load() {}
}
