package com.sofe.gametest;

import com.mojang.serialization.JsonOps;
import com.sofe.SoFEMod;
import com.sofe.entity.npc.StoryNpcEntity;
import com.sofe.registry.EntityRegistry;
import com.sofe.registry.SoFEBlocks;
import com.sofe.world.StoryPlacements;
import com.sofe.world.build.StructureBuilder;
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
        helper.assertTrue(StructureBuilder.build(level, bank), "the bank has no blockout");
        helper.assertFalse(StructureBuilder.build(level, new StructurePositions.Structure("sofe:nowhere/unknown", base.getX(), base.getZ(), 4, 4, -64, 319, null)),
                "an unknown structure should not be built");

        // the GameTest world is kept between runs, so every run uses names of its own
        String tag = "gt" + Long.toHexString(System.nanoTime());
        StructurePositions.Layout layout = new StructurePositions.Layout(base.getX(), base.getZ(), Map.of(),
                Map.of("sofe:test/" + tag, new BlockPos(base.getX() + 6, 0, base.getZ())),
                List.of(new StructurePositions.Npc("test_" + tag, "story", base.getX() - 6, base.getZ(), 0, null)));
        StoryPlacements.placeAll(level.getServer(), layout);
        var npcs = level.getEntitiesOfClass(StoryNpcEntity.class, new AABB(base).inflate(16, 400, 16), n -> ("test_" + tag).equals(n.npcId()));
        helper.assertFalse(npcs.isEmpty(), "the NPC was not placed");
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

    /** The King once stood on the palace dome: people go to the ground floor, on its carpet, never on a gallery or a roof. */
    @GameTest(template = "empty", timeoutTicks = 100)
    public static void peopleStandOnTheGroundFloorNotOnTheRoof(GameTestHelper helper) {
        var level = helper.getLevel();
        // built on the open surface of the test's column, as the palace stands on the land
        BlockPos column = helper.absolutePos(new BlockPos(4, 2, 4));
        level.getChunk(column.getX() >> 4, column.getZ() >> 4);
        int surface = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, column.getX(), column.getZ());
        BlockPos hall = new BlockPos(column.getX(), surface + 1, column.getZ());
        var bricks = net.minecraft.world.level.block.Blocks.STONE_BRICKS.defaultBlockState();
        for (int dx = -2; dx <= 2; dx++) {
            for (int dz = -2; dz <= 2; dz++) {
                for (int dy = -4; dy <= -2; dy++) level.setBlock(hall.offset(dx, dy, dz), net.minecraft.world.level.block.Blocks.STONE.defaultBlockState(), 3); // the land under it (the test world keeps what earlier runs dug)
                level.setBlock(hall.offset(dx, -1, dz), bricks, 3);
                level.setBlock(hall.offset(dx, 0, dz), net.minecraft.world.level.block.Blocks.RED_CARPET.defaultBlockState(), 3);
                level.setBlock(hall.offset(dx, 5, dz), bricks, 3); // a gallery no stairs reach
                level.setBlock(hall.offset(dx, 10, dz), bricks, 3); // the roof
            }
        }
        BlockPos ground = com.sofe.world.Grounding.groundFloor(level, hall.getX(), hall.getZ());
        helper.assertTrue(ground.getY() == hall.getY(), "the ground floor is at " + hall.getY() + ", found " + ground.getY());
        String tag = "gt" + Long.toHexString(System.nanoTime());
        var npc = StoryPlacements.spawnNpc(level, new StructurePositions.Npc("test_" + tag, "story", hall.getX(), hall.getZ(), 180, null));
        helper.assertTrue(npc.isPresent(), "the NPC was not placed");
        helper.assertTrue(npc.get().getBlockY() == hall.getY(), "the NPC stands at y " + npc.get().getBlockY() + ", not in the hall");
        npc.get().discard();
        for (int dx = -2; dx <= 2; dx++) { // the land as it was
            for (int dz = -2; dz <= 2; dz++) {
                for (int dy = -1; dy <= 10; dy++) level.setBlock(hall.offset(dx, dy, dz), net.minecraft.world.level.block.Blocks.AIR.defaultBlockState(), 3);
            }
        }
        helper.succeed();
    }

    /** A beta tester saw every lantern of the first town fall: each lantern a build sets must hold on its own. */
    @GameTest(template = "empty", timeoutTicks = 400)
    public static void everyLanternOfTheHousesHolds(GameTestHelper helper) {
        var level = helper.getLevel();
        BlockPos base = helper.absolutePos(new BlockPos(8, 1, 8)).offset(400, 0, 0);
        var district = new StructurePositions.Structure("sofe:sulthari/lower_district", base.getX(), base.getZ(), 40, 40, -64, 319, null);
        helper.assertTrue(StructureBuilder.build(level, district), "the district has no blockout");
        int lanterns = 0;
        for (int x = base.getX() - 20; x <= base.getX() + 20; x++) {
            for (int z = base.getZ() - 20; z <= base.getZ() + 20; z++) {
                int top = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, x, z);
                for (int y = top - 20; y <= top; y++) {
                    BlockPos pos = new BlockPos(x, y, z);
                    var state = level.getBlockState(pos);
                    if (!(state.getBlock() instanceof net.minecraft.world.level.block.LanternBlock)) continue;
                    lanterns++;
                    helper.assertTrue(state.canSurvive(level, pos), "a lantern hangs from nothing at " + pos.toShortString());
                }
            }
        }
        level.getEntitiesOfClass(StoryNpcEntity.class, new AABB(base).inflate(48, 200, 48)).forEach(n -> n.discard()); // its townsfolk
        helper.assertTrue(lanterns > 0, "no lanterns were built to check");
        helper.succeed();
    }
}
