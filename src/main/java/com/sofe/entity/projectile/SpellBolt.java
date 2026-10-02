package com.sofe.entity.projectile;

import com.sofe.gear.ranged.Spell;
import com.sofe.registry.EntityRegistry;
import com.sofe.registry.ItemRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.ThrowableItemProjectile;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;

/**
 * A spell bolt from a staff, wand or orb: flies straight, trails the particles of its element, and on a hit
 * deals magic damage (so Intellect and gear count) and the element's effect. A bullet is a bolt too: no
 * element, physical damage, faster.
 */
public class SpellBolt extends ThrowableItemProjectile {
    private float damage = 5;
    private Spell spell = Spell.ARCANE;
    private boolean bullet;
    private int life = 60;

    public SpellBolt(EntityType<? extends SpellBolt> type, Level level) {
        super(type, level);
    }

    public SpellBolt(Level level, LivingEntity owner, Spell spell, float damage, boolean bullet) {
        super(EntityRegistry.SPELL_BOLT.get(), owner, level);
        this.spell = spell;
        this.damage = damage;
        this.bullet = bullet;
        this.life = bullet ? 40 : 60;
        setItem(new ItemStack(display(spell, bullet)));
        setNoGravity(true);
    }

    public static Item display(Spell spell, boolean bullet) {
        if (bullet) return ItemRegistry.LEAD_SHOT.get();
        return switch (spell) {
            case EMBER -> ItemRegistry.SPELL_EMBER.get();
            case FROST -> ItemRegistry.SPELL_FROST.get();
            case VOID -> ItemRegistry.SPELL_VOID.get();
            case SOUL -> ItemRegistry.SPELL_SOUL.get();
            case BONE -> ItemRegistry.SPELL_BONE.get();
            case HOLY -> ItemRegistry.SPELL_HOLY.get();
            case STORM -> ItemRegistry.SPELL_STORM.get();
            default -> ItemRegistry.SPELL_ARCANE.get();
        };
    }

    @Override
    protected Item getDefaultItem() {
        return ItemRegistry.SPELL_ARCANE.get();
    }

    @Override
    public void tick() {
        super.tick();
        if (level() instanceof ServerLevel server) {
            if (bullet) server.sendParticles(net.minecraft.core.particles.ParticleTypes.SMOKE, getX(), getY(), getZ(), 1, 0, 0, 0, 0);
            else server.sendParticles(spell.particle(), getX(), getY(), getZ(), 2, 0.08, 0.08, 0.08, 0.01);
        }
        if (--life <= 0) discard();
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        Entity target = hit.getEntity();
        Entity owner = getOwner();
        if (target == owner) return;
        DamageSource source = bullet ? damageSources().mobProjectile(this, owner instanceof LivingEntity l ? l : null)
                : damageSources().indirectMagic(this, owner);
        float amount = target instanceof LivingEntity living ? spell.damage(living, damage) : damage;
        if (target.hurt(source, amount) && target instanceof LivingEntity living) {
            spell.apply(living, owner, amount);
            if (bullet) living.invulnerableTime = 0; // pellets of one blast all land
        }
    }

    @Override
    protected void onHit(HitResult hit) {
        super.onHit(hit);
        if (level() instanceof ServerLevel server) {
            server.sendParticles(bullet ? net.minecraft.core.particles.ParticleTypes.CRIT : spell.particle(), getX(), getY(), getZ(), 8, 0.2, 0.2, 0.2, 0.05);
            discard();
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("BoltDamage", damage);
        tag.putString("Spell", spell.id());
        tag.putBoolean("Bullet", bullet);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        damage = tag.getFloat("BoltDamage");
        spell = Spell.byId(tag.getString("Spell"));
        bullet = tag.getBoolean("Bullet");
        setNoGravity(true);
    }
}
