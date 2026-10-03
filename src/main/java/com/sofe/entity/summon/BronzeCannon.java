package com.sofe.entity.summon;

import com.sofe.entity.projectile.SpellBolt;
import com.sofe.gear.ranged.Spell;
import com.sofe.registry.EntityRegistry;
import com.sofe.skill.SkillTargeting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.UUID;

/**
 * The King's Bronze Cannon: a siege gun set down beside him that turns to the nearest enemy and fires a
 * number of shots, then is taken apart.
 */
public class BronzeCannon extends PathfinderMob {
    private UUID owner;
    private int shots = 5, interval = 24, cooldown = 20, pellets = 1;
    private float damage = 10;

    public BronzeCannon(EntityType<? extends BronzeCannon> type, Level level) {
        super(type, level);
        setNoAi(true);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return PathfinderMob.createMobAttributes().add(Attributes.MAX_HEALTH, 60).add(Attributes.KNOCKBACK_RESISTANCE, 1.0).add(Attributes.MOVEMENT_SPEED, 0);
    }

    public static void deploy(ServerPlayer owner, Vec3 at, int shots, float damage, int pellets) {
        BronzeCannon cannon = new BronzeCannon(EntityRegistry.BRONZE_CANNON.get(), owner.level());
        cannon.pellets = Math.max(1, pellets);
        cannon.owner = owner.getUUID();
        cannon.shots = shots;
        cannon.damage = damage;
        cannon.moveTo(at.x, at.y, at.z, owner.getYRot(), 0);
        cannon.setYBodyRot(owner.getYRot());
        owner.level().addFreshEntity(cannon);
        owner.level().playSound(null, cannon.blockPosition(), SoundEvents.ANVIL_PLACE, SoundSource.PLAYERS, 0.6f, 0.8f);
    }

    @Override
    public void tick() {
        super.tick();
        if (!(level() instanceof ServerLevel level)) return;
        ServerPlayer player = owner != null && level.getPlayerByUUID(owner) instanceof ServerPlayer p ? p : null;
        if (player == null || shots <= 0 || tickCount > 20 * 30) {
            level.sendParticles(ParticleTypes.LARGE_SMOKE, getX(), getY() + 0.6, getZ(), 12, 0.4, 0.3, 0.4, 0.02);
            discard();
            return;
        }
        if (--cooldown > 0) return;
        LivingEntity target = SkillTargeting.around(player, position(), 18).stream().findFirst().orElse(null);
        if (target == null) {
            cooldown = 10;
            return;
        }
        Vec3 to = target.position().add(0, target.getBbHeight() / 2, 0).subtract(position().add(0, 0.8, 0));
        float yaw = (float) (Mth.atan2(to.z, to.x) * (180 / Math.PI)) - 90f;
        setYRot(yaw);
        setYBodyRot(yaw);
        setYHeadRot(yaw);
        Vec3 muzzle = position().add(0, 0.8, 0).add(to.normalize().scale(1.2));
        for (int i = 0; i < pellets; i++) { // Grapeshot: a spray of shot, each pellet lighter
            SpellBolt shot = new SpellBolt(level, player, Spell.NONE, pellets > 1 ? damage * 0.6f : damage, true);
            shot.setPos(muzzle.x, muzzle.y, muzzle.z);
            shot.shoot(to.x, to.y, to.z, 3.2f, pellets > 1 ? 6f : 0.5f);
            level.addFreshEntity(shot);
        }
        level.sendParticles(ParticleTypes.LARGE_SMOKE, muzzle.x, muzzle.y, muzzle.z, 8, 0.2, 0.2, 0.2, 0.03);
        level.sendParticles(ParticleTypes.FLAME, muzzle.x, muzzle.y, muzzle.z, 4, 0.1, 0.1, 0.1, 0.03);
        level.playSound(null, blockPosition(), SoundEvents.GENERIC_EXPLODE, SoundSource.PLAYERS, 0.7f, 1.2f);
        shots--;
        cooldown = interval;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (source.getEntity() instanceof Player) return false;
        return super.hurt(source, amount);
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public boolean isAlliedTo(net.minecraft.world.entity.Entity other) {
        return other instanceof Player || other instanceof SummonedAlly || super.isAlliedTo(other);
    }

    @Override
    protected boolean shouldDropLoot() {
        return false;
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (owner != null) tag.putUUID("Owner", owner);
        tag.putInt("Shots", shots);
        tag.putFloat("ShotDamage", damage);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.hasUUID("Owner")) owner = tag.getUUID("Owner");
        shots = tag.getInt("Shots");
        damage = tag.getFloat("ShotDamage");
    }
}
