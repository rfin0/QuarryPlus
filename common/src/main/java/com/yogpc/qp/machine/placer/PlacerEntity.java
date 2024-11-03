package com.yogpc.qp.machine.placer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

public final class PlacerEntity extends AbstractPlacerTile {
    public PlacerEntity(BlockPos pos, BlockState blockState) {
        super(pos, blockState);
    }

    @Override
    protected BlockPos getTargetPos() {
        return getBlockPos().relative(getMachineFacing());
    }

    @Override
    protected Direction getMachineFacing() {
        return getBlockState().getValue(BlockStateProperties.FACING);
    }
}
