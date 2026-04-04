package com.idiotss.maps.mixin;

import com.idiotss.maps.AllItems;
import com.idiotss.maps.MapDrawingClient;
import com.idiotss.maps.item.DrawableMap;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.client.gui.screens.inventory.CartographyTableScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.saveddata.maps.MapId;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import javax.annotation.Nullable;

@Mixin(CartographyTableScreen.class)
public class CartographyTableScreenMixin {
    @Shadow
    @Final
    private static ResourceLocation BG_LOCATION;

    @Shadow
    @Final
    private static ResourceLocation DUPLICATED_MAP_SPRITE;

    @Shadow
    @Final
    private static ResourceLocation MAP_SPRITE;

    @Shadow
    @Final
    private static ResourceLocation SCALED_MAP_SPRITE;

    @Shadow
    @Final
    private static ResourceLocation ERROR_SPRITE;

    @Inject(method = "renderBg", at = @At("HEAD"), cancellable = true)
    private void renderBg(GuiGraphics guiGraphics, float partialTick, int mouseX, int mouseY, CallbackInfo ci) {
        int i = ((AbstractContainerScreen) (Object) this).getGuiLeft();
        int j = ((AbstractContainerScreen) (Object) this).getGuiTop();
        guiGraphics.blit(BG_LOCATION, i, j, 0, 0, ((AbstractContainerScreen) (Object) this).getXSize(), ((AbstractContainerScreen) (Object) this).getYSize());
        ItemStack itemstack = ((AbstractContainerScreen) (Object) this).getMenu().getSlot(1).getItem();
        boolean flag = itemstack.is(Items.MAP);
        boolean flag1 = itemstack.is(Items.PAPER);
        boolean flag2 = itemstack.is(Items.GLASS_PANE);
        boolean flag3 = false;
        ItemStack itemstack1 = ((AbstractContainerScreen) (Object) this).getMenu().getSlot(0).getItem();
        if (!itemstack1.is(AllItems.DRAWABLE_MAP)) {
            return;
        }
        MapId mapid = itemstack1.get(DataComponents.MAP_ID);
        MapItemSavedData savedData;
        if (mapid != null) {
            savedData = DrawableMap.getSavedData(mapid, ((Screen) (Object) this).getMinecraft().level);
            if (savedData != null) {
                if (savedData.locked) {
                    flag3 = true;
                    if (flag1 || flag2) {
                        guiGraphics.blitSprite(ERROR_SPRITE, i + 35, j + 31, 28, 21);
                    }
                }

                if (flag1 && savedData.scale >= 4) {
                    flag3 = true;
                    guiGraphics.blitSprite(ERROR_SPRITE, i + 35, j + 31, 28, 21);
                }
            }
        } else {
            savedData = null;
        }

        map_drawing$renderResultingMap(guiGraphics, mapid, savedData, flag, flag1, flag2, flag3);
        ci.cancel();
    }

    @Unique
    private void map_drawing$renderResultingMap(
            GuiGraphics guiGraphics,
            @Nullable MapId mapId,
            @Nullable MapItemSavedData savedData,
            boolean hasMap,
            boolean hasPaper,
            boolean hasGlassPane,
            boolean isMaxSize
    ) {
        int i = ((AbstractContainerScreen) (Object) this).getGuiLeft();
        int j = ((AbstractContainerScreen) (Object) this).getGuiTop();
        if (hasPaper && !isMaxSize) {
            guiGraphics.blitSprite(SCALED_MAP_SPRITE, i + 67, j + 13, 66, 66);
            map_drawing$renderMap(guiGraphics, mapId, savedData, i + 85, j + 31, 0.226F);
        } else if (hasMap) {
            guiGraphics.blitSprite(DUPLICATED_MAP_SPRITE, i + 67 + 16, j + 13, 50, 66);
            map_drawing$renderMap(guiGraphics, mapId, savedData, i + 86, j + 16, 0.34F);
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate(0.0F, 0.0F, 1.0F);
            guiGraphics.blitSprite(DUPLICATED_MAP_SPRITE, i + 67, j + 13 + 16, 50, 66);
            map_drawing$renderMap(guiGraphics, mapId, savedData, i + 70, j + 32, 0.34F);
            guiGraphics.pose().popPose();
        } else {
            guiGraphics.blitSprite(MAP_SPRITE, i + 67, j + 13, 66, 66);
            map_drawing$renderMap(guiGraphics, mapId, savedData, i + 71, j + 17, 0.45F);
        }
    }

    @Unique
    public void map_drawing$renderMap(GuiGraphics guiGraphics, @Nullable MapId mapId, @Nullable MapItemSavedData savedData, int x, int y, float scale)
    {
        if (mapId != null && savedData != null) {
            guiGraphics.pose().pushPose();
            guiGraphics.pose().translate((float)x, (float)y, 1.0F);
            guiGraphics.pose().scale(scale, scale, 1.0F);
            MapDrawingClient.getInstance().getMapRenderer().render(guiGraphics.pose(), guiGraphics.bufferSource(), mapId, savedData, true, 15728880);
            guiGraphics.flush();
            guiGraphics.pose().popPose();
        }
    }

}
