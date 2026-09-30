package com.sofe.condition;

import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.util.Map;
import java.util.Set;

import static org.junit.jupiter.api.Assertions.*;

class ConditionParserTest {

    /** A player in a given act, with some bosses, quest steps and items. */
    private record FakeProgress(int act, Set<String> bosses, Map<String, Integer> quests, Map<String, Integer> items)
            implements ProgressView {
        @Override
        public boolean hasDefeated(String bossId) {
            return bosses.contains(bossId);
        }

        @Override
        public int questStep(String questId) {
            return quests.getOrDefault(questId, 0);
        }

        @Override
        public int countItem(String itemId) {
            return items.getOrDefault(itemId, 0);
        }
    }

    private static Condition parse(String json) {
        return ConditionParser.parse(JsonParser.parseString(json));
    }

    private static FakeProgress act(int act, String... bosses) {
        return new FakeProgress(act, Set.of(bosses), Map.of(), Map.of());
    }

    @Test
    void khemetCatacombsNeedActThreeAndLuxara() {
        Condition c = parse("""
                {"type":"all_of","conditions":[
                  {"type":"act_reached","act":3},
                  {"type":"boss_defeated","boss":"sofe:luxara"}]}""");
        assertFalse(c.test(act(2, "sofe:vorath")));
        assertFalse(c.test(act(3, "sofe:vorath")));
        assertTrue(c.test(act(3, "sofe:vorath", "sofe:luxara")));
    }

    @Test
    void voidGateNeedsEnvyrisAndTwelveEyes() {
        Condition c = parse("""
                {"type":"all_of","conditions":[
                  {"type":"boss_defeated","boss":"sofe:envyris"},
                  {"type":"item_owned","item":"minecraft:ender_eye","count":12}]}""");
        FakeProgress eleven = new FakeProgress(4, Set.of("sofe:envyris"), Map.of(), Map.of("minecraft:ender_eye", 11));
        FakeProgress twelve = new FakeProgress(4, Set.of("sofe:envyris"), Map.of(), Map.of("minecraft:ender_eye", 12));
        assertFalse(c.test(eleven));
        assertTrue(c.test(twelve));
    }

    @Test
    void anyOfNotAndQuestStep() {
        Condition c = parse("""
                {"type":"any_of","conditions":[
                  {"type":"quest_step","quest":"sofe:act1_main","step":3},
                  {"type":"not","condition":{"type":"act_reached","act":2}}]}""");
        assertTrue(c.test(act(1)));                                                        // not act 2 yet
        assertFalse(c.test(act(2)));                                                       // act 2, quest not reached
        assertTrue(c.test(new FakeProgress(2, Set.of(), Map.of("sofe:act1_main", 3), Map.of())));
    }

    @Test
    void itemOwnedDefaultsToOne() {
        Condition c = parse("{\"type\":\"item_owned\",\"item\":\"sofe:sealing_quill\"}");
        assertFalse(c.test(act(5)));
        assertTrue(c.test(new FakeProgress(5, Set.of(), Map.of(), Map.of("sofe:sealing_quill", 1))));
    }

    @Test
    void errorsSayWhereTheProblemIs() {
        IllegalArgumentException e = assertThrows(IllegalArgumentException.class, () -> parse("""
                {"type":"all_of","conditions":[{"type":"act_reached","act":3},{"type":"boss_defeatd","boss":"sofe:luxara"}]}"""));
        assertTrue(e.getMessage().startsWith("$.conditions[1]"), e.getMessage());
        assertTrue(e.getMessage().contains("boss_defeatd"), e.getMessage());
    }

    @Test
    void rejectsBadValues() {
        assertThrows(IllegalArgumentException.class, () -> parse("{\"type\":\"act_reached\",\"act\":6}"));
        assertThrows(IllegalArgumentException.class, () -> parse("{\"type\":\"act_reached\",\"act\":\"three\"}"));
        assertThrows(IllegalArgumentException.class, () -> parse("{\"type\":\"boss_defeated\",\"boss\":\"Vorath\"}"));
        assertThrows(IllegalArgumentException.class, () -> parse("{\"type\":\"all_of\",\"conditions\":[]}"));
        assertThrows(IllegalArgumentException.class, () -> parse("{\"type\":\"not\"}"));
        assertThrows(IllegalArgumentException.class, () -> parse("[]"));
    }
}
