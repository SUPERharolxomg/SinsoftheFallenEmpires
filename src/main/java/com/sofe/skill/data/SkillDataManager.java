package com.sofe.skill.data;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.sofe.SoFEMod;
import com.sofe.player.PlayerClass;
import com.sofe.skill.SkillCatalog;
import com.sofe.skill.SkillInfo;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.event.AddReloadListenerEvent;

import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

/**
 * Loads data/sofe/skills/&lt;class&gt;.json on server start and on /reload, so costs, cooldowns
 * and damage can be balanced without recompiling (docs/Arquitectura.md, section 4).
 */
public final class SkillDataManager extends SimpleJsonResourceReloadListener {
    private static volatile Map<PlayerClass, ClassSkillData> data = Map.of();

    private SkillDataManager() {
        super(new Gson(), "skills");
    }

    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new SkillDataManager());
    }

    public static Optional<ClassSkillData> forClass(PlayerClass playerClass) {
        return Optional.ofNullable(data.get(playerClass));
    }

    public static Optional<SkillStats> stats(SkillInfo skill) {
        return forClass(skill.owner()).flatMap(d -> d.skill(skill.id()));
    }

    @Override
    protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager resources, ProfilerFiller profiler) {
        Map<PlayerClass, ClassSkillData> loaded = new EnumMap<>(PlayerClass.class);
        files.forEach((id, json) -> {
            Optional<PlayerClass> owner = PlayerClass.byId(id.getPath());
            if (owner.isEmpty() || !json.isJsonObject()) {
                SoFEMod.LOGGER.error("Skipping skill file {}: the file name must be a class id", id);
                return;
            }
            try {
                ClassSkillData classData = ClassSkillData.parse(json.getAsJsonObject());
                for (SkillInfo skill : SkillCatalog.forClass(owner.get())) {
                    if (classData.skill(skill.id()).isEmpty()) {
                        SoFEMod.LOGGER.warn("{} has no values for skill {}", id, skill.id());
                    }
                }
                loaded.put(owner.get(), classData);
            } catch (IllegalArgumentException e) {
                SoFEMod.LOGGER.error("Skipping skill file {}: {}", id, e.getMessage());
            }
        });
        data = Map.copyOf(loaded);
        SoFEMod.LOGGER.info("Loaded skill data for {} classes", loaded.size());
    }
}
