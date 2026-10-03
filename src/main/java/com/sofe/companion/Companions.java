package com.sofe.companion;

import com.sofe.gear.PlayerGear;
import com.sofe.player.PlayerClass;
import com.sofe.registry.EntityRegistry;
import com.sofe.skill.ClassState;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.util.Optional;

/**
 * The companions (UC-22): a player hires one of the other four Bearers, one at a time. The choice is kept with
 * the player (it survives logging out and dying); the companion itself is spawned beside them when they join and
 * removed when they leave. Dismissed, or beaten in a fight, it goes back to Sulthari and can be hired again.
 */
public final class Companions {
    public static final String TAG = "sofe_companion";

    private Companions() {
    }

    /** The Bearer the player travels with, if any. */
    public static Optional<PlayerClass> hired(ServerPlayer player) {
        String id = player.getPersistentData().getString(TAG);
        return id.isEmpty() ? Optional.empty() : PlayerClass.byId(id);
    }

    public static Optional<CompanionEntity> entity(ServerPlayer player) {
        return player.serverLevel().getEntitiesOfClass(CompanionEntity.class, player.getBoundingBox().inflate(128),
                c -> player.getUUID().equals(c.owner())).stream().findFirst();
    }

    /**
     * Hires a Bearer if the rules allow it: not the player's own hero, not one already travelling with them, and not one
     * played by a Bearer of their group (within 32 blocks; the Pact of Sprint 7.5 will take its place). On a large server
     * many players may share a class, so the rest of the server does not count: every player has their own companion.
     */
    public static boolean hire(ServerPlayer player, PlayerClass bearer) {
        if (ClassState.classOf(player).map(bearer::equals).orElse(false)) {
            tell(player, Component.translatable("message.sofe.companion.own"));
            return false;
        }
        Optional<PlayerClass> current = hired(player);
        if (current.isPresent()) {
            tell(player, Component.translatable("message.sofe.companion.already", Component.translatable(current.get().heroKey())));
            return false;
        }
        for (ServerPlayer other : player.serverLevel().getEntitiesOfClass(ServerPlayer.class, player.getBoundingBox().inflate(ClassConcord.RANGE))) {
            boolean played = other != player && ClassState.classOf(other).map(bearer::equals).orElse(false);
            if (played) {
                tell(player, Component.translatable("message.sofe.companion.taken", Component.translatable(bearer.heroKey())));
                return false;
            }
        }
        player.getPersistentData().putString(TAG, bearer.id());
        CompanionEntity companion = spawn(player, bearer);
        companion.say(player, "hired");
        player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.5f, 1.4f);
        return true;
    }

    private static CompanionEntity spawn(ServerPlayer player, PlayerClass bearer) {
        CompanionEntity companion = new CompanionEntity(EntityRegistry.COMPANION.get(), player.level());
        companion.setup(player, bearer, PlayerGear.level(player));
        Vec3 at = player.position().add(player.getViewVector(1f).multiply(1, 0, 1).normalize().scale(-1.5));
        companion.moveTo(at.x, player.getY(), at.z, player.getYRot(), 0);
        player.level().addFreshEntity(companion);
        return companion;
    }

    /** An order from the companion's dialogue: follow, stay, or go back to Sulthari. */
    public static void order(ServerPlayer player, String order) {
        Optional<CompanionEntity> companion = entity(player);
        switch (order) {
            case "follow" -> companion.ifPresent(c -> {
                c.setStaying(false);
                tell(player, Component.translatable("message.sofe.companion.follow", c.getDisplayName()));
            });
            case "stay" -> companion.ifPresent(c -> {
                c.setStaying(true);
                tell(player, Component.translatable("message.sofe.companion.stay", c.getDisplayName()));
            });
            case "dismiss" -> {
                companion.ifPresent(c -> {
                    c.say(player, "dismissed");
                    c.discard();
                });
                player.getPersistentData().remove(TAG);
            }
            default -> {
            }
        }
    }

    /** Beaten in a fight: back to Sulthari, free to be hired again. */
    static void lost(ServerPlayer player) {
        player.getPersistentData().remove(TAG);
    }

    /** The companion comes along when its Bearer joins, levelled to them again. */
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) hired(player).ifPresent(b -> spawn(player, b));
    }

    /** And leaves with them; it is not saved in the world. */
    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) entity(player).ifPresent(LivingEntity::discard);
    }

    /** Through a portal or after a respawn the companion catches up. */
    public static void onChangedDimension(PlayerEvent.PlayerChangedDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        // the one left behind finds its Bearer gone from its world and goes home by itself
        hired(player).ifPresent(b -> spawn(player, b));
    }

    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        if (entity(player).isEmpty()) hired(player).ifPresent(b -> spawn(player, b));
    }

    /** A word when a boss falls near the companion. */
    public static void onDeath(LivingDeathEvent event) {
        if (!(event.getEntity() instanceof com.sofe.entity.boss.SoFEBossEntity boss) || !(boss.level() instanceof ServerLevel level)) return;
        for (CompanionEntity c : level.getEntitiesOfClass(CompanionEntity.class, boss.getBoundingBox().inflate(48))) {
            ServerPlayer player = c.ownerPlayer(c);
            if (player != null) c.say(player, "boss");
        }
    }

    private static void tell(ServerPlayer player, Component message) {
        player.displayClientMessage(message.copy().withStyle(ChatFormatting.AQUA), true);
    }
}
