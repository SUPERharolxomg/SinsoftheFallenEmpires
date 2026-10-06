package com.sofe.pact;

import com.sofe.config.SoFEConfig;
import com.sofe.network.SoFENetwork;
import com.sofe.network.SyncPactPacket;
import com.sofe.player.PlayerClass;
import com.sofe.skill.ClassState;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.ClickEvent;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.HoverEvent;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * The Pact of the Empires (docs/Anexos.md, A5; UC-18): up to five Bearers who travel together. A player invites another
 * with /sofe pact invite (the invitation lasts a minute, with buttons in the chat to accept or decline); any member
 * may invite, the leader may send a member away, anyone may leave. Members within 48 blocks in the same dimension are
 * "together": they share kill XP, find better loot, make Class Concord and lift each other when downed in an arena.
 * The members' health goes to every member's HUD once a second.
 */
public final class Pacts {
    private static final int INVITE_TICKS = 20 * 60;
    private record Invite(UUID from, String fromName, long until) {
    }

    private static final Map<UUID, Invite> INVITES = new HashMap<>();

    private Pacts() {
    }

    public static Optional<Pact> of(ServerPlayer player) {
        return PactData.get(player.server).of(player.getUUID());
    }

    /** The members online, the player included. */
    public static List<ServerPlayer> online(ServerPlayer player) {
        List<ServerPlayer> out = new ArrayList<>();
        Optional<Pact> pact = of(player);
        if (pact.isEmpty()) {
            out.add(player);
            return out;
        }
        for (UUID id : pact.get().members()) {
            ServerPlayer p = player.server.getPlayerList().getPlayer(id);
            if (p != null) out.add(p);
        }
        return out;
    }

    /** The members together with the player (alive, same dimension, within range), the player first. */
    public static List<ServerPlayer> together(ServerPlayer player) {
        double range = SoFEConfig.SERVER.pactRange.get();
        List<ServerPlayer> out = new ArrayList<>();
        out.add(player);
        for (ServerPlayer p : online(player)) {
            if (p == player || !p.isAlive() || p.isSpectator() || p.level() != player.level()) continue;
            if (p.distanceToSqr(player) <= range * range) out.add(p);
        }
        return out;
    }

    public static boolean sameGroup(ServerPlayer a, ServerPlayer b) {
        if (a == b) return true;
        Optional<Pact> pact = of(a);
        return pact.isPresent() && pact.get().has(b.getUUID());
    }

    /** The better rarity odds the player's allies together give. */
    public static double findBonus(ServerPlayer player) {
        return PactRules.findBonus(together(player).size() - 1, SoFEConfig.SERVER.pactFindBonus.get(), SoFEConfig.SERVER.pactFindBonusCap.get());
    }

    // ------------------------------------------------------------------ the commands

    public static boolean invite(ServerPlayer from, ServerPlayer to) {
        PactData data = PactData.get(from.server);
        if (from == to) return fail(from, "message.sofe.pact.self");
        if (data.of(to.getUUID()).isPresent()) return fail(from, "message.sofe.pact.in_another", to.getDisplayName());
        Optional<Pact> pact = data.of(from.getUUID());
        if (pact.isPresent() && pact.get().size() >= SoFEConfig.SERVER.pactMaxMembers.get()) return fail(from, "message.sofe.pact.full");
        INVITES.put(to.getUUID(), new Invite(from.getUUID(), from.getGameProfile().getName(), from.server.getTickCount() + INVITE_TICKS));
        MutableComponent accept = Component.translatable("message.sofe.pact.accept_button").withStyle(s -> s.withColor(ChatFormatting.GREEN)
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sofe pact accept"))
                .withHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, Component.literal("/sofe pact accept"))));
        MutableComponent decline = Component.translatable("message.sofe.pact.decline_button").withStyle(s -> s.withColor(ChatFormatting.RED)
                .withClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/sofe pact decline")));
        to.sendSystemMessage(Component.translatable("message.sofe.pact.invited", from.getDisplayName()).withStyle(ChatFormatting.GOLD)
                .append(" ").append(accept).append(" ").append(decline));
        from.sendSystemMessage(Component.translatable("message.sofe.pact.invite_sent", to.getDisplayName()).withStyle(ChatFormatting.GRAY));
        return true;
    }

    public static boolean accept(ServerPlayer player) {
        Invite invite = INVITES.remove(player.getUUID());
        if (invite == null || invite.until() < player.server.getTickCount()) return fail(player, "message.sofe.pact.no_invite");
        PactData data = PactData.get(player.server);
        if (data.of(player.getUUID()).isPresent()) return fail(player, "message.sofe.pact.in_another", player.getDisplayName());
        Pact pact = data.of(invite.from()).orElseGet(() -> data.create(invite.from(), invite.fromName()));
        if (!PactRules.canJoin(pact.size(), false, SoFEConfig.SERVER.pactMaxMembers.get())) return fail(player, "message.sofe.pact.full");
        data.join(pact, player.getUUID(), player.getGameProfile().getName());
        tellAll(player.server, pact, Component.translatable("message.sofe.pact.joined", player.getDisplayName()).withStyle(ChatFormatting.GOLD));
        com.sofe.companion.Companions.onPactChanged(player);
        syncAll(player.server);
        return true;
    }

    public static boolean decline(ServerPlayer player) {
        Invite invite = INVITES.remove(player.getUUID());
        if (invite == null) return fail(player, "message.sofe.pact.no_invite");
        ServerPlayer from = player.server.getPlayerList().getPlayer(invite.from());
        if (from != null) from.sendSystemMessage(Component.translatable("message.sofe.pact.declined", player.getDisplayName()).withStyle(ChatFormatting.GRAY));
        player.sendSystemMessage(Component.translatable("message.sofe.pact.you_declined").withStyle(ChatFormatting.GRAY));
        return true;
    }

    public static boolean leave(ServerPlayer player) {
        PactData data = PactData.get(player.server);
        Optional<Pact> before = data.of(player.getUUID());
        if (before.isEmpty()) return fail(player, "message.sofe.pact.none");
        List<UUID> others = before.get().members();
        Optional<Pact> after = data.leave(player.getUUID());
        player.sendSystemMessage(Component.translatable("message.sofe.pact.you_left").withStyle(ChatFormatting.GRAY));
        for (UUID id : others) {
            ServerPlayer p = player.server.getPlayerList().getPlayer(id);
            if (p == null || p == player) continue;
            p.sendSystemMessage(Component.translatable(after.isPresent() ? "message.sofe.pact.left" : "message.sofe.pact.dissolved",
                    player.getDisplayName()).withStyle(ChatFormatting.GRAY));
        }
        SoFENetwork.sendTo(player, new SyncPactPacket(List.of()));
        syncAll(player.server);
        return true;
    }

    public static boolean kick(ServerPlayer leader, String name) {
        PactData data = PactData.get(leader.server);
        Optional<Pact> pact = data.of(leader.getUUID());
        if (pact.isEmpty()) return fail(leader, "message.sofe.pact.none");
        if (!pact.get().leader().equals(leader.getUUID())) return fail(leader, "message.sofe.pact.not_leader");
        Optional<UUID> target = pact.get().members().stream().filter(id -> pact.get().name(id).equalsIgnoreCase(name)).findFirst();
        if (target.isEmpty() || target.get().equals(leader.getUUID())) return fail(leader, "message.sofe.pact.not_member", Component.literal(name));
        List<UUID> members = pact.get().members();
        Optional<Pact> after = data.leave(target.get());
        ServerPlayer kicked = leader.server.getPlayerList().getPlayer(target.get());
        if (kicked != null) {
            kicked.sendSystemMessage(Component.translatable("message.sofe.pact.kicked").withStyle(ChatFormatting.GRAY));
            SoFENetwork.sendTo(kicked, new SyncPactPacket(List.of()));
        }
        for (UUID id : members) {
            ServerPlayer p = leader.server.getPlayerList().getPlayer(id);
            if (p != null && p != kicked) p.sendSystemMessage(Component.translatable(after.isPresent() ? "message.sofe.pact.left" : "message.sofe.pact.dissolved",
                    Component.literal(name)).withStyle(ChatFormatting.GRAY));
        }
        syncAll(leader.server);
        return true;
    }

    public static void list(ServerPlayer player) {
        Optional<Pact> pact = of(player);
        if (pact.isEmpty()) {
            player.sendSystemMessage(Component.translatable("message.sofe.pact.none").withStyle(ChatFormatting.GRAY));
            return;
        }
        player.sendSystemMessage(Component.translatable("message.sofe.pact.list", pact.get().size()).withStyle(ChatFormatting.GOLD));
        for (UUID id : pact.get().members()) {
            ServerPlayer p = player.server.getPlayerList().getPlayer(id);
            Component cls = p == null ? Component.translatable("message.sofe.pact.offline")
                    : ClassState.classOf(p).map(c -> (Component) Component.translatable(c.translationKey())).orElse(Component.literal("-"));
            player.sendSystemMessage(Component.literal((id.equals(pact.get().leader()) ? " ★ " : " • ") + pact.get().name(id) + " — ").append(cls)
                    .withStyle(p == null ? ChatFormatting.DARK_GRAY : ChatFormatting.WHITE));
        }
    }

    private static boolean fail(ServerPlayer player, String key, Object... args) {
        player.sendSystemMessage(Component.translatable(key, args).withStyle(ChatFormatting.RED));
        return false;
    }

    private static void tellAll(MinecraftServer server, Pact pact, Component message) {
        for (UUID id : pact.members()) {
            ServerPlayer p = server.getPlayerList().getPlayer(id);
            if (p != null) p.sendSystemMessage(message);
        }
    }

    // ------------------------------------------------------------------ the HUD and the Journal

    /** Once a second (and when the Pact changes): each member online learns how the others are. */
    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.getServer().getTickCount() % 20 != 3) return;
        syncAll(event.getServer());
        INVITES.values().removeIf(i -> i.until() < event.getServer().getTickCount());
    }

    public static void syncAll(MinecraftServer server) {
        PactData data = PactData.get(server);
        for (Pact pact : data.all()) {
            for (UUID id : pact.members()) {
                ServerPlayer p = server.getPlayerList().getPlayer(id);
                if (p != null) SoFENetwork.sendTo(p, packetFor(server, pact, p));
            }
        }
    }

    static SyncPactPacket packetFor(MinecraftServer server, Pact pact, ServerPlayer viewer) {
        double range = SoFEConfig.SERVER.pactRange.get();
        List<SyncPactPacket.Member> list = new ArrayList<>();
        for (UUID id : pact.members()) {
            ServerPlayer p = server.getPlayerList().getPlayer(id);
            boolean online = p != null;
            boolean near = online && p.level() == viewer.level() && p.distanceToSqr(viewer) <= range * range;
            String cls = online ? ClassState.classOf(p).map(PlayerClass::id).orElse("") : "";
            list.add(new SyncPactPacket.Member(id, pact.name(id), cls, online ? p.getHealth() : 0, online ? p.getMaxHealth() : 20,
                    online, near, online && Downed.isDowned(p), id.equals(pact.leader())));
        }
        return new SyncPactPacket(list);
    }

    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        PactData.get(player.server).rename(player.getUUID(), player.getGameProfile().getName());
        syncAll(player.server);
    }

    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        INVITES.remove(player.getUUID());
    }

    /** For GameTests: forget the invitations. */
    public static void forget() {
        INVITES.clear();
    }
}
