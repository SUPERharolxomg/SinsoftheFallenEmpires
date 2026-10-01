package com.sofe.gear;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.sofe.progression.CharacterAttribute;

import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * An item the loot generator can make, from data/sofe/gear_bases/*.json: the item, its slot, the
 * classes it suits (for the class bias) and the attribute heavy pieces ask for.
 */
public record GearBase(String item, GearSlot slot, Set<String> classes, int minItemLevel, CharacterAttribute requiredAttribute, int requiredValue) {

    public GearBase {
        classes = Set.copyOf(classes);
    }

    public boolean suits(String playerClass) {
        return playerClass != null && classes.contains(playerClass);
    }

    public static GearBase parse(JsonObject json) {
        Set<String> classes = new HashSet<>();
        if (json.has("classes")) for (JsonElement e : json.getAsJsonArray("classes")) classes.add(e.getAsString());
        CharacterAttribute attribute = null;
        int value = 0;
        if (json.has("requires")) {
            JsonObject r = json.getAsJsonObject("requires");
            attribute = CharacterAttribute.valueOf(r.get("attribute").getAsString().toUpperCase(Locale.ROOT));
            value = r.get("value").getAsInt();
        }
        return new GearBase(json.get("item").getAsString(), GearSlot.byId(json.get("slot").getAsString()), classes,
                json.has("min_item_level") ? json.get("min_item_level").getAsInt() : 1, attribute, value);
    }
}
