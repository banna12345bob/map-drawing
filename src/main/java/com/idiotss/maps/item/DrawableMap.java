package com.idiotss.maps.item;

import com.idiotss.maps.AllDataComponents;
import com.idiotss.maps.screens.TestScreen;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
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
    public DrawableMap(Properties properties) {
        super(properties);
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
//        List<Integer> pixels = new ArrayList<>();
//        for (int i = 0; i < 1024; i++) {
//            if (i/32%3 == 0)
//                pixels.add(0xFFFF0000);
//            if (i/32%3 == 1)
//               pixels.add(0xFF00FF00);
//            if (i/32%3 == 2)
//                pixels.add(0xFF0000FF);
//        }
//        player.getItemInHand(usedHand).set(AllDataComponents.MAP_PIXELS, pixels);
        
        if (level.isClientSide())
            Minecraft.getInstance().setScreen(new TestScreen(player.getItemInHand(usedHand), Component.translatable("block.mapdrawer.example_block")));
        return super.use(level, player, usedHand);
    }
}
