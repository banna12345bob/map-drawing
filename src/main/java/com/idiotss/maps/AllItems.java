package com.idiotss.maps;

import com.idiotss.maps.item.DrawableMap;
import com.tterrag.registrate.util.entry.ItemEntry;

import java.util.ArrayList;

import static com.idiotss.maps.MapDrawing.REGISTRATE;

public class AllItems {
    public static final ItemEntry<DrawableMap> DRAWABLE_MAP =
            REGISTRATE.item("drawable_map", DrawableMap::new)
                    .properties(p -> p
                            .component(AllDataComponents.MAP_PIXELS, new ArrayList<>())
                            .component(AllDataComponents.MAP_AUTHOR, "")
                    )
                    .register();

    public static void load() {}
}
