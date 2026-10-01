package com.sofe.world;

import com.sofe.SoFEMod;
import com.sofe.entity.npc.BearerNpcEntity;
import com.sofe.entity.npc.MerchantNpcEntity;
import com.sofe.entity.npc.MerchantRole;
import com.sofe.entity.npc.StoryNpcEntity;
import com.sofe.player.PlayerClass;
import com.sofe.registry.EntityRegistry;
import com.sofe.registry.SoFEBlocks;
import com.sofe.world.build.StructureBuilder;
import com.sofe.world.zone.StructurePositions;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.StringTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.saveddata.SavedData;
import net.minecraftforge.event.server.ServerStartedEvent;

import java.util.HashSet;
import java.util.Optional;
import java.util.Set;

/**
 * Puts the story places, people and fixed blocks from structure_positions.json in the world: Sulthari
 * (the Council, the Bearers, the merchants, the Waystones and the Personal Vault) when a journey starts,
 * and each far-away place (Nordrath's dungeons, the Burning Citadel) when a player first comes close.
 * Each is placed once; the list of what was placed is saved in the world.
 */
public final class StoryPlacements extends SavedData {
    private static final String NAME = "sofe_story_placements";
    private final Set<String> placed = new HashSet<>();

    public static StoryPlacements get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(StoryPlacements::load, StoryPlacements::new, NAME);
    }

    /** Places are built when a player comes this close, so a new journey does not generate far-away land. */
    public static final int BUILD_DISTANCE = 192;
    /** Around the world spawn everything is placed when the journey starts (the city of Sulthari). */
    public static final int START_DISTANCE = 400;
    private static final int CHECK_TICKS = 100;

    public static void onServerStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        if (!SoFEWorld.isJourney(server)) return;
        BlockPos spawn = server.overworld().getSharedSpawnPos();
        placeNear(server, StructurePositions.get(), (x, z) -> near(spawn.getX(), spawn.getZ(), x, z, START_DISTANCE));
    }

    /** Every few seconds: build what a player is now close to (Nordrath's dungeons, the Citadel...). */
    public static void onServerTick(net.minecraftforge.event.TickEvent.ServerTickEvent event) {
        if (event.phase != net.minecraftforge.event.TickEvent.Phase.END || event.getServer().getTickCount() % CHECK_TICKS != 0) return;
        MinecraftServer server = event.getServer();
        if (!SoFEWorld.isJourney(server)) return;
        var players = server.overworld().players();
        if (players.isEmpty()) return;
        placeNear(server, StructurePositions.get(), (x, z) -> players.stream()
                .anyMatch(p -> near(p.getBlockX(), p.getBlockZ(), x, z, BUILD_DISTANCE)));
    }

    private static boolean near(int ax, int az, int bx, int bz, int distance) {
        long dx = ax - bx, dz = az - bz;
        return dx * dx + dz * dz <= (long) distance * distance;
    }

    /** Places everything, wherever it is (used by GameTests and admin tools). */
    public static int placeAll(MinecraftServer server, StructurePositions.Layout layout) {
        return placeNear(server, layout, (x, z) -> true);
    }

    public static int placeNear(MinecraftServer server, StructurePositions.Layout layout, java.util.function.BiPredicate<Integer, Integer> near) {
        StoryPlacements data = get(server);
        ServerLevel level = server.overworld();
        int count = 0;
        // the buildings first, so the NPCs and Waystones stand on their floors
        java.util.List<StructurePositions.Structure> structures = new java.util.ArrayList<>(layout.structures().values());
        structures.sort(java.util.Comparator.comparing((StructurePositions.Structure st) -> !st.id().endsWith("/city"))
                .thenComparing(StructurePositions.Structure::id));
        for (StructurePositions.Structure structure : structures) {
            String key = "build:" + structure.id();
            if (data.placed.contains(key) || !near.test(structure.x(), structure.z())) continue;
            if (StructureBuilder.build(level, structure)) count++;
            data.placed.add(key);
        }
        for (StructurePositions.Npc npc : layout.npcs()) {
            String key = "npc:" + npc.npc();
            if (data.placed.contains(key) || !near.test(npc.x(), npc.z())) continue;
            if (spawnNpc(level, npc).isPresent()) {
                data.placed.add(key);
                count++;
            }
        }
        for (var entry : layout.waystones().entrySet()) {
            String key = "waystone:" + entry.getKey();
            if (data.placed.contains(key) || !near.test(entry.getValue().getX(), entry.getValue().getZ())) continue;
            placeBlock(level, entry.getValue(), SoFEBlocks.WAYSTONE.get().defaultBlockState());
            data.placed.add(key);
            count++;
        }
        layout.structure("sofe:sulthari/bank").ifPresent(bank -> {
            if (near.test(bank.x(), bank.z()) && data.placed.add("vault:sofe:sulthari/bank")) {
                placeBlock(level, new BlockPos(bank.x(), 0, bank.z()), SoFEBlocks.PERSONAL_VAULT.get().defaultBlockState());
            }
        });
        if (count > 0) {
            SoFEMod.LOGGER.info("Placed {} story buildings, NPCs and Waystones", count);
            data.setDirty();
        }
        return count;
    }

    private static BlockPos surface(ServerLevel level, int x, int z) {
        level.getChunk(x >> 4, z >> 4);
        return new BlockPos(x, level.getHeight(Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, x, z), z);
    }

    private static void placeBlock(ServerLevel level, BlockPos column, net.minecraft.world.level.block.state.BlockState state) {
        level.setBlock(surface(level, column.getX(), column.getZ()), state, 3);
    }

    public static Optional<StoryNpcEntity> spawnNpc(ServerLevel level, StructurePositions.Npc npc) {
        BlockPos pos = surface(level, npc.x(), npc.z());
        StoryNpcEntity entity;
        switch (npc.type()) {
            case "bearer" -> {
                Optional<PlayerClass> bearer = PlayerClass.byId(npc.npc());
                if (bearer.isEmpty()) return warn(npc);
                BearerNpcEntity b = EntityRegistry.BEARER_NPC.get().create(level);
                if (b == null) return Optional.empty();
                b.setBearer(bearer.get());
                entity = b;
            }
            case "merchant" -> {
                Optional<MerchantRole> role = npc.role() != null ? MerchantRole.byId(npc.role()) : MerchantRole.byNpc(npc.npc());
                if (role.isEmpty()) return warn(npc);
                MerchantNpcEntity m = EntityRegistry.MERCHANT.get().create(level);
                if (m == null) return Optional.empty();
                m.setMerchant(npc.npc(), role.get());
                entity = m;
            }
            default -> {
                StoryNpcEntity s = EntityRegistry.STORY_NPC.get().create(level);
                if (s == null) return Optional.empty();
                s.setNpcId(npc.npc());
                entity = s;
            }
        }
        entity.moveTo(pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, npc.yaw(), 0);
        entity.setYHeadRot(npc.yaw());
        entity.setYBodyRot(npc.yaw());
        entity.finalizeSpawn(level, level.getCurrentDifficultyAt(pos), MobSpawnType.STRUCTURE, null, null);
        level.addFreshEntity(entity);
        return Optional.of(entity);
    }

    private static Optional<StoryNpcEntity> warn(StructurePositions.Npc npc) {
        SoFEMod.LOGGER.warn("structure_positions.json: cannot place NPC {} of type {}", npc.npc(), npc.type());
        return Optional.empty();
    }

    @Override
    public CompoundTag save(CompoundTag tag) {
        ListTag list = new ListTag();
        placed.stream().sorted().forEach(p -> list.add(StringTag.valueOf(p)));
        tag.put("placed", list);
        return tag;
    }

    private static StoryPlacements load(CompoundTag tag) {
        StoryPlacements data = new StoryPlacements();
        ListTag list = tag.getList("placed", Tag.TAG_STRING);
        for (int i = 0; i < list.size(); i++) data.placed.add(list.getString(i));
        return data;
    }
}
