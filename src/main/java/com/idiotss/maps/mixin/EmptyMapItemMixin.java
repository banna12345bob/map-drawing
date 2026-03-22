package com.idiotss.maps.mixin;

import com.idiotss.maps.AllItems;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.stats.Stats;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.EmptyMapItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EmptyMapItem.class)
public class EmptyMapItemMixin {

    @Inject(method = "use", at = @At("HEAD"), cancellable = true)
    public void use(Level level, Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        ItemStack itemstack = player.getItemInHand(hand);
        if (level.isClientSide) {
            cir.setReturnValue(InteractionResultHolder.success(itemstack));
        } else {
            itemstack.consume(1, player);
            player.awardStat(Stats.ITEM_USED.get((EmptyMapItem) (Object) this));
            player.level().playSound((Player)null, player, SoundEvents.UI_CARTOGRAPHY_TABLE_TAKE_RESULT, player.getSoundSource(), 1.0F, 1.0F);
            ItemStack itemstack1 = new ItemStack(AllItems.DRAWABLE_MAP.asItem());
            if (itemstack.isEmpty()) {
                cir.setReturnValue(InteractionResultHolder.success(itemstack1));
            } else {
                if (!player.getInventory().add(itemstack1.copy())) {
                    player.drop(itemstack1, false);
                }
                cir.setReturnValue(InteractionResultHolder.consume(itemstack));
            }
        }
    }
}
