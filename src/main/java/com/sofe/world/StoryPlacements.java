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
        // a journey begins in front of the fountain, not at a random spot around it: on the fountain's dome,
        // a roof or an acacia a new Bearer was stuck
        var radius = server.getGameRules().getRule(net.minecraft.world.level.GameRules.RULE_SPAWN_RADIUS);
        if (radius.get() != 0) radius.set(0, server);
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

    /** How far round a place its build can leave things lying, and how long it goes on settling after. */
    private static final int SETTLE_MARGIN = 24, SETTLE_TICKS = 20 * 60;
    /** No one is near a place while it is built; a drop this close to a player is theirs (a tree they cut). */
    private static final double PLAYER_REACH = 8;

    private record Settling(net.minecraft.world.phys.AABB area, long until) {
    }

    private static final java.util.List<Settling> SETTLING = new java.util.ArrayList<>();

    /**
     * A new world was found strewn with things: what a build cuts through (tall grass, flowers, a lantern left with
     * nothing to hold it, sand that falls) drops on the ground. Right after a place is built what lies there is taken
     * away, and for a minute nothing more is dropped there unless a player is beside it.
     */
    private static void settle(ServerLevel level, StructurePositions.Structure structure) {
        int rx = structure.sizeX() / 2 + SETTLE_MARGIN, rz = structure.sizeZ() / 2 + SETTLE_MARGIN;
        var area = new net.minecraft.world.phys.AABB(structure.x() - rx, level.getMinBuildHeight(), structure.z() - rz,
                structure.x() + rx + 1, level.getMaxBuildHeight(), structure.z() + rz + 1);
        level.getEntitiesOfClass(net.minecraft.world.entity.item.ItemEntity.class, area).forEach(net.minecraft.world.entity.Entity::discard);
        SETTLING.add(new Settling(area, level.getGameTime() + SETTLE_TICKS));
    }

    /** A drop while a place settles: kept only beside a player. */
    public static void onEntityJoin(net.minecraftforge.event.entity.EntityJoinLevelEvent event) {
        if (SETTLING.isEmpty() || event.loadedFromDisk() || !(event.getEntity() instanceof net.minecraft.world.entity.item.ItemEntity item)
                || !(event.getLevel() instanceof ServerLevel level) || level.dimension() != net.minecraft.world.level.Level.OVERWORLD) return;
        long now = level.getGameTime();
        SETTLING.removeIf(s -> s.until() < now);
        for (Settling s : SETTLING) {
            if (s.area().contains(item.position()) && level.getNearestPlayer(item, PLAYER_REACH) == null) {
                event.setCanceled(true);
                return;
            }
        }
    }

    private static boolean near(int ax, int az, int bx, int bz, int distance) {
        long dx = ax - bx, dz = az - bz;
        return dx * dx + dz * dz <= (long) distance * distance;
    }

    /** Whether a story place has been built in this world. */
    public static boolean isBuilt(MinecraftServer server, String structureId) {
        return get(server).placed.contains("build:" + structureId);
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
            settle(level, structure);
            data.placed.add(key);
            com.sofe.world.build.RegionHealing.afterBuilt(server, structure.id()); // built after its region was liberated: heal it too
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
            // a Kinship Chest beside the Waystone at every dungeon's door (docs/Anexos.md, A5)
            boolean dungeon = layout.structure(entry.getKey()).map(st -> st.zone() == com.sofe.world.zone.ProtectedZone.Kind.DUNGEON).orElse(false);
            if (dungeon) placeBlock(level, entry.getValue().offset(2, 0, 2), SoFEBlocks.KINSHIP_CHEST.get().defaultBlockState());
        }
        for (StructurePositions.Npc npc : layout.npcs()) {
            var station = switch (npc.npc()) {
                case "kerem" -> SoFEBlocks.JEWELER.get();
                case "nilufar" -> SoFEBlocks.PURIFIER.get();
                case "dilara" -> SoFEBlocks.TEMPERING_ANVIL.get();
                default -> null;
            };
            if (station == null || !near.test(npc.x(), npc.z()) || !data.placed.add("station:" + npc.npc())) continue;
            placeBlock(level, new BlockPos(npc.x() + 2, 0, npc.z()), station.defaultBlockState());
        }
        // the rune puzzles before the seals and in the ruins (data/sofe/puzzles), in old worlds too
        for (com.sofe.puzzle.PuzzleDefinition puzzle : com.sofe.quest.StoryDataManager.puzzles().values()) {
            var at = com.sofe.puzzle.PuzzleService.origin(puzzle, layout);
            if (at.isEmpty() || !near.test(at.get()[0], at.get()[1]) || data.placed.contains("puzzle:" + puzzle.id())) continue;
            if (com.sofe.puzzle.PuzzleService.place(level, puzzle, layout)) {
                data.placed.add("puzzle:" + puzzle.id());
                count++;
            }
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

    private static void placeBlock(ServerLevel level, BlockPos column, net.minecraft.world.level.block.state.BlockState state) {
        level.setBlock(Grounding.groundFloor(level, column.getX(), column.getZ()), state, 3);
    }

    public static Optional<StoryNpcEntity> spawnNpc(ServerLevel level, StructurePositions.Npc npc) {
        BlockPos pos = Grounding.groundFloor(level, npc.x(), npc.z());
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
            case "citizen" -> {
                com.sofe.entity.npc.CitizenEntity c = EntityRegistry.CITIZEN.get().create(level);
                if (c == null) return Optional.empty();
                c.setNpcId(npc.npc());
                c.setHome(pos);
                entity = c;
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
