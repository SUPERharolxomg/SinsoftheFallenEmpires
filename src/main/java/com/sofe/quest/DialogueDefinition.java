package com.sofe.quest;

import com.sofe.condition.Condition;

import java.util.List;

/**
 * A conversation from data/sofe/dialogue/*.json, shown in the Warcraft III style box
 * (docs/Jugabilidad.md, "Dialogue"). Text only, no voices: every line is a lang key.
 *
 * @param style     dialogue box style (sulthari, nordrath, parsivan, khemet, aureum, void)
 * @param cinematic letterbox bars and no movement while it plays
 * @param npc       the NPC that says it when talked to, or null for scenes opened by effects
 * @param requires  when an NPC has several dialogues, only those that pass are candidates (null: always)
 * @param priority  among the candidates, the highest priority is picked
 * @param onEnd     effects run when the conversation ends, by its last line or by closing the box
 */
public record DialogueDefinition(String id, String style, boolean cinematic, String npc, Condition requires, int priority,
                                 List<Line> lines, List<QuestEffect> onEnd) {

    /**
     * @param speaker   an NPC id (ozhan, ferid...), "player" (drawn live from the player's skin) or "narrator"
     * @param condition the line is skipped when it does not pass (null: always shown)
     * @param answers   empty: the line just continues to the next one
     */
    public record Line(String speaker, String text, Condition condition, List<Answer> answers, List<QuestEffect> effects) {
        public Line {
            answers = List.copyOf(answers);
            effects = List.copyOf(effects);
        }
    }

    /** @param next index of the next line, or -1 to end the conversation */
    public record Answer(String text, int next, List<QuestEffect> effects) {
        public Answer {
            effects = List.copyOf(effects);
        }
    }

    public DialogueDefinition {
        lines = List.copyOf(lines);
        onEnd = List.copyOf(onEnd);
        if (lines.isEmpty()) throw new IllegalArgumentException(id + ": a dialogue needs at least one line");
    }
}
