package com.sofe.quest;

import com.google.gson.JsonParser;
import com.sofe.condition.ProgressView;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.OptionalInt;

import static org.junit.jupiter.api.Assertions.*;

class DialogueLogicTest {

    private static DialogueDefinition parse(String id, String json) {
        return StoryParser.dialogue(id, JsonParser.parseString(json).getAsJsonObject());
    }

    private static ProgressView player(String playerClass, int act) {
        return new ProgressView() {
            @Override public int act() { return act; }
            @Override public boolean hasDefeated(String bossId) { return false; }
            @Override public int questStep(String questId) { return 0; }
            @Override public int countItem(String itemId) { return 0; }
            @Override public String playerClass() { return playerClass; }
        };
    }

    private static final DialogueDefinition COUNCIL = parse("sofe:council", """
            {"npc": "ozhan", "lines": [
              {"speaker": "ozhan", "text": "a", "condition": {"type": "not", "condition": {"type": "class_is", "class": "king"}}},
              {"speaker": "ozhan", "text": "a.king", "condition": {"type": "class_is", "class": "king"}},
              {"speaker": "ozhan", "text": "b", "answers": [
                {"text": "go", "effects": [{"type": "advance_quest", "quest": "sofe:q"}]},
                {"text": "lore", "next": 3}]},
              {"speaker": "council_elder", "text": "lore"}
            ]}
            """);

    @Test
    void linesWhoseConditionFailsAreSkipped() {
        assertEquals(OptionalInt.of(0), DialogueLogic.lineFrom(COUNCIL, 0, player("knight", 1)));
        assertEquals(OptionalInt.of(1), DialogueLogic.lineFrom(COUNCIL, 0, player("king", 1)), "the King sees the disguise variant");
        assertEquals(OptionalInt.of(2), DialogueLogic.after(COUNCIL, 0, -1, player("knight", 1)));
    }

    @Test
    void answersJumpOrEndTheConversation() {
        ProgressView knight = player("knight", 1);
        assertEquals(OptionalInt.empty(), DialogueLogic.after(COUNCIL, 2, 0, knight));
        assertEquals(OptionalInt.of(3), DialogueLogic.after(COUNCIL, 2, 1, knight));
        assertEquals(OptionalInt.of(2), DialogueLogic.after(COUNCIL, 2, 7, knight), "a bad answer keeps the line");
        assertEquals(OptionalInt.empty(), DialogueLogic.after(COUNCIL, 3, -1, knight), "past the last line it ends");
    }

    @Test
    void anNpcSaysItsHighestPriorityDialogueThatPasses() {
        DialogueDefinition plain = parse("sofe:ozhan/default", """
                {"npc": "ozhan", "lines": [{"speaker": "ozhan", "text": "x"}]}""");
        DialogueDefinition actTwo = parse("sofe:ozhan/act2", """
                {"npc": "ozhan", "priority": 5, "requires": {"type": "act_reached", "act": 2},
                 "lines": [{"speaker": "ozhan", "text": "y"}]}""");
        DialogueDefinition other = parse("sofe:ferid/default", """
                {"npc": "ferid", "priority": 99, "lines": [{"speaker": "ferid", "text": "z"}]}""");
        List<DialogueDefinition> all = List.of(plain, actTwo, other);
        assertEquals("sofe:ozhan/default", DialogueLogic.forNpc(all, "ozhan", player("thief", 1)).orElseThrow().id());
        assertEquals("sofe:ozhan/act2", DialogueLogic.forNpc(all, "ozhan", player("thief", 2)).orElseThrow().id());
        assertTrue(DialogueLogic.forNpc(all, "selim", player("thief", 2)).isEmpty());
    }
}
