"""Act V, the Ascension (README): the Temple of Sulthari (Solrath, who is Ozhan), the Celestial Spire (Prython) and
the Inverted Throne beneath the city (Nahrazel, the First Fallen). Writes their places, gates, Waystones and lairs,
the main quest that follows "Sulthari is under siege", its scenes in English and Spanish, and the bosses' portraits.

Run from the repository root: python scripts/make_act5_story.py
"""
import json
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))
from make_temptations import portrait  # noqa: E402

DATA = os.path.join("src", "main", "resources", "data", "sofe")
LANG = os.path.join("src", "main", "resources", "assets", "sofe", "lang")
SPIRE_TOP, THRONE_FLOOR = 200, -20   # EmpireBuilder.SPIRE_TOP and THRONE_FLOOR

TEMPLE, SPIRE, THRONE = (700, 700), (-700, 700), (-700, 1050)


def places():
    path = os.path.join(DATA, "structure_positions.json")
    pos = json.load(open(path, encoding="utf-8"))
    S = pos["structures"]
    S["sofe:sulthari/temple"] = {"x": TEMPLE[0], "z": TEMPLE[1], "size_x": 48, "size_z": 48, "zone": "dungeon"}
    S["sofe:sulthari/temple_hall"] = {"x": TEMPLE[0], "z": TEMPLE[1], "size_x": 34, "size_z": 34, "zone": "arena"}
    S["sofe:sulthari/celestial_spire"] = {"x": SPIRE[0], "z": SPIRE[1], "size_x": 40, "size_z": 40, "zone": "dungeon"}
    S["sofe:sulthari/spire_crown"] = {"x": SPIRE[0], "z": SPIRE[1], "size_x": 34, "size_z": 34, "min_y": SPIRE_TOP - 2, "max_y": SPIRE_TOP + 30, "zone": "arena"}
    S["sofe:sulthari/inverted_throne"] = {"x": THRONE[0], "z": THRONE[1], "size_x": 64, "size_z": 80, "zone": "dungeon"}
    S["sofe:sulthari/throne_floor"] = {"x": THRONE[0], "z": THRONE[1], "size_x": 56, "size_z": 56, "min_y": THRONE_FLOOR - 4, "max_y": THRONE_FLOOR + 40, "zone": "arena"}
    G = pos["gates"]
    G["sofe:sulthari/temple"] = {"x": TEMPLE[0] - 24, "z": TEMPLE[1], "condition": "sofe:temple_of_sulthari", "hint": "message.sofe.gate.temple_of_sulthari", "axis": "x"}
    G["sofe:sulthari/celestial_spire"] = {"x": SPIRE[0], "z": SPIRE[1] + 8, "condition": "sofe:celestial_spire", "hint": "message.sofe.gate.celestial_spire"}
    G["sofe:sulthari/inverted_throne"] = {"x": THRONE[0], "z": THRONE[1] + 36, "condition": "sofe:inverted_throne", "hint": "message.sofe.gate.inverted_throne"}
    W = pos["waystones"]
    for old in ("sofe:sulthari/temple", "sofe:sulthari/celestial_spire", "sofe:sulthari/inverted_throne"):
        W.pop(old, None)  # outside the city walls they are not Sulthari's own Waystones
    W["sofe:ascension/temple"] = {"x": TEMPLE[0] - 30, "z": TEMPLE[1]}
    W["sofe:ascension/celestial_spire"] = {"x": SPIRE[0] + 6, "z": SPIRE[1] + 15}
    W["sofe:ascension/inverted_throne"] = {"x": THRONE[0] + 6, "z": THRONE[1] + 42}
    open(path, "w", encoding="utf-8").write(json.dumps(pos, ensure_ascii=False, indent=2) + "\n")

    conditions = {
        "temple_of_sulthari": {"type": "act_reached", "act": 5},
        "celestial_spire": {"type": "boss_defeated", "boss": "sofe:solrath"},
        "inverted_throne": {"type": "boss_defeated", "boss": "sofe:prython"},
    }
    for cid, cond in conditions.items():
        with open(os.path.join(DATA, "conditions", cid + ".json"), "w", encoding="utf-8") as f:
            f.write(json.dumps(cond, indent=2) + "\n")

    path = os.path.join(DATA, "boss_lairs.json")
    lairs = json.load(open(path, encoding="utf-8"))
    lairs["lairs"]["sofe:solrath"] = {"x": TEMPLE[0], "z": TEMPLE[1], "radius": 16}
    lairs["lairs"]["sofe:prython"] = {"x": SPIRE[0], "z": SPIRE[1], "radius": 15, "y": SPIRE_TOP + 1}
    lairs["lairs"]["sofe:nahrazel"] = {"x": THRONE[0], "z": THRONE[1], "radius": 26, "y": THRONE_FLOOR}
    open(path, "w", encoding="utf-8").write(json.dumps(lairs, ensure_ascii=False, indent=2) + "\n")


QUEST = {
    "type": "main", "act": 5,
    "steps": [
        {"objective": {"type": "defeat_boss", "boss": "sofe:solrath"}, "target": {"x": TEMPLE[0], "z": TEMPLE[1]},
         "on_start": [{"type": "open_dialogue", "dialogue": "sofe:act5/the_siege"}]},
        {"objective": {"type": "defeat_boss", "boss": "sofe:prython"}, "target": {"x": SPIRE[0], "z": SPIRE[1]},
         "on_start": [{"type": "open_dialogue", "dialogue": "sofe:act5/the_spire"}]},
        {"objective": {"type": "defeat_boss", "boss": "sofe:nahrazel"}, "target": {"x": THRONE[0], "z": THRONE[1]},
         "on_start": [{"type": "open_dialogue", "dialogue": "sofe:act5/the_inverted_throne"}]},
    ],
    "rewards": [{"type": "give_xp", "amount": 30000}, {"type": "open_dialogue", "dialogue": "sofe:act5/the_ending"}],
}
QUEST_TEXT = (("The Ascension", "La Ascensión"), [
    ("Defeat Solrath in the Temple of Sulthari", "Derrota a Solrath en el Templo de Sulthari"),
    ("Climb the Celestial Spire and defeat Prython", "Sube a la Aguja Celestial y derrota a Prython"),
    ("Descend to the Inverted Throne and defeat Nahrazel", "Desciende al Trono Invertido y derrota a Nahrazel")])

# dialogue id: (style, [(speaker, en, es, answers or None)], on_end)
REFUSE = [("I refuse.", "Me niego."), ("(Say nothing, and raise your weapon.)", "(No dices nada y alzas tu arma.)")]
DIALOGUES = {
    "act5/the_siege": ("sulthari", [
        ("narrator", "Sulthari burns. Over the Great Observatory a spire of white light hangs upside down from the sky, and the Void pours from its tip into the streets.",
         "Sulthari arde. Sobre el Gran Observatorio una aguja de luz blanca cuelga del cielo al revés, y el Vacío se derrama desde su punta sobre las calles.", None),
        ("council_elder", "Bearer! Ozhan sealed himself in the Temple when the walls broke. Since then the light has only grown. Go to him.",
         "¡Portador! Ozhan se encerró en el Templo cuando cayeron las murallas. Desde entonces la luz no ha dejado de crecer. Ve con él.", None)], None),
    "act5/ozhan_unmasked": ("sulthari", [
        ("ozhan", "You came. Of course you came: you always do what you are told, Bearer. It is what I liked most about you.",
         "Has venido. Claro que has venido: siempre haces lo que te mandan, Portador. Es lo que más me gustaba de ti.", None),
        ("ozhan", "Every fragment you won, I counted. Every Archsin you killed, I thanked. Prython promised me a seat above the Law, and I have taken it.",
         "Cada fragmento que ganaste, yo lo conté. Cada Archipecado que mataste, yo lo agradecí. Prython me prometió un sitio por encima de la Ley, y lo he tomado.", None),
        ("solrath", "Kneel before Solrath. The Law was only ever a leash.", "Arrodíllate ante Solrath. La Ley nunca fue más que una correa.",
         REFUSE)], None),
    "act5/the_spire": ("sulthari", [
        ("narrator", "With Solrath fallen, the light over the Observatory falters, and a stair opens in the Celestial Spire. It climbs into the sky, where Pride waits.",
         "Con Solrath caído, la luz sobre el Observatorio vacila, y en la Aguja Celestial se abre una escalera. Sube hacia el cielo, donde la Soberbia espera.", None)], None),
    "act5/prython_temptation": ("void", [
        ("prython", "Six of my brothers fell to you. Six! Do you understand what that makes you? Not a Bearer. A ruler.",
         "Seis de mis hermanos cayeron ante ti. ¡Seis! ¿Entiendes lo que eso te hace? No un Portador. Un soberano.", None),
        ("prython", "Look down. Every empire of Aetheris fits in your hand from here. Why give it back to the weak?",
         "Mira abajo. Desde aquí, cada imperio de Aetheris cabe en tu mano. ¿Por qué devolvérselo a los débiles?", REFUSE)], None),
    "act5/prythons_offer": ("void", [
        ("prython", "Enough. Hand me the six fragments, and I will crown you over all of Aetheris. No Council. No Law. Only you.",
         "Basta. Entrégame los seis fragmentos, y te coronaré sobre toda Aetheris. Sin Consejo. Sin Ley. Solo tú.", REFUSE)], None),
    "act5/the_inverted_throne": ("void", [
        ("narrator", "Prython falls, and the Spire falls with him: it turns over in the sky and sinks, point first, through the city and into the earth.",
         "Prython cae, y la Aguja cae con él: se da la vuelta en el cielo y se hunde, punta primero, a través de la ciudad y dentro de la tierra.", None),
        ("narrator", "Where it went down, a stair winds into the dark. At the bottom is the Inverted Throne, and on it, the one the Codex was made to hold.",
         "Donde se hundió, una escalera se enrosca hacia la oscuridad. Al fondo está el Trono Invertido, y en él, aquel para quien se hizo el Códice.", None)], None),
    "act5/the_first_fallen": ("void", [
        ("nahrazel", "Seven fragments. You carried every one of them to my door. Thank you, Bearer.",
         "Siete fragmentos. Los trajiste todos hasta mi puerta. Gracias, Portador.", None),
        ("nahrazel", "Now open it.", "Ahora ábrela.", REFUSE)], None),
    "act5/the_ending": ("void", [
        ("narrator", "The First Fallen breaks like ash in the wind. For a moment the Codex closes with you inside its pages, and there your own sin waits for you one last time.",
         "El Primer Caído se deshace como ceniza al viento. Por un momento el Códice se cierra contigo dentro de sus páginas, y allí tu propio pecado te espera una última vez.", None),
        ("narrator", "You turn it down. You wake among the ruins of the Inverted Throne, and above you, through the broken city, the black aetherium of Aetheris begins to run clear.",
         "Lo rechazas. Despiertas entre las ruinas del Trono Invertido, y sobre ti, a través de la ciudad rota, el aetherio negro de Aetheris empieza a aclararse.", None)], None),
}


def story():
    lang = {}
    with open(os.path.join(DATA, "quests", "act5_ascension.json"), "w", encoding="utf-8") as f:
        f.write(json.dumps(QUEST, indent=2) + "\n")
    lang["quest.sofe.act5_ascension"] = QUEST_TEXT[0]
    for i, step in enumerate(QUEST_TEXT[1], 1):
        lang["quest.sofe.act5_ascension.step%d" % i] = step
    for did, (style, lines, on_end) in DIALOGUES.items():
        out = []
        for i, (speaker, en, es, answers) in enumerate(lines, 1):
            k = "dialogue.sofe.%s.%d" % (did.replace("/", "."), i)
            line = {"speaker": speaker, "text": k}
            lang[k] = (en, es)
            if answers:
                line["answers"] = []
                for j, (aen, aes) in enumerate(answers, 1):
                    ak = "%s.answer%d" % (k, j)
                    lang[ak] = (aen, aes)
                    line["answers"].append({"text": ak, "next": -1})
            out.append(line)
        d = {"style": style, "cinematic": True, "lines": out}
        if on_end:
            d["on_end"] = on_end
        os.makedirs(os.path.join(DATA, "dialogue", "act5"), exist_ok=True)
        with open(os.path.join(DATA, "dialogue", did + ".json"), "w", encoding="utf-8") as f:
            f.write(json.dumps(d, ensure_ascii=False, indent=2) + "\n")
    # the end of Act IV leads into Act V
    path = os.path.join(DATA, "dialogue", "act4", "sulthari_besieged.json")
    d = json.load(open(path, encoding="utf-8"))
    d["on_end"] = [{"type": "start_quest", "quest": "sofe:act5_ascension"}]
    with open(path, "w", encoding="utf-8") as f:
        f.write(json.dumps(d, ensure_ascii=False, indent=2) + "\n")
    for id, name in (("solrath", ("Solrath", "Solrath")), ("prython", ("Prython", "Prython")), ("nahrazel", ("Nahrazel", "Nahrazel"))):
        lang["npc.sofe." + id] = name
    for cid, hint in (("temple_of_sulthari", ("The Temple opens in Act V", "El Templo se abre en el Acto V")),
                      ("celestial_spire", ("Defeat Solrath to climb the Celestial Spire", "Derrota a Solrath para subir a la Aguja Celestial")),
                      ("inverted_throne", ("Defeat Prython to descend to the Inverted Throne", "Derrota a Prython para descender al Trono Invertido"))):
        lang["message.sofe.gate." + cid] = hint
    for f, i in (("en_us", 0), ("es_es", 1)):
        p = os.path.join(LANG, f + ".json")
        data = json.load(open(p, encoding="utf-8"))
        for k, v in lang.items():
            data[k] = v[i]
        open(p, "w", encoding="utf-8").write(json.dumps(data, ensure_ascii=False, indent=2) + "\n")
    for id in ("solrath", "prython", "nahrazel"):
        portrait(id, "aureum" if id != "nahrazel" else "parsivan")


if __name__ == "__main__":
    places()
    story()
    print("act V written")
