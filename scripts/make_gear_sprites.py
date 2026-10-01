"""Refined 16x16 sprites for SoFE gear, in Minecraft's own item style: a dark outline around every
shape, a light edge and a shaded edge on each blade, and material palettes (brass, glacial iron,
the Relics' fire, blood and ash). Replaces the first placeholders of make_sprint55_assets.py.

Run from the repository root: python scripts/make_gear_sprites.py
"""
import math
import os
from PIL import Image

ITEM = os.path.join("src", "main", "resources", "assets", "sofe", "textures", "item")

# palettes: outline, dark, mid, light, highlight
BRASS = [(58, 36, 10), (138, 92, 32), (190, 138, 56), (230, 186, 96), (255, 232, 160)]
GLACIAL = [(30, 46, 66), (92, 128, 160), (142, 182, 212), (196, 226, 244), (240, 252, 255)]
STEEL = [(36, 36, 40), (110, 112, 120), (158, 160, 168), (206, 208, 214), (245, 245, 250)]
GOLD = [(70, 44, 6), (178, 120, 26), (226, 168, 48), (250, 214, 96), (255, 244, 190)]
WOOD = [(40, 24, 12), (86, 54, 28), (120, 80, 44), (150, 104, 60), (180, 132, 80)]
FIRE = [(60, 14, 4), (170, 40, 10), (232, 96, 24), (255, 160, 52), (255, 226, 140)]
BLOOD = [(46, 6, 10), (120, 14, 24), (170, 24, 36), (214, 58, 66), (250, 140, 140)]
ASH = [(14, 10, 10), (42, 30, 28), (66, 46, 40), (96, 70, 60), (140, 110, 96)]
TURQUOISE, SKY, RUBY, AMBER = (60, 210, 196), (110, 200, 245), (210, 40, 60), (255, 176, 40)


class Sprite:
    def __init__(self):
        self.px = {}

    def put(self, x, y, color):
        if 0 <= x < 16 and 0 <= y < 16:
            self.px[(x, y)] = color

    def outline(self, color):
        """Dark outline on every empty pixel touching the shape (4 neighbours), like vanilla items."""
        for (x, y) in list(self.px):
            for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                p = (x + dx, y + dy)
                if p not in self.px and 0 <= p[0] < 16 and 0 <= p[1] < 16:
                    self.px.setdefault(("o",) + p, color)
        for key in [k for k in self.px if k[0] == "o"]:
            self.px[(key[1], key[2])] = self.px.pop(key)

    def save(self, name):
        img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
        for (x, y), c in self.px.items():
            img.putpixel((x, y), tuple(c) + (255,))
        img.save(os.path.join(ITEM, name + ".png"))


def diagonal(s, x0, y0, length, pal, width=2, curve=0.0, tip=True):
    """A blade going up and to the right from (x0, y0): highlight on the upper edge, shade below."""
    for i in range(length):
        bend = round(curve * math.sin(math.pi * i / max(1, length - 1)))
        x, y = x0 + i - bend, y0 - i - bend
        s.put(x, y, pal[3])            # the edge catching the light
        s.put(x + 1, y, pal[2])
        if width >= 2:
            s.put(x + 1, y + 1, pal[1])  # the shaded side
        if width >= 3:
            s.put(x + 2, y + 1, pal[1])
        if i % 3 == 1:
            s.put(x, y, pal[4])        # small glints along the edge
    if tip:
        bend = round(curve * math.sin(math.pi))
        s.put(x0 + length - bend, y0 - length - bend, pal[3])


def hilt(s, cx, cy, guard, grip, pommel, wide=2):
    """Cross guard across the blade's base, a wrapped grip and a pommel."""
    for k in range(-wide, wide + 1):
        s.put(cx + k, cy + k, guard[2] if k else guard[3])
    s.put(cx - wide, cy - wide, guard[1])
    s.put(cx + wide, cy + wide, guard[1])
    for k in (1, 2):
        s.put(cx - k, cy + k, grip[2] if k % 2 else grip[1])
    s.put(cx - 3, cy + 3, pommel[3])
    s.put(cx - 3, cy + 4, pommel[2])
    s.put(cx - 4, cy + 3, pommel[2])


def sword(name, blade, guard, length=9, curve=0.0, width=2, gem=None):
    s = Sprite()
    diagonal(s, 5, 10, length, blade, width, curve)
    hilt(s, 4, 11, guard, WOOD, guard)
    if gem:
        s.put(4, 11, gem)
    s.outline(blade[0])
    s.save(name)


def staff(name, shaft, head, gem, ankh=False):
    s = Sprite()
    for i in range(10):
        x, y = 2 + i, 14 - i
        s.put(x, y, shaft[3] if i % 4 == 0 else shaft[2])
        s.put(x + 1, y, shaft[1])
    cx, cy = 12, 3
    if ankh:
        for x, y in ((11, 1), (12, 0), (13, 1), (10, 2), (14, 2), (11, 3), (13, 3)):
            s.put(x, y, head[3])     # the loop of the ankh
        for x in range(9, 16):
            s.put(x, 5, head[2])     # its crossbar
        s.put(12, 4, head[2])
        s.put(12, 2, gem)
    else:
        for a in range(0, 360, 30):
            x, y = cx + round(2.6 * math.cos(math.radians(a))), cy + round(2.6 * math.sin(math.radians(a)))
            s.put(x, y, head[3] if a < 180 else head[2])   # the astrolabe ring
        s.put(cx, cy, gem)
        s.put(cx - 1, cy, head[1])
        s.put(cx + 1, cy, head[1])
        s.put(cx, cy - 3, head[4])
    s.outline(head[0])
    s.save(name)


def axe(name, head, shaft, crack=None):
    s = Sprite()
    for i in range(12):
        x, y = 2 + i, 14 - i
        s.put(x, y, shaft[2] if i % 3 else shaft[3])
    # a crescent blade on the upper part of the haft
    for x in range(8, 15):
        for y in range(0, 9):
            d = math.hypot(x - 15.5, y - 4.5)
            if 3.0 <= d <= 7.0 and x + y >= 12:
                color = head[3] if d > 6.0 else head[2] if d > 4.5 else head[1]
                s.put(x, y, color)
    if crack:
        for x, y in ((10, 3), (11, 5), (12, 2), (11, 7)):
            s.put(x, y, crack)
    s.outline(head[0])
    s.save(name)


PICK = ["................",
        "....OOOOOOO.....",
        "...OHLLLLLMO....",
        "..OLMOOOOOMDO...",
        "..OMO....OWMDO..",
        "..OO....OWO.ODO.",
        "........OWO..OO.",
        ".......OWO......",
        "......OWO.......",
        ".....OWO........",
        "....OWO.........",
        "...OWO..........",
        "..OWO...........",
        ".OWO............",
        ".OO.............",
        "................"]


def pickaxe(name, head, shaft):
    """Vanilla-shaped pick: a curved head over a diagonal haft."""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    colors = {"O": head[0], "D": head[1], "M": head[2], "L": head[3], "H": head[4], "W": shaft[2]}
    for y, row in enumerate(PICK):
        for x, ch in enumerate(row):
            if ch in colors:
                c = colors[ch]
                if ch == "W" and (x + y) % 4 == 0:
                    c = shaft[3]
                if ch == "O" and y > 4:
                    c = shaft[0] if x < 9 else head[0]
                img.putpixel((x, y), tuple(c) + (255,))
    img.save(os.path.join(ITEM, name + ".png"))


# --- weapons and tools
sword("brass_scimitar", BRASS, GOLD, length=9, curve=1.6, gem=RUBY)
sword("brass_dagger", BRASS, GOLD, length=5)
sword("brass_longsword", STEEL, BRASS, length=10, width=2, gem=TURQUOISE)
sword("glacial_iron_sword", GLACIAL, GLACIAL, length=10, width=3, gem=SKY)
staff("brass_staff", WOOD, BRASS, SKY)
staff("brass_ankh_rod", WOOD, BRASS, TURQUOISE, ankh=True)
staff("glacial_iron_staff", GLACIAL, GLACIAL, (220, 250, 255))
axe("glacial_iron_greataxe", GLACIAL, WOOD)
pickaxe("brass_pickaxe", BRASS, WOOD)
pickaxe("glacial_iron_pickaxe", GLACIAL, WOOD)

# --- Relics
sword("kaleth_blade", FIRE, GOLD, length=10, width=3, gem=AMBER)
s = Sprite(); diagonal(s, 5, 10, 7, BLOOD, 2, curve=1.2); hilt(s, 4, 11, GOLD, ASH, GOLD); s.put(4, 11, RUBY)
s.put(9, 8, BLOOD[2]); s.put(9, 9, BLOOD[1])        # a drop of blood under the blade
s.outline(BLOOD[0]); s.save("serath_fang")
axe("vorath_wrath", ASH, ASH, crack=FIRE[3])

# --- armor icons (Minecraft's armor silhouettes, shaded, with a gold trim on brass)
SHAPES = {
    "helmet": ["....OOOOOOOO....", "...OHLLLLLLMO...", "..OHLMMMMMMMDO..", "..OLMMTTTTMMDO..", "..OLMOOOOOOMDO..",
               "..OLMO....OMDO..", "..OLMO....OMDO..", "..ODDO....ODDO..", "..OOOO....OOOO.."],
    "chestplate": ["..OOOO....OOOO..", "..OHLO....OLMO..", ".OLMLOOOOOOLMDO.", ".OLMMLLLLLLMMDO.", ".OOLMMTTTTMMDOO.",
                   "...OLMMMMMMDO...", "...OLMMMMMMDO...", "...OLMMTTMMDO...", "...OLMMMMMMDO...", "...OLMMMMMMDO...",
                   "...ODDDDDDDDO...", "...OOOOOOOOOO..."],
    "leggings": ["...OOOOOOOOOO...", "...OTTTTTTTTO...", "...OLMMMMMMDO...", "...OLMOOOOMDO...", "...OLMO..OMDO...",
                 "...OLMO..OMDO...", "...OLMO..OMDO...", "...OLMO..OMDO...", "...ODDO..ODDO...", "...OOOO..OOOO..."],
    "boots": ["..OOOO....OOOO..", "..OLMO....OLMO..", "..OLMO....OLMO..", "..OLMO....OLMO..", "..OLMDO...OLMDO.",
              ".OLMMDO..OLMMDO.", ".OTTTTO..OTTTTO.", ".OOOOOO..OOOOOO."],
}


def armor(name, pal, trim, kind):
    rows = SHAPES[kind]
    top = (16 - len(rows)) // 2
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    colors = {"O": pal[0], "D": pal[1], "M": pal[2], "L": pal[3], "H": pal[4], "T": trim}
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch in colors:
                img.putpixel((x, top + y), tuple(colors[ch]) + (255,))
    img.save(os.path.join(ITEM, name + ".png"))


for kind in SHAPES:
    armor(f"brass_{kind}", BRASS, GOLD[3], kind)
    armor(f"glacial_iron_{kind}", GLACIAL, GLACIAL[4], kind)

# --- jewelry and talismans
s = Sprite()
for a in range(200, 341, 10):
    s.put(round(8 + 5 * math.cos(math.radians(a))), round(7 + 5 * math.sin(math.radians(a))), GOLD[2])   # the chain
for x, y in ((7, 9), (8, 9), (7, 10), (8, 10), (7, 11), (8, 11)):
    s.put(x, y, BRASS[3] if y == 9 else BRASS[2])
s.put(7, 10, TURQUOISE); s.put(8, 10, (120, 240, 230))
s.outline(BRASS[0]); s.save("brass_amulet")
s = Sprite()
for a in range(0, 360, 20):
    s.put(round(8 + 4 * math.cos(math.radians(a))), round(9 + 3.4 * math.sin(math.radians(a))), GOLD[3] if a < 180 else GOLD[1])
s.put(7, 4, RUBY); s.put(8, 4, (240, 90, 110)); s.put(7, 5, GOLD[2]); s.put(8, 5, GOLD[2])
s.outline(GOLD[0]); s.save("brass_ring")
s = Sprite()
for x, y in ((8, 4), (7, 5), (8, 5), (9, 5), (6, 6), (7, 6), (8, 6), (9, 6), (10, 6), (7, 7), (8, 7), (9, 7), (8, 8)):
    s.put(x, y, (176, 140, 224) if x <= 8 else (120, 90, 170))
s.put(8, 6, (240, 220, 255)); s.outline((44, 30, 70)); s.save("small_talisman")
s = Sprite()
for x in range(4, 12):
    for y in range(3, 13):
        s.put(x, y, (130, 96, 180) if x < 8 else (100, 72, 150))
for k in range(4, 12):
    s.put(7, k - 1 if k < 12 else 11, GOLD[3])
for x in range(5, 11):
    s.put(x, 7, GOLD[3])
s.put(7, 7, AMBER); s.outline((40, 26, 64)); s.save("large_talisman")
print("gear sprites written")
