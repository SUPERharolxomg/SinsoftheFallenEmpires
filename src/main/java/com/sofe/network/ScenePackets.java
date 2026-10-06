package com.sofe.network;

import com.sofe.quest.SceneService;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.fml.DistExecutor;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

/** The packets of a scene (SceneService): the server starts it on one player's screen, the client says when it ended. */
public final class ScenePackets {
    private ScenePackets() {
    }

    /** Server → client: play this scene. */
    public record Play(String scene) {
        public void encode(FriendlyByteBuf buf) {
            buf.writeUtf(scene);
        }

        public static Play decode(FriendlyByteBuf buf) {
            return new Play(buf.readUtf());
        }

        public void handle(Supplier<NetworkEvent.Context> context) {
            DistExecutor.unsafeRunWhenOn(Dist.CLIENT, () -> () -> com.sofe.client.ClientPacketHandlers.playScene(scene));
        }
    }

    /** Client → server: the scene ended (played out or skipped). */
    public record Done() {
        public void encode(FriendlyByteBuf buf) {
        }

        public static Done decode(FriendlyByteBuf buf) {
            return new Done();
        }

        public void handle(Supplier<NetworkEvent.Context> context) {
            ServerPlayer player = context.get().getSender();
            if (player != null) SceneService.done(player);
        }
    }
}
