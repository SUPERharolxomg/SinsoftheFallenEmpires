"""More story in every act (docs/Jugabilidad.md): each act's main quest gains a chapter of its own, and the map gains
tombs to find.

- A chapter: someone of the land asks for help (a conversation that moves the story on), a tomb to find, its halls to
  go down through (ten of its dead), its lord to defeat in the deepest chamber, and back to them with what was found.
  Act I's comes after the Council (the general Bahadir, then the sage Esra), Act II's after reaching Nordrath
  (Gunnhild the elder), Act III's after reaching Parsivan (the poet) and Khemet (the scribe), Act IV's after reaching
  Aureum (the senator), Act V's at its start (the Council's elder).
- Tombs to find: five more, one a region, each a dungeon quest that begins by itself when a Bearer comes near, once its
  act has come (as the Crypts do).

Every tomb is built as a Crypt is (DungeonBuilder: a tomb cut into a hill, three levels down); its lord rises in the
deepest chamber. Steps put into an act are marked "_chapter"; on a later run they are refreshed in place, the act's
later steps, their texts and every "quest_step" condition on them having moved along once.

Writes the quests, the conversations (data/sofe/dialogue/chapter), the places in structure_positions.json and the
English and Spanish texts. Run after make_dungeons.py and make_invasions.py, from the repository root:
python scripts/make_chapters.py   (then python scripts/make_quest_book.py)
"""
import json
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
import make_dungeons  # noqa: E402  (move_conditions, the Crypts' size and chamber)

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DATA = os.path.join(ROOT, "src", "main", "resources", "data", "sofe")
LANG = os.path.join(ROOT, "src", "main", "resources", "assets", "sofe", "lang")
EN, ES = {}, {}
LANGS = {}

REGION_BOUNDS = {"sulthari": [(-1500, 1500, -1500, 1500)], "void": [(-1500, 1500, -1500, 1500)],
                 "nordrath": [(-6000, 6000, -12000, -1500)], "parsivan": [(1500, 12000, -1500, 1500), (6000, 12000, -12000, -1500)],
                 "khemet": [(800, 12000, 1500, 12000)], "aureum": [(-12000, -1500, -1500, 3500), (-12000, -6000, -12000, -1500)]}

# the tombs: id, region, (x, z), name (en, es), its dead (kill, en, es), its lord (kind, en, es)
TOMBS = {
    "first_vizier": ("sulthari", (300, 620), ("The Tomb of the First Vizier", "La Tumba del Primer Visir"),
                     ("sofe:sand_ghoul", "sand ghouls", "gules de arena"), ("sofe:sand_ghoul", "The First Vizier", "El Primer Visir")),
    "frost_king": ("nordrath", (-2400, -3200), ("The Barrow of the Frost King", "El Túmulo del Rey de Escarcha"),
                   ("sofe:draugr", "draugr", "draugr"), ("sofe:draugr", "The Frost King", "El Rey de Escarcha")),
    "veiled_queen": ("parsivan", (2800, 1000), ("The Tomb of the Veiled Queen", "La Tumba de la Reina Velada"),
                     ("sofe:mirage_dancer", "mirage dancers", "danzarinas del espejismo"), ("sofe:mirage_dancer", "The Veiled Queen", "La Reina Velada")),
    "scarab_priest": ("khemet", (2200, 3300), ("The House of the Scarab Priest", "La Casa del Sacerdote Escarabajo"),
                      ("sofe:bog_mummy", "bog mummies", "momias del pantano"), ("sofe:bog_mummy", "The Scarab Priest", "El Sacerdote Escarabajo")),
    "first_consul": ("aureum", (-2600, 1800), ("The Tomb of the First Consul", "La Tumba del Primer Cónsul"),
                     ("sofe:gilded_legionnaire", "gilded legionnaires", "legionarios dorados"), ("sofe:gilded_legionnaire", "The First Consul", "El Primer Cónsul")),
    "hollow_choir": ("void", (-1100, -900), ("The Hall of the Hollow Choir", "La Sala del Coro Hueco"),
                     ("sofe:void_*", "creatures of the Void", "criaturas del Vacío"), ("sofe:void_stalker", "The Hollow Choir", "El Coro Hueco")),
    # to be found on the way
    "lost_caravan": ("sulthari", (-1100, 200), ("The Tomb of the Lost Caravan", "La Tumba de la Caravana Perdida"),
                     ("sofe:sand_ghoul", "sand ghouls", "gules de arena"), ("sofe:sand_ghoul", "The Caravan Master", "El Maestro de la Caravana")),
    "skald": ("nordrath", (2600, -2600), ("The Barrow of the Last Skald", "El Túmulo del Último Escaldo"),
              ("sofe:draugr", "draugr", "draugr"), ("sofe:draugr", "The Last Skald", "El Último Escaldo")),
    "astronomer": ("parsivan", (4200, -1100), ("The Tomb of the Star-Reader", "La Tumba del Lector de Estrellas"),
                   ("sofe:mirage_dancer", "mirage dancers", "danzarinas del espejismo"), ("sofe:mirage_dancer", "The Star-Reader", "El Lector de Estrellas")),
    "twin_kings": ("khemet", (4400, 2600), ("The Tomb of the Twin Kings", "La Tumba de los Reyes Gemelos"),
                   ("sofe:bog_mummy", "bog mummies", "momias del pantano"), ("sofe:bog_mummy", "The Twin Kings", "Los Reyes Gemelos")),
    "gladiators": ("aureum", (-4200, -900), ("The Gladiators' Ossuary", "El Osario de los Gladiadores"),
                   ("sofe:gilded_legionnaire", "gilded legionnaires", "legionarios dorados"), ("sofe:gilded_legionnaire", "The Undefeated", "El Invicto")),
}
FOUND = {"lost_caravan": 1, "skald": 2, "astronomer": 3, "twin_kings": 3, "gladiators": 4}  # tomb: its act

# the chapters: quest, after (objective type and its value; None: the act begins with it), the tomb, who asks
# (npc, where they are: the poet, the scribe and the senator wait in their region's refugee camp), who is told after (npc,
# where), the conversations: (speaker, en, es) lines, the last line the
# answer that moves on (en, es)
CHAPTERS = [
    ("act1_eclipse", ("manual", None), "first_vizier", ("council_general", (-6, 87)), ("council_sage", (0, 88)),
     [("council_general", "The rifts did not open by chance, Bearer. My scouts followed the Void's trail south, to the dunes: a tomb has been opened there.",
       "Las grietas no se abrieron por casualidad, Portador. Mis exploradores siguieron el rastro del Vacío hacia el sur, a las dunas: allí han abierto una tumba."),
      ("council_general", "The First Vizier's. He served the sultan who sealed the Sins, and was buried with the seal's first copy. Something woke him.",
       "La del Primer Visir. Sirvió al sultán que selló a los Pecados y fue enterrado con la primera copia del sello. Algo lo ha despertado."),
      ("council_general", "My soldiers hold the walls; I cannot spare them. Go down into his tomb and put him back to rest.",
       "Mis soldados guardan las murallas; no puedo prescindir de ellos. Baja a su tumba y devuélvelo al descanso.")],
     ("I will go to the dunes.", "Iré a las dunas."),
     [("council_sage", "The Vizier's seal... you carried it out of the dark yourself. Let me read it.", "El sello del Visir... lo has sacado tú mismo de la oscuridad. Déjame leerlo."),
      ("council_sage", "It names the Wardens, Bearer: the order that kept the Observatory. Their dead lie in the Crypt north-east of the city, and their way in with them.",
       "Nombra a los Guardianes, Portador: la orden que custodiaba el Observatorio. Sus muertos yacen en la Cripta al noreste de la ciudad, y con ellos el camino de entrada."),
      ("council_sage", "If the Sentinel is to be stopped, it begins there.", "Si hay que detener al Centinela, empieza allí.")],
     ("To the Wardens' Crypt, then.", "A la Cripta de los Guardianes, entonces.")),
    ("act2_north", ("reach_region", "nordrath"), "frost_king", ("nordrath_elder", (0, -2400)), ("nordrath_elder", (0, -2400)),
     [("nordrath_elder", "You come from the south with the shard's light on you. Good. We need it.", "Vienes del sur con la luz del fragmento encima. Bien. La necesitamos."),
      ("nordrath_elder", "Since the Wrath woke, the Frost King walks in his barrow west of here. His draugr come down to the farms by night.",
       "Desde que despertó la Ira, el Rey de Escarcha camina en su túmulo, al oeste de aquí. Sus draugr bajan a las granjas por la noche."),
      ("nordrath_elder", "Our warriors are at the citadel's foot. Go into his barrow, Bearer, and break his crown.",
       "Nuestros guerreros están al pie de la ciudadela. Entra en su túmulo, Portador, y rompe su corona.")],
     ("I will break it.", "La romperé."),
     [("nordrath_elder", "The Frost King is still. The farms will sleep tonight.", "El Rey de Escarcha está quieto. Las granjas dormirán esta noche."),
      ("nordrath_elder", "In his barrow you saw the runes of the old jarls: the same that seal the Burnt Longhall. The way to Vorath goes through it.",
       "En su túmulo viste las runas de los viejos jarls: las mismas que sellan el Salón Quemado. El camino hasta Vorath pasa por allí.")],
     ("To the Burnt Longhall.", "Al Salón Quemado.")),
    ("act3_east", ("reach_region", "parsivan"), "veiled_queen", ("parsivan_poet", (4400, 100)), ("parsivan_poet", (4400, 100)),
     [("parsivan_poet", "Do you hear it, Bearer? The court sings in its sleep, and the song comes from the Veiled Queen's tomb.",
       "¿Lo oyes, Portador? La corte canta en sueños, y la canción viene de la tumba de la Reina Velada."),
      ("parsivan_poet", "She was the first to dream for Luxara. As long as she sings, no one in Parsivan will wake.",
       "Fue la primera en soñar para Luxara. Mientras cante, nadie en Parsivan despertará."),
      ("parsivan_poet", "Her tomb lies on the western road. Silence her, and bring me her veil.", "Su tumba está en el camino del oeste. Hazla callar y tráeme su velo.")],
     ("I will silence her.", "La haré callar."),
     [("parsivan_poet", "Her veil... still warm with the song. The sleepers turned in their beds when she fell.",
       "Su velo... aún tibio de la canción. Los durmientes se removieron en sus camas cuando cayó."),
      ("parsivan_poet", "It is not enough to wake them, but it is a start. The Abandoned Pavilion holds the rest of her verses.",
       "No basta para despertarlos, pero es un comienzo. El Pabellón Abandonado guarda el resto de sus versos.")],
     ("To the Pavilion.", "Al Pabellón.")),
    ("act3_east", ("reach_region", "khemet"), "scarab_priest", ("khemet_scribe", (5200, 5600)), ("khemet_scribe", (5200, 5600)),
     [("khemet_scribe", "The dead no longer leave, Bearer, and the Scarab Priest is why: he keeps the door of the west closed from inside his house.",
       "Los muertos ya no se marchan, Portador, y el Sacerdote Escarabajo es la razón: mantiene cerrada la puerta del oeste desde dentro de su casa."),
      ("khemet_scribe", "His house is a tomb in the hills by the old river. Go down, find him, and open the door again.",
       "Su casa es una tumba en las colinas junto al viejo río. Baja, encuéntralo y vuelve a abrir la puerta.")],
     ("I will open it.", "La abriré."),
     [("khemet_scribe", "I felt it: a breath out of the west, the first in a hundred years. Some of the dead have gone on.",
       "Lo he sentido: un aliento desde el oeste, el primero en cien años. Algunos muertos se han ido ya."),
      ("khemet_scribe", "The rest wait in the House of the Dead. Its runes are the priest's; you know them now.",
       "Los demás esperan en la Casa de los Muertos. Sus runas son las del sacerdote; ahora las conoces.")],
     ("To the House of the Dead.", "A la Casa de los Muertos.")),
    ("act4_west", ("reach_region", "aureum"), "first_consul", ("aureum_senator", (-5600, 1000)), ("aureum_senator", (-5600, 1000)),
     [("aureum_senator", "The Senate is a ruin, but the First Consul still keeps his oath, Bearer: to guard the treasury of the republic. All of it.",
       "El Senado es una ruina, pero el Primer Cónsul aún cumple su juramento, Portador: guardar el tesoro de la república. Todo."),
      ("aureum_senator", "His tomb is east of the city. While he keeps the keys of the treasury, Goldarc keeps its doors.",
       "Su tumba está al este de la ciudad. Mientras él guarde las llaves del tesoro, Goldarc guardará sus puertas."),
      ("aureum_senator", "Take the keys from him. He will not give them.", "Quítale las llaves. No te las dará.")],
     ("Then I will take them.", "Entonces se las quitaré."),
     [("aureum_senator", "The keys of the republic, in a Bearer's hand. The old men of the Senate would have died of envy.",
       "Las llaves de la república, en la mano de un Portador. Los viejos del Senado se habrían muerto de envidia."),
      ("aureum_senator", "The Fallen Senate is open to you now: its runes still answer to these keys.", "El Senado Caído está abierto para ti: sus runas aún responden a estas llaves.")],
     ("To the Senate.", "Al Senado.")),
    ("act5_ascension", None, "hollow_choir", ("council_elder", (3, 85)), ("council_elder", (3, 85)),
     [("council_elder", "Do you hear the singing under the city, Bearer? It began when the walls broke. A choir with no voices.",
       "¿Oyes el canto bajo la ciudad, Portador? Empezó cuando cayeron las murallas. Un coro sin voces."),
      ("council_elder", "It comes from a hall the Wardens sealed in the west of the plateau. While it sings, the Breach of the Seal stays open.",
       "Viene de una sala que los Guardianes sellaron al oeste de la meseta. Mientras cante, la Brecha del Sello seguirá abierta."),
      ("council_elder", "Go down and silence it.", "Baja y hazlo callar.")],
     ("I will silence it.", "Lo haré callar."),
     [("council_elder", "Silence. For the first time since the siege, silence.", "Silencio. Por primera vez desde el asedio, silencio."),
      ("council_elder", "The Breach can be closed now. Its runes are the seal's own: you have read them all by now, Bearer.",
       "Ahora la Brecha puede cerrarse. Sus runas son las del propio sello: a estas alturas ya las has leído todas, Portador.")],
     ("To the Breach.", "A la Brecha.")),
]


def text(key, en, es):
    EN[key], ES[key] = en, es


def dump(path, value, indent=2):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        json.dump(value, f, indent=indent, ensure_ascii=False)
        f.write("\n")


def de(s):
    return s.replace(" de el ", " del ").replace(" a el ", " al ")


def lower(s):
    return s[0].lower() + s[1:]


def check_place(name, region, xz, positions):
    assert any(x0 <= xz[0] <= x1 and z0 <= xz[1] <= z1 for x0, x1, z0, z1 in REGION_BOUNDS[region]), "%s is not in %s" % (name, region)
    for sid, s in positions["structures"].items():
        if sid.endswith("/tomb_" + name):
            continue
        d = max(abs(s["x"] - xz[0]) - s.get("size_x", 0) / 2, abs(s["z"] - xz[1]) - s.get("size_z", 0) / 2)
        assert d > 80, "%s is too near %s" % (name, sid)


def tomb_steps(name):
    region, (x, z), place, dead, lord = TOMBS[name]
    lair = (x + make_dungeons.CHAMBER[0], z + make_dungeons.CHAMBER[1])
    return [{"objective": {"type": "reach", "x": x, "z": z, "radius": 14}, "target": {"x": x, "z": z}},
            {"objective": {"type": "kill", "entity": dead[0], "count": make_dungeons.CRYPT_DEAD}, "target": {"x": x, "z": z},
             "haunt": make_dungeons.haunt((x, z), dead[0])},
            {"objective": {"type": "kill", "entity": "lord:" + lord[0], "count": 1}, "target": {"x": lair[0], "z": lair[1]},
             "lair": {"x": lair[0], "z": lair[1], "depth": 18, "name": "lord.sofe.tomb_%s" % name}}]


def tomb_texts(key, first, name):
    """The texts of a tomb's three steps, from step number first."""
    region, xz, place, dead, lord = TOMBS[name]
    text("%s.step%d" % (key, first), "Find %s." % place[0], "Encuentra %s." % lower(place[1]))
    text("%s.step%d" % (key, first + 1), "Go down through its halls and drive out the %s." % dead[1],
         "Baja por sus salas y acaba con sus muertos: %s." % dead[2])
    text("%s.step%d" % (key, first + 2), "In its deepest chamber, defeat %s." % lower(lord[1]), de("En su cámara más honda, derrota a %s." % lower(lord[2])))


def conversation(quest_id, step, npc, lines, answer, name):
    """A conversation that is the step's (it is offered while the quest stands at that step) and moves it on."""
    key = "dialogue.sofe.chapter.%s" % name
    out = []
    for i, (speaker, en, es) in enumerate(lines, start=1):
        text("%s.%d" % (key, i), en, es)
        out.append({"speaker": speaker, "text": "%s.%d" % (key, i)})
    text(key + ".go", answer[0], answer[1])
    out[-1]["answers"] = [{"text": key + ".go", "next": -1, "effects": [{"type": "advance_quest", "quest": quest_id}]}]
    dump(os.path.join(DATA, "dialogue", "chapter", name + ".json"),
         {"style": next((r for r in ("nordrath", "parsivan", "khemet", "aureum") if npc.startswith(r)), "sulthari"), "npc": npc, "priority": 20,
          "requires": {"type": "all_of", "conditions": [{"type": "quest_step", "quest": quest_id, "step": step},
                                                         {"type": "not", "condition": {"type": "quest_step", "quest": quest_id, "step": step + 1}}]},
          "lines": out})


def chapter(quest_name, after, tomb, asker, teller, ask_lines, ask_answer, tell_lines, tell_answer):
    path = os.path.join(DATA, "quests", quest_name + ".json")
    with open(path, encoding="utf-8") as f:
        q = json.load(f)
    quest_id, key = "sofe:" + quest_name, "quest.sofe." + quest_name
    mark = "%s/%s" % (quest_name, tomb)
    steps = [dict({"objective": {"type": "manual"}, "target": {"x": asker[1][0], "z": asker[1][1]}}, _chapter=mark)]
    steps += [dict(s, _chapter=mark) for s in tomb_steps(tomb)]
    steps.append(dict({"objective": {"type": "manual"}, "target": {"x": teller[1][0], "z": teller[1][1]}}, _chapter=mark))
    there = [i for i, st in enumerate(q["steps"]) if st.get("_chapter") == mark]
    if there:
        at = there[0]
        q["steps"][at:at + len(there)] = steps
    else:
        if after is None:
            at = 0
        else:
            at = next(i for i, st in enumerate(q["steps"]) if st["objective"]["type"] == after[0]
                      and (after[1] is None or st["objective"].get("region") == after[1])) + 1
        make_dungeons.move_conditions(quest_id, at, len(steps))
        for code in LANGS:  # the later steps' texts move along, from the last
            for n in range(len(q["steps"]), at, -1):
                if "%s.step%d" % (key, n) in LANGS[code]:
                    LANGS[code]["%s.step%d" % (key, n + len(steps))] = LANGS[code].pop("%s.step%d" % (key, n))
        q["steps"][at:at] = steps
    dump(path, q)
    # the texts: steps are counted from 1, the conversations' quest_step too
    region, xz, place, dead, lord = TOMBS[tomb]
    who = lambda npc: "npc.sofe." + npc  # noqa: E731
    asker_name = LANGS["en_us"].get(who(asker[0]), asker[0]), LANGS["es_es"].get(who(asker[0]), asker[0])
    teller_name = LANGS["en_us"].get(who(teller[0]), teller[0]), LANGS["es_es"].get(who(teller[0]), teller[0])
    text("%s.step%d" % (key, at + 1), "Hear what %s has to say." % asker_name[0], "Escucha lo que tiene que decir %s." % asker_name[1])
    tomb_texts(key, at + 2, tomb)
    text("%s.step%d" % (key, at + 5), "Take what you found to %s." % teller_name[0], "Lleva lo que encontraste a %s." % teller_name[1])
    conversation(quest_id, at + 1, asker[0], ask_lines, ask_answer, "%s_%s_ask" % (quest_name, tomb))
    conversation(quest_id, at + 5, teller[0], tell_lines, tell_answer, "%s_%s_tell" % (quest_name, tomb))
    return at


# the road from one act to the next: the act's quest, the next one, the conversation at its end, the Waystone of the
# next land, and the words (en, es) of the offer, of going and of staying
TRANSITIONS = [
    ("act1_eclipse", "act2_north", "act1/epilogue", "sofe:nordrath/city",
     ("The Seal's Veil to the north is open. Nordrath waits.", "El Velo del Sello hacia el norte está abierto. Nordrath espera."),
     ("Travel to Nordrath now.", "Viajar a Nordrath ahora."), ("Stay in Sulthari a while.", "Quedarme un tiempo en Sulthari.")),
    ("act2_north", "act3_east", "act2/thank_you", "sofe:parsivan/city",
     ("The road east is open: Parsivan, and Khemet beyond it.", "El camino al este está abierto: Parsivan, y más allá Khemet."),
     ("Travel to Parsivan now.", "Viajar a Parsivan ahora."), ("Not yet.", "Todavía no.")),
    ("act3_east", "act4_west", "act3/the_sultans_diary", "sofe:aureum/city",
     ("The road west is open: Aureum and its vaults.", "El camino al oeste está abierto: Aureum y sus bóvedas."),
     ("Travel to Aureum now.", "Viajar a Aureum ahora."), ("Not yet.", "Todavía no.")),
    ("act4_west", "act5_ascension", "act4/sulthari_besieged", "sofe:sulthari/plaza",
     ("Sulthari is under siege. Home is a step away.", "Sulthari está sitiada. El hogar está a un paso."),
     ("Go home to Sulthari now.", "Volver a Sulthari ahora."), ("Not yet.", "Todavía no.")),
]


def transition(quest_name, next_name, dialogue, waystone, offer, go, stay):
    """When an act ends, the next begins at once (no longer only when its closing conversation is read to the end), and
    that conversation ends offering the road: to the next land's Waystone now, or later."""
    path = os.path.join(DATA, "quests", quest_name + ".json")
    with open(path, encoding="utf-8") as f:
        q = json.load(f)
    start = {"type": "start_quest", "quest": "sofe:" + next_name}
    if start not in q["rewards"]:
        at = next((i for i, r in enumerate(q["rewards"]) if r["type"] == "advance_act"), len(q["rewards"]) - 1) + 1
        q["rewards"].insert(at, start)
    dump(path, q)
    dpath = os.path.join(DATA, "dialogue", dialogue + ".json")
    with open(dpath, encoding="utf-8") as f:
        d = json.load(f)
    key = "dialogue.sofe.road.%s" % next_name
    text(key, offer[0], offer[1])
    text(key + ".go", go[0], go[1])
    text(key + ".stay", stay[0], stay[1])
    d["lines"] = [line for line in d["lines"] if line.get("text") != key]
    d["lines"].append({"speaker": "narrator", "text": key, "answers": [
        {"text": key + ".go", "next": -1, "effects": [start, {"type": "travel", "waystone": waystone}]},
        {"text": key + ".stay", "next": -1, "effects": [start]}]})
    dump(dpath, d)


def found(name, act, positions):
    """A tomb found on the way: a dungeon quest that begins by itself near it, once its act has come."""
    region, (x, z), place, dead, lord = TOMBS[name]
    q = {"type": "dungeon", "act": act, "starts_near": {"x": x, "z": z, "radius": 64}, "steps": tomb_steps(name),
         "rewards": [{"type": "give_xp", "amount": {1: 450, 2: 900, 3: 1600, 4: 2400, 5: 3200}[act]}],
         "requires": {"type": "act_reached", "act": act}}
    dump(os.path.join(DATA, "quests", "dungeon", "tomb_%s.json" % name), q)
    key = "quest.sofe.dungeon.tomb_%s" % name
    text(key, place[0], place[1])
    tomb_texts(key, 1, name)


def main():
    for code in ("en_us", "es_es"):
        with open(os.path.join(LANG, code + ".json"), encoding="utf-8") as f:
            LANGS[code] = json.load(f)
    path = os.path.join(DATA, "structure_positions.json")
    with open(path, encoding="utf-8") as f:
        positions = json.load(f)
    for name, (region, xz, place, dead, lord) in TOMBS.items():
        check_place(name, region, xz, positions)
        positions["structures"]["sofe:%s/tomb_%s" % (region, name)] = {"x": xz[0], "z": xz[1], "size_x": make_dungeons.CRYPT_SIZE,
                                                                        "size_z": make_dungeons.CRYPT_SIZE, "zone": "dungeon"}
        text("place.sofe.%s.tomb_%s" % (region, name), place[0], place[1])
        positions["waystones"]["sofe:%s/tomb_%s" % (region, name)] = {"x": xz[0] + 5, "z": xz[1] + 13}  # by its court, to travel back
        text("waystone.sofe.%s.tomb_%s" % (region, name), place[0], place[1])
        text("lord.sofe.tomb_%s" % name, lord[1], lord[2])
    dump(path, positions, indent=1)
    for c in CHAPTERS:
        chapter(*c)
    for name, act in FOUND.items():
        found(name, act, positions)
    for t in TRANSITIONS:
        transition(*t)
    for code, entries in (("en_us", EN), ("es_es", ES)):
        LANGS[code].update(entries)
        with open(os.path.join(LANG, code + ".json"), "w", encoding="utf-8", newline="\n") as f:
            json.dump(LANGS[code], f, ensure_ascii=False, indent=2)
            f.write("\n")
    print("%d chapters, %d tombs (%d to be found on the way), %d texts" % (len(CHAPTERS), len(TOMBS), len(FOUND), len(EN)))


if __name__ == "__main__":
    main()
