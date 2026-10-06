package com.sofe.network;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.UUID;
import java.util.function.Supplier;

/** Server → client: the members of the player's Pact (none when they are in no Pact), for the HUD and the Journal. */
public record SyncPactPacket(List<Member> members) {
    public record Member(UUID uuid, String name, String bearer, float health, float maxHealth, boolean online, boolean near, boolean downed,
                         boolean leader) {
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeCollection(members, (b, m) -> {
            b.writeUUID(m.uuid());
            b.writeUtf(m.name());
            b.writeUtf(m.bearer());
            b.writeFloat(m.health());
            b.writeFloat(m.maxHealth());
            b.writeBoolean(m.online());
            b.writeBoolean(m.near());
            b.writeBoolean(m.downed());
            b.writeBoolean(m.leader());
        });
    }

    public static SyncPactPacket decode(FriendlyByteBuf buf) {
        return new SyncPactPacket(buf.readList(b -> new Member(b.readUUID(), b.readUtf(), b.readUtf(), b.readFloat(), b.readFloat(),
                b.readBoolean(), b.readBoolean(), b.readBoolean(), b.readBoolean())));
    }

    public void handle(Supplier<NetworkEvent.Context> context) {
        DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.sofe.client.ClientPactData.update(this));
    }
}
