"""16x16 sprites for the Sprint 7 crafting items (in the user's approved item style: a dark 1-pixel outline, strong
shading with a light and a dark edge, saturated gem colours and bright highlights): the seven sin gems rough, cut
and as Oath Gems, Moonsilk, Sunreed Papyrus, Void Crystal and Void Ink, and the faces of the Jeweler's Bench, the
Purifier and the Tempering Anvil.

Run from the repository root: python scripts/make_sprint7_sprites.py
"""
import os
import random

from PIL import Image

ASSETS = os.path.join("src", "main", "resources", "assets", "sofe", "textures")
ITEM, BLOCK = os.path.join(ASSETS, "item"), os.path.join(ASSETS, "block")

GEMS = {"wrath_ruby": (216, 36, 58), "lust_amethyst": (192, 80, 224), "greed_topaz": (240, 176, 48), "sloth_moonstone": (184, 208, 240),
        "gluttony_amber": (224, 120, 24), "envy_emerald": (48, 208, 112), "pride_sunstone": (255, 224, 112)}
OUTLINE = (24, 16, 22, 255)
GOLD = [(120, 76, 18), (200, 150, 50), (250, 214, 120), (255, 246, 200)]


def clamp(c):
    return tuple(max(0, min(255, int(v))) for v in c[:3]) + (255,)


def mul(c, f):
    return clamp([v * f for v in c[:3]])


def mix(a, b, t):
    return clamp([a[i] + (b[i] - a[i]) * t for i in range(3)])


def from_map(rows, palette):
    img = Image.new("RGBA", (16, 16), (0, 0, 0, 0))
    for y, row in enumerate(rows):
        for x, ch in enumerate(row):
            if ch in palette:
                img.putpixel((x, y), palette[ch])
    return img


def outline(img, color=OUTLINE):
    """A dark 1-pixel outline round every opaque pixel."""
    out = img.copy()
    for y in range(16):
        for x in range(16):
            if img.getpixel((x, y))[3]:
                continue
            for dx, dy in ((1, 0), (-1, 0), (0, 1), (0, -1)):
                if 0 <= x + dx < 16 and 0 <= y + dy < 16 and img.getpixel((x + dx, y + dy))[3]:
                    out.putpixel((x, y), color)
                    break
    return out


def save(img, folder, name):
    os.makedirs(folder, exist_ok=True)
    img.save(os.path.join(folder, name + ".png"))


# a brilliant-cut gem: crown facets on top, the table, the pavilion narrowing to the point
CUT = [
    "................",
    "................",
    "................",
    ".....TTTTTT.....",
    "....LTTTTTTD....",
    "...LLLTTTTDDD...",
    "..LLLLMMMMDDDD..",
    "..MMMMMMMMMMMM..",
    "...LMMMMMMMMD...",
    "....LMMMMMMD....",
    ".....LMMMMD.....",
    "......LMMD......",
    ".......MD.......",
    "................",
    "................",
    "................",
]
ROUGH = [
    "................",
    "................",
    "................",
    "......ss........",
    "....sssGGs......",
    "...ssGGGLGs.....",
    "..sssGLGGGss....",
    "..ssGGGGDGsss...",
    "..sssGGDDssss...",
    "...ssssssGGss...",
    "...sssssGLGGs...",
    "....sssssGDs....",
    ".....sssssss....",
    "................",
    "................",
    "................",
]


def gem_palette(c):
    return {"T": mix(c, (255, 255, 255), 0.45), "L": mix(c, (255, 255, 255), 0.25), "M": clamp(c), "D": mul(c, 0.55),
            "G": clamp(c), "s": (120, 112, 104, 255)}


def cut_gem(name, c, oath=False):
    img = from_map(CUT, gem_palette(c))
    img.putpixel((6, 4), (255, 255, 255, 255))     # the sparkle on the table
    img.putpixel((5, 5), (255, 255, 255, 255))
    if oath:   # an Oath Gem: set in a gold claw setting with a glow round it
        for x, y in ((2, 7), (13, 7), (7, 12), (8, 12), (5, 3), (10, 3)):
            img.putpixel((x, y), GOLD[2] + (255,))
        for x, y in ((1, 7), (14, 7), (7, 13), (8, 13)):
            img.putpixel((x, y), GOLD[1] + (255,))
        img = outline(img)
        glow = mix(c, (255, 255, 255), 0.6)
        for x, y in ((7, 1), (8, 1), (0, 8), (15, 8), (3, 2), (12, 2), (2, 13), (13, 13)):
            img.putpixel((x, y), glow[:3] + (200,))
        return save(img, ITEM, name)
    save(outline(img), ITEM, name)


def rough_gem(name, c, seed):
    rnd = random.Random(seed)
    pal = gem_palette(c)
    img = from_map(ROUGH, pal)
    for y in range(16):     # the stone speckled
        for x in range(16):
            p = img.getpixel((x, y))
            if p[:3] == pal["s"][:3]:
                img.putpixel((x, y), mul(p, rnd.uniform(0.82, 1.12)))
    img.putpixel((7, 6), (255, 255, 255, 255))
    save(outline(img), ITEM, name)


def moonsilk():
    """A folded bolt of silver-blue silk with a moon sheen."""
    base = (196, 210, 236)
    rows = ["................", "................", "................", "...aaaaaaaaaa...", "..abbbbbbbbbba..", "..accccccccccb..",
            "..abbbbbbbbbba..", "..accccccccccb..", "..abbbbbbbbbba..", "..accccccccccb..", "..abbbbbbbbbba..", "...dddddddddd...",
            "................", "................", "................", "................"]
    img = from_map(rows, {"a": mix(base, (255, 255, 255), 0.4), "b": clamp(base), "c": mul(base, 0.85), "d": mul(base, 0.6)})
    for x, y in ((4, 4), (5, 4), (9, 6), (11, 8)):
        img.putpixel((x, y), (255, 255, 255, 255))
    for x, y in ((12, 3), (13, 4)):   # a crescent stitched in silver
        img.putpixel((x, y), (240, 240, 255, 255))
    save(outline(img), ITEM, "moonsilk")


def papyrus():
    """A roll of papyrus, tan, with a gold sun painted on the open sheet."""
    rows = ["................", "................", "..rr............", ".rRRpppppppppp..", ".rRRpppppppppp..", ".rRRppppSSpppp..",
            ".rRRpppSSSSppp..", ".rRRpppSSSSppp..", ".rRRppppSSpppp..", ".rRRpppppppppp..", ".rRRpqpqpqpqpp..", "..rr............",
            "................", "................", "................", "................"]
    img = from_map(rows, {"r": (150, 112, 60, 255), "R": (196, 156, 96, 255), "p": (226, 204, 150, 255), "q": (196, 172, 120, 255),
                          "S": (232, 170, 40, 255)})
    save(outline(img), ITEM, "sunreed_papyrus")


def void_crystal():
    rows = ["................", "........L.......", ".......LMD......", "...L...LMD......", "..LMD.LMMD..L...", "..LMD.LMMD.LMD..",
            "..LMMDLMMD.LMD..", "...LMDLMMDLMMD..", "...LMMLMMMLMD...", "....LMMMMMMD....", "....dddddddd....", "................",
            "................", "................", "................", "................"]
    c = (150, 70, 220)
    img = from_map(rows, {"L": mix(c, (255, 255, 255), 0.4), "M": clamp(c), "D": mul(c, 0.5), "d": (40, 30, 50, 255)})
    for x, y in ((8, 2), (3, 4), (12, 5)):
        img.putpixel((x, y), (255, 235, 255, 255))
    save(outline(img), ITEM, "void_crystal")


def void_ink():
    rows = ["................", "................", "......cc........", "......cc........", ".....gGGg.......", "....gGGGGg......",
            "...gGvvvvGg.....", "...gvVvvVvg.....", "...gvvVVvvg.....", "...gvVvvvvg.....", "...gvvvvVvg.....", "....gvvvvg......",
            ".....gggg.......", "................", "................", "................"]
    img = from_map(rows, {"c": (110, 80, 50, 255), "g": (60, 60, 80, 255), "G": (150, 160, 190, 255), "v": (40, 10, 60, 255),
                          "V": (170, 90, 240, 255)})
    save(outline(img), ITEM, "void_ink")


# ------------------------------------------------------------------------------------------------ station faces

def noise_tex(base, spread, seed):
    rnd = random.Random(seed)
    img = Image.new("RGBA", (16, 16))
    for y in range(16):
        for x in range(16):
            img.putpixel((x, y), mul(base, rnd.uniform(1 - spread, 1 + spread)))
    return img


def frame(img, color, light=None):
    for i in range(16):
        for x, y in ((i, 0), (0, i), (i, 15), (15, i)):
            img.putpixel((x, y), color)
    if light:
        for i in range(1, 15):
            img.putpixel((i, 1), light)


def stations():
    wood, dark = (110, 70, 40), (60, 36, 20, 255)
    # the Jeweler's Bench: green velvet top with a loupe and a gem; carved wood sides with drawers and gold pulls
    top = noise_tex((40, 100, 60), 0.06, 1)
    frame(top, GOLD[1] + (255,))
    for x, y in ((4, 4), (5, 4), (4, 5), (5, 5)):
        top.putpixel((x, y), (200, 220, 240, 255))      # the loupe's glass
    top.putpixel((6, 6), (40, 40, 40, 255))
    top.putpixel((7, 7), (40, 40, 40, 255))
    for (x, y), c in zip(((10, 9), (11, 9), (10, 10), (11, 10)), ((255, 120, 140), (216, 36, 58), (150, 20, 40), (216, 36, 58))):
        top.putpixel((x, y), c + (255,))
    save(top, BLOCK, "jeweler_top")
    side = noise_tex(wood, 0.08, 2)
    frame(side, dark, mul(wood, 1.3))
    for y0 in (4, 10):
        for x in range(2, 14):
            side.putpixel((x, y0), dark)
            side.putpixel((x, y0 + 4), dark)
        for y in range(y0, y0 + 5):
            side.putpixel((2, y), dark)
            side.putpixel((13, y), dark)
        side.putpixel((7, y0 + 2), GOLD[2] + (255,))
        side.putpixel((8, y0 + 2), GOLD[2] + (255,))
    save(side, BLOCK, "jeweler_side")
    bottom = noise_tex(mul(wood, 0.7), 0.06, 3)
    save(bottom, BLOCK, "jeweler_bottom")
    # the Purifier: a furnace of pale stone and gold, a white-violet fire behind a grille
    stone = (170, 166, 160)
    side = noise_tex(stone, 0.06, 4)
    frame(side, (80, 76, 72, 255), mul(stone, 1.2))
    for y in range(6, 13):
        for x in range(4, 12):
            t = (y - 6) / 6
            side.putpixel((x, y), mix((255, 250, 255), (170, 90, 240), t))
    for x in range(4, 12, 2):
        for y in range(6, 13):
            side.putpixel((x, y), (60, 50, 40, 255))
    for x in range(3, 13):
        side.putpixel((x, 5), GOLD[1] + (255,))
        side.putpixel((x, 13), GOLD[1] + (255,))
    save(side, BLOCK, "purifier_side")
    top = noise_tex(stone, 0.06, 5)
    frame(top, (80, 76, 72, 255))
    for x in range(5, 11):
        for y in range(5, 11):
            top.putpixel((x, y), (40, 30, 50, 255) if (x + y) % 2 else (200, 150, 255, 255))
    save(top, BLOCK, "purifier_top")
    save(noise_tex(mul(stone, 0.7), 0.06, 6), BLOCK, "purifier_bottom")
    # the Tempering Anvil: blue-black steel, a polished face, runes that glow ice blue
    steel = (60, 66, 80)
    top = noise_tex((150, 156, 170), 0.05, 7)
    frame(top, (40, 44, 54, 255))
    for x in range(2, 14):
        top.putpixel((x, 3), (210, 216, 230, 255))
    for x, y in ((7, 7), (8, 8), (9, 7), (6, 8)):
        top.putpixel((x, y), (120, 200, 255, 255))
    save(top, BLOCK, "tempering_anvil_top")
    side = noise_tex(steel, 0.08, 8)
    frame(side, (24, 26, 32, 255), mul(steel, 1.5))
    for x in range(1, 15):     # the anvil's horn and face across the top of the side
        side.putpixel((x, 2), (150, 156, 170, 255))
        side.putpixel((x, 3), (110, 116, 130, 255))
    for x, y in ((4, 8), (5, 9), (6, 8), (9, 10), (10, 9), (11, 10), (7, 12), (8, 12)):
        side.putpixel((x, y), (120, 200, 255, 255))
    save(side, BLOCK, "tempering_anvil_side")
    save(noise_tex(mul(steel, 0.7), 0.06, 9), BLOCK, "tempering_anvil_bottom")


def void_crystal_ore():
    """End stone with clusters of violet crystal breaking through it."""
    img = noise_tex((222, 222, 168), 0.06, 10)
    rnd = random.Random(11)
    for _ in range(14):   # the pocks of end stone
        x, y = rnd.randrange(16), rnd.randrange(16)
        img.putpixel((x, y), (190, 190, 140, 255))
    c = (150, 70, 220)
    for cx, cy in ((4, 5), (11, 4), (7, 11), (12, 12)):
        for dx, dy, col in ((0, 0, mix(c, (255, 255, 255), 0.45)), (1, 0, clamp(c)), (0, 1, clamp(c)), (1, 1, mul(c, 0.55)),
                            (-1, 0, mul(c, 0.4)), (0, -1, mix(c, (255, 255, 255), 0.2))):
            if 0 <= cx + dx < 16 and 0 <= cy + dy < 16:
                img.putpixel((cx + dx, cy + dy), col)
    save(img, BLOCK, "void_crystal_ore")


if __name__ == "__main__":
    for i, (gem, c) in enumerate(GEMS.items()):
        rough_gem("rough_" + gem, c, i)
        cut_gem("cut_" + gem, c)
        cut_gem("oath_" + gem, c, oath=True)
    moonsilk()
    papyrus()
    void_crystal()
    void_ink()
    stations()
    void_crystal_ore()
    print("sprint 7 sprites written")
