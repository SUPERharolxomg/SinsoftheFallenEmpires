"""The art pass after the playtest: materials, ores and storage blocks, potion flasks, the empire shields
on the vanilla shield's texture layout, and the models and textures of the Imperial Forge and the iron
brazier. Shapes follow the silhouettes players know from the game (an ingot is a bar seen from above, a
nugget a few lumps, raw metal a rough chunk, an ore stone with clusters), drawn here from scratch with
each material's own colours, a dark outline and light from the upper left.

Run from the repository root: python scripts/make_art_pass.py
"""
import json
import math
import os
import random

from PIL import Image

ASSETS = os.path.join("src", "main", "resources", "assets", "sofe")
ITEM = os.path.join(ASSETS, "textures", "item")
BLOCK = os.path.join(ASSETS, "textures", "block")
SHIELD = os.path.join(ASSETS, "textures", "entity", "shield")
MODELS = os.path.join(ASSETS, "models")
for d in (ITEM, BLOCK, SHIELD, os.path.join(MODELS, "block"), os.path.join(MODELS, "item")):
    os.makedirs(d, exist_ok=True)

OUTLINE = (22, 16, 20)

# each material: d dark, m mid, l light, h highlight
METALS = {
    "sulthari_brass": {"d": (120, 70, 22), "m": (196, 134, 46), "l": (238, 188, 92), "h": (255, 236, 168)},
    "glacial_iron": {"d": (56, 104, 150), "m": (122, 176, 214), "l": (190, 226, 246), "h": (246, 252, 255)},
    "solar_gold": {"d": (170, 96, 10), "m": (240, 170, 30), "l": (255, 214, 80), "h": (255, 248, 196)},
    "orichalcum": {"d": (24, 96, 72), "m": (52, 164, 120), "l": (112, 214, 160), "h": (210, 255, 226)},
}
GEMS = {
    "star_lapis": {"d": (20, 36, 120), "m": (44, 80, 200), "l": (100, 150, 250), "h": (230, 240, 255)},
    "aetherium": {"d": (40, 140, 160), "m": (90, 210, 220), "l": (170, 246, 246), "h": (245, 255, 255)},
    "black_aetherium": {"d": (20, 10, 30), "m": (58, 30, 84), "l": (120, 70, 170), "h": (210, 160, 255)},
}


def img16():
    return Image.new("RGBA", (16, 16), (0, 0, 0, 0))


def shade_mask(mask, pal, rnd=None, speckle=0.0):
    """Light from the upper left: pixels whose upper-left neighbour is outside are lit, lower-right ones dark."""
    out = {}
    for (x, y) in mask:
        ul = (x - 1, y) not in mask or (x, y - 1) not in mask
        dr = (x + 1, y) not in mask or (x, y + 1) not in mask
        ul2 = (x - 2, y) not in mask or (x, y - 2) not in mask
        if ul:
            c = pal["h"] if (x + y) % 3 == 0 else pal["l"]
        elif dr:
            c = pal["d"]
        elif ul2:
            c = pal["l"]
        else:
            c = pal["m"]
        if rnd and speckle and rnd.random() < speckle and not ul:
            c = pal["d"] if rnd.random() < 0.6 else pal["l"]
        out[(x, y)] = c
    return out


def outlined(pixels, img=None):
    img = img or img16()
    for (x, y), c in pixels.items():
        for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
            q = (x + dx, y + dy)
            if q not in pixels and 0 <= q[0] < 16 and 0 <= q[1] < 16:
                img.putpixel(q, OUTLINE + (255,))
    for (x, y), c in pixels.items():
        if 0 <= x < 16 and 0 <= y < 16:
            img.putpixel((x, y), tuple(c) + (255,))
    return img


def disc(cx, cy, r):
    return {(x, y) for x in range(16) for y in range(16) if (x - cx) ** 2 + (y - cy) ** 2 <= r * r}


# ---------------------------------------------------------------- ingots, nuggets, raw metal
INGOT = ["................",
         "................",
         "................",
         "................",
         "........tttt....",
         "......tttttttt..",
         "....ttttttttttf.",
         "..ttttttttttfff.",
         ".ttttttttttffff.",
         ".sstttttttffff..",
         ".sssssttffff....",
         "..sssssfff......",
         "....sssf........",
         "................",
         "................",
         "................"]


def ingot(pal):
    pixels = {}
    for y, row in enumerate(INGOT):
        for x, ch in enumerate(row):
            if ch == "t":   # the top face: lit, a bright line along its far edge
                top_edge = y > 0 and INGOT[y - 1][x] == "."
                pixels[(x, y)] = pal["h"] if top_edge else pal["l"] if (x + y) % 5 else pal["h"]
            elif ch == "f":  # the side face, in shade
                pixels[(x, y)] = pal["m"] if INGOT[y][x - 1] == "t" else pal["d"]
            elif ch == "s":  # the front end
                pixels[(x, y)] = pal["m"] if INGOT[y - 1][x] in "t." else pal["d"]
    return outlined(pixels)


def nugget(pal):
    mask = disc(6, 9, 2.4) | disc(10, 7, 2.0) | disc(9, 11, 1.6)
    return outlined(shade_mask(mask, pal))


def raw(pal, seed):
    rnd = random.Random(seed)
    mask = disc(7, 8, 4.2) | disc(10, 6, 3.0) | disc(9, 11, 2.8) | disc(5, 11, 2.2)
    mask = {p for p in mask if not (rnd.random() < 0.12 and (p[0] - 7.5) ** 2 + (p[1] - 8.5) ** 2 > 16)}
    return outlined(shade_mask(mask, pal, rnd, 0.18))


# ---------------------------------------------------------------- gems, shards, powder
def gem(pal):
    """A cut gem seen from above: a table in the middle, four facets lit from the upper left."""
    pixels = {}
    for y in range(2, 14):
        for x in range(2, 14):
            dx, dy = x - 7.5, y - 7.5
            if abs(dx) + abs(dy) > 7 or max(abs(dx), abs(dy)) > 5.6:
                continue
            if abs(dx) < 2 and abs(dy) < 2:
                c = pal["l"]
            elif dx < 0 and dy < 0:
                c = pal["h"] if abs(dx) + abs(dy) > 5.5 else pal["l"]
            elif dx >= 0 and dy < 0:
                c = pal["m"]
            elif dx < 0:
                c = pal["m"]
            else:
                c = pal["d"]
            pixels[(x, y)] = c
    pixels[(6, 6)] = pal["h"]
    pixels[(9, 4)] = (255, 255, 255)
    return outlined(pixels)


def shard(pal):
    """A cluster of crystal shards rising to the upper right, each lit on one side."""
    pixels = {}
    for (bx, by, length, w) in ((4, 13, 9, 2), (8, 13, 6, 2), (2, 12, 5, 1)):
        for i in range(length):
            x, y = bx + i // 2, by - i
            for k in range(w + 1):
                pixels[(x + k, y)] = pal["h"] if k == 0 and i > 1 else pal["l"] if k == 0 else pal["m"] if k < w else pal["d"]
        pixels[(bx + length // 2, by - length)] = pal["h"]
    return outlined(pixels)


def powder(pal, seed):
    rnd = random.Random(seed)
    pixels = {}
    for _ in range(26):
        x, y = rnd.randint(3, 12), rnd.randint(7, 13)
        if (x - 7.5) ** 2 / 25 + (y - 10.5) ** 2 / 9 > 1:
            continue
        pixels[(x, y)] = rnd.choice([pal["l"], pal["m"], pal["h"], pal["m"]])
    for x in range(4, 12):
        pixels[(x, 12)] = pal["m"]
        if 5 <= x <= 10:
            pixels[(x, 11)] = pal["l"]
    return outlined(pixels)


# ---------------------------------------------------------------- ores and storage blocks
def stone(deep, seed):
    rnd = random.Random(seed)
    img = img16()
    base = (64, 64, 70) if deep else (126, 126, 126)
    for y in range(16):
        for x in range(16):
            n = rnd.randint(-12, 12)
            if deep and y % 4 == 0:
                n -= 8
            if not deep and rnd.random() < 0.08:
                n -= 22
            img.putpixel((x, y), tuple(max(0, min(255, v + n)) for v in base) + (255,))
    return img


def ore(pal, deep, seed, gemlike=False):
    """Stone with clusters of the material: each cluster outlined in dark, its top-left pixel bright."""
    img = stone(deep, seed)
    rnd = random.Random(seed + 1)
    spots = [(3, 3), (10, 2), (6, 8), (12, 9), (2, 11), (9, 13)]
    for (sx, sy) in spots:
        shape = [(0, 0), (1, 0), (0, 1), (1, 1)] + ([(2, 1), (1, 2)] if rnd.random() < 0.5 else [(-1, 1)])
        cells = {(sx + dx, sy + dy) for dx, dy in shape if 0 <= sx + dx < 16 and 0 <= sy + dy < 16}
        for (x, y) in cells:
            for dx, dy in ((1, 0), (0, 1), (1, 1)):
                q = (x + dx, y + dy)
                if q not in cells and 0 <= q[0] < 16 and 0 <= q[1] < 16:
                    img.putpixel(q, tuple(int(v * 0.45) for v in pal["d"]) + (255,))
        for (x, y) in cells:
            ul = (x - 1, y) not in cells or (x, y - 1) not in cells
            img.putpixel((x, y), (pal["h"] if ul and gemlike else pal["l"] if ul else pal["m"]) + (255,))
    return img


def storage(pal, gemlike=False):
    """A block of the material: a bevelled frame, a panel, rivets for metal or facets for gems."""
    img = img16()
    for y in range(16):
        for x in range(16):
            edge = min(x, y, 15 - x, 15 - y)
            if edge == 0:
                c = pal["l"] if x == 0 or y == 0 else pal["d"]
            elif edge == 1:
                c = pal["h"] if (x == 1 or y == 1) else pal["d"]
            else:
                if gemlike:
                    fx, fy = (x - 2) % 6, (y - 2) % 6
                    c = pal["h"] if fx + fy == 2 else pal["l"] if fx < 3 and fy < 3 else pal["m"] if fx + fy < 7 else pal["d"]
                else:
                    c = pal["m"] if (x + 2 * y) % 11 else pal["l"]
                    if y in (5, 10):
                        c = pal["d"]
            img.putpixel((x, y), tuple(c) + (255,))
    if not gemlike:
        for (x, y) in ((3, 3), (12, 3), (3, 12), (12, 12)):
            img.putpixel((x, y), pal["h"] + (255,))
            img.putpixel((x + 1, y + 1), pal["d"] + (255,))
    return img


def raw_block(pal, seed):
    rnd = random.Random(seed)
    img = img16()
    for y in range(16):
        for x in range(16):
            n = math.sin(x * 1.3 + rnd.random()) + math.cos(y * 1.1 + rnd.random())
            c = pal["l"] if n > 0.9 else pal["m"] if n > -0.3 else pal["d"]
            if rnd.random() < 0.05:
                c = pal["h"]
            img.putpixel((x, y), tuple(c) + (255,))
    return img


def save(img, folder, name):
    img.save(os.path.join(folder, name + ".png"))


# ---------------------------------------------------------------- flasks
def flask(liquid, cork=(150, 100, 60), empty=False, brass=None):
    """A round-bottomed bottle with a neck and a cork, its liquid glowing, a highlight on the glass."""
    glass = (210, 230, 240)
    pixels = {}
    body = disc(7.5, 10, 4.6)
    for p in body:
        pixels[p] = glass
    for y in range(3, 6):
        for x in (7, 8):
            pixels[(x, y)] = glass
    if not empty:
        for (x, y) in body:
            if y >= 8:
                depth = (y - 8) / 6
                pixels[(x, y)] = tuple(int(c * (1 - 0.35 * depth)) for c in liquid)
        for x in range(5, 11):
            if (x, 8) in body:
                pixels[(x, 8)] = tuple(min(255, int(c * 1.25) + 30) for c in liquid)
    for (x, y) in ((5, 9), (5, 10), (6, 8)):
        pixels[(x, y)] = (255, 255, 255)
    for x in (6, 7, 8, 9):
        pixels[(x, 2)] = cork if not brass else brass["m"]
    for x in (7, 8):
        pixels[(x, 1)] = cork if not brass else brass["l"]
    if brass:  # a brass collar on the neck
        for x in (6, 7, 8, 9):
            pixels[(x, 5)] = brass["l"] if x < 8 else brass["d"]
    return outlined(pixels)


# ---------------------------------------------------------------- the empire shields, on the vanilla shield's layout
def shield_texture(name, rim, field, emblem, back=(92, 62, 36)):
    """64x64: the plate is a 12x22x1 box at (0,0), its front face at (1,1)-(12,22); the handle a box at (26,0)."""
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    rnd = random.Random(hash(name) & 0xFFFF)
    for y in range(0, 24):
        for x in range(0, 28):
            img.putpixel((x, y), tuple(int(v * (0.85 + 0.15 * rnd.random())) for v in back) + (255,))  # wood everywhere
    for y in range(1, 23):          # the front face
        for x in range(1, 13):
            fx, fy = x - 1, y - 1
            border = fx == 0 or fx == 11 or fy == 0 or fy == 21
            inner = fx == 1 or fx == 10 or fy == 1 or fy == 20
            if border:
                c = rim["l"] if fx == 0 or fy == 0 else rim["d"]
            elif inner:
                c = rim["m"]
            else:
                shade = 1.08 if fx < 5 and fy < 8 else 0.86 if fx > 7 or fy > 15 else 1.0
                c = tuple(min(255, int(v * shade)) for v in field)
            img.putpixel((x, y), tuple(c) + (255,))
    emblem(img)
    for (x, y) in ((2, 2), (11, 2), (2, 21), (11, 21)):
        img.putpixel((x, y), rim["h"] + (255,))
    for y in range(0, 12):          # the handle
        for x in range(26, 38):
            img.putpixel((x, y), (54, 36, 24, 255) if (x + y) % 4 else (72, 50, 32, 255))
    img.save(os.path.join(SHIELD, name + ".png"))


def emblem_crescent(img):
    gold = (250, 210, 80)
    for (x, y) in ((6, 7), (5, 8), (5, 9), (4, 10), (4, 11), (4, 12), (5, 13), (5, 14), (6, 15), (7, 7), (7, 15)):
        img.putpixel((x + 1, y), gold + (255,))
    for (x, y) in ((9, 10), (10, 11), (9, 12), (8, 11), (10, 9)):
        img.putpixel((x, y), (255, 240, 170, 255))


def emblem_knot(img):
    white = (240, 234, 222)
    for i in range(-3, 4):
        for (x, y) in ((6 + i, 11 + i), (6 + i, 11 - i)):
            img.putpixel((x + 1, y), white + (255,))
    for (x, y) in ((7, 5), (7, 17), (2, 11), (12, 11)):
        img.putpixel((x, y), white + (255,))
    img.putpixel((7, 11), (150, 150, 160, 255))


def emblem_star(img):
    brass, turq = (236, 188, 92), (70, 220, 200)
    for i in range(-3, 4):
        img.putpixel((7, 11 + i), brass + (255,))
        img.putpixel((7 + i, 11), brass + (255,))
    for d in (-1, 1):
        img.putpixel((7 + d, 11 + d), brass + (255,))
        img.putpixel((7 + d, 11 - d), brass + (255,))
    img.putpixel((7, 11), turq + (255,))


def emblem_eye(img):
    glow, pupil = (200, 120, 255), (20, 8, 30)
    for x in range(3, 12):
        y = 11 - int(2.5 * math.sin((x - 3) / 8 * math.pi))
        img.putpixel((x, y), glow + (255,))
        img.putpixel((x, 22 - y), glow + (255,))
    for (x, y) in ((7, 10), (7, 11), (7, 12), (6, 11), (8, 11)):
        img.putpixel((x, y), glow + (255,))
    img.putpixel((7, 11), pupil + (255,))


# ---------------------------------------------------------------- the Imperial Forge and the brazier
def forge_side():
    img = img16()
    rnd = random.Random(31)
    for y in range(16):
        for x in range(16):
            row = y // 4
            brick = (x + (2 if row % 2 else 0)) % 8 == 0 or y % 4 == 0
            c = (70, 66, 64) if brick else tuple(v + rnd.randint(-8, 8) for v in (118, 112, 106))
            img.putpixel((x, y), c + (255,))
    for x in range(16):   # a brass band under the hood
        img.putpixel((x, 0), (238, 188, 92, 255))
        img.putpixel((x, 1), (190, 134, 46, 255))
    for y in range(7, 14):  # the mouth of the forge, glowing
        for x in range(4, 12):
            arch = y == 7 and (x in (4, 11))
            if arch:
                continue
            heat = (y - 7) / 6
            c = (255, int(220 - 120 * heat), int(80 - 60 * heat))
            if y == 13:
                c = (60, 20, 10) if x % 2 else (255, 120, 30)
            img.putpixel((x, y), c + (255,))
    for x in range(3, 13):
        img.putpixel((x, 6), (40, 36, 36, 255))
    return img


def forge_top():
    img = img16()
    for y in range(16):
        for x in range(16):
            grate = x % 3 == 0 or y % 3 == 0
            c = (52, 50, 54) if grate else (255, 140 + (x * y) % 60, 40)
            img.putpixel((x, y), c + (255,))
    return img


def brazier_iron():
    img = img16()
    rnd = random.Random(7)
    for y in range(16):
        for x in range(16):
            n = rnd.randint(-10, 10)
            c = (70 + n, 66 + n, 68 + n) if (x + y) % 7 else (110, 104, 100)
            if y in (0, 15):
                c = (46, 42, 44)
            img.putpixel((x, y), c + (255,))
    for x in range(1, 16, 3):
        img.putpixel((x, 7), (150, 140, 130, 255))  # rivets
    return img


def coals():
    img = img16()
    rnd = random.Random(9)
    for y in range(16):
        for x in range(16):
            c = rnd.choice([(40, 30, 28), (255, 120, 30), (200, 60, 20), (60, 40, 36), (255, 200, 80)])
            img.putpixel((x, y), c + (255,))
    return img


def write_json(path, data):
    with open(path, "w", newline="\n") as f:
        json.dump(data, f, indent=2)
        f.write("\n")


def cube_faces(tex, uv=None):
    face = {"texture": tex}
    if uv:
        face["uv"] = uv
    return {d: dict(face) for d in ("north", "south", "east", "west", "up", "down")}


def forge_model():
    write_json(os.path.join(MODELS, "block", "imperial_forge.json"), {
        "parent": "block/block", "render_type": "minecraft:cutout",
        "textures": {"particle": "sofe:block/imperial_forge_side", "side": "sofe:block/imperial_forge_side",
                     "top": "sofe:block/imperial_forge_top", "iron": "minecraft:block/anvil", "brass": "sofe:block/sulthari_brass_plating"},
        "elements": [
            {"from": [0, 0, 0], "to": [16, 11, 16], "faces": {**{d: {"texture": "#side", "uv": [0, 5, 16, 16]} for d in ("north", "south", "east", "west")},
                                                               "up": {"texture": "#top"}, "down": {"texture": "#iron"}}},
            {"from": [1, 11, 1], "to": [15, 13, 15], "faces": cube_faces("#iron")},
            {"from": [3, 13, 3], "to": [13, 14, 13], "faces": cube_faces("#iron")},
            {"from": [5, 14, 5], "to": [11, 16, 11], "faces": cube_faces("#brass")},
            {"from": [-0.5, 10, -0.5], "to": [16.5, 11, 16.5], "faces": cube_faces("#brass")},
        ]})
    write_json(os.path.join(MODELS, "item", "imperial_forge.json"), {"parent": "sofe:block/imperial_forge"})


def brazier_model():
    legs = [{"from": [x, 0, z], "to": [x + 2, 6, z + 2], "faces": cube_faces("#iron")} for x, z in ((2, 2), (12, 2), (2, 12), (12, 12))]
    bowl = [
        {"from": [1, 6, 1], "to": [15, 8, 15], "faces": cube_faces("#iron")},
        {"from": [1, 8, 1], "to": [15, 11, 2], "faces": cube_faces("#iron")},
        {"from": [1, 8, 14], "to": [15, 11, 15], "faces": cube_faces("#iron")},
        {"from": [1, 8, 2], "to": [2, 11, 14], "faces": cube_faces("#iron")},
        {"from": [14, 8, 2], "to": [15, 11, 14], "faces": cube_faces("#iron")},
        {"from": [2, 8, 2], "to": [14, 9, 14], "faces": {"up": {"texture": "#coals"}}},
    ]
    fire = [
        {"from": [2, 9, 8], "to": [14, 20, 8], "shade": False, "rotation": {"origin": [8, 8, 8], "axis": "y", "angle": 45},
         "faces": {"north": {"texture": "#fire"}, "south": {"texture": "#fire"}}},
        {"from": [8, 9, 2], "to": [8, 20, 14], "shade": False, "rotation": {"origin": [8, 8, 8], "axis": "y", "angle": 45},
         "faces": {"east": {"texture": "#fire"}, "west": {"texture": "#fire"}}},
    ]
    write_json(os.path.join(MODELS, "block", "nordrath_iron_brazier.json"), {
        "parent": "block/block", "render_type": "minecraft:cutout", "ambientocclusion": False,
        "textures": {"particle": "sofe:block/nordrath_iron_brazier", "iron": "sofe:block/nordrath_iron_brazier",
                     "coals": "sofe:block/brazier_coals", "fire": "minecraft:block/campfire_fire"},
        "elements": legs + bowl + fire})
    write_json(os.path.join(MODELS, "item", "nordrath_iron_brazier.json"), {"parent": "sofe:block/nordrath_iron_brazier"})


if __name__ == "__main__":
    for i, (name, pal) in enumerate(METALS.items()):
        save(ingot(pal), ITEM, name + "_ingot")
        save(nugget(pal), ITEM, name + "_nugget")
        save(raw(pal, i), ITEM, "raw_" + name)
        save(raw_block(pal, i), BLOCK, "raw_" + name + "_block")
        save(storage(pal), BLOCK, name + "_block")
        if os.path.exists(os.path.join(BLOCK, name + "_ore.png")):
            save(ore(pal, False, 10 + i), BLOCK, name + "_ore")
        if os.path.exists(os.path.join(BLOCK, "deepslate_" + name + "_ore.png")):
            save(ore(pal, True, 20 + i), BLOCK, "deepslate_" + name + "_ore")
    save(powder(METALS["solar_gold"], 3), ITEM, "solar_gold_powder")
    save(gem(GEMS["star_lapis"]), ITEM, "star_lapis")
    save(powder(GEMS["star_lapis"], 4), ITEM, "star_lapis_powder")
    save(ore(GEMS["star_lapis"], False, 40, True), BLOCK, "star_lapis_ore")
    save(storage(GEMS["star_lapis"], True), BLOCK, "star_lapis_block")
    save(shard(GEMS["aetherium"]), ITEM, "aetherium_shard")
    save(shard(GEMS["black_aetherium"]), ITEM, "black_aetherium_shard")
    save(ore(GEMS["aetherium"], True, 41, True), BLOCK, "deepslate_aetherium_ore")
    save(storage(GEMS["aetherium"], True), BLOCK, "aetherium_block")

    save(flask((214, 40, 50), brass=METALS["sulthari_brass"]), ITEM, "bearers_flask")
    save(flask((70, 140, 240)), ITEM, "bearers_tonic")
    save(flask((200, 30, 60)), ITEM, "minor_pomegranate_elixir")
    save(flask((0, 0, 0), empty=True, brass=METALS["sulthari_brass"]), ITEM, "brass_flask")

    shield_texture("sulthari_shield", METALS["solar_gold"], (176, 32, 38), emblem_crescent)
    shield_texture("nordrath_shield", {"d": (60, 60, 66), "m": (110, 110, 120), "l": (170, 170, 180), "h": (230, 230, 235)},
                   (150, 24, 30), emblem_knot)
    shield_texture("observatory_shield", METALS["sulthari_brass"], (44, 78, 160), emblem_star)
    shield_texture("void_shield", GEMS["black_aetherium"], (60, 24, 96), emblem_eye, back=(30, 22, 36))

    save(forge_side(), BLOCK, "imperial_forge_side")
    save(forge_top(), BLOCK, "imperial_forge_top")
    save(brazier_iron(), BLOCK, "nordrath_iron_brazier")
    save(coals(), BLOCK, "brazier_coals")
    forge_model()
    brazier_model()
    print("art pass written")
