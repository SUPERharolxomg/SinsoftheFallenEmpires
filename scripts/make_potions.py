"""The potions of every act (docs/Pociones.md, "Potions and alchemy"): a Pomegranate Elixir for health and a
Bearer's Tonic for the class resource, five strengths each, one per act. Writes their registration (the
generated-potions block of ItemRegistry), names and descriptions, Ferid's offers (each from its act), the
Alembic recipes (the weaker potion, the herb and the act's material), the potion-belt tag and their 32x32 art:
a glass bottle in the shape of its empire, red or blue inside, its stopper and band in the act's metal.

Run from the repository root: python scripts/make_potions.py
"""
import json
import math
import os

from PIL import Image

ROOT = os.path.join("src", "main")
RES = os.path.join(ROOT, "resources")
ITEM_TEX = os.path.join(RES, "assets", "sofe", "textures", "item")
LANG = os.path.join(RES, "assets", "sofe", "lang")
REGISTRY = os.path.join(ROOT, "java", "com", "sofe", "registry", "ItemRegistry.java")

# act, prefix (None = the Act I id kept), English and Spanish strength, heal (health), restore (fraction), price, bottle, metal, material
TIERS = [
    (1, None, "Minor", "Menor", 20, 0.25, 15, "round", "brass", None),
    (2, "strong", "Strong", "Fuerte", 35, 0.35, 40, "flask", "iron", "sofe:glacial_iron_nugget"),
    (3, "major", "Major", "Mayor", 50, 0.50, 90, "tall", "silver", "sofe:star_lapis_powder"),
    (4, "grand", "Grand", "Grande", 75, 0.65, 160, "amphora", "gold", "sofe:solar_gold_powder"),
    (5, "imperial", "Imperial", "Imperial", 100, 0.80, 260, "crown", "aetherium", "sofe:aetherium_shard"),
]


def elixir_id(t):
    return "minor_pomegranate_elixir" if t[1] is None else "%s_pomegranate_elixir" % t[1]


def tonic_id(t):
    return "bearers_tonic" if t[1] is None else "%s_bearers_tonic" % t[1]


def const(id):
    return id.upper()


# ------------------------------------------------------------------------------------------------ java
def write_registry():
    s = open(REGISTRY, encoding="utf-8").read()
    lines = ["    // <generated-potions> by scripts/make_potions.py: an elixir and a tonic per act"]
    for t in TIERS:
        e, n = elixir_id(t), tonic_id(t)
        lines.append('    public static final RegistryObject<Item> %s = ITEMS.register("%s",' % (const(e), e))
        lines.append("            () -> new ConsumableItems.Elixir(new Item.Properties()%s, %d));" % (".rarity(Rarity.UNCOMMON)" if t[0] >= 3 else "", t[4]))
        lines.append('    public static final RegistryObject<Item> %s = ITEMS.register("%s",' % (const(n), n))
        lines.append("            () -> new ConsumableItems.Tonic(new Item.Properties()%s, %sf));" % (".rarity(Rarity.UNCOMMON)" if t[0] >= 3 else "", t[5]))
    lines.append("")
    lines.append("    /** Every elixir and tonic, weakest first. */")
    lines.append("    public static List<RegistryObject<Item>> potions() {")
    lines.append("        return List.of(%s);" % ", ".join("%s, %s" % (const(elixir_id(t)), const(tonic_id(t))) for t in TIERS))
    lines.append("    }")
    lines.append("    // </generated-potions>")
    block = "\n".join(lines)
    start, end = "    // <generated-potions>", "    // </generated-potions>"
    if start in s:
        s = s[:s.index(start)] + block + s[s.index(end) + len(end):]
    else:
        # the Act I potions move into the block
        for old in ('    public static final RegistryObject<Item> MINOR_POMEGRANATE_ELIXIR = ITEMS.register("minor_pomegranate_elixir",\n'
                    '            () -> new ConsumableItems.Elixir(new Item.Properties()));\n',
                    '    public static final RegistryObject<Item> BEARERS_TONIC = ITEMS.register("bearers_tonic", () -> new ConsumableItems.Tonic(new Item.Properties()));\n'):
            assert old in s, old
            s = s.replace(old, "")
        anchor = '    public static final RegistryObject<Item> BEARERS_FLASK = ITEMS.register("bearers_flask",'
        assert anchor in s
        s = s.replace(anchor, block + "\n" + anchor, 1)
        # every potion in the tab and as a flat item model
        old = "        for (RegistryObject<Item> i : List.of(BEARERS_FLASK, BEARERS_TONIC, MINOR_POMEGRANATE_ELIXIR, BRASS_FLASK,"
        assert old in s
        s = s.replace(old, "        potions().forEach(i -> items.add(i.get()));\n        for (RegistryObject<Item> i : List.of(BEARERS_FLASK, BRASS_FLASK,")
        old = "        flat.addAll(jewelry());"
        assert old in s
        s = s.replace(old, "        flat.addAll(potions());\n" + old, 1)
    open(REGISTRY, "w", encoding="utf-8").write(s)


def write_data():
    # names and descriptions
    entries = {}
    for t in TIERS:
        e, n = elixir_id(t), tonic_id(t)
        entries["item.sofe." + e] = ("%s Pomegranate Elixir" % t[2], "Elixir de Granada %s" % t[3])
        entries["item.sofe.%s.desc" % e] = ("Heals %d health: half at once, the rest over 3 seconds" % t[4],
                                            "Cura %d de vida: la mitad al instante, el resto en 3 segundos" % t[4])
        entries["item.sofe." + n] = ("%s Bearer's Tonic" % t[2] if t[1] else "Bearer's Tonic", "Tónico del Portador %s" % t[3] if t[1] else "Tónico del Portador")
        entries["item.sofe.%s.desc" % n] = ("Restores %d%% of your class resource" % round(t[5] * 100),
                                            "Restaura el %d%% del recurso de tu clase" % round(t[5] * 100))
    for f, i in (("en_us", 0), ("es_es", 1)):
        path = os.path.join(LANG, f + ".json")
        data = json.load(open(path, encoding="utf-8"))
        for k, v in entries.items():
            data[k] = v[i]
        open(path, "w", encoding="utf-8").write(json.dumps(data, ensure_ascii=False, indent=2) + "\n")
    # Ferid sells each from its act
    path = os.path.join(RES, "data", "sofe", "merchant_offers", "ferid.json")
    ferid = json.load(open(path, encoding="utf-8"))
    ours = {elixir_id(t) for t in TIERS} | {tonic_id(t) for t in TIERS}
    rest = [o for o in ferid["offers"] if o["sell"].split(":")[1] not in ours]
    offers = []
    for t in TIERS:
        for id, price, stock in ((elixir_id(t), t[6], 8 - t[0]), (tonic_id(t), int(t[6] * 1.6), 7 - t[0])):
            o = {"sell": "sofe:" + id, "price": price, "stock": max(2, stock)}
            if t[0] > 1:
                o["min_act"] = t[0]
            offers.append(o)
    ferid["offers"] = offers + rest
    open(path, "w", encoding="utf-8").write(json.dumps(ferid, ensure_ascii=False, indent=2) + "\n")
    # the Alembic: the weaker potion, the herb, the act's material
    folder = os.path.join(RES, "data", "sofe", "recipes", "alembic")
    for prev, t in zip(TIERS, TIERS[1:]):
        for id, prev_id, herb in ((elixir_id(t), elixir_id(prev), "sofe:pomegranate"), (tonic_id(t), tonic_id(prev), "sofe:desert_lotus")):
            recipe = {"type": "sofe:alembic", "ingredients": [{"item": "sofe:" + prev_id, "count": 1}, {"item": herb, "count": 2},
                                                              {"item": t[9], "count": 1}], "result": "sofe:" + id}
            open(os.path.join(folder, id + ".json"), "w", encoding="utf-8").write(json.dumps(recipe, indent=2) + "\n")
    # all of them fit the potion belt
    path = os.path.join(RES, "data", "curios", "tags", "items", "potion_belt.json")
    tag = json.load(open(path, encoding="utf-8"))
    for t in TIERS:
        for id in (elixir_id(t), tonic_id(t)):
            if "sofe:" + id not in tag["values"]:
                tag["values"].append("sofe:" + id)
    open(path, "w", encoding="utf-8").write(json.dumps(tag, indent=2) + "\n")


# ------------------------------------------------------------------------------------------------ art
OUT = (22, 14, 20)
METALS = {
    "brass": [(120, 78, 26), (190, 136, 52), (236, 188, 96), (255, 236, 170)],
    "iron": [(70, 80, 96), (130, 146, 166), (190, 206, 222), (236, 246, 255)],
    "silver": [(96, 100, 116), (166, 172, 188), (218, 222, 232), (252, 252, 255)],
    "gold": [(140, 92, 18), (220, 164, 40), (250, 210, 90), (255, 244, 196)],
    "aetherium": [(30, 110, 130), (60, 190, 210), (150, 240, 250), (240, 255, 255)],
}
LIQUID = {"elixir": [(110, 10, 24), (180, 24, 44), (232, 60, 70), (255, 170, 160)],
          "tonic": [(14, 30, 110), (30, 70, 190), (70, 140, 250), (180, 220, 255)]}
GLASS = [(150, 190, 210), (200, 230, 240), (240, 252, 255)]


def body(shape, x, y):
    """Whether (x, y) of a 32x32 sprite is inside the bottle's glass, and how far round it (-1 left .. 1 right)."""
    cx = 15.5
    if shape == "round":      # Act I: a round brass-stoppered flask
        r = 9.5
        d = math.hypot(x - cx, y - 19.5)
        if d <= r:
            return True, (x - cx) / r
        if 6 <= y <= 11 and abs(x - cx) <= 2.6:
            return True, (x - cx) / 3
    elif shape == "flask":    # Act II: a squared Nordic flask with shoulders
        if 13 <= y <= 28 and abs(x - cx) <= 8.6 - max(0, 15 - y) * 1.3:
            return True, (x - cx) / 9
        if 6 <= y <= 12 and abs(x - cx) <= 2.6:
            return True, (x - cx) / 3
    elif shape == "tall":     # Act III: a slender Parsivan rosewater bottle, a drop widening to the bottom
        if 12 <= y <= 28:
            t = (y - 12) / 16
            w = 2.4 + 6.4 * math.sin(t * math.pi * 0.62) ** 1.4
            if abs(x - cx) <= w:
                return True, (x - cx) / w
        if 5 <= y <= 11 and abs(x - cx) <= 2.1:
            return True, (x - cx) / 2.5
    elif shape == "amphora":  # Act IV: a Roman amphora with handles
        w = 8.5 * math.sin((y - 10) / 19 * math.pi) if 10 <= y <= 29 else 0
        if 10 <= y <= 29 and abs(x - cx) <= max(2.5, w):
            return True, (x - cx) / max(2.5, w)
        if 6 <= y <= 10 and abs(x - cx) <= 2.6:
            return True, (x - cx) / 3
    elif shape == "crown":    # Act V: a faceted imperial decanter
        if 11 <= y <= 28 and abs(x - cx) + abs(y - 20) * 0.45 <= 9.5:
            return True, (x - cx) / 9.5
        if 5 <= y <= 11 and abs(x - cx) <= 2.6:
            return True, (x - cx) / 3
    return False, 0


def bottle(name, kind, shape, metal):
    m, liq = METALS[metal], LIQUID[kind]
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    px = img.load()
    inside = {}
    for y in range(32):
        for x in range(32):
            ok, side = body(shape, x, y)
            if ok:
                inside[(x, y)] = side
    level = {"round": 14, "flask": 15, "tall": 15, "amphora": 14, "crown": 14}[shape]  # liquid surface
    neck_bottom = min(y for (x, y) in inside) + 6
    for (x, y), side in inside.items():
        if y >= level and y > neck_bottom - 1:
            shade = 2 if side < -0.35 else 1 if side < 0.45 else 0
            c = liq[shade]
            if y == level:
                c = liq[3] if side < 0 else liq[2]
            px[x, y] = c + (255,)
        else:
            px[x, y] = (GLASS[1] if side < 0 else GLASS[0]) + (150,)
    # glass highlight on the left, a glint on the liquid
    for (x, y), side in inside.items():
        if -0.75 < side < -0.45 and level + 2 <= y <= level + 9:
            px[x, y] = GLASS[2] + (255,)
    # outline
    solid = set(inside)
    for (x, y) in list(solid):
        for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            q = (x + dx, y + dy)
            if q not in solid and 0 <= q[0] < 32 and 0 <= q[1] < 32:
                px[q] = OUT + (255,)
    top = min(y for (x, y) in inside)
    # the stopper and the band of the neck, in the act's metal
    for y in range(top - 3, top + 1):
        for x in range(13, 19):
            c = m[2] if x < 15 else m[1] if x < 18 else m[0]
            if y == top - 3:
                c = m[3] if x < 16 else m[2]
            px[x, y] = c + (255,)
    for x in range(12, 20):
        px[x, top + 4] = (m[2] if x < 16 else m[1]) + (255,)
    for x in (12, 19):
        for y in range(top - 3, top + 1):
            px[x, y] = OUT + (255,)
    for x in range(12, 20):
        px[x, top - 4] = OUT + (255,)
    if shape == "amphora":  # handles from the neck down to the shoulders
        for i, (dx, dy) in enumerate(((3, 4), (4, 4), (5, 5), (6, 6), (6, 7), (6, 8), (5, 9))):
            for sx in (-1, 1):
                x = int(15.5 + sx * (dx + 0.5))
                px[x, top + dy] = (m[2] if sx < 0 else m[1]) + (255,)
    if shape == "crown":    # a gold crown band and a gem
        band = level + 7
        for x in range(9, 23):
            if (x, band) in inside:
                px[x, band] = m[2] + (255,)
        px[15, band] = (255, 255, 255, 255)
    if shape == "flask":    # a rune on the flask
        for x, y in ((15, 21), (16, 22), (15, 23), (16, 24), (14, 22)):
            px[x, y] = m[3] + (255,)
    img.save(os.path.join(ITEM_TEX, name + ".png"))


if __name__ == "__main__":
    write_registry()
    write_data()
    for t in TIERS:
        bottle(elixir_id(t), "elixir", t[7], t[8])
        bottle(tonic_id(t), "tonic", t[7], t[8])
    print("%d potions written" % (len(TIERS) * 2))
