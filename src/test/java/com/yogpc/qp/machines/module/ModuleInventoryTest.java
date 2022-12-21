package com.yogpc.qp.machines.module;

import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestGenerator;
import net.minecraft.gametest.framework.TestFunction;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import com.kotori316.testutil.GameTestUtil;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ModuleInventoryTest {
    static final String BATCH = "ModuleInventory";

    @GameTestHolder(QuarryPlus.modID)
    @PrefixGameTestTemplate(value = false)
    static class SingleTest {
        @GameTest(template = GameTestUtil.EMPTY_STRUCTURE, batch = BATCH)
        void getModules1() {
            var modules = ModuleInventory.getModules(Stream.of(new ItemStack(Holder.ITEM_PUMP_MODULE)));
            assertEquals(Set.of(QuarryModule.Constant.PUMP), Set.copyOf(modules));
        }

        @GameTest(template = GameTestUtil.EMPTY_STRUCTURE, batch = BATCH)
        void getModules2() {
            var modules = ModuleInventory.getModules(Stream.of(new ItemStack(Holder.ITEM_BEDROCK_MODULE)));
            assertEquals(Set.of(QuarryModule.Constant.BEDROCK), Set.copyOf(modules));
        }

        @GameTest(template = GameTestUtil.EMPTY_STRUCTURE, batch = BATCH)
        void getModules3() {
            var modules = ModuleInventory.getModules(Stream.of(new ItemStack(Holder.ITEM_FILLER_MODULE)));
            assertEquals(Set.of(QuarryModule.Constant.FILLER), Set.copyOf(modules));
        }
    }

    @GameTestHolder(QuarryPlus.modID)
    @PrefixGameTestTemplate(value = false)
    static class MultiTest {
        @GameTest(template = GameTestUtil.EMPTY_STRUCTURE, batch = BATCH)
        void getModules3() {
            var modules = ModuleInventory.getModules(Stream.of(new ItemStack(Holder.ITEM_BEDROCK_MODULE), new ItemStack(Holder.ITEM_PUMP_MODULE)));
            assertEquals(Set.of(QuarryModule.Constant.BEDROCK, QuarryModule.Constant.PUMP), Set.copyOf(modules));
        }

        @GameTest(template = GameTestUtil.EMPTY_STRUCTURE, batch = BATCH)
        void getModules4() {
            var modules = ModuleInventory.getModules(Stream.of(new ItemStack(Holder.ITEM_PUMP_MODULE), new ItemStack(Holder.ITEM_PUMP_MODULE)));
            assertEquals(Set.of(QuarryModule.Constant.PUMP), Set.copyOf(modules));
        }
    }

    @GameTestHolder(QuarryPlus.modID)
    @PrefixGameTestTemplate(value = false)
    static class InventoryTest {
        @GameTestGenerator
        List<TestFunction> empty() {
            var inv = new InventoryHolder();
            return Stream.of(
                GameTestUtil.create(QuarryPlus.modID, BATCH, "module_inventory_pump_module", () -> assertFalse(inv.hasPumpModule())),
                GameTestUtil.create(QuarryPlus.modID, BATCH, "module_inventory_bedrock_module", () -> assertFalse(inv.hasBedrockModule())),
                GameTestUtil.create(QuarryPlus.modID, BATCH, "module_inventory_filler_module", () -> assertFalse(inv.hasFillerModule())),
                GameTestUtil.create(QuarryPlus.modID, BATCH, "module_inventory_exp_module", () -> assertTrue(inv.getExpModule().isEmpty())),
                GameTestUtil.create(QuarryPlus.modID, BATCH, "module_inventory_replacer_module", () -> assertTrue(inv.getReplacerModule().isEmpty()))
            ).toList();
        }

        @GameTestGenerator
        List<TestFunction> has() {
            return IntStream.of(0, 1, 2, 3, 4).boxed()
                .flatMap(i -> Stream.of(
                    GameTestUtil.create(QuarryPlus.modID, BATCH, "hasPump_%d".formatted(i), () -> hasPump(i)),
                    GameTestUtil.create(QuarryPlus.modID, BATCH, "hasBedrock_%d".formatted(i), () -> hasBedrock(i)),
                    GameTestUtil.create(QuarryPlus.modID, BATCH, "hasFiller_%d".formatted(i), () -> hasFiller(i)),
                    GameTestUtil.create(QuarryPlus.modID, BATCH, "hasExp_%d".formatted(i), () -> hasExp(i))
                )).toList();
        }

        void hasPump(int slot) {
            var inv = new InventoryHolder();
            inv.inventory.setItem(slot, new ItemStack(Holder.ITEM_PUMP_MODULE));
            assertTrue(inv.hasPumpModule());
        }

        void hasBedrock(int slot) {
            var inv = new InventoryHolder();
            inv.inventory.setItem(slot, new ItemStack(Holder.ITEM_BEDROCK_MODULE));
            assertTrue(inv.hasBedrockModule());
        }

        void hasFiller(int slot) {
            var inv = new InventoryHolder();
            inv.inventory.setItem(slot, new ItemStack(Holder.ITEM_FILLER_MODULE));
            assertTrue(inv.hasFillerModule());
        }

        void hasExp(int slot) {
            var inv = new InventoryHolder();
            inv.inventory.setItem(slot, new ItemStack(Holder.ITEM_EXP_MODULE));
            assertTrue(inv.getExpModule().isPresent());
        }

        @GameTestGenerator
        List<TestFunction> multi() {
            var inv = new InventoryHolder();
            inv.inventory.setItem(0, new ItemStack(Holder.ITEM_PUMP_MODULE));
            inv.inventory.setItem(1, new ItemStack(Holder.ITEM_BEDROCK_MODULE));
            inv.inventory.setItem(2, new ItemStack(Holder.ITEM_FILLER_MODULE));
            inv.inventory.setItem(3, new ItemStack(Holder.ITEM_EXP_MODULE));
            return Stream.of(
                GameTestUtil.create(QuarryPlus.modID, BATCH, "has_multi_filler_module", () -> assertTrue(inv.hasFillerModule())),
                GameTestUtil.create(QuarryPlus.modID, BATCH, "has_multi_bedrock_module", () -> assertTrue(inv.hasBedrockModule())),
                GameTestUtil.create(QuarryPlus.modID, BATCH, "has_multi_pump_module", () -> assertTrue(inv.hasPumpModule())),
                GameTestUtil.create(QuarryPlus.modID, BATCH, "has_multi_exp_module", () -> assertTrue(inv.getExpModule().isPresent()))
            ).toList();
        }
    }

    static class InventoryHolder implements ModuleInventory.HasModuleInventory {
        private final ModuleInventory inventory = new ModuleInventory(5, () -> {
        }, Objects::nonNull, this);

        @Override
        public ModuleInventory getModuleInventory() {
            return inventory;
        }

        @Override
        public Set<QuarryModule> getLoadedModules() {
            return Set.copyOf(inventory.getModules());
        }
    }
}
