package com.idiotss.maps.data;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.data.recipes.RecipeProvider;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.world.item.Items;

import java.util.concurrent.CompletableFuture;

public class ModdedRecipeProvider extends RecipeProvider {

    public ModdedRecipeProvider(PackOutput output, CompletableFuture<HolderLookup.Provider> registries) {
        super(output, registries);
    }

    @Override
    protected void buildRecipes(RecipeOutput recipeOutput) {
        ShapedRecipeBuilder.shaped(RecipeCategory.MISC, Items.MAP)
                .define('#', Items.PAPER)
                .pattern("###")
                .pattern("# #")
                .pattern("###")
                .unlockedBy("has_paper", has(Items.PAPER))
                .save(recipeOutput);
    }
}
