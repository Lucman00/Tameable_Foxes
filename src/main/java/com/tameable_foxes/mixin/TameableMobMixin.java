package com.tameable_foxes.mixin;

import com.tameable_foxes.entity.TameableFox;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.animal.fox.Fox;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Mob.class)
public abstract class TameableMobMixin {
    @Inject(method = "mobInteract", at = @At("HEAD"), cancellable = true)
    private void onMobInteract(Player player, InteractionHand hand, CallbackInfoReturnable<InteractionResult> cir) {
        Mob mob = (Mob)(Object)this;
        if (mob instanceof Fox fox) {
            TameableFox tameableFox = (TameableFox)fox;

            ItemStack heldItem = player.getItemInHand(hand);

            if (heldItem.is(Items.BAMBOO) && !tameableFox.isTame()) {
                if (!fox.level().isClientSide()) {
                    tameableFox.setOwner(player);
                    tameableFox.setTame(true);
                }
                if (!player.getAbilities().instabuild) {
                    heldItem.shrink(1);
                }
                cir.setReturnValue(InteractionResult.SUCCESS);
            }
        }
    }
}