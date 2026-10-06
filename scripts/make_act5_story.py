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
    # the ending (EndingScreen): the Codex, the fates of the regions, the Bearer's epilogue, the eighth lock; then the post-game
    "rewards": [{"type": "give_xp", "amount": 30000}, {"type": "award_advancement", "advancement": "story/the_seal_rewritten"},
                {"type": "play_scene", "scene": "the_ending", "then": "sofe:act5/after_the_ending"}],
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
    "act5/after_the_ending": ("sulthari", [
        ("council_elder", "Bearer. The Council will carve your name above the door of the Observatory, whether you like it or not.",
         "Portador. El Consejo grabará tu nombre sobre la puerta del Observatorio, te guste o no.", None),
        ("council_elder", "Aetheris is open to you, all of it. And if you miss the fighting: kneel in the middle of any arena where a great one fell, and its Echo will rise to meet you again.",
         "Aetheris está abierta para ti, entera. Y si echas de menos la lucha: arrodíllate en el centro de cualquier arena donde cayó uno de los grandes, y su Eco se alzará para enfrentarte de nuevo.", None)], None),
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
# the ending (EndingScreen, com.sofe.story.Epilogue): every line in English and Spanish
ENDING = {
    "codex.1": ("The First Fallen breaks like ash in the wind, and the Codex closes over you. Its pages are made of light.",
                "El Primer Caído se deshace como ceniza al viento, y el Códice se cierra sobre ti. Sus páginas están hechas de luz."),
    "codex.2": ("One by one, under the Sealing Quill, the seven locks turn: Wrath, Lust, Sloth, Gluttony, Greed, Envy, Pride.",
                "Una a una, bajo la Pluma Selladora, giran las siete cerraduras: Ira, Lujuria, Pereza, Gula, Avaricia, Envidia, Soberbia."),
    "codex.sin.knight": ("On the last page your own wrath waits for you. \"Strike once more,\" it says, \"and no one will ever wrong you again.\"",
                         "En la última página te espera tu propia ira. \"Golpea una vez más\", dice, \"y nadie volverá a hacerte daño.\""),
    "codex.sin.necromancer": ("On the last page your own sloth waits for you. \"Lie down,\" it says. \"The dead have waited this long; they can wait forever.\"",
                              "En la última página te espera tu propia pereza. \"Túmbate\", dice. \"Los muertos han esperado tanto; pueden esperar para siempre.\""),
    "codex.sin.sorceress": ("On the last page your own longing waits for you, in Laleh's voice. \"Stay,\" it says. \"Here, I never left.\"",
                            "En la última página te espera tu propio anhelo, con la voz de Laleh. \"Quédate\", dice. \"Aquí nunca me fui.\""),
    "codex.sin.thief": ("On the last page your own greed waits for you. \"Keep one page,\" it says. \"Just one. Who would ever know?\"",
                        "En la última página te espera tu propia avaricia. \"Quédate una página\", dice. \"Solo una. ¿Quién lo sabría?\""),
    "codex.sin.king": ("On the last page your own pride waits for you, wearing your crown. \"You sealed a god,\" it says. \"Who is left to rule you?\"",
                       "En la última página te espera tu propia soberbia, con tu corona puesta. \"Sellaste a un dios\", dice. \"¿Quién queda para gobernarte?\""),
    "codex.refuse": ("You turn the page. The last lock closes, and the light goes out.",
                     "Pasas la página. La última cerradura se cierra, y la luz se apaga."),
    "wake": ("You wake among the ruins of the Inverted Throne. Above you, through the broken city, the black aetherium of Aetheris begins to run clear.",
             "Despiertas entre las ruinas del Trono Invertido. Sobre ti, a través de la ciudad rota, el aetherio negro de Aetheris empieza a aclararse."),
    "title.fates": ("What became of Aetheris", "Lo que fue de Aetheris"),
    "title.bearer": ("The Bearer", "El Portador"),
    "unsettled": ("Nothing was settled here", "Aquí no se decidió nada"),
    # one slide per region and fate, and one for a region whose choice was never made
    "sulthari.bazaar": ("The Low Bazaar thrives. Its stalls stay open through the night, and the Observatory's dome is mended with the merchants' own coin, slowly, one tile at a time.",
                        "El Bazar Bajo prospera. Sus puestos abren toda la noche, y la cúpula del Observatorio se repara con las monedas de los mercaderes, despacio, teja a teja."),
    "sulthari.observatory": ("The Great Observatory is rebuilt first. Its lens turns to the sky again, while the Low Bazaar trades among ash and canvas for years.",
                             "El Gran Observatorio se reconstruye primero. Su lente vuelve a mirar al cielo, mientras el Bazar Bajo comercia entre ceniza y lona durante años."),
    "sulthari.unsettled": ("Sulthari rebuilds as it always has: the Council argues, the brass tramways run again, and no one agrees on what to mend first.",
                           "Sulthari se reconstruye como siempre: el Consejo discute, los tranvías de latón vuelven a correr, y nadie se pone de acuerdo en qué reparar primero."),
    "nordrath.peace": ("The warriors of the endless war lay down their axes at last. The clans meet at Skarnhold around one fire, and the cold forges burn for plows.",
                       "Los guerreros de la guerra sin fin por fin bajan sus hachas. Los clanes se reúnen en Skarnhold alrededor de un solo fuego, y las forjas frías arden para hacer arados."),
    "nordrath.war": ("The endless war goes on beneath the mountains. Its dead fight on, the clans sing of them, and send their sons to watch the passes.",
                     "La guerra sin fin continúa bajo las montañas. Sus muertos siguen luchando, los clanes les cantan, y envían a sus hijos a vigilar los pasos."),
    "nordrath.unsettled": ("Snow covers Vorath's citadel. The clans keep to their own holds, each waiting to see who moves first.",
                           "La nieve cubre la ciudadela de Vorath. Los clanes se quedan en sus fortalezas, cada uno esperando a ver quién se mueve primero."),
    "parsivan.awakened": ("The Dreaming Court wakes. It weeps for the years it lost, then opens the Gardens to anyone who will tend them.",
                          "La Corte Durmiente despierta. Llora los años que perdió, y luego abre los Jardines a cualquiera que quiera cuidarlos."),
    "parsivan.asleep": ("The Dreaming Court sleeps on, smiling. The Gardens bloom over it, and the travelers who pass lower their voices.",
                        "La Corte Durmiente sigue dormida, sonriendo. Los Jardines florecen sobre ella, y los viajeros que pasan bajan la voz."),
    "parsivan.unsettled": ("The Gardens grow wild. Some of the court wakes and some does not, and no one is sure which of them is dreaming.",
                           "Los Jardines crecen salvajes. Parte de la corte despierta y parte no, y nadie está seguro de quién sueña."),
    "khemet.rest": ("The souls of Khemet are guided below at last. The Catacombs fall silent, and the living plant reeds where the marsh was.",
                    "Las almas de Khemet por fin son guiadas al inframundo. Las Catacumbas callan, y los vivos plantan juncos donde estaba la ciénaga."),
    "khemet.guardians": ("The souls of Khemet stand guard over the living. No raider crosses the sands, and no child of Khemet walks alone at night.",
                         "Las almas de Khemet montan guardia sobre los vivos. Ningún saqueador cruza las arenas, y ningún niño de Khemet camina solo de noche."),
    "khemet.unsettled": ("The marsh dries slowly. In the Catacombs the souls still wander, waiting for someone to tell them where to go.",
                         "La ciénaga se seca despacio. En las Catacumbas las almas siguen vagando, esperando a que alguien les diga adónde ir."),
    "aureum.law": ("The courts of Aureum sit again. The Law is read aloud in the forum, and for the first time in an age it is read to everyone.",
                   "Los tribunales de Aureum vuelven a reunirse. La Ley se lee en voz alta en el foro, y por primera vez en una era se lee para todos."),
    "aureum.shared": ("The gold of the courts is shared out in the streets. Aureum is poorer and louder, and its people eat.",
                      "El oro de los tribunales se reparte en las calles. Aureum es más pobre y más ruidosa, y su gente come."),
    "aureum.unsettled": ("The forum fills again with traders and orators. The courts stay shut, and the gold stays where Avarok left it.",
                         "El foro vuelve a llenarse de mercaderes y oradores. Los tribunales siguen cerrados, y el oro sigue donde lo dejó Avarok."),
    # the Bearers' epilogues (README, 2.7): full, and the sadder one for unfinished Bearer quests
    "bearer.cassian.full": ("Cassian refounds the Order of the Scale in Aureum. Its doors are open now to anyone, from any empire, who swears to weigh before striking.",
                            "Cassian refunda la Orden de la Balanza en Aureum. Sus puertas están abiertas ahora a cualquiera, de cualquier imperio, que jure pesar antes de golpear."),
    "bearer.cassian.unfinished": ("Cassian returns to Aureum alone. The Order of the Scale stays a ruin, and every night he guards its gate, waiting for brothers who do not come.",
                                  "Cassian regresa solo a Aureum. La Orden de la Balanza sigue en ruinas, y cada noche él guarda su puerta, esperando a hermanos que no llegan."),
    "bearer.ankhareth.full": ("Ankhareth opens the gates of Khemet's underworld and walks with the souls on their last journey. His master walks beside him, and does not look back.",
                              "Ankhareth abre las puertas del inframundo de Khemet y camina con las almas en su último viaje. Su maestro camina a su lado, y no mira atrás."),
    "bearer.ankhareth.unfinished": ("Ankhareth opens the gates, but his master is not among the souls that pass. He stays at the threshold, calling a name no one answers.",
                                    "Ankhareth abre las puertas, pero su maestro no está entre las almas que pasan. Se queda en el umbral, llamando un nombre que nadie responde."),
    "bearer.shirin.full": ("Shirin frees Laleh, who dies in peace in her arms. She writes a new map of the sky, and names a star for her sister.",
                           "Shirin libera a Laleh, que muere en paz en sus brazos. Escribe un nuevo mapa del cielo, y le pone a una estrella el nombre de su hermana."),
    "bearer.shirin.unfinished": ("Laleh never finds peace. Her voice still drifts through the Gardens, and on Shirin's new map of the sky there is one star she cannot bring herself to name.",
                                 "Laleh nunca encuentra la paz. Su voz aún flota por los Jardines, y en el nuevo mapa del cielo de Shirin hay una estrella a la que no se atreve a poner nombre."),
    "bearer.rurik.full": ("Rurik returns the gold of the Vaults to the Nordrath clans and rebuilds his village with his own hands. He keeps a single coin.",
                          "Rurik devuelve el oro de las Cámaras a los clanes de Nordrath y reconstruye su aldea con sus propias manos. Se queda una sola moneda."),
    "bearer.rurik.unfinished": ("Rurik keeps the gold. His village stays ash, and every night he counts his coins in an empty hall by the light of one candle.",
                                "Rurik se queda el oro. Su aldea sigue en cenizas, y cada noche cuenta sus monedas en una sala vacía a la luz de una vela."),
    "bearer.azhar.full": ("Azhar refuses to be emperor of Aetheris and calls a new Pact between equals. Beneath his own seat, sealed, the Codex sleeps.",
                          "Azhar se niega a ser emperador de Aetheris y convoca un nuevo Pacto entre iguales. Bajo su propio asiento, sellado, duerme el Códice."),
    "bearer.azhar.unfinished": ("Azhar refuses the throne, but no one answers his call for a new Pact. He keeps the sealed Codex beneath his seat, and watches it alone.",
                                "Azhar rechaza el trono, pero nadie responde a su llamada a un nuevo Pacto. Guarda el Códice sellado bajo su asiento, y lo vigila solo."),
    # the sequel hook, and the credits
    "eighth.1": ("The seal has eight locks, not seven.", "El sello tiene ocho cerraduras, no siete."),
    "eighth.2": ("On the last page of the Codex one lock is still dark. No one knows which sin it holds.",
                 "En la última página del Códice una cerradura sigue a oscuras. Nadie sabe qué pecado guarda."),
    "credits.title": ("Sins of the Fallen Empires", "Sins of the Fallen Empires"),
    "credits.by": ("A mod by %s", "Un mod de %s"),
    "credits.thanks": ("Thank you for playing.", "Gracias por jugar."),
    "skip": ("Space: next   Esc: skip", "Espacio: siguiente   Esc: saltar"),
}
POSTGAME_TEXT = {
    "message.sofe.echo_kneel": ("Keep kneeling to call the Echo of %s", "Sigue arrodillado para llamar al Eco de %s"),
    "message.sofe.echo_rises": ("The Echo of %s rises", "El Eco de %s se alza"),
    "advancement.sofe.the_seal_rewritten": ("The Seal Rewritten", "El Sello Reescrito"),
    "advancement.sofe.the_seal_rewritten.desc": ("Close the Codex on the First Fallen and see what became of Aetheris",
                                                 "Cierra el Códice sobre el Primer Caído y mira lo que fue de Aetheris"),
}
SEAL_ADVANCEMENT = {
    "display": {"icon": {"item": "sofe:sealing_quill"}, "title": {"translate": "advancement.sofe.the_seal_rewritten"},
                "description": {"translate": "advancement.sofe.the_seal_rewritten.desc"}, "frame": "challenge",
                "show_toast": True, "announce_to_chat": True, "hidden": False},
    "criteria": {"done": {"trigger": "minecraft:impossible"}},
    "parent": "sofe:story/boss_nahrazel",
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
    for k, v in ENDING.items():
        lang["ending.sofe." + k] = v
    lang.update(POSTGAME_TEXT)
    with open(os.path.join(DATA, "advancements", "story", "the_seal_rewritten.json"), "w", encoding="utf-8") as f:
        f.write(json.dumps(SEAL_ADVANCEMENT, indent=2) + "\n")
    old = os.path.join(DATA, "dialogue", "act5", "the_ending.json")
    if os.path.exists(old):
        os.remove(old)  # the ending is a scene now (EndingScreen)
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
