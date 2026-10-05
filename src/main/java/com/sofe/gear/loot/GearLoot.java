package com.sofe.gear.loot;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonObject;
import com.google.gson.JsonSerializationContext;
import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sofe.SoFEMod;
import com.sofe.entity.boss.SoFEBossEntity;
import com.sofe.gear.GearMaker;
import com.sofe.gear.GearSlot;
import com.sofe.gear.LootGenerator;
import com.sofe.gear.Rarity;
import com.sofe.item.BlueprintItem;
import com.sofe.mob.MobLevels;
import com.sofe.registry.ItemRegistry;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.loot.LootContext;
import net.minecraft.world.level.storage.loot.functions.LootItemConditionalFunction;
import net.minecraft.world.level.storage.loot.functions.LootItemFunctionType;
import net.minecraft.world.level.storage.loot.parameters.LootContextParams;
import net.minecraft.world.level.storage.loot.predicates.LootItemCondition;
import net.minecraftforge.common.loot.IGlobalLootModifier;
import net.minecraftforge.common.loot.LootModifier;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.Optional;

/**
 * Loot that comes from code (UC-09): the loot functions "sofe:roll_gear", "sofe:set_blueprint" and
 * "sofe:make_relic" used by the boss tables, and the global loot modifier that gives every levelled
 * enemy of a journey a chance of gear and Dinars.
 */
public final class GearLoot {
    public static final DeferredRegister<LootItemFunctionType> FUNCTIONS = DeferredRegister.create(Registries.LOOT_FUNCTION_TYPE, SoFEMod.MOD_ID);
    public static final DeferredRegister<Codec<? extends IGlobalLootModifier>> MODIFIERS =
            DeferredRegister.create(ForgeRegistries.Keys.GLOBAL_LOOT_MODIFIER_SERIALIZERS, SoFEMod.MOD_ID);

    public static final RegistryObject<LootItemFunctionType> ROLL_GEAR = FUNCTIONS.register("roll_gear", () -> new LootItemFunctionType(new RollGear.Serializer()));
    public static final RegistryObject<LootItemFunctionType> SET_BLUEPRINT = FUNCTIONS.register("set_blueprint", () -> new LootItemFunctionType(new SetBlueprint.Serializer()));
    public static final RegistryObject<LootItemFunctionType> MAKE_RELIC = FUNCTIONS.register("make_relic", () -> new LootItemFunctionType(new MakeRelic.Serializer()));
    public static final RegistryObject<Codec<GearDrops>> GEAR_DROPS = MODIFIERS.register("gear_drops", () -> GearDrops.CODEC);
    public static final RegistryObject<Codec<AddItem>> ADD_ITEM = MODIFIERS.register("add_item", () -> AddItem.CODEC);

    private GearLoot() {
    }

    static Player player(LootContext context) {
        Player last = context.getParamOrNull(LootContextParams.LAST_DAMAGE_PLAYER);
        if (last != null) return last;
        return context.getParamOrNull(LootContextParams.KILLER_ENTITY) instanceof Player p ? p : null;
    }

    /** Item level: the level of the enemy that dropped it, or the player's own level. */
    static int itemLevel(LootContext context, Player player) {
        Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
        Optional<Integer> mobLevel = entity == null ? Optional.empty() : MobLevels.levelOf(entity);
        return mobLevel.orElse(GearMaker.levelOf(player));
    }

    /** Replaces the item with random gear of at least a rarity (and of one slot, if given). */
    public static class RollGear extends LootItemConditionalFunction {
        private final Rarity minRarity;
        private final GearSlot slot;

        RollGear(LootItemCondition[] conditions, Rarity minRarity, GearSlot slot) {
            super(conditions);
            this.minRarity = minRarity;
            this.slot = slot;
        }

        @Override
        protected ItemStack run(ItemStack stack, LootContext context) {
            Player player = player(context);
            LootGenerator.Builder builder = GearMaker.builder(player, itemLevel(context, player)).minRarity(minRarity);
            if (slot != null) builder.slot(slot);
            return GearMaker.roll(builder.build(), context.getRandom()).orElse(ItemStack.EMPTY);
        }

        @Override
        public LootItemFunctionType getType() {
            return ROLL_GEAR.get();
        }

        public static class Serializer extends LootItemConditionalFunction.Serializer<RollGear> {
            @Override
            public void serialize(JsonObject json, RollGear function, JsonSerializationContext context) {
                super.serialize(json, function, context);
                json.addProperty("min_rarity", function.minRarity.id());
                if (function.slot != null) json.addProperty("slot", function.slot.id());
            }

            @Override
            public RollGear deserialize(JsonObject json, JsonDeserializationContext context, LootItemCondition[] conditions) {
                return new RollGear(conditions, json.has("min_rarity") ? Rarity.byId(json.get("min_rarity").getAsString()) : Rarity.COMMON,
                        json.has("slot") ? GearSlot.byId(json.get("slot").getAsString()) : null);
            }
        }
    }

    /** Makes the item a Blueprint of one Imperial Forge recipe. */
    public static class SetBlueprint extends LootItemConditionalFunction {
        private final String blueprint;

        SetBlueprint(LootItemCondition[] conditions, String blueprint) {
            super(conditions);
            this.blueprint = blueprint;
        }

        @Override
        protected ItemStack run(ItemStack stack, LootContext context) {
            if (blueprint != null) return BlueprintItem.of(blueprint);
            var recipes = context.getLevel().getRecipeManager().getAllRecipesFor(com.sofe.registry.SoFERecipes.IMPERIAL_FORGE.get());
            if (recipes.isEmpty()) return stack;
            Player player = player(context);
            var economy = player == null ? null : com.sofe.economy.EconomyCapability.get(player).orElse(null);
            var unknown = recipes.stream().filter(r -> economy == null || !economy.knows(r.blueprint())).toList();
            var pool = unknown.isEmpty() ? recipes : unknown;
            return BlueprintItem.of(pool.get(context.getRandom().nextInt(pool.size())).blueprint());
        }

        @Override
        public LootItemFunctionType getType() {
            return SET_BLUEPRINT.get();
        }

        public static class Serializer extends LootItemConditionalFunction.Serializer<SetBlueprint> {
            @Override
            public void serialize(JsonObject json, SetBlueprint function, JsonSerializationContext context) {
                super.serialize(json, function, context);
                if (function.blueprint != null) json.addProperty("blueprint", function.blueprint);
            }

            @Override
            public SetBlueprint deserialize(JsonObject json, JsonDeserializationContext context, LootItemCondition[] conditions) {
                // without "blueprint": one the Bearer does not know yet, chosen when the loot is rolled
                return new SetBlueprint(conditions, json.has("blueprint") ? json.get("blueprint").getAsString() : null);
            }
        }
    }

    /** Makes the item a Relic, bound to the player who earned it. */
    public static class MakeRelic extends LootItemConditionalFunction {
        private final String relic;

        MakeRelic(LootItemCondition[] conditions, String relic) {
            super(conditions);
            this.relic = relic;
        }

        @Override
        protected ItemStack run(ItemStack stack, LootContext context) {
            return GearMaker.relic(relic, player(context)).orElse(stack);
        }

        @Override
        public LootItemFunctionType getType() {
            return MAKE_RELIC.get();
        }

        public static class Serializer extends LootItemConditionalFunction.Serializer<MakeRelic> {
            @Override
            public void serialize(JsonObject json, MakeRelic function, JsonSerializationContext context) {
                super.serialize(json, function, context);
                json.addProperty("relic", function.relic);
            }

            @Override
            public MakeRelic deserialize(JsonObject json, JsonDeserializationContext context, LootItemCondition[] conditions) {
                return new MakeRelic(conditions, json.get("relic").getAsString());
            }
        }
    }

    /**
     * The global loot modifier (docs/Pociones.md, "Loot"): every levelled enemy of a journey may drop
     * gear of its level and some Dinars. SoFE's own enemies drop gear twice as often as vanilla ones.
     * Bosses have their own tables and are left out.
     */
    public static class GearDrops extends LootModifier {
        /** Of the gear an enemy drops, how much is one of the droppable Relics of its level instead (a "unique"). */
        public static final double RELIC_CHANCE = 0.02;
        public static final Codec<GearDrops> CODEC = RecordCodecBuilder.create(i -> codecStart(i).and(i.group(
                Codec.DOUBLE.fieldOf("gear_chance").forGetter(m -> m.gearChance),
                Codec.DOUBLE.fieldOf("dinars_chance").forGetter(m -> m.dinarsChance),
                Codec.INT.listOf().fieldOf("dinars").forGetter(m -> java.util.List.of(m.dinarsMin, m.dinarsMax))
        )).apply(i, GearDrops::new));

        private final double gearChance, dinarsChance;
        private final int dinarsMin, dinarsMax;

        GearDrops(LootItemCondition[] conditions, double gearChance, double dinarsChance, java.util.List<Integer> dinars) {
            super(conditions);
            this.gearChance = gearChance;
            this.dinarsChance = dinarsChance;
            this.dinarsMin = dinars.isEmpty() ? 1 : dinars.get(0);
            this.dinarsMax = dinars.size() < 2 ? dinarsMin : dinars.get(1);
        }

        @Override
        protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
            Entity entity = context.getParamOrNull(LootContextParams.THIS_ENTITY);
            if (entity == null || entity instanceof SoFEBossEntity || MobLevels.levelOf(entity).isEmpty()) return loot;
            Player player = player(context);
            var key = ForgeRegistries.ENTITY_TYPES.getKey(entity.getType());
            double chance = gearChance * (key != null && key.getNamespace().equals(SoFEMod.MOD_ID) ? 1 : 0.5);
            if (context.getRandom().nextDouble() < chance) {
                int level = itemLevel(context, player);
                Optional<ItemStack> unique = context.getRandom().nextDouble() < RELIC_CHANCE
                        ? com.sofe.gear.Gamble.pickRelic(com.sofe.gear.GearDataManager.droppableRelicsFor(
                                player == null ? null : com.sofe.skill.ClassState.classOf(player).map(com.sofe.player.PlayerClass::id).orElse(null)), null, level - com.sofe.gear.Gamble.RELIC_LEVEL_MARGIN,
                                GearMaker.random(context.getRandom())).flatMap(r -> GearMaker.relic(r.id(), null))
                        : Optional.empty();
                if (unique.isPresent()) loot.add(unique.get());
                else GearMaker.roll(GearMaker.builder(player, level).build(), context.getRandom()).ifPresent(loot::add);
            }
            if (context.getRandom().nextDouble() < dinarsChance) {
                int amount = dinarsMin + context.getRandom().nextInt(Math.max(1, dinarsMax - dinarsMin + 1));
                loot.add(new ItemStack(ItemRegistry.DINAR.get(), amount));
            }
            return loot;
        }

        @Override
        public Codec<? extends IGlobalLootModifier> codec() {
            return GEAR_DROPS.get();
        }
    }

    /**
     * Adds an item to a loot table's roll (chosen by the conditions, e.g. forge:loot_table_id), at a chance and in a
     * count range: Void Crystal in the End cities' chests (docs/Mundo.md).
     */
    public static class AddItem extends LootModifier {
        public static final Codec<AddItem> CODEC = RecordCodecBuilder.create(i -> codecStart(i).and(i.group(
                ForgeRegistries.ITEMS.getCodec().fieldOf("item").forGetter(m -> m.item),
                Codec.DOUBLE.fieldOf("chance").forGetter(m -> m.chance),
                Codec.INT.fieldOf("min").forGetter(m -> m.min),
                Codec.INT.fieldOf("max").forGetter(m -> m.max)
        )).apply(i, AddItem::new));

        private final net.minecraft.world.item.Item item;
        private final double chance;
        private final int min, max;

        AddItem(LootItemCondition[] conditions, net.minecraft.world.item.Item item, double chance, int min, int max) {
            super(conditions);
            this.item = item;
            this.chance = chance;
            this.min = min;
            this.max = Math.max(min, max);
        }

        @Override
        protected ObjectArrayList<ItemStack> doApply(ObjectArrayList<ItemStack> loot, LootContext context) {
            if (context.getRandom().nextDouble() < chance) loot.add(new ItemStack(item, min + context.getRandom().nextInt(max - min + 1)));
            return loot;
        }

        @Override
        public Codec<? extends IGlobalLootModifier> codec() {
            return ADD_ITEM.get();
        }
    }
}
