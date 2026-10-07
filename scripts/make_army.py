"""The armies of the five empires (docs/Ejercitos.md): the names of their soldiers, archers and captains, and each
captain's dialogue, where a Bearer hires a soldier or an archer into their company or brings back the fallen.
Writes data/sofe/dialogue/army/<empire>_captain.json and the English and Spanish texts. The prices are the ones in
Army.java (SOLDIER_PRICE, ARCHER_PRICE, REVIVE_PRICE); the skins are painted by make_npc_skins.py.

Run from the repository root: python scripts/make_army.py
"""
import collections
import json
import os

DATA = os.path.join("src", "main", "resources", "data", "sofe")
LANG = os.path.join("src", "main", "resources", "assets", "sofe", "lang")
SOLDIER_PRICE, ARCHER_PRICE, REVIVE_PRICE, COMPANY_SIZE = 40, 50, 25, 3

# empire: dialogue style, what its captain says first (en, es)
EMPIRES = {
    "sulthari": ("sulthari", (
        "The Void still crawls out of the cracks, Bearer, and the Guard of the Crescent holds every street it can. If you go where we cannot, take some of my men.",
        "El Vacío aún se arrastra fuera de las grietas, Portador, y la Guardia de la Media Luna defiende cada calle que puede. Si vas adonde nosotros no llegamos, llévate a algunos de mis hombres.")),
    "nordrath": ("nordrath", (
        "Shields to the wall, and axes to whoever pays the clan. A shield-brother is cheap and faithful, Bearer. Will you have some?",
        "Escudos al muro, y hachas para quien pague al clan. Un hermano de escudo es barato y leal, Portador. ¿Quieres algunos?")),
    "parsivan": ("parsivan", (
        "The Violet Guard lends its blades only to those who keep their word. They say you keep yours, Bearer.",
        "La Guardia Violeta solo presta sus espadas a quien cumple su palabra. Dicen que tú cumples la tuya, Portador.")),
    "khemet": ("khemet", (
        "In Khemet the dead do not stay down. Neither do my soldiers, Bearer: as long as their wages are paid.",
        "En Khemet los muertos no se quedan quietos. Mis soldados tampoco, Portador: mientras se les pague.")),
    "aureum": ("aureum", (
        "The Legion marches for Aureum, and for whoever pays its wages. Name the road, Bearer, and they will walk it.",
        "La Legión marcha por Aureum, y por quien pague su sueldo. Nombra el camino, Portador, y lo recorrerán.")),
}
NAMES = {
    "sulthari": ("Sulthari", "Sulthari"), "nordrath": ("Nordrath", "Nordrath"), "parsivan": ("Parsivan", "Parsivan"),
    "khemet": ("Khemet", "Khemet"), "aureum": ("Aureum", "Aureum"),
}
RANKS = {"soldier": ("%s Soldier", "Soldado de %s"), "archer": ("%s Archer", "Arquero de %s"), "captain": ("%s Captain", "Capitán de %s")}

en, es = {}, {}


def text(key, pair):
    en[key], es[key] = pair
    return key


def write(path, data):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        json.dump(data, f, indent=2, ensure_ascii=False)
        f.write("\n")


text("entity.sofe.soldier", ("Soldier", "Soldado"))
offer = text("dialogue.sofe.army.offer", (
    "A soldier is %d dinars, an archer %d. Up to %d will follow you; those who fall I can bring back for %d dinars each, or send you others in their place."
    % (SOLDIER_PRICE, ARCHER_PRICE, COMPANY_SIZE, REVIVE_PRICE),
    "Un soldado cuesta %d dinares; un arquero, %d. Te seguirán hasta %d; a los que caigan puedo traerlos de vuelta por %d dinares cada uno, o mandarte otros en su lugar."
    % (SOLDIER_PRICE, ARCHER_PRICE, COMPANY_SIZE, REVIVE_PRICE)))
hire_soldier = text("dialogue.sofe.army.hire_soldier", ("Hire a soldier (%d dinars)." % SOLDIER_PRICE, "Contratar un soldado (%d dinares)." % SOLDIER_PRICE))
hire_archer = text("dialogue.sofe.army.hire_archer", ("Hire an archer (%d dinars)." % ARCHER_PRICE, "Contratar un arquero (%d dinares)." % ARCHER_PRICE))
revive = text("dialogue.sofe.army.revive", ("Bring back my fallen (%d dinars each)." % REVIVE_PRICE, "Traer de vuelta a mis caídos (%d dinares cada uno)." % REVIVE_PRICE))
leave = text("dialogue.sofe.army.leave", ("Not now.", "Ahora no."))

for empire, (style, greeting) in EMPIRES.items():
    name_en, name_es = NAMES[empire]
    for rank, (r_en, r_es) in RANKS.items():
        text("entity.sofe.soldier.%s.%s" % (empire, rank), (r_en % name_en, r_es % name_es))
    captain = "%s_captain" % empire
    text("npc.sofe." + captain, (RANKS["captain"][0] % name_en, RANKS["captain"][1] % name_es))
    lines = [
        {"speaker": captain, "text": text("dialogue.sofe.army.%s.greet" % empire, greeting)},
        {"speaker": captain, "text": offer, "answers": [
            {"text": hire_soldier, "next": -1, "effects": [{"type": "hire_soldier", "empire": empire, "rank": "soldier"}]},
            {"text": hire_archer, "next": -1, "effects": [{"type": "hire_soldier", "empire": empire, "rank": "archer"}]},
            {"text": revive, "next": -1, "effects": [{"type": "revive_soldiers"}]},
            {"text": leave, "next": -1}]},
    ]
    write(os.path.join(DATA, "dialogue", "army", captain + ".json"), {"style": style, "npc": captain, "lines": lines})

# what the company does, in the chat
text("message.sofe.army.hired", ("%s joins your company (%s dinars).", "%s se une a tu compañía (%s dinares)."))
text("message.sofe.army.no_dinars", ("You need %s dinars.", "Necesitas %s dinares."))
text("message.sofe.army.company_full", ("Your company is full: up to %s soldiers follow a Bearer.", "Tu compañía está completa: a un Portador lo siguen hasta %s soldados."))
text("message.sofe.army.none_fallen", ("None of your company has fallen.", "Nadie de tu compañía ha caído."))
text("message.sofe.army.revived", ("%s of your company stand again (%s dinars).", "%s de tu compañía vuelven a estar en pie (%s dinares)."))
text("message.sofe.army.fell", ("%s has fallen. A captain can bring them back.", "%s ha caído. Un capitán puede traerlo de vuelta."))

for name, entries in (("en_us.json", en), ("es_es.json", es)):
    path = os.path.join(LANG, name)
    with open(path, encoding="utf-8") as f:
        lang = json.load(f, object_pairs_hook=collections.OrderedDict)
    lang.update(entries)
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        json.dump(lang, f, indent=2, ensure_ascii=False)
        f.write("\n")
print(len(en), "texts,", len(EMPIRES), "captains")
