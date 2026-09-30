package com.sofe.world;

import com.sofe.SoFEMod;
import com.sofe.entity.npc.BearerNpcEntity;
import com.sofe.entity.npc.MerchantNpcEntity;
import com.sofe.entity.npc.MerchantRole;
import com.sofe.entity.npc.StoryNpcEntity;
import com.sofe.player.PlayerClass;
import com.sofe.registry.EntityRegistry;
import com.sofe.registry.SoFEBlocks;
import com.sofe.world.build.SultharisBuilder;
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
 * Puts the people and fixed blocks of Sulthari in place the first time a journey starts: the
 * Council, the Bearers, the merchants, the Waystones and the Personal Vault of the bank, from
 * structure_positions.json. Each is placed once; the list of what was placed is saved in the world.
 */
public final class StoryPlacements extends SavedData {
    private static final String NAME = "sofe_story_placements";
    private final Set<String> placed = new HashSet<>();

    public static StoryPlacements get(MinecraftServer server) {
        return server.overworld().getDataStorage().computeIfAbsent(StoryPlacements::load, StoryPlacements::new, NAME);
    }

    public static void onServerStarted(ServerStartedEvent event) {
        if (SoFEWorld.isJourney(event.getServer())) placeAll(event.getServer(), StructurePositions.get());
    }

    public static int placeAll(MinecraftServer server, StructurePositions.Layout layout) {
        StoryPlacements data = get(server);
        ServerLevel level = server.overworld();
        int count = 0;
        // the buildings first, so the NPCs and Waystones stand on their floors
        java.util.List<StructurePositions.Structure> structures = new java.util.ArrayList<>(layout.structures().values());
        structures.sort(java.util.Comparator.comparing((StructurePositions.Structure st) -> !st.id().endsWith("/city"))
                .thenComparing(StructurePositions.Structure::id));
        for (StructurePositions.Structure structure : structures) {
            String key = "build:" + structure.id();
            if (data.placed.contains(key)) continue;
            if (SultharisBuilder.build(level, structure)) count++;
            data.placed.add(key);
        }
        for (StructurePositions.Npc npc : layout.npcs()) {
            String key = "npc:" + npc.npc();
            if (data.placed.contains(key)) continue;
            if (spawnNpc(level, npc).isPresent()) {
                data.placed.add(key);
                count++;
            }
        }
        for (var entry : layout.waystones().entrySet()) {
            String key = "waystone:" + entry.getKey();
            if (data.placed.contains(key)) continue;
            placeBlock(level, entry.getValue(), SoFEBlocks.WAYSTONE.get().defaultBlockState());
            data.placed.add(key);
            count++;
        }
        layout.structure("sofe:sulthari/bank").ifPresent(bank -> {
            if (data.placed.add("vault:sofe:sulthari/bank")) {
                placeBlock(level, new BlockPos(bank.x(), 0, bank.z()), SoFEBlocks.PERSONAL_VAULT.get().defaultBlockState());
            }
        });
        if (count > 0) SoFEMod.LOGGER.info("Placed {} buildings, story NPCs and Waystones in Sulthari", count);
        data.setDirty();
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
