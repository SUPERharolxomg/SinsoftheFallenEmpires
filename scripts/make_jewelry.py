"""Sprites of the rings, necklaces and amulets (random bases and the unique ones) and of the gambler's veiled
wares, in the style of the reference sheets: a dark outline, metal lit from the upper left, saturated gems
with a bright facet.

Rings are a band seen from above at an angle with a set stone; necklaces a chain hanging in a U with a
pendant at the bottom. Run from the repository root: python scripts/make_jewelry.py
"""
import math
import os

from PIL import Image

ITEM = os.path.join("src", "main", "resources", "assets", "sofe", "textures", "item")
OUTLINE = (20, 14, 18)

SILVER = [(90, 94, 108), (160, 166, 180), (214, 218, 228), (250, 252, 255)]
GOLD = [(140, 92, 18), (220, 164, 40), (250, 210, 90), (255, 244, 196)]
BRASS = [(120, 78, 26), (190, 136, 52), (236, 188, 96), (255, 236, 170)]
ICE = [(40, 110, 170), (100, 190, 236), (176, 232, 252), (240, 252, 255)]
ORI = [(20, 90, 50), (50, 160, 90), (120, 220, 140), (210, 255, 210)]
BONE = [(130, 116, 90), (196, 182, 150), (230, 220, 196), (250, 246, 232)]
OBSIDIAN = [(14, 10, 20), (40, 28, 56), (76, 58, 104), (140, 120, 180)]
LEATHER = [(60, 30, 18), (110, 60, 34), (150, 92, 56), (180, 124, 80)]
AETH = [(120, 130, 170), (190, 200, 235), (230, 236, 255), (255, 255, 255)]
SNAKE = [(20, 70, 30), (50, 130, 50), (110, 190, 80), (190, 240, 150)]

RED, TURQ, BLUE, PURPLE, GREEN, WHITE, YELLOW, CYAN, AMBER = ((230, 40, 50), (50, 220, 200), (60, 110, 240), (180, 90, 250),
                                                             (70, 220, 90), (240, 250, 255), (255, 230, 80), (110, 240, 255),
                                                             (255, 160, 40))


class Canvas:
    def __init__(self):
        self.px = {}

    def put(self, x, y, c):
        if 0 <= x < 16 and 0 <= y < 16:
            self.px[(x, y)] = c

    def save(self, name):
        for (x, y) in list(self.px):
            for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                q = (x + dx, y + dy)
                if 0 <= q[0] < 16 and 0 <= q[1] < 16 and q not in self.px:
                    self.px.setdefault(("o",) + q, OUTLINE)
        for k in [k for k in self.px if k[0] == "o"]:
            self.px[(k[1], k[2])] = self.px.pop(k)
        img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
        for (x, y), c in self.px.items():
            img.putpixel((x, y), tuple(c) + (255,))
        img.save(os.path.join(ITEM, name + ".png"))


def lighten(c, t):
    return tuple(min(255, round(v + (255 - v) * t)) for v in c)


def darken(c, t):
    return tuple(round(v * (1 - t)) for v in c)


def gem(cv, cx, cy, color, size=1):
    """A cut stone: a bright facet top left, the color, a shade bottom right."""
    for dy in range(-size, size + 1):
        for dx in range(-size, size + 1):
            if abs(dx) + abs(dy) > size + (size > 1):
                continue
            c = color
            if dx + dy < 0:
                c = lighten(color, 0.35)
            elif dx + dy > 0:
                c = darken(color, 0.3)
            cv.put(cx + dx, cy + dy, c)
    cv.put(cx - (size > 0), cy - (size > 0), lighten(color, 0.8))


def ring(name, metal, stone, wide=False, setting=None):
    cv = Canvas()
    cx, cy, rx, ry = 7.5, 10, 5.6, 3.6
    for y in range(16):
        for x in range(16):
            d = math.hypot((x - cx) / rx, (y - cy) / ry)
            inner = 0.52 if wide else 0.66
            if inner <= d <= 1.0:
                top = y < cy
                c = metal[2] if top else metal[1]
                if d > 0.9:
                    c = metal[0] if not top else metal[1]
                if top and d < inner + 0.12:
                    c = metal[3]
                cv.put(x, y, c)
    setting = setting or metal
    for p in ((6, 6), (9, 6), (6, 4), (9, 4)):
        cv.put(p[0], p[1], setting[2])
    gem(cv, 7, 4, stone, 1)
    gem(cv, 8, 4, stone, 1)
    cv.put(7, 3, lighten(stone, 0.8))
    cv.save(name)


def chain(cv, metal, bottom=9):
    """A chain hanging from the top corners down to the pendant, links alternating light and dark."""
    for i, x in enumerate(range(2, 14)):
        t = (x - 7.5) / 5.5
        y = round(1 + (bottom - 1) * (1 - t * t))
        cv.put(x, y, metal[2] if i % 2 else metal[1])
        if abs(t) > 0.6:
            cv.put(x, y - 1 if t < 0 else y - 1, metal[0])


def necklace(name, metal, pendant):
    cv = Canvas()
    chain(cv, metal)
    pendant(cv)
    cv.save(name)


# --- pendants
def drop(stone, frame):
    def draw(cv):
        for y in range(9, 15):
            w = [1, 2, 2, 2, 1, 0][y - 9]
            for x in range(8 - w, 8 + w):
                cv.put(x, y, frame[1] if x in (8 - w, 8 + w - 1) else stone)
        gem(cv, 7, 11, stone, 1)
        cv.put(6, 10, lighten(stone, 0.8))
        cv.put(7, 9, frame[3])
    return draw


def fangs(cv):
    for x, h in ((5, 3), (7, 5), (9, 4), (11, 3)):
        for y in range(8, 8 + h):
            cv.put(x, y, BONE[2] if y < 8 + h - 1 else BONE[1])
        cv.put(x, 8, BONE[3])
    cv.put(8, 9, RED)


def scarab(cv):
    rows = [".tt.", "gggg", "gGgg", "gggg", ".gg.", "g..g"]
    for dy, row in enumerate(rows):
        for dx, ch in enumerate(row):
            c = {"t": GOLD[2], "g": TURQ, "G": lighten(TURQ, 0.7)}.get(ch)
            if c:
                cv.put(6 + dx, 9 + dy, c)
    cv.put(7, 11, darken(TURQ, 0.4))
    cv.put(8, 12, darken(TURQ, 0.4))


def laurel(cv):
    for a in range(0, 360, 30):
        x = round(7.5 + 3.2 * math.cos(math.radians(a)))
        y = round(11.5 + 3.2 * math.sin(math.radians(a)))
        cv.put(x, y, GOLD[2] if a < 180 else GOLD[1])
    gem(cv, 7, 11, RED, 1)


def crystal(cv):
    for y in range(8, 15):
        w = [0, 1, 1, 2, 1, 1, 0][y - 8]
        for x in range(8 - w, 9 + w):
            cv.put(x, y, AETH[2] if x <= 8 else AETH[1])
    cv.put(8, 9, AETH[3])
    cv.put(7, 11, CYAN)


def eye(cv):
    rows = ["..gggg..", ".gwwwwg.", "gwppPpwg", ".gwwwwg.", "..gggg.."]
    for dy, row in enumerate(rows):
        for dx, ch in enumerate(row):
            c = {"g": GOLD[2], "w": (236, 226, 210), "p": PURPLE, "P": (40, 10, 60)}.get(ch)
            if c:
                cv.put(4 + dx, 9 + dy, c)
    cv.put(7, 11, (20, 6, 30))
    cv.put(8, 10, lighten(PURPLE, 0.8))


def coil(cv):
    for a in range(0, 540, 25):
        r = 3.4 - a / 300
        x = round(7.5 + r * math.cos(math.radians(a)))
        y = round(11.5 + r * math.sin(math.radians(a)) * 0.9)
        cv.put(x, y, SNAKE[2] if a % 50 else SNAKE[1])
    cv.put(7, 11, YELLOW)
    cv.put(8, 11, RED)


def sigil(cv):
    for y in range(9, 15):
        for x in range(5, 11):
            if (x - 7.5) ** 2 + (y - 11.5) ** 2 <= 9:
                cv.put(x, y, (170, 30, 30) if (x + y) % 3 else (120, 16, 20))
    for p in ((6, 10), (7, 11), (8, 12), (8, 11), (9, 13)):
        cv.put(p[0], p[1], (40, 6, 10))   # the crack through the seal
    cv.put(6, 12, (255, 120, 100))


# --- veiled wares: a bundle under cloth with a question mark
QMARK = [".xx.", "x..x", "...x", "..x.", "....", "..x."]


def veiled(name, cloth, tie, long=False):
    cv = Canvas()
    if long:  # a weapon wrapped on the diagonal
        for i in range(11):
            for w in range(3):
                x, y = 2 + i + w - 1, 13 - i + w - 1
                cv.put(x, y, cloth[2] if w == 0 else cloth[1] if w == 1 else cloth[0])
        for i in (3, 7):
            cv.put(2 + i, 13 - i, tie)
            cv.put(3 + i, 13 - i, tie)
        qx, qy = 9, 6
    else:  # a sack tied at the neck
        for y in range(4, 15):
            w = [2, 2, 3, 5, 6, 6, 6, 6, 6, 5, 4][y - 4]
            for x in range(8 - w, 8 + w):
                c = cloth[1]
                if x < 8 - w + 2 and y < 12:
                    c = cloth[2]
                if x >= 8 + w - 2 or y >= 13:
                    c = cloth[0]
                cv.put(x, y, c)
        for x in range(5, 11):
            cv.put(x, 6, tie)
        cv.put(6, 2, cloth[2])
        cv.put(9, 2, cloth[1])
        cv.put(7, 3, cloth[1])
        cv.put(8, 3, cloth[2])
        qx, qy = 6, 8
    for dy, row in enumerate(QMARK):
        for dx, ch in enumerate(row):
            if ch == "x":
                cv.put(qx + dx, qy + dy, (255, 236, 150))
    cv.save(name)


if __name__ == "__main__":
    ring("silver_ring", SILVER, BLUE)
    ring("glacial_ring", ICE, WHITE)
    ring("star_lapis_ring", SILVER, (40, 70, 200))
    ring("solar_gold_ring", GOLD, AMBER)
    ring("orichalcum_band", ORI, GOLD[2], wide=True)
    necklace("turquoise_necklace", SILVER, drop(TURQ, SILVER))
    necklace("bone_necklace", LEATHER, fangs)
    necklace("scarab_amulet", GOLD, scarab)
    necklace("laurel_torc", GOLD, laurel)
    necklace("aetherium_pendant", SILVER, crystal)
    # the unique ones
    necklace("eye_of_the_false_prophet", GOLD, eye)
    ring("ring_of_the_last_caravan", GOLD, TURQ, wide=True)
    ring("soulkeeper_ring", OBSIDIAN, CYAN, setting=SILVER)
    ring("frozen_throne_band", ICE, CYAN, wide=True, setting=SILVER)
    necklace("serpent_coil", SNAKE, coil)
    necklace("broken_pact_sigil", OBSIDIAN, sigil)
    veiled("veiled_weapon", [(70, 40, 24), (120, 76, 44), (160, 112, 70)], (200, 160, 70), long=True)
    veiled("veiled_armor", [(60, 64, 76), (104, 110, 124), (150, 156, 170)], (190, 40, 40))
    veiled("veiled_jewelry", [(46, 20, 70), (90, 44, 130), (140, 90, 190)], (240, 200, 80))
    print("jewelry and veiled wares drawn")
