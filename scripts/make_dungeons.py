"""The dungeons of every size and the rune puzzles before the bosses (docs/Mundo.md, W6), as data:

- small: a Crypt in every region (a tomb cut into a hill, three levels down, its lord in the deepest); medium: a Ruin with a rune puzzle before a Sealed Gate and a guardian behind it;
  large: the boss dungeons of the story, each sealed by a rune puzzle (but the Feast Halls, deep in the Caverns).
- Sulthari's Crypt and Ruin are part of Act I (sofe:act1_eclipse); every other Ruin is part of its act (Nordrath in
  Act II, Parsivan and Khemet in Act III, Aureum in Act IV, the Void's Breach in Act V), right after the Bearer reaches
  its land; every other Crypt is a dungeon quest that begins by itself when a Bearer comes near it (type "dungeon",
  "starts_near"). Steps put into an act are marked "_dungeon"; the act's later steps, their texts and every
  "quest_step" condition on them move along, once.

Writes the puzzles (data/sofe/puzzles), the dungeon quests (data/sofe/quests/dungeon), the Act I quest, the chests'
loot tables, the Ruins' gate conditions, the places and gates in structure_positions.json, and their English and
Spanish texts in the lang files. The Ruin and Crypt are built by DungeonBuilder.

Run from the repository root: python scripts/make_dungeons.py   (then python scripts/make_quest_book.py)
"""
import json
import os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DATA = os.path.join(ROOT, "src", "main", "resources", "data", "sofe")
LANG = os.path.join(ROOT, "src", "main", "resources", "assets", "sofe", "lang")
EN, ES = {}, {}


def text(key, en, es):
    EN[key], ES[key] = en, es


def dump(path, value, indent=2):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        json.dump(value, f, indent=indent, ensure_ascii=False)
        f.write("\n")


# ------------------------------------------------------------------------------------------------ the runes
RUNES = {"sun": ("Rune of the Sun", "Runa del Sol"), "moon": ("Rune of the Moon", "Runa de la Luna"),
         "star": ("Rune of the Star", "Runa de la Estrella"), "flame": ("Rune of the Flame", "Runa de la Llama"),
         "wave": ("Rune of the Wave", "Runa de la Ola"), "eye": ("Rune of the Eye", "Runa del Ojo")}


def lights_start(n, presses):
    """The start of a LIGHTS row: every rune burning, then the given runes pressed (so they are its solution)."""
    lit = [True] * n
    for p in presses:
        for i in (p - 1, p, p + 1):
            if 0 <= i < n:
                lit[i] = not lit[i]
    return lit


# ------------------------------------------------------------------------------------------------ the large dungeons
# name, gate, boss, anchor (a Waystone), place (en, es), kind, row, solution or presses, riddle (en lines), (es lines)
LARGE = [
    ("nordrath_forge", "sofe:nordrath/forge", "sofe:kaleth", "sofe:nordrath/forge", ("The Forge of Nordrath", "La Forja de Nordrath"),
     "order", ["wave", "sun", "flame", "moon"], ["flame", "wave", "moon", "sun"],
     ["The blade was born in the flame and quenched in the wave;", "it cooled beneath the moon, and was raised to greet the sun."],
     ["La hoja nació en la llama y se templó en la ola;", "se enfrió bajo la luna y se alzó para saludar al sol."]),
    ("nordrath_arena", "sofe:nordrath/arena", "sofe:serath", "sofe:nordrath/arena", ("The Arena of Nordrath", "La Arena de Nordrath"),
     "lights", ["eye", "flame", "eye", "flame"], [0, 2],
     ["Serath bleeds them all for the crowd, and the crowd must see.", "Let every eye burn and every flame watch."],
     ["Serath los desangra a todos para la multitud, y la multitud debe verlo.", "Que arda cada ojo y vigile cada llama."]),
    ("nordrath_citadel", "sofe:nordrath/burning_citadel", "sofe:vorath", "sofe:nordrath/burning_citadel",
     ("The Burning Citadel", "La Ciudadela Ardiente"),
     "order", ["moon", "flame", "star", "eye", "sun"], ["eye", "star", "moon", "sun", "flame"],
     ["Wrath opens its eye first, then counts the stars of its grudges;", "it waits for the moon, curses the sun,",
      "and ends, as it always ends, in flame."],
     ["La Ira abre primero el ojo y cuenta las estrellas de sus rencores;", "espera a la luna, maldice al sol",
      "y termina, como termina siempre, en llama."]),
    ("nordrath_caverns", "sofe:nordrath/caverns", "sofe:fenrath", "sofe:nordrath/caverns", ("The Caverns of Nordrath", "Las Cavernas de Nordrath"),
     "lights", ["wave", "moon", "wave", "moon", "wave", "moon"], [0, 3, 5],
     ["Below the ice, Fenrath hoards the dark.", "Wake every rune of the deep: the cold cannot keep what burns all at once."],
     ["Bajo el hielo, Fenrath acapara la oscuridad.", "Despierta cada runa de lo hondo: el frío no puede guardar lo que arde todo a la vez."]),
    ("parsivan_baths", "sofe:parsivan/baths", "sofe:mirael", "sofe:parsivan/baths", ("The Baths of Parsivan", "Los Baños de Parsivan"),
     "order", ["wave", "moon", "star", "sun"], ["moon", "wave", "sun", "star"],
     ["Mirael bathes by moonlight, and the wave shivers after her.", "At dawn the sun finds her gone; only a star stays on the tiles."],
     ["Mirael se baña a la luz de la luna, y la ola tiembla tras ella.", "Al alba el sol ya no la encuentra; solo una estrella queda en los azulejos."]),
    ("parsivan_silk_road", "sofe:parsivan/silk_road", "sofe:thessyn", "sofe:parsivan/silk_road", ("The Silk Road", "La Ruta de la Seda"),
     "order", ["sun", "eye", "star", "flame", "wave"], ["sun", "eye", "flame", "wave", "star"],
     ["The caravan leaves with the sun and crosses the dunes under the eye of the bandits;",
      "it camps by the flame, fords the wave, and arrives beneath the star."],
     ["La caravana parte con el sol y cruza las dunas bajo el ojo de los bandidos;",
      "acampa junto a la llama, vadea la ola y llega bajo la estrella."]),
    ("parsivan_gardens", "sofe:parsivan/enchanted_gardens", "sofe:luxara", "sofe:parsivan/enchanted_gardens",
     ("The Enchanted Gardens", "Los Jardines Encantados"),
     "lights", ["star", "sun", "star", "sun", "star", "sun"], [1, 4],
     ["In Luxara's garden nothing is allowed to fade.", "Make every rune bloom at once, and the illusion will let you in."],
     ["En el jardín de Luxara nada tiene permiso para marchitarse.", "Haz florecer cada runa a la vez y la ilusión te dejará entrar."]),
    ("khemet_catacombs", "sofe:khemet/catacombs", "sofe:dormiel", "sofe:khemet/catacombs", ("The Catacombs of Khemet", "Las Catacumbas de Khemet"),
     "order", ["eye", "moon", "sun", "wave"], ["eye", "wave", "moon", "sun"],
     ["The dead close their eye, cross the wave of the river,", "sleep beneath the moon, and wake, if ever, to the sun."],
     ["Los muertos cierran el ojo, cruzan la ola del río,", "duermen bajo la luna y despiertan, si acaso, con el sol."]),
    ("khemet_marsh", "sofe:khemet/stagnant_marsh", "sofe:morthis", "sofe:khemet/stagnant_marsh", ("The Stagnant Marsh", "El Pantano Estancado"),
     "lights", ["wave", "eye", "wave", "eye"], [1, 3],
     ["The marsh swallows light, and Morthis drowns all you kindle.", "Kindle it all at once, and he will have to look."],
     ["El pantano se traga la luz, y Morthis ahoga todo lo que enciendes.", "Enciéndelo todo a la vez y tendrá que mirar."]),
    ("aureum_treasury", "sofe:aureum/treasury", "sofe:goldarc", "sofe:aureum/treasury", ("The Treasury of Aureum", "El Tesoro de Aureum"),
     "order", ["sun", "star", "moon", "flame"], ["sun", "flame", "moon", "star"],
     ["Gold counts itself four times: by the sun for the people, by the flame for the scribes,", "by the moon for the thieves, and last by a star, for the gods."],
     ["El oro se cuenta cuatro veces: al sol para el pueblo, a la llama para los escribas,", "a la luna para los ladrones y, al final, bajo una estrella, para los dioses."]),
    ("aureum_market", "sofe:aureum/market", "sofe:nixara", "sofe:aureum/market", ("The Market of Aureum", "El Mercado de Aureum"),
     "lights", ["eye", "star", "eye", "star", "eye", "star"], [0, 2, 5],
     ["In Nixara's market every eye is bought.", "Open them all: no one can cheat a market that sees everything."],
     ["En el mercado de Nixara cada ojo está comprado.", "Ábrelos todos: nadie engaña a un mercado que lo ve todo."]),
    ("aureum_vaults", "sofe:aureum/golden_vaults", "sofe:avarok", "sofe:aureum/golden_vaults", ("The Golden Vaults", "Las Bóvedas Doradas"),
     "order", ["wave", "sun", "moon", "eye", "star"], ["star", "wave", "sun", "eye", "moon"],
     ["Avarok's vault keeps five locks: the star that guides the ships, the wave that brings them,",
      "the sun that weighs their gold, the eye that counts it, and the moon that hides it."],
     ["La bóveda de Avarok guarda cinco cerrojos: la estrella que guía a los barcos, la ola que los trae,",
      "el sol que pesa su oro, el ojo que lo cuenta y la luna que lo esconde."]),
    ("aureum_colosseum", "sofe:aureum/colosseum", "sofe:shadeyn", "sofe:aureum/colosseum", ("The Colosseum", "El Coliseo"),
     "lights", ["flame", "sun", "flame", "sun"], [0, 3],
     ["The crowd roars only when every torch is lit.", "Light the arena for Shadeyn, the last shade of its glory."],
     ["La multitud solo ruge cuando arde cada antorcha.", "Ilumina la arena para Shadeyn, la última sombra de su gloria."]),
    ("aureum_shadow_throne", "sofe:aureum/shadow_throne", "sofe:envyris", "sofe:aureum/shadow_throne", ("The Shadow Throne", "El Trono de las Sombras"),
     "order", ["sun", "moon", "eye", "wave", "flame", "star"], ["eye", "flame", "star", "wave", "sun", "moon"],
     ["Envy wants what each one has: the eye of the watcher, the flame of the lover, the star of the lucky,",
      "the wave of the free, the sun of the king; and last the moon, which wants nothing."],
     ["La Envidia quiere lo que cada uno tiene: el ojo del que vigila, la llama del amante, la estrella del afortunado,",
      "la ola del libre, el sol del rey; y al final la luna, que no quiere nada."]),
    ("sulthari_temple", "sofe:sulthari/temple", "sofe:solrath", "sofe:ascension/temple", ("The Temple of Sulthari", "El Templo de Sulthari"),
     "lights", ["sun", "eye", "sun", "eye", "sun", "eye"], [1, 3, 4],
     ["The false prophet asks for light and gives none.", "Give the temple all its light at once, and see what it hides."],
     ["El falso profeta pide luz y no da ninguna.", "Dale al templo toda su luz a la vez y verás lo que esconde."]),
    ("sulthari_spire", "sofe:sulthari/celestial_spire", "sofe:prython", "sofe:ascension/celestial_spire", ("The Celestial Spire", "La Aguja Celestial"),
     "order", ["star", "sun", "moon", "eye", "flame"], ["flame", "moon", "sun", "star", "eye"],
     ["Pride climbs: from the flame of its birth to the moon of its doubts, to the sun it envies,", "to the star it claims; and at the top it finds only its own eye."],
     ["El Orgullo sube: de la llama de su nacimiento a la luna de sus dudas, al sol que envidia,", "a la estrella que reclama; y en la cima solo encuentra su propio ojo."]),
    ("sulthari_inverted_throne", "sofe:sulthari/inverted_throne", "sofe:nahrazel", "sofe:ascension/inverted_throne",
     ("The Inverted Throne", "El Trono Invertido"),
     "lights", ["sun", "moon", "star", "flame", "wave", "eye"], [0, 2, 4],
     ["The First Fallen sealed the seven in the dark.", "Wake the six runes of the old Law; the seventh is the Quill you carry."],
     ["El Primer Caído selló a los siete en la oscuridad.", "Despierta las seis runas de la vieja Ley; la séptima es la Pluma que llevas."]),
]

# ------------------------------------------------------------------------------------------------ the crypts and ruins
# region, act, crypt (x, z), ruin (x, z), the crypt's dead (kill), the ruin's guardian, names (en, es)
# (each Crypt's lord, who sleeps in its deepest chamber, is in LORDS)
REGIONS = [
    ("sulthari", 1, (480, -420), (-560, -520), "sofe:sand_ghoul", "sofe:clockwork_scarab",
     ("The Wardens' Crypt", "La Cripta de los Guardianes"), ("The Wardens' Ruin", "La Ruina de los Guardianes"),
     ("sand ghouls", "gules de arena"), ("Brass Scarab", "el Escarabajo de Latón")),
    ("nordrath", 2, (760, -2050), (-1000, -3700), "sofe:draugr", "sofe:rime_wolf",
     ("The Barrow of the Drowned Jarl", "El Túmulo del Jarl Ahogado"), ("The Burnt Longhall", "El Salón Quemado"),
     ("draugr", "draugr"), ("Rime Wolf", "el Lobo de Escarcha")),
    ("parsivan", 3, (2600, -1300), (6200, -2600), "sofe:mirage_dancer", "minecraft:evoker",
     ("The Tomb of the Silent Poet", "La Tumba del Poeta Silencioso"), ("The Abandoned Pavilion", "El Pabellón Abandonado"),
     ("mirage dancers", "danzarinas del espejismo"), ("Court Sorcerer", "el Hechicero de la Corte")),
    ("khemet", 3, (3000, 2500), (5600, 7200), "sofe:bog_mummy", "minecraft:wither_skeleton",
     ("The Embalmers' Pit", "El Pozo de los Embalsamadores"), ("The House of the Dead", "La Casa de los Muertos"),
     ("bog mummies", "momias del pantano"), ("Tomb Guardian", "el Guardián de la Tumba")),
    ("aureum", 4, (-2600, -1500), (-6600, -3000), "sofe:gilded_legionnaire", "sofe:gladiator_shade",
     ("The Legion's Ossuary", "El Osario de la Legión"), ("The Fallen Senate", "El Senado Caído"),
     ("gilded legionnaires", "legionarios dorados"), ("Shade of a Gladiator", "la Sombra de Gladiador")),
    ("void", 5, (1050, 250), (-1050, 300), "sofe:void_*", "sofe:void_stalker",
     ("The Hollow Tomb", "La Tumba Hueca"), ("The Breach of the Seal", "La Brecha del Sello"),
     ("creatures of the Void", "criaturas del Vacío"), ("Void Stalker", "el Acechador del Vacío")),
]

# the lord of each Crypt's depths: its kind (an elite of it, twice as strong) and its name (en, es)
LORDS = {"sulthari": ("sofe:sand_ghoul", ("The First Warden", "El Primer Guardián")),
         "nordrath": ("sofe:draugr", ("The Drowned Jarl", "El Jarl Ahogado")),
         "parsivan": ("sofe:mirage_dancer", ("The Silent Poet", "El Poeta Silencioso")),
         "khemet": ("sofe:bog_mummy", ("The Master Embalmer", "El Maestro Embalsamador")),
         "aureum": ("sofe:gilded_legionnaire", ("The Last Legate", "El Último Legado")),
         "void": ("sofe:void_stalker", ("The Hollow One", "El Hueco"))}
# the Crypt (DungeonBuilder.crypt): 49 across; its burial chamber lies 26 blocks under, this far from its center
CRYPT_SIZE, CHAMBER = 49, (18, 0)
CRYPT_DEAD = 10

# where each Ruin goes in the story: the act, and the step it follows (None: the act begins with it)
IN_ACT = {"nordrath": ("act2_north", ("reach_region", "nordrath")), "parsivan": ("act3_east", ("reach_region", "parsivan")),
          "khemet": ("act3_east", ("reach_region", "khemet")), "aureum": ("act4_west", ("reach_region", "aureum")),
          "void": ("act5_ascension", None)}

RUIN_PUZZLES = {  # region: kind, row, solution or presses, riddle en, riddle es
    "sulthari": ("order", ["moon", "sun", "eye", "star"], ["sun", "eye", "star", "moon"],
                 ["The Wardens kept the seal by day and by night:", "they woke with the sun, kept their eye on the stars, and slept when the moon rose."],
                 ["Los Guardianes velaron el sello de día y de noche:", "despertaban con el sol, ponían el ojo en las estrellas y dormían cuando salía la luna."]),
    "nordrath": ("lights", ["flame", "flame", "flame", "flame"], [1, 2],
                 ["The jarl's hall goes cold when a single hearth dies.", "Light all four hearths, and the doors of the hall will remember him."],
                 ["El salón del jarl se enfría cuando muere un solo fuego.", "Enciende los cuatro hogares y las puertas del salón lo recordarán."]),
    "parsivan": ("order", ["wave", "star", "moon", "sun"], ["star", "wave", "moon", "sun"],
                 ["The poet's last verse: first the star that taught him longing, then the wave that took her,",
                  "the moon he wept to, and the sun that dried his tears."],
                 ["El último verso del poeta: primero la estrella que le enseñó a añorar, luego la ola que se la llevó,",
                  "la luna a la que lloró y el sol que secó sus lágrimas."]),
    "khemet": ("lights", ["eye", "sun", "eye", "sun", "eye", "sun"], [0, 5],
               ["The eye of the sun must see every room of the house of the dead.", "Wake every rune, and the house will open its last door."],
               ["El ojo del sol debe ver cada sala de la casa de los muertos.", "Despierta cada runa y la casa abrirá su última puerta."]),
    "void": ("order", ["moon", "flame", "eye", "star", "sun", "wave"], ["eye", "sun", "moon", "star", "wave", "flame"],
             ["The seal was written in six runes before the Law was broken: the eye that watched, the sun that judged,",
              "the moon that forgave, the star that remembered, the wave that carried the dead, and the flame that ends all."],
             ["El sello se escribió en seis runas antes de que se rompiera la Ley: el ojo que vigilaba, el sol que juzgaba,",
              "la luna que perdonaba, la estrella que recordaba, la ola que llevaba a los muertos y la llama que lo acaba todo."]),
    "aureum": ("order", ["sun", "moon", "eye", "wave", "star"], ["sun", "eye", "star", "moon", "wave"],
               ["The Senate votes by rank: the sun of the consul, the eye of the censor, the star of the augur,",
                "the moon of the tribune; and the wave of the people, who always vote last."],
               ["El Senado vota por rango: el sol del cónsul, el ojo del censor, la estrella del augur,",
                "la luna del tribuno; y la ola del pueblo, que siempre vota la última."]),
}


def puzzle(name, kind, row, answer, en, es, title, place, gate=None, boss=None, anchor=None, xz=None):
    data = {"kind": kind, "runes": row, "riddle_lines": len(en)}
    if kind == "order":
        assert sorted(answer) == sorted(row), name
        data["solution"] = answer
    else:
        data["start"] = lights_start(len(row), answer)
        assert not all(data["start"]), name
    if gate:
        data["gate"] = gate
    if boss:
        data["boss"] = boss
    if anchor:
        data["anchor"] = anchor
    if xz:
        data["x"], data["z"], data["facing"] = xz[0], xz[1], "south"
    dump(os.path.join(DATA, "puzzles", name + ".json"), data)
    text("puzzle.sofe.%s" % name, title[0], title[1])
    text("puzzle.sofe.%s.place" % name, place[0], place[1])
    assert len(en) == len(es), name
    for i, (a, b) in enumerate(zip(en, es), start=1):
        text("puzzle.sofe.%s.riddle.%d" % (name, i), a, b)


def haunt(xz, dead):
    """The dead a dungeon's halls step asks for rise near the Bearer inside it, so it never hangs on its spawners."""
    return {"x": xz[0], "z": xz[1], "depth": 6, "reach": 30, "entity": "sofe:void_zombie" if dead.endswith("*") else dead}


def reach(x, z, r=14):
    return {"objective": {"type": "reach", "x": x, "z": z, "radius": r}}


LANGS = {}


def main():
    for code in ("en_us", "es_es"):
        with open(os.path.join(LANG, code + ".json"), encoding="utf-8") as f:
            LANGS[code] = json.load(f)
    positions_path = os.path.join(DATA, "structure_positions.json")
    with open(positions_path, encoding="utf-8") as f:
        positions = json.load(f)
    for name, gate, boss, anchor, place, kind, row, answer, en, es in LARGE:
        puzzle(name, kind, row, answer, en, es, ("The Seal of " + lower(place[0]), de("El sello de " + lower(place[1]))), place,
               gate=gate, boss=boss, anchor=anchor)

    for region, act, crypt, ruin, dead, guardian, crypt_name, ruin_name, dead_name, guardian_name in REGIONS:
        crypt_id, ruin_id = "sofe:%s/crypt" % region, "sofe:%s/ruin" % region
        positions["structures"][crypt_id] = {"x": crypt[0], "z": crypt[1], "size_x": CRYPT_SIZE, "size_z": CRYPT_SIZE, "zone": "dungeon"}
        positions["structures"][ruin_id] = {"x": ruin[0], "z": ruin[1], "size_x": 37, "size_z": 29, "zone": "dungeon"}
        # a Waystone at the door of each, to travel back (the court of the Crypt opens south, the Ruin's door too)
        positions["waystones"][crypt_id] = {"x": crypt[0] + 5, "z": crypt[1] + 13}
        positions["waystones"][ruin_id] = {"x": ruin[0] + 5, "z": ruin[1] + 17}
        text("waystone.sofe.%s.crypt" % region, crypt_name[0], crypt_name[1])
        text("waystone.sofe.%s.ruin" % region, ruin_name[0], ruin_name[1])
        positions["gates"][ruin_id] = {"x": ruin[0], "z": ruin[1] - 3, "condition": "sofe:ruin_%s" % region, "hint": "message.sofe.gate.ruin"}
        dump(os.path.join(DATA, "conditions", "ruin_%s.json" % region), {"type": "act_reached", "act": act})
        text("place.sofe.%s.crypt" % region, crypt_name[0], crypt_name[1])
        text("place.sofe.%s.ruin" % region, ruin_name[0], ruin_name[1])
        kind, row, answer, en, es = RUIN_PUZZLES[region]
        puzzle("%s_ruin" % region, kind, row, answer, en, es, ("The Runes of " + lower(ruin_name[0]), de("Las runas de " + lower(ruin_name[1]))),
               ruin_name, xz=(ruin[0], ruin[1] + 2))
        guardian_step = {"objective": {"type": "kill", "entity": guardian, "count": 1}, "target": {"x": ruin[0], "z": ruin[1]},
                         "on_start": [{"type": "spawn", "entity": guardian, "count": 1, "radius": 6, "elite": True}]}
        lord, lord_name = LORDS[region]
        lair = (crypt[0] + CHAMBER[0], crypt[1] + CHAMBER[1])
        text("lord.sofe.%s_crypt" % region, lord_name[0], lord_name[1])
        crypt_steps = [dict(reach(*crypt), target={"x": crypt[0], "z": crypt[1]}),
                       {"objective": {"type": "kill", "entity": dead, "count": CRYPT_DEAD}, "target": {"x": crypt[0], "z": crypt[1]},
                        "haunt": haunt(crypt, dead)},
                       {"objective": {"type": "kill", "entity": "lord:" + lord, "count": 1}, "target": {"x": lair[0], "z": lair[1]},
                        "lair": {"x": lair[0], "z": lair[1], "depth": 18, "name": "lord.sofe.%s_crypt" % region}}]
        ruin_steps = [dict(reach(*ruin, r=20), target={"x": ruin[0], "z": ruin[1]}),
                      {"objective": {"type": "solve_puzzle", "puzzle": "sofe:%s_ruin" % region}, "target": {"x": ruin[0], "z": ruin[1] + 2}},
                      guardian_step]
        if region == "sulthari":
            act1(mark(crypt_steps[1], "sulthari/crypt"), mark(crypt_steps[2], "sulthari/crypt"), mark(ruin_steps[1], "sulthari/ruin"),
                 mark(guardian_step, "sulthari/ruin"), crypt_name, ruin_name, dead_name, guardian_name, lord_name)
            continue
        quest_name, after = IN_ACT[region]
        into_act(quest_name, after, region, [mark(ruin_steps[1], region + "/ruin"), mark(guardian_step, region + "/ruin")], [
            ("Find %s and wake the runes of its court." % ruin_name[0], de("Encuentra %s y despierta las runas de su patio." % lower(ruin_name[1]))),
            ("The runes woke its guardian: defeat the %s." % guardian_name[0], de("Las runas despertaron a su guardián: derrota a %s." % guardian_name[1]))])
        stale = os.path.join(DATA, "quests", "dungeon", "%s_ruin.json" % region)  # a Ruin is no longer a quest of its own
        if os.path.exists(stale):
            os.remove(stale)
        for code in LANGS:
            for k in [k for k in LANGS[code] if k.startswith("quest.sofe.dungeon.%s_ruin" % region)]:
                del LANGS[code][k]
        # the Crypt: a dungeon quest found on the way
        q = {"type": "dungeon", "act": act, "starts_near": {"x": crypt[0], "z": crypt[1], "radius": 64}, "steps": crypt_steps,
             "rewards": [{"type": "give_xp", "amount": {2: 900, 3: 1600, 4: 2400, 5: 3200}[act]}],
             "requires": {"type": "act_reached", "act": act}}
        dump(os.path.join(DATA, "quests", "dungeon", "%s_crypt.json" % region), q)
        key = "quest.sofe.dungeon.%s_crypt" % region
        text(key, crypt_name[0], crypt_name[1])
        text(key + ".step1", "Find %s." % crypt_name[0], "Encuentra %s." % lower(crypt_name[1]))
        text(key + ".step2", "Go down through its halls and drive out the %s." % dead_name[0],
             "Baja por sus salas y acaba con sus muertos: %s." % dead_name[1])
        text(key + ".step3", "In its deepest chamber, defeat %s." % lower(lord_name[0]), de("En su cámara más honda, derrota a %s." % lower(lord_name[1])))

    dump(positions_path, positions, indent=1)  # the file's own layout
    loot()
    messages()
    lang()


def lower(s):
    return s[0].lower() + s[1:]


def mark(step, dungeon):
    """A step that belongs to a dungeon (the quest book finds it by this; the mod ignores it)."""
    return dict(step, _dungeon=dungeon)


def into_act(quest_name, after, region, steps, texts):
    """Puts a Ruin's steps into an act after the step that reaches its land (or first), once; later runs refresh them."""
    path = os.path.join(DATA, "quests", quest_name + ".json")
    with open(path, encoding="utf-8") as f:
        q = json.load(f)
    quest_id, key = "sofe:" + quest_name, "quest.sofe." + quest_name
    there = [i for i, st in enumerate(q["steps"]) if st.get("_dungeon") == region + "/ruin"]
    if there:
        at = there[0]
        q["steps"][at:at + len(there)] = steps
    else:
        if after is None:
            at = 0
        else:
            at = next(i for i, st in enumerate(q["steps"]) if st["objective"]["type"] == after[0]
                      and st["objective"].get("region") == after[1]) + 1
        move_conditions(quest_id, at, len(steps))
        for code in LANGS:  # the texts of the later steps move along, from the last
            for n in range(len(q["steps"]), at, -1):
                if "%s.step%d" % (key, n) in LANGS[code]:
                    LANGS[code]["%s.step%d" % (key, n + len(steps))] = LANGS[code].pop("%s.step%d" % (key, n))
        q["steps"][at:at] = steps
    for j, (en, es) in enumerate(texts):
        text("%s.step%d" % (key, at + j + 1), en, es)
    dump(path, q)


def move_conditions(quest_id, at, count):
    """Every "quest_step" condition on the quest that points at or past the new steps moves along by their count."""
    def walk(node):
        changed = False
        if isinstance(node, dict):
            if node.get("type") == "quest_step" and node.get("quest") == quest_id and node.get("step", 0) - 1 >= at:
                node["step"] += count
                changed = True
            for v in node.values():
                changed |= walk(v)
        elif isinstance(node, list):
            for v in node:
                changed |= walk(v)
        return changed
    for folder in ("dialogue", "conditions", "quests"):
        for root, _, files in os.walk(os.path.join(DATA, folder)):
            for name in files:
                if not name.endswith(".json"):
                    continue
                path = os.path.join(root, name)
                with open(path, encoding="utf-8") as f:
                    data = json.load(f)
                if walk(data):
                    dump(path, data)
                    print("moved the steps of %s in %s" % (quest_id, os.path.relpath(path, DATA)))


def de(s):
    """Spanish contracts "de el" and "a el"."""
    return s.replace(" de el ", " del ").replace(" a el ", " al ")


def act1(crypt_kill, crypt_lord, ruin_puzzle, guardian, crypt_name, ruin_name, dead_name, guardian_name, lord_name):
    """Act I goes on after the Council: the Wardens' Crypt and their Ruin hold the way into the Observatory."""
    path = os.path.join(DATA, "quests", "act1_eclipse.json")
    with open(path, encoding="utf-8") as f:
        q = json.load(f)
    sentinel = next(s for s in q["steps"] if s["objective"].get("boss") == "sofe:brass_sentinel")
    council = q["steps"][:3]
    chapter = [s for s in q["steps"] if s.get("_chapter")]  # the chapter make_chapters.py put after the Council stays
    q["steps"] = council + chapter + [crypt_kill, crypt_lord, ruin_puzzle, guardian, sentinel]
    dump(path, q)
    k = "quest.sofe.act1_eclipse"
    n = len(chapter)  # the steps after it are numbered past it
    step = lambda i: "%s.step%d" % (k, i + n)  # noqa: E731
    text(step(4), "Go down into %s and drive out the %s of its halls." % (crypt_name[0], dead_name[0]),
         de("Baja a %s y acaba con los %s de sus salas." % (lower(crypt_name[1]), dead_name[1])))
    text(step(5), "In the deepest chamber of the Crypt, defeat %s." % lower(lord_name[0]),
         de("En la cámara más honda de la Cripta, derrota a %s." % lower(lord_name[1])))
    text(step(6), "In %s, read the riddle and wake the Wardens' runes." % ruin_name[0],
         "En %s, lee el acertijo y despierta las runas de los Guardianes." % lower(ruin_name[1]))
    text(step(7), "The runes woke the ruin's guardian: defeat the %s." % guardian_name[0],
         de("Las runas despertaron al guardián de la ruina: derrota a %s." % guardian_name[1]))
    text(step(8), "Stop the Brass Sentinel in the Great Observatory.", "Detén al Centinela de Latón en el Gran Observatorio.")
    text("dialogue.sofe.act1.council.3",
         "The Brass Sentinel of the Great Observatory has woken. It kept the seal for three hundred years; now it obeys no one, and the Observatory has closed with it. "
         "The Wardens who built it left the way in with their dead: in their Crypt, north-east of the city, and in their Ruin, to the north-west.",
         "El Centinela de Latón del Gran Observatorio ha despertado. Guardó el sello durante trescientos años; ahora no obedece a nadie, y el Observatorio se cerró con él. "
         "Los Guardianes que lo construyeron dejaron el camino con sus muertos: en su Cripta, al noreste de la ciudad, y en su Ruina, al noroeste.")


def loot():
    def gear(rarity, rolls):
        return {"rolls": rolls, "entries": [{"type": "minecraft:item", "name": "minecraft:stick",
                                             "functions": [{"function": "sofe:roll_gear", "min_rarity": rarity}]}]}

    def goods(rolls, items):
        return {"rolls": rolls, "entries": [{"type": "minecraft:item", "name": name, "weight": w,
                                             "functions": [{"function": "minecraft:set_count", "count": {"type": "minecraft:uniform", "min": lo, "max": hi}}]}
                                            for name, w, lo, hi in items]}
    dump(os.path.join(DATA, "loot_tables", "chests", "dungeon_small.json"), {"type": "minecraft:chest", "pools": [
        gear("common", 1),
        goods({"type": "minecraft:uniform", "min": 2, "max": 4}, [("minecraft:bread", 3, 2, 5), ("minecraft:gold_nugget", 3, 3, 9),
                                                                 ("minecraft:iron_ingot", 2, 1, 3), ("sofe:minor_pomegranate_elixir", 2, 1, 2)])]})
    dump(os.path.join(DATA, "loot_tables", "chests", "dungeon_medium.json"), {"type": "minecraft:chest", "pools": [
        gear("tempered", {"type": "minecraft:uniform", "min": 1, "max": 2}),
        goods({"type": "minecraft:uniform", "min": 3, "max": 5}, [("minecraft:gold_ingot", 3, 2, 5), ("minecraft:diamond", 1, 1, 2),
                                                                 ("minecraft:emerald", 2, 1, 4), ("sofe:aetherium_shard", 2, 1, 2),
                                                                 ("sofe:minor_pomegranate_elixir", 2, 1, 3)])]})


def messages():
    for rune, (en, es) in RUNES.items():
        text("rune.sofe." + rune, en, es)
    text("block.sofe.rune_stone", "Rune Stone", "Piedra Rúnica")
    text("block.sofe.riddle_tablet", "Riddle Tablet", "Tablilla del Acertijo")
    text("quest.sofe.type.dungeon", "Dungeons", "Mazmorras")
    text("message.sofe.puzzle.wrong", "The runes go dark: that was not the order.", "Las runas se apagan: ese no era el orden.")
    text("message.sofe.puzzle.solved", "The runes burn: the riddle is solved.", "Las runas arden: el acertijo está resuelto.")
    text("message.sofe.puzzle.solved_gate", "The runes burn, and the seal of the dungeon opens for you.",
         "Las runas arden y el sello de la mazmorra se abre para ti.")
    text("message.sofe.puzzle.how_order", "Press the Rune Stones (right-click) in the order the riddle tells. A wrong rune puts them all out.",
         "Pulsa las Piedras Rúnicas (clic derecho) en el orden que dice el acertijo. Una runa equivocada las apaga todas.")
    text("message.sofe.puzzle.how_lights", "Pressing a Rune Stone (right-click) turns it and its neighbours. Make every rune burn.",
         "Pulsar una Piedra Rúnica (clic derecho) cambia esa runa y sus vecinas. Haz que ardan todas.")
    text("message.sofe.gate.runes", "The seal answers only to its runes: read the Riddle Tablet beside the Waystone and wake them.",
         "El sello solo responde a sus runas: lee la Tablilla del Acertijo junto a la Piedra de Paso y despiértalas.")
    text("message.sofe.gate.ruin", "The seal of the ruin answers only to the runes of its court.",
         "El sello de la ruina solo responde a las runas de su patio.")


def lang():
    for code, entries in (("en_us", EN), ("es_es", ES)):
        path = os.path.join(LANG, code + ".json")
        data = LANGS[code]
        data.update(entries)
        with open(path, "w", encoding="utf-8", newline="\n") as f:
            json.dump(data, f, ensure_ascii=False, indent=2)
            f.write("\n")
    print("%d texts, %d large puzzles, %d regions with a crypt and a ruin" % (len(EN), len(LARGE), len(REGIONS)))


if __name__ == "__main__":
    main()
