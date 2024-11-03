package com.yogpc.qp.machine.placer;

import com.yogpc.qp.machine.QpEntityBlock;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.material.PushReaction;
import net.minecraft.world.phys.BlockHitResult;

import java.util.List;

import static net.minecraft.world.level.block.state.properties.BlockStateProperties.TRIGGERED;

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

    @Override
    protected ItemInteractionResult useItemOn(ItemStack stack, BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult hitResult) {
        if (!player.isCrouching() && stack.is(Items.REDSTONE_TORCH)) {
            if (!level.isClientSide && level.getBlockEntity(pos) instanceof AbstractPlacerTile placer) {
                placer.cycleRedStoneMode();
                player.displayClientMessage(Component.translatable("quarryplus.chat.placer_rs", placer.redstoneMode), false);
            }
            return ItemInteractionResult.SUCCESS;
        }
        return super.useItemOn(stack, state, level, pos, player, hand, hitResult);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        super.tick(state, level, pos, random);
        if (level.getBlockEntity(pos) instanceof AbstractPlacerTile placer) {
            boolean isPowered = state.getValue(TRIGGERED);
            if (isPowered) {
                placer.placeBlock();
            } else {
                placer.breakBlock();
            }
        }
    }

    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block neighborBlock, BlockPos neighborPos, boolean movedByPiston) {
        super.neighborChanged(state, level, pos, neighborBlock, neighborPos, movedByPiston);
        if (!level.isClientSide() && level.getBlockEntity(pos) instanceof AbstractPlacerTile placer) {
            boolean poweredNow = placer.isPowered();
            boolean poweredOld = state.getValue(TRIGGERED);
            if (poweredNow != poweredOld) {
                level.scheduleTick(pos, this, 1);
                level.setBlock(pos, state.setValue(TRIGGERED, poweredNow), Block.UPDATE_INVISIBLE);
            }
        }
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltipComponents, TooltipFlag tooltipFlag) {
        super.appendHoverText(stack, context, tooltipComponents, tooltipFlag);
        if (Screen.hasShiftDown()) {
            tooltipComponents.add(Component.translatable("quarryplus.tooltip.placer_plus"));
        }
    }
}
