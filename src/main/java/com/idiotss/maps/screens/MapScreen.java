package com.idiotss.maps.screens;

import com.idiotss.maps.AllLang;
import com.idiotss.maps.MapDrawingClient;
import com.idiotss.maps.item.DrawableMap;
import com.idiotss.maps.network.protocol.MapItemDataPacket;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.MapPostProcessing;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;

@OnlyIn(Dist.CLIENT)
public class MapScreen extends Screen {
    private int canvasX;
    private int canvasY;
    private final int canvasPixelWidth;
    private final int canvasPixelScale;

    private final ItemStack stack;
    private final MapId mapId;
    private final MapItemSavedData mapItemSavedData;

    private Button signButton;

    public MapScreen(ItemStack mapStack, Level level) {
        super(AllLang.MAP_SCREEN);

        this.canvasPixelWidth = 128;
        this.canvasPixelScale = 1;

        this.stack = mapStack;
        this.mapId = mapStack.get(DataComponents.MAP_ID);
        this.mapItemSavedData = DrawableMap.getSavedData(mapId, level);
    }

    @Override
    protected void init() {
        this.signButton = this.addRenderableWidget(Button.builder(Component.translatable("book.signButton"), button -> {
            updateButtonVisability();
            saveData(true);
            this.getMinecraft().setScreen(null);
        }).bounds(this.width / 2 - 100, 196, 98, 20).build());
        canvasX = (this.width - canvasPixelWidth * canvasPixelScale) / 2;
        canvasY = this.height / 6;
        updateButtonVisability();
    }

    @Override
    public void onClose() {
        this.saveData(false);
        super.onClose();
    }

    private void saveData(boolean publish) {
        MapItemSavedData savedData = mapItemSavedData;
        if (publish)
            savedData = savedData.locked();

        MapItemSavedData.MapPatch patch = new MapItemSavedData.MapPatch(0, 0, 128, 128, savedData.colors);
        Collection<MapDecoration> decorations = (Collection<MapDecoration>) savedData.getDecorations();
        PacketDistributor.sendToServer(
                new MapItemDataPacket(mapId, savedData.scale, savedData.locked, decorations, patch)
        );
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
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(canvasX, canvasY, 0);

//        We have to flip the pose so that the decorations are rendered correctly
        guiGraphics.pose().scale(1, 1, -1);
        MapDrawingClient.getInstance().getMapRenderer().render(guiGraphics.pose(), guiGraphics.bufferSource(), mapId, mapItemSavedData, false, 255);
        guiGraphics.pose().popPose();

        for (int x = 0; x < 128; x++) {
            for (int y = 0; y < 128; y++) {
                mapItemSavedData.setColor(x, y, MapColor.COLOR_BLACK.getPackedId(MapColor.Brightness.NORMAL));
            }
        }

        guiGraphics.pose().translate(0, 0, 2);
        guiGraphics.drawString(this.getMinecraft().font, String.format("%s, %s", mouseX, mouseY), mouseX, mouseY, 0xFFFFFFFF);
    }
}
