package com.idiotss.maps.screens;

import com.idiotss.maps.MapDrawingClient;
import com.idiotss.maps.item.DrawableMap;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.NotNull;

public class MapScreen extends Screen {
    private int canvasX;
    private int canvasY;
    private final int canvasPixelWidth;
    private final int canvasPixelScale;

    private final MapId mapId;
    private final MapItemSavedData mapItemSavedData;

    public MapScreen(ItemStack mapStack, Component title, Level level) {
        super(title);

        this.canvasPixelWidth = 128;
        this.canvasPixelScale = 1;

        mapId = mapStack.get(DataComponents.MAP_ID);
        mapItemSavedData = DrawableMap.getSavedData(mapId, level);
    }

    @Override
    protected void init() {
        canvasX = (this.width - canvasPixelWidth * canvasPixelScale) / 2;
        canvasY = this.height / 4;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.pose().translate(canvasX, canvasY, 0);
        MapDrawingClient.getInstance().getMapRenderer().render(guiGraphics.pose(), guiGraphics.bufferSource(), mapId, mapItemSavedData, true, 255);
    }
}
