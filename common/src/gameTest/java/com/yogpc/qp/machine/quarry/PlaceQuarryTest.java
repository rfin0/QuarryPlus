package com.yogpc.qp.machine.quarry;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.gametest.GameTestFunctions;
import com.yogpc.qp.machine.PowerEntity;
import com.yogpc.qp.machine.module.ModuleInventoryHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.item.enchantment.ItemEnchantments;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.block.Block;

import static org.junit.jupiter.api.Assertions.*;

public final class PlaceQuarryTest {
    static final BlockPos base = BlockPos.ZERO.above();

    public static void place(GameTestHelper helper) {
        helper.setBlock(base, PlatformAccess.getAccess().registerObjects().quarryBlock().get());
        assertInstanceOf(QuarryBlock.class, helper.getBlockState(base).getBlock());
        assertInstanceOf(QuarryEntity.class, helper.getBlockEntity(base));
        helper.succeed();
    }

    public static void checkDropNormal(GameTestHelper helper) {
        helper.setBlock(base, PlatformAccess.getAccess().registerObjects().quarryBlock().get());
        var drop = Block.getDrops(helper.getBlockState(base), helper.getLevel(), helper.absolutePos(base), helper.getBlockEntity(base));
        assertFalse(drop.isEmpty());
        var quarryStack = drop.getFirst();
        assertInstanceOf(QuarryItem.class, quarryStack.getItem());
        assertFalse(quarryStack.isEnchanted());
        helper.succeed();
    }

    public static void checkDropEnchanted(GameTestHelper helper) {
        helper.setBlock(base, PlatformAccess.getAccess().registerObjects().quarryBlock().get());
        var mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        mutable.set(GameTestFunctions.getEnchantment(helper, Enchantments.EFFICIENCY), 3);
        helper.<QuarryEntity>getBlockEntity(base).setEnchantments(mutable.toImmutable());

        var drop = Block.getDrops(helper.getBlockState(base), helper.getLevel(), helper.absolutePos(base), helper.getBlockEntity(base));
        assertFalse(drop.isEmpty());
        var quarryStack = drop.getFirst();
        assertInstanceOf(QuarryItem.class, quarryStack.getItem());
        assertTrue(quarryStack.isEnchanted());
        assertEquals(1, quarryStack.getEnchantments().size());
        assertEquals(3, quarryStack.getEnchantments().getLevel(GameTestFunctions.getEnchantment(helper, Enchantments.EFFICIENCY)));
        helper.succeed();
    }

    public static void checkDropEnchanted2(GameTestHelper helper) {
        helper.setBlock(base, PlatformAccess.getAccess().registerObjects().quarryBlock().get());
        var mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        mutable.set(GameTestFunctions.getEnchantment(helper, Enchantments.EFFICIENCY), 5);
        mutable.set(GameTestFunctions.getEnchantment(helper, Enchantments.UNBREAKING), 3);
        helper.<QuarryEntity>getBlockEntity(base).setEnchantments(mutable.toImmutable());

        var drop = Block.getDrops(helper.getBlockState(base), helper.getLevel(), helper.absolutePos(base), helper.getBlockEntity(base));
        var quarryStack = drop.getFirst();
        assertInstanceOf(QuarryItem.class, quarryStack.getItem());
        assertTrue(quarryStack.isEnchanted());
        assertEquals(2, quarryStack.getEnchantments().size());
        assertEquals(5, quarryStack.getEnchantments().getLevel(GameTestFunctions.getEnchantment(helper, Enchantments.EFFICIENCY)));
        assertEquals(3, quarryStack.getEnchantments().getLevel(GameTestFunctions.getEnchantment(helper, Enchantments.UNBREAKING)));
        helper.succeed();
    }

    public static void saveEnchantment(GameTestHelper helper) {
        helper.setBlock(base, PlatformAccess.getAccess().registerObjects().quarryBlock().get());
        QuarryEntity quarry = helper.getBlockEntity(base);

        var mutable = new ItemEnchantments.Mutable(ItemEnchantments.EMPTY);
        mutable.set(GameTestFunctions.getEnchantment(helper, Enchantments.EFFICIENCY), 5);
        mutable.set(GameTestFunctions.getEnchantment(helper, Enchantments.UNBREAKING), 3);
        quarry.setEnchantments(mutable.toImmutable());

        var saved = quarry.saveWithFullMetadata(helper.getLevel().registryAccess());
        quarry.loadWithComponents(saved, helper.getLevel().registryAccess());

        var enchantments = quarry.getEnchantments();
        assertEquals(5, enchantments.getLevel(GameTestFunctions.getEnchantment(helper, Enchantments.EFFICIENCY)));
        assertEquals(3, enchantments.getLevel(GameTestFunctions.getEnchantment(helper, Enchantments.UNBREAKING)));
        assertEquals(0, enchantments.getLevel(GameTestFunctions.getEnchantment(helper, Enchantments.FORTUNE)));

        helper.succeed();
    }

    public static void placeQuarry1(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var stack = new ItemStack(PlatformAccess.getAccess().registerObjects().quarryBlock().get());
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);

        helper.placeAt(player, stack, base.below(), Direction.UP);
        helper.assertBlockPresent(PlatformAccess.getAccess().registerObjects().quarryBlock().get(), base);

        QuarryEntity quarry = helper.getBlockEntity(base);
        var enchantments = quarry.getEnchantments();
        assertTrue(enchantments.isEmpty());
        helper.succeed();
    }

    public static void placeQuarry2(GameTestHelper helper) {
        var player = helper.makeMockPlayer(GameType.SURVIVAL);
        var stack = new ItemStack(PlatformAccess.getAccess().registerObjects().quarryBlock().get());
        stack.enchant(GameTestFunctions.getEnchantment(helper, Enchantments.EFFICIENCY), 3);
        stack.enchant(GameTestFunctions.getEnchantment(helper, Enchantments.UNBREAKING), 3);
        player.setItemInHand(InteractionHand.MAIN_HAND, stack);

        helper.placeAt(player, stack, base.below(), Direction.UP);
        helper.assertBlockPresent(PlatformAccess.getAccess().registerObjects().quarryBlock().get(), base);

        QuarryEntity quarry = helper.getBlockEntity(base);
        var enchantments = quarry.getEnchantments();
        assertFalse(enchantments.isEmpty());
        assertEquals(3, enchantments.getLevel(GameTestFunctions.getEnchantment(helper, Enchantments.EFFICIENCY)));
        assertEquals(3, enchantments.getLevel(GameTestFunctions.getEnchantment(helper, Enchantments.UNBREAKING)));
        assertEquals(0, enchantments.getLevel(GameTestFunctions.getEnchantment(helper, Enchantments.SHARPNESS)));

        assertEquals(PlatformAccess.config().powerMap().quarry().maxEnergy() * 4 * PowerEntity.ONE_FE, quarry.getMaxEnergy());

        helper.succeed();
    }

    public static void accessModuleInventory(GameTestHelper helper) {
        helper.setBlock(base, PlatformAccess.getAccess().registerObjects().quarryBlock().get());
        QuarryEntity quarry = helper.getBlockEntity(base);
        var moduleInv = ModuleInventoryHolder.getFromObject(quarry);
        assertTrue(moduleInv.isPresent());
        assertTrue(moduleInv.get().getModules().isEmpty());

        helper.succeed();
    }
}
