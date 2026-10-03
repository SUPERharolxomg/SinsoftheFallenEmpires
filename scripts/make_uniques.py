"""Writes the uniques of scripts/uniques_catalog.py: the items (the generated blocks of ItemRegistry.java and
SoFETiers.java), their Relic data (data/sofe/relics), the client's table of class uniques (UniqueClasses.java),
English and Spanish names and effect texts, the Curios tags, the Grand Talisman's gear base, and the art:
weapons recolored from the weapon they are built on, armor as 3D models with icons drawn from them
(armor_lib), rings and amulets (make_jewelry) and charms drawn here.

Run from the repository root: python scripts/make_uniques.py
"""
import collections
import colorsys
import json
import os
import sys

from PIL import Image

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

import armor_lib as al  # noqa: E402
import make_jewelry as mj  # noqa: E402
from armor_catalog import heavy, leather, robe, royal  # noqa: E402
from make_armor_data import REPAIR, stats  # noqa: E402
from uniques_catalog import GRAND_TALISMAN, UNIQUES  # noqa: E402

JAVA = os.path.join("src", "main", "java", "com", "sofe")
RES = os.path.join("src", "main", "resources")
DATA = os.path.join(RES, "data", "sofe")
ITEM = os.path.join(RES, "assets", "sofe", "textures", "item")
OD = collections.OrderedDict
P = "new Item.Properties()"

# ------------------------------------------------------------------------------------------- weapons
# base: (java kind, model, details)
BASES = {
    "oathblade": ("sword", "handheld", (-2.4, 0.0, ["SWEEP", "HOLY"])), "scale_blade": ("sword", "handheld", (-2.4, 0.0, ["SWEEP"])),
    "lance_of_the_scale": ("sword", "large", (-3.1, 2.0, ["REACH", "CHARGE"])), "justicar_maul": ("sword", "large", (-3.4, 0.0, ["STUN", "SLAM"])),
    "battle_greatsword": ("sword", "large", (-3.0, 1.0, ["REACH", "SWEEP"])), "bearded_axe": ("sword", "handheld", (-3.1, 0.0, ["SWEEP"])),
    "reaper_sickle": ("sword", "handheld", (-2.2, 0.5, ["REACH", "LIFE_STEAL"])), "khopesh": ("sword", "handheld", (-2.5, 0.0, ["BLEED", "SWEEP"])),
    "jackal_glaive": ("sword", "large", (-3.0, 1.5, ["REACH"])), "shadow_claws": ("sword", "handheld", (-1.8, 0.0, ["MULTI_HIT"])),
    "viper_claws": ("sword", "handheld", (-1.8, 0.0, ["MULTI_HIT", "POISON"])), "seax": ("sword", "handheld", (-1.8, 0.0, ["BLEED"])),
    "serrated_dagger": ("sword", "handheld", (-1.5, 0.0, ["BLEED"])), "hook_blade": ("sword", "handheld", (-2.2, 0.5, ["PULL"])),
    "explorer_machete": ("sword", "handheld", (-2.0, 0.0, ["PLANTS"])), "sultan_saber": ("sword", "handheld", (-2.3, 0.0, ["SWEEP", "BLEED"])),
    "brass_scimitar": ("sword", "handheld", (-2.4, 0.0, ["SWEEP"])), "imperial_halberd": ("sword", "large", (-3.0, 1.5, ["REACH", "SWEEP"])),
    "war_hammer": ("sword", "large", (-3.3, 0.0, ["SLAM"])), "crystal_trident": ("sword", "large", (-2.9, 1.0, ["REACH", "FROST"])),
    "chain_sword": ("sword", "handheld", (-2.4, 0.0, ["MULTI_HIT"])), "shadow_scythe": ("sword", "large", (-3.0, 1.0, ["REACH", "SWEEP"])),
    "double_flail": ("sword", "large", (-2.9, 0.0, ["MULTI_HIT"])), "kaleth_blade": ("sword", "handheld", (-2.4, 0.0, ["SWEEP"])),
    "bone_wand": ("caster", "handheld", ("BONE", "BOLT", 14)), "soul_wand": ("caster", "handheld", ("SOUL", "BEAM", 20)),
    "soul_staff": ("caster", "large", ("SOUL", "BEAM", 30)), "ember_orb": ("caster", "handheld", ("EMBER", "BOLT", 12)),
    "frost_orb": ("caster", "handheld", ("FROST", "BOLT", 12)), "storm_orb": ("caster", "handheld", ("STORM", "LIGHTNING", 36)),
    "ember_staff": ("caster", "large", ("EMBER", "BOLT", 20)), "frost_staff": ("caster", "large", ("FROST", "BOLT", 20)),
    "storm_staff": ("caster", "large", ("STORM", "LIGHTNING", 50)), "royal_scepter": ("decree", "handheld", None),
    "royal_flintlock": ("gun", "gun", (1, 0.3, 30)), "gearwork_musket": ("gun", "gun", (1, 0.2, 45)),
}


def tier(level):
    return "BRASS" if level <= 20 else "GLACIAL_IRON" if level <= 40 else "SOLAR_GOLD" if level <= 60 else "ORICHALCUM" if level <= 80 else "AETHERIUM"


def cls_arg(u):
    return '"%s"' % u["cls"] if u["cls"] else "null"


def weapon_java(u):
    kind, model, d = BASES[u["base"]]
    lvl = u["level"]
    if kind == "sword":
        speed, reach, traits = d
        dmg = 2 + lvl // 14 + (2 if model == "large" else 0)
        t = ", ".join("com.sofe.gear.WeaponTrait." + x for x in traits)
        return 'new com.sofe.gear.TraitWeapon(SoFETiers.%s, %d, %sf, %s, java.util.EnumSet.of(%s), %s, %s.rarity(Rarity.EPIC))' % (
            tier(lvl), dmg, speed, reach, t, cls_arg(u), P)
    if kind == "caster":
        spell, mode, cd = d
        return 'new com.sofe.gear.ranged.RangedItems.SpellCaster(Spell.%s, CastMode.%s, %d, %d, %s, %s.durability(%d).rarity(Rarity.EPIC))' % (
            spell, mode, 5 + lvl // 10, cd, cls_arg(u), P, 300 + lvl * 20)
    if kind == "decree":
        return 'new com.sofe.gear.ranged.RangedItems.Tome(TomeKind.DECREE, %d, 500, %s, %s.durability(200).rarity(Rarity.EPIC))' % (
            1 if lvl >= 50 else 0, cls_arg(u), P)
    pellets, spread, cd = d
    return 'new com.sofe.gear.ranged.RangedItems.Firearm(%d, %d, %sf, %d, %s, %s.durability(600).rarity(Rarity.EPIC))' % (
        8 + lvl // 6, pellets, spread, cd, cls_arg(u), P)


def recolor_weapon(u):
    """The base weapon's sprite, its metal and gems turned to the unique's hue, a glow of that hue round it."""
    src = Image.open(os.path.join(ITEM, u["base"] + ".png")).convert("RGBA")
    if src.size != (32, 32):
        src = src.resize((32, 32), Image.NEAREST)
    px = src.load()
    out = src.copy()
    o = out.load()
    hue = u["hue"]
    for y in range(32):
        for x in range(32):
            r, g, b, a = px[x, y]
            if not a or r + g + b < 90:
                continue
            h, l, s = colorsys.rgb_to_hls(r / 255, g / 255, b / 255)
            if s < 0.35 or (abs(h - 0.1) > 0.06 or l > 0.55):  # metal, gems and light parts; leave the brown wood and leather
                h2 = hue
                s2 = min(1.0, 0.45 + s * 0.6)
                r2, g2, b2 = colorsys.hls_to_rgb(h2, l, s2)
                o[x, y] = (int(r2 * 255), int(g2 * 255), int(b2 * 255), a)
    # the glow: outline pixels next to the weapon take a dark tint of the hue
    gr, gg, gb = colorsys.hls_to_rgb(hue, 0.35, 0.8)
    for y in range(32):
        for x in range(32):
            r, g, b, a = px[x, y]
            if a and r + g + b < 90:
                o[x, y] = (int(gr * 160), int(gg * 160), int(gb * 160), 255)
    out.save(os.path.join(ITEM, u["id"] + ".png"))


# ------------------------------------------------------------------------------------------- armor
HEADS = {
    "great_plume": lambda p: [al.great_helm("plate", "trim"), al.plume("cloth2" if "cloth2" in p else "trim")],
    "crown_helm": lambda p: [al.helmet_shell("plate"), al.crown("trim", "gem")],
    "great": lambda p: [al.great_helm("plate", "trim")],
    "plume": lambda p: [al.helmet_shell("plate"), al.cheek_guards("trim"), al.plume("cloth" if "cloth" in p else "trim")],
    "jackal": lambda p: [al.nemes("dark", "trim"), al.jackal_headdress("dark", "trim")],
    "lich": lambda p: [al.hood_deep("cloth", "dark"), al.lich_crown("bone", "gem")],
    "wraps": lambda p: [al.mummy_wraps("cloth"), al.gold_mask("trim")],
    "skull": lambda p: [al.skull_helm("bone", "bone")],
    "veil": lambda p: [al.hood("cloth"), al.veil("cloth2" if "cloth2" in p else "cloth"), al.circlet("trim", "gem")],
    "wizard": lambda p: [al.wizard_hat("cloth", "trim", "gem")],
    "witch": lambda p: [al.witch_hat("cloth", "trim", "gem")],
    "deep_hood": lambda p: [al.hood_deep("cloth" if "cloth" in p else "leather", "dark")],
    "hood": lambda p: [al.hood("cloth" if "cloth" in p else "leather")],
    "wolf": lambda p: [al.wolf_head("fur")],
    "turban": lambda p: [al.turban("cloth", "trim", "gem", "cloth2" if "cloth2" in p else "trim")],
    "crown": lambda p: [al.crown("trim", "gem")],
    "janissary": lambda p: [al.janissary_hat("cloth", "trim")],
}
WEIGHT = {"heavy": "heavy", "robe": "light", "leather": "medium", "royal": "light"}


def armor_parts(u):
    p = u["pal"]
    head = HEADS[u["head"]](p)
    if u["style"] == "heavy":
        return heavy(head, cape_mat="cloth" if "cloth" in p else None)
    if u["style"] == "robe":
        return robe(head, cloth="cloth" if "cloth" in p else "plate")
    if u["style"] == "leather":
        return leather(head, mat="leather" if "leather" in p else "cloth", fur="fur" if "fur" in p else None)
    return royal(head, silk="cloth")


# ------------------------------------------------------------------------------------------- jewelry and charms
METALS = {"gold": mj.GOLD, "silver": mj.SILVER, "bronze": mj.BRASS, "obsidian": mj.OBSIDIAN, "ice": mj.ICE, "bone": mj.BONE}
PENDANTS = {"drop": None, "scarab": mj.scarab, "fangs": mj.fangs, "eye": mj.eye, "scale": mj.laurel, "crescent": mj.laurel,
            "star": mj.crystal, "sun": None}


def jewel_art(u):
    metal = METALS[u["metal"]]
    if u["kind"] == "ring":
        mj.ring(u["id"], metal, u["stone"], wide=u["level"] >= 15)
    else:
        pendant = PENDANTS.get(u["pendant"]) or mj.drop(u["stone"], metal)
        mj.necklace(u["id"], metal, pendant)


SHAPES = {
    "shield": ["..xxxxxx..", ".xhhhhhhx.", ".xhggggdx.", ".xhgxxgdx.", ".xhggggdx.", ".xhhhhhdx.", "..xhhhdx..", "...xhdx...", "....xx...."],
    "banner": ["g.........", "gxxxxxxx..", "ghhhhhhx..", "ghhgghhx..", "ghhhhhhx..", "ghhhhhx...", "ghhhhx....", "g.........", "g........."],
    "flame": ["....x.....", "...xhx....", "...xhx.x..", "..xhhxhx..", ".xhhghhx..", ".xhgggdx..", ".xhggddx..", "..xdddx...", "...xxx...."],
    "scale": ["....g.....", "gggggggg..", "g...g...g.", "x...g...x.", "xx..g..xx.", "....g.....", "...ggg....", "..ggggg..."],
    "scarab": ["..g..g....", "...xx.....", "..xhhx....", ".xhhhhx...", ".xhghhx...", ".xhhhhx...", ".xddddx...", "x.xddx.x..", "...xx....."],
    "ankh": ["..xxx.....", ".x...x....", ".x...x....", "..xgx.....", "xxxgxxx...", "...g......", "...g......", "...g......", "..xxx....."],
    "jar": ["..ggg.....", ".xhhhx....", ".xxxxx....", "xhhhhhx...", "xhgghhx...", "xhgghdx...", "xhhhhdx...", ".xdddx....", "..xxx....."],
    "skull": ["..xxxx....", ".xhhhhx...", "xhhhhhhx..", "xhddhddx..", "xhggggdx..", ".xhhhhx...", ".xdxdxx...", "..x.x....."],
    "snowflake": ["....h.....", ".h..h..h..", "..h.h.h...", "...hhh....", "hhhhghhhh.", "...hhh....", "..h.h.h...", ".h..h..h..", "....h....."],
    "bolt": ["....xxx...", "...xhx....", "..xhx.....", ".xhhhhx...", "....xhx...", "...xhx....", "..xhx.....", ".xx.......", "x........."],
    "star": ["....g.....", "....g.....", "...ggg....", "gggghgggg.", ".gghhhgg..", "..ggggg...", ".gg...gg..", "gg.....gg."],
    "coin": ["..xxxx....", ".xhhhhx...", "xhhgghdx..", "xhgddgdx..", "xhgddgdx..", "xhhgghdx..", ".xdddddx..", "..xxxx...."],
    "key": ["..xxx.....", ".xh.hx....", ".xh.hx....", "..xxx.....", "...x......", "...x......", "...xx.....", "...x......", "...xx....."],
    "fang": ["xxxxxx....", "xhhhhx....", ".xhhx.....", ".xhhx.....", "..xhx.....", "..xhx.....", "...x......"],
    "smoke": ["...hh.....", "..h..h.h..", ".h.hh.h...", "hh.hhhh...", ".hhhxhhh..", "..hxxxh...", "...xxx....", "..xdddx...", "..xxxxx..."],
    "scroll": ["xxxxxxxx..", "xhhhhhhx..", ".xhddhx...", ".xhhhhx...", ".xhddhx...", ".xhhhhx...", "xhhhhhhx..", "xxxxxxxx.."],
    "cannonball": ["..xxxx....", ".xddddx...", "xddhdddx..", "xdhhdddx..", "xdddddxx..", "xddddddx..", ".xddddx...", "..xxxx...."],
    "crown": ["g..g..g...", "gx.g.xg...", "ggggggg...", "ghhrhhg...", "ggggggg...", "xxxxxxx..."],
    "shard": ["....h.....", "...xhx....", "...xhx....", "..xhhgx...", "..xhggx...", ".xhhggdx..", ".xhggddx..", "..xgddx...", "...xxx...."],
    "rose": ["..xxx.....", ".xhhhx....", "xhhghhx...", "xhgghdx...", ".xhhdx....", "..xxx.....", "...g..g...", "...gg.....", "...g......"],
    "feather": [".....xx...", "....xhhx..", "...xhhx...", "..xhhhx...", "..xhhx....", ".xhhx.....", ".xhx......", "xg........", "g........."],
    "compass": ["..xxxx....", ".xhhhhx...", "xhhghhhx..", "xhhgrhhx..", "xhhrghhx..", "xhhhhhhx..", ".xhhhhx...", "..xxxx...."],
    "lantern": ["...gg.....", "..g..g....", ".xxxxxx...", ".xhhhhx...", ".xhrrhx...", ".xhrrhx...", ".xhhhhx...", ".xxxxxx..."],
    "drop": ["....x.....", "...xhx....", "..xhhhx...", ".xhhhhdx..", ".xhhhhdx..", ".xhhhddx..", "..xdddx...", "...xxx...."],
    "hourglass": ["xxxxxxx...", ".xhhhx....", "..xhx.....", "...g......", "..xgx.....", ".xgggx....", "xxxxxxx..."],
    "idol": ["..xxx.....", ".xhhhx....", ".xgxgx....", ".xhhhx....", "xxhhhxx...", "x.xhx.x...", "..xhx.....", "..x.x....."],
    "eye": ["..xxxxx...", ".xhhhhhx..", "xhhxxxhhx.", "xhxrrrxhx.", "xhhxxxhhx.", ".xhhhhhx..", "..xxxxx..."],
    "torch": ["...rr.....", "..rggr....", "...rr.....", "...xx.....", "...xh.....", "...xh.....", "...xh.....", "...xx....."],
    "lock": ["..xxx.....", ".x...x....", ".x...x....", "xxxxxxx...", "xhhghhx...", "xhgggdx...", "xhhghdx...", "xxxxxxx..."],
}


def charm_art(id, size, shape, color, grand_disc=True):
    """A charm: the shape in its color; a small charm alone, a large one on a cord, a grand one on a bronze disc."""
    cv = mj.Canvas()
    rows = SHAPES[shape]
    hi, lo = mj.lighten(color, 0.45), mj.darken(color, 0.35)
    gold = (232, 186, 70)
    key = {"x": lo, "h": color, "d": lo, "g": gold, "r": (230, 50, 60)}
    if size == "grand":
        for y in range(16):
            for x in range(16):
                d = ((x - 7.5) ** 2 + (y - 7.5) ** 2) ** 0.5
                if d <= 7.2:
                    cv.put(x, y, (120, 82, 40) if d > 6.2 else (176, 128, 64) if (x + y) % 5 else (150, 104, 50))
        key["h"] = hi
    if size == "large":
        for x in range(3, 13):
            cv.put(x, 1 + abs(x - 8) // 3, (100, 60, 34))
    oy = 3 if size != "grand" else 3
    for dy, row in enumerate(rows):
        for dx, ch in enumerate(row):
            if ch in key:
                cv.put(3 + dx, oy + dy, key[ch])
    cv.put(4, oy + 1, mj.lighten(color, 0.8))
    cv.save(id)


# ------------------------------------------------------------------------------------------- texts
def pct(x):
    return "%d%%" % round(x * 100)


def effect_text(e):
    d, w = e["do"], e["on"]
    ch = e.get("chance")
    s, lvl = e.get("s"), e.get("lvl")
    fx = {"minecraft:speed": ("Speed", "Velocidad"), "minecraft:strength": ("Strength", "Fuerza"), "minecraft:resistance": ("Resistance", "Resistencia"),
          "minecraft:night_vision": ("Night Vision", "Visión Nocturna"), "minecraft:invisibility": ("Invisibility", "Invisibilidad"),
          "minecraft:regeneration": ("Regeneration", "Regeneración"), "minecraft:jump_boost": ("Jump Boost", "Salto"),
          "minecraft:slow_falling": ("Slow Falling", "Caída Lenta"), "minecraft:luck": ("Luck", "Suerte"), "minecraft:hero_of_the_village": ("the favor of the cities", "el favor de las ciudades"),
          "minecraft:water_breathing": ("Water Breathing", "Respiración Acuática"), "minecraft:dolphins_grace": ("Dolphin's Grace", "Gracia del Delfín")}
    when = {"low_health": (" while below half health", " con menos de media vida"), "crouching": (" while crouching", " agachado"),
            "night": (" at night", " de noche"), "day": (" by day", " de día"), "water": (" in water", " en el agua"), "always": ("", "")}
    E = {}
    if w == "hit":
        E = {"ignite": ("Hits set the target ablaze (%s s)" % s if s else "Hits set the target ablaze", "Los golpes incendian al objetivo (%s s)" % s if s else "Los golpes incendian al objetivo"),
             "slow": ("Hits slow the target (%s s)" % s, "Los golpes ralentizan al objetivo (%s s)" % s),
             "poison": ("Hits poison the target (%s s)" % s, "Los golpes envenenan al objetivo (%s s)" % s),
             "wither": ("Hits wither the target (%s s)" % s, "Los golpes marchitan al objetivo (%s s)" % s),
             "weaken": ("Hits weaken the target", "Los golpes debilitan al objetivo"),
             "bleed": ("Hits make the target bleed (%s s)" % s, "Los golpes hacen sangrar al objetivo (%s s)" % s),
             "blind": ("Hits blind the target", "Los golpes ciegan al objetivo"),
             "stun": ("Hits stun the target (%s s)" % s, "Los golpes aturden al objetivo (%s s)" % s),
             "freeze": ("Hits freeze the target solid (%s s)" % s, "Los golpes congelan al objetivo (%s s)" % s),
             "lightning": ("Hits call down lightning (x%s damage)" % e.get("mult"), "Los golpes invocan un rayo (x%s de daño)" % e.get("mult")),
             "execute": ("Enemies left under %s health are executed (not bosses)" % pct(e.get("below", 0)),
                         "Los enemigos que quedan con menos del %s de vida son ejecutados (no los jefes)" % pct(e.get("below", 0))),
             "lifesteal": ("%s of the damage dealt heals you" % pct(e.get("frac", 0)), "El %s del daño que haces te cura" % pct(e.get("frac", 0))),
             "vs_undead": ("x%s damage against the undead" % e.get("mult"), "x%s de daño contra los no muertos" % e.get("mult")),
             "vs_beast": ("x%s damage against beasts" % e.get("mult"), "x%s de daño contra las bestias" % e.get("mult")),
             "vs_monster": ("x%s damage against monsters" % e.get("mult"), "x%s de daño contra los monstruos" % e.get("mult")),
             "knockup": ("Hits throw the target into the air", "Los golpes lanzan al objetivo por los aires"),
             "chain": ("Hits leap to %s more enemies (%s of the damage)" % (e.get("targets", 0), pct(e.get("frac", 0))), "Los golpes saltan a %s enemigos más (%s del daño)" % (e.get("targets", 0), pct(e.get("frac", 0)))),
             "marks": ("Hits leave %s Mark%s" % (e.get("n", 1), "s" if e.get("n", 1) > 1 else ""), "Los golpes dejan %s Marca%s" % (e.get("n", 1), "s" if e.get("n", 1) > 1 else "")),
             "resource": ("Hits give %s of your resource" % e.get("amount", 0), "Los golpes dan %s de tu recurso" % e.get("amount", 0)),
             "soul": ("Hits bind a soul", "Los golpes atan un alma")}
    elif w == "kill":
        E = {"heal": ("Each kill heals you %s" % e.get("amount", 0), "Cada muerte te cura %s" % e.get("amount", 0)),
             "effect": ("Each kill gives you %s" % fx[e["effect"]][0], "Cada muerte te da %s" % fx[e["effect"]][1]) if "effect" in e else ("", ""),
             "dinars": ("Enemies you kill drop %s Dinar%s" % (e.get("n", 1), "s" if e.get("n", 1) > 1 else ""), "Los enemigos que matas sueltan %s dinar%s" % (e.get("n", 1), "es" if e.get("n", 1) > 1 else "")),
             "souls": ("Each kill binds %s more soul%s" % (e.get("n", 1), "s" if e.get("n", 1) > 1 else ""), "Cada muerte ata %s alma%s más" % (e.get("n", 1), "s" if e.get("n", 1) > 1 else "")),
             "resource": ("Each kill gives %s of your resource" % e.get("amount", 0), "Cada muerte da %s de tu recurso" % e.get("amount", 0)),
             "explode": ("Enemies you kill burst, hurting those near (%s)" % e.get("damage", 0), "Los enemigos que matas estallan e hieren a los cercanos (%s)" % e.get("damage", 0)),
             "raise": ("The enemies you kill rise as Embalmed Dead (%s s)" % e.get("s", 0), "Los enemigos que matas se alzan como Muertos Embalsamados (%s s)" % e.get("s", 0))}
    elif w == "hurt":
        E = {"thorns": ("%s of the damage you take returns to the attacker" % pct(e.get("frac", 0)), "El %s del daño que recibes vuelve al atacante" % pct(e.get("frac", 0))),
             "ignite": ("Whoever strikes you burns", "Quien te golpea arde"), "slow": ("Whoever strikes you is slowed", "Quien te golpea queda ralentizado"),
             "poison": ("Whoever strikes you is poisoned", "Quien te golpea queda envenenado"), "wither": ("Whoever strikes you withers", "Quien te golpea se marchita"),
             "knockback": ("You knock back whoever strikes you", "Empujas a quien te golpea"),
             "reduce": ("You take %s less damage" % pct(e.get("frac", 0)), "Recibes un %s menos de daño" % pct(e.get("frac", 0))),
             "cheat_death": ("Once every %s s, a killing blow leaves you at half a heart" % e.get("cooldown_s", 0), "Una vez cada %s s, un golpe mortal te deja con medio corazón" % e.get("cooldown_s", 0))}
    else:
        wh = when.get(e.get("when", "always"), ("", ""))
        amp = " II" if e.get("amp", 0) == 1 else ""
        E = {"effect": ("%s%s%s" % (fx[e["effect"]][0], amp, wh[0]), "%s%s%s" % (fx[e["effect"]][1], amp, wh[1])) if "effect" in e else ("", ""),
             "cleanse": ("Slowness and weakness do not take hold", "La lentitud y la debilidad no te afectan"),
             "resource": ("+%s of your resource each second" % e.get("per_s", 0), "+%s de tu recurso cada segundo" % e.get("per_s", 0)),
             "heal": ("Heals you %s every %s s" % (e.get("amount", 0), e.get("every_s", 0)), "Te cura %s cada %s s" % (e.get("amount", 0), e.get("every_s", 0))),
             "absorption": ("A shield of absorption every %s s" % e.get("every_s", 0), "Un escudo de absorción cada %s s" % e.get("every_s", 0)),
             "aura": ("An aura that hurts enemies near you", "Un aura que hiere a los enemigos cercanos"),
             "reveal": ("Enemies near you are revealed through walls", "Los enemigos cercanos se ven a través de las paredes"),
             "unfreeze": ("The cold never takes you", "El frío nunca te alcanza")}
    en, es = E[d]
    if w == "hurt" and d == "effect":
        en, es = "", ""
    if ch and d not in ("execute",):
        en, es = "%s chance: %s" % (pct(ch), en[0].lower() + en[1:]), "%s de probabilidad: %s" % (pct(ch), es[0].lower() + es[1:])
    return en, es


# ------------------------------------------------------------------------------------------- write it all
def replace_block(path, tag, text):
    src = open(path, encoding="utf-8").read()
    a, b = "// <%s>" % tag, "// </%s>" % tag
    if a not in src:
        raise SystemExit("missing block %s in %s" % (tag, path))
    i, j = src.index(a), src.index(b) + len(b)
    open(path, "w", encoding="utf-8").write(src[:i] + text + src[j:])


def java():
    lines = ["// <generated-uniques> by scripts/make_uniques.py from scripts/uniques_catalog.py"]
    groups = collections.defaultdict(list)
    for u in UNIQUES:
        c = u["id"].upper()
        if u["kind"] == "weapon":
            lines.append('    public static final RegistryObject<Item> %s = ITEMS.register("%s",\n            () -> %s);' % (c, u["id"], weapon_java(u)))
            groups["U_" + BASES[u["base"]][1].upper()].append(c)
        elif u["kind"] == "armor":
            typ = u["piece"].upper()
            lines.append('    public static final RegistryObject<Item> %s = armor("%s", SoFETiers.Armor.%s, ArmorItem.Type.%s);' % (c, u["id"], c, typ))
            groups["U_ARMOR"].append(c)
        else:
            slot = "TALISMAN" if u["kind"] == "charm" else "JEWELRY"
            lines.append('    public static final RegistryObject<Item> %s = trinket("%s", GearSlot.%s);' % (c, u["id"], slot))
            groups["U_FLAT"].append(c)
    gid = GRAND_TALISMAN[0].upper()
    lines.append('    public static final RegistryObject<Item> %s = trinket("%s", GearSlot.TALISMAN);' % (gid, GRAND_TALISMAN[0]))
    groups["U_FLAT"].append(gid)
    for name in ("U_HANDHELD", "U_LARGE", "U_GUN", "U_ARMOR", "U_FLAT"):
        items = groups.get(name, [])
        rows = [", ".join(items[k:k + 6]) for k in range(0, len(items), 6)] or [""]
        lines.append("    private static final List<RegistryObject<Item>> %s = List.of(%s);" % (name, ("\n            " + ",\n            ".join(rows)) if items else ""))
    lines.append("    // </generated-uniques>")
    replace_block(os.path.join(JAVA, "registry", "ItemRegistry.java"), "generated-uniques", "\n".join(lines))

    lines = ["// <generated-unique-armor> by scripts/make_uniques.py: one material per unique armor piece"]
    for u in UNIQUES:
        if u["kind"] != "armor":
            continue
        dur, prot, ench, tough, kb = stats({"level": u["level"], "weight": WEIGHT[u["style"]]})
        repair = REPAIR[u["cls"] or "knight"]
        lines.append('        %s("%s", %d, new int[]{%s}, %d, %sf, %sf, %s),' % (u["id"].upper(), u["id"], dur, ", ".join(map(str, prot)), ench, tough, kb, repair))
    lines.append("        // </generated-unique-armor>")
    replace_block(os.path.join(JAVA, "gear", "SoFETiers.java"), "generated-unique-armor", "\n".join(lines))

    table = ",\n            ".join('Map.entry("%s", "%s")' % (u["id"], u["cls"]) for u in UNIQUES if u["cls"])
    open(os.path.join(JAVA, "client", "UniqueClasses.java"), "w", encoding="utf-8").write("""package com.sofe.client;

import java.util.Map;

/**
 * The class of each class unique, for tooltips (the Relic data lives on the server). Written by
 * scripts/make_uniques.py from scripts/uniques_catalog.py.
 */
public final class UniqueClasses {
    private static final Map<String, String> CLASSES = Map.ofEntries(
            %s);

    private UniqueClasses() {
    }

    /** The class id that may use this unique, or null for anyone. */
    public static String of(String relic) {
        return CLASSES.get(relic);
    }
}
""" % table)


def data():
    slot = {"weapon": "weapon", "armor": "armor", "ring": "jewelry", "amulet": "jewelry", "charm": "talisman"}
    for u in UNIQUES:
        relic = OD([("item", "sofe:" + u["id"]), ("item_level", u["level"]), ("droppable", True), ("slot", slot[u["kind"]]),
                    ("min_level", max(1, u["level"] - 2))])
        if u["cls"]:
            relic["class"] = u["cls"]
        relic["affixes"] = u["affixes"]
        relic["effects"] = u["effects"]
        with open(os.path.join(DATA, "relics", u["id"] + ".json"), "w", encoding="utf-8") as f:
            f.write(json.dumps(relic, indent=2, ensure_ascii=False) + "\n")
    gid, _, lvl = GRAND_TALISMAN
    with open(os.path.join(DATA, "gear_bases", gid + ".json"), "w", encoding="utf-8") as f:
        f.write(json.dumps(OD([("item", "sofe:" + gid), ("slot", "talisman"), ("classes", []), ("min_item_level", lvl)]), indent=2) + "\n")
    tags = {"ring": "ring", "amulet": "necklace", "charm": "talisman"}
    for curio in ("ring", "necklace", "talisman"):
        p = os.path.join(RES, "data", "curios", "tags", "items", curio + ".json")
        d = json.load(open(p, encoding="utf-8"), object_pairs_hook=OD)
        add = ["sofe:" + u["id"] for u in UNIQUES if tags.get(u["kind"]) == curio]
        if curio == "talisman":
            add.append("sofe:" + gid)
        d["values"] = list(OD.fromkeys(d["values"] + add))
        open(p, "w", encoding="utf-8").write(json.dumps(d, indent=2) + "\n")


def lang():
    for code, k in (("en_us", 0), ("es_es", 1)):
        p = os.path.join(RES, "assets", "sofe", "lang", code + ".json")
        d = json.load(open(p, encoding="utf-8"), object_pairs_hook=OD)
        for u in UNIQUES:
            d["item.sofe." + u["id"]] = u["names"][k]
            texts = [t for t in (effect_text(e)[k] for e in u["effects"]) if t]
            d["item.sofe.%s.effect" % u["id"]] = ". ".join(texts) + "." if texts else ("A unique of the Bearers" if k == 0 else "Un único de los Portadores")
        d["item.sofe." + GRAND_TALISMAN[0]] = GRAND_TALISMAN[1][k]
        open(p, "w", encoding="utf-8").write(json.dumps(d, ensure_ascii=False, indent=2) + "\n")


def art():
    for u in UNIQUES:
        if u["kind"] == "weapon":
            recolor_weapon(u)
        elif u["kind"] == "armor":
            al.build(u["id"], u["pal"], armor_parts(u), pieces=[u["piece"]], icon_names={u["piece"]: u["id"]})
        elif u["kind"] in ("ring", "amulet"):
            jewel_art(u)
        else:
            charm_art(u["id"], u["size"], u["shape"], u["color"])
    charm_art(GRAND_TALISMAN[0], "grand", "star", (200, 200, 210))


if __name__ == "__main__":
    java()
    data()
    lang()
    art()
    print("uniques: %d, and the %s" % (len(UNIQUES), GRAND_TALISMAN[0]))
