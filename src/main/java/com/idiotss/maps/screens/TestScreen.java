package com.idiotss.maps.screens;

import com.idiotss.maps.AllDataComponents;
import com.idiotss.maps.MapDrawing;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import java.awt.*;
import java.util.List;

public class TestScreen extends Screen {
    private int canvasX;
    private int canvasY;
    private final int canvasPixelWidth;
    private final int canvasPixelHeight;
    private final int canvasPixelScale;

    private int[] pixels;

    public TestScreen(ItemStack mapStack, Component title) {
        super(title);

        this.canvasPixelWidth = 32;
        this.canvasPixelHeight = 32;
        this.canvasPixelScale = 5;

        List<Integer> stackPixels = mapStack.get(AllDataComponents.MAP_PIXELS);
        if (stackPixels != null && !stackPixels.isEmpty()) {
            this.pixels = stackPixels.stream().mapToInt(i -> i).toArray();
        }
    }

    @Override
    protected void init() {
        canvasX = (this.width - canvasPixelWidth * canvasPixelScale) / 2;
        canvasY = 40;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private int getPixelAt(int x, int y) {
        return (this.pixels == null) ? 0xFFF9FFFE : this.pixels[y * canvasPixelWidth + x];
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTick) {
        super.render(guiGraphics, mouseX, mouseY, partialTick);
        for (int i = 0; i < canvasPixelHeight; i++) {
            for (int j = 0; j < canvasPixelWidth; j++) {
                int x = canvasX + j * canvasPixelScale;
                int y = canvasY + i * canvasPixelScale;
                // Colours are in the form 0xAARRGGBB
                // Where AA is to hex value for the alpha channel from 0 to FF (0-255), ect
                guiGraphics.fill(x, y, x + canvasPixelScale, y + canvasPixelScale, getPixelAt(j, i));
            }
        }
    }
}
