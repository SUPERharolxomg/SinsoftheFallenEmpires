package com.sofe.mob;

import com.sofe.registry.EntityRegistry;
import com.sofe.story.StoryCapability;
import com.sofe.world.SoFEWorld;
import com.sofe.world.zone.ProtectedZone;
import com.sofe.world.zone.ProtectedZoneData;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraftforge.event.TickEvent;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.UUID;

/**
 * Hordes of the Void: every seventh day of the world, as night falls, each Bearer who is far from every city and
 * camp has a 60% chance of being hunted by a horde of Void creatures, up to 30, coming in three waves from the dark
 * round them. Bearers in a city are safe. Only in a journey (where the cities are). Pure numbers in
 * {@link #size} and {@link #isHordeNight} are unit tested.
 */
public final class VoidHordes {
    public static final int EVERY_DAYS = 7, NIGHTFALL = 13_000, MAX = 30, WAVES = 3, WAVE_GAP = 200;
    public static final double CHANCE = 0.6;
    /** How far from a city or camp a Bearer must be for a horde to find them. */
    public static final int SAFE_DISTANCE = 300;
    public static final String TAG = "sofe_horde";

    private record Wave(UUID player, int count, long at) {
    }

    private static final List<Wave> PENDING = new ArrayList<>();
    private static final java.util.Map<UUID, List<Mob>> HUNTING = new java.util.HashMap<>();

    private VoidHordes() {
    }

    /** Whether this moment of the world is the nightfall of a horde day (day 7, 14, 21...). */
    public static boolean isHordeNight(long dayTime) {
        long day = dayTime / 24_000;
        return dayTime % 24_000 == NIGHTFALL && day % EVERY_DAYS == EVERY_DAYS - 1;
    }

    /** How many come for a Bearer: 10, and 3 more for each act after the first and for each Bearer with them, up to 30. */
    public static int size(int act, int companions) {
        return Math.min(MAX, 10 + 3 * (Math.max(1, act) - 1) + 3 * Math.max(0, companions));
    }

    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END) return;
        ServerLevel level = event.getServer().overworld();
        long time = level.getDayTime();
        if (isHordeNight(time) && SoFEWorld.isJourney(event.getServer())) {
            for (ServerPlayer player : level.players()) {
                if (player.isSpectator() || player.isCreative() || nearSafety(level, player)) continue;
                if (level.getRandom().nextDouble() >= CHANCE) continue;
                start(level, player);
            }
        }
        long now = level.getGameTime();
        for (Iterator<Wave> it = PENDING.iterator(); it.hasNext(); ) {
            Wave wave = it.next();
            if (wave.at() > now) continue;
            it.remove();
            ServerPlayer player = event.getServer().getPlayerList().getPlayer(wave.player());
            if (player != null && player.level() == level && player.isAlive()) spawnWave(level, player, wave.count());
        }
    }

    /** A city or camp within the safe distance. */
    static boolean nearSafety(ServerLevel level, ServerPlayer player) {
        for (ProtectedZone zone : ProtectedZoneData.get(level.getServer()).zones()) {
            if (zone.kind() != ProtectedZone.Kind.CITY && zone.kind() != ProtectedZone.Kind.CAMP && zone.kind() != ProtectedZone.Kind.HOMESTEAD) continue;
            double cx = (zone.minX() + zone.maxX()) / 2.0, cz = (zone.minZ() + zone.maxZ()) / 2.0;
            double reach = SAFE_DISTANCE + Math.max(zone.maxX() - zone.minX(), zone.maxZ() - zone.minZ()) / 2.0;
            if (Math.abs(player.getX() - cx) <= reach && Math.abs(player.getZ() - cz) <= reach) return true;
        }
        return false;
    }

    /** The horn sounds, the warning shows, and the waves are set to come. */
    public static void start(ServerLevel level, ServerPlayer player) {
        int act = StoryCapability.get(player).map(s -> s.act()).orElse(1);
        int others = level.getEntitiesOfClass(ServerPlayer.class, player.getBoundingBox().inflate(32), p -> p != player).size();
        int total = size(act, others);
        player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("message.sofe.horde.title").withStyle(ChatFormatting.DARK_PURPLE)));
        player.connection.send(new ClientboundSetSubtitleTextPacket(Component.translatable("message.sofe.horde.subtitle").withStyle(ChatFormatting.GRAY)));
        level.playSound(null, player.blockPosition(), SoundEvents.RAID_HORN.get(), SoundSource.HOSTILE, 64f, 0.8f);
        long now = level.getGameTime();
        int left = total;
        for (int i = 0; i < WAVES; i++) {
            int count = i == WAVES - 1 ? left : total / WAVES;
            left -= count;
            PENDING.add(new Wave(player.getUUID(), count, now + 60 + (long) i * WAVE_GAP));
        }
    }

    /** One wave: Void Wretches and Stalkers rising round the Bearer, 18 to 30 blocks away, hunting them. */
    public static List<Mob> spawnWave(ServerLevel level, ServerPlayer player, int count) {
        List<Mob> out = new ArrayList<>();
        RandomSource random = level.getRandom();
        // the horde that already hunts this Bearer counts against the thirty
        List<Mob> hunting = HUNTING.computeIfAbsent(player.getUUID(), k -> new ArrayList<>());
        hunting.removeIf(m -> !m.isAlive());
        count = Math.min(count, MAX - hunting.size());
        for (int i = 0; i < count; i++) {
            double angle = random.nextDouble() * Math.PI * 2, distance = 18 + random.nextDouble() * 12;
            int x = (int) Math.floor(player.getX() + Math.cos(angle) * distance), z = (int) Math.floor(player.getZ() + Math.sin(angle) * distance);
            BlockPos at = com.sofe.world.Grounding.beside(level, x, z, player); // on the ground, or the floor of the hall the Bearer is in
            int roll = random.nextInt(10); // the horde: zombies and skeletons of the Void, Wretches and Stalkers
            EntityType<? extends Mob> type = roll < 4 ? EntityRegistry.VOID_ZOMBIE.get() : roll < 6 ? EntityRegistry.VOID_SKELETON.get()
                    : roll < 8 ? EntityRegistry.VOID_WRETCH.get() : EntityRegistry.VOID_STALKER.get();
            Mob mob = type.spawn(level, at, MobSpawnType.EVENT);
            if (mob == null) continue;
            mob.getPersistentData().putBoolean(TAG, true);
            mob.setPersistenceRequired();
            mob.setTarget(player);
            out.add(mob);
            hunting.add(mob);
        }
        return out;
    }

    /** For GameTests: forget the waves still to come. */
    public static void forget() {
        PENDING.clear();
        HUNTING.clear();
    }
}
