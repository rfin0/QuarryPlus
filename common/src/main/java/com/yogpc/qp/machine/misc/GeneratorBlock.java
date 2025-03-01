package com.yogpc.qp.machine.misc;

import com.yogpc.qp.machine.*;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.stream.IntStream;

public class GeneratorBlock extends QpEntityBlock {
    public static final String NAME = "creative_generator";
    static final int LEVEL_MAX = BlockStateProperties.LEVEL.getPossibleValues().stream().max(Integer::compareTo).orElse(0);
    static final long[] ENERGY = IntStream.rangeClosed(0, LEVEL_MAX)
        .mapToLong(i -> (long) (Math.pow(2.213465466013801, i) * PowerEntity.ONE_FE))
        .toArray();

    public GeneratorBlock() {
        super(Properties.of()
            .mapColor(MapColor.METAL)
            .pushReaction(PushReaction.BLOCK)
            .strength(1f, 1f)
            .sound(SoundType.STONE), NAME, block -> new QpBlockItem(block, new Item.Properties().fireResistant()));
        registerDefaultState(getStateDefinition().any()
            .setValue(BlockStateProperties.LEVEL, LEVEL_MAX));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder);
        builder.add(BlockStateProperties.LEVEL);
    }

    @Override
    protected QpBlock createBlock(Properties properties) {
        return new GeneratorBlock();
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        tooltipComponents.add(Component.literal("Works only for Quarry"));
        tooltipComponents.add(Component.literal("Only for debug purposes").withStyle(ChatFormatting.RED));
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> blockEntityType) {
        if (level.isClientSide()) {
            return null;
        }
        return createTickerHelper(
            blockEntityType,
            this.<GeneratorEntity>getBlockEntityType().orElse(null),
            CombinedBlockEntityTicker.of(this, level, GeneratorEntity::serverTick)
        );
    }

    @Override
    protected InteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!level.isClientSide()) {
            int pre = state.getValue(BlockStateProperties.LEVEL);
            int newValue = (player.isShiftKeyDown() ? pre - 1 : pre + 1) & ((1 << 4) - 1);
            level.setBlock(pos, state.setValue(BlockStateProperties.LEVEL, newValue), Block.UPDATE_ALL);
            player.displayClientMessage(Component.literal("Change generator mode to %d(%d FE)".formatted(newValue, ENERGY[newValue] / PowerEntity.ONE_FE)), false);
        }
        return InteractionResult.SUCCESS;
    }
}
