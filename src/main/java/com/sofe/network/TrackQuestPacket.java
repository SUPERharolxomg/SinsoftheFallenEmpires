package com.sofe.network;

import com.sofe.quest.QuestEngine;
import com.sofe.story.StoryCapability;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** Client → server: the quest the Quest Compass should follow, picked in the Journal. */
public record TrackQuestPacket(String quest) {

    public void encode(FriendlyByteBuf buf) {
        buf.writeUtf(quest);
    }

    public static TrackQuestPacket decode(FriendlyByteBuf buf) {
        return new TrackQuestPacket(buf.readUtf());
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        ServerPlayer player = context.get().getSender();
        if (player == null) return;
        StoryCapability.get(player).ifPresent(story -> story.track(quest));
        QuestEngine.sync(player);
    }
}
