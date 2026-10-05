package com.sofe.gametest;

import com.sofe.SoFEMod;
import com.sofe.world.gen.SoFEBiomes;
import com.sofe.world.gen.SoFEWorldPresets;
import com.sofe.world.region.AetherisBiomeSource;
import com.sofe.world.region.Region;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.biome.Climate;
import net.minecraft.world.level.dimension.LevelStem;
import net.minecraft.world.level.levelgen.presets.WorldPreset;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.Optional;

@GameTestHolder(SoFEMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class WorldPresetGameTests {

    /** The generated preset JSON loads, uses the Aetheris biome source and puts each empire where docs/Mundo.md says. */
    @GameTest(template = "empty")
    public static void journeyPresetPlacesTheEmpires(GameTestHelper helper) {
        Optional<WorldPreset> preset = helper.getLevel().registryAccess()
                .registryOrThrow(Registries.WORLD_PRESET).getOptional(SoFEWorldPresets.AETHERIS);
        if (preset.isEmpty()) {
            helper.fail("sofe:aetheris world preset is not loaded");
            return;
        }
        BiomeSource source = preset.get().overworld().map(LevelStem::generator).orElseThrow().getBiomeSource();
        if (!(source instanceof AetherisBiomeSource aetheris)) {
            helper.fail("overworld biome source is " + source.getClass().getSimpleName());
            return;
        }

        helper.assertTrue(aetheris.regionMap().regionAt(0, 0) == Region.SULTHARI, "origin is not Sulthari");
        assertBiome(helper, aetheris, 0, 0, SoFEBiomes.SULTHARI_DESERT);
        assertBiome(helper, aetheris, 0, -4500, SoFEBiomes.NORDRATH_VOLCANIC_FORGES); // the Burning Citadel
        assertBiome(helper, aetheris, 0, -2000, SoFEBiomes.NORDRATH_TUNDRA);
        assertBiome(helper, aetheris, -1800, -4200, SoFEBiomes.NORDRATH_ICE_FIELDS);
        assertBiome(helper, aetheris, 4200, -1000, SoFEBiomes.PARSIVAN_GARDENS);
        assertBiome(helper, aetheris, 3500, 4200, SoFEBiomes.KHEMET_VALLEY);
        assertBiome(helper, aetheris, -4000, 0, SoFEBiomes.AUREUM_HILLS);
        assertBiome(helper, aetheris, 0, 8000, SoFEBiomes.AETHERIS_OCEAN);         // the Southern Sea
        assertBiome(helper, aetheris, 12_200, 0, SoFEBiomes.AETHERIS_OCEAN);       // beyond the edge
        assertBiome(helper, aetheris, 9000, -5000, SoFEBiomes.PARSIVAN_GARDENS);   // the north-east is Parsivan
        assertBiome(helper, aetheris, -9000, -8000, SoFEBiomes.AUREUM_HILLS);      // the north-west is Aureum
        helper.succeed();
    }

    private static void assertBiome(GameTestHelper helper, AetherisBiomeSource source, int x, int z,
                                    net.minecraft.resources.ResourceKey<Biome> expected) {
        Holder<Biome> biome = source.getNoiseBiome(x >> 2, 16, z >> 2, Climate.empty());
        helper.assertTrue(biome.is(expected), "at " + x + ", " + z + " expected " + expected.location() + " but got " + biome.unwrapKey().map(k -> k.location().toString()).orElse("?"));
    }
}
