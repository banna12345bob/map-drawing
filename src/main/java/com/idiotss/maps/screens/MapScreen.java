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
import org.joml.Vector2f;
import org.joml.Vector2i;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.glfw.GLFW.*;

@OnlyIn(Dist.CLIENT)
public class MapScreen extends Screen {
    enum Colour {
        ORANGE(MapColor.COLOR_ORANGE),
        MAGENTA(MapColor.COLOR_MAGENTA),
        LIGHT_BLUE(MapColor.COLOR_LIGHT_BLUE),
        YELLOW(MapColor.COLOR_YELLOW),
        LIGHT_GREEN(MapColor.COLOR_LIGHT_GREEN),
        PINK(MapColor.COLOR_PINK),
        GRAY(MapColor.COLOR_GRAY),
        LIGHT_GRAY(MapColor.COLOR_LIGHT_GRAY),
        CYAN(MapColor.COLOR_CYAN),
        PURPLE(MapColor.COLOR_PURPLE),
        BLUE(MapColor.COLOR_BLUE),
        BROWN(MapColor.COLOR_BROWN),
        GREEN(MapColor.COLOR_GREEN),
        RED(MapColor.COLOR_RED),
        BLACK(MapColor.COLOR_BLACK),
        WHITE(MapColor.SNOW);

        final MapColor mapColour;

        Colour(MapColor mapColour) {
            this.mapColour = mapColour;
        }
    }

    private int canvasX;
    private int canvasY;
    private final int canvasPixelWidth;

    private final MapId mapId;
    private final MapItemSavedData mapItemSavedData;

    private Button signButton;
    private Button closeButton;
    private ExtendedSlider brushSizeSlider;
    private final Map<Colour, Button> colourSelectionButtons = new HashMap<>();
    private final Map<MapColor.Brightness, Button> brightnessSelectionButtons = new HashMap<>();

    private MapColor currentColour = MapColor.COLOR_BLACK;
    private MapColor.Brightness currentBrightness = MapColor.Brightness.HIGH;

    public MapScreen(ItemStack mapStack, Level level) {
        super(AllLang.MAP_SCREEN);

        this.canvasPixelWidth = 128;

        this.mapId = mapStack.get(DataComponents.MAP_ID);
        this.mapItemSavedData = DrawableMap.getSavedData(mapId, level);
    }

    @Override
    protected void init() {
        this.closeButton = this.addRenderableWidget(Button.builder(Component.translatable("mco.selectServer.close"), button -> {
            updateButtonVisibility();
            saveData(false);
            this.getMinecraft().setScreen(null);
        }).bounds(this.width/4-(canvasPixelWidth/2), 10, canvasPixelWidth, 20).build());
        if (!mapItemSavedData.locked) {
            canvasX = (this.width - canvasPixelWidth + (this.width / canvasPixelWidth * 15)) / 2 + (this.width / canvasPixelWidth * 18);
            canvasY = 40;
        } else {
            canvasX = (int) (((double) this.width / 2) - canvasPixelWidth*0.75);;
            canvasY = 10;
            this.closeButton.setPosition(this.width/2-(canvasPixelWidth/2), this.height-30);
        }

        this.signButton = this.addRenderableWidget(Button.builder(Component.translatable("book.signButton"), button -> {
            updateButtonVisibility();
            saveData(true);
            this.getMinecraft().setScreen(null);
        }).bounds(canvasX+140, 10, 53, 20).build());
        this.brushSizeSlider = this.addRenderableWidget(
                new ExtendedSlider(canvasX, 10, 135, 20, AllLang.BRUSH_SIZE,
                        Component.empty(), 1, 10, 4, 1, 0, true)
        );
        int k = 0;
        for (Colour colour : Colour.values()) {
            this.colourSelectionButtons.put(colour, this.addRenderableWidget(Button.builder(Component.literal(colour.name()), builder -> {
                this.currentColour = colour.mapColour;
                updateButtonVisibility();
            }).bounds(0, k*15 + 10, 75, 15).build()));
            k++;
        }

        k = 0;
        for (MapColor.Brightness brightness : MapColor.Brightness.values()) {
            this.brightnessSelectionButtons.put(brightness, this.addRenderableWidget(Button.builder(Component.literal(brightness.name()), builder -> {
                this.currentBrightness = brightness;
                updateButtonVisibility();
            }).bounds(80, k*15 + 50, 75, 15).build()));
            k++;
        }
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
        this.brushSizeSlider.visible = !this.mapItemSavedData.locked;
        this.colourSelectionButtons.forEach((colour, button) -> {
            button.visible = !this.mapItemSavedData.locked;
            button.active = colour.mapColour != currentColour;
        });
        this.brightnessSelectionButtons.forEach((brightness, button) -> {
            button.visible = !this.mapItemSavedData.locked;
            button.active = brightness != currentBrightness;
        });
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    public void render(@NotNull GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);

//        guiGraphics.pose().pushPose();
//        guiGraphics.pose().translate(0, 0, 2);
//        if (inCanvas(convertMousePosToCanvasPos(mouseX, mouseY))) {
//            GLFW.glfwSetInputMode(getMinecraft().getWindow().getWindow(), GLFW_CURSOR, GLFW_CURSOR_HIDDEN);
//            guiGraphics.fill(mouseX-(brushSizeSlider.getValueInt()/2), mouseY-(brushSizeSlider.getValueInt()/2), mouseX+(brushSizeSlider.getValueInt()/2), mouseY+(brushSizeSlider.getValueInt()/2), 0xFFEEEEEE);
//        }
//        else {
//            GLFW.glfwSetInputMode(getMinecraft().getWindow().getWindow(), GLFW_CURSOR, GLFW_CURSOR_NORMAL);
//        }
//        guiGraphics.pose().popPose();

        guiGraphics.pose().pushPose();
        guiGraphics.pose().translate(canvasX, canvasY, 0);

//        We have to flip the pose so that the decorations are rendered on top
        guiGraphics.pose().scale(1.5f, 1.5f, -1);
        MapDrawingClient.getInstance().getMapRenderer().render(guiGraphics.pose(), guiGraphics.bufferSource(), mapId, mapItemSavedData, false, 255);
        guiGraphics.pose().popPose();

//        for (int x = 0; x < 128; x++) {
//            for (int y = 0; y < 128; y++) {
//                guiGraphics.fill(x+canvasX, y+canvasY, x+canvasPixelScale+canvasX, y+canvasPixelScale+canvasY, 0xFFFFFFFF);
//                mapItemSavedData.setColor(x, y, MapColor.COLOR_BLACK.getPackedId(MapColor.Brightness.NORMAL));
//            }
//        }

//        if (!FMLLoader.isProduction()) {
//            guiGraphics.pose().pushPose();
//            guiGraphics.pose().translate(100, 20, 2);
//            guiGraphics.drawString(this.getMinecraft().font, String.format("%s", convertMousePosToCanvasPos(mouseX, mouseY)), 10, 10,
//                    inCanvas(convertMousePosToCanvasPos(mouseX, mouseY)) ? 0xFF00FF00 : 0xFFFF0000);
//            guiGraphics.pose().popPose();
//        }
    }

    @Override
    public boolean mouseDragged(double posX, double posY, int button, double deltaX, double deltaY) {
        if (mapItemSavedData.locked)
            return super.mouseDragged(posX, posY, button, deltaX, deltaY);

        int mouseX = (int) Math.floor(posX);
        int mouseY = (int) Math.floor(posY);
        if (inCanvas(convertMousePosToCanvasPos(mouseX, mouseY))) {
            hasClickedCanvas(mouseX, mouseY, button);
        }

        return super.mouseDragged(posX, posY, button, deltaX, deltaY);
    }

    @Override
    public boolean mouseClicked(double posX, double posY, int button) {
        if (mapItemSavedData.locked)
            return super.mouseClicked(posX, posY, button);

        int mouseX = (int) Math.floor(posX);
        int mouseY = (int) Math.floor(posY);
        if (inCanvas(convertMousePosToCanvasPos(mouseX, mouseY))) {
            hasClickedCanvas(mouseX, mouseY, button);
        }

        return super.mouseClicked(posX, posY, button);
    }

    private void hasClickedCanvas(int mouseX, int mouseY, int button) {
        if (button == GLFW_MOUSE_BUTTON_LEFT) {
            if (this.brushSizeSlider.getValueInt()%2 == 0) {
                for (int x = -this.brushSizeSlider.getValueInt() / 2; x < this.brushSizeSlider.getValueInt() / 2; x++) {
                    for (int y = -this.brushSizeSlider.getValueInt() / 2; y < this.brushSizeSlider.getValueInt() / 2; y++) {
                        Vector2i pos = convertMousePosToCanvasPos(mouseX, mouseY).add(x, y);
                        if (inCanvas(pos))
                            mapItemSavedData.setColor(pos.x, pos.y, currentColour.getPackedId(currentBrightness));
                    }
                }
            } else {
                for (int x = -this.brushSizeSlider.getValueInt() / 2; x <= this.brushSizeSlider.getValueInt()/2; x++) {
                    for (int y = -this.brushSizeSlider.getValueInt() / 2; y <= this.brushSizeSlider.getValueInt()/2; y++) {
                        Vector2i pos = convertMousePosToCanvasPos(mouseX, mouseY).add(x, y);
                        if (inCanvas(pos))
                            mapItemSavedData.setColor(pos.x, pos.y, currentColour.getPackedId(currentBrightness));
                    }
                }
            }
            MapDrawingClient.getInstance().getMapRenderer().update(mapId, mapItemSavedData);
        } else if (button == GLFW_MOUSE_BUTTON_RIGHT) {
            if (this.brushSizeSlider.getValueInt()%2 == 0) {
                for (int x = -this.brushSizeSlider.getValueInt() / 2; x < this.brushSizeSlider.getValueInt() / 2; x++) {
                    for (int y = -this.brushSizeSlider.getValueInt() / 2; y < this.brushSizeSlider.getValueInt() / 2; y++) {
                        Vector2i pos = convertMousePosToCanvasPos(mouseX, mouseY).add(x, y);
                        if (inCanvas(pos))
                            mapItemSavedData.setColor(pos.x, pos.y, MapColor.SNOW.getPackedId(MapColor.Brightness.HIGH));
                    }
                }
            } else {
                for (int x = -this.brushSizeSlider.getValueInt() / 2; x <= this.brushSizeSlider.getValueInt()/2; x++) {
                    for (int y = -this.brushSizeSlider.getValueInt() / 2; y <= this.brushSizeSlider.getValueInt()/2; y++) {
                        Vector2i pos = convertMousePosToCanvasPos(mouseX, mouseY).add(x, y);
                        if (inCanvas(pos))
                            mapItemSavedData.setColor(pos.x, pos.y, MapColor.SNOW.getPackedId(MapColor.Brightness.HIGH));
                    }
                }
            }
            MapDrawingClient.getInstance().getMapRenderer().update(mapId, mapItemSavedData);
        }
    }

    private Vector2i convertMousePosToCanvasPos(int x, int y) {
        return convertMousePosToCanvasPos(new Vector2f(x, y));
    }

    private Vector2i convertMousePosToCanvasPos(Vector2f vec) {
        return new Vector2i(Math.round((vec.x - canvasX) * (127f / 191f)), Math.round((vec.y - canvasY) * (127f / 191f)));
    }

    private boolean inCanvas(Vector2i vec) {
        return inCanvas(vec.x, vec.y);
    }

    private boolean inCanvas(int x, int y) {
        return x < canvasPixelWidth && x >= 0 && y < canvasPixelWidth && y >= 0;
    }
}
