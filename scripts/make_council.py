"""The Grand Council of Sulthari sits in the palace, with the Grand Vizier Ozhan: the Council's elder (who used to wait at
the Observatory, far from the Council the story sends the Bearer to) and four councillors of their own, the treasurer,
the general, the judge and the sage. Each has a place in the palace's hall round Ozhan, a name and a few words.

Writes the places in structure_positions.json, the councillors' dialogues (data/sofe/dialogue/council), the texts that
said the elder was at the Observatory, and the English and Spanish texts. Their skins are in make_npc_skins.py.

Run from the repository root: python scripts/make_council.py   (then python scripts/make_placeholder_portraits.py)
"""
import json
import os

ROOT = os.path.dirname(os.path.dirname(os.path.abspath(__file__)))
DATA = os.path.join(ROOT, "src", "main", "resources", "data", "sofe")
LANG = os.path.join(ROOT, "src", "main", "resources", "assets", "sofe", "lang")
EN, ES = {}, {}

# id, place in the palace (x, z), name (en, es), their words (en, es)
COUNCIL = [
    ("council_elder", (3, 85), None, None),
    ("council_treasurer", (-3, 85), ("Murad, Treasurer of the Council", "Murad, Tesorero del Consejo"),
     ("Every dinar the Void burns is one we will not have when the walls need mending. Spend yours well, Bearer.",
      "Cada dinar que quema el Vacío es uno que no tendremos cuando haya que reparar las murallas. Gasta los tuyos con cabeza, Portador.")),
    ("council_general", (-6, 87), ("Bahadir, General of the Council", "Bahadır, General del Consejo"),
     ("My soldiers hold the districts. Hire some if you go where they cannot follow; a captain in every quarter will sign them over.",
      "Mis soldados guardan los distritos. Contrata a algunos si vas adonde ellos no pueden seguirte: un capitán en cada barrio te los cederá.")),
    ("council_judge", (6, 87), ("Leyla, Judge of the Council", "Leyla, Jueza del Consejo"),
     ("The Law was broken once, and seven sins walked out of the crack. Do not let anyone tell you it cannot break twice.",
      "La Ley se rompió una vez, y siete pecados salieron por la grieta. No dejes que nadie te diga que no puede romperse dos veces.")),
    ("council_sage", (0, 88), ("Esra, Sage of the Council", "Esra, Sabia del Consejo"),
     ("The Wardens left their dead in the Crypt and their runes in the Ruin. Read what they wrote before you wake what they buried.",
      "Los Guardianes dejaron a sus muertos en la Cripta y sus runas en la Ruina. Lee lo que escribieron antes de despertar lo que enterraron.")),
]


def text(key, en, es):
    EN[key], ES[key] = en, es


def dump(path, value, indent=2):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        json.dump(value, f, indent=indent, ensure_ascii=False)
        f.write("\n")


def main():
    path = os.path.join(DATA, "structure_positions.json")
    with open(path, encoding="utf-8") as f:
        positions = json.load(f)
    npcs = [n for n in positions["npcs"] if not n["npc"].startswith("council_")]
    at = next(i for i, n in enumerate(npcs) if n["npc"] == "ozhan") + 1
    for npc, (x, z), name, words in COUNCIL:
        npcs.insert(at, {"npc": npc, "type": "story", "x": x, "z": z, "yaw": 180})
        at += 1
        if name is None:
            continue
        text("npc.sofe." + npc, name[0], name[1])
        text("dialogue.sofe.%s.1" % npc, words[0], words[1])
        dump(os.path.join(DATA, "dialogue", "council", npc + ".json"),
             {"style": "sulthari", "npc": npc, "lines": [{"speaker": npc, "text": "dialogue.sofe.%s.1" % npc}]})
    positions["npcs"] = npcs
    dump(path, positions, indent=1)

    # the elder is in the palace now: the side quest that sends the Bearer to him points there
    embers = os.path.join(DATA, "quests", "side", "sulthari_embers.json")
    with open(embers, encoding="utf-8") as f:
        q = json.load(f)
    q["steps"][0]["target"] = {"x": COUNCIL[0][1][0], "z": COUNCIL[0][1][1]}
    dump(embers, q)
    text("quest.sofe.side.sulthari_embers.step1", "Dilara says the elder of the Council is in the palace, with the Council. Hear what he has to say.",
         "Dilara dice que el anciano del Consejo está en el palacio, con el Consejo. Escucha lo que tiene que decir.")
    text("dialogue.sofe.dilara.embers.2", "The elder of the Council is in the palace deciding where to send them. Talk to him. Please.",
         "El anciano del Consejo está en el palacio decidiendo adónde enviarlos. Habla con él. Por favor.")

    for code, entries in (("en_us", EN), ("es_es", ES)):
        lang = os.path.join(LANG, code + ".json")
        with open(lang, encoding="utf-8") as f:
            data = json.load(f)
        data.update(entries)
        with open(lang, "w", encoding="utf-8", newline="\n") as f:
            json.dump(data, f, ensure_ascii=False, indent=2)
            f.write("\n")
    print("%d of the Council in the palace; %d texts" % (len(COUNCIL), len(EN)))


if __name__ == "__main__":
    main()
