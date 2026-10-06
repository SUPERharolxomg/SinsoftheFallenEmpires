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
VOID_GATE = (-48, -96)   # sofe:sulthari/void_gate, beside the Great Observatory


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
        # the Throne opens only to a Bearer who carries the Sealing Quill (docs/Mundo.md, W5)
        "inverted_throne": {"type": "all_of", "conditions": [{"type": "boss_defeated", "boss": "sofe:prython"},
                                                             {"type": "item_owned", "item": "sofe:sealing_quill"}]},
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
        {"objective": {"type": "obtain_item", "item": "sofe:sealing_quill"}, "target": {"x": VOID_GATE[0], "z": VOID_GATE[1]},
         "on_start": [{"type": "open_dialogue", "dialogue": "sofe:act5/the_inverted_throne"}]},
        {"objective": {"type": "defeat_boss", "boss": "sofe:nahrazel"}, "target": {"x": THRONE[0], "z": THRONE[1]},
         "on_start": [{"type": "open_dialogue", "dialogue": "sofe:act5/the_quill_forged"}]},
    ],
    "rewards": [{"type": "give_xp", "amount": 30000}, {"type": "open_dialogue", "dialogue": "sofe:act5/the_ending"}],
}
QUEST_TEXT = (("The Ascension", "La Ascensión"), [
    ("Defeat Solrath in the Temple of Sulthari", "Derrota a Solrath en el Templo de Sulthari"),
    ("Climb the Celestial Spire and defeat Prython", "Sube a la Aguja Celestial y derrota a Prython"),
    ("Cross the Void Gate and forge the Sealing Quill with Void Ink", "Cruza la Puerta del Vacío y forja la Pluma Selladora con Tinta del Vacío"),
    ("Descend to the Inverted Throne and defeat Nahrazel", "Desciende al Trono Invertido y derrota a Nahrazel")])

# dialogue id: (style, [(speaker, en, es, answers or None)], on_end)
REFUSE = [("I refuse.", "Me niego."), ("(Say nothing, and raise your weapon.)", "(No dices nada y alzas tu arma.)")]
# Prython's offer can be taken: "Crowned in Ash", the secret bad ending (docs/Jugabilidad.md), then the offer again
OFFER = [("I refuse.", "Me niego."),
         ("(Take the crown.)", "(Tomas la corona.)", [
             {"type": "award_advancement", "advancement": "secret/crowned_in_ash"},
             {"type": "play_scene", "scene": "crowned_in_ash", "then": "sofe:act5/prythons_offer"}])]
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
         "Basta. Entrégame los seis fragmentos, y te coronaré sobre toda Aetheris. Sin Consejo. Sin Ley. Solo tú.", OFFER)], None),
    "act5/the_inverted_throne": ("void", [
        ("narrator", "Prython falls, and the Spire falls with him: it turns over in the sky and sinks, point first, through the city and into the earth.",
         "Prython cae, y la Aguja cae con él: se da la vuelta en el cielo y se hunde, punta primero, a través de la ciudad y dentro de la tierra.", None),
        ("narrator", "Where it went down, a stair winds into the dark. At the bottom is the Inverted Throne, and on it, the one the Codex was made to hold.",
         "Donde se hundió, una escalera se enrosca hacia la oscuridad. Al fondo está el Trono Invertido, y en él, aquel para quien se hizo el Códice.", None)],
        [{"type": "open_dialogue", "dialogue": "sofe:act5/the_sealing_quill"}]),
    "act5/the_sealing_quill": ("void", [
        ("nilufar", "The stair will not open for you, Bearer. A seal is a page, and his was written from the other side.",
         "La escalera no se abrirá para ti, Portador. Un sello es una página, y el suyo se escribió desde el otro lado.", None),
        ("nilufar", "Go through the Void Gate, into the Outer Void. Bring back its crystal, and the ink the Alembic draws from it.",
         "Cruza la Puerta del Vacío, hacia el Vacío Exterior. Trae su cristal, y la tinta que el Alambique saca de él.", None),
        ("nilufar", "A feather, the ink, a crystal of the Void and a shard of clean aetherium: that is the Sealing Quill. Only with it will the Throne let you down.",
         "Una pluma, la tinta, un cristal del Vacío y un fragmento de aetherio limpio: esa es la Pluma Selladora. Solo con ella te dejará bajar el Trono.", None)], None),
    "act5/the_quill_forged": ("void", [
        ("narrator", "The Quill drinks the Void Ink, and its nib glows like the edge of a page about to burn. Beneath Sulthari, the stair to the Inverted Throne lies open.",
         "La Pluma bebe la Tinta del Vacío, y su punta brilla como el borde de una página a punto de arder. Bajo Sulthari, la escalera al Trono Invertido está abierta.", None)], None),
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

# "Crowned in Ash" (CrownedInAshScreen): four lines for each Bearer, ruling a burning Aetheris
CROWNED = {
    "knight": [
        ("The Order of the Scale kneels before you. You melted its scales into your crown.",
         "La Orden de la Balanza se arrodilla ante ti. Fundiste sus balanzas para hacer tu corona."),
        ("The courts of Aureum judge in your name now, and every sentence is the same.",
         "Los tribunales de Aureum juzgan ahora en tu nombre, y todas las sentencias son la misma."),
        ("Your wrath is the only Law left, and no one has lived to break it.",
         "Tu ira es la única Ley que queda, y nadie ha vivido para romperla."),
        ("Cassian sits on a throne of ash, and the scale in his hand weighs nothing at all.",
         "Cassian se sienta en un trono de ceniza, y la balanza en su mano no pesa nada.")],
    "necromancer": [
        ("The gates of the underworld stand open, and no soul is allowed through them.",
         "Las puertas del inframundo están abiertas, y a ninguna alma se le permite cruzarlas."),
        ("The dead of Khemet stand in rows forever, guarding a king who never walks.",
         "Los muertos de Khemet forman filas para siempre, guardando a un rey que nunca camina."),
        ("Among them is your master, still waiting for the journey you promised him.",
         "Entre ellos está tu maestro, esperando todavía el viaje que le prometiste."),
        ("Ankhareth rules the dead of Aetheris, and none of them will ever rest.",
         "Ankhareth reina sobre los muertos de Aetheris, y ninguno descansará jamás.")],
    "sorceress": [
        ("The stars of Parsivan burn out one by one, until only your light is left in the sky.",
         "Las estrellas de Parsivan se apagan una a una, hasta que solo tu luz queda en el cielo."),
        ("The court dreams of you now, and only of you.",
         "La corte sueña ahora contigo, y solo contigo."),
        ("Laleh's voice still calls from the Gardens. You stopped listening long ago.",
         "La voz de Laleh aún llama desde los Jardines. Dejaste de escucharla hace mucho."),
        ("Shirin draws the new map of the sky, and her sister's name is not on it.",
         "Shirin dibuja el nuevo mapa del cielo, y el nombre de su hermana no está en él.")],
    "thief": [
        ("All the gold of Aetheris fills your vaults. The Nordrath clans dig it for you, in chains.",
         "Todo el oro de Aetheris llena tus cámaras. Los clanes de Nordrath lo excavan para ti, encadenados."),
        ("Your village was never rebuilt. There was no profit in it.",
         "Tu aldea nunca se reconstruyó. No había ganancia en ello."),
        ("You count your coins every night, and every night one is missing.",
         "Cuentas tus monedas cada noche, y cada noche falta una."),
        ("Rurik owns everything, and keeps nothing worth keeping.",
         "Rurik lo posee todo, y no guarda nada que valga la pena.")],
    "king": [
        ("Emperor of Aetheris. Every crown of every empire was melted into yours.",
         "Emperador de Aetheris. Cada corona de cada imperio se fundió en la tuya."),
        ("There is no Council now, and no Pact: only your word, carved in every square.",
         "Ya no hay Consejo, ni Pacto: solo tu palabra, tallada en cada plaza."),
        ("Sulthari kneels in the ash of its own Observatory, and calls it glory.",
         "Sulthari se arrodilla en la ceniza de su propio Observatorio, y lo llama gloria."),
        ("Azhar sits beneath the false sun, and the Codex beneath his throne lies open.",
         "Azhar se sienta bajo el sol falso, y el Códice bajo su trono yace abierto.")],
}
SCENE_TEXT = {
    "scene.sofe.crowned_in_ash": ("Crowned in Ash", "Coronado en Ceniza"),
    "scene.sofe.crowned_in_ash.title": ("Crowned in Ash", "Coronado en Ceniza"),
    "scene.sofe.crowned_in_ash.wake": ("You blink. The wind on the Spire. Prython's hand is still held out to you.",
                                       "Parpadeas. El viento en la Aguja. La mano de Prython sigue tendida hacia ti."),
    "advancement.sofe.crowned_in_ash": ("Crowned in Ash", "Coronado en Ceniza"),
    "advancement.sofe.crowned_in_ash.desc": ("Wear Pride's crown, for a moment.", "Lleva la corona de la Soberbia, por un momento."),
    "item.sofe.sealing_quill": ("Sealing Quill", "Pluma Selladora"),
    "item.sofe.sealing_quill.desc": ("Ink from the other side of the seal. It opens the Inverted Throne.",
                                     "Tinta del otro lado del sello. Abre el Trono Invertido."),
}
# the hidden advancement: no hint, no message in the chat (docs/Jugabilidad.md: its only trace)
SECRET_ADVANCEMENT = {
    "display": {"icon": {"item": "minecraft:golden_helmet"}, "title": {"translate": "advancement.sofe.crowned_in_ash"},
                "description": {"translate": "advancement.sofe.crowned_in_ash.desc"}, "frame": "challenge",
                "show_toast": True, "announce_to_chat": False, "hidden": True},
    "criteria": {"done": {"trigger": "minecraft:impossible"}},
    "parent": "sofe:story/boss_solrath",
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
                for j, answer in enumerate(answers, 1):
                    ak = "%s.answer%d" % (k, j)
                    lang[ak] = answer[:2]
                    a = {"text": ak, "next": -1}
                    if len(answer) > 2:
                        a["effects"] = answer[2]
                    line["answers"].append(a)
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
    for hero, lines in CROWNED.items():
        for i, line in enumerate(lines, 1):
            lang["scene.sofe.crowned_in_ash.%s.%d" % (hero, i)] = line
    lang.update(SCENE_TEXT)
    os.makedirs(os.path.join(DATA, "advancements", "secret"), exist_ok=True)
    with open(os.path.join(DATA, "advancements", "secret", "crowned_in_ash.json"), "w", encoding="utf-8") as f:
        f.write(json.dumps(SECRET_ADVANCEMENT, indent=2) + "\n")
    for id, name in (("solrath", ("Solrath", "Solrath")), ("prython", ("Prython", "Prython")), ("nahrazel", ("Nahrazel", "Nahrazel"))):
        lang["npc.sofe." + id] = name
    for cid, hint in (("temple_of_sulthari", ("The Temple opens in Act V", "El Templo se abre en el Acto V")),
                      ("celestial_spire", ("Defeat Solrath to climb the Celestial Spire", "Derrota a Solrath para subir a la Aguja Celestial")),
                      ("inverted_throne", ("Defeat Prython and carry the Sealing Quill to descend to the Inverted Throne",
                                           "Derrota a Prython y lleva la Pluma Selladora para descender al Trono Invertido"))):
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
