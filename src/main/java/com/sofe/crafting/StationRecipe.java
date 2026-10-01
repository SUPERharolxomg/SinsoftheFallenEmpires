package com.sofe.crafting;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sofe.registry.SoFERecipes;
import net.minecraft.core.RegistryAccess;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.crafting.Recipe;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.level.Level;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;

/**
 * A recipe of a SoFE station (docs/Pociones.md, "Forges and crafting"), a custom recipe type: the
 * Imperial Forge (needs its Blueprint, makes Tempered gear of an item level) or the Alembic (potions).
 * Ingredients are taken from the player's inventory; the station screen lists the recipes.
 */
public record StationRecipe(ResourceLocation id, Kind kind, List<Ingredient> ingredients, Item result, int count,
                            String blueprint, int itemLevel) implements Recipe<Container> {

    public enum Kind { IMPERIAL_FORGE, ALEMBIC }

    public record Ingredient(Item item, int count) {
    }

    public StationRecipe {
        ingredients = List.copyOf(ingredients);
    }

    /** Whether the inventory holds every ingredient. */
    public boolean canMake(Inventory inventory) {
        for (Ingredient ingredient : ingredients) {
            if (inventory.countItem(ingredient.item()) < ingredient.count()) return false;
        }
        return true;
    }

    /** Takes the ingredients; call after {@link #canMake}. */
    public void consume(Inventory inventory) {
        for (Ingredient ingredient : ingredients) {
            int left = ingredient.count();
            for (int i = 0; i < inventory.getContainerSize() && left > 0; i++) {
                ItemStack stack = inventory.getItem(i);
                if (!stack.is(ingredient.item())) continue;
                int take = Math.min(left, stack.getCount());
                stack.shrink(take);
                left -= take;
            }
        }
        inventory.setChanged();
    }

    @Override
    public boolean matches(Container container, Level level) {
        return false; // made from the station screen, never from a crafting grid
    }

    @Override
    public ItemStack assemble(Container container, RegistryAccess access) {
        return new ItemStack(result, count);
    }

    @Override
    public boolean canCraftInDimensions(int width, int height) {
        return false;
    }

    @Override
    public ItemStack getResultItem(RegistryAccess access) {
        return new ItemStack(result, count);
    }

    @Override
    public ResourceLocation getId() {
        return id;
    }

    @Override
    public RecipeSerializer<?> getSerializer() {
        return kind == Kind.IMPERIAL_FORGE ? SoFERecipes.FORGE_SERIALIZER.get() : SoFERecipes.ALEMBIC_SERIALIZER.get();
    }

    @Override
    public RecipeType<?> getType() {
        return kind == Kind.IMPERIAL_FORGE ? SoFERecipes.IMPERIAL_FORGE.get() : SoFERecipes.ALEMBIC.get();
    }

    @Override
    public boolean isSpecial() {
        return true; // keeps it out of the recipe book
    }

    /** Reads and writes both kinds; the kind is fixed per serializer. */
    public record Serializer(Kind kind) implements RecipeSerializer<StationRecipe> {
        @Override
        public StationRecipe fromJson(ResourceLocation id, JsonObject json) {
            List<Ingredient> ingredients = new ArrayList<>();
            for (JsonElement e : json.getAsJsonArray("ingredients")) {
                JsonObject o = e.getAsJsonObject();
                ingredients.add(new Ingredient(item(o.get("item").getAsString()), o.has("count") ? o.get("count").getAsInt() : 1));
            }
            return new StationRecipe(id, kind, ingredients, item(json.get("result").getAsString()),
                    json.has("count") ? json.get("count").getAsInt() : 1,
                    json.has("blueprint") ? json.get("blueprint").getAsString() : "",
                    json.has("item_level") ? json.get("item_level").getAsInt() : 1);
        }

        private static Item item(String id) {
            Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(id));
            if (item == null || item == net.minecraft.world.item.Items.AIR) throw new IllegalArgumentException("unknown item " + id);
            return item;
        }

        @Override
        public StationRecipe fromNetwork(ResourceLocation id, FriendlyByteBuf buf) {
            List<Ingredient> ingredients = buf.readList(b -> new Ingredient(b.readById(net.minecraft.core.registries.BuiltInRegistries.ITEM), b.readVarInt()));
            return new StationRecipe(id, kind, ingredients, buf.readById(net.minecraft.core.registries.BuiltInRegistries.ITEM), buf.readVarInt(),
                    buf.readUtf(), buf.readVarInt());
        }

        @Override
        public void toNetwork(FriendlyByteBuf buf, StationRecipe recipe) {
            buf.writeCollection(recipe.ingredients(), (b, i) -> {
                b.writeId(net.minecraft.core.registries.BuiltInRegistries.ITEM, i.item());
                b.writeVarInt(i.count());
            });
            buf.writeId(net.minecraft.core.registries.BuiltInRegistries.ITEM, recipe.result());
            buf.writeVarInt(recipe.count());
            buf.writeUtf(recipe.blueprint());
            buf.writeVarInt(recipe.itemLevel());
        }
    }
}
