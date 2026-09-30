package com.sofe.quest;

import com.sofe.condition.ProgressView;

import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.OptionalInt;

/** Moving through a conversation without Minecraft: which line comes next and which dialogue an NPC says. */
public final class DialogueLogic {

    private DialogueLogic() {
    }

    /** The first line at or after {@code from} whose condition passes; empty when the conversation is over. */
    public static OptionalInt lineFrom(DialogueDefinition dialogue, int from, ProgressView view) {
        List<DialogueDefinition.Line> lines = dialogue.lines();
        for (int i = Math.max(0, from); i < lines.size(); i++) {
            DialogueDefinition.Line line = lines.get(i);
            if (line.condition() == null || line.condition().test(view)) return OptionalInt.of(i);
        }
        return OptionalInt.empty();
    }

    /**
     * Where the conversation goes from a line. A line without answers continues with the next line;
     * an answer jumps to its "next" (or ends with -1). A bad answer index is ignored and the line stays.
     */
    public static OptionalInt after(DialogueDefinition dialogue, int line, int answer, ProgressView view) {
        DialogueDefinition.Line current = dialogue.lines().get(line);
        if (current.answers().isEmpty()) return lineFrom(dialogue, line + 1, view);
        if (answer < 0 || answer >= current.answers().size()) return OptionalInt.of(line);
        int next = current.answers().get(answer).next();
        return next < 0 ? OptionalInt.empty() : lineFrom(dialogue, next, view);
    }

    /** The dialogue an NPC says now: the highest priority one of theirs whose requirement passes. */
    public static Optional<DialogueDefinition> forNpc(Collection<DialogueDefinition> dialogues, String npc, ProgressView view) {
        return dialogues.stream()
                .filter(d -> npc.equals(d.npc()))
                .filter(d -> d.requires() == null || d.requires().test(view))
                .max(Comparator.comparingInt(DialogueDefinition::priority).thenComparing(DialogueDefinition::id, Comparator.reverseOrder()));
    }
}
