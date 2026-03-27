package com.idiotss.maps.item;

import com.idiotss.maps.AllDataComponents;
import com.idiotss.maps.screens.TestScreen;
import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ComplexItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public class DrawableMap extends ComplexItem {
    DynamicTexture TEXTURE;
    ResourceLocation TEX_LOCATION;
    private int[] pixels;


    public DrawableMap(Properties properties) {
        super(properties);
    }

    private int getPixelAt(int x, int y) {
        return (this.pixels == null || pixels.length == 0) ? 0xFFF9FFFE : this.pixels[y * 32 + x];
    }

    private int swapColor(int color) {
        int i = (color & 16711680) >> 16;
        int j = (color & '\uff00') >> 8;
        int k = (color & 255);
        return k << 16 | j << 8 | i | 0xff000000;
    }

    // TODO: Figure out a smart way to do this so that a registry holds all the textures
    public void updateTexture(ItemStack stack) {
        TEXTURE = new DynamicTexture(32, 32, true);
        if (getSavedData(stack) != null)
            pixels = getSavedData(stack).stream().mapToInt(i -> i).toArray();
        TEX_LOCATION = Minecraft.getInstance().getTextureManager().register("canvas/test", TEXTURE);
        NativeImage image = TEXTURE.getPixels();
        if (image != null) {
            for (int y = 0; y < 32; y++) {
                for (int x = 0; x < 32; x++) {
                    // For some reason in the format 0xAABBGGRR so we have to swap it
                    image.setPixelRGBA(x, y, swapColor(getPixelAt(x, y)));
                }
            }

            TEXTURE.upload();
        }
    }

    public ResourceLocation getTextureLoc() {
        return TEX_LOCATION;
    }

    @Nullable
    public static List<Integer> getSavedData(ItemStack stack) {
        if(stack.getItem() instanceof DrawableMap) {
            return stack.get(AllDataComponents.MAP_PIXELS);
        }
        return null;
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand usedHand) {
//        Some garbage testing data
        List<Integer> pixels = new ArrayList<>();
        for (int i = 0; i < 1024; i++) {
            if (i/32%3 == 0)
                pixels.add(0xFFFF0000);
            if (i/32%3 == 1)
               pixels.add(0xFF00FF00);
            if (i/32%3 == 2)
                pixels.add(0xFF0000FF);
        }
        player.getItemInHand(usedHand).set(AllDataComponents.MAP_PIXELS, pixels);

        if (level.isClientSide()) {
            this.updateTexture(player.getItemInHand(usedHand));
            Minecraft.getInstance().setScreen(new TestScreen(player.getItemInHand(usedHand), Component.translatable("block.mapdrawer.example_block")));
        }
        return super.use(level, player, usedHand);
    }
}
