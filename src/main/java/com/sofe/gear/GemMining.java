package com.sofe.gear;

import com.sofe.player.PlayerClass;
import com.sofe.player.PlayerClassCapability;
import com.sofe.registry.ItemRegistry;
import com.sofe.registry.material.Material;
import com.sofe.registry.material.MaterialForm;
import com.sofe.registry.material.MaterialRegistry;
import com.sofe.story.StoryCapability;
import com.sofe.world.SoFEWorld;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.event.level.BlockEvent;

import java.util.HashSet;
import java.util.Set;

/**
 * Rough gems from mining (docs/Pociones.md, "Gems"; docs/Mundo.md, "Sin gems by region"): a mod ore mined with the
 * right tool has a 3% chance of giving the rough gem of the region's sin as well. The Thief's Deep Pockets doubles
 * it one time in ten.
 */
public final class GemMining {
    public static final double CHANCE = 0.03, THIEF_DOUBLE = 0.10;
    private static Set<Block> ores;

    private GemMining() {
    }

    public static boolean isModOre(BlockState state) {
        if (ores == null) {
            Set<Block> set = new HashSet<>();
            for (Material m : Material.values()) {
                if (m.has(MaterialForm.ORE)) set.add(MaterialRegistry.block(m, MaterialForm.ORE));
                if (m.has(MaterialForm.DEEPSLATE_ORE)) set.add(MaterialRegistry.block(m, MaterialForm.DEEPSLATE_ORE));
            }
            ores = set;
        }
        return ores.contains(state.getBlock());
    }

    public static void onBlockBreak(BlockEvent.BreakEvent event) {
        if (event.isCanceled() || !(event.getPlayer() instanceof ServerPlayer player) || player.isCreative()) return;
        BlockState state = event.getState();
        if (!isModOre(state) || !player.hasCorrectToolForDrops(state)) return;
        if (player.getRandom().nextDouble() >= CHANCE) return;
        var map = SoFEWorld.regionMap(player.server);
        if (map.isEmpty()) return;
        var pos = event.getPos();
        int act = StoryCapability.get(player).map(s -> s.act()).orElse(1);
        SinGem.minedIn(map.get().regionAt(pos.getX(), pos.getZ()), pos.getY(), act).ifPresent(gem -> {
            boolean thief = PlayerClassCapability.get(player).map(c -> c.get().orElse(null) == PlayerClass.THIEF).orElse(false);
            int count = thief && player.getRandom().nextDouble() < THIEF_DOUBLE ? 2 : 1;
            Block.popResource(player.level(), pos, new ItemStack(ItemRegistry.sinGem(gem, SinGem.Form.ROUGH), count));
        });
    }
}
