package com.sofe.mob;

import com.sofe.SoFEMod;
import com.sofe.entity.boss.BossKit;
import com.sofe.story.StoryAct;
import com.sofe.world.SoFEWorld;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Difficulty;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.Attribute;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.CaveSpider;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.EnderMan;
import net.minecraft.world.entity.monster.Spider;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.UUID;

/**
 * Vanilla mobs grow meaner with the story (docs/Jugabilidad.md, "Difficulty rises with each act"). The act is taken
 * from the nearest player when the mob first spawns and saved on the mob:
 * <ul>
 * <li>Act II: zombies sometimes wear the armor of the fallen empires, and spiders poison like cave spiders;</li>
 * <li>Act III: zombies call more reinforcements, skeletons shoot frost arrows, spider poison lasts twice as long,
 * creepers have a shorter fuse, endermen blind whoever they hit;</li>
 * <li>Act IV: zombies come tougher, skeletons shoot fire arrows, spiders shoot webs that slow, endermen teleport
 * behind whoever hits them;</li>
 * <li>Act V: zombies are faster and corrupted (violet eyes, CorruptedEyesLayer), skeletons loose two arrows at once, one
 * creeper in ten is charged.</li>
 * </ul>
 * Mobs from other mods get no traits.
 */
public final class MobTraits {
    private static final String TAG_ACT = SoFEMod.MOD_ID + ":trait_act";
    private static final String VOLLEY = SoFEMod.MOD_ID + ":volley";
    private static final double NEAREST_PLAYER_RANGE = 64;
    public static final float ARMORED_ZOMBIE_CHANCE = 0.35f;
    public static final short SHORT_FUSE = 20;
    public static final float CHARGED_CREEPER_CHANCE = 0.10f;
    private static final UUID REINFORCE = UUID.fromString("5b0e7d1c-8a4f-4c3e-9f2a-1d6b7c8e9f10");
    private static final UUID TOUGH = UUID.fromString("5b0e7d1c-8a4f-4c3e-9f2a-1d6b7c8e9f11");
    private static final UUID SWIFT = UUID.fromString("5b0e7d1c-8a4f-4c3e-9f2a-1d6b7c8e9f12");

    private MobTraits() {
    }

    public static int traitAct(LivingEntity mob) {
        return mob.getPersistentData().getInt(TAG_ACT);
    }

    /** A vanilla zombie, husk or drowned of Act V: corrupted, with violet eyes (the mod's own zombies have their own looks). */
    public static boolean corrupted(net.minecraft.world.entity.Entity entity) {
        return entity instanceof Zombie && traitAct((LivingEntity) entity) >= 5
                && "minecraft".equals(net.minecraftforge.registries.ForgeRegistries.ENTITY_TYPES.getKey(entity.getType()).getNamespace());
    }

    /** A player who starts seeing a corrupted zombie is told to draw its eyes. */
    public static void onStartTracking(net.minecraftforge.event.entity.player.PlayerEvent.StartTracking event) {
        if (event.getEntity() instanceof net.minecraft.server.level.ServerPlayer player && corrupted(event.getTarget())) {
            com.sofe.network.SoFENetwork.sendTo(player, new com.sofe.network.CorruptedPacket(event.getTarget().getId()));
        }
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
        if (mob instanceof Zombie zombie) {
            if (act >= 2 && mob.getRandom().nextFloat() < ARMORED_ZOMBIE_CHANCE) empireArmor(zombie);
            if (act >= 3) modifier(zombie, Attributes.SPAWN_REINFORCEMENTS_CHANCE, REINFORCE, 0.15, AttributeModifier.Operation.ADDITION);
            if (act >= 4) {
                modifier(zombie, Attributes.MAX_HEALTH, TOUGH, 0.25, AttributeModifier.Operation.MULTIPLY_TOTAL);
                zombie.setHealth(zombie.getMaxHealth());
            }
            if (act >= 5) modifier(zombie, Attributes.MOVEMENT_SPEED, SWIFT, 0.15, AttributeModifier.Operation.MULTIPLY_TOTAL);
        }
        if (mob instanceof Creeper creeper && act >= 3) {
            CompoundTag tag = new CompoundTag();
            creeper.addAdditionalSaveData(tag);
            tag.putShort("Fuse", SHORT_FUSE);
            if (act >= 5 && creeper.getRandom().nextFloat() < CHARGED_CREEPER_CHANCE) tag.putBoolean("powered", true);
            creeper.readAdditionalSaveData(tag);
        }
    }

    private static void modifier(Mob mob, Attribute attribute, UUID id, double amount, AttributeModifier.Operation operation) {
        var instance = mob.getAttribute(attribute);
        if (instance != null && instance.getModifier(id) == null) {
            instance.addPermanentModifier(new AttributeModifier(id, "SoFE act trait", amount, operation));
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

    /** Skeleton arrows: frost (slowness) from Act III, burning from Act IV, and a second arrow beside the first in Act V. */
    public static void onArrow(EntityJoinLevelEvent event) {
        if (event.loadedFromDisk() || !(event.getLevel() instanceof ServerLevel level)) return;
        if (!(event.getEntity() instanceof AbstractArrow arrow) || !(arrow.getOwner() instanceof AbstractSkeleton skeleton)) return;
        int act = traitAct(skeleton);
        if (act < 3) return;
        if (arrow instanceof Arrow plain) plain.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1));
        if (act >= 4) arrow.setSecondsOnFire(100);
        if (act >= 5 && !arrow.getPersistentData().getBoolean(VOLLEY)) {
            Arrow twin = new Arrow(level, skeleton);
            twin.getPersistentData().putBoolean(VOLLEY, true);
            var v = arrow.getDeltaMovement();
            twin.setPos(arrow.getX(), arrow.getY(), arrow.getZ());
            twin.shoot(v.x, v.y, v.z, (float) v.length(), 6);
            twin.setBaseDamage(arrow.getBaseDamage());
            twin.pickup = AbstractArrow.Pickup.DISALLOWED;
            level.addFreshEntity(twin);
        }
    }

    /** Spiders from Act IV shoot a web at their prey now and then: a cobweb at its feet for five seconds. */
    public static void onTick(LivingEvent.LivingTickEvent event) {
        if (!(event.getEntity() instanceof Spider spider) || !(spider.level() instanceof ServerLevel level)) return;
        if (spider.tickCount % 80 != 0 || traitAct(spider) < 4) return;
        LivingEntity target = spider.getTarget();
        if (target == null || !target.isAlive() || spider.distanceToSqr(target) < 9 || spider.distanceToSqr(target) > 144 || !spider.hasLineOfSight(target)) return;
        var at = target.blockPosition();
        if (!level.getBlockState(at).isAir()) return;
        BossKit.temporary(level, at, Blocks.COBWEB.defaultBlockState(), 100, spider);
        level.sendParticles(ParticleTypes.ITEM_SNOWBALL, target.getX(), target.getY() + 0.5, target.getZ(), 8, 0.3, 0.3, 0.3, 0);
    }

    /**
     * Spiders from Act II poison their bite, like cave spiders (7 s on Normal, 15 s on Hard; twice as long from Act
     * III); endermen from Act III blind whoever they hit, and from Act IV may teleport behind whoever hits them.
     */
    public static void onHurt(LivingHurtEvent event) {
        var attacker = event.getSource().getEntity();
        if (attacker instanceof Spider spider && !(spider instanceof CaveSpider) && traitAct(spider) >= 2) {
            Difficulty difficulty = spider.level().getDifficulty();
            int seconds = difficulty == Difficulty.HARD ? 15 : difficulty == Difficulty.NORMAL ? 7 : 0;
            if (traitAct(spider) >= 3) seconds *= 2;
            if (seconds > 0) event.getEntity().addEffect(new MobEffectInstance(MobEffects.POISON, seconds * 20, 0), spider);
        }
        if (attacker instanceof EnderMan enderman && traitAct(enderman) >= 3) {
            event.getEntity().addEffect(new MobEffectInstance(MobEffects.BLINDNESS, 40, 0), enderman);
        }
        if (event.getEntity() instanceof EnderMan enderman && traitAct(enderman) >= 4 && attacker instanceof Player player
                && enderman.getRandom().nextFloat() < 0.5f) {
            var behind = player.position().subtract(player.getLookAngle().multiply(2, 0, 2));
            enderman.randomTeleport(behind.x, player.getY(), behind.z, true);
        }
    }
}
