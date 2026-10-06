package com.sofe.world;

import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.sofe.SoFEMod;
import com.sofe.world.lair.BossLairs;
import com.sofe.world.region.RegionMap;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStoppedEvent;
import net.minecraftforge.fml.ModList;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.WeakHashMap;

/**
 * Free mode (docs/Jugabilidad.md, "Free mode"; UC-34). A journey whose story cannot run goes to free mode: the mod's
 * items, gear, ores and stations still work, but the story, the locks, the lairs and the journey's rules stop, because
 * {@link SoFEWorld#regionMap} answers as for a world that is no journey. That happens when a mod that breaks the story
 * is installed (data/sofe/compat/incompatible_mods.json), or when the integrity check on load finds the world's map of
 * Aetheris or a story boss missing. Nothing is saved: once the cause is gone, the story goes on. Every player is told
 * why when they join, and so is a player in a world that was never a journey.
 */
public final class FreeMode {
    public static final String INCOMPATIBLE_LIST = "/data/sofe/compat/incompatible_mods.json";
    private static final Map<MinecraftServer, Component> FOUND_ON_LOAD = new WeakHashMap<>();
    private static List<String> incompatible;

    private FreeMode() {
    }

    /** The mod ids listed as breaking the story (read once from the mod's own jar). */
    public static synchronized List<String> incompatibleList() {
        if (incompatible == null) {
            List<String> ids = new ArrayList<>();
            try (InputStream in = FreeMode.class.getResourceAsStream(INCOMPATIBLE_LIST)) {
                if (in != null) {
                    JsonArray mods = JsonParser.parseReader(new InputStreamReader(in, StandardCharsets.UTF_8)).getAsJsonObject().getAsJsonArray("mods");
                    mods.forEach(m -> ids.add(m.getAsString()));
                }
            } catch (Exception e) {
                SoFEMod.LOGGER.error("Could not read {}: {}", INCOMPATIBLE_LIST, e.getMessage());
            }
            incompatible = List.copyOf(ids);
        }
        return incompatible;
    }

    /** The listed mods that are installed (the title screen warns of them before a journey begins). */
    public static List<String> incompatibleInstalled() {
        return incompatibleList().stream().filter(ModList.get()::isLoaded).toList();
    }

    /** Why this journey is in free mode, if it is; {@code raw} is the world's own map of Aetheris. */
    public static Optional<Component> reason(MinecraftServer server, RegionMap raw) {
        List<String> breaking = incompatibleInstalled();
        if (!breaking.isEmpty() && !com.sofe.config.SoFEConfig.SERVER.allowIncompatibleMods.get()) return Optional.of(Component.translatable("message.sofe.free_mode.incompatible", String.join(", ", breaking)));
        return Optional.ofNullable(FOUND_ON_LOAD.get(server));
    }

    /** The integrity check, once the world and its data are loaded: the map of Aetheris and every story boss. */
    public static Optional<Component> check(RegionMap raw) {
        if (raw.bounds().isEmpty()) return Optional.of(Component.translatable("message.sofe.free_mode.no_layout"));
        if (BossLairs.lairs().isEmpty()) return Optional.of(Component.translatable("message.sofe.free_mode.no_story"));
        for (BossLairs.Lair lair : BossLairs.lairs()) {
            ResourceLocation id = ResourceLocation.tryParse(lair.boss());
            if (id == null || !ForgeRegistries.ENTITY_TYPES.containsKey(id)) {
                return Optional.of(Component.translatable("message.sofe.free_mode.missing_boss", lair.boss()));
            }
        }
        return Optional.empty();
    }

    public static void onServerStarted(ServerStartedEvent event) {
        MinecraftServer server = event.getServer();
        SoFEWorld.rawRegionMap(server).ifPresent(raw -> {
            check(raw).ifPresent(why -> FOUND_ON_LOAD.put(server, why));
            reason(server, raw).ifPresentOrElse(why -> SoFEMod.LOGGER.warn("This journey is in SoFE free mode: {}", why.getString()),
                    () -> SoFEMod.LOGGER.info("Journey integrity check passed"));
        });
    }

    public static void onServerStopped(ServerStoppedEvent event) {
        FOUND_ON_LOAD.remove(event.getServer());
    }

    /** A player joining a world in free mode, or a world that is no journey, is told so once. */
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player)) return;
        Optional<RegionMap> raw = SoFEWorld.rawRegionMap(player.server);
        Optional<Component> why = raw.isEmpty() ? Optional.of(Component.translatable("message.sofe.free_mode.not_journey"))
                : reason(player.server, raw.get());
        why.ifPresent(w -> player.sendSystemMessage(Component.translatable("message.sofe.free_mode", w).withStyle(ChatFormatting.GRAY)));
    }
}
