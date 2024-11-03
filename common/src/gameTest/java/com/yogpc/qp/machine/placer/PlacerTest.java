package com.yogpc.qp.machine.placer;

import com.google.common.base.CaseFormat;
import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.gametest.GameTestFunctions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;

import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public final class PlacerTest {
    private static final BlockPos placerPos = new BlockPos(2, 2, 2);

    public static Stream<TestFunction> tests(String batchName, String structureName) {
        return Stream.of(
            PlacerTestDirect.removeBlock(batchName, structureName),
            PlacerTestDirect.placeBlock1(batchName, structureName),
            GameTestFunctions.getTestFunctionStream(batchName, structureName, List.of(
                PlacerTestDirect.class,
                PlacerTestRedStone.class,
                PlacerTest.class
            ), 10)
        ).flatMap(Function.identity());
    }

    static void placePlacerBlock(GameTestHelper helper) {
        helper.setBlock(placerPos, PlatformAccess.getAccess().registerObjects().placerBlock().get());
        helper.assertBlockPresent(PlatformAccess.getAccess().registerObjects().placerBlock().get(), placerPos);
        BlockEntity entity = helper.getBlockEntity(placerPos);
        assertInstanceOf(PlacerEntity.class, entity);
        helper.succeed();
    }

    static class PlacerTestDirect {
        private static Stream<TestFunction> removeBlock(String batchName, String structureName) {
            return Stream.of(Direction.values())
                .flatMap(direction -> Stream.of(Blocks.STONE, Blocks.ICE, Blocks.ACACIA_LOG, Blocks.BIRCH_LEAVES).map(b -> {
                    var r = Rotation.NONE;
                    var blockName = BuiltInRegistries.BLOCK.getKey(b);
                    var name = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, "PlacerTest_removeBlock_%s_%s_%s".formatted(
                        direction.name().toLowerCase(Locale.ROOT), r.name().toLowerCase(Locale.ROOT), blockName.getPath()));
                    return new TestFunction(batchName, name, structureName, r, 20, 0, true, g -> removeBlock(g, direction, b));
                }));
        }

        private static void removeBlock(GameTestHelper helper, Direction direction, Block block) {
            var placerBlock = PlatformAccess.getAccess().registerObjects().placerBlock().get();
            var stonePos = placerPos.relative(direction);
            helper.startSequence()
                .thenExecuteAfter(1, () -> helper.setBlock(placerPos, placerBlock.defaultBlockState().setValue(BlockStateProperties.FACING, direction)))
                .thenExecuteAfter(1, () -> helper.setBlock(stonePos, block))
                .thenExecuteAfter(1, () -> {
                    BlockEntity entity = helper.getBlockEntity(placerPos);
                    assertInstanceOf(PlacerEntity.class, entity).breakBlock();
                })
                .thenExecuteAfter(1, () -> helper.assertBlockNotPresent(block, stonePos))
                .thenExecuteAfter(1, () -> {
                    PlacerEntity placer = helper.getBlockEntity(placerPos);
                    assertEquals(1, placer.countItem(block.asItem()), "Placer should contain removed item");
                })
                .thenSucceed();
        }

        private static Stream<TestFunction> placeBlock1(String batchName, String structureName) {
            return Stream.of(Direction.values())
                .flatMap(direction -> Stream.of(Blocks.STONE, Blocks.ICE, Blocks.ACACIA_LOG, Blocks.BIRCH_LEAVES).map(b -> {
                    var r = Rotation.NONE;
                    var blockName = BuiltInRegistries.BLOCK.getKey(b);
                    var name = CaseFormat.LOWER_CAMEL.to(CaseFormat.LOWER_UNDERSCORE, "PlacerTest_placeBlock1_%s_%s_%s".formatted(
                        direction.name().toLowerCase(Locale.ROOT), r.name().toLowerCase(Locale.ROOT), blockName.getPath()));
                    return new TestFunction(batchName, name, structureName, r, 20, 0, true, g -> placeBlock1(g, direction, b));
                }));
        }

        private static void placeBlock1(GameTestHelper helper, Direction direction, Block block) {
            var placerBlock = PlatformAccess.getAccess().registerObjects().placerBlock().get();
            var stonePos = placerPos.relative(direction);
            helper.startSequence()
                .thenExecuteAfter(1, () -> helper.setBlock(placerPos, placerBlock.defaultBlockState().setValue(BlockStateProperties.FACING, direction)))
                .thenExecuteAfter(1, () -> {
                    PlacerEntity placer = helper.getBlockEntity(placerPos);
                    placer.setItem(0, new ItemStack(block));
                })
                .thenExecuteAfter(1, () -> {
                    PlacerEntity placer = helper.getBlockEntity(placerPos);
                    placer.placeBlock();
                })
                .thenExecuteAfter(1, () -> helper.assertBlockPresent(block, stonePos))
                .thenExecuteAfter(1, () -> {
                    PlacerEntity placer = helper.getBlockEntity(placerPos);
                    assertEquals(0, placer.countItem(block.asItem()), "Placer must not contain placed item");
                })
                .thenSucceed();
        }

        static void notPlaceMode(GameTestHelper helper) {
            var placerBlock = PlatformAccess.getAccess().registerObjects().placerBlock().get();
            helper.setBlock(placerPos, placerBlock.defaultBlockState().setValue(BlockStateProperties.FACING, Direction.NORTH));
            PlacerEntity tile = helper.getBlockEntity(placerPos);
            tile.redstoneMode = AbstractPlacerTile.RedStoneMode.PULSE_BREAK_ONLY;
            helper.startSequence()
                .thenExecuteAfter(1, () -> tile.setItem(0, new ItemStack(Blocks.STONE)))
                .thenExecuteAfter(1, tile::placeBlock)
                .thenExecuteAfter(1, () -> helper.assertBlockNotPresent(Blocks.STONE, placerPos.relative(Direction.NORTH)))
                .thenSucceed();
        }

        static void notBreakMode(GameTestHelper helper) {
            var placerBlock = PlatformAccess.getAccess().registerObjects().placerBlock().get();
            helper.setBlock(placerPos, placerBlock.defaultBlockState().setValue(BlockStateProperties.FACING, Direction.NORTH));
            helper.setBlock(placerPos.relative(Direction.NORTH), Blocks.STONE);
            PlacerEntity tile = helper.getBlockEntity(placerPos);
            tile.redstoneMode = AbstractPlacerTile.RedStoneMode.PULSE_PLACE_ONLY;
            helper.startSequence()
                .thenExecuteAfter(1, tile::breakBlock)
                .thenExecuteAfter(1, () -> helper.assertBlockPresent(Blocks.STONE, placerPos.relative(Direction.NORTH)))
                .thenSucceed();
        }

        static void placeInWater(GameTestHelper helper) {
            var waterPos = placerPos.above();
            var placerBlock = PlatformAccess.getAccess().registerObjects().placerBlock().get();
            helper.setBlock(placerPos, placerBlock.defaultBlockState().setValue(BlockStateProperties.FACING, Direction.UP));
            helper.setBlock(waterPos, Blocks.WATER);
            PlacerEntity tile = helper.getBlockEntity(placerPos);

            helper.startSequence()
                .thenExecuteAfter(1, () -> tile.setItem(0, new ItemStack(Blocks.STONE)))
                .thenExecuteAfter(1, tile::placeBlock)
                .thenExecuteAfter(1, () -> helper.assertBlockPresent(Blocks.STONE, placerPos.relative(Direction.UP)))
                .thenExecute(() -> assertTrue(tile.getItem(0).isEmpty()))
                .thenSucceed();
        }

        static void cantPlaceInSolidBlock(GameTestHelper helper) {
            var blockPos = placerPos.above();
            var placerBlock = PlatformAccess.getAccess().registerObjects().placerBlock().get();
            helper.setBlock(placerPos, placerBlock.defaultBlockState().setValue(BlockStateProperties.FACING, Direction.UP));
            helper.setBlock(blockPos, Blocks.DIAMOND_BLOCK);
            PlacerEntity tile = helper.getBlockEntity(placerPos);

            helper.startSequence()
                .thenExecuteAfter(1, () -> tile.setItem(0, new ItemStack(Blocks.STONE)))
                .thenExecuteAfter(1, tile::placeBlock)
                .thenExecuteAfter(1, () -> helper.assertBlockPresent(Blocks.DIAMOND_BLOCK, placerPos.relative(Direction.UP)))
                .thenExecute(() -> assertEquals(Blocks.STONE.asItem(), tile.getItem(0).getItem()))
                .thenSucceed();
        }
    }

    static class PlacerTestRedStone {
        static void sendRedStoneSignal(GameTestHelper helper) {
            var stonePos = placerPos.relative(Direction.NORTH);
            var placerBlock = PlatformAccess.getAccess().registerObjects().placerBlock().get();
            helper.setBlock(placerPos, placerBlock.defaultBlockState().setValue(BlockStateProperties.FACING, Direction.NORTH));
            PlacerEntity tile = helper.getBlockEntity(placerPos);

            helper.startSequence()
                .thenExecuteAfter(1, () -> tile.setItem(0, new ItemStack(Blocks.STONE)))
                .thenExecuteAfter(1, () -> helper.setBlock(stonePos, Blocks.STONE))
                .thenExecuteAfter(1, () -> helper.setBlock(placerPos.relative(Direction.EAST), Blocks.REDSTONE_BLOCK))
                .thenExecuteAfter(1, () -> helper.assertBlockPresent(Blocks.STONE, stonePos))
                .thenSucceed();
        }
    }
}
