package com.sofe.travel;

import com.sofe.SoFEMod;
import com.sofe.world.SoFEWorld;
import com.sofe.world.zone.ProtectedZone;
import com.sofe.world.zone.ProtectedZoneData;
import com.sofe.world.zone.ZoneRules;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.EnchantmentCategory;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingEntityUseItemEvent;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.RegistryObject;

/**
 * Homeward (Vuelta al Hogar), an enchantment for boots, levels I to III: crouch and stand still for 8, 6 or
 * 4 seconds and the Bearer is taken back to Sulthari, out of combat and outside an arena, once every ten minutes;
 * a Return Scroll is read faster too (4, 3 or 2 seconds instead of 5). Only in a journey, where Sulthari is.
 */
public final class Homeward {
    public static final DeferredRegister<Enchantment> ENCHANTMENTS = DeferredRegister.create(Registries.ENCHANTMENT, SoFEMod.MOD_ID);
    public static final RegistryObject<Enchantment> HOMEWARD = ENCHANTMENTS.register("homeward", HomewardEnchantment::new);
    public static final int COOLDOWN = 12_000;
    private static final String CHARGE = "sofe_homeward_charge", LAST = "sofe_homeward_last";

    /** The enchantment itself: rare, on boots, three levels, found at the table, in books and from librarians. */
    public static class HomewardEnchantment extends Enchantment {
        HomewardEnchantment() {
            super(Rarity.RARE, EnchantmentCategory.ARMOR_FEET, new EquipmentSlot[]{EquipmentSlot.FEET});
        }

        @Override
        public int getMaxLevel() {
            return 3;
        }

        @Override
        public int getMinCost(int level) {
            return 10 + (level - 1) * 10;
        }

        @Override
        public int getMaxCost(int level) {
            return getMinCost(level) + 30;
        }
    }

    private Homeward() {
    }

    public static int level(Player player) {
        ItemStack boots = player.getItemBySlot(EquipmentSlot.FEET);
        return boots.isEmpty() ? 0 : boots.getEnchantmentLevel(HOMEWARD.get());
    }

    /** Seconds of stillness each level needs: 8, 6, 4. */
    public static int stillSeconds(int level) {
        return Math.max(4, 10 - 2 * level);
    }

    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player)) return;
        var tag = player.getPersistentData();
        int level = level(player);
        boolean still = player.isCrouching() && player.onGround() && player.getDeltaMovement().horizontalDistanceSqr() < 1e-4;
        if (level <= 0 || !still || !SoFEWorld.isJourney(player.server)) {
            if (tag.getInt(CHARGE) > 0) tag.putInt(CHARGE, 0);
            return;
        }
        long now = player.level().getGameTime();
        if (now - tag.getLong(LAST) < COOLDOWN && tag.contains(LAST)) {
            if (tag.getInt(CHARGE) == 0) {
                long left = (COOLDOWN - (now - tag.getLong(LAST))) / 20;
                player.displayClientMessage(Component.translatable("message.sofe.homeward.cooldown", left / 60, left % 60).withStyle(ChatFormatting.GRAY), true);
                tag.putInt(CHARGE, -1);
            }
            return;
        }
        int charge = Math.max(0, tag.getInt(CHARGE)) + 1;
        tag.putInt(CHARGE, charge);
        int needed = stillSeconds(level) * 20;
        if (charge % 10 == 0 && player.level() instanceof ServerLevel server) {
            server.sendParticles(ParticleTypes.PORTAL, player.getX(), player.getY() + 1, player.getZ(), 12, 0.4, 0.6, 0.4, 0.4);
            int done = charge * 10 / needed;
            player.displayClientMessage(Component.translatable("message.sofe.homeward.charging",
                    "■".repeat(done) + "□".repeat(Math.max(0, 10 - done))).withStyle(ChatFormatting.AQUA), true);
        }
        if (charge < needed) return;
        tag.putInt(CHARGE, 0);
        boolean inArena = ZoneRules.zoneAt(ProtectedZoneData.get(player.server).zones(), player.getBlockX(), player.getBlockY(), player.getBlockZ())
                .map(z -> z.kind() == ProtectedZone.Kind.ARENA).orElse(false);
        int sinceCombat = Math.min(player.tickCount - player.getLastHurtByMobTimestamp(), player.tickCount - player.getLastHurtMobTimestamp());
        boolean fought = (player.getLastHurtByMobTimestamp() != 0 || player.getLastHurtMobTimestamp() != 0) && sinceCombat < TravelRules.COMBAT_TICKS;
        if (inArena || fought) {
            player.displayClientMessage(Component.translatable(inArena ? "message.sofe.waystone.in_arena" : "message.sofe.waystone.in_combat")
                    .withStyle(ChatFormatting.RED), true);
            return;
        }
        tag.putLong(LAST, now);
        WaystoneService.returnToSulthari(player);
        player.displayClientMessage(Component.translatable("message.sofe.homeward.home").withStyle(ChatFormatting.GOLD), true);
    }

    /** A Return Scroll is read faster with Homeward on the boots. */
    public static void onUseStart(LivingEntityUseItemEvent.Start event) {
        if (event.getItem().getItem() instanceof ReturnScrollItem && event.getEntity() instanceof Player player) {
            int level = level(player);
            if (level > 0) event.setDuration(Math.max(40, ReturnScrollItem.CHANNEL_TICKS - level * 20));
        }
    }
}
