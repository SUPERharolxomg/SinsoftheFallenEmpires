package com.sofe.quest;

import com.sofe.network.SyncStoryPacket;
import com.sofe.story.PlayerProgressView;
import com.sofe.story.StoryProgress;
import com.sofe.world.NpcDirectory;
import com.sofe.world.zone.StructurePositions;
import net.minecraft.server.level.ServerPlayer;

import java.util.Optional;

/**
 * Where the Quest Compass leads (docs/Mundo.md, W3): the story is linear, so the player should always know
 * where to go next. A step that asks to talk to someone ("talk", or a "manual" step that an NPC's dialogue
 * finishes) leads to that NPC, wherever they stand, and names them so the client can mark them; any other
 * step leads to its place, if it has one.
 */
public final class QuestGuide {

    private QuestGuide() {
    }

    public static Optional<SyncStoryPacket.Target> target(ServerPlayer player, StoryProgress story) {
        Optional<String> tracked = story.trackedQuest();
        if (tracked.isEmpty()) return Optional.empty();
        String id = tracked.get();
        Optional<QuestDefinition> quest = StoryDataManager.quest(id);
        Optional<StoryProgress.QuestState> state = story.quest(id).filter(s -> !s.completed());
        if (quest.isEmpty() || state.isEmpty()) return Optional.empty();
        Optional<QuestDefinition.Step> step = quest.get().step(state.get().step());
        if (step.isEmpty()) return Optional.empty();

        Optional<String> npc = npcToTalkTo(player, id, step.get());
        if (npc.isPresent()) {
            Optional<SyncStoryPacket.Target> at = locate(player, npc.get());
            if (at.isPresent()) return at;
        }
        if (step.get().invasion() != null) { // during an invasion, the nearest rift
            Optional<QuestDefinition.Target> rift = VoidInvasion.compass(player);
            if (rift.isPresent()) return rift.map(t -> new SyncStoryPacket.Target(t.x(), t.z()));
        }
        return step.get().compassTarget().map(t -> new SyncStoryPacket.Target(t.x(), t.z()));
    }

    private static Optional<String> npcToTalkTo(ServerPlayer player, String questId, QuestDefinition.Step step) {
        if (step.objective() instanceof Objective.Talk talk) return Optional.of(talk.npc());
        if (step.objective() instanceof Objective.Manual) {
            return DialogueLogic.npcAdvancing(StoryDataManager.dialogues().values(), questId, PlayerProgressView.of(player));
        }
        return Optional.empty();
    }

    /** Where an NPC stands: as last seen in the world, or else their place in the layout. */
    private static Optional<SyncStoryPacket.Target> locate(ServerPlayer player, String npc) {
        return NpcDirectory.get(player.server).find(npc).map(p -> new SyncStoryPacket.Target(p.getX(), p.getZ(), npc))
                .or(() -> StructurePositions.get().npcs().stream().filter(n -> n.npc().equals(npc)).findFirst()
                        .map(n -> new SyncStoryPacket.Target(n.x(), n.z(), npc)));
    }
}
