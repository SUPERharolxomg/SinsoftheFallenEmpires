package com.sofe.gear;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.random.RandomGenerator;

/**
 * Rolls random gear (UC-09): base item, rarity, affixes and requirements. Built with a Builder; the
 * class bias makes about 60% of the loot suit the player's class, so the inventory does not fill with
 * junk (docs/Pociones.md, "Class affinity"). Plain Java, tested without Minecraft.
 */
public final class LootGenerator {
    /** How many first and second parts the generated Imperial names have (gear.sofe.name.first.N / second.N). */
    public static final int NAME_FIRST_PARTS = 12, NAME_SECOND_PARTS = 12;
    private static final int MAX_PER_KIND = 2;

    /** What the generator made: the item to create and its gear data. */
    public record Generated(String item, GearSlot slot, GearData gear) {
    }

    private final List<GearBase> bases;
    private final List<Affix> affixes;
    private final int itemLevel;
    private final String playerClass;
    private final double classBias;
    private final double findBonus;
    private final Rarity minRarity;
    private final GearSlot slot;
    private final Rarity fixedRarity;

    private LootGenerator(Builder b) {
        this.bases = List.copyOf(b.bases);
        this.affixes = List.copyOf(b.affixes);
        this.itemLevel = Math.max(1, Math.min(Affix.MAX_ITEM_LEVEL, b.itemLevel));
        this.playerClass = b.playerClass;
        this.classBias = b.classBias;
        this.findBonus = b.findBonus;
        this.minRarity = b.minRarity;
        this.slot = b.slot;
        this.fixedRarity = b.fixedRarity;
    }

    public static Builder builder(List<GearBase> bases, List<Affix> affixes) {
        return new Builder(bases, affixes);
    }

    public static final class Builder {
        private final List<GearBase> bases;
        private final List<Affix> affixes;
        private int itemLevel = 1;
        private String playerClass;
        private double classBias = 0.6;
        private double findBonus;
        private Rarity minRarity = Rarity.COMMON;
        private GearSlot slot;
        private Rarity fixedRarity;

        private Builder(List<GearBase> bases, List<Affix> affixes) {
            this.bases = bases;
            this.affixes = affixes;
        }

        public Builder itemLevel(int level) {
            this.itemLevel = level;
            return this;
        }

        /** The class of the player the loot is for (its id, e.g. "sorceress"). */
        public Builder playerClass(String id) {
            this.playerClass = id;
            return this;
        }

        public Builder classBias(double bias) {
            this.classBias = bias;
            return this;
        }

        /** Better rarity odds, e.g. 0.1 for +10% (Pact bonus, luck). */
        public Builder findBonus(double bonus) {
            this.findBonus = bonus;
            return this;
        }

        /** Elites and Broken Oaths never drop below this. */
        public Builder minRarity(Rarity rarity) {
            this.minRarity = rarity;
            return this;
        }

        public Builder slot(GearSlot only) {
            this.slot = only;
            return this;
        }

        /** Exactly this rarity, as a gamble decides it beforehand (one of the random rarities). */
        public Builder rarity(Rarity exactly) {
            this.fixedRarity = exactly;
            return this;
        }

        public LootGenerator build() {
            return new LootGenerator(this);
        }
    }

    public Optional<Generated> generate(RandomGenerator random) {
        Optional<GearBase> base = pickBase(random);
        if (base.isEmpty()) return Optional.empty();
        Rarity rarity = fixedRarity != null ? fixedRarity : rollRarity(random);
        GearData gear = GearData.common(itemLevel);
        if (base.get().requiredAttribute() != null) gear = gear.withRequirement(base.get().requiredAttribute(), base.get().requiredValue());
        List<GearData.Roll> rolls = rollAffixes(random, rarity, base.get());
        gear = gear.withAffixes(rarity, rolls);
        if (rarity == Rarity.IMPERIAL) gear = gear.withNameParts(List.of(random.nextInt(NAME_FIRST_PARTS), random.nextInt(NAME_SECOND_PARTS)));
        return Optional.of(new Generated(base.get().item(), base.get().slot(), gear));
    }

    /** With the class bias, pick among the bases that suit the player's class first. */
    Optional<GearBase> pickBase(RandomGenerator random) {
        List<GearBase> fitting = bases.stream().filter(b -> (slot == null || b.slot() == slot) && b.minItemLevel() <= itemLevel).toList();
        if (fitting.isEmpty()) return Optional.empty();
        List<GearBase> suited = fitting.stream().filter(b -> b.suits(playerClass)).toList();
        List<GearBase> pool = !suited.isEmpty() && random.nextDouble() < classBias ? suited : fitting;
        return Optional.of(pool.get(random.nextInt(pool.size())));
    }

    /** Common 70, Tempered 25, Imperial 5 at level 1; higher levels and find bonus shift toward the better ones. */
    Rarity rollRarity(RandomGenerator random) {
        double common = 70, tempered = 25 * (1 + findBonus), imperial = (5 + itemLevel * 0.4) * (1 + findBonus);
        if (minRarity.atLeast(Rarity.TEMPERED)) common = 0;
        if (minRarity.atLeast(Rarity.IMPERIAL)) tempered = 0;
        double roll = random.nextDouble() * (common + tempered + imperial);
        if (roll < common) return Rarity.COMMON;
        if (roll < common + tempered) return Rarity.TEMPERED;
        return Rarity.IMPERIAL;
    }

    /** Distinct affixes that fit the rarity and slot, at most two prefixes and two suffixes. */
    List<GearData.Roll> rollAffixes(RandomGenerator random, Rarity rarity, GearBase base) {
        List<GearData.Roll> rolls = new ArrayList<>();
        if (rarity.maxAffixes() == 0) return rolls;
        int count = rarity.minAffixes() + random.nextInt(rarity.maxAffixes() - rarity.minAffixes() + 1);
        List<Affix> candidates = new ArrayList<>(affixes.stream()
                .filter(a -> a.fits(rarity, base.slot()))
                .filter(a -> a.classBias() == null || base.suits(a.classBias()) || a.classBias().equals(playerClass))
                .toList());
        Set<String> statsTaken = new HashSet<>();
        int prefixes = 0, suffixes = 0;
        while (rolls.size() < count && !candidates.isEmpty()) {
            Affix affix = weighted(candidates, random);
            candidates.remove(affix);
            String statKey = affix.stat().id() + ":" + affix.parameter();
            if (!statsTaken.add(statKey)) continue;
            if (affix.kind() == Affix.Kind.PREFIX ? prefixes >= MAX_PER_KIND : suffixes >= MAX_PER_KIND) continue;
            if (affix.kind() == Affix.Kind.PREFIX) prefixes++;
            else suffixes++;
            rolls.add(new GearData.Roll(affix.id(), affix.stat(), affix.parameter(), affix.roll(itemLevel, random)));
        }
        return rolls;
    }

    /** Affixes biased toward the player's class weigh three times as much. */
    private Affix weighted(List<Affix> candidates, RandomGenerator random) {
        double total = 0;
        for (Affix a : candidates) total += weight(a);
        double roll = random.nextDouble() * total;
        for (Affix a : candidates) {
            roll -= weight(a);
            if (roll < 0) return a;
        }
        return candidates.get(candidates.size() - 1);
    }

    private double weight(Affix affix) {
        return affix.weight() * (affix.classBias() != null && affix.classBias().equals(playerClass) ? 3 : 1);
    }
}
