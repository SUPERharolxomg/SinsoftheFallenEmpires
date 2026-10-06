package com.sofe.gear;

import com.sofe.player.PlayerClass;
import com.sofe.player.PlayerClassCapability;
import com.sofe.player.PlayerClassData;
import com.sofe.progression.ProgressionCapability;
import com.sofe.progression.ProgressionData;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.List;
import java.util.Optional;
import java.util.random.RandomGenerator;

/** Makes real items out of the loot generator's rolls and out of the Relic definitions. */
public final class GearMaker {
    public static final String SMALL_TALISMAN = "sofe:small_talisman";

    private GearMaker() {
    }

    /** Adapts Minecraft's random to the plain-Java generator. */
    public static RandomGenerator random(RandomSource source) {
        return new RandomGenerator() {
            @Override
            public long nextLong() {
                return source.nextLong();
            }

            @Override
            public int nextInt(int bound) {
                return source.nextInt(bound);
            }

            @Override
            public double nextDouble() {
                return source.nextDouble();
            }
        };
    }

    public static LootGenerator.Builder builder(Player player, int itemLevel) {
        LootGenerator.Builder b = LootGenerator.builder(GearDataManager.bases(), GearDataManager.affixes()).itemLevel(itemLevel);
        if (player != null) PlayerClassCapability.get(player).flatMap(PlayerClassData::get).map(PlayerClass::id).ifPresent(b::playerClass);
        if (player instanceof net.minecraft.server.level.ServerPlayer server) b.findBonus(com.sofe.pact.Pacts.findBonus(server)); // allies near
        return b;
    }

    /** The item level loot gets when no mob level is known: the player's own level. */
    public static int levelOf(Player player) {
        return player == null ? 1 : ProgressionCapability.get(player).map(ProgressionData::level).orElse(1);
    }

    public static Optional<ItemStack> roll(LootGenerator generator, RandomSource random) {
        return generator.generate(random(random)).flatMap(GearMaker::toStack);
    }

    public static Optional<ItemStack> toStack(LootGenerator.Generated generated) {
        Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(generated.item()));
        if (item == null || !(item instanceof SoFEGear)) return Optional.empty();
        ItemStack stack = new ItemStack(item);
        GearData gear = generated.gear();
        // a small talisman holds a single affix, so talismans never replace real gear
        if (generated.item().equals(SMALL_TALISMAN) && gear.affixes().size() > 1) {
            gear = gear.withAffixes(Rarity.TEMPERED, List.of(gear.affixes().get(0))).withNameParts(List.of());
        }
        GearNbt.write(stack, gear);
        return Optional.of(stack);
    }

    /** A specific item rolled at a rarity, as the Imperial Forge makes them (at least one affix). */
    public static Optional<ItemStack> rollItem(String itemId, int itemLevel, Rarity minRarity, Player player, RandomSource random) {
        List<GearBase> base = GearDataManager.bases().stream().filter(b -> b.item().equals(itemId)).toList();
        if (base.isEmpty()) return Optional.empty();
        LootGenerator generator = LootGenerator.builder(base, GearDataManager.affixes()).itemLevel(itemLevel).minRarity(minRarity)
                .playerClass(player == null ? null : PlayerClassCapability.get(player).flatMap(PlayerClassData::get).map(PlayerClass::id).orElse(null))
                .build();
        return roll(generator, random);
    }

    /** The random skill bonus of a class unique weapon: +1 or +2 ranks to one of its class's skills (not an upgrade). */
    public static Optional<GearData.Roll> randomSkillRanks(GearDataManager.Relic relic, RandomSource random) {
        if (relic.slot() != GearSlot.WEAPON || relic.playerClass() == null) return Optional.empty();
        var cls = com.sofe.player.PlayerClass.byId(relic.playerClass());
        if (cls.isEmpty()) return Optional.empty();
        List<com.sofe.skill.SkillInfo> skills = com.sofe.skill.SkillCatalog.forClass(cls.get()).stream()
                .filter(s -> !s.isUpgrade()).toList();
        if (skills.isEmpty()) return Optional.empty();
        var skill = skills.get(random.nextInt(skills.size()));
        return Optional.of(new GearData.Roll("relic:" + relic.id() + ":random_skill", GearStat.SKILL_RANKS, skill.id(), 1 + random.nextInt(2)));
    }

    /**
     * A Relic, bound to its owner (docs/Anexos.md, A6). A class unique weapon also rolls, as Diablo II class items
     * do, one bonus of its own: +1 or +2 ranks to one skill of its class drawn at random (a Necromancer's glaive may
     * come with +2 Clay Warden, another with +1 Scarab Plague), so two copies of it need not be alike.
     */
    public static Optional<ItemStack> relic(String id, Player owner) {
        return GearDataManager.relic(id).flatMap(relic -> {
            Item item = ForgeRegistries.ITEMS.getValue(ResourceLocation.tryParse(relic.item()));
            if (item == null) return Optional.empty();
            ItemStack stack = new ItemStack(item);
            List<GearData.Roll> affixes = new java.util.ArrayList<>(relic.affixes());
            RandomSource random = owner != null ? owner.getRandom() : RandomSource.create();
            randomSkillRanks(relic, random).ifPresent(affixes::add);
            GearNbt.write(stack, new GearData(relic.itemLevel(), Rarity.RELIC, List.copyOf(affixes), null, 0, relic.id(), List.of()));
            if (owner != null) GearNbt.bind(stack, owner);
            return Optional.of(stack);
        });
    }
}
