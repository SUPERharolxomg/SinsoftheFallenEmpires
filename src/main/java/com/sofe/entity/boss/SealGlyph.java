package com.sofe.entity.boss;

import com.sofe.entity.SoFEAnimated;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.level.Level;

/**
 * One of the seven Seals that rise round Nahrazel inside the Codex: a page of the old seal hanging in the air,
 * turning, lit from within. It does not move or fight; while it stands Nahrazel is held. Breaking it tells him.
 */
public class SealGlyph extends PathfinderMob implements SoFEAnimated {
    private final software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache animationCache =
            software.bernie.geckolib.util.GeckoLibUtil.createInstanceCache(this);
    private String keeper = "";

    public SealGlyph(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
        setNoGravity(true);
        setPersistenceRequired();
    }

    public static AttributeSupplier.Builder attributes() {
        return PathfinderMob.createMobAttributes().add(Attributes.MAX_HEALTH, 60).add(Attributes.ARMOR, 4)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1.0).add(Attributes.MOVEMENT_SPEED, 0);
    }

    @Override
    public software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache getAnimatableInstanceCache() {
        return animationCache;
    }

    @Override
    public String modelName() {
        return "seal_glyph";
    }

    public String keeper() {
        return keeper;
    }

    public void setKeeper(NahrazelEntity nahrazel) {
        keeper = nahrazel.getStringUUID();
    }

    @Override
    protected void registerGoals() {
    }

    @Override
    public boolean isPushable() {
        return false;
    }

    @Override
    public void tick() {
        super.tick();
        setDeltaMovement(0, 0, 0);
        if (level() instanceof ServerLevel level && tickCount % 5 == 0) {
            level.sendParticles(ParticleTypes.ENCHANT, getX(), getY() + 0.8, getZ(), 6, 0.4, 0.6, 0.4, 0.4);
        }
    }

    @Override
    public void die(DamageSource source) {
        super.die(source);
        if (level() instanceof ServerLevel level) {
            level.sendParticles(ParticleTypes.END_ROD, getX(), getY() + 0.8, getZ(), 40, 0.5, 0.8, 0.5, 0.1);
            if (level.getEntity(java.util.UUID.fromString(keeper)) instanceof NahrazelEntity nahrazel) nahrazel.sealBroken(level);
        }
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putString("Keeper", keeper);
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        keeper = tag.getString("Keeper");
    }
}
