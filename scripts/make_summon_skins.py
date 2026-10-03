"""Skins of the Bearers' summons (Sprint 6), 64x64 in the player layout: the Necromancer's Clay Warden (cracked
terracotta with a gold-painted face, on the vanilla iron golem), the
Sorceress's ice clone and the souls risen by the Great Judgment; the Bronze Cannon's model and texture, and the Royal Treasury coin.

The Janissary wears a skin the user chose (textures/entity/summon/janissary.png); it is not drawn here.

Run from the repository root: python scripts/make_summon_skins.py
"""
import json
import os

from PIL import Image

ASSETS = os.path.join("src", "main", "resources", "assets", "sofe")
OUT = os.path.join(ASSETS, "textures", "entity", "summon")
# box UV origins and sizes (w, h, d) of the player model parts
PARTS = {"head": ((0, 0), (8, 8, 8)), "body": ((16, 16), (8, 12, 4)), "right_arm": ((40, 16), (4, 12, 4)), "left_arm": ((32, 48), (4, 12, 4)),
         "right_leg": ((0, 16), (4, 12, 4)), "left_leg": ((16, 48), (4, 12, 4))}


def mix(a, b, t):
    return tuple(round(a[i] + (b[i] - a[i]) * t) for i in range(3))


def faces(uv, size):
    (u, v), (w, h, d) = uv, size
    return {"top": (u + d, v, w, d), "bottom": (u + d + w, v, w, d), "right": (u, v + d, d, h), "front": (u + d, v + d, w, h),
            "left": (u + d + w, v + d, d, h), "back": (u + d + w + d, v + d, w, h)}


def fill(img, rect, color, shade=True, noise=0):
    x0, y0, w, h = rect
    for y in range(h):
        for x in range(w):
            c = color
            if shade:
                c = mix(c, (255, 255, 255), 0.15) if y == 0 else mix(c, (0, 0, 0), 0.25) if y == h - 1 else c
            if noise and (x * 7 + y * 13) % noise == 0:
                c = mix(c, (0, 0, 0), 0.25)
            img.putpixel((x0 + x, y0 + y), c + (255,))


def paint(name, colors, extra):
    img = Image.new("RGBA", (64, 64), (0, 0, 0, 0))
    for part, (uv, size) in PARTS.items():
        for face, rect in faces(uv, size).items():
            fill(img, rect, colors[part], noise=colors.get("noise", 0))
    extra(img)
    os.makedirs(OUT, exist_ok=True)
    img.save(os.path.join(OUT, name + ".png"))


def px(img, pts, c):
    for (x, y) in pts:
        img.putpixel((x, y), tuple(c) + (255,))


def front(part):
    return faces(*PARTS[part])["front"]


def clay(img):
    fx, fy, _, _ = front("head")
    for y in range(8):  # the gold-painted face
        for x in range(1, 7):
            if y >= 2:
                img.putpixel((fx + x, fy + y), (226, 176, 56, 255))
    px(img, [(fx + 2, fy + 4), (fx + 5, fy + 4)], (20, 20, 30))
    px(img, [(fx + 3, fy + 6), (fx + 4, fy + 6)], (120, 60, 30))
    bx, by, bw, _ = front("body")
    for x in range(bw):
        img.putpixel((bx + x, by + 7), (50, 200, 190, 255))  # the turquoise belt
    for k in range(6):  # cracks
        img.putpixel((bx + 1 + k, by + 2 + (k % 3)), (100, 54, 30, 255))


def ice(img):
    fx, fy, _, _ = front("head")
    px(img, [(fx + 2, fy + 4), (fx + 5, fy + 4)], (40, 90, 160))
    for x in range(8):
        img.putpixel((fx + x, fy + 6), (150, 90, 200, 255))  # the veil
    bx, by, bw, _ = front("body")
    for k in range(bw):
        img.putpixel((bx + k, by + 3 + (k % 2)), (250, 255, 255, 255))


def risen(img):
    fx, fy, _, _ = front("head")
    px(img, [(fx + 2, fy + 4), (fx + 5, fy + 4)], (110, 250, 230))
    px(img, [(fx + 2, fy + 3), (fx + 5, fy + 3)], (20, 40, 40))
    bx, by, bw, bh = front("body")
    for y in range(1, bh - 1, 2):
        for x in range(1, bw - 1):
            if x != bw // 2:
                img.putpixel((bx + x, by + y), (60, 80, 80, 255))  # ribs


def cannon():
    """The cannon as an item model of cubes, and its texture: bronze barrel above, wood below."""
    tex = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            if y < 8:
                c = mix((200, 130, 50), (250, 200, 120), 0.4) if y in (1, 2) else (190, 120, 50) if y < 6 else (130, 80, 30)
            elif x < 8:
                c = (110, 70, 40) if (x + y) % 3 else (80, 50, 28)
            else:
                c = (60, 40, 26) if (x - 12) ** 2 + (y - 12) ** 2 > 6 else (40, 40, 46)
            tex.putpixel((x, y), c + (255,))
    tex.save(os.path.join(ASSETS, "textures", "item", "bronze_cannon_model.png"))

    def box(frm, to, uv, rot=None):
        e = {"from": frm, "to": to, "faces": {f: {"uv": uv, "texture": "#t"} for f in ("north", "south", "east", "west", "up", "down")}}
        if rot:
            e["rotation"] = rot
        return e
    barrel_rot = {"origin": [8, 6, 8], "axis": "x", "angle": -22.5}
    model = {"textures": {"t": "sofe:item/bronze_cannon_model", "particle": "sofe:item/bronze_cannon_model"},
             "elements": [box([3, 0, 3], [13, 3, 13], [0, 8, 8, 16]),
                          box([1, 0, 5], [3, 5, 10], [8, 8, 16, 16]), box([13, 0, 5], [15, 5, 10], [8, 8, 16, 16]),
                          box([6, 3, 1], [10, 7, 15], [0, 0, 16, 8], barrel_rot),
                          box([5.5, 2.5, 0], [10.5, 7.5, 2], [0, 0, 16, 4], barrel_rot)],
             "display": {"fixed": {"rotation": [0, 0, 0], "translation": [0, 0, 0], "scale": [1, 1, 1]}}}
    with open(os.path.join(ASSETS, "models", "item", "bronze_cannon_model.json"), "w", encoding="utf-8") as f:
        json.dump(model, f, indent=2)


def clay_golem():
    """The Clay Warden on the vanilla iron golem: iron turned to baked clay by brightness, the vines to turquoise
    wraps, the face painted gold with dark eyes. The vanilla texture is read from the Minecraft jar."""
    import io
    import zipfile
    jar = os.path.expanduser(os.path.join("~", ".gradle", "caches", "forge_gradle", "minecraft_repo", "versions", "1.20.1", "client-extra.jar"))
    img = Image.open(io.BytesIO(zipfile.ZipFile(jar).read("assets/minecraft/textures/entity/iron_golem/iron_golem.png"))).convert("RGBA")
    clay = [(70, 34, 20), (120, 62, 36), (170, 98, 60), (200, 130, 84), (230, 170, 120)]
    out = img.load()
    for y in range(img.height):
        for x in range(img.width):
            r, g, b, a = out[x, y]
            if not a:
                continue
            if g > r + 15 and g > b + 15:  # the vines
                out[x, y] = (40, 170, 160, a) if (x + y) % 3 else (30, 120, 120, a)
                continue
            lum = (0.299 * r + 0.587 * g + 0.114 * b) / 255
            c = clay[min(4, int(lum * 5))]
            if (x * 5 + y * 3) % 23 == 0:
                c = clay[0]  # cracks in the clay
            out[x, y] = c + (a,)
    for y in range(10):  # the gold face on the front of the head (box uv 0,0 8x10x8: front at 8,8)
        for x in range(8):
            out[8 + x, 8 + y] = ((226, 176, 56) if y > 1 else (190, 140, 40)) + (255,)
    for (x, y) in ((10, 12), (11, 12), (13, 12), (14, 12)):
        out[x, y] = (20, 16, 24, 255)
    for (x, y) in ((10, 11), (14, 11)):
        out[x, y] = (110, 250, 230, 255)  # the soul in the eyes
    for (x, y) in ((11, 16), (12, 16), (13, 16)):
        out[x, y] = (120, 70, 20, 255)
    img.save(os.path.join(OUT, "clay_golem.png"))


def embalmed_dead():
    """The Necromancer's Embalmed Dead on the vanilla husk: linen bands wound round the body, arms and legs,
    a gold collar, and the turquoise light of a bound soul in the eyes."""
    import io
    import zipfile
    jar = os.path.expanduser(os.path.join("~", ".gradle", "caches", "forge_gradle", "minecraft_repo", "versions", "1.20.1", "client-extra.jar"))
    img = Image.open(io.BytesIO(zipfile.ZipFile(jar).read("assets/minecraft/textures/entity/zombie/husk.png"))).convert("RGBA")
    px = img.load()
    linen = [(150, 138, 110), (196, 184, 152), (226, 216, 188), (240, 234, 214)]
    for y in range(16, 64):  # body, arms and legs (the head keeps its face)
        for x in range(img.width):
            r, g, b, a = px[x, y]
            if not a:
                continue
            band = (y + (x // 4)) % 4
            if band != 3:  # most of the body is wrapped; one row in four shows the dried skin
                lum = (r + g + b) / 765
                px[x, y] = linen[min(3, int(lum * 4.5))] + (a,)
            if (x * 7 + y * 3) % 17 == 0 and band != 3:
                px[x, y] = linen[0] + (a,)
    for x in range(16, 40):  # the gold collar along the top of the body
        for y in (20, 21):
            if px[x, y][3]:
                px[x, y] = ((232, 186, 70) if y == 20 else (170, 120, 30)) + (255,)
    for (x, y) in ((9, 12), (10, 12), (13, 12), (14, 12)):
        px[x, y] = (110, 250, 230, 255)
    for x in range(8, 16):  # a strip of linen across the brow
        px[x, 9] = linen[2] + (255,)
        px[x, 10] = linen[1] + (255,)
    img.save(os.path.join(OUT, "embalmed_dead.png"))


def royal_coin():
    """A gold coin of the Royal Treasury with an aetherium crystal set in it."""
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y in range(16):
        for x in range(16):
            d = ((x - 7.5) ** 2 + (y - 7.5) ** 2) ** 0.5
            if d <= 6.5:
                c = (255, 236, 150) if d < 3 and x + y < 15 else (232, 186, 60) if d < 5.5 else (150, 100, 20)
                if d >= 6:
                    c = (20, 14, 18)
                img.putpixel((x, y), c + (255,))
    for (x, y) in ((7, 6), (8, 6), (6, 7), (7, 7), (8, 7), (9, 7), (7, 8), (8, 8)):
        img.putpixel((x, y), (150, 230, 255, 255) if y < 7 else (90, 180, 230, 255))
    img.save(os.path.join(ASSETS, "textures", "item", "royal_coin.png"))


if __name__ == "__main__":
    royal_coin()
    clay_golem()
    embalmed_dead()
    paint("ice_clone", {"head": (190, 236, 250), "body": (150, 210, 240), "right_arm": (160, 220, 245), "left_arm": (160, 220, 245),
                        "right_leg": (140, 200, 236), "left_leg": (140, 200, 236)}, ice)
    paint("risen", {"head": (190, 200, 196), "body": (110, 130, 128), "right_arm": (150, 160, 156), "left_arm": (150, 160, 156),
                    "right_leg": (100, 116, 114), "left_leg": (100, 116, 114), "noise": 7}, risen)
    cannon()
    print("summon skins and the cannon")
