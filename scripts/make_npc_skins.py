"""Skins of the story's people (64x64, the player layout with wide arms), one per NPC id, written to
textures/entity/npc/<id>.png where SoFEEntityRenderers.skinFor finds them. Each is painted from a short description:
the empire sets the clothes (Sulthari's kaftans and turbans, Nordrath's wool, furs and braids, Parsivan's violet and
teal robes and veils, Khemet's linen, gold collars and nemes, Aureum's togas and legionaries), the person sets the
rest (skin, hair, beard, age, trade, headwear). The Bearers follow their hero cards (art/concepts/hero_cards_sheet_v2.png).

Run from the repository root: python scripts/make_npc_skins.py  (also writes a preview sheet under build/)
"""
import os
import random

from PIL import Image

OUT = os.path.join("src", "main", "resources", "assets", "sofe", "textures", "entity", "npc")

# ------------------------------------------------------------------------------------------------ the layout
BASE = {"head": ((0, 0), (8, 8, 8)), "body": ((16, 16), (8, 12, 4)), "rarm": ((40, 16), (4, 12, 4)), "larm": ((32, 48), (4, 12, 4)),
        "rleg": ((0, 16), (4, 12, 4)), "lleg": ((16, 48), (4, 12, 4))}
OVER = {"head": ((32, 0), (8, 8, 8)), "body": ((16, 32), (8, 12, 4)), "rarm": ((40, 32), (4, 12, 4)), "larm": ((48, 48), (4, 12, 4)),
        "rleg": ((0, 32), (4, 12, 4)), "lleg": ((0, 48), (4, 12, 4))}


def faces(uv, size):
    (u, v), (w, h, d) = uv, size
    return {"top": (u + d, v, w, d), "bottom": (u + d + w, v, w, d), "right": (u, v + d, d, h), "front": (u + d, v + d, w, h),
            "left": (u + d + w, v + d, d, h), "back": (u + d + w + d, v + d, w, h)}


def clamp(c):
    return tuple(max(0, min(255, int(round(v)))) for v in c[:3])


def sh(c, f):
    return clamp([v * f for v in c])


def mix(a, b, t):
    return clamp([a[i] + (b[i] - a[i]) * t for i in range(3)])


class Skin:
    def __init__(self, seed):
        self.img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
        self.rnd = random.Random(seed)

    def put(self, x, y, c, a=255):
        self.img.putpixel((x, y), clamp(c) + (a,))

    def get(self, x, y):
        return self.img.getpixel((x, y))[:3]

    def face(self, layer, part, side):
        return faces(*(BASE if layer == "base" else OVER)[part])[side]

    def paint(self, layer, part, side, fn):
        """fn(x, y, w, h) -> colour or None for each pixel of a face."""
        x0, y0, w, h = self.face(layer, part, side)
        for y in range(h):
            for x in range(w):
                c = fn(x, y, w, h)
                if c is not None:
                    self.put(x0 + x, y0 + y, c)

    def around(self, layer, part, fn, sides=("front", "right", "left", "back")):
        """fn(u, y, side, w, h) over the faces round a part, u counting along the front from its left."""
        for side in sides:
            self.paint(layer, part, side, lambda x, y, w, h, s=side: fn(x, y, s, w, h))

    def cloth(self, c, x, y, weave=0.05):
        n = self.rnd.uniform(-weave, weave)
        if (x + y) % 2 == 0:
            n -= weave * 0.6
        return sh(c, 1 + n)

    def save(self, name):
        os.makedirs(OUT, exist_ok=True)
        self.img.save(os.path.join(OUT, name + ".png"))


# ------------------------------------------------------------------------------------------------ palettes
SKIN = {1: (243, 212, 186), 2: (230, 188, 150), 3: (204, 158, 116), 4: (178, 128, 88), 5: (140, 96, 62), 6: (98, 64, 42)}
HAIR = {"black": (28, 22, 22), "dark": (58, 38, 26), "brown": (98, 64, 38), "auburn": (138, 62, 30), "red": (168, 72, 32),
        "blonde": (214, 178, 108), "ash": (186, 168, 128), "grey": (146, 144, 140), "white": (222, 220, 214)}
EYES = {"brown": (74, 46, 26), "dark": (40, 28, 20), "blue": (60, 110, 180), "green": (70, 128, 70), "grey": (110, 120, 130),
        "amber": (170, 112, 34), "teal": (60, 220, 200)}
GOLD, WHITE, LINEN, STEEL, IRON, LEATHER, FUR = (226, 180, 64), (236, 232, 222), (232, 224, 204), (176, 182, 190), (112, 116, 122), (110, 72, 42), (196, 180, 152)


# ------------------------------------------------------------------------------------------------ the head
def head(s, p):
    skin, hair = SKIN[p["skin"]], HAIR.get(p.get("hair", "dark"), (40, 30, 24))
    old, child = p.get("age") == "old", p.get("age") == "child"
    style = p.get("style", "short")
    # skin all round, a little shading toward the bottom
    for side in ("front", "right", "left", "back", "top", "bottom"):
        s.paint("base", "head", side, lambda x, y, w, h: sh(skin, 1.03 - y * 0.012 + s.rnd.uniform(-0.015, 0.015)))
    # hair
    top_hair = style not in ("bald", "shaved")
    if top_hair:
        s.paint("base", "head", "top", lambda x, y, w, h: s.cloth(hair, x, y, 0.08))
        long = style in ("long", "braids", "bun", "wig", "pigtails")
        depth = 7 if long else 3 if style in ("short", "curly") else 2
        s.around("base", "head", lambda u, y, side, w, h: s.cloth(hair, u, y, 0.08) if (y < depth if side != "back" else y < (8 if long else 5)) else None,
                 sides=("right", "left", "back"))
        fringe = p.get("fringe", 1 if not long else 1)
        s.paint("base", "head", "front", lambda x, y, w, h: s.cloth(hair, x, y, 0.08) if y < fringe or (long and (x == 0 or x == 7) and y < 7) else None)
        if style == "wig":  # the Khemet wig: square and black, a band of gold
            s.around("over", "head", lambda u, y, side, w, h: (GOLD if y == 1 else s.cloth(hair, u, y, 0.06)) if y < 7 else None,
                     sides=("right", "left", "back"))
            s.paint("over", "head", "top", lambda x, y, w, h: s.cloth(hair, x, y, 0.06))
            s.paint("over", "head", "front", lambda x, y, w, h: GOLD if y == 1 else s.cloth(hair, x, y) if y == 0 or (x in (0, 7) and y < 7) else None)
        if style == "braids":
            for side in ("right", "left"):
                s.paint("over", "head", side, lambda x, y, w, h: (sh(hair, 0.8) if y % 2 else sh(hair, 1.1)) if x in (5, 6) and y >= 3 else None)
        if style == "pigtails":
            for side in ("right", "left"):
                s.paint("over", "head", side, lambda x, y, w, h: sh(hair, 1.05 if y % 2 else 0.85) if x in (3, 4) and y >= 2 else None)
        if style == "bun":
            s.paint("over", "head", "back", lambda x, y, w, h: sh(hair, 0.9) if 2 <= x <= 5 and 0 <= y <= 2 else None)
            s.paint("over", "head", "top", lambda x, y, w, h: sh(hair, 1.05) if 2 <= x <= 5 and y >= 6 else None)
        if style == "curly":
            s.around("over", "head", lambda u, y, side, w, h: sh(hair, 1.12) if y < 2 and (u + y) % 2 == 0 else None)
    if style == "sidelock":  # a shaved head with the lock of youth on the right
        s.paint("base", "head", "top", lambda x, y, w, h: sh(skin, 0.94))
        s.paint("over", "head", "right", lambda x, y, w, h: sh(HAIR["black"], 1.0 + 0.1 * (y % 2)) if x in (5, 6) and y < 7 else None)
    if style == "shaved":
        s.paint("base", "head", "top", lambda x, y, w, h: sh(skin, 0.96))
    # the face
    fx, fy, _, _ = s.face("base", "head", "front")
    eye = EYES.get(p.get("eyes", "brown"))
    white = (240, 238, 232)
    brow = sh(hair, 0.8) if top_hair else sh(skin, 0.7)
    if old and top_hair:
        brow = HAIR["grey"]
    row = 4
    for x, c in ((1, white), (2, eye), (5, eye), (6, white)):
        s.put(fx + x, fy + row, c)
    if child:
        for x, c in ((1, eye), (6, eye)):
            s.put(fx + x, fy + row, c)
    for x in (1, 2, 5, 6):
        s.put(fx + x, fy + row - 1, brow)
    if p.get("kohl"):
        for x in (0, 1, 2, 5, 6, 7):
            s.put(fx + x, fy + row - 1, (20, 18, 20))
        s.put(fx + 0, fy + row, (20, 18, 20))
        s.put(fx + 7, fy + row, (20, 18, 20))
    if p.get("glow"):
        s.put(fx + 2, fy + row, EYES["teal"])
        s.put(fx + 5, fy + row, EYES["teal"])
    s.put(fx + 3, fy + 5, sh(skin, 0.86))  # the nose
    s.put(fx + 4, fy + 5, sh(skin, 0.9))
    lips = mix(sh(skin, 0.75), (150, 60, 60), 0.35 if p.get("female") else 0.15)
    s.put(fx + 3, fy + 6, lips)
    s.put(fx + 4, fy + 6, lips)
    if p.get("female") and not child:
        s.put(fx + 0, fy + 3, brow) if False else None
    if old:  # lines round the eyes and the mouth
        for x, y in ((0, 5), (7, 5), (2, 6), (5, 6)):
            s.put(fx + x, fy + y, sh(skin, 0.85))
    if p.get("scar"):
        for y in (3, 4, 5):
            s.put(fx + 6, fy + y, sh(skin, 0.7) if y != 4 else (190, 120, 110))
    if p.get("loupe"):  # the jeweller's glass over the right eye
        for x, y in ((4, 3), (5, 3), (6, 3), (4, 4), (4, 5), (5, 5), (6, 5), (7, 4)):
            s.put(fx + x, fy + y, GOLD)
        s.put(fx + 5, fy + 4, (150, 210, 230))
    # the beard
    beard = p.get("beard")
    if beard:
        bc = HAIR.get(p.get("beard_color", p.get("hair", "dark")))
        rows = {"stubble": (), "moustache": (5,), "short": (5, 6, 7), "full": (5, 6, 7), "long": (5, 6, 7)}[beard]
        if beard == "stubble":
            s.paint("base", "head", "front", lambda x, y, w, h: mix(s.get(fx + x, fy + y), bc, 0.35) if y >= 5 and (x + y) % 2 == 0 else None)
        else:
            def bfn(x, y, w, h):
                if y not in rows:
                    return None
                if beard == "moustache":
                    return s.cloth(bc, x, y) if 2 <= x <= 5 else None
                if y == 6 and 3 <= x <= 4:
                    return sh(bc, 0.6)  # the mouth in the beard
                if y == 5 and 3 <= x <= 4:
                    return None if beard == "short" else s.cloth(bc, x, y)
                return s.cloth(bc, x, y, 0.1)
            s.paint("base", "head", "front", bfn)
            s.around("base", "head", lambda u, y, side, w, h: s.cloth(bc, u, y, 0.1) if y >= 5 and (u >= 5 if side == "right" else u <= 2) else None,
                     sides=("right", "left"))
            if beard == "long":  # down over the chest
                s.paint("over", "body", "front", lambda x, y, w, h: s.cloth(bc, x, y, 0.1) if 2 <= x <= 5 and y < 4 - (1 if x in (2, 5) else 0) else None)
                if p.get("braided_beard"):
                    s.paint("over", "body", "front", lambda x, y, w, h: GOLD if x in (3, 4) and y == 2 else None)


# ------------------------------------------------------------------------------------------------ headwear (the hat layer)
def headwear(s, p):
    kind = p.get("hat")
    if not kind:
        return
    c = p.get("hat_color", WHITE)
    if kind == "turban":  # wound cloth round the head, folds, a jewel at the front
        s.around("over", "head", lambda u, y, side, w, h: (sh(c, 1.08 if (u + y) % 3 == 0 else 0.92 if (u - y) % 4 == 0 else 1.0) if y < 3 else None))
        s.paint("over", "head", "top", lambda x, y, w, h: sh(c, 0.95 + 0.1 * ((x + y) % 2)))
        if p.get("jewel"):
            fx, fy, _, _ = s.face("over", "head", "front")
            s.put(fx + 3, fy + 1, p["jewel"])
            s.put(fx + 4, fy + 1, p["jewel"])
            s.put(fx + 3, fy + 0, WHITE)
    elif kind == "tall_turban":  # the vizier's: a tall wound turban drawn in two rows of the hat layer and the top
        s.around("over", "head", lambda u, y, side, w, h: sh(c, 1.1 if (u + 2 * y) % 4 == 0 else 0.94) if y < 4 else None)
        s.paint("over", "head", "top", lambda x, y, w, h: sh(c, 1.05) if 1 <= x <= 6 and 1 <= y <= 6 else sh(c, 0.9))
        fx, fy, _, _ = s.face("over", "head", "front")
        for x, y in ((3, 1), (4, 1), (3, 2), (4, 2)):
            s.put(fx + x, fy + y, p.get("jewel", (40, 160, 120)))
        s.put(fx + 3, fy + 0, GOLD)
        s.put(fx + 4, fy + 0, GOLD)
    elif kind == "grand_turban":
        crown = c
        def wrap(u, y, side, w, h):
            if y in (2, 3):  # the white wraps, wound slantwise
                return sh(WHITE, 1.04 if (u + y) % 4 < 2 else 0.86)
            if y in (0, 1):  # the crown, worked in gold
                return GOLD if (u % 4 == 0 and y == 1) or (u % 4 == 2 and y == 0) else sh(crown, 1.06 if (u + y) % 2 else 0.9)
            return None
        s.around("over", "head", wrap)
        s.paint("over", "head", "top", lambda x, y, w, h: (WHITE if 3 <= x <= 4 and 3 <= y <= 4 else
                                                           GOLD if (x in (1, 6) or y in (1, 6)) and (x + y) % 2 == 0 else sh(crown, 1.0 + 0.08 * ((x + y) % 2))))
        fx, fy, _, _ = s.face("over", "head", "front")
        jewel = p.get("jewel", (190, 20, 30))
        for x, y in ((2, 1), (5, 1), (3, 0), (4, 0), (2, 2), (5, 2), (3, 3), (4, 3)):
            s.put(fx + x, fy + y, GOLD)
        for x, y in ((3, 1), (4, 1), (3, 2), (4, 2)):
            s.put(fx + x, fy + y, jewel if (x, y) != (3, 1) else mix(jewel, WHITE, 0.4))
    elif kind == "fez":
        s.around("over", "head", lambda u, y, side, w, h: sh(c, 1.0 - 0.05 * y) if y < 2 else None)
        s.paint("over", "head", "top", lambda x, y, w, h: sh(c, 0.92))
        s.paint("over", "head", "back", lambda x, y, w, h: (20, 20, 20) if x == 3 and y < 4 else None)  # the tassel
    elif kind == "bork":  # the guard's tall white felt cap with a gold band
        s.around("over", "head", lambda u, y, side, w, h: (GOLD if y == 2 else sh(WHITE, 0.95 + 0.05 * (u % 2))) if y < 3 else None)
        s.paint("over", "head", "top", lambda x, y, w, h: sh(WHITE, 0.9))
        s.paint("over", "head", "back", lambda x, y, w, h: sh(WHITE, 0.85) if y < 7 and 2 <= x <= 5 else None)  # its long flap
    elif kind == "scarf":  # a headscarf round the face
        def f(u, y, side, w, h):
            if side == "front":
                return s.cloth(c, u, y) if y < 1 or (u in (0, 7) and y < 8) else None
            return s.cloth(c, u, y, 0.06)
        s.around("over", "head", f)
        s.paint("over", "head", "top", lambda x, y, w, h: s.cloth(c, x, y))
        if p.get("veil"):  # a sheer veil over the mouth
            s.paint("over", "head", "front", lambda x, y, w, h: mix(c, WHITE, 0.25) if y >= 5 and 1 <= x <= 6 else None)
    elif kind == "hood":
        def f(u, y, side, w, h):
            if side == "front":
                return sh(c, 0.9) if y < 2 or (u in (0, 7)) else None
            return s.cloth(c, u, y, 0.06)
        s.around("over", "head", f)
        s.paint("over", "head", "top", lambda x, y, w, h: s.cloth(c, x, y))
        if p.get("mask"):
            s.paint("over", "head", "front", lambda x, y, w, h: sh(p["mask"], 0.95 + 0.05 * (x % 2)) if y >= 5 else None)
            s.around("over", "head", lambda u, y, side, w, h: sh(p["mask"], 0.9) if y >= 5 else None, sides=("right", "left"))
        if p.get("shadow"):  # the face half lost in the hood's shadow
            fx, fy, _, _ = s.face("base", "head", "front")
            for x in range(8):
                for y in range(4):
                    if s.get(fx + x, fy + y) not in ((240, 238, 232),):
                        s.put(fx + x, fy + y, sh(s.get(fx + x, fy + y), 0.6 + 0.08 * y))
    elif kind == "helm":  # an iron cap with a nose guard
        m = p.get("hat_color", STEEL)
        s.around("over", "head", lambda u, y, side, w, h: (sh(m, 1.15) if y == 0 else sh(m, 0.85) if y == 3 else sh(m, 1.0)) if y < 4 else None)
        s.paint("over", "head", "top", lambda x, y, w, h: sh(m, 1.1 if (x + y) % 3 else 0.95))
        fx, fy, _, _ = s.face("over", "head", "front")
        for y in (4, 5):
            s.put(fx + 3, fy + y, sh(m, 0.9))
            s.put(fx + 4, fy + y, sh(m, 0.9))
        if p.get("cheeks"):
            s.paint("over", "head", "front", lambda x, y, w, h: sh(m, 0.9) if x in (0, 7) and 4 <= y <= 7 else None)
        if p.get("crest"):  # a legionary's red crest across the top
            s.paint("over", "head", "top", lambda x, y, w, h: sh(p["crest"], 1.0 + 0.1 * (y % 2)) if x in (3, 4) else None)
        if p.get("plume"):
            s.paint("over", "head", "back", lambda x, y, w, h: sh(p["plume"], 1 + 0.1 * (y % 2)) if x in (3, 4) and y < 3 else None)
    elif kind == "gladiator":  # a broad-brimmed helmet with a grille over the face
        m = (198, 160, 92)
        s.around("over", "head", lambda u, y, side, w, h: sh(m, 1.1 if y == 0 else 0.9) if y < 3 or (side != "front" and y < 7) else None)
        s.paint("over", "head", "top", lambda x, y, w, h: (180, 30, 30) if x in (3, 4) else sh(m, 1.05))
        s.paint("over", "head", "front", lambda x, y, w, h: sh(m, 0.8) if 3 <= y <= 6 and (x % 2 == 0 or y in (3, 6)) else None)
    elif kind == "nemes":  # the striped royal cloth of Khemet, blue and gold
        def f(u, y, side, w, h):
            stripe = (40, 70, 170) if y % 2 == 0 else GOLD
            if side == "front":
                return (GOLD if y == 1 else stripe) if y < 2 or (u in (0, 7) and y < 8) else None
            return stripe
        s.around("over", "head", f)
        s.paint("over", "head", "top", lambda x, y, w, h: (40, 70, 170) if (x + y) % 2 else GOLD)
        fx, fy, _, _ = s.face("over", "head", "front")
        s.put(fx + 3, fy + 0, GOLD)  # the cobra
        s.put(fx + 4, fy + 0, (200, 40, 40))
    elif kind == "wreath":
        leaf = p.get("hat_color", (90, 130, 50))
        s.around("over", "head", lambda u, y, side, w, h: (sh(leaf, 1.1) if (u + y) % 2 else sh(leaf, 0.85)) if y == 2 and side != "front" or (side == "front" and y == 1 and u not in (3, 4)) else None)
    elif kind == "crown":
        s.around("over", "head", lambda u, y, side, w, h: (GOLD if y in (1, 2) or (y == 0 and u % 3 == 1) else None))
        fx, fy, _, _ = s.face("over", "head", "front")
        s.put(fx + 3, fy + 1, (60, 200, 210))
        s.put(fx + 4, fy + 1, (60, 200, 210))
    elif kind == "cap":  # a wool cap, round
        s.around("over", "head", lambda u, y, side, w, h: s.cloth(c, u, y, 0.08) if y < 2 else None)
        s.paint("over", "head", "top", lambda x, y, w, h: s.cloth(c, x, y, 0.08))
    elif kind == "fur_hood":
        s.around("over", "head", lambda u, y, side, w, h: (s.cloth(FUR, u, y, 0.12) if side == "front" and (y < 1 or u in (0, 7)) else
                                                            None if side == "front" else s.cloth(c, u, y, 0.08)))
        s.paint("over", "head", "top", lambda x, y, w, h: s.cloth(c, x, y, 0.08))
    elif kind == "circlet":
        s.around("over", "head", lambda u, y, side, w, h: GOLD if y == 2 else None)
        fx, fy, _, _ = s.face("over", "head", "front")
        s.put(fx + 3, fy + 2, (170, 60, 200))
        s.put(fx + 4, fy + 2, (170, 60, 200))
    elif kind == "tall_hat":  # the astronomer's: tall, dark blue, with gold stars
        s.around("over", "head", lambda u, y, side, w, h: (GOLD if (u * 3 + y) % 7 == 0 else s.cloth(c, u, y)) if y < 3 else None)
        s.paint("over", "head", "top", lambda x, y, w, h: GOLD if (x * 5 + y) % 9 == 0 else s.cloth(c, x, y))
    elif kind == "goggles":
        s.around("over", "head", lambda u, y, side, w, h: (LEATHER if y == 2 else None))
        fx, fy, _, _ = s.face("over", "head", "front")
        for x in (1, 2, 5, 6):
            s.put(fx + x, fy + 2, (196, 150, 70))
        s.put(fx + 2, fy + 2, (150, 220, 230))
        s.put(fx + 5, fy + 2, (150, 220, 230))
    elif kind == "palla":  # a Roman matron's shawl drawn over the head
        s.around("over", "head", lambda u, y, side, w, h: s.cloth(c, u, y, 0.07) if side != "front" else (s.cloth(c, u, y) if y < 1 or (u in (0, 7) and y < 7) else None))
        s.paint("over", "head", "top", lambda x, y, w, h: s.cloth(c, x, y))
    elif kind == "headband":
        s.around("over", "head", lambda u, y, side, w, h: c if y == 1 else None)


# ------------------------------------------------------------------------------------------------ clothes
def limbs(s, p, arm, leg):
    """Sleeves and hands, trousers and shoes; arm and leg describe them."""
    skin = SKIN[p["skin"]]
    sleeve, cuff, bare_from = arm.get("color"), arm.get("cuff"), arm.get("bare_from", 10)
    for part in ("rarm", "larm"):
        def af(u, y, side, w, h, part=part):
            if y >= bare_from:
                if arm.get("bracer") and 6 <= y <= 9 and y >= bare_from:
                    return arm["bracer"]
                return sh(skin, 1.0 - (y - bare_from) * 0.015)
            if cuff and y == bare_from - 1:
                return cuff
            return s.cloth(sleeve, u, y)
        s.around("base", part, af)
        s.paint("base", part, "top", lambda x, y, w, h: s.cloth(sleeve if bare_from > 0 else skin, x, y))
        s.paint("base", part, "bottom", lambda x, y, w, h: sh(skin, 0.92))
        if arm.get("bracer"):
            s.around("base", part, lambda u, y, side, w, h: arm["bracer"] if 7 <= y <= 9 and y >= bare_from else None)
        if arm.get("pauldron"):
            s.around("over", part, lambda u, y, side, w, h: sh(arm["pauldron"], 1.1 if y == 0 else 0.95) if y < 3 else None)
            s.paint("over", part, "top", lambda x, y, w, h: arm["pauldron"])
        if arm.get("fur"):
            s.around("over", part, lambda u, y, side, w, h: s.cloth(FUR, u, y, 0.15) if y < 2 else None)
    pants, shoes, bare_leg = leg.get("color"), leg.get("shoes", (70, 46, 30)), leg.get("bare", False)
    shoe_rows = leg.get("shoe_rows", 3)
    for part in ("rleg", "lleg"):
        def lf(u, y, side, w, h):
            if y >= 12 - shoe_rows:
                if leg.get("sandals"):
                    return LEATHER if (y + u) % 3 == 0 or y == 11 else sh(skin, 0.95)
                return sh(shoes, 1.0 - (y - (12 - shoe_rows)) * 0.06)
            if bare_leg and y >= leg.get("kilt", 0):
                return sh(skin, 0.98)
            if leg.get("hem") and y == leg.get("hem_row", 8):
                return leg["hem"]
            if leg.get("wrap") and y >= 5 and (y + u) % 2 == 0:
                return sh(pants, 0.82)
            if leg.get("greave") and y >= 5 and y < 12 - shoe_rows:
                return sh(leg["greave"], 1.05 if u % 2 else 0.95)
            return s.cloth(pants if not bare_leg else LINEN, u, y)
        s.around("base", part, lf)
        s.paint("base", part, "top", lambda x, y, w, h: s.cloth(pants if not bare_leg else LINEN, x, y))
        s.paint("base", part, "bottom", lambda x, y, w, h: sh(shoes, 0.7) if not leg.get("sandals") else LEATHER)


def body(s, p, kind, c, trim=None, belt=None):
    skin = SKIN[p["skin"]]

    def front_fill(x, y, w, h):
        if kind == "bare":
            return sh(skin, 1.0 - (0.06 if x in (3, 4) and 2 <= y <= 9 else 0)) if y < 10 else None
        return s.cloth(c, x, y)
    s.paint("base", "body", "front", front_fill)
    for side in ("back", "right", "left"):
        s.paint("base", "body", side, lambda x, y, w, h: (sh(skin, 0.97) if kind == "bare" and y < 10 else s.cloth(c, x, y)))
    s.paint("base", "body", "top", lambda x, y, w, h: sh(skin, 1) if kind == "bare" else s.cloth(c, x, y))
    s.paint("base", "body", "bottom", lambda x, y, w, h: s.cloth(c, x, y))
    if kind in ("kaftan", "robe"):  # an opening down the front, edged, with buttons
        s.paint("base", "body", "front", lambda x, y, w, h: (trim if x in (3, 4) else None) if trim else None)
        if p.get("buttons"):
            s.paint("base", "body", "front", lambda x, y, w, h: GOLD if x == 4 and y % 2 == 0 and y < 6 else None)
        s.paint("base", "body", "front", lambda x, y, w, h: sh(skin, 0.95) if y == 0 and x in (3, 4) else None)
    if kind == "vest":  # a vest over a shirt
        shirt = p.get("shirt", LINEN)
        s.paint("base", "body", "front", lambda x, y, w, h: s.cloth(shirt, x, y) if 2 <= x <= 5 else (trim if trim and x in (1, 6) else None))
    if kind == "tunic":
        s.paint("base", "body", "front", lambda x, y, w, h: (trim if trim and (y == 0 and 2 <= x <= 5 or y == 11) else None))
        s.paint("base", "body", "front", lambda x, y, w, h: sh(skin, 0.95) if y == 0 and x in (3, 4) else None)
    if kind == "toga":  # white wool, the fold from the left shoulder to the right hip, a coloured edge
        s.paint("base", "body", "front", lambda x, y, w, h: (trim if trim and abs(x - (7 - y * 0.6)) < 0.8 else
                                                            sh(c, 0.88) if abs(x - (7 - y * 0.6)) < 1.8 else None))
        s.paint("over", "body", "front", lambda x, y, w, h: sh(c, 0.96) if 5 <= x and y < 3 else None)
        s.paint("over", "body", "left", lambda x, y, w, h: sh(c, 0.94) if y < 8 else None)
    if kind == "dress":
        s.paint("base", "body", "front", lambda x, y, w, h: trim if trim and y == 0 else None)
    if kind == "armor":  # mail with a breastplate or scales
        plate = p.get("plate", STEEL)

        def af(x, y, w, h):
            if p.get("scales"):
                return sh(plate, 1.1 if (x + (y % 2)) % 2 == 0 else 0.85)
            if p.get("bands"):  # the legion's bands of steel
                return sh(plate, 1.15 if y % 2 == 0 else 0.8) if y < 8 else None
            return sh(plate, 1.1 - 0.04 * abs(x - 3.5)) if y < 8 else None
        s.paint("base", "body", "front", af)
        s.paint("base", "body", "back", af)
        if trim:
            s.paint("base", "body", "front", lambda x, y, w, h: trim if (y == 0 or (x in (0, 7) and y < 8)) else None)
    if kind == "knight":  # white plate edged in gold, a blue tabard down the middle with a gold scale
        s.paint("base", "body", "front", lambda x, y, w, h: (p["tabard"] if 2 <= x <= 5 and y >= 4 else sh((228, 228, 232), 1.08 - 0.04 * abs(x - 3.5))))
        s.paint("base", "body", "front", lambda x, y, w, h: GOLD if (y == 0 or (y == 3 and x not in (2, 3, 4, 5)) or (x in (3, 4) and y in (6, 7))) else None)
        s.paint("base", "body", "back", lambda x, y, w, h: sh((228, 228, 232), 0.95))
    if kind == "royal":  # gilded armour, a blue sash, a fur collar
        s.paint("base", "body", "front", lambda x, y, w, h: sh(GOLD, 1.05 - 0.05 * abs(x - 3.5) - (0.12 if y % 4 == 3 else 0)))
        s.paint("base", "body", "front", lambda x, y, w, h: p["tabard"] if 2 <= x <= 5 and y >= 6 else None)
        s.paint("over", "body", "front", lambda x, y, w, h: s.cloth(WHITE, x, y, 0.12) if y < 2 and (x < 2 or x > 5 or y == 0) else None)
        s.paint("over", "body", "right", lambda x, y, w, h: s.cloth(WHITE, x, y, 0.12) if y < 2 else None)
        s.paint("over", "body", "left", lambda x, y, w, h: s.cloth(WHITE, x, y, 0.12) if y < 2 else None)
    if trim and kind in ("tunic", "dress", "robe", "kaftan"):
        s.paint("base", "body", "front", lambda x, y, w, h: trim if y == 11 else None)
    if belt:
        s.paint("base", "body", "front", lambda x, y, w, h: (GOLD if x in (3, 4) and p.get("buckle", True) else belt) if y in (7, 8) else None)
        for side in ("back", "right", "left"):
            s.paint("base", "body", side, lambda x, y, w, h: belt if y in (7, 8) else None)
    if p.get("collar"):  # Khemet's broad collar of gold, lapis and turquoise
        cols = [GOLD, (40, 70, 170), (60, 180, 170), GOLD]
        s.paint("over", "body", "front", lambda x, y, w, h: cols[y] if y < 4 - (1 if x in (0, 7) else 0) - (1 if x in (1, 6) and y == 3 else 0) else None)
        s.paint("over", "body", "back", lambda x, y, w, h: cols[y] if y < 2 else None)
        for side in ("right", "left"):
            s.paint("over", "body", side, lambda x, y, w, h: cols[y] if y < 2 else None)
    if p.get("leopard"):  # the sem priest's leopard skin over one shoulder
        spot = (60, 40, 20)
        s.paint("over", "body", "front", lambda x, y, w, h: (spot if (x * 3 + y * 2) % 5 == 0 else (206, 156, 70)) if x + y * 0.5 < 6 and y >= 1 else None)
    if p.get("apron"):
        a = p["apron"]
        s.paint("over", "body", "front", lambda x, y, w, h: s.cloth(a, x, y, 0.06) if 1 <= x <= 6 and y >= 4 - (2 if p.get("bib") else 0) else None)
        for part in ("rleg", "lleg"):
            s.paint("over", part, "front", lambda x, y, w, h: s.cloth(a, x, y, 0.06) if y < 6 else None)
        if p.get("dust"):
            s.paint("over", "body", "front", lambda x, y, w, h: mix(a, WHITE, 0.5) if (x * 7 + y * 3) % 5 == 0 and y >= 3 else None)
    if p.get("cape"):
        cc = p["cape"]
        s.paint("over", "body", "back", lambda x, y, w, h: s.cloth(cc, x, y, 0.07))
        s.paint("over", "body", "top", lambda x, y, w, h: s.cloth(cc, x, y) if y >= 2 else None)
        for side in ("right", "left"):
            s.paint("over", "body", side, lambda x, y, w, h: s.cloth(cc, x, y) if x >= 2 else None)
    if p.get("fur_collar"):
        s.paint("over", "body", "front", lambda x, y, w, h: s.cloth(FUR, x, y, 0.15) if y < 2 and (x < 2 or x > 5 or y == 0) else None)
        for side in ("back", "right", "left"):
            s.paint("over", "body", side, lambda x, y, w, h: s.cloth(FUR, x, y, 0.15) if y < 2 else None)
    if p.get("sash"):  # a sash round the waist, its end hanging
        sc = p["sash"]
        s.paint("base", "body", "front", lambda x, y, w, h: sh(sc, 1.05 if y == 7 else 0.92) if y in (7, 8) else None)
        for side in ("back", "right", "left"):
            s.paint("base", "body", side, lambda x, y, w, h: sh(sc, 0.95) if y in (7, 8) else None)
        s.paint("base", "body", "front", lambda x, y, w, h: sh(sc, 0.85) if x == 6 and 9 <= y <= 11 else None)
    if p.get("bandolier"):  # a strap across the chest with pouches
        bc = p["bandolier"]
        s.paint("over", "body", "front", lambda x, y, w, h: (GOLD if (x, y) == (4, 4) else bc) if abs(x - y * 0.7) < 0.7 and y < 11 else None)
    if p.get("stars"):  # gold stars sewn on a robe
        s.paint("base", "body", "front", lambda x, y, w, h: GOLD if (x * 5 + y * 3) % 11 == 0 else None)
    if p.get("coins"):  # a dancer's belt of coins
        s.paint("over", "body", "front", lambda x, y, w, h: GOLD if y == 9 and x % 2 == 0 else None)
    if p.get("bulla"):  # a Roman child's gold amulet
        s.paint("over", "body", "front", lambda x, y, w, h: GOLD if (x, y) in ((3, 2), (4, 2), (3, 3), (4, 3)) else None)


def long_skirt(s, p, c, trim=None, split=False):
    """A robe or kaftan that falls to the ankles over the legs."""
    for part in ("rleg", "lleg"):
        def f(u, y, side, w, h, part=part):
            if y >= 10:
                return None
            if trim and y == 9:
                return trim
            if split and side == "front" and ((part == "rleg" and u == 3) or (part == "lleg" and u == 0)) and y > 3:
                return sh(c, 0.75)
            return s.cloth(c, u, y)
        s.around("base", part, f)
        s.around("over", part, lambda u, y, side, w, h: s.cloth(c, u, y, 0.06) if y < 9 else (trim if trim and y == 9 else None))


def kilt(s, p, c=LINEN, belt=GOLD):
    for part in ("rleg", "lleg"):
        s.around("base", part, lambda u, y, side, w, h: (sh(c, 0.9) if u % 2 and side == "front" else s.cloth(c, u, y)) if y < 5 else None)
    s.paint("base", "body", "front", lambda x, y, w, h: belt if y == 11 else s.cloth(c, x, y) if y == 10 else None)
    for side in ("back", "right", "left"):
        s.paint("base", "body", side, lambda x, y, w, h: belt if y == 11 else s.cloth(c, x, y) if y == 10 else None)


# ------------------------------------------------------------------------------------------------ the people

# ------------------------------------------------------------------------------------------------ ornament (after the user's
# reference of Ozhan: folds in the cloth, embroidered hems and cuffs, a gold opening, frogging across the chest, a
# patterned sash, a cape edged in embroidery; each empire embroiders its own way)
def band(style, x, y, base, accent=None):
    """A pixel of an embroidered band, in the empire's pattern, gold on the cloth's own darker shade."""
    dark = accent or sh(base, 0.55)
    light = mix(GOLD, WHITE, 0.25)
    if style == "checker":  # Sulthari: a gold checker
        return GOLD if (x + y) % 2 == 0 else dark
    if style == "knot":  # Nordrath: an interlace
        return GOLD if (x % 4 in (0, 1)) == (y % 2 == 0) else dark
    if style == "diamond":  # Parsivan: little diamonds of turquoise in gold
        k = x % 4
        return (60, 190, 180) if (k == 1 or k == 2) and y % 2 == 0 else GOLD if k in (0, 3) or y % 2 else dark
    if style == "zigzag":  # Khemet: a gold zigzag on lapis
        return GOLD if (x + (y % 2) * 2) % 4 < 2 else (40, 70, 170)
    if style == "meander":  # Aureum: the Greek key
        key = ["####", "#..#", "#.##"]
        return GOLD if key[y % 3][x % 4] == "#" else dark
    return light if (x + y) % 2 else GOLD


def near(c, base, d=46):
    return sum(abs(c[i] - base[i]) for i in range(3)) < d


def ornament(s, p):
    main, style = p.get("main"), p.get("band")
    if not main or not style:
        return
    long, kind, notable = p.get("long"), p.get("kind"), p.get("notable")
    accent = p.get("band_accent")
    # folds: a darker crease every third column of the cloth, and a lit edge beside it
    for part in ("body", "rleg", "lleg") if long else ("body",):
        for side in ("front", "back", "right", "left"):
            x0, y0, w, h = s.face("base", part, side)
            for y in range(h):
                for x in range(w):
                    c = s.get(x0 + x, y0 + y)
                    if near(c, main):
                        f = 0.86 if (x + (1 if part == "lleg" else 0)) % 3 == 2 else 1.06 if (x % 3 == 0 and y % 4 != 3) else 1.0
                        s.put(x0 + x, y0 + y, sh(c, f))
    # the hem: two rows of embroidery at the bottom of the robe (on the legs) or of the tunic
    if long:
        for part in ("rleg", "lleg"):
            s.around("over", part, lambda u, y, side, w, h: band(style, u, y, main, accent) if y in (7, 8) else None)
    elif kind not in ("armor", "bare", "knight", "royal"):
        s.around("base", "body", lambda u, y, side, w, h: band(style, u, y, main, accent) if y in (10, 11) else None)
    # the cuffs
    bare = p.get("bare_from", 10)
    if bare >= 3:
        for part in ("rarm", "larm"):
            s.around("base", part, lambda u, y, side, w, h: band(style, u, y, main, accent) if y in (bare - 2, bare - 1) else None)
    # the collar and the opening down the front
    if kind in ("kaftan", "robe", "tunic", "dress"):
        s.paint("base", "body", "front", lambda x, y, w, h: band(style, x, y, main, accent) if y == 0 and 1 <= x <= 6 else None)
    if kind in ("kaftan", "robe"):
        edge = GOLD if notable else sh(main, 0.68)  # the opening: gold for the great, a darker fold for the rest
        s.paint("base", "body", "front", lambda x, y, w, h: (edge if x == 3 else sh(edge, 0.8)) if x in (3, 4) and y > 0 else None)
        if long:
            s.paint("over", "rleg", "front", lambda x, y, w, h: edge if x == 3 and y < 7 else None)
            s.paint("over", "lleg", "front", lambda x, y, w, h: sh(edge, 0.8) if x == 0 and y < 7 else None)
    if kind == "bare":  # Khemet's linen kilt: its hem worked in gold and lapis
        for part in ("rleg", "lleg"):
            s.around("base", part, lambda u, y, side, w, h: band(style, u, y, main, accent) if y in (3, 4) else None)
    if not notable:
        return
    # frogging: gold cords across the chest
    if kind in ("kaftan", "robe") and p.get("frogging", True):
        s.paint("base", "body", "front", lambda x, y, w, h: (sh(GOLD, 0.75) if x in (1, 6) else GOLD) if y in (1, 3, 5) and 1 <= x <= 6 else None)
    # the sash: gold, patterned, a stone at the middle
    sash = p.get("noble_sash", GOLD)
    if kind in ("kaftan", "robe", "tunic", "dress"):
        s.around("base", "body", lambda u, y, side, w, h: (sh(sash, 1.1) if y == 7 else sh(sash, 0.85)) if y in (7, 8) else None)
        s.paint("base", "body", "front", lambda x, y, w, h: p.get("jewel", (180, 30, 40)) if y in (7, 8) and x in (3, 4)
                else (sh(p.get("jewel", (180, 30, 40)), 0.7) if y == 8 and x % 2 == 0 else None))
    # shoulders edged in embroidery
    for part in ("rarm", "larm"):
        s.around("over", part, lambda u, y, side, w, h: band(style, u, y, main, accent) if y == 0 else None)
        s.paint("over", part, "top", lambda x, y, w, h: band(style, x, y, main, accent))
    # the cape: down the back, edged with the band, its sides showing
    cape = p.get("cape")
    if cape:
        edge = lambda x, y, w, h: x in (0, w - 1) or y >= h - 2
        s.paint("over", "body", "back", lambda x, y, w, h: band(style, x, y, cape, accent) if edge(x, y, w, h) else
                s.cloth(sh(cape, 0.88 if x % 3 == 1 else 1.0), x, y, 0.05))
        for side in ("right", "left"):
            s.paint("over", "body", side, lambda x, y, w, h: (band(style, x, y, cape, accent) if y >= h - 2 or x == (w - 1 if side == "right" else 0)
                                                             else s.cloth(cape, x, y)) if (x >= 2 if side == "right" else x <= 1) else None)


def stern(s, p):
    """A stern face: thick brows, a frown between them, shadows under the cheekbones."""
    if not p.get("stern"):
        return
    fx, fy, _, _ = s.face("base", "head", "front")
    skin = SKIN[p["skin"]]
    brow = HAIR.get(p.get("brow", "grey" if p.get("age") == "old" else p.get("hair", "dark")))
    for x in (1, 2, 5, 6):
        s.put(fx + x, fy + 3, sh(brow, 0.85 if x in (2, 5) else 1.0))
    s.put(fx + 3, fy + 3, sh(skin, 0.8))
    s.put(fx + 4, fy + 3, sh(skin, 0.8))
    for x in (0, 7):
        s.put(fx + x, fy + 5, sh(skin, 0.86))
    s.put(fx + 2, fy + 5, sh(skin, 0.94))
    s.put(fx + 5, fy + 5, sh(skin, 0.94))


def great_beard(s, p):
    """A long beard with strands, a moustache over the mouth, down the chest to a point."""
    if p.get("beard") != "long":
        return
    bc = HAIR.get(p.get("beard_color", p.get("hair", "grey")))
    fx, fy, _, _ = s.face("base", "head", "front")
    for x in range(8):
        for y in (5, 6, 7):
            if y == 5 and x in (3, 4):
                s.put(fx + x, fy + y, sh(bc, 0.8))  # the moustache's middle
            elif y == 6 and x in (3, 4):
                s.put(fx + x, fy + y, sh(bc, 0.62))  # the mouth half hidden
            elif x in (0, 7) and y == 5:
                continue
            else:
                s.put(fx + x, fy + y, sh(bc, 1.12 if (x + y) % 3 == 0 else 0.9 if x % 2 else 1.0))
    s.paint("over", "body", "front", lambda x, y, w, h: sh(bc, 1.1 if (x + y) % 3 == 0 else 0.88 if x % 2 else 1.0)
            if (y < 4 and 1 <= x <= 6) or (y == 4 and 2 <= x <= 5) or (y == 5 and 3 <= x <= 4) else None)



# ------------------------------------------------------------------------------------------------ the Bearers' outfits
# The class outfit every Bearer wears over their own skin (players and the hero NPCs alike, so a hero never outshines
# a player): only the outer layer, the face and the hands left bare. After the hero cards, kept plain beside the armour.
OUTFIT_OUT = os.path.join("src", "main", "resources", "assets", "sofe", "textures", "entity", "outfit")


def outfit(cls):
    s = Skin(sum(map(ord, cls)) * 7)
    w = lambda c, x, y, f=0.05: s.cloth(c, x, y, f)
    if cls == "knight":
        mail, tab, steel = (170, 176, 184), (40, 64, 170), (196, 202, 210)
        ring = lambda x, y: sh(mail, 1.1 if (x + y) % 2 == 0 else 0.84)
        s.around("over", "body", lambda x, y, side, ww, h: ring(x, y))
        for side in ("front", "back"):
            s.paint("over", "body", side, lambda x, y, ww, h: (GOLD if x in (1, 6) or y == 11 else w(tab, x, y)) if 1 <= x <= 6 and y >= 1 else None)
        s.paint("over", "body", "front", lambda x, y, ww, h: GOLD if (x, y) in ((3, 4), (4, 4), (2, 5), (5, 5), (3, 6), (4, 6), (3, 3), (4, 3)) else None)
        s.paint("over", "body", "front", lambda x, y, ww, h: (LEATHER if not (x in (3, 4)) else GOLD) if y == 8 and x in (0, 7, 3, 4) else None)
        s.paint("over", "body", "top", lambda x, y, ww, h: steel)
        for part in ("rarm", "larm"):
            s.around("over", part, lambda x, y, side, ww, h: (sh(steel, 1.1 if y == 0 else 0.9) if y < 3 else ring(x, y)) if y < 9 else None)
            s.paint("over", part, "top", lambda x, y, ww, h: steel)
        for part in ("rleg", "lleg"):
            s.around("over", part, lambda x, y, side, ww, h: (ring(x, y) if y < 5 else sh(steel, 1.05 - 0.04 * (y % 3))) if y < 9 else None)
    elif cls == "necromancer":
        robe, teal, strap = (36, 40, 42), (60, 190, 180), (90, 64, 40)
        s.around("over", "body", lambda x, y, side, ww, h: w(sh(robe, 0.86 if x % 3 == 2 else 1.0), x, y))
        s.paint("over", "body", "front", lambda x, y, ww, h: teal if (y == 0 and 1 <= x <= 6) or (x in (3, 4) and y < 11) and (x == 3) else None)
        s.paint("over", "body", "front", lambda x, y, ww, h: strap if abs(x - (7 - y * 0.62)) < 0.7 and y < 11 else None)
        s.paint("over", "body", "front", lambda x, y, ww, h: (GOLD if (x, y) == (5, 3) else None))
        s.paint("over", "body", "top", lambda x, y, ww, h: sh(robe, 0.9))
        for part in ("rarm", "larm"):
            s.around("over", part, lambda x, y, side, ww, h: (teal if y == 8 else w(robe, x, y)) if y < 9 else None)
            s.paint("over", part, "top", lambda x, y, ww, h: robe)
        for part in ("rleg", "lleg"):
            s.around("over", part, lambda x, y, side, ww, h: (teal if y == 9 else w(sh(robe, 0.86 if x % 3 == 1 else 1.0), x, y)) if y < 10 else None)
    elif cls == "sorceress":
        robe, star = (44, 62, 160), mix(GOLD, WHITE, 0.3)
        s.around("over", "body", lambda x, y, side, ww, h: w(sh(robe, 0.86 if x % 3 == 2 else 1.0), x, y))
        s.paint("over", "body", "front", lambda x, y, ww, h: GOLD if (y == 0 and 1 <= x <= 6) or (x == 3 and y > 0) else None)
        s.paint("over", "body", "front", lambda x, y, ww, h: (110, 60, 150) if y in (7, 8) and x != 3 else None)
        s.around("over", "body", lambda x, y, side, ww, h: star if (x * 5 + y * 3) % 13 == 0 and y < 7 else None)
        s.paint("over", "body", "top", lambda x, y, ww, h: robe)
        for part in ("rarm", "larm"):
            s.around("over", part, lambda x, y, side, ww, h: (GOLD if y == 9 else w(robe, x, y)) if y < 10 else None)
            s.paint("over", part, "top", lambda x, y, ww, h: robe)
        for part in ("rleg", "lleg"):
            s.around("over", part, lambda x, y, side, ww, h: (GOLD if y == 9 else star if (x * 3 + y * 7) % 17 == 0 else
                                                              w(sh(robe, 0.86 if x % 3 == 1 else 1.0), x, y)) if y < 10 else None)
    elif cls == "thief":
        leather, dark, red, steel = (60, 54, 52), (40, 36, 36), (160, 36, 36), (190, 196, 204)
        s.around("over", "body", lambda x, y, side, ww, h: w(leather if y > 1 else dark, x, y, 0.07))
        s.paint("over", "body", "front", lambda x, y, ww, h: dark if x in (3, 4) and y < 7 else None)
        s.paint("over", "body", "front", lambda x, y, ww, h: (90, 64, 40) if abs(x - y * 0.62) < 0.7 and y < 11 else None)
        s.paint("over", "body", "front", lambda x, y, ww, h: steel if (x, y) in ((2, 3), (4, 6)) else None)  # throwing knives on the strap
        s.around("over", "body", lambda x, y, side, ww, h: sh(red, 1.05 if y == 7 else 0.85) if y in (7, 8) else None)
        s.paint("over", "body", "front", lambda x, y, ww, h: sh(red, 0.8) if x == 6 and 9 <= y <= 11 else None)
        s.paint("over", "body", "top", lambda x, y, ww, h: dark)
        for part in ("rarm", "larm"):
            s.around("over", part, lambda x, y, side, ww, h: (LEATHER if y >= 6 else w(leather, x, y, 0.07)) if y < 10 else None)
            s.paint("over", part, "top", lambda x, y, ww, h: dark)
        for part in ("rleg", "lleg"):
            s.around("over", part, lambda x, y, side, ww, h: (sh(leather, 0.8) if (x + y) % 2 == 0 else leather) if 4 <= y < 10 else None)
    elif cls == "king":
        crimson, fur, sash = (150, 30, 36), (230, 226, 216), (40, 64, 150)
        s.around("over", "body", lambda x, y, side, ww, h: w(sh(crimson, 0.86 if x % 3 == 2 else 1.0), x, y))
        s.paint("over", "body", "front", lambda x, y, ww, h: (GOLD if x == 3 else sh(GOLD, 0.8)) if x in (3, 4) and y > 1 else None)
        s.paint("over", "body", "front", lambda x, y, ww, h: GOLD if y in (3, 5) and 2 <= x <= 5 else None)
        s.around("over", "body", lambda x, y, side, ww, h: sh(sash, 1.05 if y == 7 else 0.85) if y in (7, 8) else None)
        s.around("over", "body", lambda x, y, side, ww, h: w(fur, x, y, 0.12) if y < 2 and (side != "front" or x < 2 or x > 5 or y == 0) else None)
        s.paint("over", "body", "top", lambda x, y, ww, h: w(fur, x, y, 0.12))
        for part in ("rarm", "larm"):
            s.around("over", part, lambda x, y, side, ww, h: (w(fur, x, y, 0.12) if y < 2 else GOLD if y == 9 else w(crimson, x, y)) if y < 10 else None)
            s.paint("over", part, "top", lambda x, y, ww, h: w(fur, x, y, 0.12))
        for part in ("rleg", "lleg"):
            s.around("over", part, lambda x, y, side, ww, h: (GOLD if y == 8 else w(sh(crimson, 0.86 if x % 3 == 1 else 1.0), x, y)) if y < 9 else None)
    os.makedirs(OUTFIT_OUT, exist_ok=True)
    s.img.save(os.path.join(OUTFIT_OUT, cls + ".png"))


WORN = {}


def person(name, p, outfit):
    s = Skin(sum(ord(ch) for ch in name))
    head(s, p)
    outfit(s, p)
    ornament(s, p)
    stern(s, p)
    great_beard(s, p)
    if p.get("hat") not in ("turban", "tall_turban", "grand_turban", "fez"):  # these are worn in 3D (NpcAccessoryLayer)
        headwear(s, p)
    s.save(name)
    WORN[name] = p  # the 3D parts are painted once every skin is done (accessories)
    return s


def sulthari(top, trim=GOLD, pants=(150, 40, 40), shoes=(140, 40, 30), long=False, sash=None, sleeve=None, kind="kaftan", cuff=None):
    def f(s, p):
        p.setdefault("sash", sash)
        p.update(main=top, band=p.get("band", "checker"), long=long, kind=kind)
        body(s, p, kind, top, trim)
        limbs(s, p, {"color": sleeve or top, "cuff": cuff or trim}, {"color": pants, "shoes": shoes, "shoe_rows": 2})
        if long:
            long_skirt(s, p, top, trim, split=True)
    return f


def nordic(top, trim=(170, 140, 80), pants=(90, 80, 70), boots=(80, 56, 36), kind="tunic", belt=LEATHER, wrap=True, skirt=False):
    def f(s, p):
        p.update(main=top, band=p.get("band", "knot"), long=skirt, kind=kind)
        body(s, p, kind, top, trim, belt)
        limbs(s, p, {"color": top, "cuff": trim, "fur": p.get("fur_sleeves")}, {"color": pants, "shoes": boots, "shoe_rows": 3, "wrap": wrap})
        if skirt:
            long_skirt(s, p, top, trim)
    return f


def parsivan(top, trim=(60, 190, 180), pants=(70, 40, 100), shoes=(90, 40, 110), long=False, kind="robe", sleeve=None):
    def f(s, p):
        p.update(main=top, band=p.get("band", "diamond"), long=long, kind=kind)
        body(s, p, kind, top, trim)
        limbs(s, p, {"color": sleeve or top, "cuff": trim}, {"color": pants, "shoes": shoes, "shoe_rows": 2})
        if long:
            long_skirt(s, p, top, trim)
    return f


def khemet_man(kilt_c=LINEN, extra=None):
    def f(s, p):
        p.setdefault("collar", True)
        p.update(main=kilt_c, band=p.get("band", "zigzag"), long=False, kind="bare", bare_from=0)
        body(s, p, "bare", LINEN)
        limbs(s, p, {"color": SKIN[p["skin"]], "bare_from": 0, "bracer": GOLD}, {"color": LINEN, "bare": True, "kilt": 5, "sandals": True, "shoe_rows": 2})
        kilt(s, p, kilt_c)
        if extra:
            extra(s, p)
    return f


def khemet_dress(c=LINEN, trim=GOLD):
    def f(s, p):
        p.setdefault("collar", True)
        p.update(main=c, band=p.get("band", "zigzag"), long=True, kind="dress", bare_from=0)
        body(s, p, "dress", c, trim, belt=(40, 70, 170))
        limbs(s, p, {"color": SKIN[p["skin"]], "bare_from": 0, "bracer": GOLD}, {"color": c, "sandals": True, "shoe_rows": 2})
        long_skirt(s, p, c)
    return f


def roman(c=WHITE, trim=None, legs="tunic", shoes=LEATHER):
    def f(s, p):
        p.update(main=c, band=p.get("band", "meander"), long=bool(p.get("toga") or p.get("stola")), kind="toga" if p.get("toga") else "tunic",
                 bare_from=4)
        body(s, p, "toga" if p.get("toga") else "tunic", c, trim, belt=p.get("belt"))
        limbs(s, p, {"color": c, "bare_from": 4}, {"color": c, "bare": legs == "tunic", "kilt": 4, "sandals": True, "shoe_rows": 3})
        if p.get("toga") or p.get("stola"):
            long_skirt(s, p, c, trim)
    return f


R = (150, 34, 40)  # crimson
PEOPLE = {
    # --- the Bearers: plain base looks after their hero cards (colours and cut), never grander than a player in the
    # mod's armour; the gold, crowns and glowing eyes of the cards are for the armour sets the players earn
    "cassian": ({"skin": 2, "hair": "brown", "eyes": "blue"},
                lambda s, p: (body(s, p, "tunic", (60, 70, 110), None, LEATHER),
                              limbs(s, p, {"color": (120, 124, 130), "bare_from": 10}, {"color": (90, 94, 104), "shoes": (70, 50, 34), "shoe_rows": 3}))),
    "ankhareth": ({"skin": 2, "hair": "black", "eyes": "grey"},
                  lambda s, p: (body(s, p, "tunic", (40, 44, 46), None, (70, 50, 34)),
                                limbs(s, p, {"color": (40, 44, 46), "bare_from": 10}, {"color": (40, 44, 46), "shoes": (54, 40, 30), "shoe_rows": 3}))),
    "shirin": ({"skin": 3, "hair": "black", "style": "long", "eyes": "dark", "female": True, "kohl": True},
               lambda s, p: (body(s, p, "robe", (44, 62, 160), None, (90, 70, 50)),
                             limbs(s, p, {"color": (44, 62, 160), "bare_from": 10}, {"color": (44, 62, 160), "shoes": (60, 40, 30), "shoe_rows": 2}))),
    "rurik": ({"skin": 2, "hair": "dark", "eyes": "grey", "scar": True},
              lambda s, p: (body(s, p, "tunic", (52, 48, 46), None, (70, 50, 34)),
                            limbs(s, p, {"color": (52, 48, 46), "bare_from": 10}, {"color": (44, 42, 44), "shoes": (44, 34, 28), "shoe_rows": 3}))),
    "azhar": ({"skin": 3, "hair": "dark", "beard": "short", "eyes": "brown"},
              lambda s, p: (body(s, p, "tunic", (150, 30, 36), None, LEATHER),
                            limbs(s, p, {"color": (150, 30, 36), "bare_from": 10}, {"color": (60, 40, 40), "shoes": (84, 52, 32), "shoe_rows": 3}))),
    # --- Sulthari
    "ozhan": ({"skin": 3, "hair": "grey", "style": "long", "fringe": 0, "beard": "long", "age": "old", "stern": True, "hat": "grand_turban",
               "hat_color": (44, 130, 52), "jewel": (190, 20, 30), "notable": True, "cape": (150, 22, 30), "bare_from": 10},
              sulthari((150, 22, 30), GOLD, pants=(120, 20, 26), shoes=(84, 52, 32), long=True)),
    "council_elder": ({"skin": 3, "hair": "white", "style": "long", "fringe": 0, "beard": "long", "age": "old", "stern": True, "hat": "grand_turban",
                       "hat_color": (34, 50, 120), "jewel": (40, 160, 110), "notable": True, "cape": (34, 50, 110), "bare_from": 10},
                      sulthari((34, 50, 110), GOLD, pants=(30, 40, 90), shoes=(84, 52, 32), long=True)),
    "yusuf": ({"skin": 3, "hair": "dark", "beard": "moustache", "hat": "cap", "hat_color": (120, 70, 40), "shirt": LINEN},
              sulthari((110, 70, 40), (160, 120, 60), pants=(90, 60, 40), kind="vest", sleeve=LINEN, sash=(150, 40, 40))),
    "selim": ({"stern": False, "skin": 3, "hair": "black", "beard": "moustache", "hat": "fez", "hat_color": (170, 30, 36), "shirt": WHITE, "buttons": True},
              sulthari((110, 24, 40), GOLD, pants=(40, 40, 60), kind="vest", sleeve=WHITE)),
    "ferid": ({"skin": 3, "hair": "dark", "beard": "short", "hat": "goggles", "apron": (110, 74, 44), "bib": True},
              sulthari((90, 40, 110), GOLD, pants=(60, 40, 70), sash=(40, 120, 100))),
    "dilara": ({"skin": 3, "hair": "dark", "style": "bun", "female": True, "hat": "scarf", "hat_color": (170, 40, 40), "apron": (90, 60, 36), "bib": True},
               lambda s, p: (body(s, p, "tunic", (160, 120, 80), (110, 70, 40)), limbs(s, p, {"color": (160, 120, 80), "bare_from": 5, "bracer": LEATHER},
                                                                                       {"color": (80, 60, 50), "shoes": (60, 40, 30)}))),
    "kerem": ({"skin": 3, "hair": "black", "beard": "short", "loupe": True, "hat": "fez", "hat_color": (40, 40, 90), "shirt": WHITE},
              sulthari((30, 70, 90), GOLD, pants=(40, 40, 60), kind="vest", sleeve=WHITE)),
    "nilufar": ({"skin": 2, "hair": "dark", "female": True, "hat": "scarf", "hat_color": (226, 222, 230), "eyes": "grey"},
                sulthari((200, 196, 210), (110, 60, 150), pants=(180, 176, 190), shoes=(90, 70, 100), long=True, sash=(110, 60, 150))),
    "zahir": ({"skin": 4, "hair": "black", "beard": "full", "eyes": "amber", "hat": "hood", "hat_color": (176, 146, 100), "mask": (160, 130, 90), "cape": (120, 90, 60)},
              sulthari((150, 120, 80), (110, 80, 50), pants=(120, 96, 66), shoes=(90, 64, 40), long=True, sash=(170, 60, 40))),
    "citizen_baker": ({"skin": 3, "hair": "dark", "female": True, "hat": "scarf", "hat_color": WHITE, "apron": (240, 236, 226), "bib": True, "dust": True},
                      sulthari((200, 120, 50), (150, 80, 30), pants=(160, 90, 40), long=True)),
    "citizen_water_carrier": ({"skin": 4, "hair": "black", "beard": "stubble", "hat": "turban", "hat_color": (150, 110, 70), "shirt": LINEN},
                              lambda s, p: (body(s, p, "vest", (70, 90, 120), (50, 60, 80)), limbs(s, p, {"color": LINEN, "bare_from": 5},
                                                                                                    {"color": (110, 90, 70), "shoes": (80, 56, 36), "shoe_rows": 2}))),
    "citizen_scholar": ({"skin": 2, "hair": "dark", "female": True, "hat": "scarf", "hat_color": (30, 50, 100), "eyes": "green", "bandolier": (110, 74, 44)},
                        sulthari((40, 130, 130), GOLD, pants=(30, 70, 80), long=True)),
    "citizen_guard": ({"skin": 3, "hair": "dark", "beard": "moustache", "hat": "bork", "scales": True, "plate": (170, 176, 186)},
                      lambda s, p: (body(s, p, "armor", (150, 30, 36), GOLD, (110, 30, 30)), limbs(s, p, {"color": (150, 30, 36), "cuff": GOLD},
                                                                                                    {"color": (40, 50, 110), "shoes": (140, 30, 30), "shoe_rows": 3}),
                                    long_skirt(s, p, (150, 30, 36), GOLD, split=True))),
    "citizen_weaver": ({"skin": 3, "hair": "black", "female": True, "hat": "scarf", "hat_color": (150, 40, 110)},
                       sulthari((180, 60, 120), GOLD, pants=(120, 40, 90), long=True, sash=(240, 200, 80))),
    "citizen_pilgrim": ({"skin": 4, "hair": "grey", "beard": "long", "age": "old", "hat": "turban", "hat_color": WHITE},
                        sulthari(WHITE, (210, 200, 180), pants=WHITE, shoes=(150, 120, 90), long=True, sash=(150, 120, 90))),
    "citizen_widow": ({"skin": 3, "hair": "grey", "female": True, "age": "old", "hat": "scarf", "hat_color": (34, 30, 34)},
                      sulthari((44, 40, 46), (70, 64, 72), pants=(40, 36, 40), shoes=(30, 26, 26), long=True)),
    "citizen_clockmaker": ({"skin": 3, "hair": "brown", "beard": "moustache", "hat": "goggles", "apron": (110, 74, 44), "bib": True, "shirt": LINEN},
                           sulthari((70, 70, 80), (190, 150, 70), pants=(60, 50, 40), kind="vest", sleeve=LINEN)),
    "citizen_storyteller": ({"skin": 3, "hair": "grey", "beard": "full", "age": "old", "hat": "fez", "hat_color": (160, 30, 36)},
                            sulthari((40, 100, 50), GOLD, pants=(100, 40, 40), long=True, sash=(200, 170, 60))),
    "citizen_child": ({"skin": 3, "hair": "black", "age": "child"}, sulthari((60, 110, 170), (240, 200, 80), pants=(90, 60, 40), kind="tunic")),
    "citizen_tram_keeper": ({"skin": 3, "hair": "brown", "beard": "moustache", "hat": "fez", "hat_color": (40, 60, 110)},
                            sulthari((40, 60, 110), (190, 150, 70), pants=(50, 50, 60), kind="vest", sleeve=LINEN)),
    "citizen_fisherman": ({"skin": 4, "hair": "black", "beard": "stubble", "hat": "turban", "hat_color": (60, 120, 140), "shirt": LINEN},
                          sulthari((70, 120, 150), (220, 210, 190), pants=(90, 80, 60), shoes=(110, 80, 50), kind="tunic", sash=(200, 170, 60))),
    "citizen_veteran": ({"skin": 3, "hair": "grey", "beard": "full", "age": "old", "hat": "bork", "plate": (150, 150, 160)},
                        sulthari((120, 40, 40), (170, 150, 110), pants=(70, 60, 50), shoes=(80, 56, 36), long=True, sash=(170, 150, 110))),
    "citizen_courier": ({"skin": 3, "hair": "dark", "female": True, "hat": "headband", "hat_color": (200, 120, 40), "bandolier": (110, 74, 44)},
                        sulthari((210, 140, 60), (120, 60, 30), pants=(120, 70, 40), kind="tunic")),
    "citizen_astronomer": ({"skin": 2, "hair": "black", "female": True, "hat": "scarf", "hat_color": (40, 40, 100), "eyes": "green"},
                           sulthari((50, 50, 120), (210, 190, 90), pants=(40, 40, 90), long=True, sash=(210, 190, 90))),
    "citizen_tea_seller": ({"skin": 4, "hair": "dark", "female": True, "hat": "scarf", "hat_color": (170, 40, 40), "apron": (230, 220, 200)},
                           sulthari((160, 50, 50), GOLD, pants=(130, 40, 40), long=True, sash=(60, 140, 90))),
    "bazaar_spicer": ({"skin": 4, "hair": "black", "female": True, "hat": "scarf", "hat_color": (220, 120, 30)},
                      sulthari((200, 160, 40), (170, 60, 30), pants=(170, 70, 30), long=True, sash=(170, 60, 30))),
    "bazaar_weaver": ({"skin": 3, "hair": "dark", "female": True, "style": "long", "hat": "headband", "hat_color": GOLD},
                      sulthari((110, 50, 140), GOLD, pants=(80, 40, 100), long=True, sash=(60, 160, 150))),
    "bazaar_fruiterer": ({"skin": 4, "hair": "black", "beard": "moustache", "apron": (90, 140, 70), "hat": "cap", "hat_color": (60, 90, 50)},
                         sulthari((220, 210, 190), (150, 140, 120), pants=(80, 70, 60), kind="tunic")),
    "bazaar_lampwright": ({"skin": 3, "hair": "dark", "beard": "full", "hat": "fez", "hat_color": (150, 30, 30), "apron": (100, 66, 40), "bib": True},
                          sulthari((60, 60, 70), (200, 150, 60), pants=(50, 50, 60), kind="tunic")),
    "kasim": ({"skin": 3, "hair": "black", "eyes": "amber", "hat": "hood", "hat_color": (40, 30, 50), "mask": (50, 36, 60), "cape": (40, 30, 50)},
              sulthari((60, 40, 70), (130, 100, 150), pants=(40, 30, 50), shoes=(30, 24, 30), long=True)),
    # --- Nordrath
    "nordrath_shieldmaiden": ({"skin": 1, "hair": "blonde", "style": "braids", "female": True, "eyes": "blue", "hat": "helm", "hat_color": IRON, "cape": (150, 40, 40), "plate": (130, 134, 140)},
                              lambda s, p: (body(s, p, "armor", (60, 80, 120), (150, 40, 40), LEATHER),
                                            limbs(s, p, {"color": (60, 80, 120), "bracer": LEATHER, "bare_from": 11}, {"color": (70, 60, 50), "shoes": (70, 50, 34), "wrap": True}))),
    "nordrath_fisher": ({"skin": 1, "hair": "auburn", "beard": "full", "eyes": "blue", "hat": "cap", "hat_color": (60, 70, 90)},
                        nordic((110, 120, 100), (80, 90, 70), pants=(80, 70, 60))),
    "nordrath_widow": ({"skin": 1, "hair": "grey", "style": "braids", "female": True, "age": "old", "eyes": "grey", "fur_collar": True},
                       nordic((60, 56, 60), (100, 90, 80), skirt=True)),
    "nordrath_apprentice": ({"skin": 1, "hair": "blonde", "eyes": "blue", "apron": (90, 60, 40), "bib": True},
                            nordic((140, 110, 80), (100, 80, 60))),
    "nordrath_hunter": ({"skin": 1, "hair": "auburn", "style": "braids", "female": True, "eyes": "green", "hat": "fur_hood", "hat_color": (110, 84, 60), "bandolier": LEATHER},
                        nordic((70, 100, 60), (120, 90, 60), pants=(80, 66, 50))),
    "nordrath_elder": ({"notable": True, "stern": True, "skin": 1, "hair": "white", "style": "braids", "female": True, "age": "old", "eyes": "blue", "fur_collar": True, "cape": (110, 84, 60)},
                       nordic((120, 40, 40), (200, 170, 90), skirt=True)),
    "nordrath_child": ({"skin": 1, "hair": "blonde", "style": "pigtails", "female": True, "age": "child", "eyes": "blue"},
                       nordic((170, 50, 40), (220, 190, 120))),
    "nordrath_brewer": ({"skin": 1, "hair": "brown", "beard": "long", "eyes": "brown", "apron": (180, 160, 120)},
                        nordic((120, 90, 60), (90, 70, 50))),
    "nordrath_raider": ({"stern": True, "skin": 1, "hair": "grey", "beard": "long", "braided_beard": True, "age": "old", "scar": True, "eyes": "grey", "hat": "helm", "hat_color": IRON, "plate": IRON, "fur_collar": True},
                        lambda s, p: (body(s, p, "armor", (70, 60, 50), None, LEATHER),
                                      limbs(s, p, {"color": (90, 70, 50), "bracer": IRON, "bare_from": 11, "fur": True}, {"color": (70, 60, 50), "shoes": (60, 44, 30), "wrap": True}))),
    "nordrath_skald": ({"notable": True, "skin": 1, "hair": "red", "beard": "full", "eyes": "green", "fur_collar": True, "cape": (40, 90, 60)},
                       nordic((50, 110, 70), GOLD)),
    "nordrath_furrier": ({"skin": 1, "hair": "dark", "style": "braids", "female": True, "eyes": "brown", "fur_collar": True, "fur_sleeves": True},
                         nordic((130, 100, 70), FUR, kind="vest")),
    "nordrath_runesmith": ({"skin": 1, "hair": "black", "beard": "full", "eyes": "teal", "apron": (80, 54, 34), "bib": True},
                           nordic((70, 70, 80), (90, 160, 200))),
    "nordrath_gambler": ({"skin": 1, "hair": "black", "style": "long", "female": True, "eyes": "grey", "kohl": True, "hat": "headband", "hat_color": (30, 30, 30), "cape": (30, 30, 34)},
                         nordic((50, 46, 50), (120, 120, 130), pants=(40, 38, 40))),
    # --- Parsivan
    "parsivan_poet": ({"notable": True, "cape": (70, 30, 110), "skin": 3, "hair": "black", "style": "long", "female": True, "eyes": "dark", "kohl": True, "hat": "circlet"},
                      parsivan((110, 50, 160), (60, 190, 180), long=True)),
    "parsivan_gardener": ({"skin": 3, "hair": "black", "beard": "short", "hat": "turban", "hat_color": (60, 160, 150), "shirt": LINEN},
                          parsivan((60, 110, 60), (200, 180, 90), pants=(80, 70, 50), kind="vest", sleeve=LINEN)),
    "parsivan_dancer": ({"skin": 3, "hair": "black", "style": "long", "female": True, "kohl": True, "hat": "scarf", "hat_color": (180, 60, 160), "veil": True, "coins": True},
                        parsivan((60, 180, 170), GOLD, long=True)),
    "isfaran_perfumer": ({"skin": 3, "hair": "dark", "female": True, "hat": "scarf", "hat_color": (170, 140, 210), "eyes": "green"},
                         parsivan((50, 140, 140), (170, 140, 210), long=True)),
    "isfaran_guard": ({"stern": True, "skin": 3, "hair": "black", "beard": "full", "hat": "helm", "hat_color": STEEL, "plume": (120, 50, 160), "scales": True, "plate": (110, 170, 170)},
                      lambda s, p: (body(s, p, "armor", (100, 40, 140), GOLD, (60, 40, 30)),
                                    limbs(s, p, {"color": (100, 40, 140), "pauldron": (110, 170, 170), "bracer": STEEL}, {"color": (60, 30, 80), "shoes": (50, 30, 40), "shoe_rows": 3}),
                                    long_skirt(s, p, (100, 40, 140), GOLD, split=True))),
    "isfaran_astronomer": ({"notable": True, "stern": True, "cape": (30, 36, 90), "frogging": False, "skin": 3, "hair": "white", "beard": "long", "age": "old", "hat": "tall_hat", "hat_color": (30, 36, 90), "stars": True},
                           parsivan((30, 36, 90), GOLD, long=True)),
    "isfaran_child": ({"skin": 3, "hair": "black", "style": "long", "female": True, "age": "child"},
                      parsivan((220, 120, 170), (240, 220, 120), long=True)),
    "isfaran_carpet_weaver": ({"skin": 3, "hair": "dark", "beard": "short", "hat": "turban", "hat_color": (150, 40, 50), "shirt": LINEN},
                              parsivan((160, 50, 60), GOLD, pants=(70, 40, 60), kind="vest", sleeve=LINEN)),
    # --- Khemet
    "khemet_embalmer": ({"skin": 5, "hair": "black", "style": "wig", "female": True, "kohl": True, "apron": (90, 80, 70)}, khemet_dress((210, 200, 180), (90, 80, 70))),
    "khemet_ferryman": ({"skin": 5, "hair": "black", "beard": "stubble", "hat": "headband", "hat_color": (40, 70, 170), "collar": False, "bandolier": (150, 120, 70)}, khemet_man()),
    "khemet_scribe": ({"skin": 4, "style": "shaved", "kohl": True}, khemet_dress(LINEN, (40, 70, 170))),
    "neferet_priest": ({"notable": True, "stern": True, "skin": 5, "style": "shaved", "kohl": True, "leopard": True}, khemet_man()),
    "neferet_boatman": ({"skin": 6, "hair": "black", "style": "short", "collar": False, "bandolier": (170, 140, 80)}, khemet_man()),
    "neferet_potter": ({"skin": 5, "hair": "black", "style": "wig", "female": True, "kohl": True, "apron": (170, 100, 70)}, khemet_dress()),
    "neferet_child": ({"skin": 5, "style": "sidelock", "age": "child", "collar": False}, khemet_man()),
    "neferet_guard": ({"stern": True, "skin": 5, "kohl": True, "hat": "nemes", "scales": True, "plate": (190, 140, 70)},
                      lambda s, p: (body(s, p, "armor", LINEN, None, GOLD), p.update(collar=True), body(s, p, "armor", LINEN, None, GOLD),
                                    limbs(s, p, {"color": SKIN[p["skin"]], "bare_from": 0, "bracer": GOLD}, {"color": LINEN, "bare": True, "kilt": 5, "sandals": True, "shoe_rows": 2}),
                                    kilt(s, p))),
    # --- Aureum
    "aureum_senator": ({"notable": True, "stern": True, "skin": 2, "hair": "grey", "age": "old", "toga": True}, roman(WHITE, (110, 30, 110))),
    "aurelion_magistrate": ({"notable": True, "skin": 2, "hair": "dark", "style": "bun", "female": True, "toga": True, "hat": "wreath", "hat_color": GOLD}, roman(WHITE, (110, 30, 110))),
    "aureum_gladiator": ({"skin": 3, "hair": "dark", "beard": "stubble", "scar": True, "hat": "gladiator", "bandolier": LEATHER, "belt": GOLD},
                         lambda s, p: (body(s, p, "bare", WHITE, None, (180, 140, 60)),
                                       limbs(s, p, {"color": (200, 170, 100), "bare_from": 0, "bracer": LEATHER}, {"color": (150, 40, 40), "bare": True, "kilt": 3, "sandals": True, "greave": (200, 160, 90), "shoe_rows": 3}),
                                       kilt(s, p, (150, 40, 40), GOLD),
                                       s.around("over", "rarm", lambda u, y, side, w, h: sh((200, 160, 90), 1.1 if y % 2 else 0.9) if y < 9 else None))),
    "aureum_widow": ({"skin": 2, "hair": "grey", "female": True, "age": "old", "hat": "palla", "hat_color": (70, 66, 74), "stola": True}, roman((90, 84, 96), (60, 56, 64), legs="long")),
    "aurelion_legionary": ({"stern": True, "skin": 2, "hair": "brown", "beard": "stubble", "hat": "helm", "hat_color": STEEL, "cheeks": True, "crest": (190, 30, 30), "bands": True, "plate": (186, 190, 196)},
                           lambda s, p: (body(s, p, "armor", (170, 34, 38), None, LEATHER),
                                         limbs(s, p, {"color": (170, 34, 38), "bare_from": 4, "pauldron": (186, 190, 196), "bracer": LEATHER},
                                               {"color": (170, 34, 38), "bare": True, "kilt": 3, "sandals": True, "shoe_rows": 3}),
                                         kilt(s, p, (170, 34, 38), LEATHER),
                                         [s.paint("over", part, "front", lambda x, y, w, h: LEATHER if y < 3 and x % 2 == 0 else None) for part in ("rleg", "lleg")])),
    "aurelion_baker": ({"skin": 2, "hair": "brown", "style": "bun", "female": True, "hat": "scarf", "hat_color": (220, 210, 190), "apron": WHITE, "dust": True, "stola": True},
                       roman((190, 120, 70), (150, 90, 50), legs="long")),
    "aurelion_child": ({"skin": 2, "hair": "brown", "age": "child", "bulla": True}, roman(WHITE, (110, 30, 110))),
    # --- the armies of the five empires (docs/Ejercitos.md): a soldier, an archer and a captain each
    "sulthari_soldier": ({"stern": True, "skin": 4, "hair": "black", "beard": "short", "hat": "bork", "scales": True, "plate": (170, 176, 186)},
                         lambda s, p: (body(s, p, "armor", (150, 30, 36), GOLD, (110, 30, 30)), limbs(s, p, {"color": (150, 30, 36), "cuff": GOLD, "bracer": STEEL},
                                                                                                       {"color": (40, 50, 110), "shoes": (140, 30, 30), "shoe_rows": 3}),
                                       long_skirt(s, p, (150, 30, 36), GOLD, split=True))),
    "sulthari_archer": ({"skin": 3, "hair": "dark", "beard": "stubble", "hat": "turban", "hat_color": (150, 40, 40), "bandolier": LEATHER, "shirt": LINEN},
                        sulthari((176, 128, 64), GOLD, pants=(120, 40, 40), shoes=(110, 70, 40), kind="vest", sleeve=LINEN)),
    "sulthari_captain": ({"stern": True, "skin": 4, "hair": "black", "beard": "full", "hat": "helm", "hat_color": STEEL, "plume": (180, 30, 40), "scales": True,
                          "plate": GOLD, "cape": (130, 24, 32)},
                         lambda s, p: (body(s, p, "armor", (110, 22, 30), GOLD, GOLD), limbs(s, p, {"color": (110, 22, 30), "pauldron": GOLD, "bracer": GOLD},
                                                                                                {"color": (30, 36, 80), "shoes": (90, 20, 26), "shoe_rows": 3}),
                                       long_skirt(s, p, (110, 22, 30), GOLD, split=True))),
    "nordrath_soldier": ({"stern": True, "skin": 1, "hair": "blonde", "beard": "full", "eyes": "blue", "hat": "helm", "hat_color": IRON, "plate": IRON, "fur_collar": True},
                         lambda s, p: (body(s, p, "armor", (56, 74, 110), None, LEATHER),
                                       limbs(s, p, {"color": (56, 74, 110), "bracer": IRON, "bare_from": 11, "fur": True}, {"color": (70, 60, 50), "shoes": (60, 44, 30), "wrap": True}))),
    "nordrath_archer": ({"skin": 1, "hair": "auburn", "style": "braids", "eyes": "green", "hat": "fur_hood", "hat_color": (96, 74, 54), "bandolier": LEATHER},
                        nordic((64, 84, 104), (120, 90, 60), pants=(80, 66, 50))),
    "nordrath_captain": ({"stern": True, "skin": 1, "hair": "red", "beard": "long", "braided_beard": True, "scar": True, "eyes": "grey", "hat": "helm", "hat_color": STEEL,
                          "plate": STEEL, "fur_collar": True, "cape": (36, 56, 110)},
                         lambda s, p: (body(s, p, "armor", (40, 52, 90), (200, 170, 90), LEATHER),
                                       limbs(s, p, {"color": (40, 52, 90), "pauldron": STEEL, "bracer": IRON, "fur": True}, {"color": (60, 52, 44), "shoes": (54, 40, 28), "wrap": True}))),
    "parsivan_soldier": ({"stern": True, "skin": 3, "hair": "black", "beard": "short", "hat": "helm", "hat_color": STEEL, "plume": (60, 170, 160), "scales": True, "plate": (110, 170, 170)},
                         lambda s, p: (body(s, p, "armor", (90, 40, 130), GOLD, (60, 40, 30)),
                                       limbs(s, p, {"color": (90, 40, 130), "pauldron": (110, 170, 170), "bracer": STEEL}, {"color": (60, 30, 80), "shoes": (50, 30, 40), "shoe_rows": 3}),
                                       long_skirt(s, p, (90, 40, 130), GOLD, split=True))),
    "parsivan_archer": ({"skin": 3, "hair": "dark", "eyes": "green", "hat": "hood", "hat_color": (50, 130, 130), "mask": (40, 110, 110), "bandolier": LEATHER},
                        parsivan((50, 130, 130), (110, 50, 150), pants=(60, 36, 80), kind="vest", sleeve=(80, 40, 110))),
    "parsivan_captain": ({"stern": True, "skin": 4, "hair": "black", "beard": "full", "hat": "helm", "hat_color": STEEL, "plume": (120, 50, 160), "scales": True,
                          "plate": GOLD, "cape": (70, 30, 110)},
                         lambda s, p: (body(s, p, "armor", (60, 130, 130), GOLD, GOLD),
                                       limbs(s, p, {"color": (60, 130, 130), "pauldron": GOLD, "bracer": GOLD}, {"color": (60, 30, 80), "shoes": (40, 24, 50), "shoe_rows": 3}),
                                       long_skirt(s, p, (70, 30, 110), GOLD, split=True))),
    "khemet_soldier": ({"stern": True, "skin": 5, "kohl": True, "hat": "nemes", "scales": True, "plate": (190, 140, 70)},
                       lambda s, p: (body(s, p, "armor", LINEN, None, GOLD), p.update(collar=True), body(s, p, "armor", LINEN, None, GOLD),
                                     limbs(s, p, {"color": SKIN[p["skin"]], "bare_from": 0, "bracer": GOLD}, {"color": LINEN, "bare": True, "kilt": 5, "sandals": True, "shoe_rows": 2}),
                                     kilt(s, p))),
    "khemet_archer": ({"skin": 6, "hair": "black", "style": "short", "kohl": True, "hat": "headband", "hat_color": (40, 70, 170), "collar": False, "bandolier": (150, 120, 70)},
                      khemet_man()),
    "khemet_captain": ({"stern": True, "skin": 5, "kohl": True, "hat": "nemes", "leopard": True, "scales": True, "plate": GOLD, "cape": (40, 70, 170)},
                       lambda s, p: (body(s, p, "armor", (40, 70, 170), None, GOLD), p.update(collar=True), body(s, p, "armor", (40, 70, 170), None, GOLD),
                                     limbs(s, p, {"color": SKIN[p["skin"]], "bare_from": 0, "bracer": GOLD, "pauldron": GOLD},
                                           {"color": LINEN, "bare": True, "kilt": 5, "sandals": True, "greave": GOLD, "shoe_rows": 2}),
                                     kilt(s, p, LINEN, GOLD))),
    "aureum_soldier": ({"stern": True, "skin": 2, "hair": "dark", "beard": "stubble", "hat": "helm", "hat_color": STEEL, "cheeks": True, "crest": (190, 30, 30), "bands": True,
                        "plate": (186, 190, 196)},
                       lambda s, p: (body(s, p, "armor", (170, 34, 38), None, LEATHER),
                                     limbs(s, p, {"color": (170, 34, 38), "bare_from": 4, "pauldron": (186, 190, 196), "bracer": LEATHER},
                                           {"color": (170, 34, 38), "bare": True, "kilt": 3, "sandals": True, "shoe_rows": 3}),
                                     kilt(s, p, (170, 34, 38), LEATHER),
                                     [s.paint("over", part, "front", lambda x, y, w, h: LEATHER if y < 3 and x % 2 == 0 else None) for part in ("rleg", "lleg")])),
    "aureum_archer": ({"skin": 2, "hair": "brown", "style": "curly", "hat": "helm", "hat_color": (150, 120, 80), "bandolier": LEATHER},
                      roman((150, 40, 40), LEATHER, legs="tunic", shoes=LEATHER)),
    "aureum_captain": ({"stern": True, "skin": 2, "hair": "grey", "beard": "short", "scar": True, "hat": "helm", "hat_color": STEEL, "cheeks": True, "crest": (230, 220, 200),
                        "bands": True, "plate": GOLD, "cape": (150, 26, 30)},
                       lambda s, p: (body(s, p, "armor", (150, 26, 30), None, GOLD),
                                     limbs(s, p, {"color": (150, 26, 30), "bare_from": 4, "pauldron": GOLD, "bracer": GOLD},
                                           {"color": (150, 26, 30), "bare": True, "kilt": 3, "sandals": True, "greave": GOLD, "shoe_rows": 3}),
                                     kilt(s, p, (150, 26, 30), GOLD))),
    "aurelion_sculptor": ({"skin": 2, "hair": "dark", "style": "curly", "beard": "full", "apron": (210, 206, 200), "dust": True}, roman((150, 146, 140), (120, 116, 110))),
}



# ------------------------------------------------------------------------------------------------ the 3D parts
# NpcAccessoryLayer's boxes on a 64x64 texture: (u, v) origin and (w, h, d) size, box UV like the player model
BOXES = {"turban": ((0, 0), (10, 5, 10)), "turban_top": ((0, 15), (8, 2, 8)), "beard": ((40, 0), (7, 8, 1)), "fez": ((40, 10), (6, 3, 6)),
         "cape": ((0, 26), (10, 21, 1)), "skirt": ((24, 26), (9, 12, 5))}
OUT3D = os.path.join(OUT, "3d")


class Box:
    def __init__(self, img, rnd):
        self.img, self.rnd = img, rnd

    def paint(self, box, side, fn):
        x0, y0, w, h = faces(*BOXES[box])[side]
        for y in range(h):
            for x in range(w):
                c = fn(x, y, w, h)
                if c is not None:
                    self.img.putpixel((x0 + x, y0 + y), clamp(c) + (255,))

    def around(self, box, fn):
        for side in ("front", "right", "left", "back"):
            self.paint(box, side, lambda x, y, w, h, sd=side: fn(x, y, sd, w, h))


def accessories(name, p):
    """Which 3D parts the person wears, and their texture."""
    parts = []
    hat = p.get("hat")
    if hat in ("turban", "tall_turban", "grand_turban"):
        parts.append("turban")
    if p.get("beard") == "long" and not p.get("mask"):  # a long beard hangs over the chest; a mask hides it
        parts.append("beard")
    if hat == "fez":
        parts.append("fez")
    if p.get("cape"):
        parts.append("cape")
    if p.get("long") and p.get("kind") in ("kaftan", "robe", "dress", "toga", "tunic"):
        parts.append("skirt")
    if not parts:
        return parts
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    b = Box(img, random.Random(len(name) * 31 + sum(map(ord, name))))
    weave = lambda c, x, y, f=0.05: sh(c, 1 + b.rnd.uniform(-f, f) - (f * 0.6 if (x + y) % 2 == 0 else 0))
    style = p.get("band", "checker")
    main = p.get("main", WHITE)

    if "turban" in parts:
        c = p.get("hat_color", WHITE)
        grand = hat == "grand_turban"
        jewel = p.get("jewel", (190, 20, 30))

        def wrap(x, y, side, w, h):
            if grand and y < 2:  # the crown of colour worked in gold over the white wraps
                return GOLD if (x % 4 == 0 and y == 1) or (x % 4 == 2 and y == 0) else sh(c, 1.06 if (x + y) % 2 else 0.9)
            base = WHITE if grand else c
            return sh(base, 1.06 if (x + 2 * y) % 5 < 2 else 0.84 if (x + 2 * y) % 5 == 4 else 0.96)  # wound slantwise
        b.around("turban", wrap)
        b.paint("turban", "top", lambda x, y, w, h: sh(WHITE if grand else c, 0.95 + 0.08 * ((x + y) % 2)))
        b.paint("turban", "bottom", lambda x, y, w, h: sh(WHITE if grand else c, 0.7))
        if grand or hat == "tall_turban":  # the jewel at the front, set in gold, with its plume
            b.paint("turban", "front", lambda x, y, w, h: (jewel if (x, y) in ((4, 2), (5, 2)) else mix(jewel, WHITE, 0.4) if (x, y) == (4, 1)
                                                           else GOLD if (3 <= x <= 6 and y == 3) or (x in (3, 6) and 1 <= y <= 3) or (y == 1 and x == 5) else None))
        top_c = c if grand or hat == "tall_turban" else c
        b.around("turban_top", lambda x, y, side, w, h: GOLD if (x + y) % 3 == 0 else sh(top_c, 1.0 + 0.08 * (x % 2)))
        b.paint("turban_top", "top", lambda x, y, w, h: (WHITE if 3 <= x <= 4 and 3 <= y <= 4 else
                                                        GOLD if (x in (1, 6) or y in (1, 6)) and (x + y) % 2 == 0 else sh(top_c, 1.0 + 0.06 * ((x + y) % 2))))

    if "beard" in parts:
        bc = HAIR.get(p.get("beard_color", p.get("hair", "grey")))
        long = p.get("beard") == "long"

        def strand(x, y, w, h):
            if not long and y > 3:
                return None
            if y == h - 1 and x in (0, w - 1):
                return None  # a point at the bottom
            return sh(bc, 1.12 if (x + y) % 3 == 0 else 0.86 if x % 2 else 1.0)
        b.paint("beard", "front", strand)
        b.paint("beard", "back", lambda x, y, w, h: sh(bc, 0.75) if long or y <= 3 else None)
        b.paint("beard", "right", lambda x, y, w, h: sh(bc, 0.9) if long or y <= 3 else None)
        b.paint("beard", "left", lambda x, y, w, h: sh(bc, 0.9) if long or y <= 3 else None)
        b.paint("beard", "bottom", lambda x, y, w, h: sh(bc, 0.8))
        b.paint("beard", "top", lambda x, y, w, h: sh(bc, 0.95))

    if "fez" in parts:
        fc = p.get("hat_color", (170, 30, 36))
        b.around("fez", lambda x, y, side, w, h: sh(fc, 1.08 if y == 0 else 0.78 if y == 2 else 0.96 - 0.04 * (x % 2)))
        b.paint("fez", "top", lambda x, y, w, h: (24, 22, 22) if (x, y) in ((2, 2), (3, 2), (3, 3)) else sh(fc, 0.9 + 0.06 * ((x + y) % 2)))
        b.paint("fez", "back", lambda x, y, w, h: (24, 22, 22) if x == 2 else None)  # the tassel falls down the back
        b.paint("fez", "bottom", lambda x, y, w, h: sh(fc, 0.6))

    if "cape" in parts:
        cc = p["cape"]

        def capef(x, y, w, h):
            if x in (0, w - 1) or y >= h - 2:
                return band(style, x, y, cc, p.get("band_accent"))
            return weave(sh(cc, 0.86 if x % 3 == 1 else 1.04 if x % 3 == 0 else 1.0), x, y)
        b.paint("cape", "back", capef)
        b.paint("cape", "front", lambda x, y, w, h: sh(cc, 0.6))
        b.paint("cape", "right", lambda x, y, w, h: band(style, x, y, cc) if y >= h - 2 else sh(cc, 0.85))
        b.paint("cape", "left", lambda x, y, w, h: band(style, x, y, cc) if y >= h - 2 else sh(cc, 0.85))
        b.paint("cape", "top", lambda x, y, w, h: band(style, x, y, cc))
        b.paint("cape", "bottom", lambda x, y, w, h: sh(cc, 0.6))

    if "skirt" in parts:
        notable = p.get("notable")

        def skirtf(x, y, side, w, h):
            if y >= h - 3 and y <= h - 2:
                return band(style, x, y, main, p.get("band_accent"))  # the embroidered hem
            if y == h - 1:
                return sh(main, 0.6)
            if side == "front" and x in (w // 2, w // 2 + 1) and y < h - 3:  # the opening
                return GOLD if notable and x == w // 2 else sh(main, 0.62 if not notable else 0.8)
            return weave(sh(main, 0.84 if x % 3 == 2 else 1.06 if x % 3 == 0 else 1.0), x, y)
        b.around("skirt", skirtf)
        b.paint("skirt", "top", lambda x, y, w, h: sh(main, 0.8))
        b.paint("skirt", "bottom", lambda x, y, w, h: sh(main, 0.5))
    os.makedirs(OUT3D, exist_ok=True)
    img.save(os.path.join(OUT3D, name + ".png"))
    return parts


def preview(names):
    """The front of every skin side by side, for a look before the game."""
    cols = 11
    sheet = Image.new("RGBA", (cols * 20, ((len(names) + cols - 1) // cols) * 36), (50, 50, 56, 255))
    for i, n in enumerate(names):
        img = Image.open(os.path.join(OUT, n + ".png"))
        fig = Image.new("RGBA", (16, 32), (0, 0, 0, 0))

        def blit(src, rect, at):
            x0, y0, w, h = rect
            fig.alpha_composite(src.crop((x0, y0, x0 + w, y0 + h)), at)
        for layer in (BASE, OVER):
            blit(img, faces(*layer["head"])["front"], (4, 0))
            blit(img, faces(*layer["body"])["front"], (4, 8))
            blit(img, faces(*layer["rarm"])["front"], (0, 8))
            blit(img, faces(*layer["larm"])["front"], (12, 8))
            blit(img, faces(*layer["rleg"])["front"], (4, 20))
            blit(img, faces(*layer["lleg"])["front"], (8, 20))
        sheet.alpha_composite(fig, ((i % cols) * 20 + 2, (i // cols) * 36 + 2))
    os.makedirs("build", exist_ok=True)
    sheet = sheet.resize((sheet.width * 4, sheet.height * 4), Image.NEAREST)
    sheet.save(os.path.join("build", "npc_skins_preview.png"))


if __name__ == "__main__":
    for cls in ("knight", "necromancer", "sorceress", "thief", "king"):
        outfit(cls)
    for name, (p, dress) in PEOPLE.items():
        person(name, dict(p), dress)
    import json
    listing = {}
    for name, p in WORN.items():
        parts = accessories(name, p)
        if parts:
            listing[name] = parts
    with open(os.path.join("src", "main", "resources", "assets", "sofe", "npc_accessories.json"), "w", encoding="utf-8") as f:
        json.dump(listing, f, indent=1, sort_keys=True)
    print("%d people wear 3D parts" % len(listing))
    preview(list(PEOPLE))
    print("%d skins" % len(PEOPLE))
