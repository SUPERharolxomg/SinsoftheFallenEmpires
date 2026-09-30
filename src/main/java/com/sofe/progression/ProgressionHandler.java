package com.sofe.progression;

import com.sofe.mob.MobExperience;
import com.sofe.mob.MobLevels;
import com.sofe.network.SoFENetwork;
import com.sofe.network.SyncProgressPacket;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

/** Experience from kills, level-ups and keeping the client's level bar up to date (UC-03). */
public final class ProgressionHandler {

    private ProgressionHandler() {
    }

    /** The level-1 points arrive when the Bearer is chosen. */
    public static void onBearerChosen(ServerPlayer player) {
        ProgressionCapability.get(player).ifPresent(p -> p.grantStartingPoints(ProgressionRulesManager.leveling()));
        sync(player);
    }

    public static void onKill(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        LivingEntity victim = event.getEntity();
        MobLevels.levelOf(victim).ifPresent(level -> {
            double baseHealth = victim.getAttribute(Attributes.MAX_HEALTH) != null
                    ? victim.getAttribute(Attributes.MAX_HEALTH).getBaseValue() : 20;
            addXp(player, MobExperience.forKill(level, baseHealth));
        });
    }

    public static void addXp(ServerPlayer player, long amount) {
        ProgressionCapability.get(player).ifPresent(progress -> {
            int gained = progress.addXp(amount, ProgressionRulesManager.leveling());
            if (gained > 0) {
                LevelingRules rules = ProgressionRulesManager.leveling();
                player.sendSystemMessage(Component.translatable("message.sofe.level_up", progress.level(),
                        gained * rules.skillPointsPerLevel(), gained * rules.attributePointsPerLevel()).withStyle(ChatFormatting.GOLD));
                player.level().playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8f, 1.2f);
            }
            sync(player);
        });
    }

    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) sync(player);
    }

    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) sync(player);
    }

    public static void sync(ServerPlayer player) {
        ProgressionCapability.get(player).ifPresent(p -> SoFENetwork.sendTo(player, new SyncProgressPacket(
                p.level(), p.xp(), ProgressionRulesManager.leveling().xpToNext(p.level()), p.skillPoints(), p.attributePoints())));
    }
}
