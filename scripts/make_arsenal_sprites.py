"""Item sprites of the arsenal, batch 1, in the style of the reference sheets: a dark outline round
every shape, a lit edge and a shaded edge on metal, warm wood, saturated gems, and glow on the Void and
ice pieces. Each weapon is a shaft on the diagonal and a head stamped from a small pixel map.

Run from the repository root: python scripts/make_arsenal_sprites.py
"""
import os

from PIL import Image

ITEM = os.path.join("src", "main", "resources", "assets", "sofe", "textures", "item")

# palettes: d dark, m mid, l light, h highlight
STEEL = {"d": (78, 82, 94), "m": (150, 156, 168), "l": (206, 212, 222), "h": (250, 252, 255)}
DARK_STEEL = {"d": (40, 42, 52), "m": (82, 86, 100), "l": (130, 136, 150), "h": (190, 196, 210)}
BRASS = {"d": (122, 78, 26), "m": (190, 136, 52), "l": (236, 188, 96), "h": (255, 236, 170)}
GOLD = {"d": (150, 100, 20), "m": (226, 170, 40), "l": (252, 214, 96), "h": (255, 246, 200)}
WOOD = {"d": (64, 38, 20), "m": (110, 70, 38), "l": (150, 102, 58), "h": (186, 136, 84)}
DARK_WOOD = {"d": (34, 22, 16), "m": (60, 40, 28), "l": (88, 62, 44), "h": (120, 90, 64)}
ICE = {"d": (40, 110, 170), "m": (100, 190, 236), "l": (176, 232, 252), "h": (240, 252, 255)}
VOID = {"d": (34, 12, 58), "m": (88, 36, 140), "l": (150, 80, 220), "h": (220, 170, 255)}
OBSIDIAN = {"d": (14, 10, 20), "m": (36, 26, 50), "l": (70, 52, 96), "h": (130, 110, 170)}
SLIME = {"d": (40, 90, 30), "m": (90, 160, 50), "l": (150, 214, 90), "h": (210, 250, 160)}
BONE = {"d": (130, 116, 90), "m": (196, 182, 150), "l": (230, 220, 196), "h": (250, 246, 232)}
RED = {"d": (110, 14, 20), "m": (180, 30, 36), "l": (230, 70, 70), "h": (255, 160, 150)}
LEATHER = {"d": (60, 30, 18), "m": (110, 60, 34), "l": (150, 92, 56), "h": (180, 124, 80)}
CHAIN = {"d": (56, 56, 64), "m": (110, 112, 122), "l": (160, 162, 172), "h": (210, 212, 220)}
GEM_TURQ, GEM_RED, GEM_PURPLE, GEM_ICE = (60, 220, 200), (240, 40, 60), (200, 110, 255), (200, 250, 255)
OUTLINE = (20, 14, 18)


class Sprite:
    def __init__(self):
        self.px = {}

    def put(self, x, y, c):
        if 0 <= x < 16 and 0 <= y < 16:
            self.px[(x, y)] = c

    def stamp(self, rows, ox, oy, pal, extra=None):
        """A pixel map: letters d m l h from the palette, others from extra, '.' empty."""
        for dy, row in enumerate(rows):
            for dx, ch in enumerate(row):
                if ch == ".":
                    continue
                c = pal.get(ch) if ch in pal else (extra or {}).get(ch)
                if c:
                    self.put(ox + dx, oy + dy, c)

    def shaft(self, x0, y0, x1, y1, pal, wrap=None):
        """A two-pixel shaft on the diagonal, lit on one side, with optional wraps of another palette."""
        n = max(abs(x1 - x0), abs(y1 - y0))
        for i in range(n + 1):
            x = x0 + round((x1 - x0) * i / n)
            y = y0 + round((y1 - y0) * i / n)
            p = wrap if wrap and i % 3 == 0 else pal
            self.put(x, y, p["l"])
            self.put(x + 1, y, p["d"] if i % 2 else p["m"])

    def outline(self):
        for (x, y) in list(self.px):
            for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                q = (x + dx, y + dy)
                if q not in self.px and 0 <= q[0] < 16 and 0 <= q[1] < 16:
                    self.px.setdefault(("o",) + q, OUTLINE)
        for k in [k for k in self.px if k[0] == "o"]:
            self.px[(k[1], k[2])] = self.px.pop(k)

    def save(self, name):
        self.outline()
        img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
        for (x, y), c in self.px.items():
            img.putpixel((x, y), tuple(c) + (255,))
        img.save(os.path.join(ITEM, name + ".png"))


def blade(s, x0, y0, length, pal, width=2, teeth=False, glow=None):
    """A blade rising to the upper right from (x0, y0): edge light, back dark, a bright tip."""
    for i in range(length):
        x, y = x0 + i, y0 - i
        s.put(x, y, pal["l"])
        s.put(x + 1, y, pal["m"])
        if width >= 2:
            s.put(x + 1, y + 1, pal["d"])
        if width >= 3:
            s.put(x + 2, y + 1, pal["d"])
            s.put(x + 2, y, pal["m"])
        if i % 3 == 1:
            s.put(x, y, pal["h"])
        if teeth and i % 2 == 0:
            s.put(x + width, y + 1 + (width >= 3), pal["l"])
        if glow and i % 2 == 0:
            s.put(x + 1, y, glow)
    s.put(x0 + length, y0 - length, pal["h"])


def guard(s, cx, cy, pal, wide=2, gem=None):
    for k in range(-wide, wide + 1):
        s.put(cx + k, cy + k, pal["l"] if k else pal["h"])
    s.put(cx - wide, cy - wide, pal["d"])
    s.put(cx + wide, cy + wide, pal["d"])
    if gem:
        s.put(cx, cy, gem)


def grip(s, cx, cy, pal, n=2, pommel=None):
    for k in range(1, n + 1):
        s.put(cx - k, cy + k, pal["m"] if k % 2 else pal["l"])
    if pommel:
        s.put(cx - n - 1, cy + n + 1, pommel["l"])
        s.put(cx - n - 1, cy + n + 2, pommel["m"])
        s.put(cx - n - 2, cy + n + 1, pommel["m"])


def save_weapon(name, build):
    s = Sprite()
    build(s)
    s.save(name)


# ---------------------------------------------------------------- polearms and hammers: shaft + head
def halberd(s):
    s.shaft(1, 14, 10, 5, WOOD, wrap=BRASS)
    s.stamp(["....h.",
             "...hl.",
             ".mlld.",
             "mllmd.",
             "lmmdd.",
             ".dd..."], 9, 0, STEEL)
    s.stamp(["ggg"], 9, 6, {}, {"g": GOLD["l"]})


def city_hammer(s):
    s.shaft(2, 14, 9, 7, WOOD, wrap=LEATHER)
    s.stamp(["hlllml",
             "lmmmmd",
             "lmtmmd",
             "lmmmmd",
             "mdddd."], 8, 2, BRASS, {"t": GEM_TURQ})


def war_hammer(s):
    s.shaft(2, 14, 9, 7, DARK_WOOD, wrap=CHAIN)
    s.stamp([".hllll.",
             "hlmmmmd",
             "lmmmmmd",
             "lmmmmmd",
             "lmmmmmd",
             ".dddd.."], 7, 1, STEEL)


def jousting_lance(s):
    s.shaft(1, 14, 6, 9, WOOD)
    s.stamp(["rwr",
             "wrw",
             "rwr"], 5, 8, {}, {"r": RED["m"], "w": BONE["l"]})
    for i in range(7):
        s.put(8 + i, 7 - i, STEEL["l"] if i % 2 else STEEL["h"])
        if i < 5:
            s.put(9 + i, 7 - i, STEEL["m"])
            s.put(8 + i, 8 - i, STEEL["d"])


def war_mace(s):
    s.shaft(2, 14, 9, 7, DARK_WOOD, wrap=CHAIN)
    s.stamp(["..h..",
             ".lml.",
             "hmmmd",
             ".lmd.",
             "..d.."], 9, 2, STEEL)
    for p in ((11, 1), (14, 4), (8, 4), (11, 7)):
        s.put(p[0], p[1], STEEL["h"])


def phlegm_mace(s):
    s.shaft(2, 14, 8, 8, WOOD)
    s.stamp([".h.h.",
             "hlmlh",
             ".mmmd",
             "hmmdh",
             ".d.d."], 8, 3, SLIME)
    s.put(9, 9, SLIME["m"])
    s.put(10, 10, SLIME["l"])


def gravity_hammer(s):
    s.shaft(2, 14, 9, 7, OBSIDIAN, wrap=VOID)
    s.stamp([".llll.",
             "lmmmmd",
             "lmggmd",
             "lmggmd",
             "lmmmmd",
             ".dddd."], 8, 1, DARK_STEEL, {"g": GEM_PURPLE})


def crystal_trident(s):
    s.shaft(2, 14, 9, 7, ICE)
    s.stamp(["h...h",
             "l.h.l",
             "mlllm",
             ".dmd.",
             "..d.."], 9, 1, ICE)
    s.put(11, 0, ICE["h"])


def shadow_scythe(s):
    s.shaft(3, 15, 9, 3, OBSIDIAN)
    s.stamp(["hlllll..",
             ".dmmmmll",
             "...ddmml",
             "......dm",
             ".......d"], 8, 1, VOID)


def double_flail(s):
    s.shaft(2, 14, 6, 10, WOOD, wrap=LEATHER)
    for p in ((7, 9), (8, 8), (9, 7), (8, 6), (8, 5)):
        s.put(p[0], p[1], CHAIN["l"])
    for p in ((10, 8), (11, 9)):
        s.put(p[0], p[1], CHAIN["m"])
    ball = [".h.", "hmd", ".d."]
    s.stamp(ball, 7, 2, STEEL)
    s.stamp(ball, 11, 9, STEEL)
    for p in ((8, 1), (6, 3), (10, 3), (12, 8), (14, 10), (12, 12)):
        s.put(p[0], p[1], STEEL["h"])


# ---------------------------------------------------------------- blades
def serrated_dagger(s):
    blade(s, 6, 9, 5, STEEL, 2, teeth=True)
    guard(s, 5, 10, BRASS, 2)
    grip(s, 5, 10, LEATHER, 2, BRASS)


def hunting_knife(s):
    blade(s, 7, 8, 4, STEEL, 2)
    s.put(12, 5, STEEL["m"])
    guard(s, 6, 9, DARK_STEEL, 1)
    grip(s, 6, 9, BONE, 3, BONE)


def explorer_machete(s):
    blade(s, 5, 10, 7, STEEL, 3)
    s.put(13, 1, STEEL["l"])
    s.put(14, 2, STEEL["m"])
    guard(s, 4, 11, DARK_STEEL, 1)
    grip(s, 4, 11, WOOD, 2, DARK_STEEL)


def chain_sword(s):
    blade(s, 5, 10, 8, DARK_STEEL, 2)
    for i in range(0, 8, 2):
        s.put(5 + i - 1, 10 - i - 1, BRASS["l"])
        s.put(5 + i + 2, 10 - i + 1, BRASS["m"])
    guard(s, 4, 11, BRASS, 2, GEM_TURQ)
    grip(s, 4, 11, LEATHER, 2, BRASS)


def battle_greatsword(s):
    blade(s, 5, 10, 9, STEEL, 3)
    guard(s, 4, 11, GOLD, 3, GEM_RED)
    grip(s, 4, 11, LEATHER, 2, GOLD)


def hook_blade(s):
    blade(s, 5, 10, 7, STEEL, 2)
    s.stamp(["llh",
             "d.m",
             "..l"], 11, 0, STEEL)
    guard(s, 4, 11, BRASS, 1)
    grip(s, 4, 11, LEATHER, 2, BRASS)


def vortex_dagger(s):
    blade(s, 6, 9, 6, VOID, 2, glow=GEM_PURPLE)
    guard(s, 5, 10, OBSIDIAN, 2, GEM_PURPLE)
    grip(s, 5, 10, OBSIDIAN, 2, VOID)


def obsidian_ritual_dagger(s):
    for i in range(6):
        wave = 1 if i % 2 else 0
        s.put(6 + i, 9 - i - wave, OBSIDIAN["l"])
        s.put(7 + i, 9 - i - wave, OBSIDIAN["m"])
        s.put(7 + i, 10 - i - wave, OBSIDIAN["d"])
    s.put(12, 2, OBSIDIAN["h"])
    guard(s, 5, 10, GOLD, 2, GEM_RED)
    grip(s, 5, 10, OBSIDIAN, 2, RED)


WEAPONS = {
    "imperial_halberd": halberd, "city_hammer": city_hammer, "war_hammer": war_hammer, "serrated_dagger": serrated_dagger,
    "hunting_knife": hunting_knife, "jousting_lance": jousting_lance, "explorer_machete": explorer_machete, "war_mace": war_mace,
    "chain_sword": chain_sword, "crystal_trident": crystal_trident, "shadow_scythe": shadow_scythe,
    "battle_greatsword": battle_greatsword, "phlegm_mace": phlegm_mace, "double_flail": double_flail, "hook_blade": hook_blade,
    "gravity_hammer": gravity_hammer, "vortex_dagger": vortex_dagger, "obsidian_ritual_dagger": obsidian_ritual_dagger,
}

# ---------------------------------------------------------------- shields: a heater shape, a rim and an emblem
HEATER = ["..rrrrrrrrrrrr..",
          ".rffffffffffffr.",
          ".rffffffffffffr.",
          ".rffffffffffffr.",
          ".rffffffffffffr.",
          ".rffffffffffffr.",
          ".rffffffffffffr.",
          ".rffffffffffffr.",
          ".rffffffffffffr.",
          "..rffffffffffr..",
          "..rffffffffffr..",
          "...rffffffffr...",
          "....rffffffr....",
          ".....rffffr.....",
          "......rrrr......",
          "................"]
ROUND = ["....rrrrrrrr....",
         "..rrffffffffrr..",
         ".rffffffffffffr.",
         ".rffffffffffffr.",
         "rffffffffffffffr",
         "rffffffffffffffr",
         "rffffffffffffffr",
         "rffffffffffffffr",
         "rffffffffffffffr",
         "rffffffffffffffr",
         ".rffffffffffffr.",
         ".rffffffffffffr.",
         "..rrffffffffrr..",
         "....rrrrrrrr....",
         "................",
         "................"]


def shield(name, shape, rim, field, emblem, emblem_at):
    s = Sprite()
    for y, row in enumerate(shape):
        for x, ch in enumerate(row):
            if ch == "r":
                s.put(x, y, rim["l"] if y < 3 or x < 4 else rim["m"])
            elif ch == "f":
                shade = field["l"] if x < 6 and y < 6 else field["d"] if x > 11 or y > 10 else field["m"]
                s.put(x, y, shade)
    s.stamp(emblem[0], emblem_at[0], emblem_at[1], emblem[1], emblem[2] if len(emblem) > 2 else None)
    s.save(name)


CRESCENT = ([".hh..",
             "h....",
             "h....",
             "h....",
             ".hh.."], GOLD)
STAR = (["..h..",
         ".hlh.",
         "hlgl h".replace(" ", ""),
         ".hlh.",
         "..h.."], BRASS, {"g": GEM_TURQ})
EYE = ([".ggg.",
        "gwpwg",
        ".ggg."], {}, {"g": VOID["l"], "w": GEM_PURPLE, "p": OBSIDIAN["d"]})
BOSS = (["rw.",
         "wbw",
         ".wr"], {}, {"r": RED["m"], "w": BONE["l"], "b": STEEL["h"]})


def nordrath_shield():
    s = Sprite()
    for y, row in enumerate(ROUND):
        for x, ch in enumerate(row):
            if ch == "r":
                s.put(x, y, CHAIN["l"] if y < 4 else CHAIN["m"])
            elif ch == "f":
                quarter = (x < 8) == (y < 7)
                c = RED if quarter else BONE
                s.put(x, y, c["l"] if (x + y) % 5 == 0 else c["m"])
    for dx in range(-1, 2):
        for dy in range(-1, 2):
            s.put(7 + dx, 6 + dy, STEEL["m"])
    s.put(7, 6, STEEL["h"])
    s.save("nordrath_shield")


if __name__ == "__main__":
    for name, build in WEAPONS.items():
        save_weapon(name, build)
    shield("sulthari_shield", HEATER, GOLD, RED, CRESCENT, (6, 4))
    nordrath_shield()
    shield("observatory_shield", HEATER, BRASS, {"d": (30, 50, 110), "m": (50, 86, 170), "l": (90, 130, 210)}, STAR, (5, 4))
    shield("void_shield", HEATER, OBSIDIAN, VOID, EYE, (5, 5))
    print("arsenal sprites:", len(WEAPONS), "weapons and 4 shields")
