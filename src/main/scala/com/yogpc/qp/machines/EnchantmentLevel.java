package com.yogpc.qp.machines;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

import com.yogpc.qp.utils.ManualOrder;
import net.minecraft.core.Registry;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.item.enchantment.Enchantments;
import org.jetbrains.annotations.Nullable;

public record EnchantmentLevel(Enchantment enchantment, int level) {
    public EnchantmentLevel(Map.Entry<Enchantment, Integer> entry) {
        this(entry.getKey(), entry.getValue());
    }

    public EnchantmentLevel(ResourceLocation enchantmentID, int level) {
        this(Registry.ENCHANTMENT.get(enchantmentID), level);
    }

    @Nullable
    public ResourceLocation enchantmentID() {
        return Registry.ENCHANTMENT.getKey(enchantment());
    }

    public interface HasEnchantments {
        List<EnchantmentLevel> getEnchantments();

        default int getLevel(Enchantment enchantment) {
            return getEnchantments().stream().filter(e -> e.enchantment() == enchantment)
                .mapToInt(EnchantmentLevel::level).findFirst().orElse(0);
        }

        default int efficiencyLevel() {
            return getLevel(Enchantments.BLOCK_EFFICIENCY);
        }

        default int unbreakingLevel() {
            return getLevel(Enchantments.UNBREAKING);
        }

        default int fortuneLevel() {
            return getLevel(Enchantments.BLOCK_FORTUNE);
        }

        default int silktouchLevel() {
            return getLevel(Enchantments.SILK_TOUCH);
        }

        default ItemStack getPickaxe() {
            var stack = new ItemStack(Items.NETHERITE_PICKAXE);
            getEnchantments().forEach(e -> stack.enchant(e.enchantment(), e.level()));
            return stack;
        }
    }

    public static List<EnchantmentLevel> fromItem(ItemStack stack) {
        var enchantmentList = stack.getEnchantmentTags();
        if (enchantmentList.isEmpty()) return Collections.emptyList();
        List<EnchantmentLevel> list = new ArrayList<>(enchantmentList.size());
        for (int i = 0; i < enchantmentList.size(); i++) {
            var tag = enchantmentList.getCompound(i);
            var name = EnchantmentHelper.getEnchantmentId(tag);
            var level = EnchantmentHelper.getEnchantmentLevel(tag);
            if (name != null && Registry.ENCHANTMENT.containsKey(name)) {
                list.add(new EnchantmentLevel(name, level));
            }
        }
        return list;
    }

    public static final Comparator<EnchantmentLevel> COMPARATOR =
        Comparator.comparingInt((EnchantmentLevel e) -> Registry.ENCHANTMENT.getId(e.enchantment()))
            .thenComparingInt(EnchantmentLevel::level);
    public static final Comparator<EnchantmentLevel> QUARRY_ENCHANTMENT_COMPARATOR =
        Comparator.comparing(EnchantmentLevel::enchantment,
            ManualOrder.builder(Comparator.comparingInt(Registry.ENCHANTMENT::getId))
                .add(Enchantments.BLOCK_EFFICIENCY)
                .add(Enchantments.UNBREAKING)
                .add(Enchantments.BLOCK_FORTUNE)
                .add(Enchantments.SILK_TOUCH)
                .build()
        ).thenComparingInt(EnchantmentLevel::level);
}
