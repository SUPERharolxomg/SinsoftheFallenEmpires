package com.sofe.economy;

import com.sofe.gear.GearData;
import com.sofe.gear.Rarity;

import java.util.OptionalInt;

/** Prices and exchange rates (docs/Anexos.md, A4). */
public final class Prices {
    /** Selim gives 10 Dinars for an emerald and asks 12 Dinars for one. */
    public static final int DINARS_PER_EMERALD_SOLD = 10, DINARS_PER_EMERALD_BOUGHT = 12;

    private Prices() {
    }

    /** What gear is worth new, before the 25% a merchant pays. */
    public static int base(Rarity rarity) {
        return switch (rarity) {
            case COMMON -> 12;
            case TEMPERED -> 40;
            case IMPERIAL -> 110;
            case RELIC, LEGACY -> 0;
        };
    }

    /**
     * Selling price of gear: rarity base × item level factor × 25%. Relics and Legacy pieces cannot
     * be sold (empty).
     */
    public static OptionalInt sellPrice(GearData gear) {
        if (gear.rarity() == Rarity.RELIC || gear.rarity() == Rarity.LEGACY) return OptionalInt.empty();
        double levelFactor = 1 + gear.itemLevel() / 10.0;
        return OptionalInt.of(Math.max(1, (int) Math.round(base(gear.rarity()) * levelFactor * 0.25)));
    }
}
