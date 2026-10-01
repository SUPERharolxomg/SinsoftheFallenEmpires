package com.sofe.gear.loot;

import com.sofe.registry.ItemRegistry;
import com.sofe.world.SoFEWorld;
import com.sofe.world.region.Region;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Fox;
import net.minecraft.world.entity.animal.PolarBear;
import net.minecraft.world.entity.animal.Rabbit;
import net.minecraft.world.entity.animal.Wolf;
import net.minecraft.world.entity.animal.camel.Camel;
import net.minecraft.world.entity.animal.goat.Goat;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Husk;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.living.LivingDropsEvent;

/**
 * Secondary materials of Acts I and II (docs/Anexos.md, A3): Dune Leather from the desert beasts of
 * Sulthari and Frostpelt from the tundra beasts of Nordrath. (Void Ash comes from the Void creatures'
 * loot tables, Runestone from mining.)
 */
public final class SecondaryDrops {
    private static final float CHANCE = 0.5f;

    private SecondaryDrops() {
    }

    public static void onDrops(LivingDropsEvent event) {
        LivingEntity entity = event.getEntity();
        if (!(event.getSource().getEntity() instanceof ServerPlayer player) || entity.level().dimension() != Level.OVERWORLD) return;
        var map = SoFEWorld.regionMap(player.server);
        if (map.isEmpty()) return;
        Region region = map.get().regionAt(entity.getBlockX(), entity.getBlockZ());
        Item drop = null;
        if (region == Region.SULTHARI && (entity instanceof Husk || entity instanceof Rabbit || entity instanceof Camel)) {
            drop = ItemRegistry.DUNE_LEATHER.get();
        } else if (region == Region.NORDRATH && (entity instanceof PolarBear || entity instanceof Wolf || entity instanceof Fox || entity instanceof Goat)) {
            drop = ItemRegistry.FROSTPELT.get();
        }
        if (drop != null && entity.getRandom().nextFloat() < CHANCE + event.getLootingLevel() * 0.1f) {
            event.getDrops().add(new ItemEntity(entity.level(), entity.getX(), entity.getY(), entity.getZ(), new ItemStack(drop)));
        }
    }
}
