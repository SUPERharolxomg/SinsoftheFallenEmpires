package com.sofe.client;

import net.minecraft.client.Minecraft;
import net.minecraft.world.Difficulty;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.LevelSettings;
import net.minecraft.world.level.WorldDataConfiguration;
import net.minecraft.world.level.levelgen.WorldOptions;
import net.minecraft.world.level.levelgen.presets.WorldPresets;
import net.minecraftforge.event.TickEvent;

/**
 * The world the screenshot tools (ArmorShots, SkillShots) open with --quickPlaySingleplayer shots. When run/saves
 * has been cleared there is none, Quick Play fails and the game waits on its error screen; then this creates a
 * fresh creative world called "shots" and opens it, so the tools run on a clean save every time. PlaceShots uses
 * "shots_aetheris" instead, made with the Aetheris preset, so every place stands on its own empire's land.
 */
public final class ShotsWorld {
    public static final String NAME = "shots", AETHERIS = "shots_aetheris";
    private static int idle;
    private static boolean created;

    private ShotsWorld() {
    }

    public static void onClientTick(TickEvent.ClientTickEvent event) {
        if (event.phase != TickEvent.Phase.END || created) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.level != null || mc.getSingleplayerServer() != null) {
            idle = 0;
            return;
        }
        boolean aetheris = PlaceShots.enabled() || DeathKeysCheck.enabled() || ProtectCheck.enabled();
        String name = aetheris ? AETHERIS : NAME;
        if (++idle < 60 || mc.getLevelSource().levelExists(name)) return;
        created = true;
        LevelSettings settings = new LevelSettings(name, GameType.CREATIVE, false, Difficulty.PEACEFUL, true, new GameRules(),
                WorldDataConfiguration.DEFAULT);
        if (aetheris) {
            mc.createWorldOpenFlows().createFreshLevel(name, settings, new WorldOptions(WorldOptions.randomSeed(), false, false),
                    registries -> registries.registryOrThrow(net.minecraft.core.registries.Registries.WORLD_PRESET)
                            .getHolderOrThrow(com.sofe.world.gen.SoFEWorldPresets.AETHERIS).value().createWorldDimensions());
        } else {
            mc.createWorldOpenFlows().createFreshLevel(name, settings, new WorldOptions(WorldOptions.randomSeed(), false, false),
                    WorldPresets::createNormalWorldDimensions);
        }
    }
}
