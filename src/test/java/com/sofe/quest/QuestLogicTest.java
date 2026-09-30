package com.sofe.quest;

import com.google.gson.JsonParser;
import com.sofe.condition.ProgressView;
import com.sofe.story.StoryProgress;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class QuestLogicTest {

    private static final QuestDefinition HUNT = StoryParser.quest("sofe:hunt", JsonParser.parseString("""
            {"type": "side", "steps": [
              {"objective": {"type": "kill", "entity": "sofe:void_*", "count": 2},
               "on_start": [{"type": "give_xp", "amount": 5}]},
              {"objective": {"type": "talk", "npc": "ozhan"},
               "on_start": [{"type": "open_dialogue", "dialogue": "sofe:thanks"}]}
            ],
            "rewards": [{"type": "give_xp", "amount": 100}],
            "requires": {"type": "act_reached", "act": 2}}
            """).getAsJsonObject());

    private static ProgressView act(int act) {
        return new ProgressView() {
            @Override public int act() { return act; }
            @Override public boolean hasDefeated(String bossId) { return false; }
            @Override public int questStep(String questId) { return 0; }
            @Override public int countItem(String itemId) { return 0; }
        };
    }

    @Test
    void aQuestStartsOnlyWhenItsRequirementPasses() {
        StoryProgress story = new StoryProgress();
        assertTrue(QuestLogic.start(story, HUNT, act(1)).isEmpty());
        assertTrue(story.quest("sofe:hunt").isEmpty());
        assertEquals(List.of(new QuestEffect.GiveXp(5)), QuestLogic.start(story, HUNT, act(2)));
        assertTrue(QuestLogic.start(story, HUNT, act(2)).isEmpty(), "starting again runs nothing");
    }

    @Test
    void killsCountUntilTheStepIsDoneThenTheNextStepStarts() {
        StoryProgress story = new StoryProgress();
        QuestLogic.start(story, HUNT, act(2));
        Map<String, QuestDefinition> quests = Map.of(HUNT.id(), HUNT);

        assertTrue(QuestLogic.record(story, quests, new QuestEvent.Killed("minecraft:zombie")).isEmpty());
        assertEquals(0, story.quest("sofe:hunt").orElseThrow().count());
        QuestLogic.record(story, quests, new QuestEvent.Killed("sofe:void_wretch"));
        assertEquals(1, story.quest("sofe:hunt").orElseThrow().count());
        List<QuestEffect> next = QuestLogic.record(story, quests, new QuestEvent.Killed("sofe:void_stalker"));
        assertEquals(List.of(new QuestEffect.OpenDialogue("sofe:thanks")), next);
        assertEquals(2, story.questStep("sofe:hunt"));
    }

    @Test
    void theLastStepGivesTheRewardsAndCompletesTheQuest() {
        StoryProgress story = new StoryProgress();
        QuestLogic.start(story, HUNT, act(2));
        Map<String, QuestDefinition> quests = Map.of(HUNT.id(), HUNT);
        QuestLogic.advance(story, HUNT);
        assertTrue(QuestLogic.record(story, quests, new QuestEvent.Talked("ferid")).isEmpty());
        assertEquals(List.of(new QuestEffect.GiveXp(100)), QuestLogic.record(story, quests, new QuestEvent.Talked("ozhan")));
        assertTrue(story.quest("sofe:hunt").orElseThrow().completed());
        assertTrue(QuestLogic.record(story, quests, new QuestEvent.Talked("ozhan")).isEmpty(), "a finished quest ignores events");
        assertTrue(QuestLogic.advance(story, HUNT).isEmpty());
    }

    @Test
    void objectivesMatchTheirOwnEventsOnly() {
        assertEquals(1, QuestLogic.progress(new Objective.Reach(100, 100, 8), 0, new QuestEvent.At(105, 104)));
        assertEquals(0, QuestLogic.progress(new Objective.Reach(100, 100, 8), 0, new QuestEvent.At(109, 100)));
        assertEquals(2, QuestLogic.progress(new Objective.LearnSkills(3), 0, new QuestEvent.SkillsLearned(2)));
        assertEquals(3, QuestLogic.progress(new Objective.LearnSkills(3), 2, new QuestEvent.SkillsLearned(7)));
        assertEquals(1, QuestLogic.progress(new Objective.ReachLevel(5), 0, new QuestEvent.LevelReached(6)));
        assertEquals(0, QuestLogic.progress(new Objective.ReachLevel(5), 0, new QuestEvent.LevelReached(4)));
        assertEquals(1, QuestLogic.progress(new Objective.ReachRegion("nordrath"), 0, new QuestEvent.EnteredRegion("nordrath")));
        assertEquals(1, QuestLogic.progress(new Objective.DefeatBoss("sofe:vorath"), 0, new QuestEvent.BossDefeated("sofe:vorath")));
        assertEquals(0, QuestLogic.progress(new Objective.Manual(), 0, new QuestEvent.Talked("ozhan")));
        assertTrue(QuestLogic.matches("sofe:void_*", "sofe:void_wretch"));
        assertFalse(QuestLogic.matches("sofe:void_wretch", "sofe:void_stalker"));
    }
}
