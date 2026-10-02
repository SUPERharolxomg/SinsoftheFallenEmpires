package com.sofe.block;

import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

/** An iron brazier of Nordrath: a bowl of burning coals on four legs, lighting the streets and walls. */
public class Brazier extends Block {
    private static final VoxelShape SHAPE = Block.box(1, 0, 1, 15, 11, 15);

    public Brazier(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public void animateTick(BlockState state, Level level, BlockPos pos, RandomSource random) {
        double x = pos.getX() + 0.3 + random.nextDouble() * 0.4, z = pos.getZ() + 0.3 + random.nextDouble() * 0.4;
        level.addParticle(ParticleTypes.FLAME, x, pos.getY() + 0.85, z, 0, 0.02, 0);
        if (random.nextInt(3) == 0) level.addParticle(ParticleTypes.SMOKE, x, pos.getY() + 1.1, z, 0, 0.04, 0);
        if (random.nextInt(8) == 0) level.addParticle(ParticleTypes.LAVA, x, pos.getY() + 0.8, z, 0, 0, 0);
    }
}
