package com.yogpc.qp.fabric;

import com.yogpc.qp.PlatformAccess;
import com.yogpc.qp.gametest.GameTestFunctions;
import net.fabricmc.fabric.api.gametest.v1.FabricGameTest;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantments;

import static org.junit.jupiter.api.Assertions.assertTrue;

public final class EnchantmentTest implements FabricGameTest {

    @GameTest(template = EMPTY_STRUCTURE)
    public void quarryEnchantmentEfficiency(GameTestHelper helper) {
        var enchantment = GameTestFunctions.getEnchantment(helper, Enchantments.EFFICIENCY);
        var stack = new ItemStack(PlatformAccess.getAccess().registerObjects().quarryBlock().get());
        assertTrue(enchantment.value().canEnchant(stack));
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void quarryEnchantmentUnbreaking(GameTestHelper helper) {
        var enchantment = GameTestFunctions.getEnchantment(helper, Enchantments.UNBREAKING);
        var stack = new ItemStack(PlatformAccess.getAccess().registerObjects().quarryBlock().get());
        assertTrue(enchantment.value().canEnchant(stack));
        helper.succeed();
    }

    @GameTest(template = EMPTY_STRUCTURE)
    public void quarryEnchantmentFortune(GameTestHelper helper) {
        var enchantment = GameTestFunctions.getEnchantment(helper, Enchantments.FORTUNE);
        var stack = new ItemStack(PlatformAccess.getAccess().registerObjects().quarryBlock().get());
        assertTrue(enchantment.value().canEnchant(stack));
        helper.succeed();
    }
}
