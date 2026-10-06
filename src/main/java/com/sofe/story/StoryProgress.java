package com.sofe.story;

import com.sofe.world.region.Region;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * One player's own campaign (docs/Anexos.md A6, docs/Jugabilidad.md G3): the act they are in,
 * their quests, the bosses they have credit for and the fate they chose for each region.
 * Plain Java so it is unit tested directly.
 */
public final class StoryProgress {
    public static final int FIRST_ACT = 1, LAST_ACT = 5;

    /** A quest in progress: the step the player is on and the count toward that step's objective. */
    public record QuestState(int step, int count, boolean completed) {
        public QuestState advance() {
            return new QuestState(step + 1, 0, false);
        }

        public QuestState withCount(int newCount) {
            return new QuestState(step, newCount, completed);
        }

        public QuestState complete() {
            return new QuestState(step, count, true);
        }
    }

    private int act = FIRST_ACT;
    private final Map<String, QuestState> quests = new HashMap<>();
    private final Set<String> bosses = new HashSet<>();
    /** The Relics a boss has given this Bearer: one of each, never again (docs/Jugabilidad.md, G9). */
    private final Set<String> relics = new HashSet<>();
    private final Map<Region, String> fates = new EnumMap<>(Region.class);
    private String trackedQuest;

    public int act() {
        return act;
    }

    /** Acts only move forward. */
    public boolean advanceTo(int newAct) {
        int clamped = Math.max(FIRST_ACT, Math.min(LAST_ACT, newAct));
        if (clamped <= act) return false;
        act = clamped;
        return true;
    }

    /** Whether the region is open for this player (docs/Mundo.md, W2). The ocean never opens. */
    public boolean hasUnlocked(Region region) {
        return region.opensAtAct() > 0 && act >= region.opensAtAct();
    }

    public Optional<QuestState> quest(String id) {
        return Optional.ofNullable(quests.get(id));
    }

    public Map<String, QuestState> quests() {
        return Map.copyOf(quests);
    }

    /** Starts a quest; a quest already started or finished is left as it is. */
    public boolean start(String questId) {
        if (quests.containsKey(questId)) return false;
        quests.put(questId, new QuestState(0, 0, false));
        if (trackedQuest == null) trackedQuest = questId;
        return true;
    }

    public void update(String questId, QuestState state) {
        quests.put(questId, state);
        if (state.completed() && questId.equals(trackedQuest)) {
            trackedQuest = quests.entrySet().stream().filter(e -> !e.getValue().completed()).map(Map.Entry::getKey).findFirst().orElse(null);
        }
    }

    public Optional<String> trackedQuest() {
        return Optional.ofNullable(trackedQuest);
    }

    public void track(String questId) {
        if (quests.containsKey(questId)) trackedQuest = questId;
    }

    /** The step a quest has reached, counting a finished quest as past every step. */
    public int questStep(String questId) {
        QuestState s = quests.get(questId);
        if (s == null) return 0;
        return s.completed() ? Integer.MAX_VALUE : s.step() + 1;
    }

    public void defeat(String bossId) {
        bosses.add(bossId);
    }

    public boolean hasDefeated(String bossId) {
        return bosses.contains(bossId);
    }

    public Set<String> bosses() {
        return Set.copyOf(bosses);
    }

    /** Records a Relic given by a boss; false when the Bearer already had it. */
    public boolean receiveRelic(String relic) {
        return relics.add(relic);
    }

    public boolean hasRelic(String relic) {
        return relics.contains(relic);
    }

    public Set<String> relics() {
        return Set.copyOf(relics);
    }

    public void loadRelics(Set<String> received) {
        relics.clear();
        relics.addAll(received);
    }

    /** The campaign is over for this Bearer once Nahrazel has fallen to them (the post-game, UC-33). */
    public boolean finishedCampaign() {
        return bosses.contains("sofe:nahrazel");
    }

    /** A region's fate is set once (docs/Jugabilidad.md, "Fates and epilogues"). */
    public boolean setFate(Region region, String fate) {
        if (fates.containsKey(region)) return false;
        fates.put(region, fate);
        return true;
    }

    public Optional<String> fate(Region region) {
        return Optional.ofNullable(fates.get(region));
    }

    public Map<Region, String> fates() {
        return Map.copyOf(fates);
    }

    public void load(int act, Map<String, QuestState> quests, Set<String> bosses, Map<Region, String> fates, String tracked) {
        this.act = Math.max(FIRST_ACT, Math.min(LAST_ACT, act));
        this.quests.clear();
        this.quests.putAll(quests);
        this.bosses.clear();
        this.bosses.addAll(bosses);
        this.fates.clear();
        this.fates.putAll(fates);
        this.trackedQuest = tracked != null && quests.containsKey(tracked) ? tracked : null;
    }
}
