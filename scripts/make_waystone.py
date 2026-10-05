"""The Waystone's model and textures: a runed obelisk of dark sandstone on a stepped plinth, its runes and the
aetherium crystal floating over its cap lit from within (Forge's per-element light, so they glow at night), drawn
at 32x32. The block itself adds the motes that rise into the crystal (WaystoneBlock.animateTick).

Run from the repository root: python scripts/make_waystone.py
"""
import json
import math
import os
import random

from PIL import Image

ASSETS = os.path.join("src", "main", "resources", "assets", "sofe")
TEX = os.path.join(ASSETS, "textures", "block")
MODEL = os.path.join(ASSETS, "models", "block", "waystone.json")

STONE = [(54, 46, 44), (78, 66, 60), (104, 90, 80), (130, 114, 100)]
GLOW = [(40, 200, 200), (120, 250, 240), (230, 255, 255)]
CRYSTAL = [(30, 120, 170), (60, 190, 230), (150, 240, 255), (240, 255, 255)]


def stone_tex(name, seed, runes=False, top=False):
    rnd = random.Random(seed)
    img = Image.new("RGBA", (32, 32))
    px = img.load()
    for y in range(32):
        for x in range(32):
            i = 1 + (rnd.random() < 0.25) - (rnd.random() < 0.15)
            if not top and (y % 11 == 0):
                i = 0  # courses of the stone
            if not top and (x in (0, 31) or y in (0, 31)):
                i = 0
            px[x, y] = STONE[max(0, min(3, i))] + (255,)
    if top:
        for y in range(32):
            for x in range(32):
                d = max(abs(x - 15.5), abs(y - 15.5))
                if 9 < d < 11:
                    px[x, y] = STONE[3] + (255,)
    img.save(os.path.join(TEX, name + ".png"))


GLYPHS = ["x.x/xxx/.x.", "xxx/x../xxx", ".x./xxx/x.x", "x../xxx/..x", "xx./.x./.xx", "x.x/.x./x.x"]


def rune_tex(name, seed):
    """Transparent but for the runes: a column of glyphs down the middle of each face, and a band round the top."""
    rnd = random.Random(seed)
    img = Image.new("RGBA", (32, 32), (0, 0, 0, 0))
    px = img.load()
    for row in range(4):
        glyph = GLYPHS[rnd.randrange(len(GLYPHS))].split("/")
        for dy, line in enumerate(glyph):
            for dx, ch in enumerate(line):
                if ch == "x":
                    for sx in range(2):
                        for sy in range(2):
                            x, y = 13 + dx * 2 + sx, 4 + row * 7 + dy * 2 + sy
                            px[x, y] = (GLOW[1] if (sx + sy) % 2 == 0 else GLOW[0]) + (255,)
    for x in range(2, 30):
        if x % 3 != 0:
            px[x, 1] = GLOW[0] + (255,)
    img.save(os.path.join(TEX, name + ".png"))


def crystal_tex(name):
    img = Image.new("RGBA", (16, 16))
    px = img.load()
    for y in range(16):
        for x in range(16):
            d = (x + (15 - y)) / 30
            c = CRYSTAL[min(3, int(d * 4))]
            if x in (0, 15) or y in (0, 15):
                c = CRYSTAL[0]
            if x == y or x == 15 - y:
                c = CRYSTAL[2]
            px[x, y] = c + (255,)
    px[4, 4] = CRYSTAL[3] + (255,)
    px[5, 4] = CRYSTAL[3] + (255,)
    img.save(os.path.join(TEX, name + ".png"))


def box(frm, to, tex, top=None, light=False, rotation=None, uv_scale=1.0):
    faces = {}
    for f in ("north", "south", "east", "west"):
        faces[f] = {"texture": tex, "uv": [0, 0, 16, 16]}
    faces["up"] = {"texture": top or tex}
    faces["down"] = {"texture": top or tex}
    e = {"from": frm, "to": to, "faces": faces}
    if light:
        e["forge_data"] = {"block_light": 15, "sky_light": 15}
        e["shade"] = False
    if rotation:
        e["rotation"] = rotation
    return e


def model():
    elements = [
        box([1, 0, 1], [15, 3, 15], "#side", "#top"),
        box([2, 3, 2], [14, 5, 14], "#side", "#top"),
        box([4, 5, 4], [12, 24, 12], "#side", "#top"),
        # the runes: a skin a hair outside the shaft, lit
        {"from": [3.95, 5, 3.95], "to": [12.05, 24, 12.05], "forge_data": {"block_light": 15, "sky_light": 15}, "shade": False,
         "faces": {f: {"texture": "#runes", "uv": [0, 0, 16, 16]} for f in ("north", "south", "east", "west")}},
        box([3, 24, 3], [13, 26, 13], "#side", "#top"),
        box([6.5, 27, 6.5], [9.5, 32, 9.5], "#crystal", light=True, rotation={"angle": 45, "axis": "y", "origin": [8, 29, 8]}),
    ]
    return {
        "parent": "block/block",
        "render_type": "minecraft:cutout",
        "ambientocclusion": False,
        "textures": {"particle": "sofe:block/waystone", "side": "sofe:block/waystone", "top": "sofe:block/waystone_top",
                     "runes": "sofe:block/waystone_runes", "crystal": "sofe:block/waystone_crystal"},
        "elements": elements,
        "display": {
            "gui": {"rotation": [30, 225, 0], "translation": [0, -2, 0], "scale": [0.42, 0.42, 0.42]},
            "ground": {"translation": [0, 3, 0], "scale": [0.25, 0.25, 0.25]},
            "fixed": {"scale": [0.5, 0.5, 0.5]},
            "thirdperson_righthand": {"rotation": [75, 45, 0], "translation": [0, 2.5, 0], "scale": [0.3, 0.3, 0.3]},
            "firstperson_righthand": {"rotation": [0, 45, 0], "scale": [0.3, 0.3, 0.3]},
        },
    }


if __name__ == "__main__":
    stone_tex("waystone", 3)
    stone_tex("waystone_top", 4, top=True)
    rune_tex("waystone_runes", 5)
    crystal_tex("waystone_crystal")
    with open(MODEL, "w", encoding="utf-8") as f:
        json.dump(model(), f, indent=2)
        f.write("\n")
    print("waystone model and textures")
