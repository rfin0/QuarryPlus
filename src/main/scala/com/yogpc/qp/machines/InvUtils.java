package com.yogpc.qp.machines;

import java.util.Optional;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.items.CapabilityItemHandler;
import net.minecraftforge.items.ItemHandlerHelper;

public class InvUtils {
    public static ItemStack injectToNearTile(Level level, BlockPos pos, ItemStack stack) {
        var remain = stack.copy();
        for (Direction d : Direction.values()) {
            if (!remain.isEmpty()) {
                Optional.ofNullable(level.getBlockEntity(pos.relative(d)))
                    .flatMap(t -> t.getCapability(CapabilityItemHandler.ITEM_HANDLER_CAPABILITY, d.getOpposite()).resolve())
                    .ifPresent(handler -> {
                        var simulate = ItemHandlerHelper.insertItem(handler, remain.copy(), true);
                        if (simulate.getCount() < remain.getCount()) {
                            var notMoved = ItemHandlerHelper.insertItem(handler,
                                ItemHandlerHelper.copyStackWithSize(remain, remain.getCount() - simulate.getCount()), false);
                            // notMoved should be empty.
                            int remainCount = simulate.getCount() + notMoved.getCount();
                            remain.setCount(remainCount);
                        }
                    });
            }
        }
        return remain;
    }
}
