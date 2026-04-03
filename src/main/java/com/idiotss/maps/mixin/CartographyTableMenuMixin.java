package com.idiotss.maps.mixin;

import com.idiotss.maps.AllItems;
import com.idiotss.maps.item.DrawableMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.*;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.MapPostProcessing;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.saveddata.maps.MapItemSavedData;
import org.jetbrains.annotations.NotNull;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(CartographyTableMenu.class)
public abstract class CartographyTableMenuMixin {
    @Shadow
    @Final
    public Container container;

    @Shadow
    @Final
    private ResultContainer resultContainer;

    @Inject(method = "<init>(ILnet/minecraft/world/entity/player/Inventory;Lnet/minecraft/world/inventory/ContainerLevelAccess;)V",
            at = @At("TAIL"))
    private void CartographyTableMenu(int containerId, Inventory playerInventory, ContainerLevelAccess access, CallbackInfo ci) {
        ((AbstractContainerMenu) (Object) this).slots.set(0,
        new Slot(container, 0, 15, 15) {
            @Override
            public boolean mayPlace(@NotNull ItemStack stack) {
                return stack.is(AllItems.DRAWABLE_MAP) || stack.is(Items.FILLED_MAP);
            }
        });
    }

    @Inject(method = "quickMoveStack",
            at = @At(value = "INVOKE", target = "Lnet/minecraft/world/item/ItemStack;is(Lnet/minecraft/world/item/Item;)Z"),
            cancellable = true)
    private void quickMoveStack(Player player, int index, CallbackInfoReturnable<ItemStack> cir) {
        Slot slot = ((AbstractContainerMenu) (Object) this).slots.get(index);
        ItemStack itemstack1 = slot.getItem();
        if (itemstack1.is(AllItems.DRAWABLE_MAP)) {
            if (!((AbstractContainerMenu) (Object) this).moveItemStackTo(itemstack1, 0, 1, false)) {
                cir.setReturnValue(ItemStack.EMPTY);
            }
        }
        if (itemstack1.is(Items.GLASS_PANE)) {
            cir.setReturnValue(ItemStack.EMPTY);
        }
    }

    @Inject(method = "lambda$setupResultSlot$0",
            at = @At("HEAD"),
            cancellable = true)
    private void setupResultSlot(ItemStack map, ItemStack firstSlotStack, ItemStack resultOutput, Level level, BlockPos blockPos, CallbackInfo ci)
    {
        if (map.is(AllItems.DRAWABLE_MAP)) {
            MapItemSavedData mapitemsaveddata = DrawableMap.getSavedData(map, level);
            if (mapitemsaveddata != null) {
                ItemStack itemstack;
                if (firstSlotStack.is(Items.PAPER) && mapitemsaveddata.scale < 4) {
                    itemstack = map.copyWithCount(1);
                    itemstack.set(DataComponents.MAP_POST_PROCESSING, MapPostProcessing.SCALE);
                    ((AbstractContainerMenu) (Object) this).broadcastChanges();
                } else if (firstSlotStack.is(Items.GLASS_PANE)) {
                    itemstack = ItemStack.EMPTY;
                    ((AbstractContainerMenu) (Object) this).broadcastChanges();
                } else {
                    if (!firstSlotStack.is(Items.MAP)) {
                        resultContainer.removeItemNoUpdate(2);
                        ((AbstractContainerMenu) (Object) this).broadcastChanges();
                        ci.cancel();
                    }

                    itemstack = map.copyWithCount(2);
                    ((AbstractContainerMenu) (Object) this).broadcastChanges();
                }


                if (!ItemStack.matches(itemstack, resultOutput)) {
                    this.resultContainer.setItem(2, itemstack);
                    ((AbstractContainerMenu) (Object) this).broadcastChanges();
                }
            }
        }

    }
}
