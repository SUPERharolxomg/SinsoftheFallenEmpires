package com.sofe.network;

import com.sofe.quest.DialogueService;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/**
 * Client → server: the player continued (answer -1), picked an answer, or closed the box
 * ({@link #close()}). The line is checked against the one the server showed.
 */
public record DialogueAnswerPacket(int line, int answer) {
    private static final int CLOSE = -2;

    public static DialogueAnswerPacket close() {
        return new DialogueAnswerPacket(-1, CLOSE);
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeVarInt(line + 1);
        buf.writeVarInt(answer + 2);
    }

    public static DialogueAnswerPacket decode(FriendlyByteBuf buf) {
        return new DialogueAnswerPacket(buf.readVarInt() - 1, buf.readVarInt() - 2);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        ServerPlayer player = context.get().getSender();
        if (player == null) return;
        if (answer == CLOSE) {
            DialogueService.close(player);
        } else {
            DialogueService.answer(player, line, answer);
        }
    }
}
