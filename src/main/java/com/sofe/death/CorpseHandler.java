package com.sofe.death;

import com.sofe.config.SoFEConfig;
import com.sofe.entity.BearerCorpseEntity;
import com.sofe.quest.QuestEngine;
import com.sofe.registry.EntityRegistry;
import com.sofe.world.SoFEWorld;
import com.sofe.world.lock.RegionEnforcer;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingDeathEvent;

/**
 * Death in a journey (docs/Jugabilidad.md, G1): the gear stays on a {@link BearerCorpseEntity} where
 * the player fell. Off with keepInventory, in hardcore, outside journeys, with corpseSystem = false, or when a grave
 * mod is installed ({@link #GRAVE_MODS}: theirs takes care of the items).
 */
public final class CorpseHandler {
    /** Grave and corpse mods that keep a dead player's items their own way (docs/Jugabilidad.md, "Other mods"). */
    public static final java.util.List<String> GRAVE_MODS = java.util.List.of("corpse", "gravestone", "tombstone", "yigd");

    public static boolean graveModInstalled() {
        return GRAVE_MODS.stream().anyMatch(net.minecraftforge.fml.ModList.get()::isLoaded);
    }

    private CorpseHandler() {
    }

    public static boolean appliesTo(ServerPlayer player) {
        return SoFEWorld.isJourney(player.server)
                && SoFEConfig.SERVER.corpseSystem.get()
                && !graveModInstalled()
                && !player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)
                && !player.level().getLevelData().isHardcore()
                && !player.isSpectator();
    }

    /** Runs before the drops, so vanilla finds an empty inventory and drops nothing. */
    public static void onDeath(LivingDeathEvent event) {
        if (event.isCanceled() || !(event.getEntity() instanceof ServerPlayer player)) return;
        // story items are bound to the soul: they never drop and never stay on the body
        if (!player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)) com.sofe.item.Soulbound.keepOnDeath(player);
        if (!appliesTo(player) || player.getInventory().isEmpty()) return;
        leaveCorpse(player);
    }

    public static BearerCorpseEntity leaveCorpse(ServerPlayer player) {
        ServerLevel level = player.serverLevel();
        Vec3 at = player.position();
        // Lava, the void or a wall: the body appears at the last safe place instead
        if (player.isInLava() || player.getY() < level.getMinBuildHeight() || player.isInWall()) {
            at = RegionEnforcer.lastSafe(player).orElse(at);
        }
        BearerCorpseEntity corpse = new BearerCorpseEntity(EntityRegistry.BEARER_CORPSE.get(), level);
        corpse.setPos(at.x, Math.max(at.y, level.getMinBuildHeight() + 1), at.z);
        corpse.setYRot(player.getYRot());
        corpse.takeFrom(player);
        level.addFreshEntity(corpse);
        CorpseRegistry.get(player.server).add(player.getUUID(), corpse.getUUID(), level.dimension(), corpse.blockPosition());
        player.sendSystemMessage(Component.translatable("message.sofe.corpse.left",
                corpse.blockPosition().getX(), corpse.blockPosition().getY(), corpse.blockPosition().getZ()).withStyle(ChatFormatting.GRAY));
        QuestEngine.sync(player);
        return corpse;
    }
}
