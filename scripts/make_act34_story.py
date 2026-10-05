"""The main quests of Acts III and IV and their scenes (README, "The Story in Five Acts"): the eastern shadows
(Parsivan, then Khemet, and the diary of the sultan who sealed the crack: the Archsins need a Bearer to hand over
the fragments willingly) and the western ruins (Aureum and the Caverns under Nordrath, ending with the news that
Sulthari is under siege). The end of each act starts the next; English and Spanish.

Run from the repository root: python scripts/make_act34_story.py
"""
import json
import os

DATA = os.path.join("src", "main", "resources", "data", "sofe")
LANG = os.path.join("src", "main", "resources", "assets", "sofe", "lang")
LAIRS = json.load(open(os.path.join(DATA, "boss_lairs.json"), encoding="utf-8"))["lairs"]


def target(boss):
    l = LAIRS["sofe:" + boss]
    return {"x": l["x"], "z": l["z"]}


def boss_step(boss, start_dialogue=None):
    step = {"objective": {"type": "defeat_boss", "boss": "sofe:" + boss}, "target": target(boss)}
    if start_dialogue:
        step["on_start"] = [{"type": "open_dialogue", "dialogue": start_dialogue}]
    return step


QUESTS = {
    "act3_east": {
        "type": "main", "act": 3,
        "steps": [
            {"objective": {"type": "reach_region", "region": "parsivan"}, "target": {"x": 1600, "z": 0},
             "on_start": [{"type": "open_dialogue", "dialogue": "sofe:act3/the_eastern_shadows"}]},
            boss_step("mirael"), boss_step("thessyn"), boss_step("luxara", "sofe:act3/the_gardens"),
            {"objective": {"type": "reach_region", "region": "khemet"}, "target": {"x": 2000, "z": 1700},
             "on_start": [{"type": "open_dialogue", "dialogue": "sofe:act3/laleh_at_rest"}]},
            boss_step("dormiel"), boss_step("morthis", "sofe:act3/the_marsh"),
        ],
        "rewards": [{"type": "give_xp", "amount": 6000}, {"type": "open_dialogue", "dialogue": "sofe:act3/the_sultans_diary"},
                    {"type": "advance_act", "act": 4}],
    },
    "act4_west": {
        "type": "main", "act": 4,
        "steps": [
            {"objective": {"type": "reach_region", "region": "aureum"}, "target": {"x": -1600, "z": 0},
             "on_start": [{"type": "open_dialogue", "dialogue": "sofe:act4/the_western_ruins"}]},
            boss_step("goldarc"), boss_step("nixara"), boss_step("avarok"),
            boss_step("fenrath", "sofe:act4/below_the_north"), boss_step("gularth"),
            boss_step("shadeyn"), boss_step("envyris", "sofe:act4/the_shadow_throne"),
        ],
        "rewards": [{"type": "give_xp", "amount": 12000}, {"type": "open_dialogue", "dialogue": "sofe:act4/sulthari_besieged"},
                    {"type": "advance_act", "act": 5}],
    },
}

# dialogue id: (style, [(speaker, en, es)], on_end effects)
DIALOGUES = {
    "act3/the_eastern_shadows": ("parsivan", [
        ("narrator", "East of Sulthari the roads grow soft with petals. Parsivan was the court of poets; now it is a paradise where no one wants to wake.",
         "Al este de Sulthari los caminos se alfombran de pétalos. Parsivan era la corte de los poetas; hoy es un paraíso del que nadie quiere despertar."),
        ("ozhan", "Two Broken Oaths guard Luxara: Mirael in the old baths and Thessyn on the Silk Road. Break them, and her gardens will open to you.",
         "Dos Juramentos Rotos guardan a Luxara: Mirael en los antiguos baños y Thessyn en la Ruta de la Seda. Quiébralos, y sus jardines se abrirán ante ti."),
        ("ozhan", "Beyond Parsivan lies Khemet, where the dead stopped departing. Its catacombs stay sealed until Luxara falls.",
         "Más allá de Parsivan está Khemet, donde los muertos dejaron de partir. Sus catacumbas siguen selladas hasta que caiga Luxara.")], None),
    "act3/the_gardens": ("parsivan", [
        ("narrator", "The gates of the Enchanted Gardens open without a sound. The air smells of rosewater, and somewhere a woman is singing.",
         "Las puertas de los Jardines Encantados se abren sin un sonido. El aire huele a agua de rosas, y en algún lugar canta una mujer."),
        ("narrator", "Every path leads to the fountain at the heart. Every path wants you to stay.",
         "Todos los senderos llevan a la fuente del centro. Todos los senderos quieren que te quedes.")], None),
    "act3/laleh_at_rest": ("parsivan", [
        ("narrator", "When Luxara falls, the singing stops. For a moment the gardens are only gardens: tired roses, cracked tiles, a fountain that needs cleaning.",
         "Cuando Luxara cae, el canto se detiene. Por un momento los jardines son solo jardines: rosas cansadas, azulejos rotos, una fuente que pide limpieza."),
        ("narrator", "Her Codex Shard is warm. Too warm. Far to the south, the seal on the Catacombs of Khemet breaks.",
         "Su fragmento del Códice está tibio. Demasiado. Muy al sur, el sello de las Catacumbas de Khemet se rompe.")], None),
    "act3/the_marsh": ("khemet", [
        ("narrator", "The river of Khemet does not flow here. It lies still under a green skin, and the pyramid in the middle of it is sinking, slowly, as if it had all the time in the world.",
         "El río de Khemet no corre aquí. Yace inmóvil bajo una piel verde, y la pirámide en su centro se hunde, despacio, como si tuviera todo el tiempo del mundo."),
        ("narrator", "On its terrace something enormous sits on a throne. It has not stood up in a thousand years. It does not intend to start now.",
         "En su terraza algo enorme se sienta en un trono. No se ha puesto de pie en mil años. No piensa empezar ahora.")], None),
    "act3/the_sultans_diary": ("khemet", [
        ("narrator", "Among the reeds of Morthis's terrace lies a satchel sealed with the crescent of Sulthari. Inside, a diary in the hand of the last sultan, the one who closed the crack.",
         "Entre los juncos de la terraza de Morthis hay un morral sellado con la media luna de Sulthari. Dentro, un diario de puño y letra del último sultán, el que cerró la grieta."),
        ("narrator", "\"They cannot take the fragments. They tried, and the Codex burned them. A fragment must be given, willingly, by the hand that carries it.\"",
         "\"No pueden tomar los fragmentos. Lo intentaron, y el Códice los quemó. Un fragmento debe ser entregado, por voluntad propia, por la mano que lo lleva.\""),
        ("narrator", "\"That is why they tempt. That is why they will always tempt. Whoever finds this: do not give them what they ask for. Not even once.\"",
         "\"Por eso tientan. Por eso siempre tentarán. Quien encuentre esto: no les des lo que piden. Ni una sola vez.\""),
        ("ozhan", "A diary? Bring it to me, Bearer, the Council must study it. And make haste west: Aureum and its vaults await you.",
         "¿Un diario? Tráemelo, Portador, el Consejo debe estudiarlo. Y date prisa hacia el oeste: Aureum y sus bóvedas te esperan.")],
        [{"type": "start_quest", "quest": "sofe:act4_west"}]),
    "act4/the_western_ruins": ("aureum", [
        ("narrator", "Aureum was a republic of marble and law. Now its forum is a market of souls, and in its colosseum the dead fight for coins they cannot spend.",
         "Aureum fue una república de mármol y ley. Hoy su foro es un mercado de almas, y en su coliseo los muertos luchan por monedas que no pueden gastar."),
        ("ozhan", "Avarok sits on the Golden Vaults, behind Goldarc in the Treasury and Nixara in the Market. Envyris rules the ruined senate, the Shadow Throne.",
         "Avarok se sienta sobre las Bóvedas Doradas, tras Goldarc en el Tesoro y Nixara en el Mercado. Envyris reina en el senado en ruinas, el Trono de las Sombras."),
        ("ozhan", "And under Nordrath, in the caverns, Gularth eats. The Caverns are open to you now.",
         "Y bajo Nordrath, en las cavernas, come Gularth. Las Cavernas ya están abiertas para ti.")], None),
    "act4/below_the_north": ("nordrath", [
        ("narrator", "The stair under Skarnhold goes down and down. The air turns warm and sour; somewhere below, something is chewing.",
         "La escalera bajo Skarnhold baja y baja. El aire se vuelve tibio y agrio; en algún lugar de abajo, algo mastica.")], None),
    "act4/the_shadow_throne": ("aureum", [
        ("narrator", "The senate of Aureum has no roof. Black mirrors stand where the senators' statues were, and in each of them you see yourself, a little better than you are.",
         "El senado de Aureum no tiene techo. Espejos negros se alzan donde estaban las estatuas de los senadores, y en cada uno te ves a ti mismo, un poco mejor de lo que eres.")], None),
    "act4/sulthari_besieged": ("void", [
        ("narrator", "Envyris's shard falls into your hand, and with it a silence so deep you can hear your own heart.",
         "El fragmento de Envyris cae en tu mano, y con él un silencio tan hondo que oyes tu propio corazón."),
        ("narrator", "Then a messenger hawk, its feathers singed, lands on your arm. The seal is the Council's. The hand is shaking.",
         "Entonces un halcón mensajero, de plumas chamuscadas, se posa en tu brazo. El sello es del Consejo. La letra tiembla."),
        ("council_elder", "\"Sulthari is under siege. A spire of light hangs over the Observatory and the walls are failing. Come home, Bearer. Come home now.\"",
         "\"Sulthari está sitiada. Una aguja de luz pende sobre el Observatorio y las murallas ceden. Vuelve a casa, Portador. Vuelve ahora.\"")], None),
}

QUEST_TEXT = {
    "act3_east": (("The Eastern Shadows", "Las Sombras del Este"), [
        ("Reach Parsivan", "Llega a Parsivan"), ("Defeat Mirael in the Parsivan Baths", "Derrota a Mirael en los Baños de Parsivan"),
        ("Defeat Thessyn on the Silk Road", "Derrota a Thessyn en la Ruta de la Seda"), ("Defeat Luxara in the Enchanted Gardens", "Derrota a Luxara en los Jardines Encantados"),
        ("Reach Khemet", "Llega a Khemet"), ("Defeat Dormiel in the Khemet Catacombs", "Derrota a Dormiel en las Catacumbas de Khemet"),
        ("Defeat Morthis in the Stagnant Marsh", "Derrota a Morthis en el Pantano Estancado")]),
    "act4_west": (("The Western Ruins", "Las Ruinas del Oeste"), [
        ("Reach Aureum", "Llega a Aureum"), ("Defeat Goldarc in the Treasury", "Derrota a Goldarc en el Tesoro"),
        ("Defeat Nixara in the Market", "Derrota a Nixara en el Mercado"), ("Defeat Avarok in the Golden Vaults", "Derrota a Avarok en las Bóvedas Doradas"),
        ("Defeat Fenrath in the Nordrath Caverns", "Derrota a Fenrath en las Cavernas de Nordrath"), ("Defeat Gularth in the Feast Halls", "Derrota a Gularth en los Salones del Festín"),
        ("Defeat Shadeyn in the Colosseum", "Derrota a Shadeyn en el Coliseo"), ("Defeat Envyris on the Shadow Throne", "Derrota a Envyris en el Trono de las Sombras")]),
}


def main():
    lang = {}
    for qid, quest in QUESTS.items():
        with open(os.path.join(DATA, "quests", qid + ".json"), "w", encoding="utf-8") as f:
            f.write(json.dumps(quest, indent=2) + "\n")
        title, steps = QUEST_TEXT[qid]
        lang["quest.sofe." + qid] = title
        assert len(steps) == len(quest["steps"]), qid
        for i, step in enumerate(steps, 1):
            lang["quest.sofe.%s.step%d" % (qid, i)] = step
    for did, (style, lines, on_end) in DIALOGUES.items():
        out = []
        for i, (speaker, en, es) in enumerate(lines, 1):
            k = "dialogue.sofe.%s.%d" % (did.replace("/", "."), i)
            out.append({"speaker": speaker, "text": k})
            lang[k] = (en, es)
        d = {"style": style, "cinematic": True, "lines": out}
        if on_end:
            d["on_end"] = on_end
        os.makedirs(os.path.join(DATA, "dialogue", os.path.dirname(did)), exist_ok=True)
        with open(os.path.join(DATA, "dialogue", did + ".json"), "w", encoding="utf-8") as f:
            f.write(json.dumps(d, ensure_ascii=False, indent=2) + "\n")
    # the end of Act II starts Act III
    path = os.path.join(DATA, "dialogue", "act2", "thank_you.json")
    d = json.load(open(path, encoding="utf-8"))
    d["on_end"] = [{"type": "start_quest", "quest": "sofe:act3_east"}]
    with open(path, "w", encoding="utf-8") as f:
        f.write(json.dumps(d, indent=2) + "\n")
    for f, i in (("en_us", 0), ("es_es", 1)):
        p = os.path.join(LANG, f + ".json")
        data = json.load(open(p, encoding="utf-8"))
        for k, v in lang.items():
            data[k] = v[i]
        open(p, "w", encoding="utf-8").write(json.dumps(data, ensure_ascii=False, indent=2) + "\n")
    print("quests: %d, dialogues: %d" % (len(QUESTS), len(DIALOGUES)))


if __name__ == "__main__":
    main()
