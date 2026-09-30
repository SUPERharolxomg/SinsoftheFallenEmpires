package com.sofe.progression;

import com.sofe.combat.CombatHandler;
import com.sofe.mob.MobExperience;
import com.sofe.mob.MobLevels;
import com.sofe.network.SoFENetwork;
import com.sofe.network.SyncProgressPacket;
import com.sofe.player.PlayerClass;
import com.sofe.quest.QuestEngine;
import com.sofe.quest.QuestEvent;
import com.sofe.player.PlayerClassCapability;
import com.sofe.player.PlayerClassData;
import com.sofe.skill.SkillBook;
import com.sofe.skill.SkillCatalog;
import com.sofe.skill.SkillInfo;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.util.Locale;
import java.util.Optional;

/** Experience from kills, level-ups, learning skills, spending attribute points and syncing it all (UC-03). */
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
            if (gained > 0) QuestEngine.event(player, new QuestEvent.LevelReached(progress.level()));
        });
    }

    /** Puts a skill point into a skill if the tree allows it (docs/Clases.md, "Skill points and ranks"). */
    public static void learnSkill(ServerPlayer player, String skillId) {
        Optional<PlayerClass> playerClass = PlayerClassCapability.get(player).flatMap(PlayerClassData::get);
        Optional<ProgressionData> progress = ProgressionCapability.get(player);
        Optional<SkillInfo> skill = SkillCatalog.byId(skillId);
        if (playerClass.isEmpty() || progress.isEmpty() || skill.isEmpty()) return;

        ProgressionData p = progress.get();
        SkillBook.LearnResult result = p.skills().learn(skill.get(), playerClass.get(), p.level(), p.skillPoints());
        if (result == SkillBook.LearnResult.LEARNED) {
            p.spendSkillPoint();
            player.level().playSound(null, player.blockPosition(), SoundEvents.ENCHANTMENT_TABLE_USE, SoundSource.PLAYERS, 0.6f, 1.3f);
            CombatHandler.sync(player); // the Combat Bar slots may have changed
            QuestEngine.event(player, new QuestEvent.SkillsLearned(p.skills().ranks().size()));
        } else {
            player.displayClientMessage(Component.translatable("message.sofe.learn." + result.name().toLowerCase(Locale.ROOT),
                    Component.translatable(skill.get().translationKey()), p.skills().levelForNextRank(skill.get())), true);
        }
        sync(player);
    }

    public static void spendAttribute(ServerPlayer player, CharacterAttribute attribute) {
        ProgressionCapability.get(player).ifPresent(p -> {
            if (PlayerClassCapability.get(player).map(PlayerClassData::hasClass).orElse(false) && p.spendAttributePoint()) {
                p.attributes().add(attribute);
                CombatHandler.refresh(player); // resource maximum, regeneration and health follow the sheet
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
        ProgressionCapability.get(player).ifPresent(p -> {
            Optional<PlayerClass> playerClass = PlayerClassCapability.get(player).flatMap(PlayerClassData::get);
            SyncProgressPacket.Derived derived = CharacterStats.effects(player).map(fx -> new SyncProgressPacket.Derived(
                    player.getMaxHealth(), (float) fx.physicalDamageMultiplier(), (float) fx.magicDamageMultiplier(),
                    (float) fx.critChance(), (float) fx.dodgeChance(), (float) fx.regenMultiplier(),
                    playerClass.map(c -> fx.maxResourceBonus(c.resource())).orElse(0))).orElse(SyncProgressPacket.Derived.NONE);
            SoFENetwork.sendTo(player, new SyncProgressPacket(p.level(), p.xp(),
                    ProgressionRulesManager.leveling().xpToNext(p.level()), p.skillPoints(), p.attributePoints(),
                    p.skills().ranks(), p.skills().slots().stream().map(s -> s == null ? "" : s).toList(),
                    p.attributes().addedPoints(), derived));
        });
    }
}
