package com.sofe.entity.boss;

import com.sofe.story.Sin;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.entity.living.LivingAttackEvent;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Luxara, the Archsin of Lust, in the Enchanted Gardens of Parsivan (README, "The Seven Archsins"): charms,
 * illusory clones and mirrors. A charmed Bearer walks toward her and cannot hurt their allies (docs/Jugabilidad.md,
 * G14); illusions in her shape dance round the fountain; in her second phase her mirrors throw part of every blow
 * back. She speaks to each Bearer of what they desire before the fight (her temptation).
 */
public class LuxaraEntity extends ArchsinEntity {
    public static final String BOSS_ID = "sofe:luxara";
    public static final int CHARM_TICKS = 80;
    private static final Map<UUID, Long> CHARMED = new HashMap<>();
    private static final Map<UUID, LuxaraEntity> CHARMER = new HashMap<>();

    public LuxaraEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 440.0).add(Attributes.ATTACK_DAMAGE, 11.0)
                .add(Attributes.MOVEMENT_SPEED, 0.30).add(Attributes.ARMOR, 10.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.8)
                .add(Attributes.FOLLOW_RANGE, 48.0);
    }

    @Override
    public String modelName() {
        return "luxara";
    }

    @Override
    public String bossId() {
        return BOSS_ID;
    }

    @Override
    public Sin sin() {
        return Sin.LUST;
    }

    @Override
    protected String introDialogue() {
        return "sofe:act3/luxara_temptation";
    }

    @Override
    protected double arenaRadius() {
        return 22;
    }

    public static boolean isCharmed(ServerPlayer player) {
        Long until = CHARMED.get(player.getUUID());
        return until != null && player.level().getGameTime() < until;
    }

    @Override
    protected void fightTick(ServerLevel level, List<ServerPlayer> fighters) {
        int charmEvery = phase() >= 2 ? 160 : 240;
        if (this.tickCount % charmEvery == 0) charm(level, fighters.get(random.nextInt(fighters.size())));
        if (this.tickCount % (phase() >= 2 ? 260 : 360) == 120) illusions(level, fighters, phase() >= 2 ? 3 : 2);
        for (ServerPlayer p : fighters) {
            if (!isCharmed(p)) continue;
            Vec3 toHer = position().subtract(p.position()).multiply(1, 0, 1);
            if (toHer.lengthSqr() > 4) {
                Vec3 step = toHer.normalize().scale(0.12);
                p.setDeltaMovement(step.x, p.getDeltaMovement().y, step.z);
                p.hurtMarked = true;
            }
            if (this.tickCount % 10 == 0) level.sendParticles(ParticleTypes.HEART, p.getX(), p.getY() + 2.1, p.getZ(), 1, 0.2, 0.1, 0.2, 0);
        }
    }

    /** One Bearer falls under her charm: they walk to her and cannot lift a hand against their own. */
    private void charm(ServerLevel level, ServerPlayer target) {
        CHARMED.put(target.getUUID(), level.getGameTime() + CHARM_TICKS);
        CHARMER.put(target.getUUID(), this);
        target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, CHARM_TICKS, 1, false, false));
        target.displayClientMessage(Component.translatable("message.sofe.luxara.charmed").withStyle(ChatFormatting.LIGHT_PURPLE, ChatFormatting.BOLD), true);
        level.playSound(null, target.blockPosition(), SoundEvents.AMETHYST_BLOCK_CHIME, SoundSource.HOSTILE, 2f, 0.6f);
    }

    /** Illusions in her shape, among the columns. */
    private void illusions(ServerLevel level, List<ServerPlayer> fighters, int count) {
        BossKit.minions(level, this, com.sofe.registry.EntityRegistry.MIRAGE_DANCER.get(), count, count + 1, 6, fighters)
                .forEach(m -> m.setCustomName(getDisplayName().copy().withStyle(ChatFormatting.LIGHT_PURPLE)));
        level.playSound(null, blockPosition(), SoundEvents.ILLUSIONER_CAST_SPELL, SoundSource.HOSTILE, 1.5f, 1f);
    }

    /** Her mirrors (second phase): a quarter of every blow goes back to the one who struck it. */
    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && phase() >= 2 && source.getDirectEntity() instanceof ServerPlayer striker
                && !source.is(net.minecraft.world.damagesource.DamageTypes.THORNS)) {
            striker.hurt(damageSources().thorns(this), amount * 0.25f);
        }
        return hurt;
    }

    /** A charmed Bearer's blows never land on anyone but Luxara and her illusions. */
    public static void onAttack(LivingAttackEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player) || !isCharmed(player)) return;
        LuxaraEntity luxara = CHARMER.get(player.getUUID());
        var target = event.getEntity();
        boolean hers = target == luxara || (luxara != null && luxara.getStringUUID().equals(target.getPersistentData().getString(BossKit.MINION_OF)));
        if (!hers) event.setCanceled(true);
    }

    @Override
    protected void onPhase(int newPhase, ServerLevel level, List<ServerPlayer> fighters) {
        illusions(level, fighters, 3);
        fighters.forEach(p -> p.displayClientMessage(Component.translatable("message.sofe.luxara.mirrors").withStyle(ChatFormatting.LIGHT_PURPLE), true));
    }

    @Override
    protected void onFightOver(ServerLevel level, boolean defeated) {
        CHARMER.values().removeIf(l -> l == this);
    }

    // --- signature attack (SoFEBossEntity.Signature): her great wings beat once: a storm of petals drags every Bearer into her embrace

    private net.minecraft.world.phys.Vec3 signatureAim = net.minecraft.world.phys.Vec3.ZERO;

    @Override
    protected Signature signature() {
        return new Signature("luxara_wingstorm", 25, 240, 10);
    }

    @Override
    protected void signatureWindup(net.minecraft.server.level.ServerLevel level, net.minecraft.world.entity.LivingEntity target, int tick,
                                   java.util.List<net.minecraft.server.level.ServerPlayer> fighters) {
        if (tick == 1) signatureAim = Signatures.toward(this, target);
        if (tick % 4 == 0) Signatures.drawRing(level, position(), 9, net.minecraft.core.particles.ParticleTypes.HEART);
    }

    @Override
    protected void signatureStrike(net.minecraft.server.level.ServerLevel level, net.minecraft.world.entity.LivingEntity target,
                                   java.util.List<net.minecraft.server.level.ServerPlayer> fighters) {
        float dealt = 0;
        for (var p : Signatures.ring(position(), 9, fighters)) {
            Signatures.strike(this, p, signatureDamage(1.2), position(), -1.5, 0.3);
            p.addEffect(new net.minecraft.world.effect.MobEffectInstance(net.minecraft.world.effect.MobEffects.WEAKNESS, 100, 1), this);
            dealt += signatureDamage(1.2);
        }
        heal(dealt * 0.5f);
        level.playSound(null, blockPosition(), net.minecraft.sounds.SoundEvents.PHANTOM_FLAP, net.minecraft.sounds.SoundSource.HOSTILE, 2.0f, 0.6f);
    }
}
