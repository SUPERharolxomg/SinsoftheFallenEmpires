"""Writes the story data of scripts/story_catalog.py: the Bearer quests (data/sofe/quests/bearer), the Nordrath fate
quests (data/sofe/quests/side), the dialogues of their givers, of the Bearers as NPCs (with the offer to travel
together) and of the companions, and every line in English and Spanish.

Run from the repository root: python scripts/make_story.py
"""
import collections
import json
import os
import sys

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from story_catalog import (BEARER_LINES, BEARER_QUESTS, CLASSES, COMPANION, FATE_QUESTS, FATE_REACTIONS, HERO, HIRE,  # noqa: E402
                           ORDERS)

RES = os.path.join("src", "main", "resources")
DATA = os.path.join(RES, "data", "sofe")
OD = collections.OrderedDict
EN, ES = OD(), OD()
STYLE = {1: "sulthari", 2: "nordrath"}


def say(key, texts):
    EN[key], ES[key] = texts


def write(path, obj):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8") as f:
        f.write(json.dumps(obj, indent=2, ensure_ascii=False) + "\n")


def step(n, quest):
    return {"type": "quest_step", "quest": quest, "step": n}


def exactly(n, quest):
    """At step n of the quest (1 is the first), not further."""
    return {"type": "all_of", "conditions": [step(n, quest), {"type": "not", "condition": step(n + 1, quest)}]}


def line(speaker, key, **extra):
    d = OD([("speaker", speaker), ("text", key)])
    d.update(extra)
    return d


def quest_files(q, kind):
    """A quest of three or two steps: (go to the place,) defeat what is there, come back to the giver."""
    qid = "sofe:%s/%s" % (kind, q["id"])
    base = "quest.sofe.%s.%s" % (kind, q["id"])
    say(base, q["title"])
    steps, n = [], 0
    giver_region = "Skarnhold" if q.get("act", 2) == 2 else "Sulthari"
    if q.get("reach"):
        place, x, z = q["reach"]
        steps.append(OD([("objective", {"type": "reach", "x": x, "z": z, "radius": 24}), ("target", {"x": x, "z": z})]))
        n += 1
        names = {"arena": ("the Nordrath Arena", "la Arena de Nordrath"), "forge": ("the Nordrath Forge", "la Forja de Nordrath"),
                 "caverns": ("the western caverns", "las cavernas del oeste")}[place]
        say("%s.step%d" % (base, n), ("Go to %s." % names[0], "Ve a %s." % names[1]))
    steps.append(OD([("objective", {"type": "kill", "entity": q["spawn"].split(":")[0] + ":" + ("void_*" if "void" in q["spawn"] else q["spawn"].split(":")[1]),
                                    "count": q["count"]}),
                     ("on_start", [{"type": "spawn", "entity": q["spawn"], "count": q["count"], "radius": 10}])]))
    n += 1
    say("%s.step%d" % (base, n), ("Defeat the enemies (%d)." % q["count"], "Derrota a los enemigos (%d)." % q["count"]))
    steps.append(OD([("objective", {"type": "manual"})]))
    n += 1
    giver_name = "npc.sofe." + q["giver"]
    say("%s.step%d" % (base, n), ("Go back to whoever asked for your help in %s." % giver_region, "Vuelve con quien te pidió ayuda en %s." % giver_region))
    rewards = [{"type": "give_xp", "amount": q["xp"]}]
    if q.get("relic"):
        rewards.append({"type": "give_relic", "relic": q["relic"]})
    if q.get("reward_item"):
        rewards.append({"type": "give_item", "item": q["reward_item"][0], "count": q["reward_item"][1]})
    quest = OD([("type", kind if kind != "bearer" else "bearer"), ("act", q.get("act", 2)), ("steps", steps), ("rewards", rewards)])
    if kind == "bearer":
        quest["requires"] = {"type": "all_of", "conditions": [{"type": "class_is", "class": q["cls"]}, {"type": "act_reached", "act": q["act"]}]}
    else:
        quest["requires"] = {"type": "act_reached", "act": 2}
    write(os.path.join(DATA, "quests", kind, q["id"] + ".json"), quest)
    return qid, n


def giver_dialogues(q, kind, qid, last):
    npc, style = q["giver"], STYLE[q.get("act", 2)]
    key = "dialogue.sofe.q.%s" % q["id"]
    folder = os.path.join(DATA, "dialogue", npc)
    say(key + ".offer.1", q["offer"][0])
    say(key + ".offer.2", q["offer"][1])
    say(key + ".accept", q["accept"])
    say(key + ".later", ("Not yet.", "Todavía no."))
    say(key + ".reminder", q["reminder"])
    starts = {"type": "all_of", "conditions": [{"type": "not", "condition": step(1, qid)}] +
              ([{"type": "class_is", "class": q["cls"]}, {"type": "act_reached", "act": q["act"]}] if kind == "bearer" else [{"type": "act_reached", "act": 2}])}
    write(os.path.join(folder, "q_%s_offer.json" % q["id"]), OD([
        ("style", style), ("npc", npc), ("priority", 20), ("requires", starts),
        ("lines", [line(npc, key + ".offer.1"),
                   line(npc, key + ".offer.2", answers=[OD([("text", key + ".accept"), ("next", -1), ("effects", [{"type": "start_quest", "quest": qid}])]),
                                                      OD([("text", key + ".later"), ("next", -1)])])])]))
    write(os.path.join(folder, "q_%s_reminder.json" % q["id"]), OD([
        ("style", style), ("npc", npc), ("priority", 15),
        ("requires", {"type": "all_of", "conditions": [step(1, qid), {"type": "not", "condition": step(last, qid)}]}),
        ("lines", [line(npc, key + ".reminder")])]))
    if q.get("choice"):  # a fate: the answer sets it and finishes the quest
        say(key + ".choice.1", q["choice"][0])
        say(key + ".choice.2", q["choice"][1])
        answers, lines = [], [line(npc, key + ".choice.1"), None]
        for fate, ask, reply in q["answers"]:
            say(key + ".answer." + fate, ask)
            say(key + ".reply." + fate, reply)
            answers.append(OD([("text", key + ".answer." + fate), ("next", 2),
                               ("effects", [{"type": "set_fate", "region": "nordrath", "fate": fate}, {"type": "advance_quest", "quest": qid}])]))
        lines[1] = line(npc, key + ".choice.2", answers=answers)
        for fate, _, _ in q["answers"]:  # only the reply to the fate chosen is shown
            lines.append(line(npc, key + ".reply." + fate, condition={"type": "fate_is", "region": "nordrath", "fate": fate}))
        write(os.path.join(folder, "q_%s_done.json" % q["id"]), OD([
            ("style", style), ("npc", npc), ("priority", 25), ("requires", exactly(last, qid)), ("lines", lines)]))
    else:
        say(key + ".done.1", q["done"][0])
        say(key + ".done.2", q["done"][1])
        say(key + ".thanks", ("Thank you.", "Gracias."))
        write(os.path.join(folder, "q_%s_done.json" % q["id"]), OD([
            ("style", style), ("npc", npc), ("priority", 25), ("requires", exactly(last, qid)),
            ("lines", [line(npc, key + ".done.1"),
                       line(npc, key + ".done.2", answers=[OD([("text", key + ".thanks"), ("next", -1),
                                                                ("effects", [{"type": "advance_quest", "quest": qid}])])])])]))


def bearers():
    for cls in CLASSES:
        hero = HERO[cls]
        folder = os.path.join(DATA, "dialogue", hero)
        for act, texts in BEARER_LINES[cls].items():
            key = "dialogue.sofe.%s.act%d" % (hero, act)
            say(key, texts)
            say("dialogue.sofe.%s.hire" % hero, HIRE["offer"])
            req = {"type": "act_reached", "act": act}
            write(os.path.join(folder, "act%d.json" % act), OD([
                ("style", STYLE[act]), ("npc", hero), ("priority", act), ("requires", req),
                ("lines", [line(hero, key),
                           line("player", "dialogue.sofe.%s.hire" % hero,
                                answers=[OD([("text", "dialogue.sofe.hire.yes"), ("next", -1), ("effects", [{"type": "hire_companion", "bearer": cls}])]),
                                         OD([("text", "dialogue.sofe.hire.no"), ("next", -1)])])])]))
        c = COMPANION[cls]
        for k in ("hired", "dismissed", "down", "boss"):
            say("companion.sofe.%s.%s" % (cls, k), c[k])
        for region, texts in c["regions"].items():
            say("companion.sofe.%s.region.%s" % (cls, region), texts)
        say("companion.sofe.%s.greet" % cls, ORDERS["greet"])
        write(os.path.join(DATA, "dialogue", "companion", cls + ".json"), OD([
            ("style", "sulthari"),
            ("lines", [line(hero, "companion.sofe.%s.greet" % cls,
                            answers=[OD([("text", "dialogue.sofe.order." + o), ("next", -1), ("effects", [{"type": "companion_order", "order": o}])])
                                     for o in ("follow", "stay", "dismiss")])])]))
    say("dialogue.sofe.hire.yes", HIRE["yes"])
    say("dialogue.sofe.hire.no", HIRE["no"])
    for o in ("follow", "stay", "dismiss"):
        say("dialogue.sofe.order." + o, ORDERS[o])


def fate_reactions():
    folder = os.path.join(DATA, "dialogue", "nordrath_skald")
    for fate, texts in FATE_REACTIONS.items():
        key = "dialogue.sofe.nordrath_skald.fate.%s" % fate
        say(key, texts)
        write(os.path.join(folder, "fate_%s.json" % fate), OD([
            ("style", "nordrath"), ("npc", "nordrath_skald"), ("priority", 5),
            ("requires", {"type": "fate_is", "region": "nordrath", "fate": fate}),
            ("lines", [line("nordrath_skald", key)])]))


def lang():
    for code, add in (("en_us", EN), ("es_es", ES)):
        p = os.path.join(RES, "assets", "sofe", "lang", code + ".json")
        d = json.load(open(p, encoding="utf-8"), object_pairs_hook=OD)
        d.update(add)
        # what the companions and the Concord say outside the files
        extra = {"en_us": {"message.sofe.companion.taken": "A Bearer of your group is already %s.",
                           "message.sofe.companion.own": "You cannot travel with yourself.",
                           "message.sofe.companion.already": "You already travel with %s. Send them back to Sulthari first.",
                           "message.sofe.companion.says": "%s: %s",
                           "message.sofe.companion.stay": "%s waits here.", "message.sofe.companion.follow": "%s follows you.",
                           "message.sofe.concord": "Class Concord: %s", "message.sofe.concord.pillars": "The Five Pillars stand together!",
                           "entity.sofe.companion": "Companion"},
                 "es_es": {"message.sofe.companion.taken": "Un portador de tu grupo ya es %s.",
                           "message.sofe.companion.own": "No puedes viajar contigo mismo.",
                           "message.sofe.companion.already": "Ya viajas con %s. Envíalo de vuelta a Sulthari primero.",
                           "message.sofe.companion.says": "%s: %s",
                           "message.sofe.companion.stay": "%s espera aquí.", "message.sofe.companion.follow": "%s te sigue.",
                           "message.sofe.concord": "Concordia de Clases: %s", "message.sofe.concord.pillars": "¡Los Cinco Pilares están juntos!",
                           "entity.sofe.companion": "Compañero"}}[code]
        d.update(extra)
        open(p, "w", encoding="utf-8").write(json.dumps(d, ensure_ascii=False, indent=2) + "\n")


if __name__ == "__main__":
    for q in BEARER_QUESTS:
        qid, last = quest_files(q, "bearer")
        giver_dialogues(q, "bearer", qid, last)
    for q in FATE_QUESTS:
        qid, last = quest_files(q, "side")
        giver_dialogues(q, "side", qid, last)
    bearers()
    fate_reactions()
    lang()
    print("story: %d Bearer quests, %d fate quests, %d companions" % (len(BEARER_QUESTS), len(FATE_QUESTS), len(COMPANION)))
