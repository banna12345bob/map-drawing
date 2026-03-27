package com.idiotss.maps.mixin;

import com.idiotss.maps.item.DrawableMap;
import net.minecraft.client.renderer.entity.ItemFrameRenderer;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ItemFrame;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.MapItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemFrameRenderer.class)
public class ItemFrameRendererMixin {
    @Shadow
    @Final
    private static ModelResourceLocation GLOW_MAP_FRAME_LOCATION;

    @Shadow
    @Final
    private static ModelResourceLocation MAP_FRAME_LOCATION;

    @Shadow
    @Final
    private static ModelResourceLocation FRAME_LOCATION;

    @Shadow
    @Final
    private static ModelResourceLocation GLOW_FRAME_LOCATION;

    @Inject(method = "getFrameModelResourceLoc",
            at = @At("HEAD"),
            cancellable = true
    )
    private void getFrameModelResourceLoc(ItemFrame entity, ItemStack item, CallbackInfoReturnable<ModelResourceLocation> cir)
    {
        boolean flag = entity.getType() == EntityType.GLOW_ITEM_FRAME;
        if (item.getItem() instanceof MapItem || item.getItem() instanceof DrawableMap) {
            cir.setReturnValue(flag ? GLOW_MAP_FRAME_LOCATION : MAP_FRAME_LOCATION);
        } else {
            cir.setReturnValue(flag ? GLOW_FRAME_LOCATION : FRAME_LOCATION);
        }
    }
}
