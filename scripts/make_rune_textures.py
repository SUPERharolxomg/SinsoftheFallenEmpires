"""Textures of the rune puzzles: the Rune Stone (one face per rune, dark and burning, and its top) and the Riddle Tablet.

16x16, drawn pixel by pixel: dark carved stone with violet veins of the Void, the rune cut into it; burning, the cut
glows gold. Writes textures/block/rune_stone_<rune>[_lit].png, rune_stone_top.png, riddle_tablet_{side,top,bottom}.png
and a preview in build/rune_preview.png.

Run from the repository root: python scripts/make_rune_textures.py
"""
import os
import random

from PIL import Image

OUT = os.path.join("src", "main", "resources", "assets", "sofe", "textures", "block")

# each rune on the 10x10 middle of the face, '#' carved
RUNES = {
    "sun": ["....##....",
            ".#..##..#.",
            "..#....#..",
            "...####...",
            "##.#..#.##",
            "##.#..#.##",
            "...####...",
            "..#....#..",
            ".#..##..#.",
            "....##...."],
    "moon": ["...####...",
             "..##......",
             ".##.......",
             "##........",
             "##........",
             "##........",
             "##........",
             ".##.......",
             "..##......",
             "...####..."],
    "star": ["....##....",
             "....##....",
             "...####...",
             "##########",
             ".########.",
             "..######..",
             "..##..##..",
             ".##....##.",
             ".#......#.",
             ".........."],
    "flame": ["....#.....",
              "....##....",
              "...###....",
              "...####...",
              "..##.##...",
              "..#..###..",
              ".##...##..",
              ".##...##..",
              "..##.##...",
              "...###...."],
    "wave": [".........."
             , ".##....##."
             , "#..#..#..#"
             , "....##...."
             , ".........."
             , ".##....##."
             , "#..#..#..#"
             , "....##...."
             , ".........."
             , ".........."],
    "eye": ["..........",
            "...####...",
            ".##....##.",
            "#...##...#",
            "#..####..#",
            "#...##...#",
            ".##....##.",
            "...####...",
            "..........",
            ".........."],
}

STONE = (52, 48, 60)
GOLD, GOLD_HOT, GOLD_DIM = (232, 182, 74), (255, 236, 160), (150, 110, 40)
VIOLET = (110, 70, 150)


def stone_base(seed, frame=True):
    rnd = random.Random(seed)
    img = Image.new("RGBA", (16, 16))
    for y in range(16):
        for x in range(16):
            v = rnd.randint(-7, 7)
            c = tuple(max(0, min(255, s + v)) for s in STONE)
            if frame and (x in (0, 15) or y in (0, 15)):
                c = tuple(int(s * 0.7) for s in STONE)
            elif frame and (x in (1, 14) or y in (1, 14)):
                c = tuple(min(255, int(s * 1.18)) for s in STONE)
            img.putpixel((x, y), c + (255,))
    for _ in range(3):  # veins of the Void in the stone
        x, y = rnd.randint(2, 13), rnd.randint(2, 13)
        for _ in range(4):
            img.putpixel((x, y), VIOLET + (255,))
            x = max(2, min(13, x + rnd.choice((-1, 0, 1))))
            y = max(2, min(13, y + 1))
    return img


def rune_face(name, lit):
    img = stone_base(name)
    glyph = RUNES[name]
    for gy, row in enumerate(glyph):
        for gx, ch in enumerate(row):
            if ch != "#":
                continue
            x, y = gx + 3, gy + 3
            if lit:
                img.putpixel((x, y), (GOLD_HOT if (gx + gy) % 3 == 0 else GOLD) + (255,))
            else:
                img.putpixel((x, y), (24, 22, 30, 255))  # the cut, in shadow
                if y + 1 < 15 and glyph[gy + 1][gx] != "#" if gy + 1 < 10 else False:
                    img.putpixel((x, y + 1), (70, 66, 80, 255))  # its lower lip catches the light
    if lit:  # the glow spills round the cut
        base = img.copy()
        for y in range(2, 14):
            for x in range(2, 14):
                if base.getpixel((x, y))[:3] in (GOLD, GOLD_HOT):
                    continue
                near = any(base.getpixel((x + dx, y + dy))[:3] in (GOLD, GOLD_HOT)
                           for dx in (-1, 0, 1) for dy in (-1, 0, 1) if 0 <= x + dx < 16 and 0 <= y + dy < 16)
                if near:
                    r, g, b, a = base.getpixel((x, y))
                    img.putpixel((x, y), (int(r * 0.5 + GOLD_DIM[0] * 0.5), int(g * 0.5 + GOLD_DIM[1] * 0.5), int(b * 0.5 + GOLD_DIM[2] * 0.5), a))
    return img


def top():
    img = stone_base("top")
    for i in range(4, 12):  # a ring cut in the top
        for x, y in ((i, 4), (i, 11), (4, i), (11, i)):
            img.putpixel((x, y), (30, 28, 36, 255))
    img.putpixel((7, 7), VIOLET + (255,))
    img.putpixel((8, 8), VIOLET + (255,))
    return img


def tablet_side():
    img = stone_base("tablet", frame=True)
    rnd = random.Random(7)
    for y in (4, 6, 8, 10, 12):  # lines of an old script
        x = 3
        while x < 13:
            w = rnd.randint(1, 3)
            for k in range(w):
                if x + k < 13:
                    img.putpixel((x + k, y), (GOLD_DIM if rnd.random() < 0.35 else (26, 24, 32)) + (255,))
            x += w + 1
    return img


def tablet_top():
    img = stone_base("tablet_top")
    for x in range(3, 13):
        img.putpixel((x, 7), GOLD_DIM + (255,))
        img.putpixel((x, 8), GOLD_DIM + (255,))
    return img


def main():
    os.makedirs(OUT, exist_ok=True)
    faces = []
    for name in RUNES:
        for lit in (False, True):
            img = rune_face(name, lit)
            img.save(os.path.join(OUT, "rune_stone_%s%s.png" % (name, "_lit" if lit else "")))
            faces.append(img)
    top().save(os.path.join(OUT, "rune_stone_top.png"))
    tablet_side().save(os.path.join(OUT, "riddle_tablet_side.png"))
    tablet_top().save(os.path.join(OUT, "riddle_tablet_top.png"))
    stone_base("tablet_bottom").save(os.path.join(OUT, "riddle_tablet_bottom.png"))
    sheet = Image.new("RGBA", (16 * len(faces) + 32, 16), (40, 40, 40, 255))
    for i, f in enumerate(faces + [tablet_side(), top()]):
        sheet.paste(f, (16 * i, 0))
    os.makedirs("build", exist_ok=True)
    sheet.resize((sheet.width * 8, sheet.height * 8), Image.NEAREST).save(os.path.join("build", "rune_preview.png"))
    print("%d rune faces, the top and the tablet" % len(faces))


if __name__ == "__main__":
    main()
