package com.yogpc.qp.machine.mover;

import com.yogpc.qp.PlatformAccess;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.GameType;

import java.util.List;

import static com.yogpc.qp.gametest.GameTestFunctions.getEnchantment;
import static org.junit.jupiter.api.Assertions.*;

@SuppressWarnings("DuplicatedCode")
public final class PlaceMoverTest {
    static final BlockPos base = BlockPos.ZERO.above();

    public static void place(GameTestHelper helper) {
        helper.setBlock(base, PlatformAccess.getAccess().registerObjects().moverBlock().get());
        assertInstanceOf(MoverBlock.class, helper.getBlockState(base).getBlock());
        assertInstanceOf(MoverEntity.class, helper.getBlockEntity(base));
        helper.succeed();
    }

    public static void setItem(GameTestHelper helper) {
        helper.setBlock(base, PlatformAccess.getAccess().registerObjects().moverBlock().get());
        MoverEntity mover = helper.getBlockEntity(base);

        var stack = new ItemStack(Items.DIAMOND_PICKAXE);
        stack.enchant(getEnchantment(helper, Enchantments.EFFICIENCY), 4);
        mover.inventory.setItem(0, stack);
        mover.inventory.setItem(1, new ItemStack(PlatformAccess.getAccess().registerObjects().quarryBlock().get()));

        assertEquals(List.of(getEnchantment(helper, Enchantments.EFFICIENCY)), mover.movableEnchantments);
        helper.succeed();
    }


    public static void move(GameTestHelper helper) {
        helper.setBlock(base, PlatformAccess.getAccess().registerObjects().moverBlock().get());
        MoverEntity mover = helper.getBlockEntity(base);

        var stack = new ItemStack(Items.DIAMOND_PICKAXE);
        var quarry = new ItemStack(PlatformAccess.getAccess().registerObjects().quarryBlock().get());
        var enchantment = getEnchantment(helper, Enchantments.EFFICIENCY);
        stack.enchant(enchantment, 4);
        mover.inventory.setItem(0, stack);
        mover.inventory.setItem(1, quarry);

        mover.moveEnchant(enchantment);
        assertEquals(3, stack.getEnchantments().getLevel(enchantment));
        assertEquals(1, quarry.getEnchantments().getLevel(enchantment));
        helper.succeed();
    }

    public static void moveLast(GameTestHelper helper) {
        helper.setBlock(base, PlatformAccess.getAccess().registerObjects().moverBlock().get());
        MoverEntity mover = helper.getBlockEntity(base);

        var stack = new ItemStack(Items.DIAMOND_PICKAXE);
        var quarry = new ItemStack(PlatformAccess.getAccess().registerObjects().quarryBlock().get());
        var enchantment = getEnchantment(helper, Enchantments.EFFICIENCY);
        stack.enchant(enchantment, 1);
        mover.inventory.setItem(0, stack);
        mover.inventory.setItem(1, quarry);
        mover.moveEnchant(enchantment);

        assertTrue(stack.getEnchantments().isEmpty());
        assertEquals(1, quarry.getEnchantments().getLevel(enchantment));
        assertTrue(mover.movableEnchantments.isEmpty());
        helper.succeed();
    }

    public static void drop(GameTestHelper helper) {
        helper.setBlock(base, PlatformAccess.getAccess().registerObjects().moverBlock().get());
        MoverEntity mover = helper.getBlockEntity(base);
        var stack = new ItemStack(Items.DIAMOND_PICKAXE);
        var quarry = new ItemStack(PlatformAccess.getAccess().registerObjects().quarryBlock().get());
        var enchantment = getEnchantment(helper, Enchantments.EFFICIENCY);
        stack.enchant(enchantment, 1);
        mover.inventory.setItem(0, stack);
        mover.inventory.setItem(1, quarry);

        helper.getLevel().destroyBlock(helper.absolutePos(base), true, helper.makeMockPlayer(GameType.SURVIVAL));
        helper.assertItemEntityPresent(PlatformAccess.getAccess().registerObjects().moverBlock().get().blockItem, base, 4);
        helper.assertItemEntityPresent(Items.DIAMOND_PICKAXE, base, 4);
        helper.assertItemEntityPresent(PlatformAccess.getAccess().registerObjects().quarryBlock().get().blockItem, base, 4);
        helper.succeed();
    }

    public static void moveFromEnchantedBook(GameTestHelper helper) {
        helper.setBlock(base, PlatformAccess.getAccess().registerObjects().moverBlock().get());
        MoverEntity mover = helper.getBlockEntity(base);

        var enchantment = getEnchantment(helper, Enchantments.EFFICIENCY);
        var stack = EnchantmentHelper.createBook(new EnchantmentInstance(enchantment, 1));
        var quarry = new ItemStack(PlatformAccess.getAccess().registerObjects().quarryBlock().get());
        mover.inventory.setItem(0, stack);
        mover.inventory.setItem(1, quarry);

        mover.moveEnchant(enchantment);
        assertAll(
            () -> assertEquals(1, quarry.getEnchantments().getLevel(enchantment)),
            () -> assertTrue(mover.inventory.getItem(0).isEmpty()),
            () -> assertTrue(mover.movableEnchantments.isEmpty())
        );
        helper.succeed();
    }
}
