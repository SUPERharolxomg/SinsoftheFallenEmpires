package com.sofe.registry;

import com.sofe.SoFEMod;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

/** Effects of the SoFE potions. */
public final class SoFEEffects {
    public static final DeferredRegister<MobEffect> EFFECTS = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, SoFEMod.MOD_ID);

    /** The Pomegranate Elixir: heals (amplifier + 1) health every second. */
    public static final RegistryObject<MobEffect> MENDING = EFFECTS.register("elixir_mending", () -> new MobEffect(MobEffectCategory.BENEFICIAL, 0xC0392B) {
        @Override
        public void applyEffectTick(LivingEntity entity, int amplifier) {
            entity.heal(amplifier + 1);
        }

        @Override
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return duration % 20 == 0;
        }
    });

    /** Bleeding, from the arsenal's serrated blades: one damage every second, ignoring armor. */
    public static final RegistryObject<MobEffect> BLEEDING = EFFECTS.register("bleeding", () -> new MobEffect(MobEffectCategory.HARMFUL, 0x8A0B0B) {
        @Override
        public void applyEffectTick(LivingEntity entity, int amplifier) {
            entity.hurt(entity.damageSources().magic(), amplifier + 1);
        }

        @Override
        public boolean isDurationEffectTick(int duration, int amplifier) {
            return duration % 20 == 0;
        }
    });

    private SoFEEffects() {
    }
}
