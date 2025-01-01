package com.yogpc.qp.machine;

import com.yogpc.qp.PlatformAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.function.Function;

public abstract class QpEntityBlock extends QpBlock implements EntityBlock {
    public QpEntityBlock(Properties properties, String name, Function<? super QpBlock, ? extends QpBlockItem> itemGenerator) {
        super(properties, name, itemGenerator);
    }

    @SuppressWarnings("unchecked")
    protected <T extends BlockEntity> Optional<BlockEntityType<T>> getBlockEntityType() {
        return PlatformAccess.getAccess().registerObjects().getBlockEntityType(this)
            .map(e -> (BlockEntityType<T>) e);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return getBlockEntityType()
            .map(t -> t.create(pos, state))
            .orElse(null);
    }

    @Override
    protected ItemStack getCloneItemStack(LevelReader level, BlockPos pos, BlockState state, boolean includeData) {
        var stack = super.getCloneItemStack(level, pos, state, includeData);
        if (shouldCloneItemHasData()) {
            MachineLootFunction.process(stack, level.getBlockEntity(pos));
        }
        return stack;
    }

    protected boolean shouldCloneItemHasData() {
        return false;
    }

    /**
     * @see net.minecraft.world.level.block.BaseEntityBlock
     */
    @SuppressWarnings("unchecked")
    @Nullable
    protected static <E extends BlockEntity, A extends BlockEntity> BlockEntityTicker<A> createTickerHelper(BlockEntityType<A> serverType, BlockEntityType<E> clientType, BlockEntityTicker<? super E> ticker) {
        return clientType == serverType ? (BlockEntityTicker<A>) ticker : null;
    }
}
