package com.sofe.story;

import com.sofe.SoFEMod;
import net.minecraft.advancements.Advancement;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.server.level.ServerPlayer;

/**
 * The "Sins of the Fallen Empires" advancement tab (docs/Jugabilidad.md, G10). The advancements
 * (data/sofe/advancements/story/*.json) have one "done" criterion that only the story grants,
 * so they follow the campaign exactly and cannot be earned any other way.
 */
public final class SoFEAdvancements {
    private static final String CRITERION = "done";

    private SoFEAdvancements() {
    }

    /** Grants sofe:&lt;path&gt; if it exists; unknown paths are ignored (not every boss has one yet). */
    public static void award(ServerPlayer player, String path) {
        if (player.server == null) return;
        Advancement advancement = player.server.getAdvancements().getAdvancement(SoFEMod.id(path));
        if (advancement == null) return;
        AdvancementProgress progress = player.getAdvancements().getOrStartProgress(advancement);
        if (!progress.isDone()) player.getAdvancements().award(advancement, CRITERION);
    }

    public static boolean has(ServerPlayer player, String path) {
        Advancement advancement = player.server.getAdvancements().getAdvancement(SoFEMod.id(path));
        return advancement != null && player.getAdvancements().getOrStartProgress(advancement).isDone();
    }

    /** "sofe:vorath" -> "story/boss_vorath". */
    public static String bossPath(String bossId) {
        return "story/boss_" + bossId.substring(bossId.indexOf(':') + 1);
    }
}
