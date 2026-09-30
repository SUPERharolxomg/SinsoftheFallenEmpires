package com.sofe.entity;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.LeapAtTargetGoal;
import net.minecraft.world.entity.ai.goal.MeleeAttackGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.level.Level;

/** A fast, fragile Void creature that leaps at its prey. */
public class VoidStalker extends VoidCreature {

    public VoidStalker(EntityType<? extends Monster> type, Level level) {
        super(type, level);
        this.xpReward = 5;
    }

    public static AttributeSupplier.Builder attributes() {
        return baseAttributes()
                .add(Attributes.MAX_HEALTH, 14.0)
                .add(Attributes.ATTACK_DAMAGE, 3.0)
                .add(Attributes.MOVEMENT_SPEED, 0.33);
    }

    @Override
    protected void addAttackGoals() {
        this.goalSelector.addGoal(1, new LeapAtTargetGoal(this, 0.45f));
        this.goalSelector.addGoal(2, new MeleeAttackGoal(this, 1.1, false));
    }
}
