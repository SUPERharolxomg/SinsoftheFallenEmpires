"""The temptations of the Archsins of Acts III and IV (README, "every hero carries the temptation of one sin"): the
cinematic each Bearer sees before the fight, with a line of its own for the Bearer whose weakness that sin is
(Shirin and Lust, Ankhareth and Sloth, Rurik and Greed), in English and Spanish, and the bosses' portraits, cut
from the front of their models' heads.

Run from the repository root: python scripts/make_temptations.py
"""
import json
import os

from PIL import Image, ImageDraw

DIALOGUE = os.path.join("src", "main", "resources", "data", "sofe", "dialogue")
LANG = os.path.join("src", "main", "resources", "assets", "sofe", "lang")
TEX = os.path.join("src", "main", "resources", "assets", "sofe", "textures", "entity")
GEO = os.path.join("src", "main", "resources", "assets", "sofe", "geo", "entity")
PORTRAIT = os.path.join("src", "main", "resources", "assets", "sofe", "textures", "gui", "portrait", "placeholder")

# id, act folder, style, the Bearer whose sin it is (or None), lines {key: (en, es)}
TEMPTATIONS = [
    ("luxara", "act3", "parsivan", "sorceress", {
        "1": ("You came so far, Bearer, and still you ache. I know that ache. In my gardens no one is ever alone again.",
              "Llegaste tan lejos, Portador, y aún te duele. Conozco ese dolor. En mis jardines nadie vuelve a estar solo."),
        "own": ("Shirin. Laleh is here. Listen: she is humming the song from the observatory roof. Stay, and you will never lose her a second time.",
                "Shirin. Laleh está aquí. Escucha: tararea la canción de la azotea del observatorio. Quédate, y nunca la perderás por segunda vez."),
        "other": ("Whatever you lost, it waits among the flowers. Every face you miss, every hand you let go.",
                  "Lo que perdiste te espera entre las flores. Cada rostro que extrañas, cada mano que soltaste."),
        "2": ("Give me the shard, and the dream is yours forever.", "Dame el fragmento, y el sueño será tuyo para siempre."),
        "refuse": ("No. A dream you cannot wake from is a cage.", "No. Un sueño del que no se despierta es una jaula."),
        "silent": ("(Say nothing, and draw your weapon.)", "(No dices nada y desenvainas tu arma.)")}),
    ("morthis", "act3", "khemet", "necromancer", {
        "1": ("Shhh. Why do you hurry? The river does not hurry. The dead do not hurry. Only the living tire themselves, and for what?",
              "Shhh. ¿Por qué te apresuras? El río no se apresura. Los muertos no se apresuran. Solo los vivos se cansan, ¿y para qué?"),
        "own": ("Ankhareth, priest of thresholds. Your master sleeps here, beside the others. Lie down with them. No more pain, no more doors to open. Rest.",
                "Ankhareth, sacerdote de los umbrales. Tu maestro duerme aquí, junto a los demás. Acuéstate con ellos. Sin dolor, sin puertas que abrir. Descansa."),
        "other": ("Rest, Bearer. Set down the shard; it is so heavy. Let Khemet keep you, as it keeps everyone.",
                  "Descansa, Portador. Deja el fragmento; pesa tanto. Deja que Khemet te guarde, como guarda a todos."),
        "2": ("Close your eyes. Only for a moment.", "Cierra los ojos. Solo un momento."),
        "refuse": ("I will rest when the dead can.", "Descansaré cuando los muertos puedan."),
        "silent": ("(Say nothing, and draw your weapon.)", "(No dices nada y desenvainas tu arma.)")}),
    ("avarok", "act4", "aureum", "thief", {
        "1": ("Welcome to the Vaults. Every coin of Aureum came to me in the end. Every one. And you, Bearer, what do you carry? Show me.",
              "Bienvenido a las Bóvedas. Cada moneda de Aureum acabó llegando a mí. Cada una. ¿Y tú, Portador, qué llevas? Enséñamelo."),
        "own": ("Rurik Grimsson. Little thief of the north. More gold than all the clans of Nordrath have ever seen. Walk out with it. All of it. Only leave the others behind.",
                "Rurik Grimsson. Pequeño ladrón del norte. Más oro del que todos los clanes de Nordrath han visto jamás. Llévatelo. Todo. Solo deja atrás a los demás."),
        "other": ("Name your price. Everyone has one. Hand me the shard and I will fill your arms with gold until you cannot lift them.",
                  "Pon tu precio. Todos tienen uno. Dame el fragmento y te llenaré los brazos de oro hasta que no puedas alzarlos."),
        "2": ("Well? A merchant's offer does not wait forever.", "¿Y bien? La oferta de un mercader no espera para siempre."),
        "refuse": ("Keep your gold. It has already bought too much.", "Quédate tu oro. Ya ha comprado demasiado."),
        "silent": ("(Say nothing, and draw your weapon.)", "(No dices nada y desenvainas tu arma.)")}),
    ("gularth", "act4", "nordrath", None, {
        "1": ("Hungry. Always hungry. The clans shared their winter bread once, and I ate it. And the clans. And the winter.",
              "Hambre. Siempre hambre. Los clanes compartían su pan de invierno, y me lo comí. Y a los clanes. Y al invierno."),
        "other": ("You smell of long roads and empty bellies. Sit. Eat. There is always more at my table.",
                  "Hueles a caminos largos y estómagos vacíos. Siéntate. Come. En mi mesa siempre hay más."),
        "2": ("Feed me the shard, Bearer, and you will never know hunger again.", "Dame de comer el fragmento, Portador, y nunca volverás a conocer el hambre."),
        "refuse": ("Your feast is a grave with a tablecloth.", "Tu banquete es una tumba con mantel."),
        "silent": ("(Say nothing, and draw your weapon.)", "(No dices nada y desenvainas tu arma.)")}),
    ("envyris", "act4", "aureum", None, {
        "1": ("Look at you. Chosen by the Codex. Everyone in Sulthari speaks your name.",
              "Mírate. Elegido por el Códice. Todo Sulthari pronuncia tu nombre."),
        "other": ("I want it. Your name, your face, your story. I will wear them better than you ever did.",
                  "Lo quiero. Tu nombre, tu rostro, tu historia. Los llevaré mejor de lo que tú los llevaste nunca."),
        "2": ("Give me the shard, and I will give you back a life no one notices. Isn't that a kind of peace?",
              "Dame el fragmento y te devolveré una vida que nadie note. ¿No es eso una especie de paz?"),
        "refuse": ("You can copy my steps, never the reason I walk.", "Puedes copiar mis pasos, nunca la razón por la que camino."),
        "silent": ("(Say nothing, and draw your weapon.)", "(No dices nada y desenvainas tu arma.)")}),
]

BACKDROPS = {"parsivan": ((40, 16, 50), (170, 90, 200)), "khemet": ((40, 34, 16), (120, 200, 110)),
             "aureum": ((50, 36, 10), (230, 180, 60)), "nordrath": ((40, 10, 10), (200, 70, 40))}


def key(id, part):
    return "dialogue.sofe.%s.%s" % (id, part)


def class_is(cls):
    return {"type": "class_is", "class": cls}


def dialogue(id, style, own, lines):
    out = [{"speaker": id, "text": key(id, "1")}]
    if own:
        out.append({"speaker": id, "text": key(id, "own"), "condition": class_is(own)})
        out.append({"speaker": id, "text": key(id, "other"), "condition": {"type": "not", "condition": class_is(own)}})
    else:
        out.append({"speaker": id, "text": key(id, "other")})
    out.append({"speaker": id, "text": key(id, "2"), "answers": [{"text": key(id, "refuse"), "next": -1}, {"text": key(id, "silent"), "next": -1}]})
    return {"style": style, "cinematic": True, "lines": out}


def boss_face(id):
    """The boss's bust drawn from its model (model_portrait.py): its face in 3D, brow, eyes, horns and crown."""
    from model_portrait import render
    return render(id)


def portrait(id, style):
    size = 64
    dark, light = BACKDROPS[style]
    out = Image.new("RGBA", (size, size), dark + (255,))
    d = ImageDraw.Draw(out)
    for y in range(size):
        t = y / size
        d.line([(0, y), (size, y)], fill=tuple(int(light[i] * (1 - t) * 0.35 + dark[i] * (0.65 + t * 0.35)) for i in range(3)) + (255,))
    face = boss_face(id)
    scale = 40 / max(face.width, face.height)
    face = face.resize((max(1, round(face.width * scale)), max(1, round(face.height * scale))), Image.NEAREST)
    out.alpha_composite(face, ((size - face.width) // 2, 10))
    d.rectangle([0, 0, size - 1, size - 1], outline=(20, 10, 10, 255), width=2)
    d.rectangle([2, 2, size - 3, size - 3], outline=light + (255,), width=1)
    out.save(os.path.join(PORTRAIT, id + ".png"))


if __name__ == "__main__":
    lang = {}
    for id, act, style, own, lines in TEMPTATIONS:
        folder = os.path.join(DIALOGUE, act)
        os.makedirs(folder, exist_ok=True)
        with open(os.path.join(folder, id + "_temptation.json"), "w", encoding="utf-8") as f:
            f.write(json.dumps(dialogue(id, style, own, lines), indent=2) + "\n")
        for part, text in lines.items():
            lang[key(id, part)] = text
        lang["npc.sofe." + id] = (id.capitalize(), id.capitalize())  # the speaker's name in the dialogue box
        portrait(id, style)
    # every boss of Acts III and IV gets a portrait, for the dialogues and quests to come
    for id, style in (("mirael", "parsivan"), ("thessyn", "parsivan"), ("dormiel", "khemet"), ("goldarc", "aureum"),
                      ("nixara", "aureum"), ("fenrath", "nordrath"), ("shadeyn", "aureum")):
        portrait(id, style)
    for f, i in (("en_us", 0), ("es_es", 1)):
        path = os.path.join(LANG, f + ".json")
        d = json.load(open(path, encoding="utf-8"))
        for k, v in lang.items():
            d[k] = v[i]
        open(path, "w", encoding="utf-8").write(json.dumps(d, ensure_ascii=False, indent=2) + "\n")
    print("temptations: %d" % len(TEMPTATIONS))
