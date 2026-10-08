package com.sofe.world.lair;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.sofe.SoFEMod;
import com.sofe.story.StoryCapability;
import com.sofe.world.SoFEWorld;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimplePreparableReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Where each boss waits (data/sofe/boss_lairs.json). When a Bearer who has not beaten it yet steps into its
 * lair, the boss rises in the middle of the room, once: it is not raised again while it lives nearby, nor for
 * a while after it was raised (a lost fight, a reset). Bearers who have all beaten it walk through an empty
 * arena. The floor is looked for from the Bearer's own height, so a boss room inside a hall gets its boss on
 * the floor, not on the roof.
 * <p>
 * After the campaign (UC-33) a Bearer who has beaten Nahrazel can call the Echo of any boss they have beaten: kneeling
 * (crouching) in the middle of its lair for a few seconds raises it again, whole, for a fight that gives gems,
 * materials and Dinars once more, but no Relic they already have (SoFEBossEntity.onlyNewRelics).
 */
public final class BossLairs {
    public static final ResourceLocation FILE = SoFEMod.id("boss_lairs.json");
    private static final int CHECK_TICKS = 40;
    public static final int RESPAWN_TICKS = 600;

    /**
     * @param radius how near the middle (horizontally) a Bearer must come to wake the boss
     * @param y      for a lair high up or deep down (the Celestial Spire, the Inverted Throne): its floor, and a Bearer
     *               must be within 12 blocks of it; Integer.MIN_VALUE for a lair on the ground
     */
    public record Lair(String boss, int x, int z, int radius, int y) {
        public Lair(String boss, int x, int z, int radius) {
            this(boss, x, z, radius, Integer.MIN_VALUE);
        }

        public boolean hasHeight() {
            return y != Integer.MIN_VALUE;
        }
    }

    /** How near the middle a Bearer kneels to call an Echo, and for how many checks (of {@link #CHECK_TICKS}). */
    public static final double ECHO_CALL_RADIUS = 4;
    public static final int ECHO_KNEEL_CHECKS = 3;
    private static final Map<java.util.UUID, Integer> KNEELING = new HashMap<>();

    private static volatile List<Lair> lairs = List.of();
    private static final Map<String, Long> LAST_RAISED = new HashMap<>();

    private BossLairs() {
    }

    public static List<Lair> lairs() {
        return lairs;
    }

    public static Optional<Lair> lair(String boss) {
        return lairs.stream().filter(l -> l.boss().equals(boss)).findFirst();
    }

    public static void set(List<Lair> newLairs) {
        lairs = List.copyOf(newLairs);
    }

    public static List<Lair> parse(JsonObject json) {
        List<Lair> out = new ArrayList<>();
        for (Map.Entry<String, JsonElement> e : json.getAsJsonObject("lairs").entrySet()) {
            JsonObject l = e.getValue().getAsJsonObject();
            out.add(new Lair(e.getKey(), l.get("x").getAsInt(), l.get("z").getAsInt(), l.has("radius") ? l.get("radius").getAsInt() : 20,
                    l.has("y") ? l.get("y").getAsInt() : Integer.MIN_VALUE));
        }
        return List.copyOf(out);
    }

    public static void onServerTick(TickEvent.ServerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || event.getServer().getTickCount() % CHECK_TICKS != 0) return;
        if (!SoFEWorld.isJourney(event.getServer())) return;
        ServerLevel level = event.getServer().overworld();
        for (Lair lair : lairs) {
            wake(level, lair);
            kneel(level, lair, level.players());
        }
    }

    /** Counts the Bearers kneeling in a lair's middle; one who has knelt long enough calls its boss's Echo. */
    public static Optional<Entity> kneel(ServerLevel level, Lair lair, List<? extends ServerPlayer> players) {
        for (ServerPlayer p : players) {
            boolean kneeling = p.isCrouching() && p.isAlive() && !p.isSpectator()
                    && sq(p.getX() - lair.x() - 0.5) + sq(p.getZ() - lair.z() - 0.5) <= sq(ECHO_CALL_RADIUS)
                    && (!lair.hasHeight() || Math.abs(p.getY() - lair.y()) <= 12)
                    && StoryCapability.get(p).map(s -> s.finishedCampaign() && s.hasDefeated(lair.boss())).orElse(false);
            if (!kneeling) {
                if (KNEELING.containsKey(p.getUUID()) && sq(p.getX() - lair.x() - 0.5) + sq(p.getZ() - lair.z() - 0.5) <= sq(ECHO_CALL_RADIUS * 3)) {
                    KNEELING.remove(p.getUUID());
                }
                continue;
            }
            int knelt = KNEELING.merge(p.getUUID(), 1, Integer::sum);
            if (knelt < ECHO_KNEEL_CHECKS) {
                name(lair).ifPresent(n -> p.displayClientMessage(Component.translatable("message.sofe.echo_kneel", n).withStyle(ChatFormatting.LIGHT_PURPLE), true));
                continue;
            }
            KNEELING.remove(p.getUUID());
            Optional<Entity> echo = callEcho(level, lair, p);
            if (echo.isPresent()) return echo;
        }
        return Optional.empty();
    }

    /** Raises the Echo of a lair's boss for this Bearer, if none stands there and it did not just rise. */
    public static Optional<Entity> callEcho(ServerLevel level, Lair lair, ServerPlayer caller) {
        Optional<Entity> boss = raise(level, lair, (int) Math.floor(caller.getY()));
        boss.ifPresent(b -> level.players().stream()
                .filter(p -> sq(p.getX() - lair.x()) + sq(p.getZ() - lair.z()) <= sq(lair.radius() + 16))
                .forEach(p -> p.displayClientMessage(Component.translatable("message.sofe.echo_rises", b.getDisplayName())
                        .withStyle(ChatFormatting.DARK_PURPLE), true)));
        if (boss.isPresent() && !level.players().contains(caller)) {
            caller.displayClientMessage(Component.translatable("message.sofe.echo_rises", boss.get().getDisplayName()), true);
        }
        return boss;
    }

    private static Optional<Component> name(Lair lair) {
        ResourceLocation id = ResourceLocation.tryParse(lair.boss());
        if (id == null || !ForgeRegistries.ENTITY_TYPES.containsKey(id)) return Optional.empty();
        return Optional.of(ForgeRegistries.ENTITY_TYPES.getValue(id).getDescription());
    }

    /** Raises the boss of this lair if a Bearer who still has to beat it is inside; returns the boss raised. */
    public static Optional<Entity> wake(ServerLevel level, Lair lair) {
        return wake(level, lair, level.players());
    }

    /** The same, among these players (GameTests use fake players, which are not in the level's list). */
    public static Optional<Entity> wake(ServerLevel level, Lair lair, List<? extends ServerPlayer> players) {
        List<ServerPlayer> inside = players.stream()
                .filter(p -> !p.isSpectator() && p.isAlive())
                .filter(p -> sq(p.getX() - lair.x() - 0.5) + sq(p.getZ() - lair.z() - 0.5) <= sq(lair.radius()))
                .filter(p -> !lair.hasHeight() || Math.abs(p.getY() - lair.y()) <= 12)
                .map(p -> (ServerPlayer) p)
                .toList();
        if (inside.isEmpty()) return Optional.empty();
        boolean someoneOwes = inside.stream().anyMatch(p -> !StoryCapability.get(p).map(s -> s.hasDefeated(lair.boss())).orElse(false));
        if (!someoneOwes) return Optional.empty();
        Optional<Entity> raised = raise(level, lair, (int) Math.floor(inside.get(0).getY()));
        raised.ifPresent(boss -> inside.forEach(p -> p.displayClientMessage(Component.translatable("message.sofe.lair_awakens", boss.getDisplayName())
                .withStyle(ChatFormatting.DARK_RED), true)));
        return raised;
    }

    /** The boss of a lair, risen in its middle, unless one stands there already or it rose a moment ago. */
    private static Optional<Entity> raise(ServerLevel level, Lair lair, int fromY) {
        ResourceLocation id = ResourceLocation.tryParse(lair.boss());
        if (id == null || !ForgeRegistries.ENTITY_TYPES.containsKey(id)) return Optional.empty(); // a boss of a later sprint, or a typo
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(id);
        double reach = lair.radius() + 48;
        AABB around = new AABB(lair.x() - reach, level.getMinBuildHeight(), lair.z() - reach,
                lair.x() + reach, level.getMaxBuildHeight(), lair.z() + reach);
        var standing = level.getEntities(type, around, Entity::isAlive);
        if (!standing.isEmpty()) {
            // one that stands away from its hall (risen on the rock over it, wandered off) and is not fighting goes back
            Entity boss = standing.get(0);
            boolean away = sq(boss.getX() - lair.x()) + sq(boss.getZ() - lair.z()) > sq(lair.radius()) || Math.abs(boss.getY() - fromY) > 8;
            boolean fighting = boss instanceof com.sofe.entity.boss.SoFEBossEntity b && b.isFighting();
            if (away && !fighting) {
                BlockPos home = spot(level, type, lair, fromY);
                boss.teleportTo(home.getX() + 0.5, home.getY(), home.getZ() + 0.5);
                return Optional.of(boss);
            }
            return Optional.empty();
        }
        long now = level.getGameTime();
        Long last = LAST_RAISED.get(lair.boss());
        if (last != null && now >= last && now - last < RESPAWN_TICKS) return Optional.empty();
        BlockPos at = spot(level, type, lair, fromY);
        Entity boss = type.spawn(level, at, MobSpawnType.EVENT);
        if (boss == null) return Optional.empty();
        if (boss instanceof Mob mob) mob.setPersistenceRequired();
        LAST_RAISED.put(lair.boss(), now);
        return Optional.of(boss);
    }

    /**
     * A survey of every lair (a development check, PlaceShots' "survey"): for each boss, where it would rise for a Bearer
     * standing on its hall's floor (the floor of the Sealed Gate nearest it, or the lair's own height), whether its body
     * fits there, how far that is from the floor and from the lair's middle, and whether the sky is over it.
     */
    public static List<String> survey(ServerLevel level) {
        List<String> out = new java.util.ArrayList<>();
        for (Lair lair : lairs) {
            ResourceLocation id = ResourceLocation.tryParse(lair.boss());
            if (id == null || !ForgeRegistries.ENTITY_TYPES.containsKey(id)) continue;
            EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(id);
            int fromY;
            if (lair.hasHeight()) fromY = lair.y();
            else {
                var gate = com.sofe.world.zone.StructurePositions.get().gates().values().stream()
                        .min(java.util.Comparator.comparingDouble(g -> sq(g.x() - lair.x()) + sq(g.z() - lair.z()))).orElse(null);
                fromY = Integer.MIN_VALUE;
                if (gate != null && sq(gate.x() - lair.x()) + sq(gate.z() - lair.z()) < sq(90)) {
                    level.getChunk(gate.x() >> 4, gate.z() >> 4);
                    for (int y = level.getMinBuildHeight(); y < level.getMaxBuildHeight() && fromY == Integer.MIN_VALUE; y++) {
                        if (level.getBlockState(new BlockPos(gate.x(), y, gate.z())).is(com.sofe.registry.SoFEBlocks.SEALED_GATE.get())) fromY = y;
                    }
                }
                if (fromY == Integer.MIN_VALUE) fromY = level.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING_NO_LEAVES, lair.x(), lair.z());
            }
            BlockPos at = spot(level, type, lair, fromY);
            boolean fits = com.sofe.world.Grounding.fits(level, type, at);
            double across = Math.sqrt(sq(at.getX() + 0.5 - lair.x()) + sq(at.getZ() + 0.5 - lair.z()));
            boolean sky = level.canSeeSky(at);
            boolean ok = fits && Math.abs(at.getY() - fromY) <= 4 && across <= lair.radius();
            out.add(String.format("%s %s floor=%d at=%s fits=%s dy=%d across=%.1f/%d sky=%s", ok ? "OK " : "BAD", lair.boss(), fromY,
                    at.toShortString(), fits, at.getY() - fromY, across, lair.radius(), sky));
        }
        return out;
    }

    /**
     * Where a lair's boss rises: on the hall's floor (the Bearer's own), nearest the lair's middle where its whole body
     * fits; a dais, an anvil, a throne or the scales in the middle are stood beside, not on. The first floor found up or
     * down the column when the hall's floor has no room at all.
     */
    static BlockPos spot(ServerLevel level, EntityType<?> type, Lair lair, int fromY) {
        int y = lair.hasHeight() ? lair.y() : fromY;
        for (int r = 0; r <= 12; r++) {
            for (int dx = -r; dx <= r; dx++) {
                for (int dz = -r; dz <= r; dz++) {
                    if (Math.max(Math.abs(dx), Math.abs(dz)) != r) continue;
                    for (int dy : new int[]{0, 1, -1, 2, -2}) {
                        BlockPos p = new BlockPos(lair.x() + dx, y + dy, lair.z() + dz);
                        if (com.sofe.world.Grounding.fits(level, type, p)) return p;
                    }
                }
            }
        }
        return com.sofe.world.Grounding.roomFor(level, type, floor(level, lair.x(), y, lair.z()), 10);
    }

    /** The first floor with two blocks of air above it, looking down from a little above the Bearer, then up. */
    static BlockPos floor(ServerLevel level, int x, int fromY, int z) {
        level.getChunk(x >> 4, z >> 4);
        BlockPos.MutableBlockPos pos = new BlockPos.MutableBlockPos();
        for (int y = fromY + 3; y > fromY - 12; y--) {
            if (standable(level, pos.set(x, y, z))) return pos.immutable();
        }
        for (int y = fromY + 4; y < fromY + 24; y++) {
            if (standable(level, pos.set(x, y, z))) return pos.immutable();
        }
        return new BlockPos(x, fromY, z);
    }

    private static boolean standable(ServerLevel level, BlockPos pos) {
        return level.getBlockState(pos).isAir() && level.getBlockState(pos.above()).isAir() && level.getBlockState(pos.below()).isSolid();
    }

    private static double sq(double v) {
        return v * v;
    }

    /** For GameTests: forget when each lair last raised its boss. */
    public static void forget() {
        LAST_RAISED.clear();
        KNEELING.clear();
    }

    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new SimplePreparableReloadListener<List<Lair>>() {
            @Override
            protected List<Lair> prepare(ResourceManager resources, ProfilerFiller profiler) {
                var resource = resources.getResource(FILE);
                if (resource.isEmpty()) return List.of();
                try (Reader reader = resource.get().openAsReader()) {
                    return parse(JsonParser.parseReader(reader).getAsJsonObject());
                } catch (Exception e) {
                    SoFEMod.LOGGER.error("Could not read {}: {}", FILE, e.getMessage());
                    return List.of();
                }
            }

            @Override
            protected void apply(List<Lair> loaded, ResourceManager resources, ProfilerFiller profiler) {
                lairs = loaded;
                SoFEMod.LOGGER.info("Loaded {} boss lairs", loaded.size());
            }
        });
    }
}
