package com.sofe.quest;

import com.sofe.SoFEMod;
import com.sofe.network.DialogueLinePacket;
import com.sofe.network.SoFENetwork;
import com.sofe.story.PlayerProgressView;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.util.Map;
import java.util.Optional;
import java.util.OptionalInt;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Conversations on the server (UC-06). The server keeps which line each player is on, so a client
 * can only answer the line it was shown, and every effect runs here.
 */
public final class DialogueService {
    /** Walking this far from the NPC ends a conversation (not a cinematic). */
    private static final double MAX_DISTANCE = 8;
    private static final Map<UUID, Session> SESSIONS = new ConcurrentHashMap<>();

    private record Session(DialogueDefinition dialogue, int line, Integer npcEntity) {
    }

    private DialogueService() {
    }

    /** What an NPC says when the player talks to them; counts as "talk" for quests first. */
    public static void talkTo(ServerPlayer player, String npc, Entity npcEntity) {
        QuestEngine.event(player, new QuestEvent.Talked(npc));
        DialogueLogic.forNpc(StoryDataManager.dialogues().values(), npc, PlayerProgressView.of(player))
                .ifPresent(d -> open(player, d, npcEntity == null ? null : npcEntity.getId()));
    }

    public static void open(ServerPlayer player, String dialogueId, Integer npcEntity) {
        Optional<DialogueDefinition> dialogue = StoryDataManager.dialogue(dialogueId);
        if (dialogue.isEmpty()) {
            SoFEMod.LOGGER.warn("Unknown dialogue {}", dialogueId);
            return;
        }
        open(player, dialogue.get(), npcEntity);
    }

    private static void open(ServerPlayer player, DialogueDefinition dialogue, Integer npcEntity) {
        show(player, dialogue, DialogueLogic.lineFrom(dialogue, 0, PlayerProgressView.of(player)), npcEntity);
    }

    public static boolean isTalking(ServerPlayer player) {
        return SESSIONS.containsKey(player.getUUID());
    }

    /** The dialogue and line a player is on, for GameTests and debugging. */
    public static Optional<String> current(ServerPlayer player) {
        return Optional.ofNullable(SESSIONS.get(player.getUUID())).map(s -> s.dialogue().id() + "#" + s.line());
    }

    /** The client pressed continue (answer -1) or picked an answer on the line it was shown. */
    public static void answer(ServerPlayer player, int line, int answer) {
        Session session = SESSIONS.get(player.getUUID());
        if (session == null || session.line() != line) return;
        DialogueDefinition.Line current = session.dialogue().lines().get(line);
        if (!current.answers().isEmpty()) {
            if (answer < 0 || answer >= current.answers().size()) return;
            QuestEngine.run(player, current.answers().get(answer).effects());
            if (SESSIONS.get(player.getUUID()) != session) return; // an effect opened another conversation
        }
        show(player, session.dialogue(), DialogueLogic.after(session.dialogue(), line, answer, PlayerProgressView.of(player)), session.npcEntity());
    }

    /** The client closed the box (Escape): the conversation ends, and its on_end effects still run. */
    public static void close(ServerPlayer player) {
        Session session = SESSIONS.remove(player.getUUID());
        if (session != null) QuestEngine.run(player, session.dialogue().onEnd());
    }

    private static void show(ServerPlayer player, DialogueDefinition dialogue, OptionalInt line, Integer npcEntity) {
        if (line.isEmpty()) {
            SESSIONS.remove(player.getUUID());
            SoFENetwork.sendTo(player, DialogueLinePacket.CLOSE);
            QuestEngine.run(player, dialogue.onEnd());
            return;
        }
        Session session = new Session(dialogue, line.getAsInt(), npcEntity);
        SESSIONS.put(player.getUUID(), session);
        DialogueDefinition.Line shown = dialogue.lines().get(line.getAsInt());
        SoFENetwork.sendTo(player, new DialogueLinePacket(dialogue.id(), dialogue.style(), dialogue.cinematic(), line.getAsInt(),
                shown.speaker(), shown.text(), shown.answers().stream().map(DialogueDefinition.Answer::text).toList(),
                npcEntity == null ? -1 : npcEntity));
        QuestEngine.run(player, shown.effects());
    }

    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player) || player.tickCount % 10 != 0) return;
        Session session = SESSIONS.get(player.getUUID());
        if (session == null || session.dialogue().cinematic() || session.npcEntity() == null) return;
        Entity npc = player.level().getEntity(session.npcEntity());
        if (npc == null || !npc.isAlive() || npc.distanceToSqr(player) > MAX_DISTANCE * MAX_DISTANCE) {
            SESSIONS.remove(player.getUUID());
            SoFENetwork.sendTo(player, DialogueLinePacket.CLOSE);
        }
    }

    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        SESSIONS.remove(event.getEntity().getUUID());
    }
}
