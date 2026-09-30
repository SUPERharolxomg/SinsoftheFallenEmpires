package com.sofe.progression;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sofe.SoFEMod;
import com.sofe.mob.MobScalingRules;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.Resource;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.event.AddReloadListenerEvent;

import java.io.Reader;
import java.util.Optional;
import java.util.function.Function;

/**
 * Loads data/sofe/leveling.json and data/sofe/mob_scaling.json on start and on /reload.
 * A missing or broken file keeps the documented defaults and logs why.
 */
public final class ProgressionRulesManager extends SimplePreparableReloadListener<ProgressionRulesManager.Loaded> {
    private static final ResourceLocation LEVELING = SoFEMod.id("leveling.json");
    private static final ResourceLocation MOB_SCALING = SoFEMod.id("mob_scaling.json");

    private static volatile LevelingRules leveling = LevelingRules.DEFAULT;
    private static volatile MobScalingRules mobScaling = MobScalingRules.defaults();

    record Loaded(LevelingRules leveling, MobScalingRules mobScaling) {
    }

    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new ProgressionRulesManager());
    }

    public static LevelingRules leveling() {
        return leveling;
    }

    public static MobScalingRules mobScaling() {
        return mobScaling;
    }

    @Override
    protected Loaded prepare(ResourceManager resources, ProfilerFiller profiler) {
        return new Loaded(
                read(resources, LEVELING, LevelingRules::parse).orElse(LevelingRules.DEFAULT),
                read(resources, MOB_SCALING, MobScalingRules::parse).orElse(MobScalingRules.defaults()));
    }

    @Override
    protected void apply(Loaded loaded, ResourceManager resources, ProfilerFiller profiler) {
        leveling = loaded.leveling();
        mobScaling = loaded.mobScaling();
    }

    private static <T> Optional<T> read(ResourceManager resources, ResourceLocation id, Function<JsonObject, T> parser) {
        Optional<Resource> resource = resources.getResource(id);
        if (resource.isEmpty()) {
            SoFEMod.LOGGER.warn("{} not found, using the defaults", id);
            return Optional.empty();
        }
        try (Reader reader = resource.get().openAsReader()) {
            return Optional.of(parser.apply(JsonParser.parseReader(reader).getAsJsonObject()));
        } catch (Exception e) {
            SoFEMod.LOGGER.error("Could not read {}, using the defaults: {}", id, e.getMessage());
            return Optional.empty();
        }
    }
}
