"""What each piece of an armor set gives on its own (data/sofe/armor_sets/<set>.json, "pieces"), by the set's theme,
and the tooltip line of each piece. A full set gives every piece's bonus together with the set's own bonus
(ArmorSets). Used by make_armor_data.py; run on its own to refresh the pieces of the sets already written:
python scripts/set_pieces.py
"""
import collections
import json
import os

DATA = os.path.join("src", "main", "resources", "data", "sofe", "armor_sets")
LANG = os.path.join("src", "main", "resources", "assets", "sofe", "lang")
PIECES = ["helmet", "chestplate", "leggings", "boots"]

THEME = {
    "sight": ["anubis", "observatory", "vizier", "astral_sage", "huntsman"],
    "fire": ["desert_emir", "dragonknight", "infernal", "pyromancer", "solari"],
    "frost": ["frost_stalker", "frost_witch", "glacial_iron"],
    "death": ["bonelord", "gravewarden", "lich", "embalmer", "mummy_lord", "pharaoh", "plaguebearer", "scarab", "soulreaver"],
    "might": ["berserker", "warlord", "void", "lion_king", "legion", "nightblade"],
    "guard": ["cobalt", "sentinel", "royal_guard", "scale", "bronze", "brass", "golden_king", "sultan", "emerald"],
    "swift": ["gear", "mirage", "scout", "tempest", "wolf_raider", "shadow"],
    "arcane": ["archmage", "enchantress", "witch", "peacock", "chronomancer"],
    "sea": ["seafarer", "corsair"],
}


def resist(damage, value):
    return {"type": "resist", "damage": damage, "value": value}


def eff(effect, when="always"):
    return {"type": "effect", "effect": "minecraft:" + effect, "amplifier": 0, "when": when}


# per theme: helmet, chestplate, leggings, boots; each a list of bonuses and its (en, es) line
BONUS = {
    "sight": [([eff("night_vision")], ("Night vision", "Visión nocturna")),
              ([resist("fire", 0.05)], ("5% fire resistance", "5% de resistencia al fuego")),
              ([eff("night_vision"), {"type": "reveal", "radius": 10}], ("Improved night vision: enemies near you are revealed",
                                                                          "Visión nocturna mejorada: los enemigos cercanos se revelan")),
              ([resist("fire", 0.05)], ("5% fire resistance", "5% de resistencia al fuego"))],
    "fire": [([resist("fire", 0.05)], ("5% fire resistance", "5% de resistencia al fuego")),
             ([resist("fire", 0.08)], ("8% fire resistance", "8% de resistencia al fuego")),
             ([resist("explosion", 0.10)], ("10% explosion resistance", "10% de resistencia a explosiones")),
             ([resist("fire", 0.05)], ("5% fire resistance", "5% de resistencia al fuego"))],
    "frost": [([resist("magic", 0.04)], ("4% magic resistance", "4% de resistencia mágica")),
              ([resist("freeze", 0.25)], ("25% cold resistance", "25% de resistencia al frío")),
              ([{"type": "unfreeze"}], ("The cold never takes hold", "El frío nunca te congela")),
              ([resist("fall", 0.15)], ("15% less fall damage", "15% menos daño por caída"))],
    "death": [([eff("night_vision")], ("Night vision", "Visión nocturna")),
              ([resist("wither", 0.10), resist("magic", 0.03)], ("10% wither and 3% magic resistance", "10% de resistencia al marchitamiento y 3% mágica")),
              ([{"type": "on_kill", "heal": 1.0}], ("Each kill heals you half a heart", "Cada muerte te cura medio corazón")),
              ([{"type": "cleanse", "effects": ["minecraft:poison"]}], ("Poison never takes hold", "El veneno nunca te afecta"))],
    "might": [([resist("projectile", 0.05)], ("5% projectile resistance", "5% de resistencia a proyectiles")),
              ([resist("all", 0.03)], ("3% less damage taken", "3% menos daño recibido")),
              ([{"type": "on_kill", "heal": 1.0}], ("Each kill heals you half a heart", "Cada muerte te cura medio corazón")),
              ([resist("fall", 0.15)], ("15% less fall damage", "15% menos daño por caída"))],
    "guard": [([resist("projectile", 0.06)], ("6% projectile resistance", "6% de resistencia a proyectiles")),
              ([resist("all", 0.04)], ("4% less damage taken", "4% menos daño recibido")),
              ([resist("explosion", 0.10)], ("10% explosion resistance", "10% de resistencia a explosiones")),
              ([resist("fall", 0.20)], ("20% less fall damage", "20% menos daño por caída"))],
    "swift": [([resist("projectile", 0.04)], ("4% projectile resistance", "4% de resistencia a proyectiles")),
              ([resist("all", 0.02)], ("2% less damage taken", "2% menos daño recibido")),
              ([eff("speed")], ("Swiftness", "Velocidad")),
              ([resist("fall", 0.30)], ("30% less fall damage", "30% menos daño por caída"))],
    "arcane": [([resist("magic", 0.05)], ("5% magic resistance", "5% de resistencia mágica")),
               ([resist("magic", 0.05)], ("5% magic resistance", "5% de resistencia mágica")),
               ([eff("slow_falling", "falling")], ("You fall slowly", "Caes lentamente")),
               ([resist("fall", 0.20)], ("20% less fall damage", "20% menos daño por caída"))],
    "sea": [([eff("water_breathing", "water")], ("You breathe under water", "Respiras bajo el agua")),
            ([resist("drown", 0.20)], ("20% drowning resistance", "20% de resistencia al ahogo")),
            ([eff("dolphins_grace", "water")], ("You swim like a dolphin", "Nadas como un delfín")),
            ([resist("fall", 0.15)], ("15% less fall damage", "15% menos daño por caída"))],
}


def theme(set_name):
    for t, sets in THEME.items():
        if set_name in sets:
            return t
    return "guard"


def pieces(set_name):
    """{"helmet": [...], ...} for a set's json."""
    t = BONUS[theme(set_name)]
    return collections.OrderedDict((p, t[i][0]) for i, p in enumerate(PIECES))


def lang(set_name):
    """{"armorpiece.sofe.<set>.<piece>": (en, es)}."""
    t = BONUS[theme(set_name)]
    return {"armorpiece.sofe.%s.%s" % (set_name, p): t[i][1] for i, p in enumerate(PIECES)}


def write_all():
    entries = {}
    for f in sorted(os.listdir(DATA)):
        name = f[:-5]
        path = os.path.join(DATA, f)
        data = json.load(open(path, encoding="utf-8"), object_pairs_hook=collections.OrderedDict)
        data["pieces"] = pieces(name)
        open(path, "w", encoding="utf-8").write(json.dumps(data, indent=2) + "\n")
        entries.update(lang(name))
    for code, k in (("en_us", 0), ("es_es", 1)):
        path = os.path.join(LANG, code + ".json")
        d = json.load(open(path, encoding="utf-8"), object_pairs_hook=collections.OrderedDict)
        for key, v in entries.items():
            d[key] = v[k]
        d["armorpiece.sofe.full_set"] = ("Full set: every piece's bonus together, and:", "Set completo: las mejoras de todas las piezas juntas, y:")[k]
        open(path, "w", encoding="utf-8").write(json.dumps(d, ensure_ascii=False, indent=2) + "\n")
    print("piece bonuses for %d sets" % len(os.listdir(DATA)))


if __name__ == "__main__":
    write_all()
