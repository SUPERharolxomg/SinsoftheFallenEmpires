package com.sofe.quest;

import com.sofe.story.StoryCapability;
import com.sofe.network.InvasionHudPacket;
import com.sofe.network.SoFENetwork;
import com.sofe.story.StoryProgress;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.TagParser;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket;
import net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * The Void attacks a place in waves (a step's "invasion", QuestDefinition.Invasion): Sulthari in Act I, a street or a
 * camp in the smaller quests. Each wave opens a rift on one or more sides of the place (east, then west...), a portal
 * the creatures pour out of until the wave's count has fallen; then a short breath, and the next wave comes from
 * elsewhere, bigger. Three soldiers of the land come to hold each rift; a counter in the top right corner of the
 * screen counts the fallen (InvasionHudOverlay), the Quest Compass points at the nearest rift, and in the last wave an
 * elite leads out of each rift. The rifts are block displays, so they never touch the city's blocks.
 * <p>
 * Every creature that falls counts for the Bearer it came for, whoever struck it (QuestEngine.onKill): the town's
 * garrison fights beside them.
 */
public final class VoidInvasion {
    /** The tag of every creature of an invasion, of every rift's display, and the tag naming the Bearer they came for. */
    public static final String INVADER = "sofe_invader", RIFT = "sofe_rift", FOR = "sofe_for_", GUARD = "sofe_rift_guard";
    /** The soldiers who hold each rift: two soldiers and an archer. */
    static final List<com.sofe.entity.army.SoldierEntity.Rank> RIFT_GUARD = List.of(com.sofe.entity.army.SoldierEntity.Rank.SOLDIER,
            com.sofe.entity.army.SoldierEntity.Rank.SOLDIER, com.sofe.entity.army.SoldierEntity.Rank.ARCHER);
    /** The breath between two waves, and before the first. */
    static final int BREATH = 20 * 8, FIRST_BREATH = 20 * 3;
    /** How many of a rift's creatures are out at once, and how many more in each later wave. */
    static final int AT_ONCE = 3, MORE_EACH_WAVE = 1;
    /** How far from its side's point a rift may open, to stand on the open ground (a street, not a roof). */
    static final int RIFT_RANGE = 6;

    /** One Bearer's invasion in progress. */
    static final class Run {
        final String quest;
        final int step;
        QuestDefinition.Target anchor;
        int wave = -1;
        long openAt;
        boolean opened, announced;
        final List<Rift> rifts = new ArrayList<>();
        /** The soldiers who came to hold each side's rift, by side. */
        final Map<String, List<Entity>> guards = new HashMap<>();
        InvasionHudPacket shown = InvasionHudPacket.NONE;

        Run(String quest, int step) {
            this.quest = quest;
            this.step = step;
        }
    }

    /** A rift: its side, where it stands, the displays that draw it, and whether its elite came out. */
    static final class Rift {
        final String side;
        final BlockPos pos;
        final boolean alongX;
        final List<Entity> displays = new ArrayList<>();
        boolean eliteOut;

        Rift(String side, BlockPos pos, boolean alongX) {
            this.side = side;
            this.pos = pos;
            this.alongX = alongX;
        }
    }

    private static final Map<UUID, Run> RUNS = new HashMap<>();

    private VoidInvasion() {
    }

    /** The tag naming the Bearer a creature came for. */
    public static String forTag(UUID player) {
        return FOR + player;
    }

    /** The invasion step the Bearer is on (the followed quest first), with its quest. */
    static Optional<Map.Entry<String, QuestDefinition.Step>> current(StoryProgress story) {
        List<String> ids = new ArrayList<>(story.quests().keySet());
        ids.sort(null);
        story.trackedQuest().ifPresent(t -> {
            ids.remove(t);
            ids.add(0, t);
        });
        for (String id : ids) {
            var state = story.quest(id).orElse(null);
            if (state == null || state.completed()) continue;
            var step = StoryDataManager.quest(id).flatMap(q -> q.step(state.step())).orElse(null);
            if (step != null && step.invasion() != null) return Optional.of(Map.entry(id, step));
        }
        return Optional.empty();
    }

    /** Every second (QuestEngine.onPlayerTick): opens the waves' rifts and calls up their creatures. */
    public static void tick(ServerPlayer player) {
        StoryProgress story = StoryCapability.get(player).orElse(null);
        if (story == null) return;
        var found = current(story).orElse(null);
        Run run = RUNS.get(player.getUUID());
        if (found == null) {
            if (run != null) end(player, run, true);
            return;
        }
        String questId = found.getKey();
        QuestDefinition.Step step = found.getValue();
        int stepIndex = story.questStep(questId);
        if (run != null && (!run.quest.equals(questId) || run.step != stepIndex)) {
            end(player, run, true);
            run = null;
        }
        if (run == null) {
            run = new Run(questId, stepIndex);
            RUNS.put(player.getUUID(), run);
        }
        QuestDefinition.Invasion invasion = step.invasion();
        QuestDefinition.Target center = invasion.center() != null ? invasion.center() : step.target();
        if (center == null) {
            if (run.anchor == null) run.anchor = new QuestDefinition.Target(player.getBlockX(), player.getBlockZ());
            center = run.anchor;
        }
        double dx = player.getX() - center.x(), dz = player.getZ() - center.z();
        if (dx * dx + dz * dz > (double) invasion.reach() * invasion.reach()) {
            closeRifts(run); // away from the place: the rifts and their soldiers wait for the Bearer to come back
            dismiss(run);
            hideBar(player, run);
            run.wave = -1;
            return;
        }
        int fallen = story.quest(questId).map(StoryProgress.QuestState::count).orElse(0);
        int wave = invasion.waveAt(fallen);
        ServerLevel level = player.serverLevel();
        long now = level.getGameTime();
        if (wave != run.wave) {
            closeRifts(run);
            if (run.wave >= 0 && wave > run.wave) {
                player.displayClientMessage(Component.translatable("invasion.sofe.wave_cleared", run.wave + 1).withStyle(ChatFormatting.GOLD), true);
                level.playSound(null, player.blockPosition(), SoundEvents.PLAYER_LEVELUP, SoundSource.PLAYERS, 0.8f, 0.8f);
                run.openAt = now + BREATH;
            } else {
                run.openAt = now + FIRST_BREATH;
            }
            run.wave = wave;
            run.announced = false;
        }
        showBar(player, run, invasion, fallen, now < run.openAt);
        if (now < run.openAt) return;
        if (!run.announced) {
            open(level, run, invasion, center, wave, player);
            announce(player, invasion, wave);
            run.announced = true;
            run.opened = true;
            QuestEngine.sync(player); // the compass turns to the rifts
        }
        for (Rift rift : run.rifts) draw(level, rift, player.getUUID());
        call(level, player, run, invasion, center, wave, fallen);
    }

    /** Opens the rifts of a wave, each on the open ground nearest its side's point, facing the place. */
    private static void open(ServerLevel level, Run run, QuestDefinition.Invasion invasion, QuestDefinition.Target center, int wave, ServerPlayer player) {
        for (String side : invasion.waves().get(wave).from()) {
            QuestDefinition.Target at = invasion.point(center, side);
            BlockPos pos = com.sofe.entity.army.Army.outdoors(level, at.x(), at.z(), RIFT_RANGE);
            if (pos == null) pos = com.sofe.world.Grounding.groundFloor(level, at.x(), at.z());
            boolean alongX = side.equals("north") || side.equals("south"); // the portal's face turns to the place
            run.rifts.add(new Rift(side, pos, alongX));
            if (!run.guards.containsKey(side)) run.guards.put(side, guard(level, pos, center));
            level.playSound(null, pos, SoundEvents.END_PORTAL_SPAWN, SoundSource.HOSTILE, 1.5f, 0.6f);
        }
    }

    /** "Wave 2", "They attack from the west!" on the Bearer's screen, and the horn. */
    private static void announce(ServerPlayer player, QuestDefinition.Invasion invasion, int wave) {
        boolean last = wave == invasion.waves().size() - 1;
        Component title = last ? Component.translatable("invasion.sofe.last_wave").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD)
                : Component.translatable("invasion.sofe.wave", wave + 1, invasion.waves().size()).withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD);
        Component sub = from(invasion.waves().get(wave).from()).withStyle(ChatFormatting.LIGHT_PURPLE);
        player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 60, 15));
        player.connection.send(new ClientboundSetTitleTextPacket(title));
        player.connection.send(new ClientboundSetSubtitleTextPacket(sub));
        player.sendSystemMessage(Component.translatable("invasion.sofe.chat", title, sub).withStyle(ChatFormatting.LIGHT_PURPLE));
        player.level().playSound(null, player.blockPosition(), SoundEvents.RAID_HORN.value(), SoundSource.HOSTILE, 4f, 1f);
    }

    /** "They attack from the east!", "...from the north and the south!", "...from every side!". */
    static net.minecraft.network.chat.MutableComponent from(List<String> sides) {
        if (sides.size() >= QuestDefinition.Invasion.SIDES.size()) return Component.translatable("invasion.sofe.from_all");
        Component list = Component.translatable("invasion.sofe.side." + sides.get(0));
        for (int i = 1; i < sides.size(); i++) {
            Component next = Component.translatable("invasion.sofe.side." + sides.get(i));
            list = i == sides.size() - 1 ? Component.translatable("invasion.sofe.and", list, next) : Component.translatable("invasion.sofe.comma", list, next);
        }
        return Component.translatable("invasion.sofe.from", list);
    }

    /** Calls up the wave's creatures still to come, out of its rifts, never more than the rifts hold at once. */
    private static void call(ServerLevel level, ServerPlayer player, Run run, QuestDefinition.Invasion invasion, QuestDefinition.Target center, int wave, int fallen) {
        if (run.rifts.isEmpty()) return;
        int remaining = invasion.endOf(wave) - fallen;
        String tag = forTag(player.getUUID());
        int r = invasion.reach() + 64;
        AABB box = new AABB(center.x() - r, level.getMinBuildHeight(), center.z() - r, center.x() + r, level.getMaxBuildHeight(), center.z() + r);
        int alive = level.getEntitiesOfClass(Mob.class, box, m -> m.isAlive() && m.getTags().contains(tag) && m.getTags().contains(INVADER)).size();
        int cap = Math.min(remaining, run.rifts.size() * (AT_ONCE + MORE_EACH_WAVE * wave));
        boolean last = wave == invasion.waves().size() - 1;
        int act = StoryCapability.get(player).map(StoryProgress::act).orElse(1);
        for (int i = 0; alive < cap && i < run.rifts.size() * 2; i++) {
            Rift rift = run.rifts.get(i % run.rifts.size());
            List<String> mobs = invasion.mobs();
            // the stronger kinds come more often as the waves go on
            int pick = level.random.nextInt(10) < 2 + wave ? mobs.size() - 1 : level.random.nextInt(mobs.size());
            EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(mobs.get(pick)));
            if (type == null) return;
            int ox = rift.alongX ? level.random.nextInt(3) - 1 : (center.x() > rift.pos.getX() ? 2 : -2);
            int oz = rift.alongX ? (center.z() > rift.pos.getZ() ? 2 : -2) : level.random.nextInt(3) - 1;
            BlockPos at = com.sofe.world.Grounding.near(level, rift.pos.getX() + ox, rift.pos.getY(), rift.pos.getZ() + oz, 4);
            if (!(type.spawn(level, at, MobSpawnType.EVENT) instanceof Mob mob)) continue;
            mob.setPersistenceRequired(); // they stay until driven out, even when the Bearer steps away
            mob.addTag(tag);
            mob.addTag(INVADER);
            if (last && !rift.eliteOut) {
                com.sofe.mob.EliteMobs.make(mob, act, level.random);
                rift.eliteOut = true;
            }
            if (mob.distanceToSqr(player) < 48 * 48) mob.setTarget(player);
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, mob.getX(), mob.getY() + 1, mob.getZ(), 40, 0.4, 0.9, 0.4, 0.05);
            level.playSound(null, at, SoundEvents.ENDERMAN_TELEPORT, SoundSource.HOSTILE, 0.8f, 0.6f);
            alive++;
        }
    }

    // ------------------------------------------------------------------------------------------------ the rifts

    /**
     * Draws a rift (again, when its displays were unloaded): a frame of crying obsidian round a sheet of the Nether
     * portal, five wide and six high, and the Void's mist round it.
     */
    private static void draw(ServerLevel level, Rift rift, UUID owner) {
        rift.displays.removeIf(Entity::isRemoved);
        if (rift.displays.isEmpty() && level.isLoaded(rift.pos)) {
            String frame = "minecraft:crying_obsidian";
            // u runs along the portal's face, v across it; y up from the ground
            piece(level, rift, owner, "{Name:\"minecraft:nether_portal\",Properties:{axis:\"" + (rift.alongX ? "x" : "z") + "\"}}", -1.5f, 1f, 3f, 4f);
            piece(level, rift, owner, "{Name:\"" + frame + "\"}", -2.5f, 0f, 5f, 1f);
            piece(level, rift, owner, "{Name:\"" + frame + "\"}", -2.5f, 5f, 5f, 1f);
            piece(level, rift, owner, "{Name:\"" + frame + "\"}", -2.5f, 1f, 1f, 4f);
            piece(level, rift, owner, "{Name:\"" + frame + "\"}", 1.5f, 1f, 1f, 4f);
        }
        double x = rift.pos.getX() + 0.5, y = rift.pos.getY() + 3, z = rift.pos.getZ() + 0.5;
        level.sendParticles(ParticleTypes.PORTAL, x, y, z, 30, rift.alongX ? 1.2 : 0.2, 1.6, rift.alongX ? 0.2 : 1.2, 0.4);
        level.sendParticles(ParticleTypes.REVERSE_PORTAL, x, y, z, 12, rift.alongX ? 1.4 : 0.3, 1.8, rift.alongX ? 0.3 : 1.4, 0.02);
        level.sendParticles(ParticleTypes.SQUID_INK, x, rift.pos.getY() + 0.2, z, 4, 1.5, 0.1, 1.5, 0.01);
        if (level.random.nextInt(4) == 0) level.playSound(null, rift.pos, SoundEvents.PORTAL_AMBIENT, SoundSource.HOSTILE, 0.6f, 0.5f);
    }

    /** One block display of a rift, from (u, y) along its face, w wide and h high, one block thick. */
    private static void piece(ServerLevel level, Rift rift, UUID owner, String block, float u, float y, float w, float h) {
        float tx = rift.alongX ? u : -0.5f, tz = rift.alongX ? -0.5f : u, sx = rift.alongX ? w : 1f, sz = rift.alongX ? 1f : w;
        String snbt = "{id:\"minecraft:block_display\",block_state:" + block + ",brightness:{sky:15,block:15},view_range:4f,"
                + "transformation:{left_rotation:[0f,0f,0f,1f],right_rotation:[0f,0f,0f,1f],"
                + "translation:[" + tx + "f," + y + "f," + tz + "f],scale:[" + sx + "f," + h + "f," + sz + "f]}}";
        try {
            CompoundTag tag = TagParser.parseTag(snbt);
            Entity display = EntityType.create(tag, level).orElse(null);
            if (display == null) return;
            display.moveTo(rift.pos.getX() + 0.5, rift.pos.getY(), rift.pos.getZ() + 0.5, 0f, 0f);
            display.addTag(RIFT);
            display.addTag(forTag(owner));
            level.addFreshEntity(display);
            rift.displays.add(display);
        } catch (com.mojang.brigadier.exceptions.CommandSyntaxException e) {
            throw new IllegalStateException(snbt, e);
        }
    }

    /** A rift and two of its creatures, still, for the screenshots (PlaceShots' "rift"). */
    public static void showcase(ServerLevel level, BlockPos pos) {
        Rift rift = new Rift("north", pos, true);
        draw(level, rift, UUID.randomUUID());
        for (int i = 0; i < 2; i++) {
            EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(new ResourceLocation(i == 0 ? "sofe:void_wretch" : "sofe:void_stalker"));
            if (type != null && type.create(level) instanceof Mob mob) {
                mob.moveTo(pos.getX() + (i == 0 ? -1.5 : 2.5), pos.getY(), pos.getZ() + 3.5, 180, 0);
                mob.setNoAi(true);
                level.addFreshEntity(mob);
            }
        }
    }

    private static void closeRifts(Run run) {
        for (Rift rift : run.rifts) rift.displays.forEach(Entity::discard);
        run.rifts.clear();
    }

    // ------------------------------------------------------------------------------------------------ the counter and the soldiers

    /** The counter in the top right corner of the Bearer's screen (InvasionHudOverlay), sent when it changes. */
    private static void showBar(ServerPlayer player, Run run, QuestDefinition.Invasion invasion, int fallen, boolean waiting) {
        InvasionHudPacket now = new InvasionHudPacket(run.wave + 1, invasion.waves().size(), Math.min(fallen, invasion.total()), invasion.total(), waiting);
        if (now.equals(run.shown)) return;
        run.shown = now;
        SoFENetwork.sendTo(player, now);
    }

    private static void hideBar(ServerPlayer player, Run run) {
        if (run.shown.equals(InvasionHudPacket.NONE)) return;
        run.shown = InvasionHudPacket.NONE;
        SoFENetwork.sendTo(player, InvasionHudPacket.NONE);
    }

    /**
     * Three soldiers of the land's empire come to hold a rift: they stand between it and the place, keep near it and
     * fight what comes out, until the invasion is over.
     */
    private static List<Entity> guard(ServerLevel level, BlockPos rift, QuestDefinition.Target center) {
        List<Entity> guards = new ArrayList<>();
        var empire = com.sofe.world.SoFEWorld.regionMap(level.getServer()).map(m -> m.regionAt(rift.getX(), rift.getZ()))
                .filter(r -> r != com.sofe.world.region.Region.OCEAN).orElse(com.sofe.world.region.Region.SULTHARI);
        double dx = center.x() - rift.getX(), dz = center.z() - rift.getZ(), len = Math.max(1, Math.sqrt(dx * dx + dz * dz));
        int i = 0;
        for (var rank : RIFT_GUARD) {
            double side = (i++ - 1) * 2.5;
            int x = (int) Math.round(rift.getX() + dx / len * 5 - dz / len * side), z = (int) Math.round(rift.getZ() + dz / len * 5 + dx / len * side);
            BlockPos at = com.sofe.world.Grounding.near(level, x, rift.getY(), z, 4);
            var soldier = com.sofe.registry.EntityRegistry.SOLDIER.get().create(level);
            if (soldier == null) continue;
            soldier.moveTo(at.getX() + 0.5, at.getY(), at.getZ() + 0.5, (float) Math.toDegrees(Math.atan2(-dx, dz)) + 180f, 0f);
            soldier.enlist(empire, rank);
            soldier.post(rift);
            soldier.addTag(GUARD);
            level.addFreshEntity(soldier);
            level.sendParticles(ParticleTypes.CLOUD, at.getX() + 0.5, at.getY() + 0.5, at.getZ() + 0.5, 6, 0.3, 0.3, 0.3, 0.02);
            guards.add(soldier);
        }
        return guards;
    }

    /** The rifts' soldiers go back to their posts. */
    private static void dismiss(Run run) {
        run.guards.values().forEach(list -> list.forEach(Entity::discard));
        run.guards.clear();
    }

    /** The invasion is over (the step done) or left: the rifts close, their soldiers go, the counter goes; a won one says so, and its stragglers fade. */
    private static void end(ServerPlayer player, Run run, boolean won) {
        boolean wasOn = run.opened;
        closeRifts(run);
        dismiss(run);
        hideBar(player, run);
        RUNS.remove(player.getUUID());
        if (!won || !wasOn) return;
        ServerLevel level = player.serverLevel();
        String tag = forTag(player.getUUID());
        for (Mob straggler : level.getEntities(net.minecraft.world.level.entity.EntityTypeTest.forClass(Mob.class), m -> m.getTags().contains(tag) && m.getTags().contains(INVADER))) {
            level.sendParticles(ParticleTypes.REVERSE_PORTAL, straggler.getX(), straggler.getY() + 1, straggler.getZ(), 30, 0.4, 0.9, 0.4, 0.05);
            straggler.discard();
        }
        player.connection.send(new ClientboundSetTitlesAnimationPacket(10, 60, 20));
        player.connection.send(new ClientboundSetTitleTextPacket(Component.translatable("invasion.sofe.won").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD)));
        player.connection.send(new ClientboundSetSubtitleTextPacket(Component.translatable("invasion.sofe.won_sub").withStyle(ChatFormatting.YELLOW)));
        level.playSound(null, player.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1f, 1f);
    }

    /** Where the Quest Compass leads during an invasion: the rift nearest the Bearer, while one is open. */
    public static Optional<QuestDefinition.Target> compass(ServerPlayer player) {
        Run run = RUNS.get(player.getUUID());
        if (run == null || run.rifts.isEmpty()) return Optional.empty();
        Rift nearest = null;
        double best = Double.MAX_VALUE;
        for (Rift rift : run.rifts) {
            double d = rift.pos.distToCenterSqr(player.position());
            if (d < best) {
                best = d;
                nearest = rift;
            }
        }
        return Optional.of(new QuestDefinition.Target(nearest.pos.getX(), nearest.pos.getZ()));
    }

    /** For GameTests: the sides whose rifts are open for this Bearer, and the wave (from 1; 0 before the first). */
    public static List<String> openSides(ServerPlayer player) {
        Run run = RUNS.get(player.getUUID());
        return run == null ? List.of() : run.rifts.stream().map(r -> r.side).toList();
    }

    /** For GameTests: the soldiers holding this Bearer's rifts. */
    public static int guards(ServerPlayer player) {
        Run run = RUNS.get(player.getUUID());
        return run == null ? 0 : run.guards.values().stream().mapToInt(List::size).sum();
    }

    public static int wave(ServerPlayer player) {
        Run run = RUNS.get(player.getUUID());
        return run == null ? 0 : run.wave + 1;
    }

    /** For GameTests: the breath before the next wave is over now. */
    public static void hurry(ServerPlayer player) {
        Run run = RUNS.get(player.getUUID());
        if (run != null) run.openAt = 0;
    }

    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            Run run = RUNS.remove(player.getUUID());
            if (run != null) {
                closeRifts(run);
                dismiss(run);
            }
        }
    }

    /** A rift's display or soldier saved with its chunk never comes back: the invasion draws its rifts anew. */
    public static void onJoin(EntityJoinLevelEvent event) {
        if (event.loadedFromDisk() && (event.getEntity().getTags().contains(RIFT) || event.getEntity().getTags().contains(GUARD))) event.setCanceled(true);
    }
}
