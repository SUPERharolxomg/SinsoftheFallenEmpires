package com.sofe.mob;

import com.sofe.SoFEMod;
import com.sofe.progression.ProgressionRulesManager;
import com.sofe.story.StoryAct;
import com.sofe.world.SoFEWorld;
import com.sofe.world.region.Region;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.Optional;
import java.util.UUID;

/**
 * Gives every hostile mob a level when it first spawns in a journey and scales it
 * (docs/Jugabilidad.md, "Difficulty rises with each act"). The level is kept in the mob's
 * data and the attribute modifiers are saved with it, so reloading the world keeps both.
 */
public final class MobLevels {
    private static final String TAG_LEVEL = SoFEMod.MOD_ID + ":level";
    private static final double NEAREST_PLAYER_RANGE = 64;
    /** Mobs of the Burning Deep are this many levels above the region they lie under. */
    public static final int DEEP_BONUS = 2;
    private static final UUID HEALTH_ID = UUID.fromString("5c1f5f0e-2d8a-4c55-9c0a-7a1b3e9d0a01");
    private static final UUID DAMAGE_ID = UUID.fromString("5c1f5f0e-2d8a-4c55-9c0a-7a1b3e9d0a02");
    private static final UUID ARMOR_ID = UUID.fromString("5c1f5f0e-2d8a-4c55-9c0a-7a1b3e9d0a03");

    private MobLevels() {
    }

    public static Optional<Integer> levelOf(Entity entity) {
        CompoundTag data = entity.getPersistentData();
        return data.contains(TAG_LEVEL) ? Optional.of(data.getInt(TAG_LEVEL)) : Optional.empty();
    }

    public static void onJoin(EntityJoinLevelEvent event) {
        if (event.loadedFromDisk() || !(event.getLevel() instanceof ServerLevel level)) return;
        if (!(event.getEntity() instanceof Mob mob) || !(mob instanceof Enemy) || levelOf(mob).isPresent()) return;
        if (mob instanceof com.sofe.entity.summon.Ally) return; // a Bearer's summon is not a monster of the region
        // the Burning Deep uses the region above it at 1:8, two levels higher (docs/Mundo.md, W1); the End comes later
        if (level.dimension() != Level.OVERWORLD && level.dimension() != Level.NETHER) return;
        boolean deep = level.dimension() == Level.NETHER;
        var map = SoFEWorld.regionMap(level.getServer());
        if (map.isEmpty()) return; // free mode: vanilla mobs stay vanilla

        MobScalingRules rules = ProgressionRulesManager.mobScaling();
        var key = ForgeRegistries.ENTITY_TYPES.getKey(mob.getType());
        if (key != null && rules.excludedMods().contains(key.getNamespace())) return;

        int scale = deep ? 8 : 1;
        Region region = map.get().regionAt(mob.getBlockX() * scale, mob.getBlockZ() * scale);
        Player nearest = level.getNearestPlayer(mob, NEAREST_PLAYER_RANGE);
        int act = nearest != null ? StoryAct.of(nearest) : 0;
        boolean boss = mob instanceof com.sofe.entity.boss.SoFEBossEntity;
        int bearer = boss ? strongestNear(level, mob) : groupLevel(level, mob, nearest);
        int mobLevel = rules.levelFor(region, act, bearer, boss, new java.util.Random(level.getRandom().nextLong()));
        if (deep) mobLevel = Math.min(rules.maxLevel(), mobLevel + DEEP_BONUS);
        apply(mob, mobLevel, rules);
    }

    /**
     * The level a mob follows: the average of the Bearers within 32 blocks of the nearest one (its group), so a group
     * of mixed levels meets enemies fit for all of it; a lone Bearer, their own level.
     */
    private static int groupLevel(ServerLevel level, Mob mob, Player nearest) {
        if (nearest == null) return 0;
        var group = level.getEntitiesOfClass(Player.class, nearest.getBoundingBox().inflate(32));
        if (group.isEmpty()) return com.sofe.gear.PlayerGear.level(nearest);
        return (int) Math.round(group.stream().mapToInt(com.sofe.gear.PlayerGear::level).average().orElse(1));
    }

    /** The level of the strongest Bearer near a boss (its whole party fights it). */
    private static int strongestNear(ServerLevel level, Mob mob) {
        int best = 0;
        for (Player p : level.getEntitiesOfClass(Player.class, mob.getBoundingBox().inflate(48))) best = Math.max(best, com.sofe.gear.PlayerGear.level(p));
        return best;
    }

    /** Stores the level and adds the scaling modifiers; also used by tests and future admin tools. */
    public static void apply(LivingEntity mob, int mobLevel, MobScalingRules rules) {
        mob.getPersistentData().putInt(TAG_LEVEL, mobLevel);
        if (mobLevel <= 1) return;
        modify(mob.getAttribute(Attributes.MAX_HEALTH), HEALTH_ID, "SoFE level health",
                rules.healthMultiplier(mobLevel) - 1, AttributeModifier.Operation.MULTIPLY_TOTAL);
        modify(mob.getAttribute(Attributes.ATTACK_DAMAGE), DAMAGE_ID, "SoFE level damage",
                rules.damageMultiplier(mobLevel) - 1, AttributeModifier.Operation.MULTIPLY_TOTAL);
        modify(mob.getAttribute(Attributes.ARMOR), ARMOR_ID, "SoFE level armor",
                rules.armorBonus(mobLevel), AttributeModifier.Operation.ADDITION);
        mob.setHealth(mob.getMaxHealth());
    }

    private static void modify(AttributeInstance attribute, UUID id, String name, double amount, AttributeModifier.Operation op) {
        if (attribute == null || amount == 0) return;
        attribute.removeModifier(id);
        attribute.addPermanentModifier(new AttributeModifier(id, name, amount, op));
    }
}
