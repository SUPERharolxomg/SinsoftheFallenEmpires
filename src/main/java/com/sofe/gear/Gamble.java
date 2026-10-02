package com.sofe.gear;

import java.util.List;
import java.util.Optional;
import java.util.random.RandomGenerator;

/**
 * The gamble of the veiled wares (Kasim in Sulthari, Hrafna in Skarnhold): the buyer pays for a covered
 * weapon, armor piece or jewel without knowing what is under the cloth, as with Diablo II's Gheed. Most
 * veils hold plain gear, some something fine, a few a Relic, and some nothing at all. Plain Java, tested
 * without Minecraft.
 */
public final class Gamble {
    /** What a veil holds. */
    public enum Outcome {
        /** Nothing but a handful of Void ash: the merchant swindled you. */
        SWINDLE(8),
        COMMON(47),
        TEMPERED(28),
        IMPERIAL(14),
        /** One of the droppable Relics of the slot and level (Imperial when none fits). */
        RELIC(3);

        private final double weight;

        Outcome(double weight) {
            this.weight = weight;
        }

        public double weight() {
            return weight;
        }

        /** The rarity the loot generator rolls for this outcome. */
        public Rarity rarity() {
            return switch (this) {
                case TEMPERED -> Rarity.TEMPERED;
                case IMPERIAL, RELIC -> Rarity.IMPERIAL;
                default -> Rarity.COMMON;
            };
        }

        public String messageKey() {
            return "message.sofe.gamble." + name().toLowerCase(java.util.Locale.ROOT);
        }
    }

    /** Veiled gear comes up to this many levels above the buyer. */
    public static final int LEVEL_SPREAD = 2;
    /** A Relic out of a veil may be this many levels above the item's level. */
    public static final int RELIC_LEVEL_MARGIN = 3;
    /** Jewelry veils hide Relics twice as often (rings were the gambler's prize in Diablo II too). */
    public static final double JEWELRY_RELIC_FACTOR = 2;

    private Gamble() {
    }

    public static Outcome roll(RandomGenerator random, GearSlot slot) {
        double relic = Outcome.RELIC.weight() * (slot == GearSlot.JEWELRY ? JEWELRY_RELIC_FACTOR : 1);
        double total = 0;
        for (Outcome o : Outcome.values()) total += o == Outcome.RELIC ? relic : o.weight();
        double r = random.nextDouble() * total;
        for (Outcome o : Outcome.values()) {
            r -= o == Outcome.RELIC ? relic : o.weight();
            if (r < 0) return o;
        }
        return Outcome.COMMON;
    }

    /** The item level of a veiled piece bought at this player level. */
    public static int itemLevel(int playerLevel, RandomGenerator random) {
        return Math.max(1, Math.min(Affix.MAX_ITEM_LEVEL, playerLevel + random.nextInt(LEVEL_SPREAD + 1)));
    }

    /** A droppable Relic of the slot that fits the item level, or empty when none does. */
    public static Optional<GearDataManager.Relic> pickRelic(List<GearDataManager.Relic> relics, GearSlot slot, int itemLevel,
                                                            RandomGenerator random) {
        List<GearDataManager.Relic> fitting = relics.stream()
                .filter(r -> r.droppable() && (slot == null || r.slot() == slot) && r.minLevel() <= itemLevel + RELIC_LEVEL_MARGIN)
                .toList();
        return fitting.isEmpty() ? Optional.empty() : Optional.of(fitting.get(random.nextInt(fitting.size())));
    }
}
