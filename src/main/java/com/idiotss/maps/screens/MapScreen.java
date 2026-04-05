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
import net.minecraft.world.level.Level;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.saveddata.maps.MapDecoration;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.gui.widget.ExtendedSlider;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.util.Collection;

import static org.lwjgl.glfw.GLFW.*;

@OnlyIn(Dist.CLIENT)
public class MapScreen extends Screen {
    private int canvasX;
    private int canvasY;
    private final int canvasPixelWidth;
    private final int canvasPixelScale;

    private final MapId mapId;
    private final MapItemSavedData mapItemSavedData;

    private Button signButton;
    private ExtendedSlider slider;

    private byte currentColour = MapColor.COLOR_PURPLE.getPackedId(MapColor.Brightness.NORMAL);

    public MapScreen(ItemStack mapStack, Level level) {
        super(AllLang.MAP_SCREEN);

        this.canvasPixelWidth = 128;
        this.canvasPixelScale = 1;

        this.mapId = mapStack.get(DataComponents.MAP_ID);
        this.mapItemSavedData = DrawableMap.getSavedData(mapId, level);
    }

    @Override
    protected void init() {
        this.signButton = this.addRenderableWidget(Button.builder(Component.translatable("book.signButton"), button -> {
            updateButtonVisibility();
            saveData(true);
            this.getMinecraft().setScreen(null);
        }).bounds(this.width / 2 - 50, 196, 98, 20).build());
        this.slider = this.addRenderableWidget(
                new ExtendedSlider(this.width/2-50, 220, 98, 20, Component.literal("Brush size: "),
                        Component.empty(), 2, 10, 2D, 2, 0, true)
        );
        canvasX = (this.width - canvasPixelWidth * canvasPixelScale) / 2;
        canvasY = this.height / 6;
        updateButtonVisibility();
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

    private void updateButtonVisibility() {
        this.signButton.visible = !this.mapItemSavedData.locked;
        this.slider.visible = !this.mapItemSavedData.locked;
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

//        We have to flip the pose so that the decorations are rendered on top
        guiGraphics.pose().scale(1, 1, -1);
        MapDrawingClient.getInstance().getMapRenderer().render(guiGraphics.pose(), guiGraphics.bufferSource(), mapId, mapItemSavedData, false, 255);
        guiGraphics.pose().popPose();

//        for (int x = 0; x < 128; x++) {
//            for (int y = 0; y < 128; y++) {
//                guiGraphics.fill(x+canvasX, y+canvasY, x+canvasPixelScale+canvasX, y+canvasPixelScale+canvasY, 0xFFFFFFFF);
//                mapItemSavedData.setColor(x, y, MapColor.COLOR_BLACK.getPackedId(MapColor.Brightness.NORMAL));
//            }
//        }

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(0, 0, 2);
        guiGraphics.drawString(this.getMinecraft().font, String.format("%s, %s", mouseX, mouseY), mouseX, mouseY, 0xFFFFFFFF);
        guiGraphics.pose().popPose();
    }

    @Override
    public boolean mouseDragged(double posX, double posY, int button, double deltaX, double deltaY) {
        if (mapItemSavedData.locked)
            return super.mouseDragged(posX, posY, button, deltaX, deltaY);

        int mouseX = (int) Math.floor(posX);
        int mouseY = (int) Math.floor(posY);
        if (inCanvas(mouseX, mouseY)) {
            if (button == GLFW_MOUSE_BUTTON_LEFT) {
                for (int x = this.slider.getValueInt()/-2; x < this.slider.getValueInt()/2; x++) {
                    for (int y = this.slider.getValueInt()/-2; y < this.slider.getValueInt()/2; y++) {
                        if (inCanvas(mouseX+x, mouseY+y))
                            mapItemSavedData.setColor(mouseX-canvasX+x, mouseY-canvasY+y, currentColour);
                    }
                }
                MapDrawingClient.getInstance().getMapRenderer().update(mapId, mapItemSavedData);
            }
        }

        return super.mouseDragged(posX, posY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseClicked(double posX, double posY, int button) {
        if (mapItemSavedData.locked)
            return super.mouseClicked(posX, posY, button);

        int mouseX = (int) Math.floor(posX);
        int mouseY = (int) Math.floor(posY);
        if (inCanvas(mouseX, mouseY)) {
            if (button == GLFW_MOUSE_BUTTON_LEFT) {
                for (int x = this.slider.getValueInt()/-2; x < this.slider.getValueInt()/2; x++) {
                    for (int y = this.slider.getValueInt()/-2; y < this.slider.getValueInt()/2; y++) {
                        if (inCanvas(mouseX+x, mouseY+y))
                            mapItemSavedData.setColor(mouseX-canvasX+x, mouseY-canvasY+y, currentColour);
                    }
                }
                MapDrawingClient.getInstance().getMapRenderer().update(mapId, mapItemSavedData);
            }
        }
        return super.mouseClicked(posX, posY, button);
    }

    private boolean inCanvas(int x, int y) {
        return x < canvasX + 128 && x >= canvasX && y < canvasY + 128 && y >= canvasY;
    }
}
