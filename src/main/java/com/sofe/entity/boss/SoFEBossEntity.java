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
    public static final int RESET_AFTER_TICKS = 600;
    /** Standing this long in the arena makes a player a participant, even without hitting. */
    public static final int PRESENCE_TICKS = 600;

    private final ServerBossEvent bossBar;
    private final Set<UUID> participants = new HashSet<>();
    private final Map<UUID, Integer> presence = new HashMap<>();
    /** The last player object seen for each participant, for players the server list does not have (fake players). */
    private final Map<UUID, ServerPlayer> lastSeen = new HashMap<>();
    private final Set<UUID> inside = new HashSet<>();
    private final Set<UUID> greeted = new HashSet<>();
    private Vec3 arenaCenter;
    private boolean fighting;
    private int phase = 1;
    private int emptyTicks;

    protected SoFEBossEntity(EntityType<? extends Monster> type, Level level, BossEvent.BossBarColor color) {
        super(type, level);
        this.bossBar = new ServerBossEvent(Component.translatable(type.getDescriptionId()), color, BossEvent.BossBarOverlay.NOTCHED_10);
        this.xpReward = 150;
        setPersistenceRequired();
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
            sealArena(level, center, radius);
            if (fighters.isEmpty()) {
                if (++emptyTicks >= RESET_AFTER_TICKS) reset();
                return;
            }
            emptyTicks = 0;
            updatePhase(level, fighters);
            fightTick(level, fighters);
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
            onPhase(phase, level, fighters);
        }
    }

    /** Back to full health, as if the fight never happened. */
    public void reset() {
        setHealth(getMaxHealth());
        participants.clear();
        presence.clear();
        inside.clear();
        lastSeen.clear();
        fighting = false;
        phase = 1;
        emptyTicks = 0;
        setTarget(null);
        if (arenaCenter != null) teleportTo(arenaCenter.x, arenaCenter.y, arenaCenter.z);
        arenaCenter = null;
        onReset();
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
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
        for (UUID id : participants) {
            ServerPlayer player = level.getServer().getPlayerList().getPlayer(id); // after a respawn this is the new player
            if (player == null) player = lastSeen.get(id);
            if (player == null) continue;
            boolean first = !StoryCapability.get(player).map(s -> s.hasDefeated(bossId())).orElse(false);
            QuestEngine.bossDefeated(player, bossId());
            givePersonalLoot(level, player, source);
            onCredited(player, first);
            com.sofe.economy.EconomyHandler.refillFlask(player); // an elite fell: the Flask refills
        }
        SoFEMod.LOGGER.info("{} fell; {} participant(s) credited", bossId(), participants.size());
    }

    /** Subclasses add their own rewards (Codex Shards for Archsins). */
    protected void onCredited(ServerPlayer player, boolean firstTime) {
    }

    /**
     * Archsins and Broken Oaths leave a Reward Coffer in the arena with each participant's share
     * (docs/Anexos.md, A6); other bosses put the loot straight into the inventory.
     */
    protected boolean usesRewardCoffer() {
        return false;
    }

    /** Each participant rolls the boss's loot table for themselves. */
    private void givePersonalLoot(ServerLevel level, ServerPlayer player, DamageSource source) {
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
            BlockPos at = arenaCenterPos();
            while (!level.getBlockState(at).isAir() && !level.getBlockState(at).is(com.sofe.registry.SoFEBlocks.REWARD_COFFER.get())
                    && at.getY() < level.getMaxBuildHeight() - 1) {
                at = at.above();
            }
            RewardCoffer.store(level, at, player.getUUID(), loot);
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
