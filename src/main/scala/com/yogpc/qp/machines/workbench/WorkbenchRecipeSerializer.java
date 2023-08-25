package com.yogpc.qp.machines.workbench;

import com.google.gson.JsonObject;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.GsonHelper;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraftforge.common.crafting.conditions.ICondition;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Map;
import java.util.Objects;

public class WorkbenchRecipeSerializer implements RecipeSerializer<WorkbenchRecipe> {
    private final Map<String, PacketSerialize<? extends WorkbenchRecipe>> serializeMap;

    WorkbenchRecipeSerializer() {
        serializeMap = Map.of(
            "default", new IngredientRecipeSerialize()
        );
    }

    @Override
    @Deprecated
    public WorkbenchRecipe fromJson(ResourceLocation id, JsonObject jsonObject) {
        return fromJson(id, jsonObject, ICondition.IContext.EMPTY);
    }

    @Override
    public WorkbenchRecipe fromJson(ResourceLocation id, JsonObject recipeJson, ICondition.IContext context) {
        var subType = GsonHelper.getAsString(recipeJson, "subType", "default");
        return serializeMap.get(subType).fromJson(id, recipeJson, context);
    }

    public JsonObject toJson(WorkbenchRecipe recipe, JsonObject o) {
        o.addProperty("subType", recipe.getSubTypeName());
        return toJson(o, serializeMap.get(recipe.getSubTypeName()), recipe);
    }

    @SuppressWarnings("unchecked")
    private static <T extends WorkbenchRecipe> JsonObject toJson(JsonObject object, PacketSerialize<T> serialize, WorkbenchRecipe recipe) {
        return serialize.toJson(object, (T) recipe);
    }

    @Override
    public WorkbenchRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buffer) {
        var subType = buffer.readUtf();
        return serializeMap.get(subType).fromPacket(id, buffer);
    }

    @Override
    public void toNetwork(FriendlyByteBuf buffer, WorkbenchRecipe recipe) {
        buffer.writeUtf(recipe.getSubTypeName());
        toNetwork(serializeMap.get(recipe.getSubTypeName()), buffer, recipe);
    }

    @SuppressWarnings("unchecked")
    private static <T extends WorkbenchRecipe> void toNetwork(PacketSerialize<T> serialize, FriendlyByteBuf buffer, WorkbenchRecipe recipe) {
        serialize.toPacket(buffer, (T) recipe);
    }

    interface PacketSerialize<T extends WorkbenchRecipe> {
        T fromJson(ResourceLocation id, JsonObject jsonObject, ICondition.IContext context);

        JsonObject toJson(JsonObject jsonObject, T recipe);

        T fromPacket(ResourceLocation id, FriendlyByteBuf buffer);

        void toPacket(FriendlyByteBuf buffer, T recipe);

        static JsonObject toJson(ItemStack stack) {
            var o = new JsonObject();
            o.addProperty("item", Objects.requireNonNull(ForgeRegistries.ITEMS.getKey(stack.getItem())).toString());
            o.addProperty("count", stack.getCount());
            if (stack.getTag() != null)
                o.addProperty("nbt", stack.getTag().toString());
            return o;
        }
    }
}
