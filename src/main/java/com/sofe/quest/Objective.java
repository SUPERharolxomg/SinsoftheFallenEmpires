package com.sofe.quest;

/** What a quest step asks for. The server checks it through game events. */
public sealed interface Objective {

    /** How many times the objective must happen before the step is done. */
    default int required() {
        return 1;
    }

    record Kill(String entity, int count) implements Objective {
        @Override
        public int required() {
            return count;
        }
    }

    record Talk(String npc) implements Objective {
    }

    record ReachRegion(String region) implements Objective {
    }

    record Reach(int x, int z, int radius) implements Objective {
    }

    record LearnSkills(int count) implements Objective {
        @Override
        public int required() {
            return count;
        }
    }

    record ReachLevel(int level) implements Objective {
    }

    record DefeatBoss(String boss) implements Objective {
    }

    /** Carrying an item (the Sealing Quill): checked every second, so an item already carried counts at once. */
    record Obtain(String item) implements Objective {
    }

    /** Done only by an "advance_quest" effect, usually from a dialogue answer. */
    record Manual() implements Objective {
    }
}
