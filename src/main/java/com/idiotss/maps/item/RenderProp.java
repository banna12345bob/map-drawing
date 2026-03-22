package com.idiotss.maps.item;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BlockEntityWithoutLevelRenderer;
import net.neoforged.neoforge.client.extensions.common.IClientItemExtensions;
import org.jetbrains.annotations.NotNull;

public class RenderProp implements IClientItemExtensions {
    public static final RenderProp INSTANCE = new RenderProp();

    @Override
    public @NotNull BlockEntityWithoutLevelRenderer getCustomRenderer() {
        return new DrawableMapRenderer(Minecraft.getInstance().getBlockEntityRenderDispatcher(), Minecraft.getInstance().getEntityModels());
    }
}
