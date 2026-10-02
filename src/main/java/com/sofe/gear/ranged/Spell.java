package com.sofe.gear.ranged;

import com.sofe.registry.SoFEEffects;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MobType;
import org.joml.Vector3f;

import java.util.Locale;

/**
 * What a ranged or arcane hit does besides its damage: the element of a spell bolt, a tome, a bow's arrows,
 * a thrown weapon or a bullet. Each has a color for its particles and an effect on the target.
 */
public enum Spell {
    NONE(0xFFFFFF), EMBER(0xFF7A1A), FROST(0x9AE6FF), STORM(0xFFF07A), VOID(0xB050FF), SOUL(0x60F0E0),
    BONE(0xE8DCC0), ARCANE(0x7A9CFF), HOLY(0xFFE7A0), POISON(0x7AD040), BLEED(0xC01020);

    private final int color;

    Spell(int color) {
        this.color = color;
    }

    public static Spell byId(String id) {
        try {
            return valueOf(id.toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException e) {
            return NONE;
        }
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    public ParticleOptions particle() {
        return switch (this) {
            case EMBER -> ParticleTypes.FLAME;
            case FROST -> ParticleTypes.SNOWFLAKE;
            case SOUL -> ParticleTypes.SOUL_FIRE_FLAME;
            case STORM -> ParticleTypes.ELECTRIC_SPARK;
            case VOID -> ParticleTypes.PORTAL;
            default -> new DustParticleOptions(new Vector3f(((color >> 16) & 255) / 255f, ((color >> 8) & 255) / 255f, (color & 255) / 255f), 1.0f);
        };
    }

    /** The damage of a hit of this element on this target (holy hits harder on the undead). */
    public float damage(LivingEntity target, float amount) {
        return this == HOLY && target.getMobType() == MobType.UNDEAD ? amount * 1.5f : amount;
    }

    /** What the element does to the target, and for soul and holy to the one who struck. */
    public void apply(LivingEntity target, Entity owner, float dealt) {
        LivingEntity source = owner instanceof LivingEntity l ? l : null;
        switch (this) {
            case EMBER -> target.setSecondsOnFire(4);
            case FROST -> {
                target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1), source);
                target.setTicksFrozen(Math.min(target.getTicksRequiredToFreeze() + 40, target.getTicksFrozen() + 60));
            }
            case VOID -> target.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0), source);
            case SOUL -> {
                if (source != null) source.heal(dealt * 0.3f);
            }
            case HOLY -> {
                if (source != null) source.heal(1.0f);
            }
            case POISON -> target.addEffect(new MobEffectInstance(MobEffects.POISON, 80, 0), source);
            case BLEED -> target.addEffect(new MobEffectInstance(SoFEEffects.BLEEDING.get(), 80, 0), source);
            case BONE -> target.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 60, 0), source);
            case STORM -> target.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 20, 3), source);
            default -> {
            }
        }
    }
}
