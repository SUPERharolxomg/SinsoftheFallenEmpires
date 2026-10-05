package com.sofe.entity.boss;

import com.sofe.gear.ranged.Spell;
import com.sofe.player.PlayerClass;
import com.sofe.skill.ClassState;
import com.sofe.story.Sin;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Envyris, the Archsin of Envy, on the Shadow Throne of Aureum: she copies the skills of the player's class. She
 * raises a copy of each Bearer's own hero, up to three (with more Bearers, the classes of those who hurt her most:
 * docs/Jugabilidad.md, G14), and answers each Bearer with the last skill they cast. In her second phase the copies
 * rise again and her echoes come faster.
 */
public class EnvyrisEntity extends ArchsinEntity {
    public static final String BOSS_ID = "sofe:envyris";
    public static final String LAST_SKILL = "sofe_last_skill";
    public static final int MAX_COPIES = 3;
    private final Map<UUID, Float> dealt = new HashMap<>();

    public EnvyrisEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 600.0).add(Attributes.ATTACK_DAMAGE, 14.0)
                .add(Attributes.MOVEMENT_SPEED, 0.30).add(Attributes.ARMOR, 14.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.9)
                .add(Attributes.FOLLOW_RANGE, 48.0);
    }

    @Override
    public String modelName() {
        return "envyris";
    }

    @Override
    public String bossId() {
        return BOSS_ID;
    }

    @Override
    public Sin sin() {
        return Sin.ENVY;
    }

    @Override
    protected String introDialogue() {
        return "sofe:act4/envyris_temptation";
    }

    @Override
    protected double arenaRadius() {
        return 20;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && source.getEntity() instanceof ServerPlayer player) dealt.merge(player.getUUID(), amount, Float::sum);
        return hurt;
    }

    @Override
    protected void fightTick(ServerLevel level, List<ServerPlayer> fighters) {
        if (this.tickCount % 40 == 1 && copiesAlive(level) == 0 && (this.tickCount < 60 || this.tickCount % 600 == 1)) raiseCopies(level, fighters);
        if (this.tickCount % (phase() >= 2 ? 80 : 120) == 60) {
            for (ServerPlayer p : fighters) echo(level, p);
        }
    }

    private long copiesAlive(ServerLevel level) {
        return level.getEntitiesOfClass(EnvyCopy.class, getBoundingBox().inflate(48), c -> c.isAlive() && getStringUUID().equals(c.maker())).size();
    }

    /** The Bearers whose heroes she copies: everyone, or with more than three the three who hurt her most. */
    public List<ServerPlayer> copied(List<ServerPlayer> fighters) {
        return fighters.stream().sorted(Comparator.comparing((ServerPlayer p) -> -dealt.getOrDefault(p.getUUID(), 0f))).limit(MAX_COPIES).toList();
    }

    /** A copy of each Bearer's own hero rises from her shadow, hunting the one it copies. */
    public void raiseCopies(ServerLevel level, List<ServerPlayer> fighters) {
        for (ServerPlayer player : copied(fighters)) {
            PlayerClass cls = ClassState.classOf(player).orElse(PlayerClass.KNIGHT);
            EnvyCopy copy = com.sofe.registry.EntityRegistry.ENVY_COPY.get().create(level);
            if (copy == null) continue;
            copy.setup(this, player, cls);
            double a = random.nextDouble() * Math.PI * 2;
            copy.moveTo(getX() + Math.cos(a) * 3, getY(), getZ() + Math.sin(a) * 3, 0, 0);
            level.addFreshEntity(copy);
            player.displayClientMessage(Component.translatable("message.sofe.envyris.copy", Component.translatable(cls.heroKey()))
                    .withStyle(ChatFormatting.DARK_PURPLE), true);
        }
        level.playSound(null, blockPosition(), SoundEvents.ILLUSIONER_PREPARE_MIRROR, SoundSource.HOSTILE, 2f, 0.6f);
    }

    /** She answers a Bearer with the last skill they cast, turned against them. */
    private void echo(ServerLevel level, ServerPlayer player) {
        String skill = player.getPersistentData().getString(LAST_SKILL);
        if (skill.isEmpty()) return;
        double damage = com.sofe.skill.SkillCatalog.byId(skill)
                .flatMap(info -> com.sofe.skill.data.SkillDataManager.forClass(info.owner()).flatMap(d -> d.skill(skill)))
                .map(s -> s.param("damage", 6)).orElse(6.0);
        BossKit.bolt(level, this, player, Spell.VOID, (float) Math.max(5, damage * (phase() >= 2 ? 1.3 : 1.0)), 1.4f, 1);
        player.displayClientMessage(Component.translatable("message.sofe.envyris.echo", Component.translatable("skill.sofe." + skill))
                .withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC), true);
    }

    @Override
    protected void onPhase(int newPhase, ServerLevel level, List<ServerPlayer> fighters) {
        raiseCopies(level, fighters);
    }

    @Override
    protected void onFightOver(ServerLevel level, boolean defeated) {
        level.getEntitiesOfClass(EnvyCopy.class, getBoundingBox().inflate(64), c -> getStringUUID().equals(c.maker())).forEach(EnvyCopy::discard);
        dealt.clear();
    }

    // --- signature attack (SoFEBossEntity.Signature): her scythe sweeps all the way round her and reaps the life of everyone it touches

    private net.minecraft.world.phys.Vec3 signatureAim = net.minecraft.world.phys.Vec3.ZERO;

    @Override
    protected Signature signature() {
        return new Signature("envyris_reap", 25, 220, 6);
    }

    @Override
    protected void signatureWindup(net.minecraft.server.level.ServerLevel level, net.minecraft.world.entity.LivingEntity target, int tick,
                                   java.util.List<net.minecraft.server.level.ServerPlayer> fighters) {
        if (tick == 1) signatureAim = Signatures.toward(this, target);
        if (tick % 4 == 0) Signatures.drawRing(level, position(), 5.5, net.minecraft.core.particles.ParticleTypes.GLOW);
    }

    @Override
    protected void signatureStrike(net.minecraft.server.level.ServerLevel level, net.minecraft.world.entity.LivingEntity target,
                                   java.util.List<net.minecraft.server.level.ServerPlayer> fighters) {
        float dealt = 0;
        for (var p : Signatures.ring(position(), 5.5, fighters)) {
            Signatures.strike(this, p, signatureDamage(1.8), position(), -0.4, 0.2);
            dealt += signatureDamage(1.8);
        }
        heal(dealt * 0.25f);
        for (int i = 0; i < 12; i++) {
            double a = Math.PI * 2 * i / 12;
            level.sendParticles(net.minecraft.core.particles.ParticleTypes.SWEEP_ATTACK, getX() + Math.cos(a) * 3, getY() + 1, getZ() + Math.sin(a) * 3, 1, 0, 0, 0, 0);
        }
        level.playSound(null, blockPosition(), net.minecraft.sounds.SoundEvents.PLAYER_ATTACK_SWEEP, net.minecraft.sounds.SoundSource.HOSTILE, 2.0f, 0.5f);
    }
}
