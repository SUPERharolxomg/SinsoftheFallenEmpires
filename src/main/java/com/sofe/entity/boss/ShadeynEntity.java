package com.sofe.entity.boss;

import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

import java.util.List;

/**
 * Shadeyn, the Mirror, Broken Oath of Law IX ("You shall not covet another's crown"), in the Aureum Colosseum. He
 * covets what the Bearers wear: he raises shades of gladiators clad in copies of a Bearer's own armor and weapon
 * (copies that crumble with them), and blows struck at him glance back. In his second phase every Bearer gets a
 * shade of their own.
 */
public class ShadeynEntity extends BrokenOathEntity {
    public static final String BOSS_ID = "sofe:shadeyn";
    public static final float REFLECT = 0.15f;

    public ShadeynEntity(EntityType<? extends Monster> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return Monster.createMonsterAttributes().add(Attributes.MAX_HEALTH, 300.0).add(Attributes.ATTACK_DAMAGE, 11.0)
                .add(Attributes.MOVEMENT_SPEED, 0.30).add(Attributes.ARMOR, 10.0).add(Attributes.KNOCKBACK_RESISTANCE, 0.7)
                .add(Attributes.FOLLOW_RANGE, 48.0);
    }

    @Override
    public String modelName() {
        return "shadeyn";
    }

    @Override
    public String bossId() {
        return BOSS_ID;
    }

    @Override
    public int law() {
        return 9;
    }

    @Override
    public String archsin() {
        return EnvyrisEntity.BOSS_ID;
    }

    @Override
    protected double arenaRadius() {
        return 24;
    }

    @Override
    protected void fightTick(ServerLevel level, List<ServerPlayer> fighters) {
        if (this.tickCount % (phase() >= 2 ? 300 : 400) == 0) {
            List<ServerPlayer> mirrored = phase() >= 2 ? fighters : List.of(fighters.get(random.nextInt(fighters.size())));
            for (ServerPlayer p : mirrored) mirror(level, p);
        }
    }

    /** A shade in a copy of this Bearer's gear: what they wear, it wears, and it never drops it. */
    private void mirror(ServerLevel level, ServerPlayer player) {
        var shades = BossKit.minions(level, this, com.sofe.registry.EntityRegistry.GLADIATOR_SHADE.get(), 1, 4, 3, List.of(player));
        for (Mob shade : shades) {
            for (EquipmentSlot slot : EquipmentSlot.values()) {
                var worn = player.getItemBySlot(slot);
                if (worn.isEmpty()) continue;
                shade.setItemSlot(slot, worn.copy());
                shade.setDropChance(slot, 0f);
            }
            shade.setTarget(player);
            shade.setCustomName(Component.translatable("message.sofe.shadeyn.reflection", player.getDisplayName()).withStyle(ChatFormatting.GRAY));
            level.sendParticles(ParticleTypes.END_ROD, shade.getX(), shade.getY() + 1, shade.getZ(), 20, 0.3, 0.8, 0.3, 0.05);
        }
        level.playSound(null, blockPosition(), SoundEvents.ILLUSIONER_PREPARE_MIRROR, SoundSource.HOSTILE, 1.5f, 0.8f);
        player.displayClientMessage(Component.translatable("message.sofe.shadeyn.covets").withStyle(ChatFormatting.GRAY, ChatFormatting.ITALIC), true);
    }

    /** The Mirror: part of every blow glances back at the one who struck. */
    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        boolean hurt = super.hurt(source, amount);
        if (hurt && source.getDirectEntity() instanceof ServerPlayer striker && !source.is(net.minecraft.world.damagesource.DamageTypes.THORNS)) {
            striker.hurt(damageSources().thorns(this), amount * (phase() >= 2 ? REFLECT * 1.5f : REFLECT));
        }
        return hurt;
    }

    @Override
    protected void onPhase(int newPhase, ServerLevel level, List<ServerPlayer> fighters) {
        fighters.forEach(p -> mirror(level, p));
    }

    // --- signature attack (SoFEBossEntity.Signature): his blade of glass shatters into a fan of shards flung at the Bearers, and grows back

    private net.minecraft.world.phys.Vec3 signatureAim = net.minecraft.world.phys.Vec3.ZERO;

    @Override
    protected Signature signature() {
        return new Signature("shadeyn_shatter", 25, 220, 14);
    }

    @Override
    protected void signatureWindup(net.minecraft.server.level.ServerLevel level, net.minecraft.world.entity.LivingEntity target, int tick,
                                   java.util.List<net.minecraft.server.level.ServerPlayer> fighters) {
        if (tick == 1) signatureAim = Signatures.toward(this, target);
        if (tick % 2 == 0) level.sendParticles(net.minecraft.core.particles.ParticleTypes.END_ROD, getX(), getY() + getBbHeight() * 0.6, getZ(), 3, 0.6, 0.6, 0.6, 0.02);
    }

    @Override
    protected void signatureStrike(net.minecraft.server.level.ServerLevel level, net.minecraft.world.entity.LivingEntity target,
                                   java.util.List<net.minecraft.server.level.ServerPlayer> fighters) {
        if (target == null) return;
        for (int i = 0; i < 5; i++) BossKit.bolt(level, this, target, com.sofe.gear.ranged.Spell.ARCANE, signatureDamage(0.9), 1.2f, 8);
        level.playSound(null, blockPosition(), net.minecraft.sounds.SoundEvents.GLASS_BREAK, net.minecraft.sounds.SoundSource.HOSTILE, 2.0f, 0.6f);
    }
}
