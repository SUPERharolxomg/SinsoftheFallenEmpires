package com.sofe.gear;

import com.sofe.world.region.Region;

import java.util.Locale;
import java.util.Optional;

/**
 * The seven sin gems (docs/Anexos.md, "Sin gems"; docs/Pociones.md, "Gems"). Each comes rough (3% when mining a mod
 * ore, the gem of that region's sin), cut (at the Jeweler) or as an Oath Gem (perfect, from the Broken Oaths, twice
 * as strong). Cut and Oath gems are set in the sockets of weapons and chestplates (Sockets), where they give their
 * stat like an affix.
 */
public enum SinGem {
    WRATH_RUBY(GearStat.PHYSICAL_DAMAGE, 6, 0xD8243A),
    LUST_AMETHYST(GearStat.LIFE_STEAL, 2, 0xC050E0),
    GREED_TOPAZ(GearStat.CHARISMA, 4, 0xF0B030),
    SLOTH_MOONSTONE(GearStat.ARMOR, 6, 0xB8D0F0),
    GLUTTONY_AMBER(GearStat.MAX_HEALTH, 10, 0xE07818),
    ENVY_EMERALD(GearStat.CRIT_CHANCE, 3, 0x30D070),
    PRIDE_SUNSTONE(GearStat.ALL_ATTRIBUTES, 2, 0xFFE070);

    public enum Form {
        ROUGH, CUT, OATH;

        public String id() {
            return name().toLowerCase(Locale.ROOT);
        }
    }

    private final GearStat stat;
    private final int cutValue;
    private final int color;

    SinGem(GearStat stat, int cutValue, int color) {
        this.stat = stat;
        this.cutValue = cutValue;
        this.color = color;
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    /** "rough_wrath_ruby", "cut_wrath_ruby", "oath_wrath_ruby". */
    public String id(Form form) {
        return form.id() + "_" + id();
    }

    public GearStat stat() {
        return stat;
    }

    public int color() {
        return color;
    }

    /** What a set gem gives: a cut gem its value, an Oath Gem twice that; a rough gem cannot be set. */
    public int value(Form form) {
        return switch (form) {
            case ROUGH -> 0;
            case CUT -> cutValue;
            case OATH -> cutValue * 2;
        };
    }

    /** The gem and form an item id names ("sofe:cut_envy_emerald"), if it is a sin gem. */
    public static Optional<Found> of(String itemId) {
        String path = itemId.contains(":") ? itemId.substring(itemId.indexOf(':') + 1) : itemId;
        for (Form form : Form.values()) {
            if (!path.startsWith(form.id() + "_")) continue;
            String rest = path.substring(form.id().length() + 1);
            for (SinGem gem : values()) if (gem.id().equals(rest)) return Optional.of(new Found(gem, form));
        }
        return Optional.empty();
    }

    public record Found(SinGem gem, Form form) {
    }

    /**
     * The rough gem a mod ore can give in a region (docs/Mundo.md, "Sin gems by region"): Nordrath's Ruby (Amber
     * below y 0), Parsivan's Amethyst, Khemet's Moonstone, Aureum's Topaz (Emerald below y 0, in its ruins), and the
     * Sunstone in Sulthari only in the fifth act. None elsewhere.
     */
    public static Optional<SinGem> minedIn(Region region, int y, int act) {
        return Optional.ofNullable(switch (region) {
            case NORDRATH -> y < 0 ? GLUTTONY_AMBER : WRATH_RUBY;
            case PARSIVAN -> LUST_AMETHYST;
            case KHEMET -> SLOTH_MOONSTONE;
            case AUREUM -> y < 0 ? ENVY_EMERALD : GREED_TOPAZ;
            case SULTHARI -> act >= 5 ? PRIDE_SUNSTONE : null;
            default -> null;
        });
    }
}
