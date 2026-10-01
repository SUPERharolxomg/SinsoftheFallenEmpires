package com.sofe.crafting;

import com.sofe.economy.EconomyCapability;
import com.sofe.gear.GearMaker;
import com.sofe.gear.Rarity;
import com.sofe.registry.SoFERecipes;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Making things at a station (UC-11, UC-14): the server checks that the player still stands at the
 * station they opened, knows the Blueprint (Imperial Forge) and has every ingredient.
 */
public final class StationService {
    private record Opened(BlockPos pos, StationRecipe.Kind kind) {
    }

    private static final Map<UUID, Opened> OPENED = new ConcurrentHashMap<>();
    private static final double REACH = 6;

    private StationService() {
    }

    static void opened(ServerPlayer player, BlockPos pos, StationRecipe.Kind kind) {
        OPENED.put(player.getUUID(), new Opened(pos.immutable(), kind));
    }

    public static Optional<StationRecipe> recipe(ServerPlayer player, ResourceLocation id) {
        return player.server.getRecipeManager().byKey(id).filter(r -> r instanceof StationRecipe).map(r -> (StationRecipe) r);
    }

    public enum Result { MADE, NOT_AT_STATION, UNKNOWN_BLUEPRINT, MISSING }

    public static Result make(ServerPlayer player, ResourceLocation recipeId) {
        Opened opened = OPENED.get(player.getUUID());
        Optional<StationRecipe> recipe = recipe(player, recipeId);
        if (opened == null || recipe.isEmpty() || recipe.get().kind() != opened.kind()
                || opened.pos().distToCenterSqr(player.position()) > REACH * REACH
                || !(player.level().getBlockState(opened.pos()).getBlock() instanceof StationBlock)) {
            return Result.NOT_AT_STATION;
        }
        StationRecipe r = recipe.get();
        if (r.kind() == StationRecipe.Kind.IMPERIAL_FORGE && !EconomyCapability.get(player).map(e -> e.knows(r.blueprint())).orElse(false)) {
            return Result.UNKNOWN_BLUEPRINT;
        }
        if (!r.canMake(player.getInventory())) {
            player.displayClientMessage(Component.translatable("message.sofe.station.missing").withStyle(ChatFormatting.RED), true);
            return Result.MISSING;
        }
        r.consume(player.getInventory());
        ItemStack made = r.kind() == StationRecipe.Kind.IMPERIAL_FORGE
                // the Forge always makes Tempered gear or better: one guaranteed affix
                ? GearMaker.rollItem(net.minecraftforge.registries.ForgeRegistries.ITEMS.getKey(r.result()).toString(),
                        Math.max(r.itemLevel(), GearMaker.levelOf(player)), Rarity.TEMPERED, player, player.getRandom())
                        .orElse(new ItemStack(r.result(), r.count()))
                : new ItemStack(r.result(), r.count());
        Component name = made.getHoverName();
        if (!player.getInventory().add(made)) player.drop(made, false);
        player.level().playSound(null, opened.pos(), r.kind() == StationRecipe.Kind.IMPERIAL_FORGE ? SoundEvents.ANVIL_USE : SoundEvents.BREWING_STAND_BREW,
                SoundSource.BLOCKS, 0.8f, 1f);
        player.displayClientMessage(Component.translatable("message.sofe.station.made", name).withStyle(ChatFormatting.GOLD), true);
        return Result.MADE;
    }

    public static java.util.List<StationRecipe> all(net.minecraft.world.item.crafting.RecipeManager recipes, StationRecipe.Kind kind) {
        return java.util.List.copyOf(recipes.getAllRecipesFor(kind == StationRecipe.Kind.IMPERIAL_FORGE
                ? SoFERecipes.IMPERIAL_FORGE.get() : SoFERecipes.ALEMBIC.get()));
    }

    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        OPENED.remove(event.getEntity().getUUID());
    }
}
