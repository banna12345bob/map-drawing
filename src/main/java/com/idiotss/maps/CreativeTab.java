package com.idiotss.maps;

import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredHolder;

import static com.idiotss.maps.MapDrawing.REGISTRATE;

public class CreativeTab {
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> EXAMPLE_TAB = REGISTRATE.object("example_tab")
            .defaultCreativeTab(tab -> tab
                    .title(Component.translatable("itemGroup.mapdrawer"))
                    .icon(AllItems.EXAMPLE_ITEM::asStack))
            .register();

    public static void load() {}
}
