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
 * the player fell. Off with keepInventory, in hardcore, outside journeys, or with
 * corpseSystem = false (for servers that use a grave mod).
 */
public final class CorpseHandler {

    private CorpseHandler() {
    }

    public static boolean appliesTo(ServerPlayer player) {
        return SoFEWorld.isJourney(player.server)
                && SoFEConfig.SERVER.corpseSystem.get()
                && !player.level().getGameRules().getBoolean(GameRules.RULE_KEEPINVENTORY)
                && !player.level().getLevelData().isHardcore()
                && !player.isSpectator();
    }

    /** Runs before the drops, so vanilla finds an empty inventory and drops nothing. */
    public static void onDeath(LivingDeathEvent event) {
        if (event.isCanceled() || !(event.getEntity() instanceof ServerPlayer player) || !appliesTo(player)) return;
        if (player.getInventory().isEmpty()) return;
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
