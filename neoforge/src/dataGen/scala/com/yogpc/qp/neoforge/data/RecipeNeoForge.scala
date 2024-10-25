package com.yogpc.qp.neoforge.data

import com.yogpc.qp.data.Recipe
import net.minecraft.core.HolderLookup
import net.minecraft.core.registries.Registries
import net.minecraft.data.PackOutput
import net.minecraft.data.recipes.{RecipeOutput, RecipeProvider}

import java.util.concurrent.CompletableFuture

class RecipeNeoForge(output: PackOutput, registries: CompletableFuture[HolderLookup.Provider]) extends RecipeProvider.Runner(output, registries) {

  override def createRecipeProvider(provider: HolderLookup.Provider, recipeOutput: RecipeOutput): RecipeProvider = {
    val ingredientProvider = IngredientProviderNeoForge(provider.lookupOrThrow(Registries.ITEM))

    given HolderLookup.Provider = provider

    given RecipeOutput = recipeOutput

    new Recipe(ingredientProvider)
  }

  override def getName: String = getClass.getSimpleName
}
