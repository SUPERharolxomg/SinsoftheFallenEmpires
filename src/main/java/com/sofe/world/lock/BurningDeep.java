package com.sofe.world.lock;

import com.sofe.entity.boss.VorathEntity;
import com.sofe.registry.ItemRegistry;
import com.sofe.story.StoryCapability;
import com.sofe.world.SoFEWorld;
import com.sofe.world.region.RegionMap;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.MagmaCube;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.event.entity.EntityTravelToDimensionEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.level.BlockEvent;

import java.util.List;
import java.util.Optional;

/**
 * The Burning Deep, the Nether of a journey (docs/Mundo.md, W5; UC-37): it opens when Vorath falls.
 * Before that no portal lights. Inside, the same region locks apply at 1:8, and a portal whose exit
 * would land in a sealed overworld region does not link.
 */
public final class BurningDeep {
    private static final double PORTAL_PLAYER_RANGE = 16;
    private static final float EMBER_FROM_MAGMA_CUBES = 0.25f;

    private BurningDeep() {
    }

    /** Whether this player has opened the Burning Deep. */
    public static boolean isOpenFor(ServerPlayer player) {
        return LockAccess.bypasses(player) || StoryCapability.get(player).map(s -> s.hasDefeated(VorathEntity.BOSS_ID)).orElse(false);
    }

    /** A Nether portal only lights if a nearby player has defeated Vorath. */
    public static void onPortalSpawn(BlockEvent.PortalSpawnEvent event) {
        if (!(event.getLevel() instanceof ServerLevel level) || !SoFEWorld.isJourney(level.getServer())) return;
        BlockPos pos = event.getPos();
        List<ServerPlayer> near = level.getEntitiesOfClass(ServerPlayer.class, new AABB(pos).inflate(PORTAL_PLAYER_RANGE));
        if (near.stream().anyMatch(BurningDeep::isOpenFor)) return;
        event.setCanceled(true);
        near.forEach(p -> p.displayClientMessage(Component.translatable("message.sofe.deep_silent").withStyle(ChatFormatting.DARK_RED), true));
    }

    /** Going down needs Vorath's fall; coming up must not land in a region still sealed for the player. */
    public static void onTravel(EntityTravelToDimensionEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !SoFEWorld.isJourney(player.server)) return;
        if (event.getDimension() == Level.NETHER && !isOpenFor(player)) {
            event.setCanceled(true);
            player.displayClientMessage(Component.translatable("message.sofe.deep_silent").withStyle(ChatFormatting.DARK_RED), true);
            return;
        }
        if (player.level().dimension() == Level.NETHER && event.getDimension() == Level.OVERWORLD && !LockAccess.bypasses(player)) {
            Optional<RegionMap> map = SoFEWorld.regionMap(player.server);
            if (map.isEmpty()) return;
            int x = player.getBlockX() * 8, z = player.getBlockZ() * 8;
            if (!RegionLocks.canBeAt(map.get(), x, z, LockAccess.act(player))) {
                event.setCanceled(true);
                player.displayClientMessage(Component.translatable("message.sofe.portal_sealed",
                        RegionEnforcer.sealedMessage(map.get().regionAt(x, z))).withStyle(ChatFormatting.DARK_PURPLE), true);
            }
        }
    }

    /** Magma cubes of the Burning Deep sometimes leave an Infernal Ember (docs/Anexos.md, A3). */
    public static void onDrops(LivingDropsEvent event) {
        if (!(event.getEntity() instanceof MagmaCube cube) || !(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        if (cube.level().dimension() != Level.NETHER || !SoFEWorld.isJourney(player.server)) return;
        if (cube.getRandom().nextFloat() < EMBER_FROM_MAGMA_CUBES * cube.getSize()) {
            event.getDrops().add(new ItemEntity(cube.level(), cube.getX(), cube.getY(), cube.getZ(), new ItemStack(ItemRegistry.INFERNAL_EMBER.get())));
        }
    }
}
