"""The liberated camps, Kerem, Sister Nilufar and Zahir the Wanderer (docs/Anexos.md, A4): where each camp stands
(a protected camp zone with a Waystone), the copies of Ferid and Dilara who trade there once the region's Archsin has
fallen, Kerem's and Nilufar's shops in Sulthari, Zahir's rare stock, their few words, and their names in English
and Spanish.

Run from the repository root: python scripts/make_camps.py
"""
import json
import os

DATA = os.path.join("src", "main", "resources", "data", "sofe")
OFFERS = os.path.join(DATA, "merchant_offers")
DIALOGUE = os.path.join(DATA, "dialogue")
LANG = os.path.join("src", "main", "resources", "assets", "sofe", "lang")
POSITIONS = os.path.join(DATA, "structure_positions.json")

# region, centre, the Archsin whose fall frees it, its dialogue style, English and Spanish names
CAMPS = [
    ("nordrath", (300, -4200), "sofe:vorath", "nordrath", "Camp of the Ash Road", "Campamento del Camino de Ceniza"),
    ("parsivan", (4400, 100), "sofe:luxara", "parsivan", "Camp of the Silk Wells", "Campamento de los Pozos de Seda"),
    ("khemet", (5200, 5600), "sofe:morthis", "khemet", "Camp of the Reed Shore", "Campamento de la Orilla de Juncos"),
    ("aureum", (-5600, 1000), "sofe:envyris", "aureum", "Camp of the Broken Aqueduct", "Campamento del Acueducto Roto"),
]


def write(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        f.write(json.dumps(data, indent=2, ensure_ascii=False) + "\n")


def camp_copy(base, region, requires):
    """A camp's copy of a Sulthari merchant: the same offers, half the stock, its empire's Favor."""
    d = json.load(open(os.path.join(OFFERS, base + ".json"), encoding="utf-8"))
    offers = [dict(o, stock=max(1, o.get("stock", 8) // 2)) for o in d["offers"]]
    return {"role": d["role"], "empire": region, "requires": requires, "offers": offers, "buys": d.get("buys", [])}


def shops():
    for region, _, requires, _, _, _ in CAMPS:
        write(os.path.join(OFFERS, "ferid_camp_%s.json" % region), camp_copy("ferid", region, requires))
        write(os.path.join(OFFERS, "dilara_camp_%s.json" % region), camp_copy("dilara", region, requires))
    gems = ["wrath_ruby", "lust_amethyst", "greed_topaz", "sloth_moonstone", "gluttony_amber", "envy_emerald", "pride_sunstone"]
    acts = {"wrath_ruby": 2, "gluttony_amber": 2, "lust_amethyst": 3, "sloth_moonstone": 3, "greed_topaz": 4, "envy_emerald": 4, "pride_sunstone": 5}
    write(os.path.join(OFFERS, "kerem.json"), {
        "role": "jeweler", "empire": "sulthari",
        "offers": [{"sell": "sofe:cut_" + g, "price": 300, "min_act": acts[g], "min_favor": 2, "stock": 1} for g in gems],
        "buys": [{"item": "sofe:rough_" + g, "price": 25} for g in gems] + [{"item": "sofe:cut_" + g, "price": 70} for g in gems]})
    write(os.path.join(OFFERS, "nilufar.json"), {
        "role": "purifier", "empire": "sulthari",
        "offers": [{"sell": "sofe:aetherium_shard", "price": 180, "min_act": 3, "stock": 2},
                   {"sell": "sofe:aetherium_shard", "count": 3, "price": 480, "min_act": 4, "min_favor": 3, "stock": 1}],
        "buys": [{"item": "sofe:black_aetherium_shard", "price": 9}, {"item": "sofe:void_ash", "price": 2}]})
    write(os.path.join(OFFERS, "zahir.json"), {
        "role": "wanderer", "empire": "sulthari",
        "offers": [{"sell": "sofe:rough_" + g, "price": 90, "min_act": acts[g], "stock": 1} for g in gems] + [
            {"sell": "sofe:moonsilk", "count": 4, "price": 60, "stock": 1},
            {"sell": "sofe:sunreed_papyrus", "count": 6, "price": 40, "stock": 1},
            {"sell": "sofe:imperial_marble", "count": 16, "price": 30, "stock": 1},
            {"sell": "sofe:star_lapis", "count": 2, "price": 120, "min_act": 3, "stock": 1},
            {"sell": "sofe:solar_gold_ingot", "count": 2, "price": 160, "min_act": 3, "stock": 1},
            {"sell": "sofe:orichalcum_ingot", "price": 260, "min_act": 4, "stock": 1},
            {"sell": "sofe:aetherium_shard", "price": 200, "min_act": 4, "stock": 1},
            {"sell": "sofe:void_crystal", "price": 600, "min_act": 5, "stock": 1},
            {"sell": "sofe:veiled_jewelry", "price": 150, "min_act": 3, "stock": 1}]})


def positions():
    d = json.load(open(POSITIONS, encoding="utf-8"))
    npcs = [n for n in d["npcs"] if not ("_camp_" in n["npc"] or n["npc"] in ("kerem", "nilufar"))]
    for region, (x, z), _, _, _, _ in CAMPS:
        d["structures"]["sofe:%s/camp" % region] = {"x": x, "z": z, "size_x": 32, "size_z": 32, "zone": "camp"}
        d["waystones"]["sofe:%s/camp" % region] = {"x": x, "z": z + 6}
        npcs.append({"npc": "ferid_camp_" + region, "type": "merchant", "role": "alchemist", "x": x - 5, "z": z - 4, "yaw": 0})
        npcs.append({"npc": "dilara_camp_" + region, "type": "merchant", "role": "smith", "x": x + 5, "z": z - 4, "yaw": 0})
    npcs.append({"npc": "kerem", "type": "merchant", "role": "jeweler", "x": -44, "z": -36, "yaw": 90})      # in the Bank
    npcs.append({"npc": "nilufar", "type": "merchant", "role": "purifier", "x": -36, "z": -104, "yaw": 0})  # by the Void Gate
    d["npcs"] = npcs
    write(POSITIONS, d)


def words():
    lines = {
        "kerem": ("sulthari", [
            ("Every stone in Aetheris hides the face of a sin. Bring them to me rough; I will show you which.",
             "Cada piedra de Aetheris esconde el rostro de un pecado. Tráemelas en bruto; te mostraré cuál."),
            ("At my bench I cut them, open your steel and set them. Sneak at the bench to open a socket; hold the gem to set it.",
             "En mi mesa las tallo, abro tu acero y las engasto. Agáchate ante la mesa para abrir un engaste; sostén la gema para engastarla.")]),
        "nilufar": ("sulthari", [
            ("The Void leaves a black residue in all it touches. Burnt clean, it is aetherium again: the Seal's own light.",
             "El Vacío deja un residuo negro en todo lo que toca. Quemado limpio, vuelve a ser aetherium: la luz misma del Sello."),
            ("Bring me the black shards and some coal. The Purifier does the rest; the Codex forgives what it can.",
             "Tráeme los fragmentos negros y algo de carbón. El Purificador hace el resto; el Códice perdona lo que puede.")]),
        "zahir": ("aureum", [
            ("Zahir the Wanderer, at your service. My caravan never stays: three days here, then wherever the road is free.",
             "Zahir el Errante, a tu servicio. Mi caravana nunca se queda: tres días aquí y luego donde el camino esté libre."),
            ("What I carry, no city sells. One of each to each Bearer; the rest is for the next camp.",
             "Lo que traigo no lo vende ninguna ciudad. Uno de cada cosa a cada Portador; el resto es para el próximo campamento.")]),
    }
    lang = {}
    for npc, (style, texts) in lines.items():
        write(os.path.join(DIALOGUE, npc, "default.json"), {"style": style, "npc": npc, "lines": [
            {"speaker": npc, "text": "dialogue.sofe.%s.%d" % (npc, i + 1)} for i in range(len(texts))]})
        for i, t in enumerate(texts):
            lang["dialogue.sofe.%s.%d" % (npc, i + 1)] = t
    for region, _, _, style, en, es in CAMPS:
        for base, (ben, bes) in (("ferid", ("Camp Alchemist", "Alquimista del Campamento")), ("dilara", ("Camp Smith", "Herrera del Campamento"))):
            npc = "%s_camp_%s" % (base, region)
            write(os.path.join(DIALOGUE, npc, "default.json"), {"style": style, "npc": npc, "lines": [
                {"speaker": npc, "text": "dialogue.sofe.camp.%s" % base}]})
            lang["npc.sofe." + npc] = (ben, bes)
        lang["waystone.sofe.%s.camp" % region] = (en, es)
        lang["place.sofe.%s.camp" % region] = (en, es)
    lang.update({
        "dialogue.sofe.camp.ferid": ("Ferid sent me with what he could spare. Now that the road is free, the camp breathes again.",
                                     "Ferid me envió con lo que pudo. Ahora que el camino está libre, el campamento vuelve a respirar."),
        "dialogue.sofe.camp.dilara": ("Dilara's anvil travels with me. Bring me ingots and I will keep your steel sharp out here.",
                                      "El yunque de Dilara viaja conmigo. Tráeme lingotes y mantendré tu acero afilado aquí afuera."),
        "npc.sofe.kerem": ("Kerem the Cutter", "Kerem el Tallador"),
        "npc.sofe.nilufar": ("Sister Nilufar", "Hermana Nilufar"),
        "npc.sofe.zahir": ("Zahir the Wanderer", "Zahir el Errante"),
        "merchant.sofe.role.jeweler": ("Jeweler", "Joyero"),
        "merchant.sofe.role.purifier": ("Purifier", "Purificadora"),
        "merchant.sofe.role.wanderer": ("Traveling Caravan", "Caravana Errante"),
        "message.sofe.zahir.arrives": ("The caravan of Zahir the Wanderer has stopped at the %s, for three days.",
                                       "La caravana de Zahir el Errante se ha detenido en el %s, por tres días."),
    })
    for f, i in (("en_us", 0), ("es_es", 1)):
        path = os.path.join(LANG, f + ".json")
        d = json.load(open(path, encoding="utf-8"))
        for k, v in lang.items():
            d[k] = v[i] if isinstance(v, tuple) else v[i]
        open(path, "w", encoding="utf-8").write(json.dumps(d, ensure_ascii=False, indent=2) + "\n")


if __name__ == "__main__":
    shops()
    positions()
    words()
    print("%d camps, Kerem, Nilufar and Zahir written" % len(CAMPS))
