package com.sofe.network;

import com.sofe.SoFEMod;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.network.NetworkDirection;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

/**
 * The mod's network channel. Client and server must run the same protocol version;
 * bump it whenever a packet's format changes.
 */
public final class SoFENetwork {
    private static final String PROTOCOL = "4"; // 4: economy, merchants, stations and gear sync

    public static final SimpleChannel CHANNEL = NetworkRegistry.newSimpleChannel(
            SoFEMod.id("main"), () -> PROTOCOL, PROTOCOL::equals, PROTOCOL::equals);

    private static int nextId;

    private SoFENetwork() {
    }

    public static void register() {
        CHANNEL.messageBuilder(RegionEnteredPacket.class, nextId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(RegionEnteredPacket::encode)
                .decoder(RegionEnteredPacket::decode)
                .consumerMainThread(RegionEnteredPacket::handle)
                .add();
        CHANNEL.messageBuilder(OpenClassSelectPacket.class, nextId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(OpenClassSelectPacket::encode)
                .decoder(OpenClassSelectPacket::decode)
                .consumerMainThread(OpenClassSelectPacket::handle)
                .add();
        CHANNEL.messageBuilder(SyncClassPacket.class, nextId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SyncClassPacket::encode)
                .decoder(SyncClassPacket::decode)
                .consumerMainThread(SyncClassPacket::handle)
                .add();
        CHANNEL.messageBuilder(ChooseClassPacket.class, nextId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(ChooseClassPacket::encode)
                .decoder(ChooseClassPacket::decode)
                .consumerMainThread(ChooseClassPacket::handle)
                .add();
        registerCombat();
        registerStory();
    }

    private static void registerStory() {
        CHANNEL.messageBuilder(SyncStoryPacket.class, nextId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SyncStoryPacket::encode)
                .decoder(SyncStoryPacket::decode)
                .consumerMainThread(SyncStoryPacket::handle)
                .add();
        CHANNEL.messageBuilder(DialogueLinePacket.class, nextId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(DialogueLinePacket::encode)
                .decoder(DialogueLinePacket::decode)
                .consumerMainThread(DialogueLinePacket::handle)
                .add();
        CHANNEL.messageBuilder(DialogueAnswerPacket.class, nextId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(DialogueAnswerPacket::encode)
                .decoder(DialogueAnswerPacket::decode)
                .consumerMainThread(DialogueAnswerPacket::handle)
                .add();
        CHANNEL.messageBuilder(RegionLayoutPacket.class, nextId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(RegionLayoutPacket::encode)
                .decoder(RegionLayoutPacket::decode)
                .consumerMainThread(RegionLayoutPacket::handle)
                .add();
        CHANNEL.messageBuilder(OpenWaystonesPacket.class, nextId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(OpenWaystonesPacket::encode)
                .decoder(OpenWaystonesPacket::decode)
                .consumerMainThread(OpenWaystonesPacket::handle)
                .add();
        CHANNEL.messageBuilder(TravelPacket.class, nextId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(TravelPacket::encode)
                .decoder(TravelPacket::decode)
                .consumerMainThread(TravelPacket::handle)
                .add();
        CHANNEL.messageBuilder(BearerOfPacket.class, nextId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(BearerOfPacket::encode)
                .decoder(BearerOfPacket::decode)
                .consumerMainThread(BearerOfPacket::handle)
                .add();
        CHANNEL.messageBuilder(SyncEconomyPacket.class, nextId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SyncEconomyPacket::encode).decoder(SyncEconomyPacket::decode).consumerMainThread(SyncEconomyPacket::handle).add();
        CHANNEL.messageBuilder(OpenMerchantPacket.class, nextId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(OpenMerchantPacket::encode).decoder(OpenMerchantPacket::decode).consumerMainThread(OpenMerchantPacket::handle).add();
        CHANNEL.messageBuilder(MerchantActionPacket.class, nextId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(MerchantActionPacket::encode).decoder(MerchantActionPacket::decode).consumerMainThread(MerchantActionPacket::handle).add();
        CHANNEL.messageBuilder(StationPackets.Open.class, nextId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(StationPackets.Open::encode).decoder(StationPackets.Open::decode).consumerMainThread(StationPackets.Open::handle).add();
        CHANNEL.messageBuilder(StationPackets.Make.class, nextId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(StationPackets.Make::encode).decoder(StationPackets.Make::decode).consumerMainThread(StationPackets.Make::handle).add();
        CHANNEL.messageBuilder(StationPackets.Drink.class, nextId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(StationPackets.Drink::encode).decoder(StationPackets.Drink::decode).consumerMainThread(StationPackets.Drink::handle).add();
        CHANNEL.messageBuilder(TrackQuestPacket.class, nextId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(TrackQuestPacket::encode)
                .decoder(TrackQuestPacket::decode)
                .consumerMainThread(TrackQuestPacket::handle)
                .add();
    }

    private static void registerCombat() {
        CHANNEL.messageBuilder(SyncCombatPacket.class, nextId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SyncCombatPacket::encode)
                .decoder(SyncCombatPacket::decode)
                .consumerMainThread(SyncCombatPacket::handle)
                .add();
        CHANNEL.messageBuilder(SyncProgressPacket.class, nextId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(SyncProgressPacket::encode)
                .decoder(SyncProgressPacket::decode)
                .consumerMainThread(SyncProgressPacket::handle)
                .add();
        CHANNEL.messageBuilder(LearnSkillPacket.class, nextId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(LearnSkillPacket::encode)
                .decoder(LearnSkillPacket::decode)
                .consumerMainThread(LearnSkillPacket::handle)
                .add();
        CHANNEL.messageBuilder(SpendAttributePacket.class, nextId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(SpendAttributePacket::encode)
                .decoder(SpendAttributePacket::decode)
                .consumerMainThread(SpendAttributePacket::handle)
                .add();
        CHANNEL.messageBuilder(CastPosePacket.class, nextId++, NetworkDirection.PLAY_TO_CLIENT)
                .encoder(CastPosePacket::encode)
                .decoder(CastPosePacket::decode)
                .consumerMainThread(CastPosePacket::handle)
                .add();
        CHANNEL.messageBuilder(AssignSlotPacket.class, nextId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(AssignSlotPacket::encode)
                .decoder(AssignSlotPacket::decode)
                .consumerMainThread(AssignSlotPacket::handle)
                .add();
        CHANNEL.messageBuilder(StancePacket.class, nextId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(StancePacket::encode)
                .decoder(StancePacket::decode)
                .consumerMainThread(StancePacket::handle)
                .add();
        CHANNEL.messageBuilder(CastSkillPacket.class, nextId++, NetworkDirection.PLAY_TO_SERVER)
                .encoder(CastSkillPacket::encode)
                .decoder(CastSkillPacket::decode)
                .consumerMainThread(CastSkillPacket::handle)
                .add();
    }

    /**
     * Sends to one player. Players without a real network connection (GameTest mock players,
     * fake players from other mods) are skipped: Forge cannot send to them and they have no HUD.
     */
    public static void sendTo(ServerPlayer player, Object packet) {
        if (player instanceof FakePlayer || player.connection == null || player.connection.connection.channel() == null) {
            return;
        }
        CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), packet);
    }

    /** Sends to every real player who can see this one (not to the player themselves). */
    public static void sendToTracking(ServerPlayer player, Object packet) {
        if (player instanceof FakePlayer || player.connection == null) return;
        CHANNEL.send(PacketDistributor.TRACKING_ENTITY.with(() -> player), packet);
    }

    public static void sendToServer(Object packet) {
        CHANNEL.sendToServer(packet);
    }
}
