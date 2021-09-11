package com.yogpc.qp.data;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

import com.google.gson.JsonObject;
import com.yogpc.qp.Holder;
import com.yogpc.qp.machines.PowerTile;
import com.yogpc.qp.machines.advpump.BlockAdvPump;
import com.yogpc.qp.machines.checker.ItemChecker;
import com.yogpc.qp.machines.marker.BlockMarker;
import com.yogpc.qp.machines.miningwell.MiningWellBlock;
import com.yogpc.qp.machines.misc.YSetterItem;
import com.yogpc.qp.machines.module.BedrockModuleItem;
import com.yogpc.qp.machines.module.ExpModuleItem;
import com.yogpc.qp.machines.module.ExpPumpBlock;
import com.yogpc.qp.machines.module.PumpModuleItem;
import com.yogpc.qp.machines.module.PumpPlusBlock;
import com.yogpc.qp.machines.mover.BlockMover;
import com.yogpc.qp.machines.quarry.QuarryBlock;
import com.yogpc.qp.machines.workbench.BlockWorkbench;
import com.yogpc.qp.machines.workbench.EnableCondition;
import com.yogpc.qp.machines.workbench.EnchantmentIngredient;
import com.yogpc.qp.machines.workbench.IngredientList;
import com.yogpc.qp.machines.workbench.IngredientRecipe;
import com.yogpc.qp.machines.workbench.IngredientWithCount;
import com.yogpc.qp.machines.workbench.QuarryDebugCondition;
import com.yogpc.qp.machines.workbench.WorkbenchRecipe;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.recipes.FinishedRecipe;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.Tag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.enchantment.EnchantmentInstance;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.ItemLike;
import net.minecraftforge.common.Tags;
import org.apache.commons.lang3.tuple.Pair;

public class Recipe extends QuarryPlusDataProvider.QuarryDataProvider {
    Recipe(DataGenerator generatorIn) {
        super(generatorIn);
    }

    @Override
    String directory() {
        return "recipes";
    }

    @Override
    List<? extends QuarryPlusDataProvider.DataBuilder> data() {
        return Stream.of(workbenchRecipes(), crafting(), debug()).flatMap(List::stream).toList();
    }

    private List<RecipeSerializeHelper> workbenchRecipes() {
        List<RecipeSerializeHelper> list = new ArrayList<>();
        // Quarry Plus
        list.add(RecipeSerializeHelper.by(new FinishedWorkbenchRecipe(new IngredientRecipe(
            QuarryPlusDataProvider.location(QuarryBlock.NAME), new ItemStack(Holder.BLOCK_QUARRY), 320000 * PowerTile.ONE_FE, true, List.of(
            makeList(Tags.Items.GEMS_DIAMOND, 32),
            makeList(Tags.Items.INGOTS_GOLD, 32),
            makeList(Tags.Items.INGOTS_IRON, 64),
            makeList(Tags.Items.DUSTS_REDSTONE, 16),
            makeList(Tags.Items.ENDER_PEARLS, 4)
        )))).addCondition(new EnableCondition(QuarryBlock.NAME)));
        // Pump Plus
        list.add(RecipeSerializeHelper.by(new FinishedWorkbenchRecipe(new IngredientRecipe(
            QuarryPlusDataProvider.location(PumpPlusBlock.NAME), new ItemStack(Holder.BLOCK_PUMP), 320000 * PowerTile.ONE_FE, true, List.of(
            makeList(Tags.Items.INGOTS_GOLD, 16),
            makeList(Tags.Items.INGOTS_IRON, 48),
            makeList(Tags.Items.DUSTS_REDSTONE, 64),
            makeList(Tags.Items.GLASS_COLORLESS, 512),
            makeList(Items.CACTUS, 80)
        )))).addCondition(new EnableCondition(PumpPlusBlock.NAME)));
        // Adv Pump
        list.add(RecipeSerializeHelper.by(new FinishedWorkbenchRecipe(new IngredientRecipe(
            QuarryPlusDataProvider.location(BlockAdvPump.NAME), new ItemStack(Holder.BLOCK_ADV_PUMP), 3200000 * PowerTile.ONE_FE, true, List.of(
            as(Pair.of(Ingredient.of(Holder.BLOCK_PUMP), 2), Pair.of(Ingredient.of(Holder.ITEM_PUMP_MODULE), 2)),
            makeList(Holder.BLOCK_MINING_WELL, 2),
            makeList(Holder.BLOCK_MARKER, 3)
        )))).addCondition(new EnableCondition(BlockAdvPump.NAME)));
        // Marker Plus
        list.add(RecipeSerializeHelper.by(new FinishedWorkbenchRecipe(new IngredientRecipe(
            QuarryPlusDataProvider.location(BlockMarker.NAME), new ItemStack(Holder.BLOCK_MARKER), 20000 * PowerTile.ONE_FE, true, List.of(
            makeList(Tags.Items.INGOTS_GOLD, 7),
            makeList(Tags.Items.INGOTS_IRON, 8),
            makeList(Tags.Items.DUSTS_REDSTONE, 12),
            makeList(Tags.Items.DUSTS_GLOWSTONE, 4),
            makeList(Items.LAPIS_LAZULI, 12)
        )))).addCondition(new EnableCondition(BlockMarker.NAME)));
        // Mining Well Plus
        list.add(RecipeSerializeHelper.by(new FinishedWorkbenchRecipe(new IngredientRecipe(
            QuarryPlusDataProvider.location(MiningWellBlock.NAME), new ItemStack(Holder.BLOCK_MINING_WELL), 160000 * PowerTile.ONE_FE, true, List.of(
            makeList(Tags.Items.INGOTS_GOLD, 3),
            makeList(Tags.Items.INGOTS_IRON, 16),
            makeList(Tags.Items.DUSTS_REDSTONE, 8)
        )))).addCondition(new EnableCondition(MiningWellBlock.NAME)));
        // Status Checker
        list.add(RecipeSerializeHelper.by(new FinishedWorkbenchRecipe(new IngredientRecipe(
            QuarryPlusDataProvider.location(ItemChecker.NAME), new ItemStack(Holder.ITEM_CHECKER), 80000 * PowerTile.ONE_FE, true, List.of(
            makeList(Tags.Items.INGOTS_GOLD, 16),
            makeList(Tags.Items.INGOTS_IRON, 24),
            makeList(Tags.Items.DUSTS_REDSTONE, 32),
            makeList(Tags.Items.OBSIDIAN, 4),
            makeList(Items.LAPIS_LAZULI, 8)
        )))).addCondition(new EnableCondition(ItemChecker.NAME)));
        // Y Setter
        list.add(RecipeSerializeHelper.by(new FinishedWorkbenchRecipe(new IngredientRecipe(
            QuarryPlusDataProvider.location(YSetterItem.NAME), new ItemStack(Holder.ITEM_Y_SETTER), 80000 * PowerTile.ONE_FE, true, List.of(
            makeList(Tags.Items.INGOTS_GOLD, 32),
            makeList(Tags.Items.GEMS_QUARTZ, 64),
            makeList(Items.REPEATER, 16),
            makeList(Items.COMPARATOR, 8)
        )))).addCondition(new EnableCondition(YSetterItem.NAME)));
        // Enchantment Mover
        list.add(RecipeSerializeHelper.by(new FinishedWorkbenchRecipe(new IngredientRecipe(
            QuarryPlusDataProvider.location(BlockMover.NAME), new ItemStack(Holder.BLOCK_MOVER), 320000 * PowerTile.ONE_FE, true, List.of(
            makeList(Tags.Items.GEMS_DIAMOND, 32),
            makeList(Tags.Items.INGOTS_GOLD, 8),
            makeList(Tags.Items.INGOTS_IRON, 8),
            makeList(Tags.Items.DUSTS_REDSTONE, 48),
            makeList(Tags.Items.OBSIDIAN, 64),
            makeList(Items.ANVIL, 2),
            makeList(Tags.Items.ENDER_PEARLS, 2)
        )))).addCondition(new EnableCondition(BlockMover.NAME)));
        // Remove Bedrock Module
        var diamond_pickaxe = new ItemStack(Items.DIAMOND_PICKAXE);
        diamond_pickaxe.removeTagKey(ItemStack.TAG_DAMAGE);
        list.add(RecipeSerializeHelper.by(new FinishedWorkbenchRecipe(new IngredientRecipe(
            QuarryPlusDataProvider.location(BedrockModuleItem.NAME), new ItemStack(Holder.ITEM_BEDROCK_MODULE), 640000 * PowerTile.ONE_FE, true, List.of(
            makeList(Tags.Items.OBSIDIAN, 32),
            makeList(Tags.Items.STORAGE_BLOCKS_DIAMOND, 16),
            new IngredientList(new IngredientWithCount(new EnchantmentIngredient(diamond_pickaxe, List.of(new EnchantmentInstance(Enchantments.SILK_TOUCH, 1)), false), 1))
        )))).addCondition(new EnableCondition(BedrockModuleItem.NAME)));
        // Fuel Module
        list.add(RecipeSerializeHelper.by(new FinishedWorkbenchRecipe(new IngredientRecipe(
            QuarryPlusDataProvider.location("fuel_module_normal"), new ItemStack(Holder.ITEM_FUEL_MODULE_NORMAL), 3200 * PowerTile.ONE_FE, true, List.of(
            makeList(Tags.Items.INGOTS_GOLD, 16),
            makeList(Tags.Items.RODS_BLAZE, 5),
            makeList(Tags.Items.NETHERRACK, 64),
            makeList(Items.FURNACE, 3)
        )))).addCondition(new EnableCondition("fuel_module_normal")));
        // Exp Pump
        var waterBottle = PotionUtils.setPotion(new ItemStack(Items.POTION), Potions.WATER);
        list.add(RecipeSerializeHelper.by(new FinishedWorkbenchRecipe(new IngredientRecipe(
            QuarryPlusDataProvider.location(ExpPumpBlock.NAME), new ItemStack(Holder.BLOCK_EXP_PUMP), 320000 * PowerTile.ONE_FE, true, List.of(
            makeList(Tags.Items.INGOTS_GOLD, 16),
            makeList(Tags.Items.INGOTS_IRON, 40),
            makeList(Tags.Items.DUSTS_REDSTONE, 64),
            as(Pair.of(IngredientWithCount.createNbtIngredient(waterBottle), 128), Pair.of(Ingredient.of(Items.EXPERIENCE_BOTTLE), 1)),
            makeList(Items.HAY_BLOCK, 32),
            makeList(Tags.Items.ENDER_PEARLS, 2)
        )))).addCondition(new EnableCondition(ExpPumpBlock.NAME)));

        return list;
    }

    @Nonnull
    private IngredientList makeList(ItemLike item, int count) {
        return new IngredientList(new IngredientWithCount(Ingredient.of(item), count));
    }

    @Nonnull
    private static IngredientList makeList(Tag<Item> tag, int count) {
        return new IngredientList(new IngredientWithCount(Ingredient.of(tag), count));
    }

    private static final String MODULE_RECIPE_GROUP = "quarryplus:group_module";

    private List<RecipeSerializeHelper> crafting() {
        List<RecipeSerializeHelper> list = new ArrayList<>();
        list.add(
            // WORKBENCH
            RecipeSerializeHelper.by(
                ShapedRecipeBuilder.shaped(Holder.BLOCK_WORKBENCH)
                    .pattern("III")
                    .pattern("GDG")
                    .pattern("RRR")
                    .define('D', Tags.Items.STORAGE_BLOCKS_DIAMOND)
                    .define('R', Items.REDSTONE)
                    .define('I', Tags.Items.STORAGE_BLOCKS_IRON)
                    .define('G', Tags.Items.STORAGE_BLOCKS_GOLD),
                null
            ).addCondition(new EnableCondition(BlockWorkbench.NAME)));
        list.add(
            // Flexible Marker
            RecipeSerializeHelper.by(
                ShapedRecipeBuilder.shaped(Holder.BLOCK_FLEX_MARKER)
                    .pattern("E")
                    .pattern("T")
                    .define('E', Tags.Items.GEMS_EMERALD)
                    .define('T', Holder.BLOCK_MARKER),
                null
            )
        );
        list.add(
            // Chunk Marker
            RecipeSerializeHelper.by(
                ShapedRecipeBuilder.shaped(Holder.BLOCK_16_MARKER)
                    .pattern("R")
                    .pattern("T")
                    .define('R', Tags.Items.DUSTS_REDSTONE)
                    .define('T', Holder.BLOCK_MARKER),
                null
            )
        );
        list.add(
            // Pump Module
            RecipeSerializeHelper.by(
                ShapelessRecipeBuilder.shapeless(Holder.ITEM_PUMP_MODULE)
                    .requires(Holder.BLOCK_PUMP)
                    .group(MODULE_RECIPE_GROUP), null
            ).addCondition(new EnableCondition(PumpModuleItem.NAME))
        );
        list.add(
            // Exp Module
            RecipeSerializeHelper.by(
                ShapelessRecipeBuilder.shapeless(Holder.ITEM_EXP_MODULE)
                    .requires(Holder.BLOCK_EXP_PUMP)
                    .group(MODULE_RECIPE_GROUP), null
            ).addCondition(new EnableCondition(ExpModuleItem.NAME))
        );
        return list;
    }

    private List<RecipeSerializeHelper> debug() {
        List<RecipeSerializeHelper> list = List.of(
            RecipeSerializeHelper.by(new FinishedWorkbenchRecipe(new IngredientRecipe(
                testLocation("diamond1"), new ItemStack(Items.DIAMOND), 640 * PowerTile.ONE_FE, true, List.of(
                as(Pair.of(Ingredient.of(Items.DIRT), 32), Pair.of(Ingredient.of(Items.STONE), 16))
            )))),
            RecipeSerializeHelper.by(new FinishedWorkbenchRecipe(new IngredientRecipe(
                testLocation("diamond2"), new ItemStack(Items.DIAMOND, 2), 640 * PowerTile.ONE_FE, true, List.of(
                as(Pair.of(Ingredient.of(Items.IRON_INGOT), 8), Pair.of(Ingredient.of(Tags.Items.INGOTS_GOLD), 4))
            ))))
        );

        return list.stream().map(r -> r.addCondition(new QuarryDebugCondition())).toList();
    }

    @SafeVarargs
    private static IngredientList as(Pair<Ingredient, Integer> first, Pair<Ingredient, Integer>... other) {
        return new IngredientList(
            Stream.concat(Stream.of(first), Stream.of(other))
                .map(p -> new IngredientWithCount(p.getKey(), p.getRight()))
                .toList()
        );
    }

    private static ResourceLocation testLocation(String path) {
        return QuarryPlusDataProvider.location("test_" + path);
    }
}

record FinishedWorkbenchRecipe(WorkbenchRecipe recipe) implements FinishedRecipe {

    @Override
    public void serializeRecipeData(JsonObject object) {
        WorkbenchRecipe.SERIALIZER.toJson(recipe, object);
    }

    @Override
    public ResourceLocation getId() {
        return recipe.getId();
    }

    @Override
    public RecipeSerializer<?> getType() {
        return WorkbenchRecipe.SERIALIZER;
    }

    @Nullable
    @Override
    public JsonObject serializeAdvancement() {
        return null;
    }

    @Nullable
    @Override
    public ResourceLocation getAdvancementId() {
        return null;
    }
}
