package com.sofe.entity.voidkin;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Skeleton;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

/** A Void Skeleton: bones gone black and violet; does not burn by day, and its arrows leave the Void's darkness. */
public class VoidSkeleton extends Skeleton {
    public VoidSkeleton(EntityType<? extends Skeleton> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return Skeleton.createAttributes().add(Attributes.MAX_HEALTH, 24).add(Attributes.ARMOR, 3).add(Attributes.FOLLOW_RANGE, 40);
    }

    @Override
    protected boolean isSunBurnTick() {
        return false;
    }

    @Override
    protected AbstractArrow getArrow(ItemStack arrow, float velocity) {
        AbstractArrow shot = super.getArrow(arrow, velocity);
        if (shot instanceof Arrow plain) plain.addEffect(new MobEffectInstance(MobEffects.DARKNESS, 60, 0));
        if (shot instanceof Arrow plain) plain.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0));
        return shot;
    }
}
