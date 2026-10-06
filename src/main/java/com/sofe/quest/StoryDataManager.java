package com.sofe.quest;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.sofe.SoFEMod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraftforge.event.AddReloadListenerEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.BiFunction;

/** Loads data/&lt;ns&gt;/quests/*.json, data/&lt;ns&gt;/dialogue/*.json and data/&lt;ns&gt;/puzzles/*.json on start and on /reload. */
public final class StoryDataManager {
    private static volatile Map<String, QuestDefinition> quests = Map.of();
    private static volatile Map<String, DialogueDefinition> dialogues = Map.of();
    private static volatile Map<String, com.sofe.puzzle.PuzzleDefinition> puzzles = Map.of();

    private StoryDataManager() {
    }

    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new Loader<>("quests", StoryParser::quest, loaded -> quests = loaded));
        event.addListener(new Loader<>("dialogue", StoryParser::dialogue, loaded -> dialogues = loaded));
        event.addListener(new Loader<>("puzzles", com.sofe.puzzle.PuzzleParser::parse, loaded -> puzzles = loaded));
    }

    /** The rune puzzles (data/sofe/puzzles). */
    public static Map<String, com.sofe.puzzle.PuzzleDefinition> puzzles() {
        return puzzles;
    }

    public static Optional<QuestDefinition> quest(String id) {
        return Optional.ofNullable(quests.get(id));
    }

    public static Map<String, QuestDefinition> quests() {
        return quests;
    }

    public static Optional<DialogueDefinition> dialogue(String id) {
        return Optional.ofNullable(dialogues.get(id));
    }

    public static Map<String, DialogueDefinition> dialogues() {
        return dialogues;
    }

    /** For GameTests: a puzzle of their own, next to the real ones. */
    public static void putPuzzleForTest(com.sofe.puzzle.PuzzleDefinition puzzle) {
        Map<String, com.sofe.puzzle.PuzzleDefinition> copy = new HashMap<>(puzzles);
        copy.put(puzzle.id(), puzzle);
        puzzles = Map.copyOf(copy);
    }

    /** For GameTests, which run without the reload listeners of a real server start. */
    public static void putForTest(QuestDefinition quest) {
        Map<String, QuestDefinition> copy = new HashMap<>(quests);
        copy.put(quest.id(), quest);
        quests = Map.copyOf(copy);
    }

    private static final class Loader<T> extends SimpleJsonResourceReloadListener {
        private final String folder;
        private final BiFunction<String, com.google.gson.JsonObject, T> parser;
        private final java.util.function.Consumer<Map<String, T>> sink;

        Loader(String folder, BiFunction<String, com.google.gson.JsonObject, T> parser, java.util.function.Consumer<Map<String, T>> sink) {
            super(new Gson(), folder);
            this.folder = folder;
            this.parser = parser;
            this.sink = sink;
        }

        @Override
        protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager resources, ProfilerFiller profiler) {
            Map<String, T> loaded = new HashMap<>();
            files.forEach((id, json) -> {
                try {
                    loaded.put(id.toString(), parser.apply(id.toString(), json.getAsJsonObject()));
                } catch (RuntimeException e) {
                    SoFEMod.LOGGER.error("Skipping {} {}: {}", folder, id, e.getMessage());
                }
            });
            sink.accept(Map.copyOf(loaded));
            SoFEMod.LOGGER.info("Loaded {} {}", loaded.size(), folder);
        }
    }
}
