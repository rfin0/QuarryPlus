package com.yogpc.qp.integration.jei;

import java.util.Collections;

import com.mojang.blaze3d.vertex.PoseStack;
import com.yogpc.qp.Holder;
import com.yogpc.qp.QuarryPlus;
import com.yogpc.qp.machines.PowerTile;
import com.yogpc.qp.machines.workbench.IngredientList;
import com.yogpc.qp.machines.workbench.WorkbenchRecipe;
import mezz.jei.api.constants.VanillaTypes;
import mezz.jei.api.gui.IRecipeLayout;
import mezz.jei.api.gui.drawable.IDrawable;
import mezz.jei.api.gui.drawable.IDrawableAnimated;
import mezz.jei.api.gui.drawable.IDrawableStatic;
import mezz.jei.api.helpers.IGuiHelper;
import mezz.jei.api.ingredients.IIngredients;
import mezz.jei.api.recipe.category.IRecipeCategory;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

class WorkBenchRecipeCategory implements IRecipeCategory<WorkbenchRecipe> {
    public static final ResourceLocation UID = new ResourceLocation(QuarryPlus.modID, "jei_workbenchplus");
    private static final ResourceLocation backGround = new ResourceLocation(QuarryPlus.modID, "textures/gui/workbench_jei2.png");
    private static final int xOff = 0;
    private static final int yOff = 0;
    private final IGuiHelper helper;
    private final IDrawableAnimated animateBar;

    WorkBenchRecipeCategory(IGuiHelper helper) {
        this.helper = helper;
        IDrawableStatic bar = helper.createDrawable(backGround, xOff, 87, 160, 4);
        this.animateBar = helper.createAnimatedDrawable(bar, 300, IDrawableAnimated.StartDirection.LEFT, false);
    }

    @Override
    public ResourceLocation getUid() {
        return UID;
    }

    @Override
    public Class<? extends WorkbenchRecipe> getRecipeClass() {
        return WorkbenchRecipe.class;
    }

    @Override
    public Component getTitle() {
        return Holder.BLOCK_WORKBENCH.getName();
    }

    @Override
    public IDrawable getBackground() {
        return helper.createDrawable(backGround, xOff, yOff, 167, 86);
    }

    @Override
    public IDrawable getIcon() {
        return helper.createDrawableIngredient(new ItemStack(Holder.BLOCK_WORKBENCH));
    }

    @Override
    public void draw(WorkbenchRecipe recipe, PoseStack stack, double mouseX, double mouseY) {
        IRecipeCategory.super.draw(recipe, stack, mouseX, mouseY);
        animateBar.draw(stack, 4, 60);
        Minecraft.getInstance().font.draw(stack, (recipe.getRequiredEnergy() / PowerTile.ONE_FE) + "MJ", 36 - xOff, 70 - yOff, 0x404040);
        // Enchantment copy
        // Minecraft.getInstance().font.drawString(matrixStack, (recipe.energy.toDouble / APowerTile.MJToMicroMJ).toString + "MJ", 36 - xOff, 67 - yOff, 0x404040)
        // Minecraft.getInstance().font.drawString(matrixStack, "Keeps enchantments", 36 - xOff, 77 - yOff, 0x404040)
    }

    @Override
    public void setIngredients(WorkbenchRecipe recipe, IIngredients ingredients) {
        var input = recipe.inputs().stream()
            .map(IngredientList::stackList)
            .toList();
        var output = Collections.singletonList(recipe.getResultItem());
        ingredients.setInputLists(VanillaTypes.ITEM, input);
        ingredients.setOutputs(VanillaTypes.ITEM, output);
    }

    @Override
    public void setRecipe(IRecipeLayout recipeLayout, WorkbenchRecipe recipe, IIngredients ingredients) {
        var stacks = recipeLayout.getItemStacks();
        //7, 17 -- 7, 89
        int x0 = 3;
        final int o = 18;
        for (int i = 0; i < recipe.inputs().size(); i++) {
            int xIndex = i % 9;
            int yIndex = i / 9;
            stacks.init(i, true, x0 + o * xIndex - xOff, x0 + o * yIndex - yOff);
        }
        stacks.init(recipe.inputs().size(), false, x0 - xOff, x0 + 64 - yOff);
        stacks.set(ingredients);
    }
}
