package com.sofe.gear;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sofe.SoFEMod;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.packs.resources.ResourceManager;
import net.minecraft.server.packs.resources.SimpleJsonResourceReloadListener;
import net.minecraft.util.profiling.ProfilerFiller;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ArmorItem;
import net.minecraftforge.event.AddReloadListenerEvent;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;

/**
 * The full-set bonuses: wearing all four pieces of one set gives the bonuses of data/sofe/armor_sets/&lt;set&gt;.json
 * (written by scripts/make_armor_data.py from the armor catalog). Effects are renewed every second as short,
 * quiet effects, so they end as soon as a piece comes off. The kinds of bonus:
 * <ul>
 *   <li>{@code effect}: a potion effect, {@code when} always, low_health, crouching, water, night, day or falling</li>
 *   <li>{@code cleanse}: those effects never take hold</li>
 *   <li>{@code heal} and {@code absorption}: on a timer ({@code every} ticks)</li>
 *   <li>{@code answer}: what melee attackers suffer: knockback, reflect, ignite, slow, poison, wither or weakness</li>
 *   <li>{@code on_kill}: a heal or an effect after each kill</li>
 *   <li>{@code unfreeze}: the cold never takes hold; {@code reveal}: enemies around glow through walls</li>
 * </ul>
 */
public final class ArmorSets {
    private static final int CHECK = 20, LASTS = 50;
    private static volatile Map<String, List<JsonObject>> bonuses = Map.of();

    private ArmorSets() {
    }

    public static void onAddReloadListeners(AddReloadListenerEvent event) {
        event.addListener(new SimpleJsonResourceReloadListener(new Gson(), "armor_sets") {
            @Override
            protected void apply(Map<ResourceLocation, JsonElement> files, ResourceManager resources, ProfilerFiller profiler) {
                Map<String, List<JsonObject>> loaded = new HashMap<>();
                files.forEach((id, json) -> {
                    List<JsonObject> list = new ArrayList<>();
                    for (JsonElement e : json.getAsJsonObject().getAsJsonArray("bonuses")) list.add(e.getAsJsonObject());
                    loaded.put(id.getPath(), List.copyOf(list));
                });
                bonuses = Map.copyOf(loaded);
                SoFEMod.LOGGER.info("Loaded {} armor set bonuses", loaded.size());
            }
        });
    }

    /** The set whose four pieces the player wears, if any. */
    public static Optional<SoFETiers.Armor> fullSet(Player player) {
        SoFETiers.Armor set = null;
        for (EquipmentSlot slot : new EquipmentSlot[]{EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET}) {
            if (!(player.getItemBySlot(slot).getItem() instanceof ArmorItem armor) || !(armor.getMaterial() instanceof SoFETiers.Armor m)) {
                return Optional.empty();
            }
            if (set != null && set != m) return Optional.empty();
            set = m;
        }
        return Optional.ofNullable(set);
    }

    private static List<JsonObject> of(Player player) {
        return fullSet(player).map(s -> bonuses.getOrDefault(s.name().toLowerCase(Locale.ROOT), List.of())).orElse(List.of());
    }

    private static String type(JsonObject b) {
        return b.get("type").getAsString();
    }

    private static Optional<MobEffect> effect(String id) {
        return Optional.ofNullable(ForgeRegistries.MOB_EFFECTS.getValue(ResourceLocation.tryParse(id)));
    }

    private static boolean holds(Player player, String when) {
        return switch (when) {
            case "low_health" -> player.getHealth() < player.getMaxHealth() / 2;
            case "crouching" -> player.isCrouching();
            case "water" -> player.isInWater();
            case "night" -> player.level().isNight();
            case "day" -> player.level().isDay();
            case "falling" -> player.fallDistance > 3 && !player.onGround();
            default -> true;
        };
    }

    public static void onPlayerTick(TickEvent.PlayerTickEvent event) {
        Player player = event.player;
        if (event.phase != TickEvent.Phase.END || player.level().isClientSide() || player.tickCount % CHECK != 0) return;
        for (JsonObject b : of(player)) {
            switch (type(b)) {
                case "effect" -> {
                    if (!holds(player, b.has("when") ? b.get("when").getAsString() : "always")) break;
                    int amp = b.has("amplifier") ? b.get("amplifier").getAsInt() : 0;
                    effect(b.get("effect").getAsString()).ifPresent(e ->
                            player.addEffect(new MobEffectInstance(e, e == MobEffects.NIGHT_VISION ? 260 : LASTS, amp, true, false, true)));
                }
                case "cleanse" -> {
                    for (JsonElement e : b.getAsJsonArray("effects")) effect(e.getAsString()).ifPresent(player::removeEffect);
                }
                case "heal" -> {
                    if (player.tickCount % b.get("every").getAsInt() == 0) player.heal(b.get("amount").getAsFloat());
                }
                case "absorption" -> {
                    int every = b.get("every").getAsInt();
                    if (player.tickCount % every == 0 && player.getAbsorptionAmount() < 4) {
                        player.addEffect(new MobEffectInstance(MobEffects.ABSORPTION, every, 0, true, false, true));
                    }
                }
                case "unfreeze" -> player.setTicksFrozen(0);
                case "reveal" -> {
                    double radius = b.has("radius") ? b.get("radius").getAsDouble() : 14;
                    for (LivingEntity e : player.level().getEntitiesOfClass(LivingEntity.class, player.getBoundingBox().inflate(radius),
                            e -> e instanceof Enemy && e.isAlive())) {
                        e.addEffect(new MobEffectInstance(MobEffects.GLOWING, 40, 0, true, false));
                    }
                }
                default -> {
                }
            }
        }
    }

    /** Melee blows taken: the answers of the set. */
    public static void onHurt(LivingHurtEvent event) {
        if (!(event.getEntity() instanceof Player player) || player.level().isClientSide()) return;
        if (!(event.getSource().getDirectEntity() instanceof LivingEntity attacker) || attacker == player) return;
        if (event.getSource().is(DamageTypes.THORNS)) return; // a reflected blow is not answered again
        for (JsonObject b : of(player)) {
            if (!type(b).equals("answer")) continue;
            float value = b.has("value") ? b.get("value").getAsFloat() : 0;
            switch (b.get("action").getAsString()) {
                case "knockback" -> attacker.knockback(value, player.getX() - attacker.getX(), player.getZ() - attacker.getZ());
                case "reflect" -> attacker.hurt(player.damageSources().thorns(player), event.getAmount() * value);
                case "ignite" -> attacker.setSecondsOnFire((int) Math.max(1, value));
                case "slow" -> attacker.addEffect(new MobEffectInstance(MobEffects.MOVEMENT_SLOWDOWN, 60, 1), player);
                case "poison" -> attacker.addEffect(new MobEffectInstance(MobEffects.POISON, 60, 0), player);
                case "wither" -> attacker.addEffect(new MobEffectInstance(MobEffects.WITHER, 60, 0), player);
                case "weakness" -> attacker.addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 80, 0), player);
                default -> {
                }
            }
        }
    }

    public static void onKill(LivingDeathEvent event) {
        if (!(event.getSource().getEntity() instanceof Player player) || player.level().isClientSide()) return;
        for (JsonObject b : of(player)) {
            if (!type(b).equals("on_kill")) continue;
            if (b.has("heal")) player.heal(b.get("heal").getAsFloat());
            if (b.has("effect")) {
                int amp = b.has("amplifier") ? b.get("amplifier").getAsInt() : 0;
                int duration = b.has("duration") ? b.get("duration").getAsInt() : 100;
                effect(b.get("effect").getAsString()).ifPresent(e -> player.addEffect(new MobEffectInstance(e, duration, amp)));
            }
            if (player.level() instanceof ServerLevel level) {
                level.sendParticles(ParticleTypes.SOUL, player.getX(), player.getY() + 1, player.getZ(), 4, 0.3, 0.4, 0.3, 0.02);
            }
        }
    }

    /** The tooltip line of a set's bonus, or null for armor without one (the unique pieces). */
    public static String bonusKey(SoFETiers.Armor set) {
        String key = "armorset.sofe." + set.name().toLowerCase(Locale.ROOT);
        return net.minecraft.locale.Language.getInstance().has(key) ? key : null;
    }
}
