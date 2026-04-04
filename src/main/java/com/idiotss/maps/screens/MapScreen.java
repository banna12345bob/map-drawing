package com.idiotss.maps.screens;

import com.idiotss.maps.AllDataComponents;
import com.idiotss.maps.AllLang;
import com.idiotss.maps.MapDrawing;
import com.idiotss.maps.MapDrawingClient;
import com.idiotss.maps.item.DrawableMap;
import com.idiotss.maps.network.protocol.ServerboundMapItemDataPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.MapPostProcessing;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

public class MapScreen extends Screen {
    private int canvasX;
    private int canvasY;
    private final int canvasPixelWidth;
    private final int canvasPixelScale;

    private final ItemStack stack;
    private final MapId mapId;
    private final Player owner;
    private final Level level;
    private MapItemSavedData mapItemSavedData;

    private Button signButton;

    public MapScreen(ItemStack mapStack, Level level, Player owner) {
        super(AllLang.MAP_SCREEN);

        this.canvasPixelWidth = 128;
        this.canvasPixelScale = 1;

        this.stack = mapStack;
        this.mapId = mapStack.get(DataComponents.MAP_ID);
        this.mapItemSavedData = DrawableMap.getSavedData(mapId, level);
        this.owner = owner;
        this.level = level;
    }

    @Override
    protected void init() {
        this.signButton = this.addRenderableWidget(Button.builder(Component.translatable("book.signButton"), button -> {
            this.mapItemSavedData = this.mapItemSavedData.locked();
            updateButtonVisability();
            this.getMinecraft().setScreen(null);
            MapItemSavedData.MapPatch patch = new MapItemSavedData.MapPatch(0, 0, 128, 128, mapItemSavedData.colors);
            PacketDistributor.sendToServer(
                    new ServerboundMapItemDataPacket(mapId, mapItemSavedData.scale, mapItemSavedData.locked,
                            (Collection<MapDecoration>) mapItemSavedData.getDecorations(), patch)
            );
        }).bounds(this.width / 2 - 100, 196, 98, 20).build());
        this.signButton.visible = !mapItemSavedData.locked;
        canvasX = (this.width - canvasPixelWidth * canvasPixelScale) / 2;
        canvasY = this.height / 4;
    }

    private void updateButtonVisability() {
        this.signButton.visible = stack.get(DataComponents.MAP_POST_PROCESSING) != MapPostProcessing.LOCK && !this.mapItemSavedData.locked;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        if (!level.isClientSide)
            return;
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.pose().translate(canvasX, canvasY, 0);
        MapDrawingClient.getInstance().getMapRenderer().render(guiGraphics.pose(), guiGraphics.bufferSource(), mapId, mapItemSavedData, true, 255);

//        stack.set(AllDataComponents.MAP_AUTHOR, owner.getName().getString());
    }
}
