"""Writes what the game needs for the arsenal, batch 3, and the class items of scripts/arsenal3_catalog.py:
the generated block of ItemRegistry.java (the items and their lists), the English and Spanish names and
tooltips, the gear bases, the recipes, and the item models of the bows, crossbows and firearms (the rest are
datagen's). Run from the repository root: python scripts/make_arsenal3_data.py
"""
import collections
import json
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from arsenal3_catalog import ITEMS, LANG  # noqa: E402

RES = os.path.join("src", "main", "resources")
DATA = os.path.join(RES, "data", "sofe")
MODELS = os.path.join(RES, "assets", "sofe", "models", "item")
REGISTRY = os.path.join("src", "main", "java", "com", "sofe", "registry", "ItemRegistry.java")


def const(i):
    return i["id"].upper()


def java_block():
    lines = ["// <generated-arsenal3> by scripts/make_arsenal3_data.py from scripts/arsenal3_catalog.py"]
    for i in ITEMS:
        lines.append('    public static final RegistryObject<Item> %s = ITEMS.register("%s",\n            () -> %s);' % (const(i), i["id"], i["java"]))

    def lst(name, doc, pick):
        names = [const(i) for i in ITEMS if pick(i)]
        rows = [", ".join(names[k:k + 6]) for k in range(0, len(names), 6)]
        lines.append("    /** %s */" % doc)
        lines.append("    private static final List<RegistryObject<Item>> %s = List.of(\n            %s);" % (name, ",\n            ".join(rows)))

    lst("A3_HANDHELD", "Held like a sword (handheld and large models).", lambda i: i["model"] in ("handheld", "large"))
    lst("A3_LARGE", "Held bigger.", lambda i: i["model"] == "large")
    lst("A3_GUNS", "Held barrel forward.", lambda i: i["model"] == "gun")
    lst("A3_BOWS", "Bows: their models pull back while drawn.", lambda i: i["model"] == "bow")
    lst("A3_CROSSBOWS", "Crossbows: their models pull and show the loaded bolt.", lambda i: i["model"] == "crossbow")
    lst("A3_FLAT", "Flat item models.", lambda i: i["model"] == "flat")
    lst("A3_TAB_EXTRA", "In the equipment tab besides the handheld ones.", lambda i: i["tab"] and i["model"] in ("gun", "bow", "crossbow", "flat"))
    lines.append("    // </generated-arsenal3>")
    src = open(REGISTRY, encoding="utf-8").read()
    a, b = src.index("// <generated-arsenal3>"), src.index("// </generated-arsenal3>") + len("// </generated-arsenal3>")
    open(REGISTRY, "w", encoding="utf-8").write(src[:a] + "\n".join(lines) + src[b:])


def lang():
    for code, k in (("en_us", 0), ("es_es", 1)):
        path = os.path.join(RES, "assets", "sofe", "lang", code + ".json")
        d = json.load(open(path, encoding="utf-8"), object_pairs_hook=collections.OrderedDict)
        for i in ITEMS:
            d["item.sofe." + i["id"]] = i["en"] if k == 0 else i["es"]
        for key, texts in LANG.items():
            d[key] = texts[k]
        open(path, "w", encoding="utf-8").write(json.dumps(d, ensure_ascii=False, indent=2) + "\n")


def data():
    for i in ITEMS:
        if i["gear"]:
            base = collections.OrderedDict([("item", "sofe:" + i["id"]), ("slot", "weapon"), ("classes", list(i["cls"] or [])),
                                            ("min_item_level", i["level"])])
            open(os.path.join(DATA, "gear_bases", i["id"] + ".json"), "w", encoding="utf-8").write(json.dumps(base, indent=2) + "\n")
        r = i["recipe"]
        if not r:
            continue
        out = {"item": "sofe:" + i["id"], "count": r.get("count", 1)}
        if r["type"] == "shaped":
            recipe = {"type": "minecraft:crafting_shaped", "pattern": r["pattern"], "key": {k: {"item": v} for k, v in r["key"].items()}, "result": out}
        else:
            recipe = {"type": "minecraft:crafting_shapeless", "ingredients": [{"item": v} for v in r["ingredients"]], "result": out}
        open(os.path.join(DATA, "recipes", i["id"] + ".json"), "w", encoding="utf-8").write(json.dumps(recipe, indent=2) + "\n")


def write(name, model):
    open(os.path.join(MODELS, name + ".json"), "w", encoding="utf-8").write(json.dumps(model, indent=2) + "\n")


def models():
    for i in ITEMS:
        n = i["id"]
        if i["model"] == "bow":
            write(n, {"parent": "minecraft:item/bow", "textures": {"layer0": "sofe:item/" + n},
                      "overrides": [{"predicate": {"pulling": 1}, "model": "sofe:item/%s_pulling_0" % n},
                                    {"predicate": {"pulling": 1, "pull": 0.65}, "model": "sofe:item/%s_pulling_1" % n},
                                    {"predicate": {"pulling": 1, "pull": 0.9}, "model": "sofe:item/%s_pulling_2" % n}]})
            for k in range(3):
                write("%s_pulling_%d" % (n, k), {"parent": "sofe:item/" + n, "textures": {"layer0": "sofe:item/%s_pulling_%d" % (n, k)}})
        elif i["model"] == "crossbow":
            write(n, {"parent": "minecraft:item/crossbow", "textures": {"layer0": "sofe:item/%s_standby" % n},
                      "overrides": [{"predicate": {"pulling": 1}, "model": "sofe:item/%s_pulling_0" % n},
                                    {"predicate": {"pulling": 1, "pull": 0.58}, "model": "sofe:item/%s_pulling_1" % n},
                                    {"predicate": {"pulling": 1, "pull": 1.0}, "model": "sofe:item/%s_pulling_2" % n},
                                    {"predicate": {"charged": 1}, "model": "sofe:item/%s_arrow" % n},
                                    {"predicate": {"charged": 1, "firework": 1}, "model": "sofe:item/%s_arrow" % n}]})
            for v in ("pulling_0", "pulling_1", "pulling_2", "arrow"):
                write("%s_%s" % (n, v), {"parent": "sofe:item/" + n, "textures": {"layer0": "sofe:item/%s_%s" % (n, v)}})


if __name__ == "__main__":
    java_block()
    lang()
    data()
    models()
    print("arsenal 3: %d items" % len(ITEMS))
