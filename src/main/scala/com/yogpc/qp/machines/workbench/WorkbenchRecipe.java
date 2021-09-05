package com.yogpc.qp.machines.workbench;

import java.util.Comparator;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

import com.google.common.annotations.VisibleForTesting;
import com.yogpc.qp.QuarryPlus;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;

public abstract class WorkbenchRecipe implements Recipe<TileWorkbench> {
    public static final ResourceLocation recipeLocation = new ResourceLocation(QuarryPlus.modID, "workbench_recipe");
    public static final WorkbenchRecipeSerializer SERIALIZER = new WorkbenchRecipeSerializer();
    public static final RecipeType<WorkbenchRecipe> RECIPE_TYPE = RecipeType.register(recipeLocation.toString());
    @VisibleForTesting
    static RecipeFinder recipeFinder = new DefaultFinder();
    public static final Comparator<WorkbenchRecipe> COMPARATOR =
        Comparator.comparingLong(WorkbenchRecipe::getRequiredEnergy)
            .thenComparingInt(r -> Item.getId(r.getResultItem().getItem()))
            .thenComparing(WorkbenchRecipe::getId);

    private final ResourceLocation location;
    private final ItemStack output;
    private final long energy;
    private final boolean showInJEI;

    public WorkbenchRecipe(ResourceLocation location, ItemStack output, long energy, boolean showInJEI) {
        this.location = location;
        this.output = output;
        this.energy = energy;
        this.showInJEI = showInJEI;
    }

    @Override
    public String toString() {
        return "WorkbenchRecipe{id=" + location + ", output=" + output + ", energy=" + energy + '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        WorkbenchRecipe that = (WorkbenchRecipe) o;
        return energy == that.energy && location.equals(that.location) && ItemStack.isSame(this.output, that.output);
    }

    @Override
    public int hashCode() {
        return Objects.hash(location, energy);
    }

    public final long getRequiredEnergy() {
        return energy;
    }

    public final boolean showInJEI() {
        return showInJEI;
    }

    // Recipe interface implementation.
    @Override
    public final boolean matches(TileWorkbench workbench, Level level) {
        return hasContent() && hasAllRequiredItems(workbench.ingredientInventory);
    }

    @Override
    public final ItemStack assemble(TileWorkbench workbench) {
        return getOutput(workbench.ingredientInventory);
    }

    @Override
    public final boolean canCraftInDimensions(int width, int height) {
        return true;
    }

    @Override
    public final ItemStack getResultItem() {
        return output;
    }

    @Override
    public final ResourceLocation getId() {
        return location;
    }

    @Override
    public final RecipeSerializer<?> getSerializer() {
        return SERIALIZER;
    }

    @Override
    public final RecipeType<?> getType() {
        return RECIPE_TYPE;
    }

    @Override
    public final boolean isSpecial() {
        return true;
    }

    public static WorkbenchRecipe dummyRecipe() {
        return DummyRecipe.INSTANCE;
    }

    public static RecipeFinder getRecipeFinder() {
        return recipeFinder;
    }

    // Workbench Recipe methods.
    public abstract List<IngredientList> inputs();

    public boolean hasContent() {
        return true;
    }

    protected abstract String getSubTypeName();

    protected abstract ItemStack getOutput(List<ItemStack> inventory);

    protected boolean hasAllRequiredItems(List<ItemStack> inventory) {
        var copied = inventory.stream().map(ItemStack::copy).collect(Collectors.toList());
        for (IngredientList input : this.inputs()) {
            var found = copied.stream().anyMatch(input::shrink);
            if (!found) return false;
        }
        return copied.stream().allMatch(i -> i.getCount() >= 0);
    }

    public void consumeItems(List<ItemStack> inventory) {
        for (IngredientList input : inputs()) {
            for (ItemStack stack : inventory) {
                if (input.shrink(stack)) break;
            }
        }
    }
}
