package com.sofe.skill;

import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * Skills that last: a line of fire, a zone, a rain of stars, a run of leaps. A task runs once per server
 * tick for its duration (the argument is the tick, 0 first) and stops early if its caster leaves or dies.
 */
public final class SkillTasks {
    @FunctionalInterface
    public interface Step {
        /** Returns false to stop the task early. */
        boolean run(int tick);
    }

    private record Task(ServerPlayer caster, int duration, Step step, int[] tick) {
    }

    private static final List<Task> TASKS = new ArrayList<>();

    private SkillTasks() {
    }

    public static void run(ServerPlayer caster, int durationTicks, Step step) {
        TASKS.add(new Task(caster, durationTicks, step, new int[]{0}));
    }

    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || TASKS.isEmpty()) return;
        List<Task> now = new ArrayList<>(TASKS);
        TASKS.clear();
        List<Task> keep = new ArrayList<>();
        for (Task task : now) {
            if (task.caster().isRemoved() || !task.caster().isAlive()) continue;
            boolean more = task.step().run(task.tick()[0]++);
            if (more && task.tick()[0] < task.duration()) keep.add(task);
        }
        keep.addAll(TASKS); // tasks started by other tasks
        TASKS.clear();
        TASKS.addAll(keep);
    }

    /** For tests: how many tasks are running. */
    public static int running() {
        return TASKS.size();
    }
}
