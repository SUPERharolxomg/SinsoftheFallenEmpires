"""Writes what the game needs to know about the armor of scripts/armor_catalog.py: the armor materials and
items of the new sets (the generated blocks of SoFETiers.java and ItemRegistry.java), the English and
Spanish names, the gear bases (class, level and the Strength heavy pieces ask for) and the full-set
bonuses (data/sofe/armor_sets). Run from the repository root after editing the catalog:
python scripts/make_armor_data.py
"""
import collections
import json
import os
import re
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from armor_catalog import SETS  # noqa: E402

JAVA = os.path.join("src", "main", "java", "com", "sofe")
RES = os.path.join("src", "main", "resources")
DATA = os.path.join(RES, "data", "sofe")
LANG = os.path.join(RES, "assets", "sofe", "lang")
PIECES = ("helmet", "chestplate", "leggings", "boots")
TYPES = {"helmet": "HELMET", "chestplate": "CHESTPLATE", "leggings": "LEGGINGS", "boots": "BOOTS"}
STYLE_NAMES = {  # piece names: (en, es) for helmet, chestplate, leggings, boots
    "plate": (("Helmet", "Yelmo"), ("Chestplate", "Peto"), ("Leggings", "Grebas"), ("Boots", "Botas")),
    "robe": (("Hat", "Sombrero"), ("Robe", "Túnica"), ("Trousers", "Calzas"), ("Slippers", "Zapatillas")),
    "leather": (("Hood", "Capucha"), ("Jerkin", "Jubón"), ("Breeches", "Calzones"), ("Boots", "Botas")),
    "royal": (("Crown", "Corona"), ("Robes", "Ropajes"), ("Trousers", "Calzas"), ("Slippers", "Babuchas")),
}
REPAIR = {"knight": "() -> MaterialRegistry.item(Material.GLACIAL_IRON, MaterialForm.INGOT)",
          "necromancer": "() -> MaterialRegistry.item(Material.BLACK_AETHERIUM, MaterialForm.SHARD)",
          "sorceress": "() -> MaterialRegistry.item(Material.AETHERIUM, MaterialForm.SHARD)",
          "thief": "() -> com.sofe.registry.ItemRegistry.FROSTPELT.get()",
          "king": "() -> MaterialRegistry.item(Material.SOLAR_GOLD, MaterialForm.INGOT)"}
CLASS_ORDER = ("knight", "necromancer", "sorceress", "thief", "king")


def existing_materials():
    src = open(os.path.join(JAVA, "gear", "SoFETiers.java"), encoding="utf-8").read()
    head = src.split("// <generated-armor>")[0]
    return set(m.lower() for m in re.findall(r"^\s+([A-Z_]+)\(\"", head, re.M))


def stats(s):
    """Protection, toughness and the rest from the level and the weight."""
    lvl, w = s["level"], s["weight"]
    tier = 0 if lvl <= 3 else 1 if lvl <= 7 else 2 if lvl <= 10 else 3
    base = [[2, 5, 6, 2], [3, 5, 7, 3], [3, 6, 8, 3], [3, 7, 9, 3]][tier]
    cut = {"heavy": 0, "medium": 1, "light": 2}[w]
    prot = [max(1, v - cut) for v in base]
    tough = {"heavy": min(3.0, 0.25 * lvl), "medium": round(0.12 * lvl, 2), "light": 0.0}[w]
    kb = 0.1 if w == "heavy" and lvl >= 8 else 0.0
    ench = {"heavy": 10, "medium": 14, "light": 18}[w]
    return 10 + lvl * 2 + (4 if w == "heavy" else 0), prot, ench, tough, kb


def requirement(s):
    if s["weight"] == "heavy":
        return {"attribute": "strength", "value": 10 + 2 * s["level"]}
    if s["weight"] == "medium":
        return {"attribute": "strength", "value": 8 + s["level"]}
    return None


def replace_block(path, start, end, text):
    src = open(path, encoding="utf-8").read()
    i, j = src.index(start), src.index(end) + len(end)
    open(path, "w", encoding="utf-8").write(src[:i] + text + src[j:])


def main():
    old = existing_materials()
    new = [s for s in SETS if s["name"] not in old]
    # the materials
    lines = ["// <generated-armor> by scripts/make_armor_data.py from scripts/armor_catalog.py: the class sets"]
    for i, s in enumerate(new):
        dur, prot, ench, tough, kb = stats(s)
        end = ";" if i == len(new) - 1 else ","
        lines.append('        %s("%s", %d, new int[]{%s}, %d, %sf, %sf, %s)%s' % (
            s["name"].upper(), s["name"], dur, ", ".join(map(str, prot)), ench, tough, kb, REPAIR[s["cls"]], end))
    lines.append("        // </generated-armor>")
    replace_block(os.path.join(JAVA, "gear", "SoFETiers.java"), "// <generated-armor>", "// </generated-armor>", "\n".join(lines))
    # the items, and every set piece in class order
    lines = ["// <generated-armor> by scripts/make_armor_data.py from scripts/armor_catalog.py: the class sets"]
    for s in new:
        for p in PIECES:
            lines.append('    public static final RegistryObject<Item> %s_%s = armor("%s_%s", SoFETiers.Armor.%s, ArmorItem.Type.%s);'
                         % (s["name"].upper(), p.upper(), s["name"], p, s["name"].upper(), TYPES[p]))
    ordered = [s for c in CLASS_ORDER for s in sorted((x for x in SETS if x["cls"] == c), key=lambda x: x["level"])]
    names = ["%s_%s" % (s["name"].upper(), p.upper()) for s in ordered for p in PIECES]
    rows = [", ".join(names[k:k + 8]) for k in range(0, len(names), 8)]
    lines.append("    /** Every set piece, by class and level, for the creative tab. */")
    lines.append("    private static final List<RegistryObject<Item>> SET_PIECES = List.of(\n            " + ",\n            ".join(rows) + ");")
    lines.append("    // </generated-armor>")
    replace_block(os.path.join(JAVA, "registry", "ItemRegistry.java"), "// <generated-armor>", "// </generated-armor>", "\n".join(lines))

    # names and bonus lines
    for code, k in (("en_us", 0), ("es_es", 1)):
        path = os.path.join(LANG, code + ".json")
        d = json.load(open(path, encoding="utf-8"), object_pairs_hook=collections.OrderedDict)
        for s in SETS:
            style = STYLE_NAMES[s["style"]]
            for i, p in enumerate(PIECES):
                piece = style[i][k]
                if i == 0 and s.get("head"):
                    d["item.sofe.%s_%s" % (s["name"], p)] = s["head"][k]
                    continue
                d["item.sofe.%s_%s" % (s["name"], p)] = ("%s %s" % (s["names"][0], piece)) if k == 0 else ("%s %s" % (piece, s["names"][1]))
            d["armorset.sofe." + s["name"]] = s["text"][k]
        open(path, "w", encoding="utf-8").write(json.dumps(d, ensure_ascii=False, indent=2) + "\n")

    # gear bases and bonuses
    os.makedirs(os.path.join(DATA, "armor_sets"), exist_ok=True)
    for s in SETS:
        for p in PIECES:
            base = collections.OrderedDict([("item", "sofe:%s_%s" % (s["name"], p)), ("slot", "armor"), ("classes", [s["cls"]]),
                                            ("min_item_level", s["level"])])
            req = requirement(s)
            if req:
                base["requires"] = req
            with open(os.path.join(DATA, "gear_bases", "%s_%s.json" % (s["name"], p)), "w", encoding="utf-8") as f:
                f.write(json.dumps(base, indent=2) + "\n")
        with open(os.path.join(DATA, "armor_sets", s["name"] + ".json"), "w", encoding="utf-8") as f:
            f.write(json.dumps({"bonuses": s["bonus"]}, indent=2) + "\n")
    print("armor data: %d sets (%d new)" % (len(SETS), len(new)))


if __name__ == "__main__":
    main()
