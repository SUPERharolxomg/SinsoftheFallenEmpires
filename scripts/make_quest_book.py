"""The FTB Quests book of the modpack, made from the mod's own quests, so a Bearer who joins for the first time sees at
once what to do and in what order (the story is linear).

The mod knows nothing of FTB Quests. It grants a hidden advancement for every quest step done
(sofe:quest/<quest>/step<n>, no toast, no chat, not in the advancement screen: QuestMilestones), and each FTB quest of
the book is ticked by its advancement. Without FTB Quests in the pack nothing changes.

Writes:
  src/main/resources/data/sofe/advancements/quest/<quest>/step<n>.json   one per step of every quest
  pack/config/ftbquests/quests/                                          the book: data, chapter groups, chapters
Every text of the book is a lang key ({ftbquests.sofe...} or the quest's own), so it reads in English and Spanish.

Run from the repository root: python scripts/make_quest_book.py
"""
import glob
import hashlib
import json
import os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DATA = os.path.join(ROOT, "src", "main", "resources", "data", "sofe")
BOOK = os.path.join(ROOT, "pack", "config", "ftbquests", "quests")
LANG = os.path.join(ROOT, "src", "main", "resources", "assets", "sofe", "lang")


def oid(name):
    """A stable FTB Quests id (16 hex digits) for a name, so the book keeps its ids each time it is made. FTB Quests reads
    ids as signed longs: one from 8000000000000000 up is not an id to it, and it gives the object a new one (which
    breaks every dependency on it), so the top bit is cleared."""
    value = int(hashlib.sha1(("sofe:" + name).encode()).hexdigest()[:16], 16) & 0x7FFFFFFFFFFFFFFF
    return "%016X" % (value or 1)


def quests():
    out = {}
    for f in sorted(glob.glob(os.path.join(DATA, "quests", "**", "*.json"), recursive=True)):
        path = os.path.relpath(f, os.path.join(DATA, "quests")).replace(os.sep, "/")[:-5]
        with open(f, encoding="utf-8") as fh:
            out["sofe:" + path] = json.load(fh)
    return out


def key(quest_id):
    return "quest.sofe." + quest_id.split(":", 1)[1].replace("/", ".")


def advancements(all_quests):
    root = os.path.join(DATA, "advancements", "quest")
    for quest_id, q in all_quests.items():
        folder = os.path.join(root, *quest_id.split(":", 1)[1].split("/"))
        os.makedirs(folder, exist_ok=True)
        for n in range(1, len(q["steps"]) + 1):
            with open(os.path.join(folder, "step%d.json" % n), "w", encoding="utf-8", newline="\n") as f:
                json.dump({"criteria": {"done": {"trigger": "minecraft:impossible"}}}, f, indent=2)
                f.write("\n")


# ------------------------------------------------------------------------------------------------ SNBT
def snbt(value, indent=0):
    pad = "\t" * indent
    if isinstance(value, bool):
        return "true" if value else "false"
    if isinstance(value, float):
        return "%sd" % repr(value)
    if isinstance(value, int):
        return str(value)
    if isinstance(value, str):
        return json.dumps(value, ensure_ascii=False)
    if isinstance(value, list):
        if not value:
            return "[ ]"
        return "[\n" + "\n".join(pad + "\t" + snbt(v, indent + 1) for v in value) + "\n" + pad + "]"
    if isinstance(value, dict):
        return "{\n" + "\n".join("%s\t%s: %s" % (pad, k, snbt(value[k], indent + 1)) for k in sorted(value)) + "\n" + pad + "}"
    raise TypeError(value)


def write(path, value):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        f.write(snbt(value) + "\n")


def quest(name, title, x, y, tasks, description=(), subtitle=None, deps=(), shape=None, icon=None, size=None):
    q = {"id": oid(name), "title": title, "x": float(x), "y": float(y), "tasks": tasks,
         "description": list(description), "dependencies": [oid(d) for d in deps]}
    if subtitle:
        q["subtitle"] = subtitle
    if shape:
        q["shape"] = shape
    if icon:
        q["icon"] = icon
    if size:
        q["size"] = float(size)
    return q


def check(name):
    return [{"id": oid(name + "/task"), "type": "checkmark"}]


def by_advancement(name, advancement):
    return [{"id": oid(name + "/task"), "type": "advancement", "advancement": advancement, "criterion": ""}]


def chapter(filename, order, title, icon, quests_):
    return {"id": oid("chapter/" + filename), "filename": filename, "group": "", "order_index": order, "title": title,
            "icon": icon, "default_quest_shape": "", "default_hide_dependency_lines": False, "quest_links": [], "quests": quests_}


# ------------------------------------------------------------------------------------------------ the book
FIRST_STEPS = [  # name, icon, how many lines of description
    ("welcome", "minecraft:book", 3),
    ("compass", "minecraft:compass", 3),
    ("keys", "minecraft:oak_sign", 5),
    ("flask", "sofe:bearers_flask", 3),
    ("death", "minecraft:skeleton_skull", 2),
    ("travel", "minecraft:lodestone", 2),
    ("stuck", "minecraft:ender_pearl", 2),
    ("runes", "sofe:rune_stone", 3),
]

ACTS = [("act1_eclipse", "minecraft:clock"), ("act2_north", "minecraft:snowball"), ("act3_east", "minecraft:amethyst_shard"),
        ("act4_west", "minecraft:gold_ingot"), ("act5_ascension", "minecraft:nether_star")]

LANG_EN = {
    "ftbquests.sofe.title": "The Bearer's Path",
    "ftbquests.sofe.chapter.first_steps": "First steps",
    "ftbquests.sofe.chapter.side": "Side quests",
    "ftbquests.sofe.first.welcome": "Welcome, Bearer",
    "ftbquests.sofe.first.welcome.1": "Choose your class when you join: your Bearer, their story and their skills.",
    "ftbquests.sofe.first.welcome.2": "The story is linear: one step at a time, act after act. This book shows the steps in order.",
    "ftbquests.sofe.first.welcome.3": "Each step ticks itself here when you do it in the game.",
    "ftbquests.sofe.first.compass": "Follow the gold",
    "ftbquests.sofe.first.compass.1": "The compass at the top of the screen points to your next step and says how far it is.",
    "ftbquests.sofe.first.compass.2": "Golden motes on the ground lead the way; a column of light rises over the place.",
    "ftbquests.sofe.first.compass.3": "A crown of light shines over the person you must talk to. Right-click them.",
    "ftbquests.sofe.first.keys": "Your keys",
    "ftbquests.sofe.first.keys.1": "K: the skill tree. Learn your first three skills there.",
    "ftbquests.sofe.first.keys.2": "1 to 5: your skills; 6: your ultimate.",
    "ftbquests.sofe.first.keys.3": "U: the Journal, with every quest and the one the compass follows.",
    "ftbquests.sofe.first.keys.4": "I: your character sheet. H: drink from the Bearer's Flask.",
    "ftbquests.sofe.first.keys.5": "Every key can be changed in Options > Controls.",
    "ftbquests.sofe.first.flask": "The Bearer's Flask",
    "ftbquests.sofe.first.flask.1": "H (or right-click it) heals 40% of your life and of your resource (mana, rage...) at once.",
    "ftbquests.sofe.first.flask.2": "3 charges, one more for every Archsin you defeat (up to 10). At full strength it is not drunk, so no charge is wasted.",
    "ftbquests.sofe.first.flask.3": "It refills when you come back to Sulthari or when an elite enemy falls, and you keep it when you die.",
    "ftbquests.sofe.first.death": "If you fall",
    "ftbquests.sofe.first.death.1": "Your body stays where you fell with your gear, and only you can take it back.",
    "ftbquests.sofe.first.death.2": "The compass points to your body first, until you recover it.",
    "ftbquests.sofe.first.travel": "Waystones",
    "ftbquests.sofe.first.travel.1": "Touch every Waystone you find: you can travel between the ones you know.",
    "ftbquests.sofe.first.travel.2": "The sealed regions open act by act, as the story goes on.",
    "ftbquests.sofe.first.stuck": "Stuck?",
    "ftbquests.sofe.first.stuck.1": "In a hole or on a roof with no way down? Type /sofe unstuck in the chat.",
    "ftbquests.sofe.first.stuck.2": "It takes you back to the plaza of Sulthari, once every five minutes.",
    "ftbquests.sofe.first.runes": "Rune puzzles",
    "ftbquests.sofe.first.runes.1": "Every boss's dungeon is sealed by runes. Beside its Waystone stand Rune Stones and a Riddle Tablet.",
    "ftbquests.sofe.first.runes.2": "Right-click the tablet to read the riddle; right-click the stones to press them, in its order, or until all burn.",
    "ftbquests.sofe.first.runes.3": "Each Rune Stone says its rune when pressed. Solved once, the seal stays open for you; a Pact solves it together.",
    "ftbquests.sofe.chapter.dungeons": "Dungeons",
    "ftbquests.sofe.dungeon.crypt": "Small dungeon: one hall of the dead, a chest at the back.",
    "ftbquests.sofe.dungeon.ruin": "Medium dungeon: wake the runes of its court to open its seal; its guardian wakes with them. Two chests behind the seal.",
    "ftbquests.sofe.dungeon.large": "Large dungeon: a boss of the story waits inside.",
    "ftbquests.sofe.dungeon.runes": "Wake the runes beside its Waystone to open its seal.",
    "ftbquests.sofe.dungeon.act1": "Part of Act I: the Council sends you there.",
    "ftbquests.sofe.step_hint": "The golden compass leads you there.",
    "ftbquests.sofe.side.hint": "Opens with its act. Talk to the townsfolk to find it.",
}
LANG_ES = {
    "ftbquests.sofe.title": "El camino del Portador",
    "ftbquests.sofe.chapter.first_steps": "Primeros pasos",
    "ftbquests.sofe.chapter.side": "Misiones secundarias",
    "ftbquests.sofe.first.welcome": "Bienvenido, Portador",
    "ftbquests.sofe.first.welcome.1": "Al entrar elige tu clase: tu Portador, su historia y sus habilidades.",
    "ftbquests.sofe.first.welcome.2": "La historia es lineal: un paso tras otro, acto tras acto. Este libro te muestra los pasos en orden.",
    "ftbquests.sofe.first.welcome.3": "Cada paso se marca solo aquí cuando lo cumples en el juego.",
    "ftbquests.sofe.first.compass": "Sigue el oro",
    "ftbquests.sofe.first.compass.1": "La brújula de arriba de la pantalla apunta a tu siguiente paso y dice a cuántos metros está.",
    "ftbquests.sofe.first.compass.2": "Destellos dorados en el suelo marcan el camino; una columna de luz se alza sobre el lugar.",
    "ftbquests.sofe.first.compass.3": "Una corona de luz brilla sobre la persona con quien debes hablar. Haz clic derecho sobre ella.",
    "ftbquests.sofe.first.keys": "Tus teclas",
    "ftbquests.sofe.first.keys.1": "K: el árbol de habilidades. Aprende ahí tus tres primeras habilidades.",
    "ftbquests.sofe.first.keys.2": "1 a 5: tus habilidades; 6: tu definitiva.",
    "ftbquests.sofe.first.keys.3": "U: el Diario, con todas las misiones y la que sigue la brújula.",
    "ftbquests.sofe.first.keys.4": "I: tu hoja de personaje. H: bebe del Frasco del Portador.",
    "ftbquests.sofe.first.keys.5": "Todas las teclas se cambian en Opciones > Controles.",
    "ftbquests.sofe.first.flask": "El Frasco del Portador",
    "ftbquests.sofe.first.flask.1": "H (o clic derecho con él) cura el 40% de tu vida y de tu recurso (maná, furia...) a la vez.",
    "ftbquests.sofe.first.flask.2": "3 cargas, una más por cada Archipecado que derrotes (hasta 10). Con la vida y el recurso llenos no se bebe, así no gastas cargas.",
    "ftbquests.sofe.first.flask.3": "Se rellena al volver a Sulthari o al caer un enemigo élite, y lo conservas al morir.",
    "ftbquests.sofe.first.death": "Si caes",
    "ftbquests.sofe.first.death.1": "Tu cuerpo se queda donde caíste con tu equipo, y solo tú puedes recuperarlo.",
    "ftbquests.sofe.first.death.2": "La brújula apunta primero a tu cuerpo, hasta que lo recuperes.",
    "ftbquests.sofe.first.travel": "Piedras de paso",
    "ftbquests.sofe.first.travel.1": "Toca cada Piedra de paso que encuentres: podrás viajar entre las que conozcas.",
    "ftbquests.sofe.first.travel.2": "Las regiones selladas se abren acto a acto, según avanza la historia.",
    "ftbquests.sofe.first.stuck": "¿Atrapado?",
    "ftbquests.sofe.first.stuck.1": "¿En un hueco o en un techo sin salida? Escribe /sofe unstuck en el chat.",
    "ftbquests.sofe.first.stuck.2": "Te devuelve a la plaza de Sulthari, una vez cada cinco minutos.",
    "ftbquests.sofe.first.runes": "Acertijos de runas",
    "ftbquests.sofe.first.runes.1": "La mazmorra de cada jefe está sellada por runas. Junto a su Piedra de Paso hay Piedras Rúnicas y una Tablilla del Acertijo.",
    "ftbquests.sofe.first.runes.2": "Clic derecho en la tablilla para leer el acertijo; clic derecho en las piedras para pulsarlas, en su orden o hasta que ardan todas.",
    "ftbquests.sofe.first.runes.3": "Cada Piedra Rúnica dice su runa al pulsarla. Resuelto una vez, el sello queda abierto para ti; un Pacto lo resuelve junto.",
    "ftbquests.sofe.chapter.dungeons": "Mazmorras",
    "ftbquests.sofe.dungeon.crypt": "Mazmorra pequeña: una sala de muertos, con un cofre al fondo.",
    "ftbquests.sofe.dungeon.ruin": "Mazmorra mediana: despierta las runas de su patio para abrir su sello; su guardián despierta con ellas. Dos cofres tras el sello.",
    "ftbquests.sofe.dungeon.large": "Mazmorra grande: dentro espera un jefe de la historia.",
    "ftbquests.sofe.dungeon.runes": "Despierta las runas junto a su Piedra de Paso para abrir su sello.",
    "ftbquests.sofe.dungeon.act1": "Parte del Acto I: el Consejo te envía allí.",
    "ftbquests.sofe.step_hint": "La brújula dorada te lleva hasta allí.",
    "ftbquests.sofe.side.hint": "Se abre con su acto. Habla con la gente de las ciudades para encontrarla.",
}


def book(all_quests):
    chapters = []
    # first steps: read and tick, in a row
    first = []
    for i, (name, icon, lines) in enumerate(FIRST_STEPS):
        k = "ftbquests.sofe.first." + name
        first.append(quest("first/" + name, "{%s}" % k, i * 1.5, 0, check("first/" + name),
                           ["{%s.%d}" % (k, n) for n in range(1, lines + 1)], icon=icon,
                           deps=["first/" + FIRST_STEPS[i - 1][0]] if i else (), shape="rsquare"))
    chapters.append(chapter("first_steps", 0, "{ftbquests.sofe.chapter.first_steps}", "minecraft:book", first))
    # one chapter per act: its steps in a line, each ticked by its advancement
    previous = None
    for order, (act, icon) in enumerate(ACTS, start=1):
        quest_id = "sofe:" + act
        q = all_quests[quest_id]
        steps = []
        for n in range(1, len(q["steps"]) + 1):
            name = "%s/step%d" % (act, n)
            deps = ["%s/step%d" % (act, n - 1)] if n > 1 else ([previous] if previous else [])
            steps.append(quest(name, "{%s.step%d}" % (key(quest_id), n), (n - 1) * 1.5, 0,
                               by_advancement(name, "sofe:quest/%s/step%d" % (act, n)),
                               ["{ftbquests.sofe.step_hint}"], deps=deps,
                               shape="gear" if n == len(q["steps"]) else None, size=1.5 if n == len(q["steps"]) else None))
        previous = "%s/step%d" % (act, len(q["steps"]))
        chapters.append(chapter(act, order, "{%s}" % key(quest_id), icon, steps))
    # the side quests, a row per act
    side = []
    rows = {}
    for quest_id, q in sorted(all_quests.items()):
        if q["type"] != "side":
            continue
        col = rows.get(q["act"], 0)
        rows[q["act"]] = col + 1
        name = quest_id.split(":", 1)[1]
        last = len(q["steps"])
        side.append(quest(name, "{%s}" % key(quest_id), col * 1.5, (q["act"] - 1) * 1.5,
                          by_advancement(name, "sofe:quest/%s/step%d" % (name, last)),
                          ["{%s.step1}" % key(quest_id), "", "{ftbquests.sofe.side.hint}"]))
    chapters.append(chapter("side_quests", len(ACTS) + 2, "{ftbquests.sofe.chapter.side}", "minecraft:map", side))
    chapters.append(dungeons(all_quests))
    return chapters


REGIONS = [("sulthari", "act1_eclipse"), ("nordrath", None), ("parsivan", None), ("khemet", None), ("aureum", None)]


def dungeons(all_quests):
    """A row per region: its Crypt (small), its Ruin (medium) and its boss dungeons (large), each sealed by runes."""
    puzzles = {}
    for f in sorted(glob.glob(os.path.join(DATA, "puzzles", "*.json"))):
        with open(f, encoding="utf-8") as fh:
            puzzles[os.path.basename(f)[:-5]] = json.load(fh)
    out = []
    for row, (region, act1) in enumerate(REGIONS):
        col = 0
        for size in ("crypt", "ruin"):
            quest_id = "sofe:dungeon/%s_%s" % (region, size)
            name = "dungeons/%s_%s" % (region, size)
            if quest_id in all_quests:
                done = "sofe:quest/dungeon/%s_%s/step%d" % (region, size, len(all_quests[quest_id]["steps"]))
                desc = ["{ftbquests.sofe.dungeon.%s}" % size, "", "{%s.step1}" % key(quest_id)]
            else:  # Sulthari's are steps of Act I
                done = "sofe:quest/%s/step%d" % (act1, 4 if size == "crypt" else 6)
                desc = ["{ftbquests.sofe.dungeon.%s}" % size, "", "{ftbquests.sofe.dungeon.act1}"]
            out.append(quest(name, "{place.sofe.%s.%s}" % (region, size), col * 1.5, row * 1.5, by_advancement(name, done), desc,
                             icon="minecraft:chest" if size == "crypt" else "sofe:rune_stone", size=0.8 if size == "crypt" else 1.0))
            col += 1
        for pname, p in sorted(puzzles.items()):
            if not pname.startswith(region + "_") or "boss" not in p:
                continue
            name = "dungeons/" + pname
            boss = p["boss"].split(":", 1)[1]
            out.append(quest(name, "{puzzle.sofe.%s.place}" % pname, col * 1.5, row * 1.5,
                             by_advancement(name, "sofe:story/boss_" + boss),
                             ["{ftbquests.sofe.dungeon.large}", "", "{puzzle.sofe.%s}" % pname, "{ftbquests.sofe.dungeon.runes}"],
                             icon="minecraft:wither_skeleton_skull", shape="gear", size=1.2))
            col += 1
    return chapter("dungeons", len(ACTS) + 1, "{ftbquests.sofe.chapter.dungeons}", "sofe:rune_stone", out)


def lang():
    for code, entries in (("en_us", LANG_EN), ("es_es", LANG_ES)):
        path = os.path.join(LANG, code + ".json")
        with open(path, encoding="utf-8") as f:
            data = json.load(f)
        data = {k: v for k, v in data.items() if not k.startswith("ftbquests.sofe.")}
        data.update(entries)
        with open(path, "w", encoding="utf-8", newline="\n") as f:
            json.dump(data, f, ensure_ascii=False, indent=2)
            f.write("\n")


def main():
    all_quests = quests()
    advancements(all_quests)
    write(os.path.join(BOOK, "data.snbt"), {"version": 13, "title": "{ftbquests.sofe.title}", "default_quest_shape": "circle",
                                             "default_autoclaim_rewards": "disabled", "progression_mode": "linear",
                                             "drop_loot_crates": False, "disable_gui": False, "pause_game": False})
    write(os.path.join(BOOK, "chapter_groups.snbt"), {"chapter_groups": []})
    chapters_dir = os.path.join(BOOK, "chapters")
    for old in glob.glob(os.path.join(chapters_dir, "*.snbt")):
        os.remove(old)
    for c in book(all_quests):
        write(os.path.join(chapters_dir, c["filename"] + ".snbt"), c)
    lang()
    print("%d quests, %d step advancements, the book in %s" % (len(all_quests), sum(len(q["steps"]) for q in all_quests.values()), BOOK))


if __name__ == "__main__":
    main()
