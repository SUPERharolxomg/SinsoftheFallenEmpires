package com.sofe.quest;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Reads every real quest and dialogue file, so a typo fails the build instead of being skipped at
 * runtime, and checks that every text they use exists in both lang files.
 */
class StoryDataFilesTest {
    private static final Path DATA = Path.of("src/main/resources/data/sofe");
    private static final Path LANG = Path.of("src/main/resources/assets/sofe/lang");

    private static Map<String, JsonObject> read(String folder) throws IOException {
        Map<String, JsonObject> files = new HashMap<>();
        Path root = DATA.resolve(folder);
        try (Stream<Path> paths = Files.walk(root)) {
            for (Path p : paths.filter(f -> f.toString().endsWith(".json")).toList()) {
                String rel = root.relativize(p).toString().replace('\\', '/');
                files.put("sofe:" + rel.substring(0, rel.length() - 5), JsonParser.parseString(Files.readString(p)).getAsJsonObject());
            }
        }
        return files;
    }

    private static JsonObject lang(String name) throws IOException {
        return JsonParser.parseString(Files.readString(LANG.resolve(name))).getAsJsonObject();
    }

    @Test
    void everyQuestParsesAndHasItsTexts() throws IOException {
        Map<String, JsonObject> files = read("quests");
        assertTrue(files.containsKey(QuestEngine.FIRST_QUEST), "the Act I quest exists");
        for (String langFile : List.of("en_us.json", "es_es.json")) {
            JsonObject lang = lang(langFile);
            files.forEach((id, json) -> {
                QuestDefinition quest = StoryParser.quest(id, json);
                assertTrue(lang.has(quest.translationKey()), langFile + " is missing " + quest.translationKey());
                for (int i = 0; i < quest.steps().size(); i++) {
                    assertTrue(lang.has(quest.stepKey(i)), langFile + " is missing " + quest.stepKey(i));
                }
            });
        }
    }

    @Test
    void everyDialogueParsesAndHasItsTexts() throws IOException {
        Map<String, JsonObject> files = read("dialogue");
        Map<String, JsonObject> quests = read("quests");
        for (String langFile : List.of("en_us.json", "es_es.json")) {
            JsonObject lang = lang(langFile);
            files.forEach((id, json) -> {
                DialogueDefinition dialogue = StoryParser.dialogue(id, json);
                for (DialogueDefinition.Line line : dialogue.lines()) {
                    assertTrue(lang.has(line.text()), langFile + " is missing " + line.text());
                    if (!line.speaker().equals("player") && !line.speaker().equals("narrator")) {
                        assertTrue(lang.has("npc.sofe." + line.speaker()), langFile + " is missing the name of " + line.speaker());
                    }
                    for (DialogueDefinition.Answer answer : line.answers()) {
                        assertTrue(lang.has(answer.text()), langFile + " is missing " + answer.text());
                        assertTrue(answer.next() < dialogue.lines().size(), id + ": an answer jumps past the last line");
                        answer.effects().forEach(e -> checkReferences(id, e, files, quests));
                    }
                    line.effects().forEach(e -> checkReferences(id, e, files, quests));
                }
            });
        }
        quests.forEach((id, json) -> {
            QuestDefinition quest = StoryParser.quest(id, json);
            quest.rewards().forEach(e -> checkReferences(id, e, files, quests));
            quest.steps().forEach(s -> s.onStart().forEach(e -> checkReferences(id, e, files, quests)));
        });
    }

    private static void checkReferences(String from, QuestEffect effect, Map<String, JsonObject> dialogues, Map<String, JsonObject> quests) {
        if (effect instanceof QuestEffect.OpenDialogue d) {
            assertTrue(dialogues.containsKey(d.dialogue()), from + " opens a missing dialogue " + d.dialogue());
        }
        if (effect instanceof QuestEffect.StartQuest q) {
            assertTrue(quests.containsKey(q.quest()), from + " starts a missing quest " + q.quest());
        }
        if (effect instanceof QuestEffect.AdvanceQuest q) {
            assertTrue(quests.containsKey(q.quest()), from + " advances a missing quest " + q.quest());
        }
    }
}
