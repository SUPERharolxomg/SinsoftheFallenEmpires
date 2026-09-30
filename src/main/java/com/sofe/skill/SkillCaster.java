package com.sofe.skill;

import com.sofe.combat.CombatCapability;
import com.sofe.combat.CombatData;
import com.sofe.combat.CombatHandler;
import com.sofe.combat.ResourcePool;
import com.sofe.player.PlayerClass;
import com.sofe.player.PlayerClassCapability;
import com.sofe.player.PlayerClassData;
import com.sofe.skill.data.ClassSkillData;
import com.sofe.skill.data.SkillDataManager;
import com.sofe.skill.data.SkillStats;
import com.sofe.skill.sorceress.Constellations;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;

import java.util.List;
import java.util.Locale;
import java.util.Optional;

/**
 * Casts the skill in a Combat Bar slot (UC-02). The server decides everything: the player's
 * class, the skill in the slot, cooldown and resource. Refusals are shown on the action bar.
 */
public final class SkillCaster {

    private SkillCaster() {
    }

    public static void cast(ServerPlayer player, int slot) {
        if (player.isSpectator() || !player.isAlive()) return;
        Optional<PlayerClass> playerClass = PlayerClassCapability.get(player).flatMap(PlayerClassData::get);
        Optional<CombatData> combat = CombatCapability.get(player);
        if (playerClass.isEmpty() || combat.isEmpty()) return;

        List<SkillInfo> loadout = SkillCatalog.defaultLoadout(playerClass.get());
        if (slot < 0 || slot >= loadout.size()) {
            tell(player, Component.translatable("message.sofe.skill.empty_slot"));
            return;
        }
        SkillInfo info = loadout.get(slot);
        Component name = Component.translatable(info.translationKey());
        Optional<Skill> skill = SkillRegistry.get(info.id());
        Optional<ClassSkillData> classData = SkillDataManager.forClass(info.owner());
        Optional<SkillStats> stats = classData.flatMap(d -> d.skill(info.id()));
        Optional<ResourcePool> resource = combat.get().resource();
        if (skill.isEmpty() || stats.isEmpty() || resource.isEmpty()) {
            tell(player, Component.translatable("message.sofe.skill.not_ready", name));
            return;
        }

        long now = player.level().getGameTime();
        CombatData data = combat.get();
        if (!data.cooldowns().isReady(info.id(), now)) {
            String seconds = String.format(Locale.ROOT, "%.1f", data.cooldowns().remaining(info.id(), now) / 20.0);
            tell(player, Component.translatable("message.sofe.skill.cooldown", name, seconds));
            return;
        }
        if (!resource.get().spend(stats.get().cost())) {
            tell(player, Component.translatable("message.sofe.skill.no_resource",
                    Component.translatable(info.owner().resource().translationKey())));
            return;
        }
        data.cooldowns().start(info.id(), now, stats.get().cooldownTicks());

        Skill.Result result = skill.get().cast(new Skill.Context(player, info, stats.get(), data, classData.get()));
        result.rune().flatMap(rune -> data.runes().add(rune)).ifPresent(done ->
                Constellations.trigger(player, done.constellation(), done.runes(), result.impact(), classData.get()));

        data.markDirty();
        CombatHandler.sync(player);
    }

    private static void tell(ServerPlayer player, Component message) {
        player.displayClientMessage(message, true);
    }
}
