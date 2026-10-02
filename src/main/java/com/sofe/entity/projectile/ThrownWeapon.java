package com.sofe.entity.projectile;

import com.sofe.gear.ranged.Spell;
import com.sofe.registry.EntityRegistry;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.Vec3;

/**
 * A thrown javelin, knife or star: flies like a trident, strikes once with the weapon's damage and element,
 * then lies where it fell to be picked up again. A returning one (the Aetherium Javelin) flies back.
 */
public class ThrownWeapon extends AbstractArrow {
    private static final EntityDataAccessor<ItemStack> WEAPON = SynchedEntityData.defineId(ThrownWeapon.class, EntityDataSerializers.ITEM_STACK);
    private static final EntityDataAccessor<Boolean> RETURNING = SynchedEntityData.defineId(ThrownWeapon.class, EntityDataSerializers.BOOLEAN);
    private float damage = 4;
    private Spell spell = Spell.NONE;
    private boolean dealtDamage;

    public ThrownWeapon(EntityType<? extends ThrownWeapon> type, Level level) {
        super(type, level);
    }

    public ThrownWeapon(Level level, LivingEntity owner, ItemStack weapon, float damage, Spell spell, boolean returning) {
        super(EntityRegistry.THROWN_WEAPON.get(), owner, level);
        this.entityData.set(WEAPON, weapon.copy());
        this.entityData.set(RETURNING, returning);
        this.damage = damage;
        this.spell = spell;
    }

    @Override
    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(WEAPON, ItemStack.EMPTY);
        this.entityData.define(RETURNING, false);
    }

    public ItemStack weapon() {
        return entityData.get(WEAPON);
    }

    @Override
    protected ItemStack getPickupItem() {
        return weapon().copy();
    }

    @Override
    public void tick() {
        if (inGroundTime > 4) dealtDamage = true;
        Entity owner = getOwner();
        if (entityData.get(RETURNING) && dealtDamage && owner instanceof Player player && player.isAlive()) {
            setNoPhysics(true);
            Vec3 toward = player.getEyePosition().subtract(position());
            setPosRaw(getX(), getY() + toward.y * 0.03, getZ());
            setDeltaMovement(getDeltaMovement().scale(0.9).add(toward.normalize().scale(0.15)));
            if (!level().isClientSide && toward.length() < 1.5) {
                if (!player.getAbilities().instabuild && !player.getInventory().add(weapon().copy())) spawnAtLocation(weapon().copy());
                discard();
                return;
            }
        }
        super.tick();
        if (level() instanceof ServerLevel server && spell != Spell.NONE && !inGround && tickCount % 2 == 0) {
            server.sendParticles(spell.particle(), getX(), getY(), getZ(), 1, 0.05, 0.05, 0.05, 0);
        }
    }

    @Override
    protected EntityHitResult findHitEntity(Vec3 from, Vec3 to) {
        return dealtDamage ? null : super.findHitEntity(from, to);
    }

    @Override
    protected void onHitEntity(EntityHitResult hit) {
        Entity target = hit.getEntity();
        Entity owner = getOwner();
        DamageSource source = damageSources().trident(this, owner == null ? this : owner);
        dealtDamage = true;
        float amount = target instanceof LivingEntity living ? spell.damage(living, damage) : damage;
        if (target.hurt(source, amount) && target instanceof LivingEntity living) {
            if (owner instanceof LivingEntity) doPostHurtEffects(living);
            spell.apply(living, owner, amount);
        }
        setDeltaMovement(getDeltaMovement().multiply(-0.01, -0.1, -0.01));
        playSound(SoundEvents.TRIDENT_HIT, 1.0f, 1.2f);
    }

    @Override
    protected SoundEvent getDefaultHitGroundSoundEvent() {
        return SoundEvents.TRIDENT_HIT_GROUND;
    }

    @Override
    public void playerTouch(Player player) {
        if (ownedBy(player) || getOwner() == null) super.playerTouch(player);
    }

    @Override
    protected boolean tryPickup(Player player) {
        return super.tryPickup(player) || isNoPhysics() && ownedBy(player) && player.getInventory().add(getPickupItem());
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.put("Weapon", weapon().save(new CompoundTag()));
        tag.putFloat("WeaponDamage", damage);
        tag.putString("Spell", spell.id());
        tag.putBoolean("Returning", entityData.get(RETURNING));
        tag.putBoolean("DealtDamage", dealtDamage);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("Weapon")) entityData.set(WEAPON, ItemStack.of(tag.getCompound("Weapon")));
        damage = tag.getFloat("WeaponDamage");
        spell = Spell.byId(tag.getString("Spell"));
        entityData.set(RETURNING, tag.getBoolean("Returning"));
        dealtDamage = tag.getBoolean("DealtDamage");
    }

    @Override
    protected float getWaterInertia() {
        return 0.99f;
    }

    @Override
    public boolean shouldRender(double x, double y, double z) {
        return true;
    }
}
