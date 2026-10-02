"""Sprites of the arsenal, batch 3, and the class items (scripts/arsenal3_catalog.py), drawn at 32x32 from shapes:
each part is a polygon, a stroked curve or an ellipse drawn at four times the size and reduced to pixels, then
lit from the upper left, shaded on the lower right, given its bright edge and outlined. Bows get their three
pull frames and crossbows their pull and loaded frames, after the vanilla ones (the arrow points to the upper
left). Pickaxes and bows are recolored vanilla ones (the bow with its own pull frames).

Run from the repository root: python scripts/make_arsenal3_sprites.py
"""
import math
import os
import sys

from PIL import Image, ImageDraw

sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

import make_heavy_weapons as hw  # noqa: E402
import make_pickaxes as mp  # noqa: E402
from arsenal3_catalog import ITEMS  # noqa: E402

ITEM = os.path.join("src", "main", "resources", "assets", "sofe", "textures", "item")
N, K = 32, 4
OUTLINE = (20, 14, 18)
pal = hw.pal

STEEL, DARK_STEEL, ICE, GOLD, BRASS = hw.STEEL, hw.DARK_STEEL, hw.ICE, hw.GOLD, pal((196, 140, 56))
WOOD, DARK_WOOD, LEATHER, BONE = hw.WOOD, hw.DARK_WOOD, hw.LEATHER, hw.BONE
OBSIDIAN, EMBER, VOID, RED, STORM, MARBLE = hw.OBSIDIAN, hw.EMBER, hw.VOID, hw.RED, hw.STORM, hw.MARBLE
ROYAL, SILK, PAPER, GREEN = pal((40, 70, 170)), pal((140, 30, 40)), pal((236, 226, 200)), pal((60, 140, 60))
STRING = {"d": (150, 150, 150), "m": (220, 220, 220), "l": (240, 240, 240), "h": (255, 255, 255)}
GLOW = {"ember": (255, 140, 40), "frost": (150, 230, 255), "storm": (255, 236, 110), "void": (200, 120, 255), "soul": (100, 250, 230),
        "bone": (236, 226, 200), "holy": (255, 236, 160), "arcane": (130, 160, 255)}


class Canvas:
    def __init__(self):
        self.layers = []

    @staticmethod
    def _mask(draw):
        img = Image.new("L", (N * K, N * K), 0)
        draw(ImageDraw.Draw(img))
        small = img.resize((N, N), Image.BOX)
        return {(x, y) for y in range(N) for x in range(N) if small.getpixel((x, y)) >= 110}

    def poly(self, pts, p, edge=None):
        self.layers.append((self._mask(lambda d: d.polygon([(x * K, y * K) for x, y in pts], fill=255)), p,
                            self._mask(lambda d: d.polygon([(x * K, y * K) for x, y in edge], fill=255)) if edge else set(), None))

    def line(self, pts, width, p):
        def draw(d):
            d.line([(x * K, y * K) for x, y in pts], fill=255, width=int(width * K), joint="curve")
            for x, y in (pts[0], pts[-1]):
                r = width * K / 2
                d.ellipse([x * K - r, y * K - r, x * K + r, y * K + r], fill=255)
        self.layers.append((self._mask(draw), p, set(), None))

    def ellipse(self, cx, cy, rx, ry, p, glow=None):
        m = self._mask(lambda d: d.ellipse([(cx - rx) * K, (cy - ry) * K, (cx + rx) * K, (cy + ry) * K], fill=255))
        self.layers.append((m, p, set(), (cx, cy, rx, ry, glow)))

    def save(self, name):
        out = {}
        for mask, p, edge, orb in self.layers:
            for (x, y) in mask:
                if orb:  # a sphere: light from the upper left, a bright spot, a dark rim
                    cx, cy, rx, ry, glow = orb
                    dx, dy = (x + 0.5 - cx) / rx, (y + 0.5 - cy) / ry
                    d = math.hypot(dx + 0.35, dy + 0.35)
                    c = p["h"] if d < 0.35 else p["l"] if d < 0.7 else p["m"] if d < 1.1 else p["d"]
                    if glow and math.hypot(dx, dy) < 0.45:
                        c = glow
                else:
                    ul = (x - 1, y) not in mask or (x, y - 1) not in mask
                    dr = (x + 1, y) not in mask or (x, y + 1) not in mask
                    c = p["l"] if ul and not dr else p["d"] if dr and not ul else p["m"]
                    if (x, y) in edge:
                        c = p["h"]
                out[(x, y)] = c
        img = Image.new("RGBA", (N, N), (0, 0, 0, 0))
        for (x, y), c in out.items():
            img.putpixel((x, y), tuple(c) + (255,))
        for y in range(N):
            for x in range(N):
                if (x, y) not in out and any((x + dx, y + dy) in out for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1))):
                    img.putpixel((x, y), OUTLINE + (255,))
        img.save(os.path.join(ITEM, name + ".png"))


def frame(start, end):
    """Points u along the line start->end (from end) and v across it (to the upper left)."""
    dx, dy = end[0] - start[0], end[1] - start[1]
    n = math.hypot(dx, dy)
    d, nn = (dx / n, dy / n), (dy / n, -dx / n)
    return lambda u, v: (end[0] + u * d[0] + v * nn[0], end[1] + u * d[1] + v * nn[1])


def solid(c):
    return {"d": tuple(int(v * 0.6) for v in c), "m": c, "l": tuple(min(255, v + 40) for v in c), "h": tuple(min(255, v + 90) for v in c)}


def bezier(a, c, b, steps=24):
    return [((1 - t) ** 2 * a[0] + 2 * (1 - t) * t * c[0] + t * t * b[0], (1 - t) ** 2 * a[1] + 2 * (1 - t) * t * c[1] + t * t * b[1])
            for t in (i / steps for i in range(steps + 1))]


# ------------------------------------------------------------------------------------------------ pieces
def diag_haft(cv, at, p, start=(3.5, 28.5), end=(26, 6), width=1.1, wraps=None):
    f = frame(start, end)
    length = math.hypot(end[0] - start[0], end[1] - start[1])
    cv.poly([f(-length, width), f(0, width), f(0, -width), f(-length, -width)], p)
    if wraps:
        for k in range(3):
            u0 = -length + 2 + k * 4
            cv.poly([f(u0, width + 0.3), f(u0 + 1.6, width + 0.3), f(u0 + 1.6, -width - 0.3), f(u0, -width - 0.3)], wraps)
    return f


def javelin(name, metal, haft):
    cv = Canvas()
    f = diag_haft(cv, 0, haft, start=(2, 30), end=(25, 7), width=0.9, wraps=LEATHER)
    cv.poly([f(-1, 2.0), f(6.5, 0), f(-1, -2.0), f(-3, 0)], metal, [f(6.5, 0), f(-1, 2.0), f(0, 1.2)])
    cv.save(name)


def knife(name, metal, grip):
    cv = Canvas()
    f = frame((7, 25), (26, 6))
    cv.poly([f(-19, 1.2), f(-12, 1.2), f(-12, -1.2), f(-19, -1.2)], grip)
    cv.poly([f(-12, 3), f(-10.5, 3), f(-10.5, -3), f(-12, -3)], DARK_STEEL)
    cv.poly([f(-10.5, 2.0), f(-2, 1.6), f(1, 0), f(-10.5, -1.4)], metal, [f(-10.5, 2.0), f(-2, 1.6), f(1, 0), f(-2, 0.9)])
    cv.save(name)


def staff(name, kind):
    cv = Canvas()
    glow = GLOW[kind]
    shaft = {"ember": DARK_WOOD, "frost": STEEL, "storm": DARK_WOOD, "void": OBSIDIAN, "soul": DARK_WOOD}[kind]
    f = diag_haft(cv, 0, shaft, start=(2, 30), end=(22, 10), width=1.0, wraps=GOLD if kind != "frost" else ICE)
    if kind == "ember":
        cv.poly([f(-1, 3.5), f(3, 4.5), f(5, 2), f(9, 0), f(5, -2), f(3, -4.5), f(-1, -3.5)], GOLD)
        cv.poly([f(1, 2.4), f(7.5, 0), f(1, -2.4), f(-1, 0)], solid(glow), [f(7.5, 0), f(1, 2.4), f(2, 1)])
    elif kind == "frost":
        for v, l in ((0, 9), (2.6, 6), (-2.6, 6)):
            cv.poly([f(0, v + 1.2), f(l, v * 1.4), f(0, v - 1.2)], ICE, [f(l, v * 1.4), f(0, v + 1.2), f(1, v + 0.5)])
    elif kind == "storm":
        cv.poly([f(-1, 4), f(5, 4.5), f(2, 2), f(2, -2), f(5, -4.5), f(-1, -4)], GOLD)
        x, y = f(4, 0)
        cv.ellipse(x, y, 3.2, 3.2, STORM, glow)
    elif kind == "void":
        cv.poly([f(-1, 3.8), f(5, 4.2), f(3, 1.5), f(3, -1.5), f(5, -4.2), f(-1, -3.8)], OBSIDIAN)
        x, y = f(4, 0)
        cv.ellipse(x, y, 3.3, 3.3, VOID, glow)
    else:  # soul: a skull with burning eyes
        x, y = f(3.5, 0)
        cv.ellipse(x, y, 3.6, 3.4, BONE)
        cv.poly([(x - 1.8, y - 0.6), (x - 0.6, y - 0.6), (x - 0.6, y + 0.6), (x - 1.8, y + 0.6)], solid(glow))
        cv.poly([(x + 0.6, y - 0.6), (x + 1.8, y - 0.6), (x + 1.8, y + 0.6), (x + 0.6, y + 0.6)], solid(glow))
    cv.save(name)


def wand(name, kind):
    cv = Canvas()
    glow = GLOW[kind]
    f = diag_haft(cv, 0, BONE if kind == "bone" else DARK_WOOD, start=(6, 26), end=(22, 10), width=1.0, wraps=LEATHER)
    if kind == "bone":
        x, y = f(3, 0)
        cv.ellipse(x, y, 3.4, 3.2, BONE)
        for dx in (-1.3, 1.1):
            cv.poly([(x + dx - 0.6, y - 0.8), (x + dx + 0.6, y - 0.8), (x + dx + 0.6, y + 0.4), (x + dx - 0.6, y + 0.4)], solid((30, 24, 24)))
    else:
        cv.poly([f(-0.5, 2), f(5, 0), f(-0.5, -2), f(-2, 0)], solid(glow), [f(5, 0), f(-0.5, 2), f(0.5, 1)])
        cv.poly([f(-2, 2.6), f(-0.5, 2.6), f(-0.5, -2.6), f(-2, -2.6)], GOLD)
    cv.save(name)


def orb(name, kind):
    cv = Canvas()
    glow = GLOW[kind]
    cv.poly([(9, 26), (23, 26), (21, 30), (11, 30)], GOLD)
    cv.poly([(12, 23), (20, 23), (22, 27), (10, 27)], GOLD)
    cv.ellipse(16, 14, 10, 10, solid(tuple(int(c * 0.75) for c in glow)), glow)
    cv.save(name)


def tome(name, kind):
    cv = Canvas()
    cover = {"ember": EMBER, "frost": ICE, "wards": ROYAL, "gale": GREEN, "souls": OBSIDIAN}[kind]
    cv.poly([(8, 7), (27, 5), (27, 26), (8, 28)], PAPER)                    # the pages, seen at the edge
    cv.poly([(5, 6), (24, 4), (24, 26), (5, 28)], cover)                    # the cover
    cv.poly([(5, 6), (8, 6), (8, 28), (5, 28)], LEATHER)                    # the spine
    for (x0, y0) in ((21, 5), (21, 23)):
        cv.poly([(x0, y0), (24, y0 - 0.4), (24, y0 + 3), (x0, y0 + 3.4)], GOLD)
    sym = {"ember": [(14, 10), (18, 16), (16, 22), (12, 22), (11, 16)], "frost": [(15, 9), (17, 15), (21, 16), (17, 17), (15, 23), (13, 17), (9, 16), (13, 15)],
           "wards": [(11, 10), (19, 10), (19, 17), (15, 22), (11, 17)], "gale": [(10, 12), (19, 11), (19, 14), (12, 15), (18, 18), (11, 20)],
           "souls": [(12, 11), (18, 11), (19, 16), (17, 19), (13, 19), (11, 16)]}[kind]
    cv.poly(sym, solid(GLOW.get({"wards": "holy", "gale": "frost", "souls": "soul"}.get(kind, kind), (255, 230, 120))))
    cv.save(name)


def bomb(name, kind):
    cv = Canvas()
    body = {"clockwork": BRASS, "smoke": pal((110, 110, 120)), "fire": RED}[kind]
    cv.line([(21, 9), (24, 5), (27, 4)], 1.4, LEATHER)
    cv.ellipse(27.5, 3.5, 2, 2, solid((255, 220, 90)), (255, 250, 200))
    cv.poly([(17, 9), (23, 9), (23, 12), (17, 12)], DARK_STEEL)
    cv.ellipse(15, 19, 10, 10, body, None)
    if kind == "clockwork":
        cv.ellipse(15, 19, 4, 4, GOLD, (90, 230, 220))
    elif kind == "fire":
        cv.poly([(15, 13), (19, 19), (17, 24), (13, 24), (11, 19)], solid((255, 170, 50)))
    else:
        cv.poly([(8, 17), (22, 17), (22, 20), (8, 20)], pal((200, 196, 186)))
    cv.save(name)


def mining_hammer(name, kind):
    metal = BRASS if kind == "brass" else ICE
    hw.hammer(name, metal, DARK_WOOD, LEATHER, band=GOLD if kind == "brass" else STEEL, long=10, half=8.5)


def drill(name):
    cv = Canvas()
    cv.poly([(11, 19), (15, 19), (14, 29), (10, 29)], WOOD)
    cv.poly([(4, 11), (21, 11), (21, 21), (4, 21)], BRASS)
    cv.poly([(4, 13), (21, 13), (21, 14), (4, 14)], GOLD)
    cv.poly([(21, 10), (31, 16), (21, 22)], STEEL, [(21, 10), (31, 16), (22, 12)])
    for x in (23, 26):
        cv.poly([(x, 11.5 + (x - 21) * 0.5), (x + 1, 11.5 + (x - 21) * 0.5), (x + 1, 20.5 - (x - 21) * 0.5), (x, 20.5 - (x - 21) * 0.5)], DARK_STEEL)
    cv.ellipse(10, 16, 3.4, 3.4, GOLD, (90, 230, 220))
    cv.save(name)


def gun(name, kind):
    cv = Canvas()
    metal = {"brass": BRASS, "royal": GOLD, "musket": DARK_STEEL, "blunderbuss": BRASS}[kind]
    stock = {"brass": DARK_WOOD, "royal": SILK, "musket": WOOD, "blunderbuss": DARK_WOOD}[kind]
    if kind == "musket":
        cv.poly([(1, 14), (18, 13), (19, 17), (6, 20), (1, 22)], stock)
        cv.poly([(8, 12), (31, 12), (31, 15), (8, 15)], metal, [(8, 12), (31, 12), (31, 13), (8, 13)])
        cv.poly([(13, 15), (16, 15), (16, 18), (13, 18)], BRASS)
    elif kind == "blunderbuss":
        cv.poly([(2, 16), (14, 14), (14, 18), (8, 24), (3, 24)], stock)
        cv.poly([(12, 12), (25, 12), (31, 9), (31, 19), (25, 16), (12, 16)], metal, [(12, 12), (25, 12), (31, 9), (31, 10), (25, 13), (12, 13)])
    else:
        cv.poly([(4, 17), (10, 15), (13, 17), (9, 27), (4, 27)], stock)
        cv.poly([(9, 12), (29, 12), (29, 16), (9, 16)], metal, [(9, 12), (29, 12), (29, 13), (9, 13)])
        cv.poly([(9, 11), (12, 9), (13, 12)], DARK_STEEL)
        cv.line([(12, 17), (13, 20), (16, 19)], 1.0, metal)
        if kind == "royal":
            cv.ellipse(7.5, 21, 1.6, 1.6, solid((230, 40, 60)), (255, 140, 150))
    cv.save(name)


def cartridge(name):
    cv = Canvas()
    for ox, oy in ((0, 0), (8, 6)):
        cv.poly([(8 + ox, 10 + oy), (14 + ox, 10 + oy), (14 + ox, 22 + oy), (8 + ox, 22 + oy)], BRASS, [(8 + ox, 10 + oy), (9 + ox, 10 + oy), (9 + ox, 22 + oy), (8 + ox, 22 + oy)])
        cv.ellipse(11 + ox, 9 + oy, 3, 3, DARK_STEEL)
    cv.save(name)


VANILLA_BOW_WOOD = [(40, 30, 11), (73, 54, 21), (104, 78, 30), (137, 103, 39)]  # dark to light
BOW_WOOD = {"recurve": [(60, 26, 14), (110, 52, 28), (160, 86, 46), (200, 126, 72)],
            "longbow": [(30, 20, 14), (58, 40, 26), (88, 62, 40), (120, 88, 58)],
            "frost": [(30, 80, 130), (70, 150, 210), (130, 200, 240), (200, 240, 255)],
            "solar": [(120, 70, 10), (200, 130, 20), (240, 180, 50), (255, 226, 120)],
            "void": [(16, 8, 28), (46, 20, 80), (90, 44, 150), (160, 100, 230)]}


def bow(name, kind, pull):
    """The vanilla bow and its pull frames, the wood recolored to the bow; the string and arrow stay vanilla."""
    img = mp_vanilla("item/bow.png" if pull is None else "item/bow_pulling_%d.png" % pull)
    px = img.load()
    for y in range(img.height):
        for x in range(img.width):
            c = px[x, y]
            if c[3] and c[:3] in VANILLA_BOW_WOOD:
                px[x, y] = BOW_WOOD[kind][VANILLA_BOW_WOOD.index(c[:3])] + (255,)
    img.save(os.path.join(ITEM, (name if pull is None else "%s_pulling_%d" % (name, pull)) + ".png"))


def mp_vanilla(path):
    import io
    import zipfile
    return Image.open(io.BytesIO(zipfile.ZipFile(mp.JAR).read("assets/minecraft/textures/" + path))).convert("RGBA")


def crossbow(name, kind, state):
    """state: standby, pulling_0..2 or arrow (loaded). The stock runs from the butt (lower right) to the
    front (upper left); the prod is a wide bow across the front, its string drawn back to the nut."""
    cv = Canvas()
    stock, prod = {"brass": (DARK_WOOD, BRASS), "repeater": (WOOD, DARK_STEEL), "glacial": (DARK_WOOD, ICE)}[kind]
    cv.line([(29, 29), (9, 9)], 3.6, stock)
    cv.poly([(25, 24), (31, 27), (28, 31), (24, 26)], stock)                 # the butt
    cv.poly([(19, 20), (22, 20), (21, 25), (18, 24)], DARK_STEEL)              # the trigger
    a, b = (0.5, 21), (21, 0.5)
    cv.line(bezier(a, (2, 2), b), 2.0, prod)                                   # the prod
    cv.poly([(5, 4), (8, 4), (8, 8), (4, 8)], DARK_STEEL)                      # the prod mount
    cv.line([(4, 4), (1.5, 1.5)], 1.3, DARK_STEEL)                             # the stirrup
    if kind == "repeater":
        cv.poly([(12, 9), (17, 12), (14, 16), (9, 13)], DARK_STEEL)          # the magazine
    pull = {"standby": 0, "pulling_0": 3, "pulling_1": 5, "pulling_2": 8, "arrow": 8}[state]
    nut = (10 + pull, 10 + pull)
    cv.line([a, nut, b], 0.8, STRING)
    if state == "arrow":
        cv.line([nut, (4, 4)], 1.2, WOOD)
        cv.poly([(1, 1), (6, 2.5), (2.5, 6)], STEEL)
    cv.save("%s_%s" % (name, state))


def class_sword(name, *_):
    cv = Canvas()
    f = frame((4, 28), (27, 5))
    cv.poly([f(-26, 1.3), f(-21, 1.3), f(-21, -1.3), f(-26, -1.3)], ROYAL)
    x, y = f(-27, 0)
    cv.ellipse(x, y, 1.8, 1.8, GOLD)
    cv.poly([f(-21, 5), f(-19.5, 5), f(-19.5, -5), f(-21, -5)], GOLD)
    cv.poly([f(-19.5, 2.2), f(-1.5, 2.0), f(1.5, 0), f(-1.5, -2.0), f(-19.5, -2.2)], MARBLE, [f(-19.5, 2.2), f(-1.5, 2.0), f(1.5, 0), f(-1.5, 1.2), f(-19.5, 1.4)])
    for u in (-16, -12, -8):
        cv.poly([f(u, 0.5), f(u + 1.2, 0.5), f(u + 1.2, -0.5), f(u, -0.5)], solid((255, 220, 120)))
    cv.save(name)


def class_mace(name, *_):
    cv = Canvas()
    f = diag_haft(cv, 0, DARK_WOOD, wraps=GOLD)
    x, y = f(1, 0)
    cv.ellipse(x, y, 5, 5, GOLD)
    for ang in range(0, 360, 60):
        a = math.radians(ang)
        cv.poly([(x + math.cos(a) * 3.5, y + math.sin(a) * 3.5), (x + math.cos(a + 0.35) * 7.2, y + math.sin(a + 0.35) * 7.2),
                 (x + math.cos(a + 0.7) * 3.5, y + math.sin(a + 0.7) * 3.5)], MARBLE)
    cv.ellipse(x, y, 2, 2, solid((255, 236, 160)), (255, 255, 230))
    cv.save(name)


def class_lance(name):
    cv = Canvas()
    f = diag_haft(cv, 0, WOOD, start=(2, 30), end=(27, 5), width=1.0, wraps=ROYAL)
    cv.poly([f(-20, 4), f(-17, 4), f(-17, -4), f(-20, -4)], GOLD)
    cv.poly([f(-17, 2.6), f(3, 0.6), f(4.5, 0), f(3, -0.6), f(-17, -2.6)], MARBLE, [f(-17, 2.6), f(3, 0.6), f(4.5, 0), f(-17, 1.8)])
    cv.save(name)


def class_sickle(name):
    cv = Canvas()
    diag_haft(cv, 0, DARK_WOOD, start=(6, 28), end=(18, 14), width=1.1, wraps=BONE)
    cv.line(bezier((17, 15), (24, 1), (6, 6)), 2.6, pal((90, 100, 110)))
    cv.line(bezier((18, 13), (23, 3), (8, 6)), 0.8, solid((120, 250, 230)))
    cv.save(name)


def claws(name, kind):
    cv = Canvas()
    metal = DARK_STEEL if kind == "shadow" else pal((60, 120, 60))
    cv.poly([(4, 20), (12, 20), (12, 29), (4, 29)], LEATHER)
    cv.poly([(4, 18), (13, 18), (13, 21), (4, 21)], metal)
    for k, x0 in enumerate((4.5, 8, 11.5)):
        cv.poly([(x0, 18), (x0 + 1.8, 18), (x0 + 12 + k, 3 + k), (x0 + 10 + k, 3 + k)], metal, [(x0, 18), (x0 + 0.8, 18), (x0 + 11 + k, 3 + k), (x0 + 10 + k, 3 + k)])
    cv.save(name)


def star(name):
    cv = Canvas()
    for ang in (0, 90, 180, 270):
        a = math.radians(ang)
        tip = (16 + math.cos(a) * 13, 16 + math.sin(a) * 13)
        l = (16 + math.cos(a - 0.8) * 4, 16 + math.sin(a - 0.8) * 4)
        r = (16 + math.cos(a + 0.8) * 4, 16 + math.sin(a + 0.8) * 4)
        cv.poly([l, tip, r, (16, 16)], STEEL, [l, tip, (16 + math.cos(a - 0.3) * 5, 16 + math.sin(a - 0.3) * 5)])
    cv.ellipse(16, 16, 2.2, 2.2, solid((140, 20, 30)))
    cv.save(name)


def scepter(name):
    cv = Canvas()
    f = diag_haft(cv, 0, GOLD, start=(5, 27), end=(21, 11), width=1.1, wraps=SILK)
    x, y = f(4, 0)
    cv.ellipse(x, y, 4, 4, GOLD)
    for k in (-1, 0, 1):
        cv.poly([(x - 1 + k * 3.2, y - 3.5), (x + k * 3.2, y - 7.5), (x + 1 + k * 3.2, y - 3.5)], GOLD)
    cv.ellipse(x, y, 2, 2, solid((230, 40, 60)), (255, 150, 160))
    cv.save(name)


def class_saber(name, *_):
    cv = Canvas()
    f = frame((5, 27), (26, 6))
    cv.poly([f(-25, 1.2), f(-20, 1.2), f(-20, -1.2), f(-25, -1.2)], SILK)
    cv.poly([f(-20, 3.5), f(-18.5, 3.5), f(-18.5, -3.5), f(-20, -3.5)], GOLD)
    cv.line(bezier(f(-18.5, 0), f(-8, -3.5), f(1, 2.5)), 2.6, STEEL)
    cv.line(bezier(f(-18, 0.8), f(-8, -2.6), f(0.6, 2.4)), 0.7, solid((250, 252, 255)))
    cv.save(name)


def spell(name, kind):
    cv = Canvas()
    if kind == "bone":
        cv.poly([(4, 26), (24, 6), (28, 4), (26, 8), (6, 28)], BONE, [(24, 6), (28, 4), (25, 6)])
    else:
        g = GLOW[kind]
        cv.ellipse(16, 16, 9, 9, solid(tuple(int(c * 0.7) for c in g)), tuple(min(255, c + 60) for c in g))
    cv.save(name)


def shot(name):
    cv = Canvas()
    cv.ellipse(16, 16, 5, 5, pal((110, 110, 120)))
    cv.save(name)


PICK_RAMPS = {"star_lapis": [(20, 30, 100), (40, 60, 170), (80, 110, 220), (150, 180, 250), (220, 230, 255)],
              "solar_gold": [(120, 70, 10), (200, 130, 20), (240, 180, 50), (255, 220, 110), (255, 248, 200)],
              "orichalcum": [(14, 70, 36), (30, 130, 66), (70, 190, 100), (140, 230, 150), (210, 255, 210)],
              "aetherium": [(90, 100, 150), (150, 160, 210), (200, 210, 245), (230, 236, 255), (255, 255, 255)]}

if __name__ == "__main__":
    for i in ITEMS:
        kind, *args = i["sprite"]
        n = i["id"]
        if kind == "pickaxe":
            mp.pickaxe(n, PICK_RAMPS[args[0]])
        elif kind == "bow":
            bow(n, args[0], None)
            for k in range(3):
                bow(n, args[0], k)
        elif kind == "crossbow":
            for st in ("standby", "pulling_0", "pulling_1", "pulling_2", "arrow"):
                crossbow(n, args[0], st)
        else:
            fn = {"mining_hammer": mining_hammer, "drill": drill, "javelin": None, "knife": None, "staff": staff, "wand": wand, "orb": orb,
                  "tome": tome, "bomb": bomb, "pistol": gun, "musket": None, "blunderbuss": None, "cartridge": cartridge,
                  "class_sword": class_sword, "class_mace": class_mace, "class_lance": class_lance, "class_sickle": class_sickle,
                  "claws": claws, "star": star, "scepter": scepter, "class_saber": class_saber, "spell": spell, "shot": shot}[kind]
            if kind == "javelin":
                javelin(n, {"brass": BRASS, "glacial": ICE, "aetherium": pal((200, 210, 245))}[args[0]], DARK_WOOD if args[0] != "aetherium" else GOLD)
            elif kind == "knife":
                knife(n, {"brass": BRASS, "glacial": ICE, "venom": pal((70, 150, 60))}[args[0]], LEATHER)
            elif kind in ("musket", "blunderbuss"):
                gun(n, kind)
            elif args:
                fn(n, *args)
            else:
                fn(n)
    print("arsenal 3 sprites:", len(ITEMS))
