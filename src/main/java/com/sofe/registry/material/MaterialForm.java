package com.sofe.registry.material;

import java.util.Locale;

/**
 * The shapes a material can take, following vanilla naming (docs/Anexos.md, A3):
 * sofe:&lt;material&gt;_ore, sofe:raw_&lt;material&gt;, sofe:&lt;material&gt;_ingot, ...
 */
public enum MaterialForm {
    ORE(Kind.BLOCK, "%s_ore", "ores"),
    DEEPSLATE_ORE(Kind.BLOCK, "deepslate_%s_ore", "ores"),
    RAW_BLOCK(Kind.BLOCK, "raw_%s_block", "storage_blocks"),
    STORAGE_BLOCK(Kind.BLOCK, "%s_block", "storage_blocks"),
    RAW(Kind.ITEM, "raw_%s", "raw_materials"),
    INGOT(Kind.ITEM, "%s_ingot", "ingots"),
    NUGGET(Kind.ITEM, "%s_nugget", "nuggets"),
    GEM(Kind.ITEM, "%s", "gems"),
    POWDER(Kind.ITEM, "%s_powder", "dusts"),
    /** Aetherium is found as shards; there is no common Forge tag for shards. */
    SHARD(Kind.ITEM, "%s_shard", null);

    public enum Kind { BLOCK, ITEM }

    private final Kind kind;
    private final String pattern;
    private final String forgeTagFolder;

    MaterialForm(Kind kind, String pattern, String forgeTagFolder) {
        this.kind = kind;
        this.pattern = pattern;
        this.forgeTagFolder = forgeTagFolder;
    }

    public Kind kind() {
        return kind;
    }

    public boolean isBlock() {
        return kind == Kind.BLOCK;
    }

    public boolean isOre() {
        return this == ORE || this == DEEPSLATE_ORE;
    }

    /** Registry path for a material, e.g. DEEPSLATE_ORE.id("glacial_iron") is "deepslate_glacial_iron_ore". */
    public String id(String material) {
        return String.format(Locale.ROOT, pattern, material);
    }

    /**
     * Forge tag this form belongs to, e.g. "ingots/glacial_iron" or "storage_blocks/raw_glacial_iron";
     * null when there is no common tag.
     */
    public String forgeTag(String material) {
        if (forgeTagFolder == null) return null;
        String name = this == RAW_BLOCK ? "raw_" + material : material;
        return forgeTagFolder + "/" + name;
    }

    public String forgeTagFolder() {
        return forgeTagFolder;
    }
}
