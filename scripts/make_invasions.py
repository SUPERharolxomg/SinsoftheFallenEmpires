"""The Void's invasions (com.sofe.quest.VoidInvasion), as data: a quest step whose creatures come in waves out of rifts
on the sides of a place, rather than all at once round the Bearer.

- Act I (sofe:act1_eclipse, step 2): the Void attacks Sulthari in five waves, 80 creatures: from the east (the training
  grounds), then the west (the low bazaar), the north (the Observatory's avenue), the south (the lower district), and
  at last from every side at once, an elite leading each rift. Each side's rift opens by a district's garrison.
- Every other step that asks to drive out the Void and called its creatures round the Bearer (the side quests and the
  Bearers' own quests) becomes a small invasion round where the Bearer stands when it begins: three waves for a side
  quest, two for a Bearer's.

Writes the quest files and the English and Spanish texts. Run after make_story.py and make_dungeons.py, from the
repository root: python scripts/make_invasions.py   (then python scripts/make_quest_book.py)
"""
import glob
import json
import os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DATA = os.path.join(ROOT, "src", "main", "resources", "data", "sofe")
LANG = os.path.join(ROOT, "src", "main", "resources", "assets", "sofe", "lang")
EN, ES = {}, {}


def text(key, en, es):
    EN[key], ES[key] = en, es


def wave(sides, count):
    return {"from": sides, "count": count}


SULTHARI = {
    "center": {"x": 0, "z": 12}, "reach": 170,
    "points": {"east": {"x": 96, "z": 40}, "west": {"x": -92, "z": 24}, "north": {"x": 0, "z": -58}, "south": {"x": -96, "z": 100}},
    "mobs": ["sofe:void_wretch", "sofe:void_zombie", "sofe:void_stalker"],
    "waves": [wave(["east"], 8), wave(["west"], 12), wave(["north"], 16), wave(["south"], 20),
              wave(["east", "west", "north", "south"], 24)],
}
SIDE = {"reach": 48, "distance": 18, "waves": [wave(["east"], 4), wave(["west"], 6), wave(["north", "south"], 12)]}
BEARER = {"reach": 48, "distance": 16, "waves": [wave(["east"], 4), wave(["west"], 8)]}


def total(invasion):
    return sum(w["count"] for w in invasion["waves"])


def dump(path, value):
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        json.dump(value, f, indent=2, ensure_ascii=False)
        f.write("\n")


def act1():
    path = os.path.join(DATA, "quests", "act1_eclipse.json")
    with open(path, encoding="utf-8") as f:
        q = json.load(f)
    step = q["steps"][1]
    assert step["objective"]["entity"] == "sofe:void_*", "Act I's second step is no longer the Void's attack"
    step["objective"]["count"] = total(SULTHARI)
    step["target"] = dict(SULTHARI["center"])
    step["invasion"] = SULTHARI
    step.pop("_invasion", None)
    dump(path, q)
    text("quest.sofe.act1_eclipse.step2", "The Void tears rifts open round Sulthari: hold the city through its five waves.",
         "El Vacío abre grietas alrededor de Sulthari: defiende la ciudad durante sus cinco oleadas.")
    text("dialogue.sofe.act1.invasion.1", "The ground shakes. Rifts tear open at the edges of the city, and faceless shapes pour out of them.",
         "El suelo tiembla. Se abren grietas en los bordes de la ciudad y de ellas brotan formas sin rostro.")


def smaller():
    """The side quests' and the Bearers' Void fights: a small invasion where the Bearer stands."""
    changed = 0
    for path in sorted(glob.glob(os.path.join(DATA, "quests", "**", "*.json"), recursive=True)):
        folder = os.path.basename(os.path.dirname(path))
        if folder not in ("side", "bearer"):
            continue
        with open(path, encoding="utf-8") as f:
            q = json.load(f)
        name = "%s.%s" % (folder, os.path.basename(path)[:-5])
        touched = False
        for i, step in enumerate(q["steps"]):
            o = step["objective"]
            if o["type"] != "kill" or o.get("entity") != "sofe:void_*":
                continue
            spawns = [e for e in step.get("on_start", []) if e["type"] == "spawn" and e["entity"].startswith("sofe:void_")]
            if not spawns and "invasion" not in step:
                continue
            invasion = dict(SIDE if folder == "side" else BEARER)
            kinds = sorted({e["entity"] for e in spawns}) or step.get("invasion", {}).get("mobs", [])
            invasion["mobs"] = ["sofe:void_wretch"] + [k for k in kinds if k != "sofe:void_wretch"]
            step["on_start"] = [e for e in step.get("on_start", []) if e not in spawns]
            if not step["on_start"]:
                del step["on_start"]
            o["count"] = total(invasion)
            step["invasion"] = invasion
            touched = True
            key = "quest.sofe.%s.step%d" % (name, i + 1)
            if folder == "bearer":
                text(key, "The Void tears rifts open round you: hold out through its waves.",
                     "El Vacío abre grietas a tu alrededor: resiste sus oleadas.")
            elif LANGS["es_es"].get(key, "").startswith("Derrota a los enemigos"):
                text(key, "The Void tears rifts open round you: hold out through its three waves.",
                     "El Vacío abre grietas a tu alrededor: resiste sus tres oleadas.")
        if touched:
            dump(path, q)
            changed += 1
    return changed


def messages():
    text("invasion.sofe.bar", "Void Invasion · Wave %s/%s · %s/%s fallen", "Invasión del Vacío · Oleada %s/%s · %s/%s abatidos")
    text("invasion.sofe.bar_waiting", "Void Invasion · Wave %s/%s is coming...", "Invasión del Vacío · Se acerca la oleada %s/%s...")
    text("invasion.sofe.wave", "Wave %s of %s", "Oleada %s de %s")
    text("invasion.sofe.last_wave", "Last wave", "Última oleada")
    text("invasion.sofe.from", "They attack from %s!", "¡Atacan por %s!")
    text("invasion.sofe.from_all", "They attack from every side!", "¡Atacan por todos los flancos!")
    text("invasion.sofe.and", "%s and %s", "%s y %s")
    text("invasion.sofe.comma", "%s, %s", "%s, %s")
    text("invasion.sofe.side.east", "the east", "el este")
    text("invasion.sofe.side.west", "the west", "el oeste")
    text("invasion.sofe.side.north", "the north", "el norte")
    text("invasion.sofe.side.south", "the south", "el sur")
    text("invasion.sofe.chat", "%s: %s", "%s: %s")
    text("invasion.sofe.wave_cleared", "Wave %s driven back. Catch your breath...", "Oleada %s rechazada. Recupera el aliento...")
    text("invasion.sofe.won", "The rifts close", "Las grietas se cierran")
    text("invasion.sofe.won_sub", "The Void is driven back", "El Vacío ha sido rechazado")
    text("message.sofe.lord_awakens", "%s rises in the depths!", "¡%s se alza en las profundidades!")


LANGS = {}


def main():
    for code in ("en_us", "es_es"):
        with open(os.path.join(LANG, code + ".json"), encoding="utf-8") as f:
            LANGS[code] = json.load(f)
    act1()
    changed = smaller()
    messages()
    for code, entries in (("en_us", EN), ("es_es", ES)):
        LANGS[code].update(entries)
        with open(os.path.join(LANG, code + ".json"), "w", encoding="utf-8", newline="\n") as f:
            json.dump(LANGS[code], f, ensure_ascii=False, indent=2)
            f.write("\n")
    print("Act I's invasion: %d waves, %d creatures; %d smaller quests; %d texts" % (len(SULTHARI["waves"]), total(SULTHARI), changed, len(EN)))


if __name__ == "__main__":
    main()
