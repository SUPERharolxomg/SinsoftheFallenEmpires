package com.sofe.entity.boss;

import com.sofe.story.Sin;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobSpawnType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.projectile.SmallFireball;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;

/**
 * Nahrazel, the First Fallen, the devil of Aetheris, on the Inverted Throne beneath Sulthari (README, Act V), in
 * three phases:
 * <ol>
 * <li>a colossus of ash: slams that shake the floor and a rain of burning ash;</li>
 * <li>all seven sins at once: seven orbs of their colours circle him, he wields every Archsin's power in turn and
 * raises the Archsins themselves as echoes;</li>
 * <li>inside the Codex: the arena turns to its pages and seven Seals rise round him. While any Seal stands he takes
 * almost nothing; the Bearers break them to rewrite the seal, and then he can be ended.</li>
 * </ol>
 * The look changes with the phase (ash, then Void), synced to the client.
 */
public class NahrazelEntity extends ArchsinEntity {
    public static final String BOSS_ID = "sofe:nahrazel";
    private static final EntityDataAccessor<Integer> FORM = SynchedEntityData.defineId(NahrazelEntity.class, EntityDataSerializers.INT);
    public static final int SEALS = 7;
    public static final float SEALED_DAMAGE = 0.15f;
    public static final List<String> ARCHSINS = List.of("sofe:vorath", "sofe:luxara", "sofe:morthis", "sofe:avarok", "sofe:gularth", "sofe:envyris");
    private int sinIndex;

    public NahrazelEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 1500.0).add(Attributes.ATTACK_DAMAGE, 20.0)
                .add(Attributes.MOVEMENT_SPEED, 0.26).add(Attributes.ARMOR, 20.0).add(Attributes.KNOCKBACK_RESISTANCE, 1.0)
                .add(Attributes.FOLLOW_RANGE, 64.0);
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        entityData.define(FORM, 1);
    }

    /** 1 the colossus of ash, 2 the seven sins, 3 inside the Codex. */
    public int form() {
        return entityData.get(FORM);
    }

    @Override
    public String modelName() {
        return "nahrazel";
    }

    @Override
    public String bossId() {
        return BOSS_ID;
    }

    @Override
    public Sin sin() {
        return Sin.PRIDE; // the source of all: his Shard is not given, the Codex closes
    }

    @Override
    protected String introDialogue() {
        return "sofe:act5/the_first_fallen";
    }

    @Override
    protected double[] phaseThresholds() {
        return new double[]{0.66, 0.33};
    }

    @Override
    protected double arenaRadius() {
        return 26;
    }

    @Override
    protected void fightTick(ServerLevel level, List<ServerPlayer> fighters) {
        switch (phase()) {
            case 1 -> ash(level, fighters);
            case 2 -> sins(level, fighters);
            default -> codex(level, fighters);
        }
    }

    // --- 1: the colossus of ash

    private void ash(ServerLevel level, List<ServerPlayer> fighters) {
        if (this.tickCount % 120 == 0) slam(level, fighters);
        if (this.tickCount % 80 == 40) {
            for (ServerPlayer p : fighters) {
                for (int i = 0; i < 3; i++) {
                    Vec3 above = p.position().add(random.nextGaussian() * 3, 18, random.nextGaussian() * 3);
                    SmallFireball ember = new SmallFireball(level, this, 0, -1, 0);
                    ember.setPos(above.x, above.y, above.z);
                    level.addFreshEntity(ember);
                }
            }
        }
        if (this.tickCount % 6 == 0) level.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 4, getZ(), 4, 1.5, 2, 1.5, 0.02);
    }

    private void slam(ServerLevel level, List<ServerPlayer> fighters) {
        level.playSound(null, blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.HOSTILE, 2f, 0.4f);
        level.sendParticles(ParticleTypes.EXPLOSION, getX(), getY() + 0.5, getZ(), 12, 4, 0.2, 4, 0);
        for (ServerPlayer p : fighters) {
            if (p.distanceToSqr(this) > 8 * 8) continue;
            Vec3 away = p.position().subtract(position()).multiply(1, 0, 1);
            Vec3 push = away.lengthSqr() < 0.01 ? new Vec3(1, 0, 0) : away.normalize();
            p.knockback(1.6, -push.x, -push.z);
            p.hurtMarked = true;
            p.hurt(damageSources().mobAttack(this), 12);
        }
    }

    // --- 2: the seven sins

    private void sins(ServerLevel level, List<ServerPlayer> fighters) {
        if (this.tickCount % 4 == 0) {
            for (Sin sin : Sin.values()) SinPowers.aura(level, this, sin, 1);
        }
        if (this.tickCount % 120 == 0) {
            SinPowers.use(level, this, SinPowers.SIX[sinIndex++ % SinPowers.SIX.length], fighters);
        }
        if (this.tickCount % 900 == 450) raiseArchsin(level, fighters);
    }

    private void raiseArchsin(ServerLevel level, List<ServerPlayer> fighters) {
        long echoes = level.getEntitiesOfClass(SoFEBossEntity.class, getBoundingBox().inflate(48), b -> b.isEcho() && b.isAlive()).size();
        if (echoes >= 1) return;
        String id = ARCHSINS.get(random.nextInt(ARCHSINS.size()));
        EntityType<?> type = ForgeRegistries.ENTITY_TYPES.getValue(ResourceLocation.tryParse(id));
        if (type == null) return;
        BlockPos at = BlockPos.containing(getX() + 7, getY(), getZ());
        if (type.spawn(level, at, MobSpawnType.MOB_SUMMONED) instanceof SoFEBossEntity archsin) {
            archsin.makeEcho();
            archsin.getPersistentData().putString(BossKit.MINION_OF, getStringUUID());
            fighters.forEach(p -> p.displayClientMessage(Component.translatable("message.sofe.nahrazel.raises", archsin.getDisplayName())
                    .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD), true));
        }
    }

    // --- 3: inside the Codex

    private void codex(ServerLevel level, List<ServerPlayer> fighters) {
        if (this.tickCount % 3 == 0) level.sendParticles(ParticleTypes.ENCHANT, getX(), getY() + 4, getZ(), 12, 6, 3, 6, 0.5);
        if (this.tickCount % 140 == 0) slam(level, fighters);
        if (this.tickCount % 160 == 80) SinPowers.use(level, this, SinPowers.SIX[sinIndex++ % SinPowers.SIX.length], fighters);
    }

    /** The Seals still standing round him. */
    public long sealsStanding(ServerLevel level) {
        return level.getEntitiesOfClass(SealGlyph.class, getBoundingBox().inflate(40), g -> g.isAlive() && getStringUUID().equals(g.keeper())).size();
    }

    /** Into the Codex: its pages under the Bearers' feet, and seven Seals in a ring. */
    public void openTheCodex(ServerLevel level, List<ServerPlayer> fighters) {
        BlockPos center = BlockPos.containing(position()).below();
        for (int dx = -20; dx <= 20; dx++) {
            for (int dz = -20; dz <= 20; dz++) {
                if (dx * dx + dz * dz > 400) continue;
                BlockPos pos = center.offset(dx, 0, dz);
                if (level.getBlockState(pos).isAir()) continue;
                var page = (Math.floorMod(dx, 6) == 0 || Math.floorMod(dz, 6) == 0) ? Blocks.PURPLE_GLAZED_TERRACOTTA : Blocks.WHITE_CONCRETE;
                BossKit.temporary(level, pos, page.defaultBlockState(), 24_000, this);
            }
        }
        for (int i = 0; i < SEALS; i++) {
            double a = i * Math.PI * 2 / SEALS;
            SealGlyph seal = com.sofe.registry.EntityRegistry.SEAL_GLYPH.get().create(level);
            if (seal == null) continue;
            seal.setKeeper(this);
            seal.moveTo(getX() + Math.cos(a) * 13, getY() + 2.5, getZ() + Math.sin(a) * 13, 0, 0);
            level.addFreshEntity(seal);
        }
        level.playSound(null, blockPosition(), SoundEvents.BOOK_PAGE_TURN, SoundSource.HOSTILE, 3f, 0.4f);
        fighters.forEach(p -> p.displayClientMessage(Component.translatable("message.sofe.nahrazel.codex").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD), true));
    }

    /** A Seal broken: the Bearers rewrite one line of the old seal. */
    public void sealBroken(ServerLevel level) {
        long left = sealsStanding(level);
        for (ServerPlayer p : fightersNow(level)) {
            p.displayClientMessage(Component.translatable(left == 0 ? "message.sofe.nahrazel.rewritten" : "message.sofe.nahrazel.seal",
                    SEALS - left, SEALS).withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD), true);
        }
        if (left == 0) level.playSound(null, blockPosition(), SoundEvents.END_PORTAL_SPAWN, SoundSource.HOSTILE, 2f, 0.8f);
    }

    /** While a Seal stands, the Codex holds him: almost nothing reaches him. */
    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (phase() >= 3 && level() instanceof ServerLevel level && sealsStanding(level) > 0
                && !source.is(net.minecraft.tags.DamageTypeTags.BYPASSES_INVULNERABILITY)) {
            amount *= SEALED_DAMAGE;
        }
        return super.hurt(source, amount);
    }

    @Override
    protected void onPhase(int newPhase, ServerLevel level, List<ServerPlayer> fighters) {
        entityData.set(FORM, newPhase);
        if (newPhase == 2) {
            fighters.forEach(p -> p.displayClientMessage(Component.translatable("message.sofe.nahrazel.sins").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.BOLD), true));
            raiseArchsin(level, fighters);
        } else if (newPhase >= 3) {
            openTheCodex(level, fighters);
        }
    }

    @Override
    protected void onFightOver(ServerLevel level, boolean defeated) {
        level.getEntitiesOfClass(SealGlyph.class, getBoundingBox().inflate(64), g -> getStringUUID().equals(g.keeper())).forEach(SealGlyph::discard);
        level.getEntitiesOfClass(EnvyCopy.class, getBoundingBox().inflate(64), c -> getStringUUID().equals(c.maker())).forEach(EnvyCopy::discard);
        entityData.set(FORM, 1);
        sinIndex = 0;
    }

    /** No Shard of his own: the Codex closes with him (the ending). */
    @Override
    protected void onCredited(ServerPlayer player, boolean firstTime) {
        player.sendSystemMessage(Component.translatable("message.sofe.nahrazel.fallen").withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
    }

    // --- signature attack (SoFEBossEntity.Signature): his claws rend the air in a cross: two wounds of the Void open in the floor

    private net.minecraft.world.phys.Vec3 signatureAim = net.minecraft.world.phys.Vec3.ZERO;

    @Override
    protected Signature signature() {
        return new Signature("nahrazel_rend", 30, 220, 10);
    }

    @Override
    protected void signatureWindup(net.minecraft.server.level.ServerLevel level, net.minecraft.world.entity.LivingEntity target, int tick,
                                   java.util.List<net.minecraft.server.level.ServerPlayer> fighters) {
        if (tick == 1) signatureAim = Signatures.toward(this, target);
        if (tick % 3 == 0) {
            var mote = form() <= 1 ? net.minecraft.core.particles.ParticleTypes.FLAME : net.minecraft.core.particles.ParticleTypes.REVERSE_PORTAL;
            Signatures.drawLine(level, position(), signatureAim.yRot(0.45f), 10, mote);
            Signatures.drawLine(level, position(), signatureAim.yRot(-0.45f), 10, mote);
        }
    }

    @Override
    protected void signatureStrike(net.minecraft.server.level.ServerLevel level, net.minecraft.world.entity.LivingEntity target,
                                   java.util.List<net.minecraft.server.level.ServerPlayer> fighters) {
        var hit = new java.util.HashSet<net.minecraft.server.level.ServerPlayer>();
        hit.addAll(Signatures.line(position(), signatureAim.yRot(0.45f), 10, 2.4, fighters));
        hit.addAll(Signatures.line(position(), signatureAim.yRot(-0.45f), 10, 2.4, fighters));
        for (var p : hit) {
            Signatures.strike(this, p, signatureDamage(2.4), position(), 1.0, 0.4);
            p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WITHER, 80, 1), this);
        }
        for (double t = 1; t <= 10; t += 1) for (float s : new float[]{0.45f, -0.45f}) {
            var d = signatureAim.yRot(s);
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.DRAGON_BREATH, getX() + d.x * t, getY() + 0.4, getZ() + d.z * t, 4, 0.3, 0.2, 0.3, 0.01);
        }
        level.playSound(null, blockPosition(), net.minecraft.sounds.SoundEvents.ENDER_DRAGON_GROWL, net.minecraft.sounds.SoundSource.HOSTILE, 1.6f, 0.8f);
    }
}
