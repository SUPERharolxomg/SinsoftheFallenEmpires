package com.sofe.gear;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.EnumSet;
import java.util.Locale;
import java.util.Set;
import java.util.random.RandomGenerator;

/**
 * An affix from data/sofe/affixes/*.json (docs/Pociones.md). Its value is rolled inside a range that
 * grows with the item level: {@code low} is the range at item level 1, {@code high} at item level 30.
 *
 * @param parameter    the class of CLASS_SKILL_RANKS or the skill of SKILL_RANKS; null otherwise
 * @param classBias    the class whose loot favors this affix (+1 Clay Warden for the Necromancer...), or null
 */
public record Affix(String id, Kind kind, GearStat stat, String parameter, double[] low, double[] high,
                    Rarity minRarity, Set<GearSlot> slots, String classBias, int weight) {

    public enum Kind { PREFIX, SUFFIX }

    public static final int MAX_ITEM_LEVEL = 30;

    /** The lowest and highest roll at an item level. */
    public double[] range(int itemLevel) {
        double t = (Math.max(1, Math.min(MAX_ITEM_LEVEL, itemLevel)) - 1) / (double) (MAX_ITEM_LEVEL - 1);
        return new double[]{low[0] + (high[0] - low[0]) * t, low[1] + (high[1] - low[1]) * t};
    }

    /** A value inside the item level's range, a whole number like the tooltips show. */
    public int roll(int itemLevel, RandomGenerator random) {
        double[] r = range(itemLevel);
        int min = (int) Math.round(r[0]), max = Math.max(min, (int) Math.round(r[1]));
        return min + random.nextInt(max - min + 1);
    }

    public boolean fits(Rarity rarity, GearSlot slot) {
        return rarity.atLeast(minRarity) && slots.contains(slot);
    }

    /** "gear.sofe.affix.of_the_bull": the name part shown in generated names. */
    public String translationKey() {
        return "gear.sofe.affix." + id.substring(id.indexOf(':') + 1).replace('/', '.');
    }

    public static Affix parse(String id, JsonObject json) {
        Kind kind = Kind.valueOf(json.get("kind").getAsString().toUpperCase(Locale.ROOT));
        GearStat stat = GearStat.byId(json.get("stat").getAsString());
        String parameter = json.has("parameter") ? json.get("parameter").getAsString() : null;
        if ((stat == GearStat.CLASS_SKILL_RANKS || stat == GearStat.SKILL_RANKS) && parameter == null) {
            throw new IllegalArgumentException(id + ": " + stat.id() + " needs a \"parameter\"");
        }
        double[] low = pair(json, "low"), high = pair(json, "high");
        Rarity minRarity = json.has("min_rarity") ? Rarity.byId(json.get("min_rarity").getAsString()) : Rarity.TEMPERED;
        Set<GearSlot> slots = EnumSet.noneOf(GearSlot.class);
        for (JsonElement e : json.getAsJsonArray("slots")) slots.add(GearSlot.byId(e.getAsString()));
        if (slots.isEmpty()) throw new IllegalArgumentException(id + ": \"slots\" is empty");
        String classBias = json.has("class") ? json.get("class").getAsString() : null;
        int weight = json.has("weight") ? json.get("weight").getAsInt() : 10;
        return new Affix(id, kind, stat, parameter, low, high, minRarity, slots, classBias, weight);
    }

    private static double[] pair(JsonObject json, String key) {
        JsonArray a = json.getAsJsonArray(key);
        if (a == null || a.size() != 2) throw new IllegalArgumentException("\"" + key + "\" must be [min, max]");
        double lo = a.get(0).getAsDouble(), hi = a.get(1).getAsDouble();
        if (hi < lo) throw new IllegalArgumentException("\"" + key + "\" has max below min");
        return new double[]{lo, hi};
    }
}
