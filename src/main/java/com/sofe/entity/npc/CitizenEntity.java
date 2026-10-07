package com.sofe.entity.npc;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.NbtUtils;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.MoveTowardsRestrictionGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.level.Level;

/**
 * A citizen of a city (scripts/make_citizens.py): talks like a story NPC, about what is happening in
 * the current act, but strolls slowly around their home so the streets feel alive.
 */
public class CitizenEntity extends StoryNpcEntity {
    private static final int HOME_RADIUS = 10;

    public CitizenEntity(EntityType<? extends PathfinderMob> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder attributes() {
        return StoryNpcEntity.attributes().add(Attributes.MOVEMENT_SPEED, 0.2);
    }

    @Override
    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(3, new MoveTowardsRestrictionGoal(this, 0.6));
        this.goalSelector.addGoal(4, new WaterAvoidingRandomStrollGoal(this, 0.5, 0.004f));
    }

    /** Where the citizen lives: they never wander farther than a few blocks from it. */
    public void setHome(BlockPos home) {
        restrictTo(home, HOME_RADIUS);
    }

    @Override
    protected BlockPos homeColumn() {
        return hasRestriction() ? getRestrictCenter() : blockPosition();
    }

    @Override
    protected void onRegrounded(BlockPos floor) {
        setHome(floor);
    }

    @Override
    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        if (hasRestriction()) tag.put("home", NbtUtils.writeBlockPos(getRestrictCenter()));
    }

    @Override
    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        if (tag.contains("home")) setHome(NbtUtils.readBlockPos(tag.getCompound("home")));
    }
}
