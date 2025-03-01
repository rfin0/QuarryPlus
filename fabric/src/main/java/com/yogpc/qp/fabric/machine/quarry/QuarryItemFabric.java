package com.yogpc.qp.fabric.machine.quarry;

import com.yogpc.qp.QuarryDataComponents;
import com.yogpc.qp.machine.QpBlock;
import com.yogpc.qp.machine.quarry.QuarryItem;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;

import java.util.List;

public final class QuarryItemFabric extends QuarryItem {
    public QuarryItemFabric(QpBlock block) {
        super(block);
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        if (stack.getOrDefault(QuarryDataComponents.QUARRY_REMOVE_BEDROCK_COMPONENT, Boolean.FALSE)) {
            tooltipComponents.add(Component.literal("Remove Bedrock: ON"));
        }
    }
}
