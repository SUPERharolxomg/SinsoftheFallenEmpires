package com.sofe.mob;

import com.sofe.SoFEMod;
import com.sofe.story.StoryAct;
import com.sofe.world.SoFEWorld;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.monster.CaveSpider;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.Difficulty;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.registries.ForgeRegistries;

/**
 * Vanilla mobs grow meaner with the story (docs/Jugabilidad.md, "Difficulty rises with each act").
 * The act is taken from the nearest player when the mob first spawns and saved on the mob. Act II:
 * zombies sometimes wear the armor of the fallen empires, and spiders poison like cave spiders.
 * Later acts add their own traits in their sprints. Mobs from other mods get no traits.
 */
public final class MobTraits {
    private static final String TAG_ACT = SoFEMod.MOD_ID + ":trait_act";
    private static final double NEAREST_PLAYER_RANGE = 64;
    public static final float ARMORED_ZOMBIE_CHANCE = 0.35f;

    private MobTraits() {
    }

    public static int traitAct(LivingEntity mob) {
        return mob.getPersistentData().getInt(TAG_ACT);
    }

    /** Runs after MobLevels (low priority), on the first spawn only. */
    public static void onJoin(EntityJoinLevelEvent event) {
        if (event.loadedFromDisk() || !(event.getLevel() instanceof ServerLevel level) || !(event.getEntity() instanceof Mob mob)) return;
        if (mob.getPersistentData().contains(TAG_ACT) || SoFEWorld.regionMap(level.getServer()).isEmpty()) return;
        var key = ForgeRegistries.ENTITY_TYPES.getKey(mob.getType());
        if (key == null || !key.getNamespace().equals("minecraft")) return;
        Player nearest = level.getNearestPlayer(mob, NEAREST_PLAYER_RANGE);
        int act = nearest != null ? StoryAct.of(nearest) : 1;
        apply(mob, act);
    }

    /** Stores the act and gives the traits of that act; used by tests too. */
    public static void apply(Mob mob, int act) {
        mob.getPersistentData().putInt(TAG_ACT, act);
        if (act >= 2 && mob instanceof Zombie zombie && mob.getRandom().nextFloat() < ARMORED_ZOMBIE_CHANCE) {
            empireArmor(zombie);
        }
    }

    /** Pieces of the chainmail and iron the empires' soldiers wore. */
    private static void empireArmor(Zombie zombie) {
        Item[][] pieces = {
                {Items.CHAINMAIL_HELMET, Items.IRON_HELMET},
                {Items.CHAINMAIL_CHESTPLATE, Items.IRON_CHESTPLATE},
                {Items.CHAINMAIL_LEGGINGS, Items.IRON_LEGGINGS},
                {Items.CHAINMAIL_BOOTS, Items.IRON_BOOTS}};
        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        for (int i = 0; i < slots.length; i++) {
            if (!zombie.getItemBySlot(slots[i]).isEmpty() || zombie.getRandom().nextFloat() > 0.6f) continue;
            zombie.setItemSlot(slots[i], new ItemStack(pieces[i][zombie.getRandom().nextInt(2)]));
            zombie.setDropChance(slots[i], 0.05f);
        }
    }

    /** Spiders from Act II poison their bite, like cave spiders: 7 s on Normal, 15 s on Hard. */
    public static void onHurt(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof Spider spider) || spider instanceof CaveSpider) return;
        if (traitAct(spider) < 2) return;
        Difficulty difficulty = spider.level().getDifficulty();
        int seconds = difficulty == Difficulty.HARD ? 15 : difficulty == Difficulty.NORMAL ? 7 : 0;
        if (seconds > 0) event.getEntity().addEffect(new MobEffectInstance(MobEffects.POISON, seconds * 20, 0), spider);
    }
}
