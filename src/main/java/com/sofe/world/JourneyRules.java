package com.sofe.world;

import com.sofe.SoFEMod;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.animal.horse.TraderLlama;
import net.minecraft.world.entity.npc.AbstractVillager;
import net.minecraft.world.level.GameRules;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.server.ServerStartedEvent;

/**
 * World rules of a journey: no vanilla villagers or wandering traders (the merchants are SoFE NPCs,
 * docs/Anexos.md), and Peaceful is replaced by Easy because the story needs enemies (docs/Jugabilidad.md, G6).
 */
public final class JourneyRules {
    private static final int DIFFICULTY_CHECK_TICKS = 40;

    private JourneyRules() {
    }

    /** Villagers, wandering traders and their llamas never join a journey world, however they appear. */
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !SoFEWorld.isJourney(level.getServer())) return;
        Entity entity = event.getEntity();
        if (entity instanceof AbstractVillager || entity instanceof TraderLlama) {
            event.setCanceled(true);
        }
    }

    public static void onServerStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        if (!SoFEWorld.isJourney(server)) return;
        server.getGameRules().getRule(GameRules.RULE_DO_TRADER_SPAWNING).set(false, server);
        replacePeaceful(server);
    }

    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.getServer().getTickCount() % DIFFICULTY_CHECK_TICKS != 0) return;
        if (SoFEWorld.isJourney(event.getServer())) replacePeaceful(event.getServer());
    }

    private static void replacePeaceful(MinecraftServer server) {
        if (server.getWorldData().getDifficulty() != Difficulty.PEACEFUL) return;
        server.setDifficulty(Difficulty.EASY, true);
        server.getPlayerList().broadcastSystemMessage(Component.translatable("message.sofe.no_peaceful").withStyle(ChatFormatting.GOLD), false);
        SoFEMod.LOGGER.info("Peaceful is not allowed in a journey; switched to Easy");
    }
}
