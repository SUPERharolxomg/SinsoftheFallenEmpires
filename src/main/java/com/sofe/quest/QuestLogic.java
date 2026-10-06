package com.sofe.quest;

import com.sofe.condition.ProgressView;
import com.sofe.story.StoryProgress;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * The quest rules without Minecraft: starting quests, counting objectives and moving through
 * steps. Every call returns the effects the game must now run (a step's on_start, the rewards),
 * in order; the caller runs them.
 */
public final class QuestLogic {

    private QuestLogic() {
    }

    /** Starts a quest when its requirement passes. Returns the first step's effects. */
    public static List<QuestEffect> start(StoryProgress story, QuestDefinition quest, ProgressView view) {
        if (quest.requires() != null && !quest.requires().test(view)) return List.of();
        if (!story.start(quest.id())) return List.of();
        return new ArrayList<>(quest.steps().get(0).onStart());
    }

    /** Counts an event toward every active quest whose current step it matches. */
    public static List<QuestEffect> record(StoryProgress story, Map<String, QuestDefinition> quests, QuestEvent event) {
        List<QuestEffect> effects = new ArrayList<>();
        story.quests().forEach((id, state) -> {
            QuestDefinition quest = quests.get(id);
            if (quest == null || state.completed()) return;
            quest.step(state.step()).ifPresent(step -> {
                int count = progress(step.objective(), state.count(), event);
                if (count == state.count()) return;
                if (count >= step.objective().required()) {
                    effects.addAll(finishStep(story, quest));
                } else {
                    story.update(id, state.withCount(count));
                }
            });
        });
        return effects;
    }

    /** Finishes the current step (an "advance_quest" effect, or a completed objective). */
    public static List<QuestEffect> advance(StoryProgress story, QuestDefinition quest) {
        return story.quest(quest.id()).filter(s -> !s.completed()).map(s -> finishStep(story, quest)).orElse(List.of());
    }

    private static List<QuestEffect> finishStep(StoryProgress story, QuestDefinition quest) {
        StoryProgress.QuestState state = story.quest(quest.id()).orElseThrow();
        int next = state.step() + 1;
        if (next >= quest.steps().size()) {
            story.update(quest.id(), state.complete());
            return new ArrayList<>(quest.rewards());
        }
        story.update(quest.id(), state.advance());
        return new ArrayList<>(quest.steps().get(next).onStart());
    }

    /** The new count toward an objective after the event (unchanged when it does not match). */
    static int progress(Objective objective, int count, QuestEvent event) {
        if (objective instanceof Objective.Kill kill && event instanceof QuestEvent.Killed k && matches(kill.entity(), k.entity())) return count + 1;
        if (objective instanceof Objective.Talk talk && event instanceof QuestEvent.Talked t && talk.npc().equals(t.npc())) return 1;
        if (objective instanceof Objective.ReachRegion r && event instanceof QuestEvent.EnteredRegion e && r.region().equals(e.region())) return 1;
        if (objective instanceof Objective.Reach r && event instanceof QuestEvent.At at && within(r, at)) return 1;
        if (objective instanceof Objective.LearnSkills l && event instanceof QuestEvent.SkillsLearned s) return Math.max(count, Math.min(s.total(), l.count()));
        if (objective instanceof Objective.ReachLevel l && event instanceof QuestEvent.LevelReached r && r.level() >= l.level()) return 1;
        if (objective instanceof Objective.DefeatBoss b && event instanceof QuestEvent.BossDefeated d && b.boss().equals(d.boss())) return 1;
        if (objective instanceof Objective.Obtain o && event instanceof QuestEvent.Carries c && c.items().contains(o.item())) return 1;
        return count;
    }

    /** "sofe:void_*" matches every Void creature; anything else must match exactly. */
    static boolean matches(String pattern, String entity) {
        if (pattern.endsWith("*")) return entity.startsWith(pattern.substring(0, pattern.length() - 1));
        return pattern.equals(entity);
    }

    private static boolean within(Objective.Reach r, QuestEvent.At at) {
        long dx = at.x() - r.x(), dz = at.z() - r.z();
        return dx * dx + dz * dz <= (long) r.radius() * r.radius();
    }
}
