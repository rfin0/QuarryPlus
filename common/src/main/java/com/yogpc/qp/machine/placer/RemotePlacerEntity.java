package com.yogpc.qp.machine.placer;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtOps;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

public final class RemotePlacerEntity extends AbstractPlacerTile {
    public static final String KEY_TARGET = "targetPos";
    @NotNull
    BlockPos targetPos;

    public RemotePlacerEntity(BlockPos pos, BlockState blockState) {
        super(pos, blockState);
        targetPos = pos.above();
    }

    @Override
    protected BlockPos getTargetPos() {
        return targetPos;
    }

    @Override
    protected Direction getMachineFacing() {
        return Direction.UP;
    }

    @Override
    public void fromClientTag(CompoundTag tag, HolderLookup.Provider registries) {
        super.fromClientTag(tag, registries);
        targetPos = BlockPos.CODEC.parse(NbtOps.INSTANCE, tag.get(KEY_TARGET)).getOrThrow();
    }

    @Override
    public CompoundTag toClientTag(CompoundTag tag, HolderLookup.Provider registries) {
        tag.put(KEY_TARGET, BlockPos.CODEC.encodeStart(NbtOps.INSTANCE, targetPos).getOrThrow());
        return super.toClientTag(tag, registries);
    }
}
