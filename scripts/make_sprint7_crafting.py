"""Sprint 7 crafting data (docs/Pociones.md, "Forges and crafting"; docs/Anexos.md, A3): the Jeweler's cuts, the
Purifier, the recipes of the three new stations, Imperial Marble, Sunreed Papyrus, Void Ink, where Black Aetherium
and Moonsilk come from, and the names (en/es) of the gems, materials and stations and of their messages.

Run from the repository root: python scripts/make_sprint7_crafting.py
"""
import json
import os

DATA = os.path.join("src", "main", "resources", "data", "sofe")
RECIPES = os.path.join(DATA, "recipes")
LOOT = os.path.join(DATA, "loot_tables", "entities")
LANG = os.path.join("src", "main", "resources", "assets", "sofe", "lang")

GEMS = [  # id, English, Spanish
    ("wrath_ruby", "Ruby of Wrath", "Rubí de la Ira"),
    ("lust_amethyst", "Amethyst of Lust", "Amatista de la Lujuria"),
    ("greed_topaz", "Topaz of Greed", "Topacio de la Avaricia"),
    ("sloth_moonstone", "Moonstone of Sloth", "Piedra Luna de la Pereza"),
    ("gluttony_amber", "Amber of Gluttony", "Ámbar de la Gula"),
    ("envy_emerald", "Emerald of Envy", "Esmeralda de la Envidia"),
    ("pride_sunstone", "Sunstone of Pride", "Piedra Sol de la Soberbia"),
]
FORMS = {"rough": ("Rough %s", "%s en Bruto"), "cut": ("Cut %s", "%s Tallado"), "oath": ("Oath %s", "%s del Juramento")}
CUT_FEE = 120


def write(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        f.write(json.dumps(data, indent=2, ensure_ascii=False) + "\n")


def shaped(name, pattern, key, result, count=1):
    write(os.path.join(RECIPES, name + ".json"), {
        "type": "minecraft:crafting_shaped", "pattern": pattern,
        "key": {k: {"item": v} for k, v in key.items()}, "result": {"item": result, "count": count}})


def station(kind, name, ingredients, result, count=1, dinars=0):
    data = {"type": "sofe:" + kind, "ingredients": [{"item": i, "count": c} for i, c in ingredients], "result": result, "count": count}
    if dinars:
        data["dinars"] = dinars
    write(os.path.join(RECIPES, kind, name + ".json"), data)


def recipes():
    for gem, _, _ in GEMS:   # the Jeweler cuts a rough gem for a fee
        station("jeweler", "cut_" + gem, [("sofe:rough_" + gem, 1)], "sofe:cut_" + gem, dinars=CUT_FEE)
    # the Purifier: Black Aetherium, burnt clean, becomes aetherium (docs/Pociones.md)
    station("purifier", "aetherium_shard", [("sofe:black_aetherium_shard", 4), ("minecraft:coal", 1)], "sofe:aetherium_shard")
    station("purifier", "aetherium_shard_from_void_ash", [("sofe:black_aetherium_shard", 2), ("sofe:void_ash", 4)], "sofe:aetherium_shard")
    # Void Ink, at the Alembic: chorus fruit, Void Crystal and an ink sac (docs/Mundo.md)
    station("alembic", "void_ink", [("minecraft:chorus_fruit", 2), ("sofe:void_crystal", 1), ("minecraft:ink_sac", 1)], "sofe:void_ink")
    shaped("jeweler", [" L ", "GTG", "BBB"], {"L": "sofe:star_lapis", "G": "sofe:solar_gold_ingot", "T": "minecraft:smithing_table",
                                              "B": "sofe:sulthari_brass_ingot"}, "sofe:jeweler")
    shaped("purifier", ["GLG", "GFG", "SSS"], {"G": "sofe:solar_gold_ingot", "L": "sofe:star_lapis", "F": "minecraft:blast_furnace",
                                               "S": "minecraft:smooth_stone"}, "sofe:purifier")
    shaped("tempering_anvil", ["GGG", " A ", "III"], {"G": "sofe:solar_gold_ingot", "A": "minecraft:anvil", "I": "sofe:glacial_iron_ingot"},
           "sofe:tempering_anvil")
    shaped("aureum_polished_marble", ["MM", "MM"], {"M": "sofe:imperial_marble"}, "sofe:aureum_polished_marble", 4)
    for target, count in (("aureum_marble_bricks", 1), ("aureum_marble_pillar", 1), ("aureum_polished_marble", 1)):
        write(os.path.join(RECIPES, target + "_from_imperial_marble_stonecutting.json"),
              {"type": "minecraft:stonecutting", "ingredient": {"item": "sofe:imperial_marble"}, "result": "sofe:" + target, "count": count})
    shaped("sunreed_papyrus", ["RRR", " P "], {"R": "minecraft:sugar_cane", "P": "sofe:solar_gold_powder"}, "sofe:sunreed_papyrus", 3)


def add_loot(entity, item, lo, hi, chance=None):
    """One more pool on a creature's loot: an item when a player kills it."""
    path = os.path.join(LOOT, entity + ".json")
    data = json.load(open(path, encoding="utf-8"))
    data["pools"] = [p for p in data["pools"] if not any(e.get("name") == item for e in p["entries"])]
    conditions = [{"condition": "minecraft:killed_by_player"}]
    if chance:
        conditions.append({"condition": "minecraft:random_chance_with_looting", "chance": chance, "looting_multiplier": 0.02})
    data["pools"].append({"rolls": 1, "entries": [{"type": "minecraft:item", "name": item, "functions": [
        {"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": lo, "max": hi}}]}], "conditions": conditions})
    write(path, data)


def loot():
    # Black Aetherium comes from the Void's creatures (docs/Pociones.md: "Corrupted enemies drop Black Aetherium")
    add_loot("void_wretch", "sofe:black_aetherium_shard", 1, 2, 0.5)
    add_loot("void_stalker", "sofe:black_aetherium_shard", 1, 2, 0.5)
    add_loot("void_zombie", "sofe:black_aetherium_shard", 1, 1, 0.15)
    add_loot("void_skeleton", "sofe:black_aetherium_shard", 1, 1, 0.15)
    # Moonsilk from the Silk Road's dancers and its Weaver (docs/Anexos.md: "Silk Road weaver mobs")
    path = os.path.join(LOOT, "mirage_dancer.json")
    data = json.load(open(path, encoding="utf-8"))
    for p in data["pools"]:
        for e in p["entries"]:
            if e.get("name") == "minecraft:string":
                e["name"] = "sofe:moonsilk"
    write(path, data)
    add_loot("thessyn", "sofe:moonsilk", 4, 8)


LANG_EN, LANG_ES = {}, {}


def names():
    for gem, en, es in GEMS:
        for form, (fen, fes) in FORMS.items():
            LANG_EN["item.sofe.%s_%s" % (form, gem)] = fen % en
            # Spanish keeps the gem's gender: "Rubí de la Ira en Bruto", "Rubí de la Ira Tallado", "Rubí de la Ira del Juramento"
            LANG_ES["item.sofe.%s_%s" % (form, gem)] = fes % es if form != "cut" else es + (" Tallada" if es.split()[0] in ("Amatista", "Piedra", "Esmeralda") else " Tallado")
    for key, en, es in [
        ("item.sofe.moonsilk", "Moonsilk", "Seda Lunar"),
        ("item.sofe.sunreed_papyrus", "Sunreed Papyrus", "Papiro de Junco Solar"),
        ("item.sofe.void_crystal", "Void Crystal", "Cristal del Vacío"),
        ("item.sofe.void_ink", "Void Ink", "Tinta del Vacío"),
        ("block.sofe.jeweler", "Jeweler's Bench", "Mesa del Joyero"),
        ("block.sofe.purifier", "Purifier", "Purificador"),
        ("block.sofe.tempering_anvil", "Tempering Anvil", "Yunque de Temple"),
        ("gui.sofe.station.jeweler", "Jeweler: cut gems", "Joyero: tallar gemas"),
        ("gui.sofe.station.purifier", "Purifier", "Purificador"),
        ("gui.sofe.station.dinars", "%s Dinars", "%s dinares"),
        ("gear.sofe.tooltip.empty_socket", "Empty socket", "Engaste vacío"),
        ("message.sofe.jeweler.full", "This piece takes no more than %s sockets.", "Esta pieza no admite más de %s engastes."),
        ("message.sofe.jeweler.no_dinars", "Opening a socket costs %s Dinars.", "Abrir un engaste cuesta %s dinares."),
        ("message.sofe.jeweler.opened", "Socket opened (%s of %s) for %s Dinars.", "Engaste abierto (%s de %s) por %s dinares."),
        ("message.sofe.jeweler.how", "Sockets %s/%s. Sneak to open one; hold a cut gem in the other hand to set it.",
         "Engastes %s/%s. Agáchate para abrir uno; sostén una gema tallada en la otra mano para engastarla."),
        ("message.sofe.jeweler.rough", "A rough gem must be cut first (use the bench with empty hands).",
         "Una gema en bruto hay que tallarla antes (usa la mesa con las manos vacías)."),
        ("message.sofe.jeweler.no_socket", "There is no open socket for it.", "No hay un engaste abierto para ella."),
        ("message.sofe.jeweler.set", "%s set.", "%s engastada."),
        ("message.sofe.anvil.tempered", "Tempered (%s of %s).", "Templado (%s de %s)."),
        ("message.sofe.anvil.rerolled", "The weakest affix is forged anew.", "El afijo más débil se forja de nuevo."),
        ("message.sofe.anvil.how", "Hold a piece of SoFE gear: use to temper an affix (1 aetherium), sneak to reroll the weakest (2).",
         "Sostén una pieza de equipo: úsalo para templar un afijo (1 aetherium), agáchate para rehacer el más débil (2)."),
        ("message.sofe.anvil.nothing", "Nothing on this piece can be worked (Relics and gems are left alone).",
         "No hay nada que trabajar en esta pieza (las Reliquias y las gemas no se tocan)."),
        ("message.sofe.anvil.worn_out", "This piece has been tempered %s times already.", "Esta pieza ya se templó %s veces."),
        ("message.sofe.anvil.no_aetherium", "It takes %s aetherium shards.", "Hacen falta %s fragmentos de aetherium."),
        ("block.sofe.imperial_marble", "Imperial Marble", "Mármol Imperial"),
    ]:
        LANG_EN[key] = en
        LANG_ES[key] = es


def write_lang():
    for f, table in (("en_us", LANG_EN), ("es_es", LANG_ES)):
        path = os.path.join(LANG, f + ".json")
        d = json.load(open(path, encoding="utf-8"))
        d.update(table)
        open(path, "w", encoding="utf-8").write(json.dumps(d, ensure_ascii=False, indent=2) + "\n")


if __name__ == "__main__":
    recipes()
    loot()
    names()
    write_lang()
    print("sprint 7 crafting written: %d names" % len(LANG_EN))
