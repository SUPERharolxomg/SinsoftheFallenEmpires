package com.sofe.registry;

import com.sofe.SoFEMod;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public final class BlockRegistry {
    public static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, SoFEMod.MOD_ID);

    private BlockRegistry() {
    }
}
