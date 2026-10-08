package com.sofe.quest;

import com.sofe.SoFEMod;
import com.sofe.config.SoFEConfig;
import com.sofe.death.CorpseRegistry;
import com.sofe.network.RegionLayoutPacket;
import com.sofe.network.SoFENetwork;
import com.sofe.player.ClassSelectionHandler;
import com.sofe.network.SyncStoryPacket;
import com.sofe.progression.ProgressionHandler;
import com.sofe.story.PlayerProgressView;
import com.sofe.story.RegionFates;
import com.sofe.story.StoryCapability;
import com.sofe.story.StoryProgress;
import com.sofe.world.SoFEWorld;
import com.sofe.world.region.Region;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * The server side of quests (UC-05): turns game events into {@link QuestEvent}s, runs the effects
 * that {@link QuestLogic} returns and keeps the Journal and the Quest Compass in sync.
 */
public final class QuestEngine {
    /** The Act I main quest, started when the player becomes a Bearer. */
    public static final String FIRST_QUEST = "sofe:act1_eclipse";
    /** Favor a finished quest earns with the empire where it ends (docs/Anexos.md, "Favor per empire"). */
    public static final int QUEST_FAVOR = 80;

    /** Favor with the empire of the land the player stands in (Sulthari when it is no empire's). */
    public static void favorHere(ServerPlayer player, int points) {
        String empire = com.sofe.world.SoFEWorld.regionMap(player.server)
                .map(m -> m.regionAt(player.getBlockX(), player.getBlockZ()))
                .filter(r -> r != com.sofe.world.region.Region.OCEAN).map(r -> r.id()).orElse("sulthari");
        com.sofe.economy.MerchantService.gainFavor(player, empire, points);
    }
    private static final int MAX_EFFECT_DEPTH = 16; // a dialogue that starts a quest that opens a dialogue...
    private static final int POSITION_CHECK_TICKS = 20;
    private static final double BOSS_WAKE_RADIUS = 24;

    private QuestEngine() {
    }

    public static void onBearerChosen(ServerPlayer player) {
        startQuest(player, FIRST_QUEST);
    }

    public static boolean startQuest(ServerPlayer player, String questId) {
        return startQuest(player, questId, 0);
    }

    private static boolean startQuest(ServerPlayer player, String questId, int depth) {
        Optional<QuestDefinition> quest = StoryDataManager.quest(questId);
        Optional<StoryProgress> story = StoryCapability.get(player);
        if (quest.isEmpty() || story.isEmpty()) {
            if (quest.isEmpty()) SoFEMod.LOGGER.warn("Unknown quest {}", questId);
            return false;
        }
        boolean wasStarted = story.get().quest(questId).isPresent();
        List<QuestEffect> effects = QuestLogic.start(story.get(), quest.get(), PlayerProgressView.of(player));
        boolean started = !wasStarted && story.get().quest(questId).isPresent();
        if (started) {
            if (quest.get().type() == QuestDefinition.Type.MAIN) story.get().track(questId); // the story leads: its next chapter is followed at once
            player.sendSystemMessage(Component.translatable("message.sofe.quest_started",
                    Component.translatable(quest.get().translationKey())).withStyle(ChatFormatting.GOLD));
            player.level().playSound(null, player.blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.PLAYERS, 1f, 0.9f);
        }
        run(player, effects, depth + 1);
        sync(player);
        return started;
    }

    public static void event(ServerPlayer player, QuestEvent event) {
        StoryCapability.get(player).ifPresent(story -> {
            Map<String, StoryProgress.QuestState> before = story.quests();
            Optional<String> trackedBefore = story.trackedQuest();
            List<QuestEffect> effects = QuestLogic.record(story, StoryDataManager.quests(), event);
            if (!before.equals(story.quests())) {
                announceCompleted(player, before, story);
                followTheStory(story, trackedBefore);
                run(player, effects, 0);
                sync(player);
            }
        });
    }

    public static void advance(ServerPlayer player, String questId) {
        advance(player, questId, 0);
    }

    private static void advance(ServerPlayer player, String questId, int depth) {
        Optional<QuestDefinition> quest = StoryDataManager.quest(questId);
        Optional<StoryProgress> story = StoryCapability.get(player);
        if (quest.isEmpty() || story.isEmpty()) return;
        Map<String, StoryProgress.QuestState> before = story.get().quests();
        Optional<String> trackedBefore = story.get().trackedQuest();
        List<QuestEffect> effects = QuestLogic.advance(story.get(), quest.get());
        announceCompleted(player, before, story.get());
        followTheStory(story.get(), trackedBefore);
        run(player, effects, depth + 1);
        sync(player);
    }

    /**
     * When the quest the player followed is finished, the compass goes back to the main story (the next
     * chapter if it has begun), not to whichever quest happens to come first.
     */
    static void followTheStory(StoryProgress story, Optional<String> trackedBefore) {
        if (trackedBefore.isPresent() && !story.quest(trackedBefore.get()).map(StoryProgress.QuestState::completed).orElse(false)) return;
        story.quests().entrySet().stream()
                .filter(e -> !e.getValue().completed())
                .filter(e -> StoryDataManager.quest(e.getKey()).map(q -> q.type() == QuestDefinition.Type.MAIN).orElse(false))
                .map(Map.Entry::getKey).sorted().findFirst()
                .ifPresent(story::track);
    }

    private static void announceCompleted(ServerPlayer player, Map<String, StoryProgress.QuestState> before, StoryProgress story) {
        story.quests().forEach((id, state) -> {
            StoryProgress.QuestState old = before.get(id);
            if (state.completed() && (old == null || !old.completed())) {
                favorHere(player, QUEST_FAVOR);
                StoryDataManager.quest(id).ifPresent(q -> player.sendSystemMessage(Component.translatable("message.sofe.quest_completed",
                        Component.translatable(q.translationKey())).withStyle(ChatFormatting.GOLD)));
                player.level().playSound(null, player.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 0.7f, 1f);
            } else if (old != null && state.step() > old.step()) {
                player.level().playSound(null, player.blockPosition(), SoundEvents.EXPERIENCE_ORB_PICKUP, SoundSource.PLAYERS, 0.6f, 1.4f);
            }
        });
    }

    /** Runs effects in order, for quests and dialogue answers alike. */
    public static void run(ServerPlayer player, List<QuestEffect> effects) {
        run(player, effects, 0);
    }

    static void run(ServerPlayer player, List<QuestEffect> effects, int depth) {
        if (effects.isEmpty()) return;
        if (depth > MAX_EFFECT_DEPTH) {
            SoFEMod.LOGGER.error("Quest effects nested more than {} deep for {}; stopping (a loop in the quest files?)",
                    MAX_EFFECT_DEPTH, player.getGameProfile().getName());
            return;
        }
        for (QuestEffect effect : new ArrayList<>(effects)) {
            runOne(player, effect, depth);
        }
    }

    private static void runOne(ServerPlayer player, QuestEffect effect, int depth) {
        StoryProgress story = StoryCapability.get(player).orElse(null);
        if (story == null) return;
        if (effect instanceof QuestEffect.StartQuest e) {
            startQuest(player, e.quest(), depth);
        } else if (effect instanceof QuestEffect.AdvanceQuest e) {
            advance(player, e.quest(), depth);
        } else if (effect instanceof QuestEffect.SetFate e) {
            Region region = Region.byId(e.region()).orElse(null);
            if (region == null || !RegionFates.isValid(region, e.fate())) {
                SoFEMod.LOGGER.warn("Unknown fate {} for region {}", e.fate(), e.region());
            } else if (story.setFate(region, e.fate())) {
                player.sendSystemMessage(Component.translatable("message.sofe.fate_set",
                        Component.translatable(region.translationKey()),
                        Component.translatable(RegionFates.translationKey(region, e.fate()))).withStyle(ChatFormatting.LIGHT_PURPLE));
            }
        } else if (effect instanceof QuestEffect.AdvanceAct e) {
            if (story.advanceTo(e.act())) {
                com.sofe.story.SoFEAdvancements.award(player, "story/act" + story.act());
                player.sendSystemMessage(Component.translatable("message.sofe.act_begins",
                        Component.translatable("act.sofe." + story.act())).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
                player.level().playSound(null, player.blockPosition(), SoundEvents.UI_TOAST_CHALLENGE_COMPLETE, SoundSource.PLAYERS, 1f, 0.8f);
            }
        } else if (effect instanceof QuestEffect.GiveXp e) {
            ProgressionHandler.addXp(player, e.amount());
        } else if (effect instanceof QuestEffect.Spawn e) {
            spawn(player, e);
        } else if (effect instanceof QuestEffect.OpenDialogue e) {
            DialogueService.open(player, e.dialogue(), null);
        } else if (effect instanceof QuestEffect.PlaySound e) {
            SoundEvent sound = ForgeRegistries.SOUND_EVENTS.getValue(ResourceLocation.tryParse(e.sound()));
            if (sound != null) player.level().playSound(null, player.blockPosition(), sound, SoundSource.AMBIENT, e.volume(), e.pitch());
        } else if (effect instanceof QuestEffect.Particles e) {
            var type = ForgeRegistries.PARTICLE_TYPES.getValue(ResourceLocation.tryParse(e.particle()));
            if (type instanceof net.minecraft.core.particles.SimpleParticleType simple) {
                player.serverLevel().sendParticles(simple, player.getX(), player.getY() + 1, player.getZ(), e.count(), 0.6, 1.0, 0.6, 0.05);
            }
        } else if (effect instanceof QuestEffect.GiveRelic e) {
            com.sofe.gear.GearMaker.relic(e.relic(), player).ifPresentOrElse(stack -> {
                if (!player.getInventory().add(stack)) player.drop(stack, false);
                player.sendSystemMessage(Component.translatable("message.sofe.relic_received", stack.getHoverName()).withStyle(ChatFormatting.GOLD));
            }, () -> SoFEMod.LOGGER.warn("Unknown relic {} in a quest effect", e.relic()));
        } else if (effect instanceof QuestEffect.HireCompanion e) {
            com.sofe.player.PlayerClass.byId(e.bearer()).ifPresent(c -> com.sofe.companion.Companions.hire(player, c));
        } else if (effect instanceof QuestEffect.HireSoldier e) {
            com.sofe.world.region.Region.byId(e.empire()).ifPresent(empire ->
                    com.sofe.entity.army.Army.hire(player, empire, com.sofe.entity.army.SoldierEntity.Rank.byId(e.rank())));
        } else if (effect instanceof QuestEffect.ReviveSoldiers) {
            com.sofe.entity.army.Army.revive(player);
        } else if (effect instanceof QuestEffect.CompanionOrder e) {
            com.sofe.companion.Companions.order(player, e.order());
        } else if (effect instanceof QuestEffect.AwardAdvancement e) {
            com.sofe.story.SoFEAdvancements.award(player, e.advancement());
        } else if (effect instanceof QuestEffect.PlayScene e) {
            SceneService.play(player, e.scene(), e.then());
        } else if (effect instanceof QuestEffect.OpenClassSelect) {
            ClassSelectionHandler.openIfNeeded(player);
        } else if (effect instanceof QuestEffect.Travel e) {
            travel(player, e.waystone());
        } else if (effect instanceof QuestEffect.GiveItem e) {
            Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(e.item()));
            if (item == null) {
                SoFEMod.LOGGER.warn("Unknown item {} in a quest effect", e.item());
            } else if (!player.getInventory().add(new ItemStack(item, e.count()))) {
                player.drop(new ItemStack(item, e.count()), false);
            }
        }
    }

    /** To a Waystone of the layout: beside it on its ground, and it is woken for the player (they can travel back). */
    private static void travel(ServerPlayer player, String waystone) {
        BlockPos at = com.sofe.world.zone.StructurePositions.get().waystones().get(waystone);
        if (at == null) {
            SoFEMod.LOGGER.warn("Unknown waystone {} in a quest effect", waystone);
            return;
        }
        ServerLevel level = player.server.overworld();
        BlockPos stone = com.sofe.world.Grounding.groundFloor(level, at.getX(), at.getZ());
        for (int dy = -3; dy <= 3; dy++) { // the Waystone block itself, to wake it
            BlockPos p = stone.offset(0, dy, 0);
            if (level.getBlockState(p).is(com.sofe.registry.SoFEBlocks.WAYSTONE.get())) {
                com.sofe.travel.WaystoneService.activate(player, p);
                break;
            }
        }
        com.sofe.travel.WaystoneService.teleport(player, com.sofe.world.Grounding.groundFloor(level, at.getX() + 2, at.getZ() + 2));
    }

    private static void spawn(ServerPlayer player, QuestEffect.Spawn spawn) {
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.tryParse(spawn.entity()));
        if (type == null) {
            SoFEMod.LOGGER.warn("Unknown entity {} in a quest effect", spawn.entity());
            return;
        }
        ServerLevel level = player.serverLevel();
        for (int i = 0; i < spawn.count(); i++) {
            double angle = level.random.nextDouble() * Math.PI * 2;
            double distance = spawn.radius() * (0.5 + level.random.nextDouble() * 0.5);
            int x = (int) Math.floor(player.getX() + Math.cos(angle) * distance);
            int z = (int) Math.floor(player.getZ() + Math.sin(angle) * distance);
            BlockPos pos = com.sofe.world.Grounding.roomFor(level, type, com.sofe.world.Grounding.beside(level, x, z, player), 8);
            Entity entity = type.spawn(level, pos, MobSpawnType.EVENT);
            if (entity instanceof Mob mob) {
                // the step's foes are the Bearer's: they count whoever strikes them (a companion, a soldier), and they
                // stay until they fall (keepFoes calls them again if they are all gone)
                mob.addTag(VoidInvasion.forTag(player.getUUID()));
                mob.addTag(FOE);
                mob.setPersistenceRequired();
                if (spawn.elite()) com.sofe.mob.EliteMobs.make(mob, StoryCapability.get(player).map(StoryProgress::act).orElse(1), level.random);
                mob.setTarget(player);
            }
        }
    }

    // --- game events ---

    /**
     * A kill counts for the Bearer who struck it, and also for the Bearer a creature came for (an invasion's, a lair's
     * lord), whoever struck it: the garrison fights beside them. A lord counts as "lord:&lt;entity&gt;".
     */
    public static void onKill(LivingDeathEvent event) {
        var dead = event.getEntity();
        if (dead.level().isClientSide) return;
        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(dead.getType());
        if (id == null) return;
        String killed = (dead.getTags().contains(CryptLord.TAG) ? Objective.LORD : "") + id;
        ServerPlayer striker = event.getSource().getEntity() instanceof ServerPlayer p ? p : null;
        if (striker != null) event(striker, new QuestEvent.Killed(killed));
        for (String tag : dead.getTags()) {
            if (!tag.startsWith(VoidInvasion.FOR)) continue;
            try {
                ServerPlayer owner = dead.getServer().getPlayerList().getPlayer(java.util.UUID.fromString(tag.substring(VoidInvasion.FOR.length())));
                if (owner != null && owner != striker) event(owner, new QuestEvent.Killed(killed));
            } catch (IllegalArgumentException ignored) {
                // not a player's id
            }
        }
    }

    /** Called by boss entities on every participant when they fall. */
    public static void bossDefeated(ServerPlayer player, String bossId) {
        StoryCapability.get(player).ifPresent(story -> story.defeat(bossId));
        com.sofe.story.SoFEAdvancements.award(player, com.sofe.story.SoFEAdvancements.bossPath(bossId));
        event(player, new QuestEvent.BossDefeated(bossId));
        sync(player);
    }

    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        if (player.tickCount % POSITION_CHECK_TICKS != 0) return;
        event(player, new QuestEvent.At(player.getBlockX(), player.getBlockZ()));
        if (com.sofe.world.SoFEWorld.isJourney(player.server)) discover(player);
        skipDone(player);
        checkCarried(player);
        wakeBosses(player);
        VoidInvasion.tick(player);
        CryptLord.wake(player);
        keepFoes(player);
    }

    /**
     * A step already done before it came moves on by itself: a boss this Bearer has beaten, runes they have solved.
     * When new steps come into an act (a Ruin before its bosses), a Bearer who had gone on is not asked twice.
     */
    public static void skipDone(ServerPlayer player) {
        StoryProgress story = StoryCapability.get(player).orElse(null);
        if (story == null) return;
        for (var entry : story.quests().entrySet()) {
            String id = entry.getKey();
            for (int guard = 0; guard < 16; guard++) { // a few steps at once, never forever
                var state = story.quest(id).filter(s -> !s.completed());
                var step = state.flatMap(s -> StoryDataManager.quest(id).flatMap(q -> q.step(s.step())));
                if (step.isEmpty()) break;
                Objective o = step.get().objective();
                boolean done = o instanceof Objective.DefeatBoss b && story.hasDefeated(b.boss())
                        || o instanceof Objective.SolvePuzzle z && story.hasSolved(z.puzzle());
                if (!done) break;
                advance(player, id);
            }
        }
    }

    /** A dungeon quest begins by itself when a Bearer comes near its dungeon, once its act has come. */
    public static void discover(ServerPlayer player) {
        StoryProgress story = StoryCapability.get(player).orElse(null);
        if (story == null) return;
        for (QuestDefinition quest : StoryDataManager.quests().values()) {
            QuestDefinition.Discovery near = quest.discovery();
            if (near == null || story.quest(quest.id()).isPresent()) continue;
            double dx = player.getX() - near.x(), dz = player.getZ() - near.z();
            if (dx * dx + dz * dz > (double) near.radius() * near.radius()) continue;
            if (quest.requires() != null && !quest.requires().test(PlayerProgressView.of(player))) continue;
            startQuest(player, quest.id());
        }
    }

    /** Counts what the player carries toward an "obtain_item" step, if one is active (every second, and in tests). */
    public static void checkCarried(ServerPlayer player) {
        if (wantsItem(player)) event(player, new QuestEvent.Carries(carried(player)));
    }

    /** Whether an active quest step asks the player to carry an item (only then is the pack looked through). */
    private static boolean wantsItem(ServerPlayer player) {
        return StoryCapability.get(player).map(story -> story.quests().entrySet().stream().anyMatch(en -> !en.getValue().completed()
                && StoryDataManager.quest(en.getKey()).flatMap(q -> q.step(en.getValue().step()))
                .map(s -> s.objective() instanceof Objective.Obtain).orElse(false))).orElse(false);
    }

    private static java.util.Set<String> carried(ServerPlayer player) {
        java.util.Set<String> ids = new java.util.HashSet<>();
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack s = player.getInventory().getItem(i);
            if (!s.isEmpty()) ids.add(String.valueOf(ForgeRegistries.ITEMS.getKey(s.getItem())));
        }
        return ids;
    }

    /**
     * A "defeat_boss" step with a place wakes its boss when the player arrives there, and again if the
     * boss is gone before it was beaten (a reset, a /kill). The boss id is also its entity type.
     */
    private static void wakeBosses(ServerPlayer player) {
        StoryCapability.get(player).ifPresent(story -> story.quests().forEach((id, state) -> {
            if (state.completed()) return;
            StoryDataManager.quest(id).flatMap(q -> q.step(state.step())).ifPresent(step -> {
                if (!(step.objective() instanceof Objective.DefeatBoss boss) || step.compassTarget().isEmpty()) return;
                QuestDefinition.Target at = step.compassTarget().get();
                double dx = player.getX() - at.x(), dz = player.getZ() - at.z();
                if (dx * dx + dz * dz > BOSS_WAKE_RADIUS * BOSS_WAKE_RADIUS) return;
                EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.tryParse(boss.boss()));
                if (type == null) return;
                var near = player.serverLevel().getEntities(type, player.getBoundingBox().inflate(64), e -> e.isAlive());
                if (near.isEmpty()) {
                    // in its own hall, the step's place: the ground floor of the building there (the Observatory's hall,
                    // not its stairs or its dome), where its whole body fits
                    ServerLevel level = player.serverLevel();
                    BlockPos hall = com.sofe.world.Grounding.roomFor(level, type, com.sofe.world.Grounding.groundFloor(level, at.x(), at.z()), 12);
                    if (type.spawn(level, hall, MobSpawnType.EVENT) instanceof Mob mob) {
                        mob.setPersistenceRequired();
                        mob.setTarget(player);
                    }
                    player.displayClientMessage(Component.translatable("message.sofe.boss_awakens",
                            Component.translatable("npc.sofe." + boss.boss().substring(boss.boss().indexOf(':') + 1))).withStyle(ChatFormatting.RED), true);
                }
            });
        }));
    }

    /** The tag of the foes a quest step called for a Bearer; how long none must stand before they are called again, and how near its place. */
    public static final String FOE = "sofe_foe";
    public static final int FOES_AGAIN_TICKS = 20 * 10, FOES_NEAR = 64;
    private static final Map<String, Long> FOES_GONE_SINCE = new java.util.HashMap<>();

    /**
     * A step that asks to kill the foes it called (a Ruin's guardian, a Bearer's quest) never hangs: when none of them
     * stands any more (they despawned, fell in lava, were lost in an unloaded land) and the step still asks for more, they
     * are called again near the Bearer after a little while (near the step's place, when it has one).
     */
    public static void keepFoes(ServerPlayer player) {
        StoryProgress story = StoryCapability.get(player).orElse(null);
        if (story == null || !player.isAlive() || player.isSpectator()) return;
        ServerLevel level = player.serverLevel();
        String mine = VoidInvasion.forTag(player.getUUID());
        story.quests().forEach((id, state) -> {
            if (state.completed()) return;
            StoryDataManager.quest(id).flatMap(q -> q.step(state.step())).ifPresent(step -> {
                if (!(step.objective() instanceof Objective.Kill kill) || step.invasion() != null || step.lair() != null || step.haunt() != null) return;
                QuestEffect.Spawn spawn = step.onStart().stream().filter(e -> e instanceof QuestEffect.Spawn s && QuestLogic.matches(kill.entity(), s.entity()))
                        .map(e -> (QuestEffect.Spawn) e).findFirst().orElse(null);
                int remaining = kill.count() - state.count();
                if (spawn == null || remaining <= 0) return;
                String key = player.getUUID() + "|" + id + "|" + state.step();
                if (step.target() != null) {
                    double dx = player.getX() - step.target().x(), dz = player.getZ() - step.target().z();
                    if (dx * dx + dz * dz > (double) FOES_NEAR * FOES_NEAR) return;
                }
                boolean standing = !level.getEntities(net.minecraft.world.level.entity.EntityTypeTest.forClass(Mob.class),
                        m -> m.isAlive() && m.getTags().contains(FOE) && m.getTags().contains(mine)).isEmpty();
                long now = level.getGameTime();
                if (standing) {
                    FOES_GONE_SINCE.remove(key);
                    return;
                }
                long since = FOES_GONE_SINCE.computeIfAbsent(key, k -> now);
                if (now - since < FOES_AGAIN_TICKS) return;
                FOES_GONE_SINCE.remove(key);
                spawn(player, new QuestEffect.Spawn(spawn.entity(), Math.min(remaining, spawn.count()), spawn.radius(), spawn.elite()));
            });
        });
    }

    /** Joining a journey also sends the region layout, which the client needs for the Seal Veil. */
    public static void onLogin(PlayerEvent.PlayerLoggedInEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) {
            SoFEWorld.regionMap(player.server).ifPresent(map -> SoFENetwork.sendTo(player, new RegionLayoutPacket(map.bounds())));
            sync(player);
        }
    }

    public static void onRespawn(PlayerEvent.PlayerRespawnEvent event) {
        if (event.getEntity() instanceof ServerPlayer player) sync(player);
    }

    // --- sync ---

    public static void sync(ServerPlayer player) {
        Optional<SyncStoryPacket.Target> corpse = CorpseRegistry.get(player.server).latest(player.getUUID())
                .filter(c -> c.dimension() == net.minecraft.world.level.Level.OVERWORLD)
                .map(c -> new SyncStoryPacket.Target(c.pos().getX(), c.pos().getZ()));
        StoryCapability.get(player).ifPresent(story -> grantMilestones(player, story));
        StoryCapability.get(player).ifPresent(story -> SoFENetwork.sendTo(player, packet(story, QuestGuide.target(player, story), corpse,
                SoFEConfig.SERVER.opsBypass.get() && player.hasPermissions(2))));
    }

    /**
     * Every step done grants its hidden advancement sofe:quest/&lt;quest&gt;/step&lt;n&gt; (no toast, no chat, not on the
     * advancement screen), so a quest book such as the modpack's FTB Quests can tick the step
     * (scripts/make_quest_book.py). Steps done before this existed are granted on the next sync.
     */
    public static void grantMilestones(ServerPlayer player, StoryProgress story) {
        story.quests().forEach((id, state) -> StoryDataManager.quest(id).ifPresent(q -> {
            int done = state.completed() ? q.steps().size() : state.step();
            String path = "quest/" + id.substring(id.indexOf(':') + 1) + "/step";
            for (int n = 1; n <= done; n++) com.sofe.story.SoFEAdvancements.award(player, path + n);
        }));
    }

    public static SyncStoryPacket packet(StoryProgress story, Optional<SyncStoryPacket.Target> compass, Optional<SyncStoryPacket.Target> corpse,
                                         boolean bypassLocks) {
        List<SyncStoryPacket.Quest> quests = new ArrayList<>();
        story.quests().forEach((id, state) -> StoryDataManager.quest(id).ifPresent(q -> {
            int step = Math.min(state.step(), q.steps().size() - 1);
            quests.add(new SyncStoryPacket.Quest(id, q.type().name().toLowerCase(java.util.Locale.ROOT), step, q.steps().size(),
                    state.count(), q.steps().get(step).objective().required(), state.completed()));
        }));
        quests.sort(java.util.Comparator.comparing(SyncStoryPacket.Quest::type).thenComparing(SyncStoryPacket.Quest::id));
        Map<String, String> fates = new java.util.TreeMap<>();
        story.fates().forEach((r, f) -> fates.put(r.id(), f));
        List<String> open = RegionFates.open(story).stream().map(Region::id).toList();
        return new SyncStoryPacket(story.act(), quests, story.trackedQuest(), fates, open, compass, corpse, bypassLocks,
                story.bosses().stream().sorted().toList());
    }
}
