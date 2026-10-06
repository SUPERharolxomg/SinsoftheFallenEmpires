package com.sofe.entity.boss;

import com.sofe.SoFEMod;
import com.sofe.mob.BossDifficulty;
import com.sofe.quest.DialogueService;
import com.sofe.quest.QuestEngine;
import com.sofe.story.StoryCapability;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerBossEvent;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.BossEvent;
import net.minecraft.world.Difficulty;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.SpawnGroupData;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.storage.loot.LootParams;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.parameters.LootContextParamSets;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * What every SoFE boss shares (docs/Jugabilidad.md, "Boss fights"; UC-05): a boss bar, phases by
 * health, strength by difficulty, a sealed arena, a reset after 30 s with nobody left, credit and
 * personal loot for every participant (damage dealt or 30 s in the arena, docs/Anexos.md A6).
 */
public abstract class SoFEBossEntity extends Monster {
    private static final UUID DIFFICULTY_ID = UUID.fromString("7d0a3c2e-5b6f-4f7e-9a1d-2c3b4e5f6a71");
    private static final UUID PLAYERS_ID = UUID.fromString("3e9f1b72-4c58-4d6a-b0e2-8a7c5d1f9b34");
    /** How many players the boss's health is scaled for (docs/Anexos.md, A5: +60% per extra player, never more damage). */
    private int scaledFor = 1;
    public static final int RESET_AFTER_TICKS = 600;
    /** Favor its fall earns with the empire it held (docs/Anexos.md: Favor comes from liberating a region, too). */
    public static final int BOSS_FAVOR = 150, ARCHSIN_FAVOR = 400;
    /** Standing this long in the arena makes a player a participant, even without hitting. */
    public static final int PRESENCE_TICKS = 600;

    private final ServerBossEvent bossBar;
    private final Set<UUID> participants = new HashSet<>();
    private final Map<UUID, Integer> presence = new HashMap<>();
    /** The last player object seen for each participant, for players the server list does not have (fake players). */
    private final Map<UUID, ServerPlayer> lastSeen = new HashMap<>();
    private final Set<UUID> inside = new HashSet<>();
    private final Set<UUID> greeted = new HashSet<>();
    /** Those who have felt the boss's presence (the title, the darkness) once. */
    private final Set<UUID> awed = new HashSet<>();
    private static final UUID ENRAGE_DAMAGE = UUID.fromString("7d0a3c2e-5b6f-4f7e-9a1d-2c3b4e5f6a72");
    private static final UUID ENRAGE_SPEED = UUID.fromString("7d0a3c2e-5b6f-4f7e-9a1d-2c3b4e5f6a73");
    private static final UUID ENRAGE_ARMOR = UUID.fromString("7d0a3c2e-5b6f-4f7e-9a1d-2c3b4e5f6a74");
    /** Each phase after the first: more damage, speed and armor (docs/Jugabilidad.md, boss fights). */
    public static final double ENRAGE_DAMAGE_PER_PHASE = 0.25, ENRAGE_SPEED_PER_PHASE = 0.12, ENRAGE_ARMOR_PER_PHASE = 4;
    /** How far beyond the arena a Bearer first feels the boss. */
    public static final double PRESENCE_REACH = 14;
    /** The short pause of a phase change, when the boss cannot be hurt. */
    public static final int TRANSITION_TICKS = 30;
    private int transition;
    private Vec3 arenaCenter;
    private boolean fighting;
    private int phase = 1;
    private int emptyTicks;
    /** The phase as the client sees it: a boss past its first phase is drawn broken (its _broken texture, its p2_ bones). */
    private static final net.minecraft.network.syncher.EntityDataAccessor<Integer> SHOWN_PHASE =
            net.minecraft.network.syncher.SynchedEntityData.defineId(SoFEBossEntity.class, net.minecraft.network.syncher.EntityDataSerializers.INT);
    /** True while the boss winds up and delivers its signature attack (the client plays its signature animation). */
    private static final net.minecraft.network.syncher.EntityDataAccessor<Boolean> SIGNING =
            net.minecraft.network.syncher.SynchedEntityData.defineId(SoFEBossEntity.class, net.minecraft.network.syncher.EntityDataSerializers.BOOLEAN);
    /** Ticks after the blow that the signature animation still plays. */
    public static final int SIGNATURE_FOLLOW = 12;
    private int signatureTicks;
    private int signatureCooldown = 160;
    private net.minecraft.world.entity.LivingEntity signatureTarget;

    protected SoFEBossEntity(EntityType<? extends Monster> type, Level level, BossEvent.BossBarColor color) {
        super(type, level);
        this.bossBar = new ServerBossEvent(Component.translatable(type.getDescriptionId()), color, BossEvent.BossBarOverlay.NOTCHED_10);
        this.xpReward = 150;
        setPersistenceRequired();
    }

    /**
     * The default mind of a boss: it closes in and strikes (or, for one that fights from afar, keeps its ground and
     * turns to face its foe), looks at the Bearers and hunts the one who hurt it. Bosses with their own ways override it.
     */
    @Override
    protected void registerGoals() {
        this.goalSelector.addGoal(0, new net.minecraft.world.entity.ai.goal.FloatGoal(this));
        if (fightsInMelee()) this.goalSelector.addGoal(2, new net.minecraft.world.entity.ai.goal.MeleeAttackGoal(this, 1.1, true));
        this.goalSelector.addGoal(7, new net.minecraft.world.entity.ai.goal.LookAtPlayerGoal(this, net.minecraft.world.entity.player.Player.class, 24.0f));
        this.goalSelector.addGoal(8, new net.minecraft.world.entity.ai.goal.RandomLookAroundGoal(this));
        this.targetSelector.addGoal(1, new net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal(this));
        this.targetSelector.addGoal(2, new net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal<>(this,
                net.minecraft.world.entity.player.Player.class, false));
    }

    /** False for a boss that never walks up to strike (it casts from where it stands). */
    protected boolean fightsInMelee() {
        return true;
    }

    public static final String ECHO = "sofe_echo";

    /**
     * An echo: a fallen boss raised again by another (Solrath raises the Broken Oaths, Nahrazel the Archsins). It is
     * weaker, gives no credit, no loot and no coffer, and does not announce itself.
     */
    public boolean isEcho() {
        return getPersistentData().getBoolean(ECHO);
    }

    /** Makes this boss an echo of itself: a third of its health. */
    public void makeEcho() {
        getPersistentData().putBoolean(ECHO, true);
        var health = getAttribute(Attributes.MAX_HEALTH);
        if (health != null) health.setBaseValue(health.getBaseValue() / 3);
        setHealth(getMaxHealth());
    }

    @Override
    protected boolean shouldDropLoot() {
        return !isEcho() && super.shouldDropLoot();
    }

    /** The id used for story credit and conditions, e.g. "sofe:vorath". */
    public abstract String bossId();

    /** Health fractions at which the next phase starts (phase 2 at 0.5 means below half health). */
    protected double[] phaseThresholds() {
        return new double[]{0.5};
    }

    /** Called once when a new phase begins (2, 3...). */
    protected void onPhase(int newPhase, ServerLevel level, List<ServerPlayer> fighters) {
    }

    /** The boss's own mechanics, every server tick while players are in the arena. */
    protected abstract void fightTick(ServerLevel level, List<ServerPlayer> fighters);

    /** Undo the boss's mechanics when the fight resets. */
    protected void onReset() {
    }

    /** The line under the boss's name when a Bearer first feels its presence (its Law, its Sin), or null. */
    protected Component presenceSubtitle() {
        return null;
    }

    /** A cinematic dialogue played to each player the first time they enter the fight, or null. */
    protected String introDialogue() {
        return null;
    }

    /** Radius of the sealed arena around where the fight began. */
    protected double arenaRadius() {
        return 24;
    }

    public int phase() {
        return phase;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(SHOWN_PHASE, 1);
        entityData.define(SIGNING, false);
    }

    /** Whether the client should play the signature animation now. */
    public boolean signing() {
        return entityData.get(SIGNING);
    }

    // --- the signature attack: every boss has one blow of its own, born of the weapon it carries

    /**
     * A boss's own attack: its name (signature.sofe.&lt;id&gt;, shown to the Bearers as it begins), how long the
     * wind-up lasts (the Bearers can read it and get out of the way), how long until the next one, and how near a
     * Bearer must be for the boss to start it.
     */
    public record Signature(String id, int windup, int cooldown, double range) {
    }

    /** This boss's signature attack, or null for one that has none. */
    protected Signature signature() {
        return null;
    }

    /** Each tick of the wind-up (tick from 1): the warning the Bearers see (particles, sounds, marks). */
    protected void signatureWindup(ServerLevel level, net.minecraft.world.entity.LivingEntity target, int tick, List<ServerPlayer> fighters) {
    }

    /** The blow itself, at the end of the wind-up. */
    protected void signatureStrike(ServerLevel level, net.minecraft.world.entity.LivingEntity target, List<ServerPlayer> fighters) {
    }

    /** Damage for a signature blow: a multiple of the boss's own attack damage, so it grows with its level. */
    protected float signatureDamage(double times) {
        return (float) (getAttributeValue(Attributes.ATTACK_DAMAGE) * times);
    }

    /** Starts the signature attack at once (tests). */
    public void startSignature(net.minecraft.world.entity.LivingEntity target) {
        Signature sig = signature();
        if (sig == null) return;
        signatureTarget = target;
        signatureTicks = sig.windup() + SIGNATURE_FOLLOW;
        entityData.set(SIGNING, true);
    }

    /** The whole signature attack at once, its first warning and its blow (tests); false for a boss without one. */
    public boolean signatureNow(ServerLevel level, net.minecraft.world.entity.LivingEntity target, List<ServerPlayer> fighters) {
        Signature sig = signature();
        if (sig == null) return false;
        markEchoes(target, fighters);
        signatureWindup(level, target, 1, fighters);
        signatureStrike(level, target, fighters);
        strikeEchoes(level, fighters);
        return true;
    }

    // --- with more Bearers, the signature reaches more of them (docs/Anexos.md, A5: "some attacks start targeting
    // several players at once"): each Bearer but the target, up to three, gets a mark of its own under their feet

    /** The marks' reach, their blow (times the boss's attack, less than the signature's) and how many at most. */
    public static final double ECHO_RADIUS = 2.5, ECHO_DAMAGE = 1.2;
    public static final int MAX_ECHOES = 3;
    private final List<net.minecraft.world.phys.Vec3> signatureEchoes = new java.util.ArrayList<>();
    private static final net.minecraft.core.particles.DustParticleOptions ECHO_MOTE =
            new net.minecraft.core.particles.DustParticleOptions(new org.joml.Vector3f(0.85f, 0.1f, 0.12f), 1.3f);

    /** Where the signature's echoes will fall: under the Bearers farthest from its target, an echo boss marks none. */
    private void markEchoes(net.minecraft.world.entity.LivingEntity target, List<ServerPlayer> fighters) {
        signatureEchoes.clear();
        if (isEcho() || fighters.size() < 2) return;
        fighters.stream().filter(p -> p != target && p.isAlive())
                .sorted(java.util.Comparator.comparingDouble((ServerPlayer p) -> target == null ? 0 : -p.distanceToSqr(target)))
                .limit(MAX_ECHOES).forEach(p -> signatureEchoes.add(p.position()));
    }

    /** The marks of the echoes still to fall (tests). */
    public List<net.minecraft.world.phys.Vec3> signatureEchoes() {
        return List.copyOf(signatureEchoes);
    }

    private void drawEchoes(ServerLevel level) {
        for (var at : signatureEchoes) Signatures.drawRing(level, at, ECHO_RADIUS, ECHO_MOTE);
    }

    private void strikeEchoes(ServerLevel level, List<ServerPlayer> fighters) {
        for (var at : signatureEchoes) {
            for (ServerPlayer p : Signatures.ring(at, ECHO_RADIUS, fighters)) Signatures.strike(this, p, signatureDamage(ECHO_DAMAGE), at, 0.6, 0.3);
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.EXPLOSION, at.x, at.y + 0.3, at.z, 1, 0, 0, 0, 0);
            level.sendParticles(ECHO_MOTE, at.x, at.y + 0.5, at.z, 24, ECHO_RADIUS / 2, 0.3, ECHO_RADIUS / 2, 0);
        }
        if (!signatureEchoes.isEmpty()) {
            level.playSound(null, blockPosition(), net.minecraft.sounds.SoundEvents.GENERIC_EXPLODE, net.minecraft.sounds.SoundSource.HOSTILE, 0.8f, 1.3f);
        }
        signatureEchoes.clear();
    }

    /** Whether the boss is in the middle of its signature attack. */
    public boolean isSigning() {
        return signatureTicks > 0;
    }

    private void signatureTick(ServerLevel level, List<ServerPlayer> fighters) {
        Signature sig = signature();
        if (sig == null) return;
        if (signatureTicks > 0) {
            signatureTicks--;
            int elapsed = sig.windup() + SIGNATURE_FOLLOW - signatureTicks;
            var target = signatureTarget;
            if (elapsed <= sig.windup()) {
                getNavigation().stop();
                if (target != null) getLookControl().setLookAt(target, 60, 60);
                if (elapsed == 1) markEchoes(target, fighters);
                if (elapsed % 3 == 0) drawEchoes(level);
                signatureWindup(level, target, elapsed, fighters);
                if (elapsed == sig.windup()) {
                    signatureStrike(level, target, fighters);
                    strikeEchoes(level, fighters);
                }
            }
            if (signatureTicks == 0) entityData.set(SIGNING, false);
            return;
        }
        if (transition > 0 || --signatureCooldown > 0) return;
        ServerPlayer target = null;
        for (ServerPlayer p : fighters) {
            if (p.distanceToSqr(this) <= sig.range() * sig.range() && (target == null || p.distanceToSqr(this) < target.distanceToSqr(this))) target = p;
        }
        if (target == null) {
            signatureCooldown = 20;
            return;
        }
        signatureCooldown = phase >= 2 ? sig.cooldown() * 7 / 10 : sig.cooldown();
        startSignature(target);
        Component name = Component.translatable("signature.sofe." + sig.id()).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD);
        for (ServerPlayer p : fighters) p.displayClientMessage(name, true);
        level.playSound(null, blockPosition(), net.minecraft.sounds.SoundEvents.RAVAGER_ROAR, net.minecraft.sounds.SoundSource.HOSTILE, 1.2f, 0.7f);
    }

    /** The phase the client draws (see SHOWN_PHASE). */
    public int shownPhase() {
        return entityData.get(SHOWN_PHASE);
    }

    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide && entityData.get(SHOWN_PHASE) != phase) entityData.set(SHOWN_PHASE, phase);
    }

    public boolean isFighting() {
        return fighting;
    }

    public Set<UUID> participants() {
        return Set.copyOf(participants);
    }

    // --- difficulty

    @Override
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor level, DifficultyInstance difficulty, MobSpawnType reason, SpawnGroupData data, CompoundTag tag) {
        applyDifficulty(level.getLevel().getDifficulty());
        return super.finalizeSpawn(level, difficulty, reason, data, tag);
    }

    /** Health and damage follow the difficulty (Easy −25%, Hard +25%). */
    public void applyDifficulty(Difficulty difficulty) {
        double bonus = BossDifficulty.multiplier(difficulty) - 1;
        for (Attribute attribute : new Attribute[]{Attributes.MAX_HEALTH, Attributes.ATTACK_DAMAGE}) {
            var instance = getAttribute(attribute);
            if (instance == null) continue;
            instance.removeModifier(DIFFICULTY_ID);
            if (bonus != 0) instance.addPermanentModifier(new AttributeModifier(DIFFICULTY_ID, "SoFE boss difficulty", bonus, AttributeModifier.Operation.MULTIPLY_TOTAL));
        }
        setHealth(getMaxHealth());
    }

    protected boolean hardMechanics() {
        return BossDifficulty.extraMechanics(level().getDifficulty());
    }

    // --- the fight

    @Override
    protected void customServerAiStep() {
        super.customServerAiStep();
        ServerLevel level = (ServerLevel) level();
        bossBar.setProgress(getHealth() / getMaxHealth());
        Vec3 center = arenaCenter != null ? arenaCenter : position();
        double radius = arenaRadius();

        List<ServerPlayer> fighters = level.getEntitiesOfClass(ServerPlayer.class, new AABB(center, center).inflate(radius, 16, radius),
                p -> p.isAlive() && !p.isSpectator() && p.position().distanceToSqr(center.x, p.getY(), center.z) <= radius * radius);
        double reach = radius + PRESENCE_REACH;
        if (!isEcho()) {
            for (ServerPlayer p : level.getEntitiesOfClass(ServerPlayer.class, getBoundingBox().inflate(reach, 16, reach), p -> p.isAlive() && !p.isSpectator())) {
                if (awed.add(p.getUUID())) awe(level, p);
            }
        }
        if (transition > 0) transition--;
        dread(level);
        for (ServerPlayer p : fighters) {
            bossBar.addPlayer(p);
            lastSeen.put(p.getUUID(), p);
            int ticks = presence.merge(p.getUUID(), 1, Integer::sum);
            if (ticks >= PRESENCE_TICKS || fighting) join(p);
        }
        for (ServerPlayer p : Set.copyOf(bossBar.getPlayers())) {
            if (!fighters.contains(p) && (arenaCenter == null || p.distanceToSqr(this) > 64 * 64)) bossBar.removePlayer(p);
        }
        if (!fighting && (getTarget() instanceof ServerPlayer || getHealth() < getMaxHealth()) && !fighters.isEmpty()) {
            startFight(level, fighters);
        }
        if (fighting) {
            if (!isEcho()) sealArena(level, center, radius); // an echo fights inside its raiser's arena
            if (fighters.isEmpty()) {
                if (++emptyTicks >= RESET_AFTER_TICKS) reset();
                return;
            }
            emptyTicks = 0;
            scaleFor(fighters.size());
            updatePhase(level, fighters);
            fightTick(level, fighters);
            signatureTick(level, fighters);
        }
    }

    private void startFight(ServerLevel level, List<ServerPlayer> fighters) {
        fighting = true;
        arenaCenter = position();
        fighters.forEach(this::join);
        level.playSound(null, blockPosition(), net.minecraft.sounds.SoundEvents.WITHER_SPAWN, net.minecraft.sounds.SoundSource.HOSTILE, 0.5f, 0.8f);
        fighters.forEach(p -> p.displayClientMessage(Component.translatable("message.sofe.arena_sealed").withStyle(ChatFormatting.RED), true));
    }

    private void join(ServerPlayer player) {
        if (participants.add(player.getUUID())) {
            inside.add(player.getUUID());
            String intro = introDialogue();
            boolean alreadyBeaten = StoryCapability.get(player).map(s -> s.hasDefeated(bossId())).orElse(false);
            if (intro != null && !alreadyBeaten && greeted.add(player.getUUID())) DialogueService.open(player, intro, null);
        }
        inside.add(player.getUUID());
    }

    /**
     * Nobody enters or leaves while the fight is on. Players who are not part of it are pushed out;
     * participants who walk out are put back in. A participant who died may come back in for their body.
     */
    private void sealArena(ServerLevel level, Vec3 center, double radius) {
        if (this.tickCount % 5 != 0) return;
        double outer = radius + 8;
        for (ServerPlayer p : level.getEntitiesOfClass(ServerPlayer.class, new AABB(center, center).inflate(outer, 24, outer))) {
            if (p.isSpectator() || p.isCreative()) continue;
            double dx = p.getX() - center.x, dz = p.getZ() - center.z;
            double distance = Math.sqrt(dx * dx + dz * dz);
            boolean participant = participants.contains(p.getUUID());
            if (!participant && distance < radius) {
                push(p, center, radius + 2, true);
                p.displayClientMessage(Component.translatable("message.sofe.arena_sealed").withStyle(ChatFormatting.RED), true);
            } else if (participant && distance > radius && inside.contains(p.getUUID()) && p.isAlive()) {
                push(p, center, radius - 2, false);
            } else if (participant && distance <= radius) {
                inside.add(p.getUUID());
            }
        }
    }

    private static void push(ServerPlayer player, Vec3 center, double toRadius, boolean outward) {
        Vec3 direction = new Vec3(player.getX() - center.x, 0, player.getZ() - center.z);
        if (direction.lengthSqr() < 0.01) direction = new Vec3(1, 0, 0);
        Vec3 to = center.add(direction.normalize().scale(toRadius));
        player.teleportTo(to.x, player.getY(), to.z);
    }

    private void updatePhase(ServerLevel level, List<ServerPlayer> fighters) {
        double fraction = getHealth() / getMaxHealth();
        double[] thresholds = phaseThresholds();
        int shouldBe = 1;
        for (double t : thresholds) if (fraction < t) shouldBe++;
        while (phase < shouldBe) {
            phase++;
            bossBar.setColor(phase >= 2 ? BossEvent.BossBarColor.RED : bossBar.getColor());
            enrage(level, fighters);
            onPhase(phase, level, fighters);
        }
    }

    /** Moves the fight on to a phase at once (tests and admin tools): stronger, and the phase's own change. */
    public void enterPhase(ServerLevel level, int newPhase, List<ServerPlayer> fighters) {
        while (phase < newPhase) {
            phase++;
            enrage(level, fighters);
            onPhase(phase, level, fighters);
        }
    }

    /**
     * The presence of the boss, the first time a Bearer comes near: its name across the screen with its Law or Sin
     * under it, a roar, and a moment of darkness. It frightens; it does not harm.
     */
    private void awe(ServerLevel level, ServerPlayer player) {
        player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitlesAnimationPacket(10, 50, 20));
        player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetTitleTextPacket(getDisplayName().copy().withStyle(ChatFormatting.DARK_RED)));
        Component subtitle = presenceSubtitle();
        if (subtitle != null) {
            player.connection.send(new net.minecraft.network.protocol.game.ClientboundSetSubtitleTextPacket(subtitle.copy().withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC)));
        }
        player.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.DARKNESS, 60, 0, false, false));
        player.playNotifySound(net.minecraft.sounds.SoundEvents.ENDER_DRAGON_GROWL, net.minecraft.sounds.SoundSource.HOSTILE, 0.7f, 0.6f);
    }

    /** Its aura while it lives: dark motes round it, heavy steps while it moves, and a darker one when enraged. */
    private void dread(ServerLevel level) {
        if (this.tickCount % 6 == 0) {
            var mote = phase >= 2 ? net.minecraft.core.particles.ParticleTypes.SOUL_FIRE_FLAME : net.minecraft.core.particles.ParticleTypes.SMOKE;
            level.sendParticles(mote, getX(), getY() + getBbHeight() * 0.5, getZ(), phase >= 2 ? 6 : 3,
                    getBbWidth() * 0.7, getBbHeight() * 0.4, getBbWidth() * 0.7, 0.01);
        }
        if (this.tickCount % 16 == 0 && getDeltaMovement().horizontalDistanceSqr() > 0.002) {
            level.playSound(null, blockPosition(), net.minecraft.sounds.SoundEvents.RAVAGER_STEP, net.minecraft.sounds.SoundSource.HOSTILE, 0.9f, 0.6f);
        }
    }

    /**
     * A new phase: stronger every time (damage, speed, armor), never stronger by healing. A shockwave throws the
     * Bearers back, the sky darkens, every fighter is told, and for a moment the boss cannot be hurt.
     */
    private void enrage(ServerLevel level, List<ServerPlayer> fighters) {
        int extra = phase - 1;
        setEnrage(Attributes.ATTACK_DAMAGE, ENRAGE_DAMAGE, ENRAGE_DAMAGE_PER_PHASE * extra, AttributeModifier.Operation.MULTIPLY_TOTAL);
        setEnrage(Attributes.MOVEMENT_SPEED, ENRAGE_SPEED, ENRAGE_SPEED_PER_PHASE * extra, AttributeModifier.Operation.MULTIPLY_TOTAL);
        setEnrage(Attributes.ARMOR, ENRAGE_ARMOR, ENRAGE_ARMOR_PER_PHASE * extra, AttributeModifier.Operation.ADDITION);
        transition = TRANSITION_TICKS;
        bossBar.setDarkenScreen(true);
        bossBar.setCreateWorldFog(true);
        level.playSound(null, blockPosition(), net.minecraft.sounds.SoundEvents.ENDER_DRAGON_GROWL, net.minecraft.sounds.SoundSource.HOSTILE, 1.2f, 0.5f);
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.EXPLOSION_EMITTER, getX(), getY() + 1, getZ(), 1, 0, 0, 0, 0);
        level.sendParticles(net.minecraft.core.particles.ParticleTypes.SOUL, getX(), getY() + 1, getZ(), 40, 2, 1, 2, 0.08);
        for (ServerPlayer p : fighters) {
            Vec3 away = p.position().subtract(position()).multiply(1, 0, 1);
            if (away.lengthSqr() < 64) {
                Vec3 push = away.lengthSqr() < 0.01 ? new Vec3(1, 0, 0) : away.normalize();
                p.knockback(1.2, -push.x, -push.z);
                p.hurtMarked = true;
            }
            p.displayClientMessage(Component.translatable("message.sofe.boss.enraged", getDisplayName()).withStyle(ChatFormatting.DARK_RED, ChatFormatting.BOLD), true);
        }
    }

    private void setEnrage(Attribute attribute, UUID id, double amount, AttributeModifier.Operation operation) {
        var instance = getAttribute(attribute);
        if (instance == null) return;
        instance.removeModifier(id);
        if (amount != 0) instance.addPermanentModifier(new AttributeModifier(id, "SoFE boss phase", amount, operation));
    }

    /** Back to full health, as if the fight never happened. */
    /**
     * More players in the arena, more health (its share of health kept): it only grows during a fight, so leaving the
     * arena never heals or weakens the boss, and it comes back to one player's when the fight resets.
     */
    public void scaleFor(int players) {
        if (isEcho() || players <= scaledFor) return;
        scaledFor = players;
        applyPlayerScale();
    }

    private void applyPlayerScale() {
        var health = getAttribute(Attributes.MAX_HEALTH);
        if (health == null) return;
        float share = getHealth() / Math.max(1, getMaxHealth());
        health.removeModifier(PLAYERS_ID);
        double bonus = com.sofe.pact.PactRules.bossHealthMultiplier(scaledFor, com.sofe.config.SoFEConfig.SERVER.bossHealthPerPlayer.get()) - 1;
        if (bonus > 0) health.addPermanentModifier(new AttributeModifier(PLAYERS_ID, "SoFE boss players", bonus, AttributeModifier.Operation.MULTIPLY_TOTAL));
        setHealth(getMaxHealth() * share);
    }

    public int scaledFor() {
        return scaledFor;
    }

    public void reset() {
        scaledFor = 1;
        applyPlayerScale();
        setHealth(getMaxHealth());
        participants.clear();
        presence.clear();
        inside.clear();
        lastSeen.clear();
        fighting = false;
        phase = 1;
        emptyTicks = 0;
        transition = 0;
        signatureTicks = 0;
        signatureCooldown = 160;
        signatureTarget = null;
        signatureEchoes.clear();
        entityData.set(SIGNING, false);
        setEnrage(Attributes.ATTACK_DAMAGE, ENRAGE_DAMAGE, 0, AttributeModifier.Operation.MULTIPLY_TOTAL);
        setEnrage(Attributes.MOVEMENT_SPEED, ENRAGE_SPEED, 0, AttributeModifier.Operation.MULTIPLY_TOTAL);
        setEnrage(Attributes.ARMOR, ENRAGE_ARMOR, 0, AttributeModifier.Operation.ADDITION);
        bossBar.setDarkenScreen(false);
        bossBar.setCreateWorldFog(false);
        setTarget(null);
        if (arenaCenter != null) teleportTo(arenaCenter.x, arenaCenter.y, arenaCenter.z);
        arenaCenter = null;
        BossKit.restore(this);
        if (level() instanceof ServerLevel level) {
            BossKit.dismissMinions(level, this);
            onFightOver(level, false);
        }
        onReset();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (transition > 0 && !source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) return false; // the phase change
        if (source.getEntity() instanceof ServerPlayer player && !level().isClientSide()) {
            lastSeen.put(player.getUUID(), player);
            join(player);
        }
        return super.hurt(source, amount);
    }

    // --- the end of the fight

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (!(level() instanceof ServerLevel level)) return;
        BossKit.restore(this);
        BossKit.dismissMinions(level, this);
        onFightOver(level, true);
        if (isEcho()) return; // an echo gives nothing
        com.sofe.world.build.RegionHealing.bossFell(level, bossId()); // an Archsin's fall heals its region's land
        int index = 0;
        for (UUID id : participants) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(id); // after a respawn this is the new player
            if (player == null) player = lastSeen.get(id);
            if (player == null) continue;
            if (player.hasDisconnected()) { // gone from the game: their reward waits for their return (UC-32)
                com.sofe.pact.OwedRewards.get(level.getServer()).owe(id, net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getKey(getType()).toString());
                continue;
            }
            boolean first = !StoryCapability.get(player).map(s -> s.hasDefeated(bossId())).orElse(false);
            QuestEngine.bossDefeated(player, bossId());
            QuestEngine.favorHere(player, this instanceof ArchsinEntity ? ARCHSIN_FAVOR : BOSS_FAVOR);
            givePersonalLoot(level, player, source, index++, participants.size());
            onCredited(player, first);
            com.sofe.economy.EconomyHandler.refillFlask(player); // an elite fell: the Flask refills
        }
        SoFEMod.LOGGER.info("{} fell; {} participant(s) credited", bossId(), participants.size());
    }

    /**
     * The reward of a fight this player won while they were away: credit, Favor, their own loot (straight into the
     * inventory, the arena's coffers are long gone) and the Archsin's Shard. Called on a boss made for it, never spawned.
     */
    public void creditLate(ServerLevel level, ServerPlayer player) {
        boolean first = !StoryCapability.get(player).map(s -> s.hasDefeated(bossId())).orElse(false);
        QuestEngine.bossDefeated(player, bossId());
        QuestEngine.favorHere(player, this instanceof ArchsinEntity ? ARCHSIN_FAVOR : BOSS_FAVOR);
        moveTo(player.getX(), player.getY(), player.getZ());
        LootTable table = level.getServer().getLootData().getLootTable(getLootTable());
        LootParams params = new LootParams.Builder(level).withParameter(LootContextParams.THIS_ENTITY, this)
                .withParameter(LootContextParams.ORIGIN, player.position()).withParameter(LootContextParams.DAMAGE_SOURCE, player.damageSources().playerAttack(player))
                .withParameter(LootContextParams.KILLER_ENTITY, player).withParameter(LootContextParams.LAST_DAMAGE_PLAYER, player)
                .withLuck(player.getLuck()).create(LootContextParamSets.ENTITY);
        for (ItemStack stack : table.getRandomItems(params)) {
            if (!player.getInventory().add(stack)) player.drop(stack, false);
        }
        onCredited(player, first);
        com.sofe.economy.EconomyHandler.refillFlask(player);
        player.sendSystemMessage(Component.translatable("message.sofe.boss.late_credit", getDisplayName()).withStyle(ChatFormatting.GOLD));
    }

    /** When the fight ends, won (the boss died) or lost (it reset): give back what was taken, undo what was done. */
    protected void onFightOver(ServerLevel level, boolean defeated) {
    }

    /** The players fighting now (in the arena), for mechanics outside the fight tick. */
    protected List<ServerPlayer> fightersNow(ServerLevel level) {
        Vec3 center = arenaCenter != null ? arenaCenter : position();
        double radius = arenaRadius();
        return level.getEntitiesOfClass(ServerPlayer.class, new AABB(center, center).inflate(radius, 16, radius),
                p -> p.isAlive() && !p.isSpectator() && p.position().distanceToSqr(center.x, p.getY(), center.z) <= radius * radius);
    }

    /** Subclasses add their own rewards (Codex Shards for Archsins). */
    protected void onCredited(ServerPlayer player, boolean firstTime) {
    }

    /**
     * A boss leaves a Reward Coffer for each participant in a ring round the middle of its arena (docs/Anexos.md, A6);
     * a boss that should put the loot straight into the inventory returns false.
     */
    protected boolean usesRewardCoffer() {
        return true;
    }

    /** Each participant rolls the boss's loot table for themselves. */
    private void givePersonalLoot(ServerLevel level, ServerPlayer player, DamageSource source, int index, int count) {
        ResourceLocation tableId = getLootTable();
        LootTable table = level.getServer().getLootData().getLootTable(tableId);
        LootParams params = new LootParams.Builder(level)
                .withParameter(LootContextParams.THIS_ENTITY, this)
                .withParameter(LootContextParams.ORIGIN, position())
                .withParameter(LootContextParams.DAMAGE_SOURCE, source)
                .withParameter(LootContextParams.KILLER_ENTITY, player)
                .withParameter(LootContextParams.LAST_DAMAGE_PLAYER, player)
                .withLuck(player.getLuck())
                .create(LootContextParamSets.ENTITY);
        List<ItemStack> loot = table.getRandomItems(params);
        if (usesRewardCoffer()) {
            RewardCoffer.place(level, arenaCenterPos(), index, count, player, loot);
            player.displayClientMessage(Component.translatable("message.sofe.coffer.waiting").withStyle(ChatFormatting.GOLD), false);
            return;
        }
        for (ItemStack stack : loot) {
            if (!player.getInventory().add(stack)) player.drop(stack, false);
        }
    }

    /** The shared drops are replaced by personal loot. */
    @Override
    protected void dropFromLootTable(DamageSource source, boolean hitByPlayer) {
    }

    @Override
    protected void dropCustomDeathLoot(DamageSource source, int looting, boolean hitByPlayer) {
    }

    @Override
    public ResourceLocation getDefaultLootTable() {
        ResourceLocation id = ForgeRegistries.ENTITY_TYPES.getKey(getType());
        return id == null ? super.getDefaultLootTable() : id.withPrefix("entities/");
    }

    // --- housekeeping

    @Override
    public void stopSeenByPlayer(ServerPlayer player) {
        super.stopSeenByPlayer(player);
        bossBar.removePlayer(player);
    }

    @Override
    public boolean removeWhenFarAway(double distance) {
        return false;
    }

    @Override
    public boolean canChangeDimensions() {
        return false;
    }

    @Override
    public boolean fireImmune() {
        return true;
    }

    public BlockPos arenaCenterPos() {
        return BlockPos.containing(arenaCenter != null ? arenaCenter : position());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putInt("phase", phase);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("phase")) phase = Math.max(1, tag.getInt("phase"));
        if (hasCustomName()) bossBar.setName(getDisplayName());
    }
}
