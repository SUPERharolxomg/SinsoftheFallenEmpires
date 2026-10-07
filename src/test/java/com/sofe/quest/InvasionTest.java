package com.sofe.quest;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

/** The waves of an invasion and a lair, as the quest files give them (QuestDefinition.Invasion, StoryParser). */
class InvasionTest {
    private static final String STEP = """
            {"type": "side", "steps": [{"objective": {"type": "kill", "entity": "sofe:void_*", "count": %d},
              "invasion": {"center": {"x": 10, "z": 20}, "distance": 30, "points": {"south": {"x": 1, "z": 2}},
                           "waves": [{"from": ["east"], "count": 2}, {"from": ["west", "north"], "count": 4}]}}]}
            """;

    @Test
    void wavesFollowTheFallen() {
        var invasion = StoryParser.quest("sofe:test", JsonParser.parseString(STEP.formatted(6)).getAsJsonObject()).steps().get(0).invasion();
        assertEquals(6, invasion.total());
        assertEquals(0, invasion.waveAt(0));
        assertEquals(0, invasion.waveAt(1));
        assertEquals(1, invasion.waveAt(2));
        assertEquals(1, invasion.waveAt(6)); // all fallen: still the last
        assertEquals(2, invasion.endOf(0));
        assertEquals(6, invasion.endOf(1));
        var at = new QuestDefinition.Target(10, 20);
        assertEquals(new QuestDefinition.Target(40, 20), invasion.point(at, "east"));
        assertEquals(new QuestDefinition.Target(10, -10), invasion.point(at, "north"));
        assertEquals(new QuestDefinition.Target(1, 2), invasion.point(at, "south")); // a side with a place of its own
    }

    @Test
    void wavesMustAddUpToTheKillCount() {
        assertThrows(IllegalArgumentException.class, () -> StoryParser.quest("sofe:test", JsonParser.parseString(STEP.formatted(20)).getAsJsonObject()));
    }

    @Test
    void aLairAsksForItsLord() {
        String lair = """
                {"type": "dungeon", "steps": [{"objective": {"type": "kill", "entity": "%s", "count": 1},
                  "lair": {"x": 5, "z": 6, "depth": 18, "name": "lord.sofe.test"}}]}
                """;
        var step = StoryParser.quest("sofe:test", JsonParser.parseString(lair.formatted("lord:sofe:draugr")).getAsJsonObject()).steps().get(0);
        assertEquals(new QuestDefinition.Lair(5, 6, 18, "lord.sofe.test"), step.lair());
        assertThrows(IllegalArgumentException.class, () -> StoryParser.quest("sofe:test", JsonParser.parseString(lair.formatted("sofe:draugr")).getAsJsonObject()));
        // a lord counts only as one: a draugr met on the way does not
        assertEquals(0, QuestLogic.progress(new Objective.Kill("lord:sofe:draugr", 1), 0, new QuestEvent.Killed("sofe:draugr")));
        assertEquals(1, QuestLogic.progress(new Objective.Kill("lord:sofe:draugr", 1), 0, new QuestEvent.Killed("lord:sofe:draugr")));
    }
}
