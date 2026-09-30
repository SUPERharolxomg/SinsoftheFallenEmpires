package com.sofe.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.function.Supplier;

/**
 * Server → client: one line of a conversation to show, or {@link #CLOSE} to end it.
 *
 * @param speaker   NPC id, "player" or "narrator"
 * @param text      lang key of the line
 * @param answers   lang keys of the answers (empty: continue)
 * @param npcEntity the NPC being talked to, so the client can face it (-1: none)
 */
public record DialogueLinePacket(String dialogue, String style, boolean cinematic, int line, String speaker, String text,
                                 List<String> answers, int npcEntity) {
    public static final DialogueLinePacket CLOSE = new DialogueLinePacket("", "", false, -1, "", "", List.of(), -1);

    public boolean isClose() {
        return line < 0;
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(dialogue);
        buf.writeUtf(style);
        buf.writeBoolean(cinematic);
        buf.writeVarInt(line + 1);
        buf.writeUtf(speaker);
        buf.writeUtf(text);
        buf.writeCollection(answers, FriendlyByteBuf::writeUtf);
        buf.writeVarInt(npcEntity + 1);
    }

    public static DialogueLinePacket decode(FriendlyByteBuf buf) {
        return new DialogueLinePacket(buf.readUtf(), buf.readUtf(), buf.readBoolean(), buf.readVarInt() - 1, buf.readUtf(),
                buf.readUtf(), buf.readList(FriendlyByteBuf::readUtf), buf.readVarInt() - 1);
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.sofe.client.ClientPacketHandlers.showDialogue(this));
    }
}
