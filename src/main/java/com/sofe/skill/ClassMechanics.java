package com.sofe.skill;

import com.sofe.combat.CombatCapability;
import com.sofe.combat.CombatData;
import com.sofe.entity.summon.SummonedAlly;
import com.sofe.player.PlayerClass;
import com.sofe.registry.ItemRegistry;
import com.sofe.skill.necromancer.NecromancerSkills;
import com.sofe.skill.thief.ThiefSkills;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.living.ShieldBlockEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;

import java.util.ArrayList;
import java.util.List;

/**
 * The unique mechanics of the four classes of Sprint 6 and their passives (docs/Clases.md):
 * the Knight's stances, Resolve from blocking, Contained Wrath and Last One Standing; the Necromancer's
 * souls, Rite of Passage; the Thief's Marks, backstabs, Deep Pockets and the Great Heist; the King's
 * Authority, Imperial Lineage, Command and Siege Decree. Values come from the class skill files.
 */
public final class ClassMechanics {
    public static final int MAX_SOULS = 10;
    private static final double SOUL_RANGE = 16;

    private ClassMechanics() {
    }

    // ------------------------------------------------------------------------------------- shared
    static Vec3 center(LivingEntity e) {
        return e.position().add(0, e.getBbHeight() / 2, 0);
    }

    private static boolean is(Player player, PlayerClass c) {
        return ClassState.classOf(player).map(c::equals).orElse(false);
    }

    /** Adds to a player's class resource and updates the HUD. */
    public static void gain(ServerPlayer player, float amount) {
        CombatCapability.get(player).ifPresent(combat -> combat.resource().ifPresent(r -> {
            r.gain(amount);
            combat.markDirty();
        }));
    }

    private static CombatData combat(ServerPlayer player) {
        return CombatCapability.get(player).orElse(null);
    }

    // ------------------------------------------------------------------------------------- the Knight's stances
    /** Shield Stance takes less damage and gathers Resolve; Charge Stance hits harder and runs, burning Resolve. */
    public static void toggleStance(ServerPlayer player) {
        if (!is(player, PlayerClass.KNIGHT)) return;
        ClassState state = ClassState.of(player);
        state.stance = state.stance == ClassState.Stance.SHIELD ? ClassState.Stance.CHARGE : ClassState.Stance.SHIELD;
        player.displayClientMessage(Component.translatable("message.sofe.stance." + state.stance.name().toLowerCase(java.util.Locale.ROOT))
                .withStyle(state.stance == ClassState.Stance.SHIELD ? ChatFormatting.AQUA : ChatFormatting.GOLD), true);
        player.level().playSound(null, player.blockPosition(), state.stance == ClassState.Stance.SHIELD ? SoundEvents.SHIELD_BLOCK : SoundEvents.ARMOR_EQUIP_NETHERITE,
                SoundSource.PLAYERS, 0.8f, 1.0f);
    }

    // ------------------------------------------------------------------------------------- blows
    public static void onHurt(LivingHurtEvent event) {
        LivingEntity victim = event.getEntity();
        if (victim.level().isClientSide) return;
        if (victim instanceof ServerPlayer defender) defend(event, defender);
        if (event.isCanceled()) return;
        if (event.getSource().getEntity() instanceof ServerPlayer attacker && attacker != victim) attack(event, attacker, victim);
    }

    private static void defend(LivingHurtEvent event, ServerPlayer player) {
        ClassState state = ClassState.of(player);
        float amount = event.getAmount();
        // a Knight's oath: part of the blow goes to the Knight who swore it
        for (ServerPlayer knight : player.serverLevel().getEntitiesOfClass(ServerPlayer.class, player.getBoundingBox().inflate(12))) {
            if (knight == player || !ClassState.active(ClassState.of(knight).oathUntil, knight)) continue;
            float share = amount * (float) KnightPassives.oathShare(knight);
            amount -= share;
            knight.hurt(knight.damageSources().magic(), share);
            break;
        }
        if (is(player, PlayerClass.KNIGHT)) {
            // Shield Wall: nothing from the front gets through
            var attacker = event.getSource().getEntity();
            if (ClassState.active(state.shieldWallUntil, player) && attacker != null) {
                Vec3 look = player.getViewVector(1f);
                Vec3 to = attacker.position().subtract(player.position()).normalize();
                if (look.x * to.x + look.z * to.z > 0.2) {
                    wrath(player, state, amount);
                    gain(player, 3);
                    player.serverLevel().sendParticles(ParticleTypes.WAX_OFF, player.getX(), player.getY() + 1, player.getZ(), 8, 0.4, 0.4, 0.4, 0.05);
                    event.setCanceled(true);
                    return;
                }
            }
            if (state.stance == ClassState.Stance.SHIELD) {
                double cut = 0.15 + ClassState.passive(player, "iron_discipline").map(s -> s.param("armor_bonus", 0.15)).orElse(0.0);
                float blocked = (float) (amount * Math.min(0.6, cut));
                wrath(player, state, blocked);
                amount -= blocked;
                gain(player, 2);
            }
            if (ClassState.active(state.lastStandUntil, player) && amount >= player.getHealth()) {
                amount = Math.max(0, player.getHealth() - 1);
            }
        }
        event.setAmount(amount);
    }

    /** Contained Wrath: what is blocked builds up, to come out on the next blow. */
    private static void wrath(ServerPlayer player, ClassState state, float blocked) {
        ClassState.passive(player, "contained_wrath").ifPresent(s ->
                state.containedWrath = (float) Math.min(s.param("max", 20), state.containedWrath + blocked * s.param("fraction", 0.5)));
    }

    private static void attack(LivingHurtEvent event, ServerPlayer player, LivingEntity target) {
        ClassState state = ClassState.of(player);
        float amount = event.getAmount();
        boolean melee = event.getSource().getDirectEntity() == player;
        PlayerClass cls = ClassState.classOf(player).orElse(null);
        if (cls == PlayerClass.KNIGHT) {
            if (state.stance == ClassState.Stance.CHARGE) amount *= 1.15f + (float) ClassState.upgradeValue(player, "charge_mastery", "damage_bonus");
            if (melee && state.containedWrath > 0) {
                amount += state.containedWrath;
                state.containedWrath = 0;
                player.serverLevel().sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1, target.getZ(), 12, 0.3, 0.3, 0.3, 0.2);
            }
            if (ClassState.active(state.lastStandUntil, player)) { // every blow heals the allies around him
                for (Player ally : player.level().getEntitiesOfClass(Player.class, player.getBoundingBox().inflate(8))) {
                    ally.heal(1 + amount * 0.1f);
                }
            }
            if (melee) gain(player, 1);
        }
        if (cls == PlayerClass.NECROMANCER && ClassState.active(state.riteUntil, player)) {
            amount *= 1 + 0.05f * state.riteStacks;
        }
        if (cls == PlayerClass.THIEF) {
            if (melee) ThiefSkills.addMarks(player, target, 1);
            var instinct = ClassState.passive(player, "scavengers_instinct");
            if (melee && instinct.isPresent() && behind(player, target)) {
                amount *= (float) instinct.get().param("backstab", 1.5);
                player.serverLevel().sendParticles(ParticleTypes.CRIT, target.getX(), target.getY() + 1, target.getZ(), 10, 0.3, 0.3, 0.3, 0.2);
            }
            if (melee && ClassState.active(state.heistUntil, player)) ThiefSkills.steal(player, target, true);
        }
        if (cls == PlayerClass.KING) {
            if (target.getPersistentData().getLong("sofe_commanded_until") > player.level().getGameTime()) {
                amount *= 1.3f + (float) ClassState.upgradeValue(player, "order_of_execution", "damage_bonus");
            }
        }
        // a Siege Decree makes whoever is caught in it take more from everyone
        if (target.getPersistentData().getLong("sofe_siege_until") > player.level().getGameTime()) amount *= 1.15f;
        event.setAmount(amount);
    }

    static boolean behind(Player player, LivingEntity target) {
        Vec3 facing = Vec3.directionFromRotation(0, target.getYRot());
        Vec3 to = player.position().subtract(target.position()).normalize();
        return facing.x * to.x + facing.z * to.z < -0.3;
    }

    public static void onShieldBlock(ShieldBlockEvent event) {
        if (!(event.getEntity() instanceof ServerPlayer player) || !is(player, PlayerClass.KNIGHT)) return;
        ClassState state = ClassState.of(player);
        gain(player, state.stance == ClassState.Stance.SHIELD ? 6 : 3);
        wrath(player, state, event.getBlockedDamage());
    }

    // ------------------------------------------------------------------------------------- deaths: souls, Authority, Resolve
    public static void onDeath(LivingDeathEvent event) {
        LivingEntity dead = event.getEntity();
        if (!(dead.level() instanceof ServerLevel level) || dead instanceof Player || dead instanceof SummonedAlly) return;
        NecromancerSkills.judgmentDeath(dead);
        java.util.Set<ServerPlayer> binders = new java.util.LinkedHashSet<>(level.getEntitiesOfClass(ServerPlayer.class, dead.getBoundingBox().inflate(SOUL_RANGE)));
        if (event.getSource().getEntity() instanceof ServerPlayer killer) binders.add(killer); // the one who struck always takes the soul
        for (ServerPlayer player : binders) {
            if (!is(player, PlayerClass.NECROMANCER)) continue;
            boolean marked = dead.getPersistentData().getString("sofe_threshold").contains(player.getStringUUID());
            if (marked || player.distanceTo(dead) <= SOUL_RANGE) addSoul(player, dead);
        }
        if (event.getSource().getEntity() instanceof ServerPlayer killer) {
            if (is(killer, PlayerClass.KING)) gain(killer, 10 + (float) ClassState.upgradeValue(killer, "authority_of_the_crown", "authority_per_kill"));
            if (is(killer, PlayerClass.KNIGHT)) gain(killer, 5);
            if (is(killer, PlayerClass.THIEF)) gain(killer, 5);
        }
    }

    /** How many souls the Necromancer can hold (more with Soul Harvest). */
    public static int maxSouls(ServerPlayer player) {
        return MAX_SOULS + (int) ClassState.upgradeValue(player, "soul_harvest", "max_souls_add");
    }

    /** Extra souls straight to the Necromancer (Final Rites). */
    public static void addSouls(ServerPlayer player, int count) {
        CombatData combat = combat(player);
        if (combat == null) return;
        combat.setSouls(Math.min(maxSouls(player), combat.souls() + count));
        combat.markDirty();
    }

    private static void addSoul(ServerPlayer player, LivingEntity from) {
        CombatData combat = combat(player);
        if (combat == null) return;
        combat.setSouls(Math.min(maxSouls(player), combat.souls() + 1));
        gain(player, 4);
        SkillTargeting.beam(player.serverLevel(), center(from), center(player), ParticleTypes.SOUL, 0.5);
        combat.markDirty();
    }

    /** A soul goes free (a Warden crumbles, the jars are drunk): Rite of Passage gives a stack. */
    public static void soulReleased(ServerPlayer player) {
        ClassState.passive(player, "rite_of_passage").ifPresent(s -> {
            ClassState state = ClassState.of(player);
            state.riteStacks = ClassState.active(state.riteUntil, player) ? Math.min((int) s.param("max_stacks", 5), state.riteStacks + 1) : 1;
            state.riteUntil = player.level().getGameTime() + s.ticks("duration_s", 10);
        });
    }

    // ------------------------------------------------------------------------------------- loot: Deep Pockets
    public static void onDrops(LivingDropsEvent event) {
        if (!(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        ClassState.passive(player, "deep_pockets").ifPresent(s -> {
            List<ItemEntity> extra = new ArrayList<>();
            for (ItemEntity drop : event.getDrops()) {
                ItemStack stack = drop.getItem();
                if (stack.is(ItemRegistry.DINAR.get())) {
                    stack.setCount((int) Math.ceil(stack.getCount() * (1 + s.param("gold_bonus", 0.25))));
                } else if (player.getRandom().nextDouble() < s.param("double_chance", 0.1)) {
                    extra.add(new ItemEntity(drop.level(), drop.getX(), drop.getY(), drop.getZ(), stack.copy()));
                }
            }
            event.getDrops().addAll(extra);
        });
    }

    // ------------------------------------------------------------------------------------- every second
    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        if (event.phase != TickEvent.Phase.END || !(event.player instanceof ServerPlayer player) || player.tickCount % 20 != 0) return;
        PlayerClass cls = ClassState.classOf(player).orElse(null);
        if (cls == null) return;
        ClassState state = ClassState.of(player);
        if (cls == PlayerClass.KNIGHT && state.stance == ClassState.Stance.CHARGE) {
            CombatData combat = combat(player);
            if (combat != null && combat.resource().isPresent() && !combat.resource().get().spend(2)) {
                toggleStance(player); // out of Resolve: back behind the shield
            } else {
                player.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SPEED, 30, 0, true, false, true));
                if (combat != null) combat.markDirty();
            }
        }
        if (cls == PlayerClass.KING) {
            ClassState.passive(player, "imperial_lineage").ifPresent(s -> {
                double radius = s.param("radius", 8);
                for (LivingEntity ally : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(radius),
                        e -> e instanceof Player || e instanceof SummonedAlly)) {
                    ally.heal((float) (ally.getMaxHealth() * s.param("regen_percent", 0.01)));
                }
            });
        }
        if (cls == PlayerClass.THIEF) ThiefSkills.refreshMarkCounter(player);
    }

    public static void onLogout(PlayerEvent.PlayerLoggedOutEvent event) {
        ClassState.forget(event.getEntity());
    }

    /** The Knight's passives that other classes' code reads. */
    static final class KnightPassives {
        static double oathShare(ServerPlayer knight) {
            return SkillCatalog.byId("protectors_oath").flatMap(i -> com.sofe.skill.data.SkillDataManager.forClass(i.owner()))
                    .flatMap(d -> d.skill("protectors_oath")).map(s -> s.withRank(Math.max(1, ClassState.rank(knight, "protectors_oath"))).param("absorb", 0.3))
                    .orElse(0.3);
        }
    }
}
