package com.sofe.world.zone;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sofe.SoFEMod;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.event.AddReloadListenerEvent;

import java.io.Reader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The fixed places of the story from data/sofe/structure_positions.json (docs/Mundo.md, W1 and W6):
 * where new players appear and where each story structure stands. Structures with a "zone" become
 * protected zones when a journey is created.
 */
public final class StructurePositions {
    public static final ResourceLocation FILE = SoFEMod.id("structure_positions.json");

    /** @param zone the kind of protected place, or null for structures that are only positions */
    public record Structure(String id, int x, int z, int sizeX, int sizeZ, int minY, int maxY, ProtectedZone.Kind zone) {
        public Optional<ProtectedZone> protectedZone() {
            if (zone == null) return Optional.empty();
            int halfX = sizeX / 2, halfZ = sizeZ / 2;
            return Optional.of(new ProtectedZone(id, zone, x - halfX, minY, z - halfZ, x + halfX - 1, maxY, z + halfZ - 1));
        }
    }

    /**
     * An NPC placed when a journey starts.
     *
     * @param type "story", "bearer" (npc is the class id) or "merchant" (role is its MerchantRole)
     */
    public record Npc(String npc, String type, int x, int z, float yaw, String role) {
    }

    public record Layout(int spawnX, int spawnZ, Map<String, Structure> structures, Map<String, BlockPos> waystones, List<Npc> npcs) {
        public static final Layout EMPTY = new Layout(0, 0, Map.of(), Map.of(), List.of());

        public List<ProtectedZone> zones() {
            List<ProtectedZone> zones = new ArrayList<>();
            structures.values().forEach(s -> s.protectedZone().ifPresent(zones::add));
            zones.sort(java.util.Comparator.comparing(ProtectedZone::id));
            return zones;
        }

        public Optional<Structure> structure(String id) {
            return Optional.ofNullable(structures.get(id));
        }
    }

    private static volatile Layout layout = Layout.EMPTY;

    private StructurePositions() {
    }

    public static Layout get() {
        return layout;
    }

    /** For GameTests and unit tests. */
    public static void set(Layout newLayout) {
        layout = newLayout;
    }

    public static Layout parse(JsonObject json) {
        JsonObject spawn = json.getAsJsonObject("spawn");
        Map<String, Structure> structures = new java.util.TreeMap<>();
        JsonObject list = json.getAsJsonObject("structures");
        for (Map.Entry<String, JsonElement> e : list.entrySet()) {
            JsonObject s = e.getValue().getAsJsonObject();
            ProtectedZone.Kind zone = s.has("zone") ? ProtectedZone.Kind.byId(s.get("zone").getAsString()) : null;
            structures.put(e.getKey(), new Structure(e.getKey(), s.get("x").getAsInt(), s.get("z").getAsInt(),
                    s.has("size_x") ? s.get("size_x").getAsInt() : 32, s.has("size_z") ? s.get("size_z").getAsInt() : 32,
                    s.has("min_y") ? s.get("min_y").getAsInt() : -64, s.has("max_y") ? s.get("max_y").getAsInt() : 319, zone));
        }
        Map<String, BlockPos> waystones = new java.util.TreeMap<>();
        if (json.has("waystones")) {
            for (Map.Entry<String, JsonElement> e : json.getAsJsonObject("waystones").entrySet()) {
                JsonObject w = e.getValue().getAsJsonObject();
                waystones.put(e.getKey(), new BlockPos(w.get("x").getAsInt(), 0, w.get("z").getAsInt()));
            }
        }
        List<Npc> npcs = new ArrayList<>();
        if (json.has("npcs")) {
            for (JsonElement e : json.getAsJsonArray("npcs")) {
                JsonObject n = e.getAsJsonObject();
                npcs.add(new Npc(n.get("npc").getAsString(), n.has("type") ? n.get("type").getAsString() : "story",
                        n.get("x").getAsInt(), n.get("z").getAsInt(), n.has("yaw") ? n.get("yaw").getAsFloat() : 0f,
                        n.has("role") ? n.get("role").getAsString() : null));
            }
        }
        return new Layout(spawn.get("x").getAsInt(), spawn.get("z").getAsInt(), Map.copyOf(structures), Map.copyOf(waystones), List.copyOf(npcs));
    }

    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new SimplePreparableReloadListener<Layout>() {
            @Override
            protected Layout prepare(ResourceManager resources, ProfilerFiller profiler) {
                Optional<Resource> resource = resources.getResource(FILE);
                if (resource.isEmpty()) return Layout.EMPTY;
                try (Reader reader = resource.get().openAsReader()) {
                    return parse(JsonParser.parseReader(reader).getAsJsonObject());
                } catch (Exception e) {
                    SoFEMod.LOGGER.error("Could not read {}: {}", FILE, e.getMessage());
                    return Layout.EMPTY;
                }
            }

            @Override
            protected void apply(Layout loaded, ResourceManager resources, ProfilerFiller profiler) {
                layout = loaded;
                SoFEMod.LOGGER.info("Loaded {} structure positions", loaded.structures().size());
            }
        });
    }
}
