package com.sofe.condition;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.sofe.SoFEMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.event.AddReloadListenerEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

/**
 * Loads every condition in data/&lt;namespace&gt;/conditions/*.json on server start and on /reload.
 * A broken file is logged and skipped, so one typo does not stop the server.
 */
public final class ConditionManager extends SimpleJsonResourceReloadListener {
    private static final Gson GSON = new Gson();
    private static volatile Map<ResourceLocation, Condition> conditions = Map.of();

    private ConditionManager() {
        super(GSON, "conditions");
    }

    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new ConditionManager());
    }

    public static Optional<Condition> get(ResourceLocation id) {
        return Optional.ofNullable(conditions.get(id));
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager resources, ProfilerFiller profiler) {
        Map<ResourceLocation, Condition> loaded = new HashMap<>();
        files.forEach((id, json) -> {
            try {
                loaded.put(id, ConditionParser.parse(json));
            } catch (IllegalArgumentException e) {
                SoFEMod.LOGGER.error("Skipping condition {}: {}", id, e.getMessage());
            }
        });
        conditions = Map.copyOf(loaded);
        SoFEMod.LOGGER.info("Loaded {} conditions", loaded.size());
    }
}
