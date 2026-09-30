package com.sofe.registry.material;

import java.util.Collections;
import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;

import static com.sofe.registry.material.MaterialForm.*;

/**
 * The empire materials (docs/Anexos.md A3, docs/Pociones.md). Worldgen, recipes and tool
 * tiers come in later sprints; this defines what exists and how it is named.
 */
public enum Material {
    SULTHARI_BRASS(MiningLevel.STONE, ORE, RAW, RAW_BLOCK, INGOT, NUGGET, STORAGE_BLOCK),
    GLACIAL_IRON(MiningLevel.IRON, ORE, DEEPSLATE_ORE, RAW, RAW_BLOCK, INGOT, NUGGET, STORAGE_BLOCK),
    STAR_LAPIS(MiningLevel.IRON, ORE, GEM, POWDER, STORAGE_BLOCK),
    SOLAR_GOLD(MiningLevel.DIAMOND, ORE, DEEPSLATE_ORE, RAW, RAW_BLOCK, INGOT, NUGGET, STORAGE_BLOCK, POWDER),
    ORICHALCUM(MiningLevel.DIAMOND, DEEPSLATE_ORE, RAW, RAW_BLOCK, INGOT, NUGGET, STORAGE_BLOCK),
    AETHERIUM(MiningLevel.DIAMOND, DEEPSLATE_ORE, SHARD, STORAGE_BLOCK),
    /** Dropped by corrupted enemies and purified into aetherium; never mined. */
    BLACK_AETHERIUM(MiningLevel.STONE, SHARD);

    private final MiningLevel miningLevel;
    private final Set<MaterialForm> forms;

    Material(MiningLevel miningLevel, MaterialForm... forms) {
        this.miningLevel = miningLevel;
        EnumSet<MaterialForm> set = EnumSet.noneOf(MaterialForm.class);
        Collections.addAll(set, forms);
        this.forms = Collections.unmodifiableSet(set);
    }

    public String id() {
        return name().toLowerCase(Locale.ROOT);
    }

    /** Forms in declaration order of {@link MaterialForm}. */
    public Set<MaterialForm> forms() {
        return forms;
    }

    public boolean has(MaterialForm form) {
        return forms.contains(form);
    }

    public MiningLevel miningLevel() {
        return miningLevel;
    }

    public String id(MaterialForm form) {
        if (!has(form)) {
            throw new IllegalArgumentException(this + " has no " + form);
        }
        return form.id(id());
    }

    /** What the ore drops when mined: the raw metal, or the gem or shard for non-metals. */
    public MaterialForm oreDrop() {
        if (has(RAW)) return RAW;
        if (has(GEM)) return GEM;
        if (has(SHARD)) return SHARD;
        throw new IllegalStateException(this + " has no ore drop");
    }

    public boolean hasOre() {
        return has(ORE) || has(DEEPSLATE_ORE);
    }

    /** Lang key, e.g. "block.sofe.glacial_iron_ore" or "item.sofe.glacial_iron_ingot". */
    public String translationKey(MaterialForm form) {
        return (form.isBlock() ? "block" : "item") + ".sofe." + id(form);
    }
}
