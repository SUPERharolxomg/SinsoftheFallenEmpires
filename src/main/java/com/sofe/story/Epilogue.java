package com.sofe.story;

import com.sofe.player.PlayerClass;
import com.sofe.world.region.Region;

import java.util.List;
import java.util.Map;
import java.util.function.Predicate;

/**
 * What the ending shows a Bearer (docs/Jugabilidad.md, "Fates and epilogues"; UC-31): one slide per region, picked by
 * the fate they chose there (or the region's unsettled slide when they chose none), then their own Bearer's epilogue,
 * the full one when they finished their four Bearer quests and the unfinished one when they did not. The same rules
 * on the server and on the client (EndingScreen), from the story the client is sent.
 */
public final class Epilogue {
    /** The regions in the order their slides are shown. */
    public static final List<Region> REGIONS = List.of(Region.SULTHARI, Region.NORDRATH, Region.PARSIVAN, Region.KHEMET, Region.AUREUM);
    /** The fate of a region whose choice was never made. */
    public static final String UNSETTLED = "unsettled";
    public static final int BEARER_QUESTS = 4;

    private Epilogue() {
    }

    /** The fate a region's slide shows: the one chosen, or {@link #UNSETTLED}. */
    public static String fateShown(Region region, Map<String, String> fates) {
        String fate = fates.get(region.id());
        return fate != null && RegionFates.isValid(region, fate) ? fate : UNSETTLED;
    }

    /** The lang key of a region's slide text: ending.sofe.&lt;region&gt;.&lt;fate&gt;. */
    public static String slideKey(Region region, Map<String, String> fates) {
        return "ending.sofe." + region.id() + "." + fateShown(region, fates);
    }

    /** The Bearer quests of a hero: sofe:bearer/&lt;hero&gt;_act1 to act4. */
    public static List<String> bearerQuests(PlayerClass bearer) {
        return java.util.stream.IntStream.rangeClosed(1, BEARER_QUESTS).mapToObj(i -> "sofe:bearer/" + bearer.npcId() + "_act" + i).toList();
    }

    /** Whether the Bearer earned the full epilogue: every one of their Bearer quests finished. */
    public static boolean full(PlayerClass bearer, Predicate<String> questCompleted) {
        return bearerQuests(bearer).stream().allMatch(questCompleted);
    }

    /** The lang key of the Bearer's epilogue: ending.sofe.bearer.&lt;hero&gt;.full or .unfinished. */
    public static String epilogueKey(PlayerClass bearer, boolean full) {
        return "ending.sofe.bearer." + bearer.npcId() + (full ? ".full" : ".unfinished");
    }
}
