package com.idiotss.maps;

import net.minecraft.network.chat.MutableComponent;

import static com.idiotss.maps.MapDrawing.REGISTRATE;
public class AllLang {
    public static final MutableComponent MAP_SCREEN = REGISTRATE.addRawLang("screen.map_drawing.map_screen", "Map Screen");
    public static final MutableComponent BRUSH_SIZE = REGISTRATE.addRawLang("screen.map_drawing.map_screen.brush_size", "Brush size: ");

    public static void load() {}
}
