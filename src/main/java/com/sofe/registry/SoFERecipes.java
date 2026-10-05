package com.sofe.registry;

import com.sofe.SoFEMod;
import com.sofe.crafting.StationRecipe;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.crafting.RecipeSerializer;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** The station recipe types: the Imperial Forge, the Alembic, the Jeweler and the Purifier. */
public final class SoFERecipes {
    public static final DeferredRegister<RecipeType<?>> TYPES = DeferredRegister.create(Registries.RECIPE_TYPE, SoFEMod.MOD_ID);
    public static final DeferredRegister<RecipeSerializer<?>> SERIALIZERS = DeferredRegister.create(ForgeRegistries.RECIPE_SERIALIZERS, SoFEMod.MOD_ID);

    public static final RegistryObject<RecipeType<StationRecipe>> IMPERIAL_FORGE = TYPES.register("imperial_forge", () -> type("imperial_forge"));
    public static final RegistryObject<RecipeType<StationRecipe>> ALEMBIC = TYPES.register("alembic", () -> type("alembic"));
    public static final RegistryObject<RecipeSerializer<StationRecipe>> FORGE_SERIALIZER =
            SERIALIZERS.register("imperial_forge", () -> new StationRecipe.Serializer(StationRecipe.Kind.IMPERIAL_FORGE));
    public static final RegistryObject<RecipeSerializer<StationRecipe>> ALEMBIC_SERIALIZER =
            SERIALIZERS.register("alembic", () -> new StationRecipe.Serializer(StationRecipe.Kind.ALEMBIC));
    public static final RegistryObject<RecipeType<StationRecipe>> JEWELER = TYPES.register("jeweler", () -> type("jeweler"));
    public static final RegistryObject<RecipeType<StationRecipe>> PURIFIER = TYPES.register("purifier", () -> type("purifier"));
    public static final RegistryObject<RecipeSerializer<StationRecipe>> JEWELER_SERIALIZER =
            SERIALIZERS.register("jeweler", () -> new StationRecipe.Serializer(StationRecipe.Kind.JEWELER));
    public static final RegistryObject<RecipeSerializer<StationRecipe>> PURIFIER_SERIALIZER =
            SERIALIZERS.register("purifier", () -> new StationRecipe.Serializer(StationRecipe.Kind.PURIFIER));

    private SoFERecipes() {
    }

    private static RecipeType<StationRecipe> type(String name) {
        return new RecipeType<>() {
            @Override
            public String toString() {
                return SoFEMod.MOD_ID + ":" + name;
            }
        };
    }
}
