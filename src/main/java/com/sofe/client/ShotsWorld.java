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
 * fresh creative world called "shots" and opens it, so the tools run on a clean save every time.
 */
public final class ShotsWorld {
    public static final String NAME = "shots";
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
        if (++idle < 60 || mc.getLevelSource().levelExists(NAME)) return;
        created = true;
        LevelSettings settings = new LevelSettings(NAME, GameType.CREATIVE, false, Difficulty.PEACEFUL, true, new GameRules(),
                WorldDataConfiguration.DEFAULT);
        mc.createWorldOpenFlows().createFreshLevel(NAME, settings, new WorldOptions(WorldOptions.randomSeed(), false, false),
                WorldPresets::createNormalWorldDimensions);
    }
}
