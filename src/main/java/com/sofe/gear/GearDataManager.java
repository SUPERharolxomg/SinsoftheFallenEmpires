package com.sofe.gear;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sofe.SoFEMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.event.AddReloadListenerEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Loads data/&lt;ns&gt;/affixes, gear_bases and relics on start and on /reload. A broken file is logged
 * and skipped, so a typo in one affix does not stop the server.
 */
public final class GearDataManager {
    /** A Relic: its item, item level and fixed affixes (docs/Pociones.md, "Relic"). */
    public record Relic(String id, String item, int itemLevel, List<GearData.Roll> affixes) {
    }

    private static volatile List<Affix> affixes = List.of();
    private static volatile List<GearBase> bases = List.of();
    private static volatile Map<String, Relic> relics = Map.of();

    private GearDataManager() {
    }

    public static List<Affix> affixes() {
        return affixes;
    }

    public static List<GearBase> bases() {
        return bases;
    }

    public static Optional<Relic> relic(String id) {
        return Optional.ofNullable(relics.get(id));
    }

    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new SimpleJsonResourceReloadListener(new Gson(), "affixes") {
            @Override
            protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager resources, ProfilerFiller profiler) {
                List<Affix> loaded = new ArrayList<>();
                files.forEach((id, json) -> {
                    try {
                        loaded.add(Affix.parse(id.toString(), json.getAsJsonObject()));
                    } catch (RuntimeException e) {
                        SoFEMod.LOGGER.error("Skipping affix {}: {}", id, e.getMessage());
                    }
                });
                affixes = List.copyOf(loaded);
                SoFEMod.LOGGER.info("Loaded {} affixes", loaded.size());
            }
        });
        event.addListener(new SimpleJsonResourceReloadListener(new Gson(), "gear_bases") {
            @Override
            protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager resources, ProfilerFiller profiler) {
                List<GearBase> loaded = new ArrayList<>();
                files.forEach((id, json) -> {
                    try {
                        loaded.add(GearBase.parse(json.getAsJsonObject()));
                    } catch (RuntimeException e) {
                        SoFEMod.LOGGER.error("Skipping gear base {}: {}", id, e.getMessage());
                    }
                });
                bases = List.copyOf(loaded);
            }
        });
        event.addListener(new SimpleJsonResourceReloadListener(new Gson(), "relics") {
            @Override
            protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager resources, ProfilerFiller profiler) {
                Map<String, Relic> loaded = new HashMap<>();
                files.forEach((id, json) -> {
                    try {
                        JsonObject o = json.getAsJsonObject();
                        List<GearData.Roll> rolls = new ArrayList<>();
                        for (JsonElement e : o.getAsJsonArray("affixes")) {
                            JsonObject a = e.getAsJsonObject();
                            rolls.add(new GearData.Roll("relic:" + id.getPath(), GearStat.byId(a.get("stat").getAsString()),
                                    a.has("parameter") ? a.get("parameter").getAsString() : null, a.get("value").getAsInt()));
                        }
                        loaded.put(id.getPath(), new Relic(id.getPath(), o.get("item").getAsString(), o.get("item_level").getAsInt(), rolls));
                    } catch (RuntimeException e) {
                        SoFEMod.LOGGER.error("Skipping relic {}: {}", id, e.getMessage());
                    }
                });
                relics = Map.copyOf(loaded);
            }
        });
    }

    /** For GameTests and the unit-test style checks in game. */
    public static void setForTest(List<Affix> testAffixes, List<GearBase> testBases) {
        affixes = List.copyOf(testAffixes);
        bases = List.copyOf(testBases);
    }
}
