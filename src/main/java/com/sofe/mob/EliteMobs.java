package com.sofe.mob;

import com.sofe.entity.boss.SoFEBossEntity;
import com.sofe.entity.summon.Ally;
import com.sofe.gear.GearDataManager;
import com.sofe.gear.GearMaker;
import com.sofe.gear.GearSlot;
import com.sofe.gear.Rarity;
import com.sofe.registry.ItemRegistry;
import com.sofe.story.StoryCapability;
import net.minecraft.ChatFormatting;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.RandomSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.item.ArmorItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraftforge.event.entity.living.LivingDropsEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingExperienceDropEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.registries.ForgeRegistries;
import org.joml.Vector3f;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * Elites (mini-bosses): now and then a monster comes as an elite, named in gold, with an aura, two and a half
 * times its health, enchanted armor and weapon of its act's metal and one or two traits. When a Bearer kills it,
 * it leaves one of its enchanted pieces and either a veiled (unidentified) item, an Imperial piece of gear or a
 * piece of an armor set of the killer's class, three times the experience, and the Bearer's Flask refills.
 */
public final class EliteMobs {
    public static final String TAG = "sofe_elite", CHECKED = "sofe_elite_checked";
    /** The chance a monster comes as an elite: 4%, and one more for every act after the first. */
    public static final double BASE_CHANCE = 0.04, PER_ACT = 0.01;
    public static final double HEALTH = 1.5; // +150%: two and a half times
    private static final UUID HEALTH_ID = UUID.fromString("5b0e7c1a-3d2f-4e8a-9c61-7a4b2d9e1f01");
    private static final UUID SPEED_ID = UUID.fromString("5b0e7c1a-3d2f-4e8a-9c61-7a4b2d9e1f02");
    private static final UUID ARMOR_ID = UUID.fromString("5b0e7c1a-3d2f-4e8a-9c61-7a4b2d9e1f03");

    /** What makes one elite unlike another. */
    public enum Trait {
        STONESKIN, SWIFT, BURNING, VAMPIRIC, CURSED, BRUTAL;

        public String key() {
            return "elite.sofe." + name().toLowerCase(java.util.Locale.ROOT);
        }
    }

    private EliteMobs() {
    }

    public static boolean isElite(LivingEntity entity) {
        return entity.getPersistentData().contains(TAG);
    }

    public static List<Trait> traits(LivingEntity entity) {
        List<Trait> out = new ArrayList<>();
        for (String t : entity.getPersistentData().getString(TAG).split(",")) {
            for (Trait trait : Trait.values()) if (trait.name().equals(t)) out.add(trait);
        }
        return out;
    }

    /**
     * A monster that spawns by itself (in the wild, from a spawner, in a patrol) may be an elite. Monsters a skill,
     * a quest or a test brings in never are.
     */
    public static void onSpawn(net.minecraftforge.event.entity.living.MobSpawnEvent.FinalizeSpawn event) {
        ServerLevel level = event.getLevel().getLevel();
        if (!(event.getEntity() instanceof Monster mob) || mob instanceof SoFEBossEntity || mob instanceof Ally) return;
        var type = event.getSpawnType();
        if (type != net.minecraft.world.entity.MobSpawnType.NATURAL && type != net.minecraft.world.entity.MobSpawnType.CHUNK_GENERATION
                && type != net.minecraft.world.entity.MobSpawnType.SPAWNER && type != net.minecraft.world.entity.MobSpawnType.PATROL) return;
        CompoundTag tag = mob.getPersistentData();
        if (tag.getBoolean(CHECKED)) return;
        tag.putBoolean(CHECKED, true);
        int act = actNear(level, mob);
        if (level.getRandom().nextDouble() < BASE_CHANCE + PER_ACT * (act - 1)) make(mob, act, level.getRandom());
    }

    private static int actNear(ServerLevel level, Mob mob) {
        var player = level.getNearestPlayer(mob, 96);
        return player == null ? 1 : StoryCapability.get(player).map(s -> Math.max(1, Math.min(5, s.act()))).orElse(1);
    }

    /** Turns a monster into an elite of this act. */
    public static void make(Mob mob, int act, RandomSource random) {
        List<Trait> traits = new ArrayList<>(List.of(Trait.values()));
        java.util.Collections.shuffle(traits, new java.util.Random(random.nextLong()));
        List<Trait> chosen = traits.subList(0, 1 + (random.nextInt(3) == 0 ? 1 : 0));
        mob.getPersistentData().putString(TAG, String.join(",", chosen.stream().map(Enum::name).toList()));
        mob.getPersistentData().putBoolean(CHECKED, true);
        add(mob.getAttribute(Attributes.MAX_HEALTH), HEALTH_ID, HEALTH, AttributeModifier.Operation.MULTIPLY_TOTAL);
        if (chosen.contains(Trait.SWIFT)) add(mob.getAttribute(Attributes.MOVEMENT_SPEED), SPEED_ID, 0.3, AttributeModifier.Operation.MULTIPLY_TOTAL);
        if (chosen.contains(Trait.STONESKIN)) {
            add(mob.getAttribute(Attributes.ARMOR), ARMOR_ID, 8, AttributeModifier.Operation.ADDITION);
            var kb = mob.getAttribute(Attributes.KNOCKBACK_RESISTANCE);
            if (kb != null) kb.setBaseValue(Math.max(kb.getBaseValue(), 0.6));
        }
        if (chosen.contains(Trait.BURNING)) mob.addEffect(new MobEffectInstance(MobEffects.FIRE_RESISTANCE, Integer.MAX_VALUE, 0, false, false));
        mob.setHealth(mob.getMaxHealth());
        equip(mob, act, random);
        Component name = Component.translatable(chosen.get(0).key()).append(" ").append(mob.getType().getDescription());
        mob.setCustomName(name.copy().withStyle(ChatFormatting.GOLD, ChatFormatting.BOLD));
        mob.setCustomNameVisible(true);
        mob.setPersistenceRequired();
    }

    private static void add(AttributeInstance attribute, UUID id, double value, AttributeModifier.Operation operation) {
        if (attribute == null) return;
        attribute.removeModifier(id);
        attribute.addPermanentModifier(new AttributeModifier(id, "SoFE elite", value, operation));
    }

    /** Enchanted armor of the act's metal (iron, then diamond, then netherite), and a weapon for those that hold one. */
    private static void equip(Mob mob, int act, RandomSource random) {
        Item[][] metal = {
                {Items.IRON_HELMET, Items.IRON_CHESTPLATE, Items.IRON_LEGGINGS, Items.IRON_BOOTS, Items.IRON_SWORD},
                {Items.DIAMOND_HELMET, Items.DIAMOND_CHESTPLATE, Items.DIAMOND_LEGGINGS, Items.DIAMOND_BOOTS, Items.DIAMOND_SWORD},
                {Items.NETHERITE_HELMET, Items.NETHERITE_CHESTPLATE, Items.NETHERITE_LEGGINGS, Items.NETHERITE_BOOTS, Items.NETHERITE_SWORD}};
        Item[] set = metal[act <= 2 ? 0 : act <= 4 ? 1 : 2];
        EquipmentSlot[] slots = {EquipmentSlot.HEAD, EquipmentSlot.CHEST, EquipmentSlot.LEGS, EquipmentSlot.FEET};
        int power = 10 + act * 5;
        for (int i = 0; i < 4; i++) {
            if (random.nextInt(4) == 0) continue; // most, not always all, of the pieces
            mob.setItemSlot(slots[i], EnchantmentHelper.enchantItem(random, new ItemStack(set[i]), power, false));
            mob.setDropChance(slots[i], 0f);
        }
        if (mob instanceof AbstractSkeleton) {
            mob.setItemSlot(EquipmentSlot.MAINHAND, EnchantmentHelper.enchantItem(random, new ItemStack(Items.BOW), power, false));
        } else if (mob.getMainHandItem().isEmpty()) {
            mob.setItemSlot(EquipmentSlot.MAINHAND, EnchantmentHelper.enchantItem(random, new ItemStack(set[4]), power, false));
        }
        mob.setDropChance(EquipmentSlot.MAINHAND, 0f);
    }

    private static final DustParticleOptions AURA = new DustParticleOptions(new Vector3f(1f, 0.72f, 0.18f), 1.1f);

    /** The aura: gold motes round the elite, and fire round a burning one. */
    public static void onTick(LivingEvent.LivingTickEvent event) {
        LivingEntity e = event.getEntity();
        if (e.tickCount % 8 != 0 || !(e.level() instanceof ServerLevel level) || !isElite(e)) return;
        level.sendParticles(AURA, e.getX(), e.getY() + e.getBbHeight() * 0.6, e.getZ(), 3, e.getBbWidth() * 0.6, e.getBbHeight() * 0.35, e.getBbWidth() * 0.6, 0);
        if (traits(e).contains(Trait.BURNING)) level.sendParticles(ParticleTypes.FLAME, e.getX(), e.getY() + 0.3, e.getZ(), 2, 0.3, 0.2, 0.3, 0.01);
    }

    /** An elite's blows: Brutal hits harder, Burning sets ablaze, Cursed weakens, Vampiric drinks. */
    public static void onHurt(LivingHurtEvent event) {
        if (!(event.getSource().getEntity() instanceof LivingEntity attacker) || !isElite(attacker)) return;
        List<Trait> traits = traits(attacker);
        float amount = event.getAmount();
        if (traits.contains(Trait.BRUTAL)) amount *= 1.4f;
        if (traits.contains(Trait.BURNING)) event.getEntity().setSecondsOnFire(4);
        if (traits.contains(Trait.CURSED)) event.getEntity().addEffect(new MobEffectInstance(MobEffects.WEAKNESS, 100, 0), attacker);
        if (traits.contains(Trait.VAMPIRIC)) attacker.heal(amount * 0.3f);
        event.setAmount(amount);
    }

    /** What an elite leaves for the Bearer who killed it. */
    public static void onDrops(LivingDropsEvent event) {
        LivingEntity dead = event.getEntity();
        if (!isElite(dead) || !(event.getSource().getEntity() instanceof ServerPlayer player)) return;
        RandomSource random = player.getRandom();
        List<ItemStack> worn = new ArrayList<>();
        for (EquipmentSlot slot : EquipmentSlot.values()) {
            ItemStack s = dead.getItemBySlot(slot);
            if (!s.isEmpty() && s.isEnchanted()) worn.add(s.copy());
        }
        if (!worn.isEmpty()) drop(event, worn.get(random.nextInt(worn.size())));
        double roll = random.nextDouble();
        if (roll < 0.45) {
            Item[] veiled = {ItemRegistry.VEILED_WEAPON.get(), ItemRegistry.VEILED_ARMOR.get(), ItemRegistry.VEILED_JEWELRY.get()};
            drop(event, new ItemStack(veiled[random.nextInt(veiled.length)]));
        } else if (roll < 0.80) {
            GearSlot[] slots = {GearSlot.WEAPON, GearSlot.ARMOR, GearSlot.JEWELRY};
            var generator = GearMaker.builder(player, GearMaker.levelOf(player)).slot(slots[random.nextInt(slots.length)]).rarity(Rarity.IMPERIAL).build();
            GearMaker.roll(generator, random).ifPresent(s -> drop(event, s));
        } else {
            setPiece(player, random).ifPresent(s -> drop(event, s));
        }
        com.sofe.economy.EconomyHandler.refillFlask(player);
        player.displayClientMessage(Component.translatable("message.sofe.elite_slain", dead.getDisplayName()).withStyle(ChatFormatting.GOLD), true);
    }

    /** A piece of an armor set of the player's class, of a set at or below their level. */
    public static Optional<ItemStack> setPiece(ServerPlayer player, RandomSource random) {
        int level = GearMaker.levelOf(player);
        String cls = com.sofe.skill.ClassState.classOf(player).map(com.sofe.player.PlayerClass::id).orElse(null);
        var fitting = GearDataManager.bases().stream()
                .filter(b -> b.slot() == GearSlot.ARMOR && b.minItemLevel() <= level && (cls == null || b.classes().isEmpty() || b.classes().contains(cls)))
                .filter(b -> ForgeRegistries.ITEMS.getValue(net.minecraft.resources.ResourceLocation.tryParse(b.item())) instanceof ArmorItem armor
                        && armor.getMaterial() instanceof com.sofe.gear.SoFETiers.Armor set && com.sofe.gear.ArmorSets.bonusKey(set) != null)
                .toList();
        if (fitting.isEmpty()) return Optional.empty();
        var base = fitting.get(random.nextInt(fitting.size()));
        return GearMaker.rollItem(base.item(), Math.max(base.minItemLevel(), level), Rarity.TEMPERED, player, random);
    }

    private static void drop(LivingDropsEvent event, ItemStack stack) {
        LivingEntity dead = event.getEntity();
        event.getDrops().add(new ItemEntity(dead.level(), dead.getX(), dead.getY() + 0.5, dead.getZ(), stack));
    }

    public static void onExperience(LivingExperienceDropEvent event) {
        if (isElite(event.getEntity())) event.setDroppedExperience(event.getDroppedExperience() * 3);
    }
}
