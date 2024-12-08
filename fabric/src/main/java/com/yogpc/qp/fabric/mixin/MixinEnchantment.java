package com.yogpc.qp.fabric.mixin;

import com.yogpc.qp.machine.advquarry.AdvQuarryItem;
import com.yogpc.qp.machine.quarry.QuarryItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Enchantment.class)
public abstract class MixinEnchantment {
    @Inject(method = "canEnchant", at = @At("HEAD"), cancellable = true)
    public void canEnchant(ItemStack stack, CallbackInfoReturnable<Boolean> cir) {
        if (stack.getItem() instanceof QuarryItem || stack.getItem() instanceof AdvQuarryItem) {
            cir.setReturnValue(stack.getCount() == 1);
        }
    }
}
