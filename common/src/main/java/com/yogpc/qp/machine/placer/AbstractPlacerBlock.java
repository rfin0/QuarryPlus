package com.yogpc.qp.machine.placer;

import com.yogpc.qp.machine.QpEntityBlock;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;

public abstract class AbstractPlacerBlock extends QpEntityBlock {
    public AbstractPlacerBlock(String name) {
        super(
            Properties.of()
                .mapColor(MapColor.METAL)
                .pushReaction(PushReaction.BLOCK).strength(1.2f),
            name,
            b -> new BlockItem(b, new Item.Properties())
        );
    }
}
