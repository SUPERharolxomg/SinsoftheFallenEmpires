package com.sofe.gametest;

import com.mojang.serialization.JsonOps;
import com.sofe.SoFEMod;
import com.sofe.entity.npc.StoryNpcEntity;
import com.sofe.registry.EntityRegistry;
import com.sofe.registry.SoFEBlocks;
import com.sofe.world.StoryPlacements;
import com.sofe.world.build.SultharisBuilder;
import com.sofe.world.gen.SoFEBiomes;
import com.sofe.world.gen.SoFEWorldPresets;
import com.sofe.world.region.AetherisBiomeSource;
import com.sofe.world.region.Region;
import com.sofe.world.region.RegionMap;
import com.sofe.world.zone.StructurePositions;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.registries.Registries;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.resources.RegistryOps;
import net.minecraft.world.level.biome.Biome;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/** The pieces a new journey runs at creation, checked on the GameTest server (which is not a journey itself). */
@GameTestHolder(SoFEMod.MOD_ID)
@PrefixGameTestTemplate(false)
public class JourneyWorldGameTests {

    @GameTest(template = "empty")
    public static void theLayoutAndTheAshenWastesAreSavedWithTheWorld(GameTestHelper helper) {
        var biomes = helper.getLevel().registryAccess().registryOrThrow(Registries.BIOME);
        Map<Region, Holder<Biome>> byRegion = new EnumMap<>(Region.class);
        SoFEBiomes.BY_REGION.forEach((region, key) -> byRegion.put(region, biomes.getHolderOrThrow(key)));
        Holder<Biome> wastes = biomes.getHolderOrThrow(SoFEBiomes.ASHEN_WASTES);
        AetherisBiomeSource source = new AetherisBiomeSource(RegionMap.defaultLayout(), byRegion,
                Optional.of(new AetherisBiomeSource.Wastes(wastes, SoFEWorldPresets.ASHEN_WASTES_INNER_RADIUS)));

        RegistryOps<com.google.gson.JsonElement> ops = RegistryOps.create(JsonOps.INSTANCE, helper.getLevel().registryAccess());
        var json = AetherisBiomeSource.CODEC.encodeStart(ops, source).getOrThrow(false, SoFEMod.LOGGER::error);
        AetherisBiomeSource read = AetherisBiomeSource.CODEC.parse(ops, json).getOrThrow(false, SoFEMod.LOGGER::error);
        helper.assertTrue(read.regionMap().regionAt(0, -4500) == Region.NORDRATH, "the layout was not read back");
        helper.assertTrue(read.getNoiseBiome(1400 >> 2, 64 >> 2, 0, null).is(SoFEBiomes.ASHEN_WASTES), "the edge of Sulthari should be Ashen Wastes");
        helper.assertTrue(read.getNoiseBiome(0, 64 >> 2, 0, null).is(SoFEBiomes.SULTHARI_DESERT), "the city should be desert");

        // a world saved before the Wastes existed keeps loading, without them
        json.getAsJsonObject().remove("ashen_wastes");
        AetherisBiomeSource old = AetherisBiomeSource.CODEC.parse(ops, json).getOrThrow(false, SoFEMod.LOGGER::error);
        helper.assertTrue(old.getNoiseBiome(1400 >> 2, 64 >> 2, 0, null).is(SoFEBiomes.SULTHARI_DESERT), "old worlds changed their biomes");
        helper.succeed();
    }

    @GameTest(template = "empty", timeoutTicks = 200)
    public static void theBuilderRaisesTheBlockoutsAndThePeopleAreThere(GameTestHelper helper) {
        BlockPos base = helper.absolutePos(new BlockPos(8, 1, 8));
        var level = helper.getLevel();
        var bank = new StructurePositions.Structure("sofe:sulthari/bank", base.getX(), base.getZ(), 9, 9, -64, 319, null);
        helper.assertTrue(SultharisBuilder.build(level, bank), "the bank has no blockout");
        helper.assertFalse(SultharisBuilder.build(level, new StructurePositions.Structure("sofe:nowhere/unknown", base.getX(), base.getZ(), 4, 4, -64, 319, null)),
                "an unknown structure should not be built");

        String tag = "gt" + Math.abs(base.asLong() % 100000);
        StructurePositions.Layout layout = new StructurePositions.Layout(base.getX(), base.getZ(), Map.of(),
                Map.of("sofe:test/" + tag, new BlockPos(base.getX() + 6, 0, base.getZ())),
                List.of(new StructurePositions.Npc("ozhan", "story", base.getX() - 6, base.getZ(), 0, null)));
        StoryPlacements.placeAll(level.getServer(), layout);
        var npcs = level.getEntitiesOfClass(StoryNpcEntity.class, new AABB(base).inflate(16, 400, 16), n -> "ozhan".equals(n.npcId()));
        helper.assertFalse(npcs.isEmpty(), "Ozhan was not placed");
        // placed on the surface of that column, wherever the ground is
        boolean waystone = false;
        for (int y = level.getMinBuildHeight(); y < level.getMaxBuildHeight() && !waystone; y++) {
            waystone = level.getBlockState(new BlockPos(base.getX() + 6, y, base.getZ())).is(SoFEBlocks.WAYSTONE.get());
        }
        helper.assertTrue(waystone, "the Waystone was not placed");
        int placedAgain = StoryPlacements.placeAll(level.getServer(), layout);
        helper.assertTrue(placedAgain == 0, "placing twice should do nothing, placed " + placedAgain);
        npcs.forEach(n -> n.discard());
        helper.succeed();
    }
}
