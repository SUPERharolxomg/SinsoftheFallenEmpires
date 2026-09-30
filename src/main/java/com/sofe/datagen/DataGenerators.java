package com.sofe.datagen;

import net.minecraft.core.HolderLookup;
import net.minecraft.data.DataGenerator;
import net.minecraft.data.PackOutput;
import net.minecraft.data.loot.LootTableProvider;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraftforge.common.data.ExistingFileHelper;
import net.minecraftforge.data.event.GatherDataEvent;

import java.util.List;
import java.util.Set;
import java.util.concurrent.CompletableFuture;

/**
 * Entry point for ./gradlew runData. Output goes to src/generated/resources and is committed,
 * so a normal build never needs to run the generators.
 */
public final class DataGenerators {

    private DataGenerators() {
    }

    public static void gatherData(GatherDataEvent event) {
        DataGenerator generator = event.getGenerator();
        PackOutput output = generator.getPackOutput();
        ExistingFileHelper files = event.getExistingFileHelper();
        CompletableFuture<HolderLookup.Provider> lookup = event.getLookupProvider();

        generator.addProvider(event.includeClient(), new SoFEBlockStateProvider(output, files));
        generator.addProvider(event.includeClient(), new SoFEItemModelProvider(output, files));

        generator.addProvider(event.includeServer(), new LootTableProvider(output, Set.of(),
                List.of(new LootTableProvider.SubProviderEntry(SoFEBlockLootTables::new, LootContextParamSets.BLOCK))));
        SoFEBlockTagsProvider blockTags = generator.addProvider(event.includeServer(), new SoFEBlockTagsProvider(output, lookup, files));
        generator.addProvider(event.includeServer(), new SoFEItemTagsProvider(output, lookup, blockTags.contentsGetter(), files));

        // Biome tags need the SoFE biomes, so they read the lookup that includes the worldgen entries
        SoFEWorldgenProvider worldgen = generator.addProvider(event.includeServer(), new SoFEWorldgenProvider(output, lookup));
        generator.addProvider(event.includeServer(), new SoFEBiomeTagsProvider(output, worldgen.getRegistryProvider(), files));
    }
}
